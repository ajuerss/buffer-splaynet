import com.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/* This class transformed traced from FB, HPC etc. to the format which this implementation can read*/
public class csvWriter {

    public static void main (String[]args) throws IOException {
        String line = "";
        File folder = new File("./1toConvert/old");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File file: listOfFiles){
            String name = file.getName();
            String path = file.getAbsolutePath();
            BufferedReader br = new BufferedReader(new FileReader(path));
            List<String[]> dataLines = new ArrayList<>();
            int count = 1;
            while ((line = br.readLine()) != null){
                String[] values = line.replaceAll("\"", "").split(",");
                int u = Integer.parseInt(values[0]);
                int v = Integer.parseInt(values[1]);
                dataLines.add(new String[]{Integer.toString(count++), Integer.toString(u), Integer.toString(v)});
            }

            File newFile = new File("./1toConvert/new/new" + name);
            try {
                FileWriter outputfile = new FileWriter(newFile);
                CSVWriter writer = new CSVWriter(outputfile, ',', CSVWriter.NO_QUOTE_CHARACTER);
                for (String[] element: dataLines){
                    writer.writeNext(element);
                }
                writer.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
