import java.security.*;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        // --- Generate RSA key pairs ---
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        PublicKey pub = pair.getPublic();
        PrivateKey priv = pair.getPrivate();

        // --- Create UTXO pool and add an initial coin ---
        UTXOPool pool = new UTXOPool();

        Transaction genesis = new Transaction();
        genesis.addOutput(10.0, pub);
        genesis.finalize();

        UTXO u = new UTXO(genesis.getHash(), 0);
        pool.addUTXO(u, genesis.getOutput(0));

        System.out.println("=== GENESIS CREATED ===");
        System.out.println("UTXO -> value: 10.0, owner: " + pub.toString());
        System.out.println();

        // --- Create a transaction that spends the genesis coin ---
        Transaction tx = new Transaction();
        tx.addInput(genesis.getHash(), 0);
        tx.addOutput(5.0, pub);  // send 5 back to self
        tx.addOutput(4.0, pub);  // another 5

        // --- Signing ---
        byte[] raw = tx.getRawDataToSign(0);
        byte[] sig = Crypto.sign(priv, raw);
        tx.addSignature(sig, 0);   // IMPORTANT

        // now finalize
        tx.finalize();




        // --- Build a new transaction that spends the output from the previous tx ---
        Transaction tx2 = new Transaction();

        // Spend output 0 of the previous tx
        tx2.addInput(tx.getHash(), 0);

        // Create outputs
        tx2.addOutput(3.0, pub);

        // --- Sign the input ---
        byte[] raw2 = tx2.getRawDataToSign(0);
        byte[] sig2 = Crypto.sign(priv, raw2);
        tx2.addSignature(sig2, 0);

        // Finalize the tx
        tx2.finalize();

        Transaction tx3 = new Transaction();
        // Spend the unspent 5-coin UTXO from tx (index 1)
        tx3.addInput(tx.getHash(), 1);
        // Create outputs (example: split into 2 + 3)
        tx3.addOutput(2.0, pub);
        // Sign it
        byte[] raw4 = tx3.getRawDataToSign(0);
        byte[] sig4 = Crypto.sign(priv, raw4);
        tx3.addSignature(sig4, 0);
        // Finalize
        tx3.finalize();


        Transaction tx4 = new Transaction();

        tx4.addInput(tx2.getHash(), 0);

        tx4.addOutput(2.0, pub);

        byte[] raw5 = tx4.getRawDataToSign(0);
        byte[] sig5 = Crypto.sign(priv, raw5);
        tx4.addSignature(sig5, 0);   // ✔️ correct

        tx4.finalize();

        Transaction tx5 = new Transaction();

        tx5.addInput(tx4.getHash(), 0);

        tx5.addOutput(1.0, pub);
        tx5.addOutput(1.0, pub);

        byte[] raw6 = tx5.getRawDataToSign(0);
        byte[] sig6 = Crypto.sign(priv, raw6);
        tx5.addSignature(sig6, 0);   // ✔️ correct

        tx5.finalize();

        Transaction tx6 = new Transaction();
        tx6.addInput(tx5.getHash(), 0);
        tx6.addOutput(0.5, pub);

        byte[] raw7 = tx6.getRawDataToSign(0);
        byte[] sig7 = Crypto.sign(priv, raw7);
        tx6.addSignature(sig7, 0);   // ✔️ correct
        tx6.finalize();

        Transaction txBait = new Transaction();
        txBait.addInput(genesis.getHash(), 0);
        txBait.addOutput(5.5, pub);

        byte[] rawBait = txBait.getRawDataToSign(0);
        txBait.addSignature(Crypto.sign(priv, rawBait), 0);
        txBait.finalize();



        // --- Run TxHandler on this tx ---
        TxHandler handler = new TxHandler(pool);


        //TODO
        // Mudar isto para não ser hardcoded
        // Isto é, ver o número de inputs e de outputs de forma dinâmica sem estar só uma string
        System.out.println("=== PROCESSING TRANSACTION ===");
        System.out.println("Inputs: 2");
        System.out.println("Outputs: 2 (5.0, 3.0, 2.0)");
        System.out.println();

        Transaction[] allTxs = {tx, tx2, tx3, tx4, tx5, tx6};

        System.out.println("\n##################################\n##################################\n##################################");


        // --- Greedy selector ---
        Greedy greedy = new Greedy(pool);
        Transaction[] greedySelected = greedy.selectTransactions(allTxs);
        System.out.println("\n--- Greedy selection ---");
        System.out.println("Selected txs: " + greedySelected.length);
        UTXOPool working = new UTXOPool(pool);
        double totalFee = 0.0;
        for (Transaction g : greedySelected) {
            double fee = applyAndComputeFee(working, g);
            totalFee += fee;
            System.out.println(String.format("  %s  (fee=%.4f)", bytesToHex(g.getHash()), fee));
        }
        System.out.println(String.format("Total fee collected by Greedy: %.4f", totalFee));

        Transaction[] accepted = handler.handleTxs(allTxs);

        // Final results
        System.out.println("\n\n=== RESULTS ===");
        System.out.println("Accepted transactions: " + accepted.length);
        UTXOPool finalPool = handler.getUtxoPool();
        System.out.println("Final UTXO count: " + finalPool.getAllUTXO().size());

        System.out.println("\n" +
                "\n##################################" +
                "\n##################################" +
                "\n##################################");

        System.out.println("-----------------------------");
        List<Object> trans =  handler.getPool();
        System.out.println(trans);


        System.out.println("-----------------------------");
        System.out.println("Fee of Tx1: " + handler.getTxFee(tx));
        System.out.println("-----------------------------");
        System.out.println("Fee of Tx2: " + handler.getTxFee(tx2));
        System.out.println("-----------------------------");
        System.out.println("Fee of Tx3: " + handler.getTxFee(tx3));
        System.out.println("-----------------------------");
        System.out.println("Fee of Tx4: " + handler.getTxFee(tx4));
        System.out.println("-----------------------------");
        System.out.println("Fee of Tx5: " + handler.getTxFee(tx5));
        System.out.println("-----------------------------");
        System.out.println("Fee of Tx6: " + handler.getTxFee(tx6));
        System.out.println("-----------------------------");
        System.out.println(handler.getAcceptedTxs());
        System.out.println("-----------------------------");



        // CASE FOR 2
        System.out.println("## Case for Max Two Transactions");
        float[] bestTwo = Brute.BruteF(pool, allTxs, handler);
        System.out.println("Best 2 Fees found (Total Max Fee): " + Arrays.toString(bestTwo));
        System.out.println("Total Fee: " + (bestTwo[0] + bestTwo[1]));
        System.out.println("-----------------------------");


        // CASE FOR 3
        System.out.println("## Case for Max Three Transactions");
        float[] bestThree = Brute.BruteF_Three(handler); // Uses the new method
        System.out.println("Best 3 Fees found (Total Max Fee): " + Arrays.toString(bestThree));
        System.out.println("Total Fee: " + (bestThree[0] + bestThree[1] + bestThree[2]));
        System.out.println("-----------------------------");


        // CASE FOR MAX ALL (Brute Force Power Set)
        System.out.println("## Case for Max Subset (Brute Force)");
        float[] bestSubset = Brute.BruteF_MaxAll(handler);
        System.out.println("Best Subset Fees found: " + Arrays.toString(bestSubset));

        float totalMaxFee = 0.0f;
        for (float f : bestSubset) {
            totalMaxFee += f;
        }
        System.out.println("Total Fee: " + totalMaxFee);
        System.out.println("-----------------------------");




    }

    // Small helper for pretty hex formatting
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
    // Apply tx to working pool and return its fee
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
