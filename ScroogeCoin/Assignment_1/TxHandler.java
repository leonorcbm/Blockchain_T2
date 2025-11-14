import java.util.ArrayList;
import java.util.HashSet;
import java.security.PublicKey;

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     * values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        HashSet<UTXO> claimedUTXOs = new HashSet<>();
        double inputSum = 0;

        // Input Checks (Rules 1, 2, 3)
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

            // (1) Check if UTXO is in the current UTXO pool
            if (!utxoPool.contains(utxo)) {
                return false;
            }

            // (3) Check for double-claiming within the current transaction
            if (claimedUTXOs.contains(utxo)) {
                return false;
            }
            claimedUTXOs.add(utxo);

            Transaction.Output correspondingOutput = utxoPool.getTxOutput(utxo);
            PublicKey pubKey = correspondingOutput.address;

            // (2) Check if the signature is valid
            // Note: signature can be null if it's the first transaction (Coinbase/Genesis),
            // but in this assignment context, we expect non-null signatures for inputs.
            byte[] message = tx.getRawDataToSign(i);
            byte[] signature = input.signature;

            if (signature == null || !Crypto.verifySignature(pubKey, message, signature)) {
                return false;
            }

            // Accumulate input value for Rule (5)
            inputSum += correspondingOutput.value;
        }

        // Output Checks (Rule 4) and Output Sum Check (Rule 5)
        double outputSum = 0;
        for (Transaction.Output output : tx.getOutputs()) {

            // (4) Check if output value is non-negative
            // Use a small epsilon for floating point comparison to ensure value >= 0.
            if (output.value < 0) {
                return false;
            }

            // Accumulate output value for Rule (5)
            outputSum += output.value;
        }

        // (5) Check if the sum of input values >= sum of output values
        // Use an epsilon for robust floating-point comparison
        final double EPSILON = 1e-12;
        if (inputSum < outputSum - EPSILON) {
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> acceptedTxs = new ArrayList<>();
        HashSet<UTXO> spentInThisBatch = new HashSet<>();

        // Simple greedy approach to find a mutually valid set of maximal size
        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {

                // Check for double-spending against transactions *already accepted* in this batch.
                boolean alreadySpent = false;
                for(Transaction.Input input : tx.getInputs()) {
                    UTXO claimed = new UTXO(input.prevTxHash, input.outputIndex);
                    if (spentInThisBatch.contains(claimed)) {
                        alreadySpent = true;
                        break;
                    }
                }

                if (!alreadySpent) {
                    acceptedTxs.add(tx);

                    // --- UPDATE THE UTXO POOL & TRACKERS ---

                    // 1. Remove spent UTXOs (inputs) from the pool and track them
                    for (Transaction.Input input : tx.getInputs()) {
                        UTXO spent = new UTXO(input.prevTxHash, input.outputIndex);
                        this.utxoPool.removeUTXO(spent);
                        spentInThisBatch.add(spent);
                    }

                    // 2. Add new UTXOs (outputs) to the pool
                    tx.finalize();
                    byte[] txHash = tx.getHash();
                    for (int i = 0; i < tx.numOutputs(); i++) {
                        UTXO newUTXO = new UTXO(txHash, i);
                        this.utxoPool.addUTXO(newUTXO, tx.getOutput(i));
                    }
                }
            }
        }

        return acceptedTxs.toArray(new Transaction[acceptedTxs.size()]);
    }
}
/*
 * Implementation Notes:
 * The TxHandler class manages the public ledger (UTXOPool).
 * * isValidTx() checks five conditions:
 * 1. UTXO existence in the pool.
 * 2. Valid digital signatures on inputs using Crypto.verifySignature().
 * 3. No double-spending within the transaction itself (tracked via claimedUTXOs HashSet).
 * 4. Non-negative output values.
 * 5. Input sum must be greater than or equal to output sum (no coin creation). Epsilon (1e-12)
 * is used for floating-point comparison robustness.
 * * handleTxs() implements a greedy algorithm to select a mutually valid, maximal-sized subset
 * of transactions. Mutual validity is ensured by checking against UTXOs spent by transactions
 * already accepted in this batch (tracked via spentInThisBatch HashSet) and updating the UTXOPool
 * immediately upon acceptance.
 */