import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Part_Graph {
    public static void main(String[] args) throws Exception {
        ArrayList<Buffer.BufferNodePair> p = new ArrayList<>();
        p.add(new Buffer.BufferNodePair(new SplayNet.CommunicatingNodes(1, 2, 3)));
        p.add(new Buffer.BufferNodePair(new SplayNet.CommunicatingNodes(1, 2, 3)));
        p.add(new Buffer.BufferNodePair(new SplayNet.CommunicatingNodes(1, 1, 4)));
        p.add(new Buffer.BufferNodePair(new SplayNet.CommunicatingNodes(1, 3, 4)));
        p.add(new Buffer.BufferNodePair(new SplayNet.CommunicatingNodes(1, 2, 1)));
        for(Buffer.BufferNodePair element: p){
            System.out.print(element.getU() + "-" + element.getV() + ";");
        }
        System.out.println();
        call_part_graph(p, 4);
        for(ArrayList<Buffer.BufferNodePair> j: call_part_graph(p, 4)){
            for(Buffer.BufferNodePair element: j){
                System.out.print(element.getU() + "-" + element.getV() + ";");
            }
            System.out.println();
        }
        /*for (int k = 0; k<10;k++) {
            long start = System.nanoTime();
            double elapsedTime = (double) (System.nanoTime() - start) / 1_000_000_000;
            System.out.println("Ohne Parser: " + elapsedTime);

            long start1 = System.nanoTime();
            for (int i = 0; i < 100; i++) {
                call_python_script();
            }
            double elapsedTime1 = (double) (System.nanoTime() - start1) / 1_000_000_000;
            System.out.println("Mit Parser: " + elapsedTime1);
        }*/
    }

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
        ArrayList<Buffer.BufferNodePair> originalList = list;
        ArrayList<ArrayList<Buffer.BufferNodePair>> newList = new ArrayList<>();

        Map<Integer, Integer> dic = new HashMap<Integer, Integer>();
        ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();
        int count = 0;
        for (Buffer.BufferNodePair element: originalList){
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
        Thread.sleep(1000);
        Runtime.getRuntime().exec("python3 src/main/java/metis.py");
        Thread.sleep(1000);
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader(String.valueOf(Paths.get("./json/", "output.json")));
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

        for(ArrayList<Integer> element: newComponentsNodes){
            ArrayList<Buffer.BufferNodePair> newComponent = new ArrayList<>();
            for(Integer node: element){
                ArrayList<Buffer.BufferNodePair> found = new ArrayList<>();
                for(Buffer.BufferNodePair request: originalList){
                    if(request.nodePair.getU() == node || request.nodePair.getV() == node){
                        found.add(request);
                        newComponent.add(request);
                    }
                }
                originalList.removeAll(found);
                newList.add(newComponent);
            }
        }
        if (originalList.size() > 0) throw new Exception("this.listBufferNodePairs bigger than one");

        return newList;

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
