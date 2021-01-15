package simpleBlockchainV2;

public class TransactionInput {
	public String transactionOutputId;
	public transient TransactionOutput UTXO;
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}

	public String getTransactionOutputId(){
		return transactionOutputId;
	}

}
