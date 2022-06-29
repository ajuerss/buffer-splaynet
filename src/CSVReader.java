import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    public static List<SplayNet.CommunicatingNodes> readCSV(String path) throws IOException {
        List<SplayNet.CommunicatingNodes> ListOfCommNode = new ArrayList<>();
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader(path));
        while ((line = br.readLine()) != null){
            String[] values = line.replaceAll("\"", "").split(",");
            int u = Integer.parseInt(values[1]);
            int v = Integer.parseInt(values[2]);
            SplayNet.CommunicatingNodes pair = new SplayNet.CommunicatingNodes(Integer.parseInt(values[0]), u, v);
            ListOfCommNode.add(pair);
        }
        return ListOfCommNode;
    }

    public static ArrayList<Integer> extractNodes(List<SplayNet.CommunicatingNodes> inputList){
        ArrayList<Integer> outputList = new ArrayList<>();
        for (SplayNet.CommunicatingNodes element: inputList){
            if (!outputList.contains(element.u) ){
                outputList.add(element.u);
            }
            if (!outputList.contains(element.v) ){
                outputList.add(element.v);
            }
        }
        return outputList;
    }
}
