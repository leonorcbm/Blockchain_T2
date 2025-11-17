import java.util.List;

public class Brute {


    public int  BruteF(int sub, UTXOPool pool){
        TxHandler handler = new TxHandler(pool);
        List<Object> pooltx = handler.getPool();

        for (Object tx : pooltx) {
            //TODO
            // meter aqui a o index e somar o index x vezes, guardar valor e ver
            // exemplo
            // [1, 2, 3, 4, 5]
            // somar 1+2 1+3 1+4 1+5
            // mas depois só temos de fazer 2+3 2+4 e 2+5
            // até chegarmos a penultima, isto é, o 4
            // guardar número de operações e tempo

        }

        return 2;
    }




}
