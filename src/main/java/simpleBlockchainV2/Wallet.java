package simpleBlockchainV2;

import java.security.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class Wallet {
	private String privateKey;
	private String publicKey;
	private transient PublicKey _publicKey;
	private transient PrivateKey _privateKey;
	public float balance;
	public transient HashMap<String,TransactionOutput> UTXOs;// Unspent transaction outputs
	
	public Wallet(String privateKey, String publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}

	// Get wallet updated balance
	public float getBalance() {
		float total = 0;
		UTXOs = new HashMap<String,TransactionOutput>();
        for (Map.Entry<String, TransactionOutput> outputs: Blockchain.UTXOs.entrySet()){
        	TransactionOutput UTXO = outputs.getValue();
            if(UTXO.isMine(getPublicKey())) { //if output belongs to wallet
            	UTXOs.put(UTXO.hash,UTXO); //add to UTXO list
            	total += UTXO.value ; 
            }
		}
		balance = total;  
		return total;
	}
	
	// Create new transaction using recipient publicKey and amount to send
	public Transaction sendFunds(String recipient,float value ) {
		if(getBalance() < value) {
			System.out.println("Not Enough funds to send transaction!! Transaction Discarded.");
			return null;
		}
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.hash));
			if(total > value) break;
		}
		
		Transaction newTransaction = new Transaction(publicKey, recipient , value, inputs);
		newTransaction.generateSignature(getPrivateKey());
		
		for(TransactionInput input: inputs){
			UTXOs.remove(input.transactionOutputId);
		}
		return newTransaction;
	}

	public void generateKeysFromStrings(){
		if (getStringPrivateKey().equals(null) || getStringPublicKey().equals(null)) {
			System.out.println("PublicKey or PrivateKey string is NULL");
			System.out.println("Unable to geenrate key");
		} else {
			setPrivateKey(this.privateKey);
			setPublicKey(this.publicKey);
		}
	}

	// Set publicKey from hex String
	public void setPublicKey(String publicKeyString){
		_publicKey = Utility.getPublicKeyFromString(publicKey);
	}

	// Set privateKey from hex string
	public void setPrivateKey(String privateKeyString){
		_privateKey = Utility.getPrivateKeyFromString(privateKey);
	}

	public PublicKey getPublicKey(){
		return _publicKey;
	}

	public PrivateKey getPrivateKey(){
		return _privateKey;
	}
	
	public String getStringPublicKey() {
		return publicKey;
	}
	
	public String getStringPrivateKey() {
		return privateKey;
	}
}
