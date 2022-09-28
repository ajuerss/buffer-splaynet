import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class printCSV {
    public static void main (String[]args) throws Exception {
        printRequests();
    }

    public static void printRequests() throws Exception {
        int num = 50;
        int nodes = 15;
        int k = 0;
        double p = 0.3;
        List<String[]> dataLines = new ArrayList<>();
        while (k < num){
            Random rand = new Random();
            int ran1 = rand.nextInt(nodes) + 1;
            int ran2 = rand.nextInt(nodes) + 1;
            if (ran1 != ran2){
                k++;
                dataLines.add(new String[]{ String.valueOf(k), String.valueOf(ran1), String.valueOf(ran2) });
            }
        }
        for (int o = 0; o < dataLines.size()-1; o++){
            Random r = new Random();
            double randomValue = r.nextDouble();
            System.out.println(randomValue);
            if (randomValue < p){
                dataLines.get(o+1)[1] = dataLines.get(o)[1];
                dataLines.get(o+1)[2] = dataLines.get(o)[2];
            }
        }
        Path path = FileSystems.getDefault().getPath("./csv/", "n"+nodes+"seq"+num+".csv");
        writeLineByLine(dataLines, path);
    }

    public static void writeLineByLine(List<String[]> lines, Path path) throws Exception {
        try (CSVWriter writer = new CSVWriter(new FileWriter(path.toString()), ',', CSVWriter.NO_QUOTE_CHARACTER);) {
            for (String[] line : lines) {
                writer.writeNext(line);
            }
        }
    }
}
