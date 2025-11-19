import java.security.*;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        // ================================================
        // --- 0. Generate RSA Key Pair ---
        // ================================================
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        PublicKey pub = pair.getPublic();
        PrivateKey priv = pair.getPrivate();

        // ================================================
        // --- 1. Create UTXO pool and add initial coin ---
        // ================================================
        UTXOPool pool = new UTXOPool();

        Transaction genesis = new Transaction();
        genesis.addOutput(10.0, pub);
        genesis.finalize();
        UTXO u = new UTXO(genesis.getHash(), 0);
        pool.addUTXO(u, genesis.getOutput(0));

        System.out.println("=== GENESIS CREATED ===");
        System.out.println("UTXO -> value: 10.0, owner: " + pub.toString());
        System.out.println();

        // ================================================
        // --- 2. Tx that spends the first UTXO created ---
        // ================================================

        Transaction tx = new Transaction();
        tx.addInput(genesis.getHash(), 0);
        tx.addOutput(5.0, pub);
        tx.addOutput(4.0, pub);
        byte[] raw = tx.getRawDataToSign(0);
        byte[] sig = Crypto.sign(priv, raw);
        tx.addSignature(sig, 0);
        tx.finalize();

        Transaction tx2 = new Transaction();
        tx2.addInput(tx.getHash(), 0);
        tx2.addOutput(3.0, pub);
        byte[] raw2 = tx2.getRawDataToSign(0);
        byte[] sig2 = Crypto.sign(priv, raw2);
        tx2.addSignature(sig2, 0);
        tx2.finalize();

        Transaction tx3 = new Transaction();
        tx3.addInput(tx.getHash(), 1);
        tx3.addOutput(2.0, pub);
        byte[] raw4 = tx3.getRawDataToSign(0);
        byte[] sig4 = Crypto.sign(priv, raw4);
        tx3.addSignature(sig4, 0);
        tx3.finalize();

        Transaction tx4 = new Transaction();
        tx4.addInput(tx2.getHash(), 0);
        tx4.addOutput(2.0, pub);
        byte[] raw5 = tx4.getRawDataToSign(0);
        byte[] sig5 = Crypto.sign(priv, raw5);
        tx4.addSignature(sig5, 0);
        tx4.finalize();

        Transaction tx5 = new Transaction();
        tx5.addInput(tx4.getHash(), 0);
        tx5.addOutput(1.0, pub);
        tx5.addOutput(1.0, pub);
        byte[] raw6 = tx5.getRawDataToSign(0);
        byte[] sig6 = Crypto.sign(priv, raw6);
        tx5.addSignature(sig6, 0);
        tx5.finalize();

        Transaction tx6 = new Transaction();
        tx6.addInput(tx5.getHash(), 0);
        tx6.addOutput(0.5, pub);
        byte[] raw7 = tx6.getRawDataToSign(0);
        byte[] sig7 = Crypto.sign(priv, raw7);
        tx6.addSignature(sig7, 0);
        tx6.finalize();

        Transaction txBait = new Transaction();
        txBait.addInput(genesis.getHash(), 0);
        txBait.addOutput(5.5, pub);
        byte[] rawBait = txBait.getRawDataToSign(0);
        txBait.addSignature(Crypto.sign(priv, rawBait), 0);
        txBait.finalize();

        TxHandler handler = new TxHandler(pool);
        Transaction[] allTxs = {tx, tx2, tx3, tx4, tx5, tx6};

        System.out.println("\n##################################" +
                "\n##################################" +
                "\n##################################\n\n");

        // ==========================================
        // --- 3. Greedy Selector Timing ---
        // ==========================================
        Greedy greedy = new Greedy(pool);

        long startTime = System.nanoTime(); // START TIMER
        Transaction[] greedySelected = greedy.selectTransactions(allTxs);
        long endTime = System.nanoTime();   // END TIMER
        double greedyDuration = (endTime - startTime) / 1_000_000.0; // ns to ms

        System.out.println("--- Greedy Selection ---");
        System.out.println("Time Taken: " + String.format("%.4f ms", greedyDuration));
        System.out.println("Selected txs: " + greedySelected.length);

        UTXOPool working = new UTXOPool(pool);
        double totalFee = 0.0;
        for (Transaction g : greedySelected) {
            double fee = applyAndComputeFee(working, g);
            totalFee += fee;
        }
        System.out.println(String.format("Total Fee (Greedy): %.4f", totalFee));

        handler.handleTxs(allTxs);

        System.out.println("\n" +
                "\n##################################" +
                "\n##################################" +
                "\n##################################\n\n");

        handler.printPool();

        System.out.println("\n" +
                "\n##################################" +
                "\n##################################" +
                "\n##################################\n");

        // ==========================================
        // --- 4. Brute Force (Max 2) Timing ---
        // ==========================================
        System.out.println("\n--- Brute Force (Max 2 Txs) ---");

        startTime = System.nanoTime(); // START TIMER
        float[] bestTwo = Brute.BruteF(handler);
        endTime = System.nanoTime();   // END TIMER
        double brute2Duration = (endTime - startTime) / 1_000_000.0;

        System.out.println("Time Taken: " + String.format("%.4f ms", brute2Duration));
        System.out.println("Total Fee: " + (bestTwo[0] + bestTwo[1]));

        // ==========================================
        // --- 5. Brute Force (Max 3) Timing ---
        // ==========================================
        System.out.println("\n--- Brute Force (Max 3 Txs) ---");

        startTime = System.nanoTime(); // START TIMER
        float[] bestThree = Brute.BruteF_Three(handler);
        endTime = System.nanoTime();   // END TIMER
        double brute3Duration = (endTime - startTime) / 1_000_000.0;

        System.out.println("Time Taken: " + String.format("%.4f ms", brute3Duration));
        System.out.println("Total Fee: " + (bestThree[0] + bestThree[1] + bestThree[2]));

        // ==========================================
        // --- 6. Brute Force (Max Subset/Power Set) Timing ---
        // ==========================================
        System.out.println("\n--- Brute Force (Max Subset / Power Set) ---");

        startTime = System.nanoTime(); // START TIMER
        float[] bestSubset = Brute.BruteF_MaxAll(handler);
        endTime = System.nanoTime();   // END TIMER
        double bruteAllDuration = (endTime - startTime) / 1_000_000.0;

        System.out.println("Time Taken: " + String.format("%.4f ms", bruteAllDuration));

        float totalMaxFee = 0.0f;
        for (float f : bestSubset) {
            totalMaxFee += f;
        }
        System.out.println("Total Fee: " + totalMaxFee);

        System.out.println("\n================ SUMMARY ================");
        System.out.println(String.format("Greedy Algo : %8.4f ms", greedyDuration));
        System.out.println(String.format("Brute (2)   : %8.4f ms", brute2Duration));
        System.out.println(String.format("Brute (3)   : %8.4f ms", brute3Duration));
        System.out.println(String.format("Brute (All) : %8.4f ms", bruteAllDuration));
        System.out.println("=========================================");
    }

    // =============================================================================
    // --- 1a. Helper for greedy that applies tx to working pool and returns fee ---
    // =============================================================================
    private static double applyAndComputeFee(UTXOPool working, Transaction tx) {
        double inSum = 0, outSum = 0;
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output prev = working.getTxOutput(u);
            if (prev != null) inSum += prev.value;
            working.removeUTXO(u);
        }
        for (Transaction.Output o : tx.getOutputs()) { outSum += o.value; }
        byte[] h = tx.getHash();
        for (int i = 0; i < tx.numOutputs(); i++) working.addUTXO(new UTXO(h, i), tx.getOutput(i));
        return inSum - outSum;
    }
}
