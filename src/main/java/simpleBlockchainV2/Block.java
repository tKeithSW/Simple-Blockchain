package simpleBlockchainV2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Block{
    String hash;
    final String previousHash;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private final long timeStamp;
    private int nonce;
    private String merkleRoot;

    public Block(String previousHash){
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    // Calculate Block hash
    public String calculateHash(){
        String newHash = previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot;
        return Utility.applySha256(newHash);
    }

    // Get hash that is lower than the difficulty
    public void mineBlock(int difficulty){
        merkleRoot = getMerkleRoot(transactions);
		String target = new String(new char[difficulty]).replace('\0', '0');
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++;
			hash = calculateHash();
		}
		System.out.println("Block Mined!");
    }
    
    // Add Transaction to current block
    public boolean addTransaction(Transaction transaction) {
		if(transaction == null) return false;		
		if((previousHash != "0")) {
			if((transaction.processTransaction() != true)) { // If transaction is not valid
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}
		transactions.add(transaction);
		System.out.println("Transaction added to Block");
		return true;
    }
   
    // Get Merkle root by hashing transations together in Merkle tree
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
		int count = transactions.size();
		
		List<String> previousTreeLayer = new ArrayList<String>();
		for(Transaction transaction : transactions) {
			previousTreeLayer.add(transaction.hash);
		}
		List<String> treeLayer = previousTreeLayer;
		
		while(count > 1) {
			treeLayer = new ArrayList<String>();
			for(int i=1; i < previousTreeLayer.size(); i+=2) {
				treeLayer.add(Utility.applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
			}
			count = treeLayer.size();
			previousTreeLayer = treeLayer;
		}
		
		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		return merkleRoot;
	}
}
