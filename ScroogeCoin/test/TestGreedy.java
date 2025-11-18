import java.util.*;

public class TestGreedy {
    public static void main(String[] args) {
        // --- Minimal stubs for required classes ---
        class Crypto {
            public static boolean verifySignature(String address, byte[] message, byte[] signature) {
                return true; // always valid for testing
            }
        }

        class Transaction {
            static class Input {
                byte[] prevTxHash;
                int outputIndex;
                byte[] signature;
                Input(byte[] hash, int idx) { prevTxHash = hash; outputIndex = idx; signature = new byte[0]; }
            }
            static class Output {
                double value;
                String address;
                Output(double v, String addr) { value = v; address = addr; }
            }

            private List<Input> inputs = new ArrayList<>();
            private List<Output> outputs = new ArrayList<>();
            private byte[] hash;

            public void addInput(byte[] hash, int idx) { inputs.add(new Input(hash, idx)); }
            public void addOutput(double value, String addr) { outputs.add(new Output(value, addr)); }
            public Input getInput(int i) { return inputs.get(i); }
            public Output getOutput(int i) { return outputs.get(i); }
            public Output[] getOutputs() { return outputs.toArray(new Output[0]); }
            public int numInputs() { return inputs.size(); }
            public int numOutputs() { return outputs.size(); }

            public byte[] getHash() {
                if (hash == null) hash = UUID.randomUUID().toString().getBytes();
                return hash;
            }

            public byte[] getRawDataToSign(int index) { return new byte[0]; }
        }

        class UTXO {
            byte[] txHash;
            int index;
            UTXO(byte[] h, int i) { txHash = h; index = i; }
            public boolean equals(Object o) {
                if (!(o instanceof UTXO)) return false;
                UTXO u = (UTXO)o;
                return Arrays.equals(txHash, u.txHash) && index == u.index;
            }
            public int hashCode() { return Arrays.hashCode(txHash) + index; }
        }

        class UTXOPool {
            private Map<UTXO, Transaction.Output> map = new HashMap<>();
            public UTXOPool() {}
            public UTXOPool(UTXOPool other) { map.putAll(other.map); }
            public void addUTXO(UTXO u, Transaction.Output o) { map.put(u, o); }
            public void removeUTXO(UTXO u) { map.remove(u); }
            public boolean contains(UTXO u) { return map.containsKey(u); }
            public Transaction.Output getTxOutput(UTXO u) { return map.get(u); }
        }

        // --- Setup initial UTXO pool ---
        UTXOPool pool = new UTXOPool();
        Transaction genesis = new Transaction();
        genesis.addOutput(10, "Alice");
        pool.addUTXO(new UTXO(genesis.getHash(), 0), genesis.getOutput(0));

        // --- Create candidate transactions ---
        Transaction tx1 = new Transaction();
        tx1.addInput(genesis.getHash(), 0);
        tx1.addOutput(6, "Bob");    // fee = 4
        tx1.addOutput(3, "Charlie");

        Transaction tx2 = new Transaction();
        tx2.addInput(genesis.getHash(), 0);
        tx2.addOutput(9, "Dave");   // fee = 1

        Transaction[] candidates = { tx1, tx2 };

        // --- Run greedy selection ---
        Greedy greedy = new Greedy(pool);
        Transaction[] selected = greedy.selectTransactions(candidates);

        // --- Print results ---
        System.out.println("Selected transactions:");
        for (Transaction tx : selected) {
            System.out.println(Arrays.toString(tx.getHash()));
        }
    }
}
