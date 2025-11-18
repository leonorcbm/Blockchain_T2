import java.util.*;

public class Greedy {
    private final UTXOPool pool;

    // keep only what's required by the assignment: constructor + greedy selector
    public Greedy(UTXOPool pool) { this.pool = new UTXOPool(pool); }

    // Return a set of transactions chosen greedily to maximize fees.
    public Transaction[] selectTransactions(Transaction[] candidates) {
        if (candidates == null || candidates.length == 0) return new Transaction[0];

        UTXOPool working = new UTXOPool(pool);
        List<Transaction> remaining = new ArrayList<>(Arrays.asList(candidates));
        List<Transaction> accepted = new ArrayList<>();

        while (true) {
            Transaction best = null; double bestFee = Double.NEGATIVE_INFINITY;
            for (Transaction tx : remaining) {
                if (!isValidTxAgainstPool(tx, working)) continue;
                double fee = computeFeeUsingPool(tx, working);
                if (fee > bestFee) { best = tx; bestFee = fee; }
            }
            if (best == null) break;
            applyTxToPool(best, working);
            accepted.add(best);
            remaining.remove(best);
        }

        return accepted.toArray(new Transaction[0]);
    }

    // --- helper methods (minimal, internal) ---
    private double computeFeeUsingPool(Transaction tx, UTXOPool p) {
        double in = 0, out = 0;
        for (int i=0;i<tx.numInputs();i++) {
            Transaction.Input I = tx.getInput(i);
            UTXO u = new UTXO(I.prevTxHash, I.outputIndex);
            Transaction.Output o = p.getTxOutput(u);
            if (o == null) return Double.NEGATIVE_INFINITY;
            in += o.value;
        }
        for (Transaction.Output o: tx.getOutputs()) out += o.value;
        return in - out;
    }

    private boolean isValidTxAgainstPool(Transaction tx, UTXOPool p) {
        HashSet<UTXO> seen = new HashSet<>(); double in = 0, out = 0;
        for (int i=0;i<tx.numInputs();i++) {
            Transaction.Input I = tx.getInput(i);
            UTXO u = new UTXO(I.prevTxHash, I.outputIndex);
            if (!p.contains(u) || !seen.add(u)) return false;
            Transaction.Output prev = p.getTxOutput(u);
            if (!Crypto.verifySignature(prev.address, tx.getRawDataToSign(i), I.signature)) return false;
            in += prev.value;
        }
        for (Transaction.Output o: tx.getOutputs()) { if (o.value < 0) return false; out += o.value; }
        return in + 1e-12 >= out;
    }

    private void applyTxToPool(Transaction tx, UTXOPool p) {
        for (int i=0;i<tx.numInputs();i++) p.removeUTXO(new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex));
        byte[] h = tx.getHash();
        for (int i=0;i<tx.numOutputs();i++) p.addUTXO(new UTXO(h,i), tx.getOutput(i));
    }
}
