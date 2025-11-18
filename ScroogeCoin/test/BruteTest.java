import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class BruteTest {

    /** Mock Transaction that stores a fake fee. */
    static class MockTx extends Transaction {
        float fee;
        private final byte[] hash;

        MockTx(float fee, int id) {
            this.fee = fee;
            // stable unique hash per transaction
            this.hash = new byte[]{ (byte) id };
        }

        @Override
        public byte[] getHash() {
            return hash;
        }
    }

    /** Mock TxHandler that allows us to inject fake txs + fees. */
    static class MockTxHandler extends TxHandler {

        private final List<Transaction> accepted;

        public MockTxHandler(List<Transaction> accepted, List<Float> fees) {
            super(new UTXOPool());   // dummy pool
            this.accepted = accepted;

            // Put fake fees into the TxHandler.feeMap
            for (int i = 0; i < accepted.size(); i++) {
                Transaction tx = accepted.get(i);
                float fee = fees.get(i);
                super.feeMap.put(tx.getHash(), (double) fee);
            }
        }

        @Override
        public List<Transaction> getAcceptedTxs() {
            return new ArrayList<>(accepted);
        }

        @Override
        public double getTxFee(Transaction tx) {
            return super.feeMap.getOrDefault(tx.getHash(), 0.0);
        }
    }

    // ===========================================
    // TESTS FOR BruteF (Best 2)
    // ===========================================

    @Test
    public void testBestFeePair() {
        List<Transaction> txs = Arrays.asList(
                new MockTx(1.0f, 0),
                new MockTx(5.0f, 1),
                new MockTx(3.5f, 2),
                new MockTx(4.0f, 3)
        );
        List<Float> fees = Arrays.asList(1f, 5f, 3.5f, 4f);

        MockTxHandler handler = new MockTxHandler(txs, fees);

        float[] result = Brute.BruteF(null, null, handler);
        Arrays.sort(result);

        assertArrayEquals(new float[]{4.0f, 5.0f}, result, 0.0001f);
    }

    @Test
    public void testPairEqualValues() {
        List<Transaction> txs = Arrays.asList(
                new MockTx(2.0f, 0),
                new MockTx(2.0f, 1),
                new MockTx(2.0f, 2)
        );
        List<Float> fees = Arrays.asList(2f, 2f, 2f);

        MockTxHandler handler = new MockTxHandler(txs, fees);

        float[] result = Brute.BruteF(null, null, handler);
        Arrays.sort(result);

        assertArrayEquals(new float[]{2.0f, 2.0f}, result, 0.0001f);
    }

    @Test
    public void testPairExactTwo() {
        List<Transaction> txs = Arrays.asList(
                new MockTx(10.0f, 0),
                new MockTx(1.0f, 1)
        );
        List<Float> fees = Arrays.asList(10f, 1f);

        MockTxHandler handler = new MockTxHandler(txs, fees);

        float[] result = Brute.BruteF(null, null, handler);
        Arrays.sort(result);

        assertArrayEquals(new float[]{1.0f, 10.0f}, result, 0.0001f);
    }

    @Test
    public void testPairTooFew() {
        List<Transaction> txs = Collections.singletonList(
                new MockTx(7.0f, 0)
        );
        List<Float> fees = Collections.singletonList(7f);

        MockTxHandler handler = new MockTxHandler(txs, fees);

        float[] result = Brute.BruteF(null, null, handler);

        assertEquals(0f, result[0], 0.0001);
        assertEquals(0f, result[1], 0.0001);
    }

    @Test
    public void testPairEmpty() {
        List<Transaction> txs = new ArrayList<>();
        List<Float> fees = new ArrayList<>();

        MockTxHandler handler = new MockTxHandler(txs, fees);
        float[] result = Brute.BruteF(null, null, handler);
        assertArrayEquals(new float[]{0f, 0f}, result, 0.0001f);
    }

    // ===========================================
    // TESTS FOR BruteF_Three (Best 3)
    // ===========================================

    @Test
    public void testBestFeeTriple() {
        // Fees: 1, 6, 2, 5, 3. Best 3 should be 6, 5, 3
        List<Transaction> txs = Arrays.asList(
                new MockTx(1.0f, 0),
                new MockTx(6.0f, 1),
                new MockTx(2.0f, 2),
                new MockTx(5.0f, 3),
                new MockTx(3.0f, 4)
        );
        List<Float> fees = Arrays.asList(1f, 6f, 2f, 5f, 3f);

        MockTxHandler handler = new MockTxHandler(txs, fees);

        float[] result = Brute.BruteF_Three(handler);
        Arrays.sort(result);

        assertArrayEquals(new float[]{3.0f, 5.0f, 6.0f}, result, 0.0001f);
    }

    @Test
    public void testTripleExactThree() {
        List<Transaction> txs = Arrays.asList(
                new MockTx(1.0f, 0),
                new MockTx(2.0f, 1),
                new MockTx(3.0f, 2)
        );
        List<Float> fees = Arrays.asList(1f, 2f, 3f);

        MockTxHandler handler = new MockTxHandler(txs, fees);
        float[] result = Brute.BruteF_Three(handler);
        Arrays.sort(result);

        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, result, 0.0001f);
    }

    @Test
    public void testTripleTooFew() {
        List<Transaction> txs = Arrays.asList(
                new MockTx(1.0f, 0),
                new MockTx(2.0f, 1)
        );
        List<Float> fees = Arrays.asList(1f, 2f);

        MockTxHandler handler = new MockTxHandler(txs, fees);
        float[] result = Brute.BruteF_Three(handler);

        assertArrayEquals(new float[]{0f, 0f, 0f}, result, 0.0001f);
    }

    // ===========================================
    // TESTS FOR BruteF_MaxAll (Best Subset / Power Set)
    // ===========================================

    @Test
    public void testMaxAllNormal() {
        // With all positive fees, max subset is ALL of them.
        List<Transaction> txs = Arrays.asList(
                new MockTx(10.0f, 0),
                new MockTx(5.0f, 1)
        );
        List<Float> fees = Arrays.asList(10f, 5f);

        MockTxHandler handler = new MockTxHandler(txs, fees);
        float[] result = Brute.BruteF_MaxAll(handler);
        Arrays.sort(result);

        assertArrayEquals(new float[]{5.0f, 10.0f}, result, 0.0001f);
    }

    @Test
    public void testMaxAllSingle() {
        List<Transaction> txs = Collections.singletonList(new MockTx(100.0f, 0));
        List<Float> fees = Collections.singletonList(100f);

        MockTxHandler handler = new MockTxHandler(txs, fees);
        float[] result = Brute.BruteF_MaxAll(handler);

        assertEquals(1, result.length);
        assertEquals(100.0f, result[0], 0.0001f);
    }

    @Test
    public void testMaxAllEmpty() {
        MockTxHandler handler = new MockTxHandler(new ArrayList<>(), new ArrayList<>());
        float[] result = Brute.BruteF_MaxAll(handler);

        // Should return empty array
        assertEquals(0, result.length);
    }

    @Test
    public void testMaxAllWithNegativeAndZero() {
        // Logic check: BruteF_MaxAll generates subsets.
        // If fees are [10, -5, 20], subset [10, 20] = 30 is better than [10, -5, 20] = 25.
        // Note: Your brute force implementation sums the subset.

        List<Transaction> txs = Arrays.asList(
                new MockTx(10.0f, 0),
                new MockTx(-5.0f, 1), // Negative fee (unlikely in crypto but possible in logic)
                new MockTx(20.0f, 2)
        );
        List<Float> fees = Arrays.asList(10f, -5f, 20f);

        MockTxHandler handler = new MockTxHandler(txs, fees);
        float[] result = Brute.BruteF_MaxAll(handler);
        Arrays.sort(result);

        // We expect it to pick {10, 20} and skip -5
        assertArrayEquals(new float[]{10.0f, 20.0f}, result, 0.0001f);
    }
}