package simpleBlockchainV2;

import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class Transaction {

	//public String sender;
	private String sender;
	private String recipient;
	public float value;
	public String hash;
	public transient byte[] signatureInBytes;
	public String signature;

    
	public ArrayList<String> inputs = new ArrayList<String>();
	public transient ArrayList<TransactionInput> transactionInputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	private int sequence; // Number of generated transaction
	private static int seq = 1; // Set to 1 as genesis Block was given
	
	public Transaction(String from, String to, Float value,  ArrayList<TransactionInput> inputs) {

		sender = from;
		recipient = to;
		this.value = value;
		this.transactionInputs = inputs;
		sequence = seq;
	}
	
	// Process transaction - validate transaction, get inputs, create outputs, remove inputs from UTXO lists as spent  
	public boolean processTransaction() {
		
		if(verifySignature() == false) {
			System.out.println("Fail to verify transaction signature!");
			return false;
		}
				
		//Get transaction inputs
		for(TransactionInput i : transactionInputs) {
			i.UTXO = Blockchain.UTXOs.get(i.transactionOutputId);
			inputs.add(i.getTransactionOutputId());
		}

		//Checks if transaction is valid
		if(getInputsValue() < Blockchain.minimumTransaction) { // Below minimum transaction value
			System.out.println("Transaction Inputs too small: " + getInputsValue());
			System.out.println("Please enter the amount greater than " + Blockchain.minimumTransaction);
			return false;
		}
		
		//Generate transaction outputs:
		float leftOver = getInputsValue() - value; //get value of inputs and remainder
		hash = calulateHash();
		outputs.add(new TransactionOutput(this.recipient, value,hash)); //send value to recicpient
		outputs.add(new TransactionOutput(this.sender, leftOver,hash)); //send remainder back to sender		
				
		//Add outputs UTXOs
		for(TransactionOutput o : outputs) {
			Blockchain.UTXOs.put(o.hash , o);
		}
		
		//Remove transaction inputs from UTXO lists as spent:
		for(TransactionInput i : transactionInputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it 
			Blockchain.UTXOs.remove(i.UTXO.hash);
		}
		
		return true;
	}
	
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : transactionInputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it, This behavior may not be optimal.
			total += i.UTXO.value;
		}
		return total;
	}
	
	// Generate transaction signature
	public void generateSignature(PrivateKey privateKey) {
		String data = sender + recipient + Float.toString(value); // String values of sender + recipient + value
		signatureInBytes = Utility.applyECDSASig(privateKey,data);	
		signatureInBytes = Base64.getEncoder().encode(signatureInBytes);
		signature = signatureToString(signatureInBytes);
	}
	
	// Convert signature to String
	private String signatureToString(byte[] signatureInBytes) {
		String signature = new String(signatureInBytes);
		return signature;
	}

	// Verify Signature
	public boolean verifySignature() {
		String data = sender + recipient + Float.toString(value);
		return Utility.verifyECDSASig(Utility.getPublicKeyFromString(sender), data, Base64.getDecoder().decode(signature));
	}
	
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}
	
	// Calculate transaction hash
	private String calulateHash() {
		sequence = seq;
		seq++;
		return Utility.applySha256(sender + recipient + Float.toString(value) + sequence);
	}

	public String getSender(){
		return sender;
	}

	public String getRecipient(){
		return recipient;
	}
	
}
