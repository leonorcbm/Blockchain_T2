import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Brute {


    public static float[]  BruteF(UTXOPool pool, Transaction[] allTxs, TxHandler handler){
        List<Object> pooltx = handler.getPool();
        float[] Bigtx = new float[2]; // size is 2
        Float F = 0.0F;

        HashMap<Integer, Float> feesTx = new HashMap<Integer, Float>();
        Integer i = 0;
        for (Transaction tx : handler.getAcceptedTxs()){
            float fee = (float) handler.getTxFee(tx);
            feesTx.put(i, fee);
            /*System.out.println("Tx"+i);
            System.out.println(feesTx.get(i));*/
            i++;
        }

        for (i= 0; i < feesTx.size(); i++) {
            for (int j = i + 1; j < feesTx.size(); j++) {
                Float sum = feesTx.get(i)+feesTx.get(j);
                if (sum > F){
                    F = sum;
                    Bigtx[0] = feesTx.get(i);
                    Bigtx[1] = feesTx.get(j);
                }
            }

        }
        return Bigtx;
    }

}
