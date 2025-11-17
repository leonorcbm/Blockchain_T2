import java.security.*;
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
        tx.addOutput(5.0, pub);  // another 5

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

        Transaction tx4 = new Transaction();

        // Spend the unspent 5-coin UTXO from tx (index 1)
        tx4.addInput(tx.getHash(), 1);

        // Create outputs (example: split into 2 + 3)
        tx4.addOutput(2.0, pub);

        // Sign it
        byte[] raw4 = tx4.getRawDataToSign(0);
        byte[] sig4 = Crypto.sign(priv, raw4);
        tx4.addSignature(sig4, 0);

        // Finalize
        tx4.finalize();


        Transaction tx5 = new Transaction();

        tx5.addInput(tx2.getHash(), 0);

        tx5.addOutput(2.0, pub);
        tx5.addOutput(1.0, pub);

        byte[] raw5 = tx5.getRawDataToSign(0);
        byte[] sig5 = Crypto.sign(priv, raw5);
        tx5.addSignature(sig5, 0);   // ✔️ correct

        tx5.finalize();

        Transaction tx6 = new Transaction();

        tx6.addInput(tx5.getHash(), 0);

        tx6.addOutput(1.0, pub);
        tx6.addOutput(1.0, pub);

        byte[] raw6 = tx6.getRawDataToSign(0);
        byte[] sig6 = Crypto.sign(priv, raw6);
        tx6.addSignature(sig6, 0);   // ✔️ correct

        tx6.finalize();



        // --- Run TxHandler on this tx ---
        TxHandler handler = new TxHandler(pool);


        //TODO
        // Mudar isto para não ser hardcoded
        // Isto é, ver o número de inputs e de outputs de forma dinâmica sem estar só uma string
        System.out.println("=== PROCESSING TRANSACTION ===");
        System.out.println("Inputs: 2");
        System.out.println("Outputs: 2 (5.0, 3.0, 2.0)");
        System.out.println();

        Transaction[] accepted = handler.handleTxs(new Transaction[]{tx});
        Transaction[] accepted2 = handler.handleTxs(new Transaction[]{tx2});
        Transaction[] accepted4 = handler.handleTxs(new Transaction[]{tx4});
        Transaction[] accepted5 = handler.handleTxs(new Transaction[]{tx5});
        Transaction[] accepted6 = handler.handleTxs(new Transaction[]{tx6});


        // --- Show final results ---
        System.out.println("=== RESULTS ===");
        System.out.println("Accepted transactions: " + accepted.length);

        System.out.println("Final UTXO pool entries:");
        for (UTXO utxo : pool.getAllUTXO()) {
            System.out.println(" - Hash=" + bytesToHex(utxo.getTxHash()) +
                    " Index=" + utxo.getIndex() +
                    " Value=" + pool.getTxOutput(utxo).value);
        }

        System.out.println("-----------------------------");
        List<Object> miau =  handler.getPool();
        System.out.println(miau);



    }






    // Small helper for pretty hex formatting
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
