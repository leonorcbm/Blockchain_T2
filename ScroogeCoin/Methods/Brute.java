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


}
