import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Part_Graph {

    public static void writeInJSON(ArrayList<ArrayList<Integer>> matrix, double maxComponentSize) throws Exception {


        JSONObject data = new JSONObject();
        data.put("maxComponentSize", (int)maxComponentSize);

        JSONArray m = new JSONArray();
        for(ArrayList<Integer> element: matrix){
            JSONArray array = new JSONArray();
            for (Integer number: element){
                array.add(number);
            }
            m.add(array);
        }
        data.put("array", m);
        Files.write(Paths.get("./json/", "input.json"), data.toJSONString().getBytes());
    }

    public static ArrayList<ArrayList<Buffer.BufferNodePair>> call_part_graph(ArrayList<Buffer.BufferNodePair> list, double maxComponentSize) throws Exception {
        ArrayList<ArrayList<Buffer.BufferNodePair>> newList = new ArrayList<>();

        Map<Integer, Integer> dic = new HashMap<Integer, Integer>();
        ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();
        int count = 0;
        for (Buffer.BufferNodePair element: list){
            if (!dic.containsKey(element.nodePair.getU())){
                dic.put(element.nodePair.getU(), count);
                count++;
            }
            if (!dic.containsKey(element.nodePair.getV())){
                dic.put(element.nodePair.getV(), count);
                count++;
            }
            while(matrix.size() < count){
                matrix.add(new ArrayList<Integer>());
            }
            matrix.get(dic.get(element.nodePair.getU())).add(dic.get(element.nodePair.getV()));
            matrix.get(dic.get(element.nodePair.getV())).add(dic.get(element.nodePair.getU()));
        }
        writeInJSON(matrix, maxComponentSize);
        Process process = Runtime.getRuntime().exec("python3 src/main/python/louvain.py");

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String output = stdInput.readLine();

        String s = String.valueOf(Paths.get("./json/", "output.json"));
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader(s);
        JSONObject obj = (JSONObject) jsonParser.parse(reader);
        JSONArray resultArray =  (JSONArray) obj.get("array");
        int cuts = (int) (long)obj.get("cuts");

        ArrayList<ArrayList<Integer>> newComponentsNodes = new ArrayList<>();
        for (int k = 0; k < cuts; k++){
            newComponentsNodes.add(new ArrayList<Integer>());
            for(int n = 0; n < resultArray.size(); n++){
                if (Integer.parseInt(resultArray.get(n).toString()) == k){
                    newComponentsNodes.get(k).add(getKeyByValue(dic, n));
                }
            }
        }
        /*
        for(ArrayList<Integer> x: newComponentsNodes){
            MainBufferSplayNet.printLogs(String.valueOf(x));
        }
        */
        for(ArrayList<Integer> element: newComponentsNodes){
            ArrayList<Buffer.BufferNodePair> newComponent = new ArrayList<>();
            ArrayList<Buffer.BufferNodePair> found = new ArrayList<>();
            for(Buffer.BufferNodePair request: list){
                if(element.contains(request.nodePair.getU()) && element.contains(request.nodePair.getV())){
                    found.add(request);
                    newComponent.add(request);
                }
            }
            list.removeAll(found);
            newList.add(newComponent);
        }
        /*
        MainBufferSplayNet.printLogs("new cluster");
        for(ArrayList<Buffer.BufferNodePair> j: newList){
            for(Buffer.BufferNodePair element: j){
                MainBufferSplayNet.printLogs(element.getU() + "-" + element.getV() + ";");
            }
            MainBufferSplayNet.printLogs("");
        }
        */
        ArrayList<Buffer.BufferNodePair> found = new ArrayList<>();
        for(Buffer.BufferNodePair element: list){
            boolean foundU = false;
            boolean foundV = false;
            int uIndex = -1;
            int vIndex = -1;
            for(int k = 0; k < newComponentsNodes.size(); k++){
                if(newComponentsNodes.get(k).contains(element.getU())){
                    foundU = true;
                    uIndex = k;
                }
                if(newComponentsNodes.get(k).contains(element.getV())){
                    foundV = true;
                    vIndex = k;
                }
                if (foundU && foundV) break;
            }
            if (uIndex < 0 || vIndex < 0) throw new Exception("edge between clusters not found");
            int countU = 0;
            int countV = 0;
            for (Buffer.BufferNodePair edge: newList.get(uIndex)){
                if (edge.nodePair.getU() == element.nodePair.getU() || edge.nodePair.getV() == element.nodePair.getU()){
                    countU++;
                }
            }
            for (Buffer.BufferNodePair edge: newList.get(vIndex)){
                if (edge.nodePair.getU() == element.nodePair.getV() || edge.nodePair.getV() == element.nodePair.getV()){
                    countV++;
                }
            }
            if (countU > countV){
                newList.get(uIndex).add(element);
            }else{
                newList.get(vIndex).add(element);
            }
            found.add(element);

        }
        list.removeAll(found);
        if (list.size() > 0) throw new Exception("this.listBufferNodePairs bigger than one");

        /*
        MainBufferSplayNet.printLogs("final cluster");
        for(ArrayList<Buffer.BufferNodePair> j: newList){
            for(Buffer.BufferNodePair element: j){
                MainBufferSplayNet.printLogs(element.getU() + "-" + element.getV() + ";");
            }
            MainBufferSplayNet.printLogs("");
        }
        for (ArrayList<Buffer.BufferNodePair> element: newList){
            if (element.size() <= 0){
                throw new Exception("leere liste wird hinzugefügt");
            }
        }
        */
        return newList;
    }

    public static boolean containsNode(final ArrayList<Buffer.BufferNodePair> list, final int value){
        return list.stream().anyMatch(o -> o.getU() == value) || list.stream().anyMatch(o -> o.getV() == value);
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}
