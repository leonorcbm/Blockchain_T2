import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Brute {


    public static float[] BruteF(UTXOPool pool, Transaction[] allTxs, TxHandler handler) {
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

        // 1. Start with a list that holds one empty subset
        List<List<Float>> allSubsets = new ArrayList<>();
        allSubsets.add(new ArrayList<>());

        // 2. Loop through every fee available
        for (float fee : fees) {
            // Get the current number of subsets we have found so far
            int currentSize = allSubsets.size();

            // For every subset we already have...
            for (int i = 0; i < currentSize; i++) {
                List<Float> existingSubset = allSubsets.get(i);

                // Create a NEW subset that is a copy of the existing one + the new fee
                List<Float> newSubset = new ArrayList<>(existingSubset);
                newSubset.add(fee);

                // Add this new combination to our master list
                allSubsets.add(newSubset);
            }
        }

        //

        // 3. Find the winner among all generated subsets
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

        // 4. Convert the winning list to a float[] array
        float[] result = new float[bestSubset.size()];
        for (int i = 0; i < bestSubset.size(); i++) {
            result[i] = bestSubset.get(i);
        }

        return result;
    }


}
