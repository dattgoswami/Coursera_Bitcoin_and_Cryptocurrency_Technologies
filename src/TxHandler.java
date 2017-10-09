
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class TxHandler {
	
	UTXOPool currentUTXOPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	this.currentUTXOPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	boolean flag = true;
    	ArrayList<Transaction.Output> outputs = tx.getOutputs(); 
    	ArrayList<Transaction.Input> inputs = tx.getInputs();
    	Set<UTXO> setUTXO = new HashSet<UTXO>(); 
    	double transactionSum = 0;
    	//1
    	for(int i=0; i<inputs.size(); i++){
    		UTXO utxo = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
    		if(!currentUTXOPool.contains(utxo)){
    			flag = false;
    		}
    	}
    	//2
    	for(int i=0; i<tx.numInputs(); i++){
    		UTXO utxo = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
    		try{
    			flag &= Crypto.verifySignature(currentUTXOPool.getTxOutput(utxo).address,tx.getRawDataToSign(i),tx.getInput(i).signature);
    		}catch(NullPointerException e){
    			System.out.println(e);
    		}
    	}
    	//3
    	for(int i=0; i<tx.numInputs(); i++){
    		UTXO utxo = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
    		if(!setUTXO.contains(utxo)){
    			setUTXO.add(utxo);
    		}else{
    			flag = false;
    		}
    	}
    	//4
    	for(int i=0; i<outputs.size(); i++){
    		if(outputs.get(i).value < 0){
    			flag = false;
    		}
    	}
    	//5
    	for(int i=0; i<tx.numInputs(); i++){
    		try{
	    		UTXO utxo = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
	    		transactionSum += currentUTXOPool.getTxOutput(utxo).value;	
	    	}catch(NullPointerException e){
				System.out.println(e);
			}	
    	}
    	for(int i=0; i<outputs.size(); i++){
    		try{
    			transactionSum -= outputs.get(i).value;
    		}catch(NullPointerException e){
    			System.out.println(e);
    		}	
    	}
    	if(transactionSum < 0){
    		flag = false;
    	}
		return flag;
		
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	Set<Transaction> validTxs = new HashSet<Transaction>();
    	for(int i=0 ; i<possibleTxs.length ; i++){
    		if(isValidTx(possibleTxs[i])){
    			validTxs.add(possibleTxs[i]);
	    		for(int j=0; j<possibleTxs[i].numInputs(); j++){
	    			UTXO tempUTXO = new UTXO(possibleTxs[i].getInput(j).prevTxHash,possibleTxs[i].getInput(j).outputIndex);
	    			currentUTXOPool.removeUTXO(tempUTXO);
	    		}
	    		for(int k=0; k<possibleTxs[i].numOutputs(); k++){
	    			Transaction.Output output = possibleTxs[i].getOutput(k);
	    			UTXO utxo = new UTXO(possibleTxs[i].getHash(),k);
	    			currentUTXOPool.addUTXO(utxo, output);
	    		}
    		}
    	}
    	Transaction[] validTxArray = new Transaction[validTxs.size()];
		return validTxs.toArray(validTxArray);
    }

}
