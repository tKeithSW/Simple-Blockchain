package simpleBlockchainV2;

import java.security.PublicKey;

public class TransactionOutput {
	public String recipient;
	public float value; 
	public String parentHash; 
	public String hash;
	
	public TransactionOutput(String recipient, float value, String parentHash) {
		this.recipient = recipient;
		this.value = value;
		this.parentHash = parentHash;
		this.hash = calculateHash(recipient, value, parentHash);
	}

	public TransactionOutput(String recipient, float value, String parentHash, String hash) {
		this.recipient = recipient;
		this.value = value;
		this.parentHash = parentHash;
		this.hash = hash;
	}
	
	// Check does coin belongs to recipient
	public boolean isMine(PublicKey publicKey) {
		return (publicKey.equals(Utility.getPublicKeyFromString(recipient)));
	}

	// Calculate transactionOutput hash
	public String calculateHash(String recipient, float value, String parentHash){
		return Utility.applySha256(recipient + Float.toString(value) + parentHash);
	}

}
