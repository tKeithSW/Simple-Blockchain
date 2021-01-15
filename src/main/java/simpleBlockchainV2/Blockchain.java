package simpleBlockchainV2;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.google.gson.*;

import java.util.Map;

public class Blockchain{
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public static int difficulty = 3;
	public static float minimumTransaction = 0.1f;
	public static Wallet myWallet;
	public static Wallet aliceWallet;
	public static Wallet bobWallet;
	public static Wallet coinbase;

	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(getGenesisTransactionHash(), getGenesisTransactionOutput());
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.transactionInputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.hash, output);
				}
				
				if(Utility.getPublicKeyFromString(currentTransaction.outputs.get(0).recipient) != Utility.getPublicKeyFromString(currentTransaction.getRecipient())) {
					System.out.println("recipient: " + Utility.getPublicKeyFromString(currentTransaction.outputs.get(0).recipient));
					System.out.println("Recipient: " + Utility.getPublicKeyFromString(currentTransaction.getRecipient()));
					System.out.println("#Transaction(" + t + ") output recipient is not who it should be");
					return false;
				}
				if( Utility.getPublicKeyFromString(currentTransaction.outputs.get(1).recipient) != Utility.getPublicKeyFromString(currentTransaction.getSender())) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain is valid");
		return true;
	}

	public static String getGenesisTransactionHash(){
		return blockchain.get(0).transactions.get(0).outputs.get(0).hash;
	}

	public static TransactionOutput getGenesisTransactionOutput(){
		return blockchain.get(0).transactions.get(0).outputs.get(0);
	}
	
	// Add block to blockchain arraylist
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}

	// Store genesis transactionOutputs in UTXO
	public static void storeGenesisTransactionInUTXO(){
		UTXOs.put(blockchain.get(0).transactions.get(0).outputs.get(0).hash, blockchain.get(0).transactions.get(0).outputs.get(0));
	}

	// Combine Wallet with blockchain
	public static String getOutputFormat(String name, String wallet, String blockchain){ 
		return "{" + "\n" 
				+ "  " + "\"name\": " + "\"" + name + "\"," + "\n"
				+ "  " + "\"wallet\": " + wallet + "," + "\n"
				+ "  " + "\"blockchain\": " + blockchain + "\n" 
				+ "}";
	}

    public static void main(String[] args) {
    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		// Deserialise Wallets
		myWallet = Utility.parseJsonToWallet("Wallet-my.json");
		aliceWallet = Utility.parseJsonToWallet("Wallet-alice.json");
		bobWallet = Utility.parseJsonToWallet("Wallet-bob.json");

		// Deserialise blockchain
		blockchain = Utility.parseJsonToBlockChain("blockchain.json");

		storeGenesisTransactionInUTXO();

		// Block 1
		Block block1 = new Block(blockchain.get(0).hash); // Create 1st Block
		block1.addTransaction(myWallet.sendFunds(aliceWallet.getStringPublicKey(), 10f)); // User sends 10 coins to Alice
		block1.addTransaction(myWallet.sendFunds(bobWallet.getStringPublicKey(), 50f)); // User sends 50 coins to Bob
		addBlock(block1); // Add Block1 to blockchain

		// Block 2
		Block block2 = new Block(block1.hash); // Create 2nd Block
		block2.addTransaction(aliceWallet.sendFunds(bobWallet.getStringPublicKey(), 20f)); // Alice sends 20 coins to Bob
		block2.addTransaction(bobWallet.sendFunds(myWallet.getStringPublicKey(), 30f)); // Bob sends 30 coins to User
		addBlock(block2); // Add Block2 to blockchain

		myWallet.getBalance(); // Update Wallet balance
		String myWalletJson = Utility.serialiseToJson(myWallet); // Seralise user Wallet to JSON
		String blockchainJson = Utility.serialiseToJson(blockchain); // Serialise Blockchain to JSON
		String commbinedBlockchain = getOutputFormat("Your Name", myWalletJson, blockchainJson);
		Utility.jsonStringToFile(commbinedBlockchain, "outputFormat.json");
	}
}
