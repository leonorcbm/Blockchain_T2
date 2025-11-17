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

        // --- Run TxHandler on this tx ---
        TxHandler handler = new TxHandler(pool);

        System.out.println("=== PROCESSING TRANSACTION ===");
        System.out.println("Inputs: 1");
        System.out.println("Outputs: 2 (5.0, 5.0)");
        System.out.println();

        Transaction[] accepted = handler.handleTxs(new Transaction[]{tx});

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
