import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestGreedy {

    private KeyPair pairAlice;
    private KeyPair pairBob;
    private KeyPair pairCharlie;
    private UTXOPool pool;
    private Transaction genesis;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Generate real RSA keys
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        pairAlice = keyGen.generateKeyPair();
        pairBob = keyGen.generateKeyPair();
        pairCharlie = keyGen.generateKeyPair();

        // 2. Create Genesis Transaction (Alice gets 10.0 coins)
        genesis = new Transaction();
        genesis.addOutput(10.0, pairAlice.getPublic());
        genesis.finalize();

        // 3. Initialize UTXOPool
        pool = new UTXOPool();
        UTXO u = new UTXO(genesis.getHash(), 0);
        pool.addUTXO(u, genesis.getOutput(0));
    }

    @Test
    public void testSimpleValidTransaction() throws Exception {
        // Alice sends 9.0 to Bob, fee is 1.0
        Transaction tx = new Transaction();
        tx.addInput(genesis.getHash(), 0);
        tx.addOutput(9.0, pairBob.getPublic());

        // Sign
        byte[] sig = Crypto.sign(pairAlice.getPrivate(), tx.getRawDataToSign(0));
        tx.addSignature(sig, 0);
        tx.finalize();

        Transaction[] candidates = { tx };

        Greedy greedy = new Greedy(pool);
        Transaction[] result = greedy.selectTransactions(candidates);

        assertEquals(1, result.length);
        assertArrayEquals(tx.getHash(), result[0].getHash());
    }

    @Test
    public void testDoubleSpendConflict() throws Exception {
        // Scenario: Alice tries to spend the same UTXO twice.
        // Tx1: Fee 1.0 (10.0 -> 9.0)
        Transaction txLowFee = new Transaction();
        txLowFee.addInput(genesis.getHash(), 0);
        txLowFee.addOutput(9.0, pairBob.getPublic());
        txLowFee.addSignature(Crypto.sign(pairAlice.getPrivate(), txLowFee.getRawDataToSign(0)), 0);
        txLowFee.finalize();

        // Tx2: Fee 4.0 (10.0 -> 6.0)
        Transaction txHighFee = new Transaction();
        txHighFee.addInput(genesis.getHash(), 0);
        txHighFee.addOutput(6.0, pairCharlie.getPublic());
        txHighFee.addSignature(Crypto.sign(pairAlice.getPrivate(), txHighFee.getRawDataToSign(0)), 0);
        txHighFee.finalize();

        Transaction[] candidates = { txLowFee, txHighFee };

        Greedy greedy = new Greedy(pool);
        Transaction[] result = greedy.selectTransactions(candidates);

        // Should only pick ONE transaction
        assertEquals(1, result.length);
        // Should pick the one with HIGHER fee (Tx2)
        assertArrayEquals(txHighFee.getHash(), result[0].getHash(), "Greedy should pick the higher fee transaction");
    }

    @Test
    public void testDependentTransactions() throws Exception {
        // Scenario: Tx2 depends on Tx1.

        // Tx1: Alice -> Bob (10.0 -> 10.0, Fee 0)
        Transaction tx1 = new Transaction();
        tx1.addInput(genesis.getHash(), 0);
        tx1.addOutput(10.0, pairBob.getPublic());
        tx1.addSignature(Crypto.sign(pairAlice.getPrivate(), tx1.getRawDataToSign(0)), 0);
        tx1.finalize();

        // Tx2: Bob -> Charlie (10.0 -> 8.0, Fee 2)
        Transaction tx2 = new Transaction();
        tx2.addInput(tx1.getHash(), 0);
        tx2.addOutput(8.0, pairCharlie.getPublic());
        tx2.addSignature(Crypto.sign(pairBob.getPrivate(), tx2.getRawDataToSign(0)), 0);
        tx2.finalize();

        // Pass them in REVERSE order to ensure algorithm handles dependencies
        Transaction[] candidates = { tx2, tx1 };

        Greedy greedy = new Greedy(pool);
        Transaction[] result = greedy.selectTransactions(candidates);

        assertEquals(2, result.length);

        // Verify both are present
        Set<String> hashes = new HashSet<>();
        for(Transaction t : result) hashes.add(bytesToHex(t.getHash()));

        assertTrue(hashes.contains(bytesToHex(tx1.getHash())));
        assertTrue(hashes.contains(bytesToHex(tx2.getHash())));
    }

    @Test
    public void testInvalidSignature() throws Exception {
        // Transaction with wrong signature (Alice signs with Bob's key)
        Transaction tx = new Transaction();
        tx.addInput(genesis.getHash(), 0);
        tx.addOutput(9.0, pairBob.getPublic());

        // WRONG KEY used for signing
        byte[] sig = Crypto.sign(pairBob.getPrivate(), tx.getRawDataToSign(0));
        tx.addSignature(sig, 0);
        tx.finalize();

        Transaction[] candidates = { tx };

        Greedy greedy = new Greedy(pool);
        Transaction[] result = greedy.selectTransactions(candidates);

        assertEquals(0, result.length, "Should reject invalid signature");
    }

    @Test
    public void testTamperedTransaction() throws Exception {
        // Create valid tx, then tamper with output value
        Transaction tx = new Transaction();
        tx.addInput(genesis.getHash(), 0);
        tx.addOutput(9.0, pairBob.getPublic());
        byte[] sig = Crypto.sign(pairAlice.getPrivate(), tx.getRawDataToSign(0));
        tx.addSignature(sig, 0);

        // Tamper: Change output value AFTER signing (signature becomes invalid for new data)
        // Note: This depends on implementation of Transaction.
        // If getRawDataToSign includes output values, this breaks the sig.
        // We'll simulate tampering by just finalizing a different object with same signature.

        Transaction tampered = new Transaction();
        tampered.addInput(genesis.getHash(), 0);
        tampered.addOutput(100.0, pairBob.getPublic()); // Changed 9.0 to 100.0
        tampered.addSignature(sig, 0); // Reused old signature
        tampered.finalize();

        Transaction[] candidates = { tampered };

        Greedy greedy = new Greedy(pool);
        Transaction[] result = greedy.selectTransactions(candidates);

        assertEquals(0, result.length, "Should reject tampered transaction");
    }

    // Helper
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}