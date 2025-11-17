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

    // -------------------------------------------
    // TEST 1 — Best pair of fees
    // -------------------------------------------
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

    // -------------------------------------------
    // TEST 2 — All equal values
    // -------------------------------------------
    @Test
    public void testEqualValues() {
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

    // -------------------------------------------
    // TEST 3 — Only two transactions
    // -------------------------------------------
    @Test
    public void testTwoTxs() {
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

    // -------------------------------------------
    // TEST 4 — Only one transaction → return {0,0}
    // -------------------------------------------
    @Test
    public void testSingleTx() {
        List<Transaction> txs = Collections.singletonList(
                new MockTx(7.0f, 0)
        );
        List<Float> fees = Collections.singletonList(7f);

        MockTxHandler handler = new MockTxHandler(txs, fees);

        float[] result = Brute.BruteF(null, null, handler);

        assertEquals(0f, result[0], 0.0001);
        assertEquals(0f, result[1], 0.0001);
    }

    // -------------------------------------------
    // TEST 5 — No transactions → return {0,0}
    // -------------------------------------------
    @Test
    public void testEmptyList() {
        List<Transaction> txs = new ArrayList<>();
        List<Float> fees = new ArrayList<>();

        MockTxHandler handler = new MockTxHandler(txs, fees);

        float[] result = Brute.BruteF(null, null, handler);

        assertEquals(0f, result[0], 0.0001);
        assertEquals(0f, result[1], 0.0001);
    }
}
