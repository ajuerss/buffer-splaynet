import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    public static List<SplayNet.CommunicatingNodes> readCSV(String path, long maxRequests) throws IOException {
        List<SplayNet.CommunicatingNodes> ListOfCommNode = new ArrayList<>();
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader(path));
        int count = 0;
        long maxCount;
        if (maxRequests > 0){
            maxCount = maxRequests;
        }else{
            maxCount = 100000000;
        }
        while ((line = br.readLine()) != null && count < maxCount){
            String[] values = line.replaceAll("\"", "").split(",");
            int u = Integer.parseInt(values[1]);
            int v = Integer.parseInt(values[2]);
            SplayNet.CommunicatingNodes pair = new SplayNet.CommunicatingNodes(Integer.parseInt(values[0]), u, v);
            ListOfCommNode.add(pair);
            count++;
        }
        return ListOfCommNode;
    }

    public static ArrayList<Integer> extractNodes(List<SplayNet.CommunicatingNodes> inputList){
        ArrayList<Integer> outputList = new ArrayList<>();
        for (SplayNet.CommunicatingNodes element: inputList){
            if (!outputList.contains(element.getU()) ) outputList.add(element.getU());
            if (!outputList.contains(element.getV()) ) outputList.add(element.getV());
        }
        return outputList;
    }
}
