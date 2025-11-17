import java.util.List;

public class Brute {


    public int  BruteF(int sub, UTXOPool pool){
        TxHandler handler = new TxHandler(pool);
        List<Object> pooltx = handler.getPool();

        for (Object tx : pooltx) {
            /*meter aqui a o index e somar o index x vezes, guardar valor e ver*/
        }

        return 2;
    }


    /*for ( Object o: )
*/

}
