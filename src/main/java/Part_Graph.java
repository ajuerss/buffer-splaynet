import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Part_Graph {

    public static void main(String[] args) throws Exception {
        for (int k = 0; k<10;k++) {
            long start = System.nanoTime();
            double elapsedTime = (double) (System.nanoTime() - start) / 1_000_000_000;
            System.out.println("Ohne Parser: " + elapsedTime);

            long start1 = System.nanoTime();
            for (int i = 0; i < 100; i++) {
                call_python_script();
            }
            double elapsedTime1 = (double) (System.nanoTime() - start1) / 1_000_000_000;
            System.out.println("Mit Parser: " + elapsedTime1);
        }
    }

    //https://stackoverflow.com/questions/14155669/call-python-script-from-bash-with-argument
    public static void call_python_script() throws Exception {
        int[][] var = {{1, 1, 2}, {0, 0}, {0}, {4}, {3}};
        int num_cuts = 2;
        StringBuilder prop = new StringBuilder();
        for (int[] ints : var) {
            prop.append(Arrays.toString(ints));
            prop.append(".");
        }
        prop.deleteCharAt(prop.length() - 1);
        String adj_str = prop.toString();
        String cuts_str = String.valueOf(num_cuts);
        adj_str = adj_str.replaceAll(" ", "");
        Process p = Runtime.getRuntime().exec("python3 src/main/java/metis.py " + adj_str + " " + cuts_str);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
        /*
        System.out.println("Here is the standard output of the command:\n");
        int number_of_cuts = Integer.parseInt(stdInput.readLine());

        String str = stdInput.readLine();
        str = str.substring(0, str.length() - 1);
        str = str.substring(1);
        String[] str_arr = str.split(",");
        List<Integer> int_list = new ArrayList<Integer>();
        for(String s : str_arr){
            int_list.add(Integer.parseInt(s.trim()));
        }
        System.out.println(int_list);
        System.out.println(number_of_cuts);

        String k = null;
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((k = stdError.readLine()) != null) {
            System.out.println(k);
        }
        */
    }

}
