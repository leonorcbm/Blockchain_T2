import java.util.ArrayList;
import java.util.List;

public class Brute {

    public static float[] BruteF(TxHandler handler) {
        List<Transaction> accepted = handler.getAcceptedTxs();
        float[] Bigtx = new float[2];
        float F = 0.0f;

        // Store fees in a List
        List<Float> fees = new ArrayList<>();
        for (Transaction tx : accepted) {
            fees.add((float) handler.getTxFee(tx));
        }

        for (int i = 0; i < fees.size(); i++) {
            for (int j = i + 1; j < fees.size(); j++) {
                float sum = fees.get(i) + fees.get(j);
                if (sum > F) {
                    F = sum;
                    Bigtx[0] = fees.get(i);
                    Bigtx[1] = fees.get(j);
                }
            }
        }
        return Bigtx;
    }

    public static float[] BruteF_Three(TxHandler handler) {
        List<Transaction> accepted = handler.getAcceptedTxs();
        List<Float> fees = new ArrayList<>();
        for (Transaction tx : accepted) {
            fees.add((float) handler.getTxFee(tx));
        }

        if (fees.size() < 3) {
            return new float[]{0.0f, 0.0f, 0.0f};
        }

        float F = 0.0f;
        float[] Bigtx = new float[3];

        // Triple nested loops for unique triplets (i, j, k)
        for (int i = 0; i < fees.size(); i++) {
            for (int j = i + 1; j < fees.size(); j++) {
                for (int k = j + 1; k < fees.size(); k++) {

                    float sum = fees.get(i) + fees.get(j) + fees.get(k);

                    if (sum > F) {
                        F = sum;
                        Bigtx[0] = fees.get(i);
                        Bigtx[1] = fees.get(j);
                        Bigtx[2] = fees.get(k);
                    }
                }
            }
        }
        return Bigtx;
    }

    public static float[] BruteF_MaxAll(TxHandler handler) {
        List<Transaction> accepted = handler.getAcceptedTxs();
        List<Float> fees = new ArrayList<>();
        for (Transaction tx : accepted) {
            fees.add((float) handler.getTxFee(tx));
        }

        List<List<Float>> allSubsets = new ArrayList<>();
        allSubsets.add(new ArrayList<>());

        for (float fee : fees) {
            int currentSize = allSubsets.size();

            for (int i = 0; i < currentSize; i++) {
                List<Float> existingSubset = allSubsets.get(i);
                List<Float> newSubset = new ArrayList<>(existingSubset);
                newSubset.add(fee);
                allSubsets.add(newSubset);
            }
        }
        float maxFee = -1.0f;
        List<Float> bestSubset = new ArrayList<>();

        for (List<Float> subset : allSubsets) {
            float sum = 0;
            for (float f : subset) sum += f;

            if (sum > maxFee) {
                maxFee = sum;
                bestSubset = subset;
            }
        }

        float[] result = new float[bestSubset.size()];
        for (int i = 0; i < bestSubset.size(); i++) {
            result[i] = bestSubset.get(i);
        }
        return result;
    }
}
