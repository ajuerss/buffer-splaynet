import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************
 * CSV Reader Function.
 * => This class reads the given csv file, which includes the communication sequence.
 * => Each request should be contained in a particular line in the CSV following the
 * pattern "id,u,v", where u and v represent the two communicating nodes.
 * => The order of u and v is not important.
 * **********************************************************************/
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
            maxCount = Integer.MAX_VALUE;
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

    /* Based on the nodes in the communication sequence, the nodes for the communication network
       are extracted. */
    public static ArrayList<Integer> extractNodes(List<SplayNet.CommunicatingNodes> inputList){
        ArrayList<Integer> outputList = new ArrayList<>();
        for (SplayNet.CommunicatingNodes element: inputList){
            if (!outputList.contains(element.getU()) ) outputList.add(element.getU());
            if (!outputList.contains(element.getV()) ) outputList.add(element.getV());
        }
        return outputList;
    }
}
