import java.security.*;
import java.util.ArrayList;

public class TestGreedy {

    public static void main(String[] args) throws Exception {

        // ===== Setup RSA keys =====
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        PublicKey pub = pair.getPublic();

        // ===== Create UTXO Pool =====
        UTXOPool pool = new UTXOPool();

        Transaction tx = new Transaction();
        tx.addOutput(5.0, pub);
        tx.addOutput(3.0, pub);
        tx.addOutput(2.0, pub);
        tx.addOutput(1.0, pub);
        tx.addOutput(14.0, pub);
        tx.addOutput(0.5, pub);
        tx.finalize();

        for (int i = 0; i < tx.numOutputs(); i++) {
            pool.addUTXO(new UTXO(tx.getHash(), i), tx.getOutput(i));
        }

        // ===== Find highest UTXO automatically =====
        double highestValue = 0;
        for (UTXO u : pool.getAllUTXO()) {
            double v = pool.getTxOutput(u).value;
            if (v > highestValue) highestValue = v;
        }

        System.out.println("Highest UTXO value detected = " + highestValue);

        // ===== Call Greedy using the highest UTXO value =====
        Greedy greedy = new Greedy(pool);
        ArrayList<UTXO> selected = greedy.isValidForGreedy(highestValue);

        // ===== Print results =====
        System.out.println("\n=== Selected UTXOs ===");
        double total = 0;
        for (UTXO u : selected) {
            double v = pool.getTxOutput(u).value;
            total += v;
            System.out.println("UTXO idx " + u.getIndex() + " | value = " + v);
        }

        System.out.println("\nTotal selected = " + total);

        // ===== Test result =====
        if (total == highestValue) {
            System.out.println("✅ Test Passed: Highest UTXO selected!");
        } else {
            System.out.println("❌ Test Failed");
        }
    }
}
