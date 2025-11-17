import java.util.*;

public class TxHandler {

    private UTXOPool utxoPool;
    private List<Transaction> acceptedTxs;
    private Map<byte[], Double> feeMap = new HashMap<>();
    private Double fee;


    /** Creates a copy of the given utxoPool */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
        this.acceptedTxs = new ArrayList<>();
    }

    /** Checks transaction validity under ScroogeCoin rules */
    public boolean isValidTx(Transaction tx) {
        HashSet<UTXO> claimed = new HashSet<>();

        double inputSum = 0;
        double outputSum = 0;

        // Check UTXOs
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);

            // UTXO must exist
            if (!utxoPool.contains(utxo)) return false;

            // No double spending within the tx
            if (!claimed.add(utxo)) return false;

            // Signature must be valid
            Transaction.Output prevOut = utxoPool.getTxOutput(utxo);
            if (!Crypto.verifySignature(prevOut.address, tx.getRawDataToSign(i), in.signature))
                return false;

            inputSum += prevOut.value;
        }

        // Check outputs
        for (Transaction.Output out : tx.getOutputs()) {
            // 4. Output values must be non-negative
            if (out.value < 0) return false;
            outputSum += out.value;
        }

        // No value creation
        return inputSum + 1e-12 >= outputSum;
    }

    /**
     * Repeatedly process transactions until no more can be accepted.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {

        Set<Transaction> remaining = new HashSet<>(Arrays.asList(possibleTxs));
        List<Transaction> accepted = new ArrayList<>();


        boolean progress = true;

        while (progress) {
            progress = false;

            Iterator<Transaction> it = remaining.iterator();
            while (it.hasNext()) {
                Transaction tx = it.next();

                if (isValidTx(tx)) {
                    fee = getTxFeeBeforeApply(tx);
                    feeMap.put(tx.getHash(), fee);
                    /*System.out.println("Processing tx fee: " + fee);*/
                    applyTx(tx);
                    accepted.add(tx);
                    acceptedTxs.add(tx);
                    printTx(tx);
                    printPool();
                    it.remove();
                    progress = true;   // new tx accepted → try again
                }
            }
        }

        return accepted.toArray(new Transaction[0]);
    }

    /** Updates UTXOPool after accepting tx */
    private void applyTx(Transaction tx) {
        // remove inputs
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO spent = new UTXO(in.prevTxHash, in.outputIndex);
            utxoPool.removeUTXO(spent);
        }

        // add outputs
        byte[] h = tx.getHash(); // do NOT re-finalize
        for (int i = 0; i < tx.numOutputs(); i++) {
            UTXO newUtxo = new UTXO(h, i);
            utxoPool.addUTXO(newUtxo, tx.getOutput(i));
        }
    }

    /* Print a transaction in a readable way */
    private void printTx(Transaction tx) {
        System.out.println("-------------------------------------------------");
        System.out.println("Transaction " + bytesToHex(tx.getHash()));

        System.out.println("Inputs:");
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            System.out.println("  - prevTx: " + bytesToHex(in.prevTxHash)
                    + " | outputIndex: " + in.outputIndex);
        }

        System.out.println("Outputs:");
        for (int i = 0; i < tx.numOutputs(); i++) {
            Transaction.Output out = tx.getOutput(i);
            System.out.println("  - value: " + out.value + " | address: " + out.address);
        }
    }


    //TODO
    // ter as tx todas para conseguir calcular a fee entre o input e output
    // para cada tx temos de ter o input e output para calcular a fee
    //

    public List<Transaction> getAcceptedTxs() {
        return new ArrayList<>(acceptedTxs); // return a copy to avoid external modification
    }

    public double getTxFee(Transaction tx) {
        return feeMap.put(tx.getHash(), fee);
    }

    public double getTxFeeBeforeApply(Transaction tx) {
        double inputSum = 0;
        double outputSum = 0;

        for (Transaction.Input in : tx.getInputs()) {
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output prevOut = utxoPool.getTxOutput(utxo);

            if (prevOut == null) {
                System.err.println("ERROR: input UTXO not found for fee calculation!");
                return -1;
            }

            inputSum += prevOut.value;
        }

        for (Transaction.Output out : tx.getOutputs()) {
            outputSum += out.value;
        }

        return inputSum - outputSum;
    }



    /* Print UTXO pool contents */
    private void printPool() {
        System.out.println("Current UTXO Pool:");
        for (UTXO u : utxoPool.getAllUTXO()) {
            Transaction.Output out = utxoPool.getTxOutput(u);
            System.out.println("  UTXO: " + bytesToHex(u.getTxHash()) +
                    " | idx: " + u.getIndex() +
                    " | value: " + out.value);
        }
    }

    /* Convert bytes → hex */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public List<Object> getPool(){
        List<Object> pool;
        pool = new ArrayList<>();
        for (UTXO u : utxoPool.getAllUTXO()) {
            Transaction.Output out = utxoPool.getTxOutput(u);
            pool.add(out);
        }
        return pool;
    }

    public UTXOPool getUtxoPool() {
        return utxoPool;
    }

}
