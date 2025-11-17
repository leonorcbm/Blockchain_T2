import java.util.*;

public class Greedy {

    private final UTXOPool utxoPool;

    public Greedy(UTXOPool pool) {
        this.utxoPool = pool;
    }

    /**
     * Greedy algorithm:
     * - Sort UTXOs by highest value
     * - Pick until targetAmount is reached or exceeded
     * - Return selected UTXOs
     */
    public ArrayList<UTXO> isValidForGreedy(double targetAmount) {

        ArrayList<UTXO> allUTXOs = new ArrayList<>(utxoPool.getAllUTXO());
        ArrayList<UTXO> selected = new ArrayList<>();

        // Sort descending by value
        allUTXOs.sort((a, b) -> {
            double va = utxoPool.getTxOutput(a).value;
            double vb = utxoPool.getTxOutput(b).value;
            return Double.compare(vb, va);   // descending
        });

        double sum = 0;

        for (UTXO u : allUTXOs) {
            if (sum >= targetAmount) break;

            double value = utxoPool.getTxOutput(u).value;
            selected.add(u);
            sum += value;
        }

        // Could not reach the target → return empty set
        if (sum < targetAmount) {
            return new ArrayList<>();
        }

        return selected;
    }
}
