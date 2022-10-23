import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;

public class test {

    public static void main (String[]args){
        double k = 100000;
        double n = 0;
        long start = currentTimeMillis();
        while (n < k){
            for (int x = 0; x < 1000000000; x++){

            }
            n+=50;
            System.out.println(currentTimeMillis()-start);
        }
        double x = ((double)5/(double)3);
        System.out.println(x);
        long end = nanoTime();
        System.out.println(end-start);
    }
}
