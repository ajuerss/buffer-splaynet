import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MainBufferSplayNet {

    public static long[][] timestampCollector;
    public static List<DatasetTraces> traceCollection = new ArrayList<>();
    public static long timestamp = 0;
    public static double starvationParameter = 0.5;
    public static boolean starvationSimulation = false;

    public static boolean log = false;

    public static void printLogs(String message){
        if (log) System.out.println(message);
    }


    public static class DatasetTraces {
        public String name;
        public List<BufferTraces> regroupingTrace = new ArrayList<>();
        public List<BufferTraces> distanceTrace = new ArrayList<>();
        public List<BufferTraces> clusterNodeByNodeTrace = new ArrayList<>();
        public List<BufferTraces> clusterDistanceTrace = new ArrayList<>();
        public List<BufferTraces> clusterEdgeWeightTrace = new ArrayList<>();
        public List<List<BufferTraces>> allTraces = Arrays.asList(regroupingTrace, distanceTrace, clusterNodeByNodeTrace, clusterDistanceTrace, clusterEdgeWeightTrace);

        public DatasetTraces(String name) {
            this.name = name;
        }

    }
    public static class BufferTraces {
        private final int buffersize;
        private List<SplayNet.CommunicatingNodes> modifiedTrace = new ArrayList<>();

        public BufferTraces(int buffersize) {
            this.buffersize = buffersize;
        }
    }

    private static class SplaynetParameters {
        List<Integer> bufferSizes;
        List<SplayNet.CommunicatingNodes> nodeRequestPairs;
        public SplaynetParameters(List<Integer> bufferSizes, List<SplayNet.CommunicatingNodes> nodeRequestPairs){
            this.bufferSizes = bufferSizes;
            this.nodeRequestPairs = nodeRequestPairs;
        }
    }

    private static class DataParameter {
        double a;
        double p;
        int n;
        int seq;
        double starvation = -1.0;
        public DataParameter(double a, double p, int n, int seq){
            this.a = a;
            this.p = p;
            this.n = n;
            this.seq = seq;
        }

        public String getDatasetName(){
            return "result" + "-" + "n" + this.n + "-seq" + this.seq + "-a" + this.a + "-p" + this.p;
        }
    }

    private static class Result {
        int bufferSize;
        long serviceCost;
        long rotationCost;
        long partitions;
        long clusters;

        long executionTime;

        public Result(int bufferSize, long serviceCost, long rotationCost, long partitions, long clusters, long executionTime){
            this.bufferSize = bufferSize;
            this.serviceCost = serviceCost;
            this.rotationCost = rotationCost;
            this.partitions = partitions;
            this.clusters = clusters;
            this.executionTime = executionTime;
        }
    }

    public static void main (String[]args) throws Exception {

        Scanner s = new Scanner(System.in);

        System.out.println("Simulation (Y/N)?");
        String n = s.next();
        if (n.equalsIgnoreCase("Y")) {
            simulation(s);
        } else if (n.equalsIgnoreCase("N")){
            // declare where logs of current iterations of the toplogy are needed
            log = printLogQuestionaire(s);
            // get parameters for Experiment (Buffersize and Communication pairs). The Splaynet tree will be initialized
            SplaynetParameters parameters = useParametersQuestionaire(s);
            DatasetTraces datasetTrace = new DatasetTraces("trace");
            traceCollection.add(datasetTrace);
            runExperiment(parameters, 0);
            runExperiment(parameters, 1);
            runExperiment(parameters, 2);
            runExperiment(parameters, 3);
            runExperiment(parameters, 4);
        } else {
            throw new Exception("wrong input");
        }
    }

    public static void simulation(Scanner s) throws Exception {
        File folder = new File("./csvExperiment");
        File[] listOfFiles = folder.listFiles();
        List<Integer> bufferSizes = getBuffersizeQuestionnaire(s);
        List<Double> starvation_parameters = getStarvationParametersQuestionnaire(s);
        assert listOfFiles != null;
        for (File file: listOfFiles){
            String[] k = file.getAbsolutePath().split("-");
            DataParameter dataP = new DataParameter(Double.parseDouble(k[5].substring(1)),Double.parseDouble(k[6].substring(1, 4)),Integer. parseInt(k[3].substring(1)),Integer. parseInt(k[4].substring(3)));
            DatasetTraces datasetTrace = new DatasetTraces(dataP.getDatasetName());
            traceCollection.add(datasetTrace);
            List<SplayNet.CommunicatingNodes> inputPairs = simulationGetCSVdata(file.getAbsolutePath());
            SplayNet.CommunicatingNodes old = inputPairs.get(0);
            int total = 0;
            int p = 0;
            for(SplayNet.CommunicatingNodes element: inputPairs){
                if (element.getU() == old.getU() && element.getV() == old.getV()) p++;
                old = element;
                total++;
            }
            System.out.println("Temporal locality: " + p + "/" + total);
            SplaynetParameters parameters = new SplaynetParameters(bufferSizes, inputPairs);
            System.out.printf("Dataset (a = %f, p = %f, n = %d, seq = %d):\n", dataP.a, dataP.p, dataP.n, dataP.seq);
            //writeToTxt(runExperiment(parameters, 0), 0, dataP);
            for (Double parameter: starvation_parameters){
                if (starvation_parameters.size() > 1) dataP.starvation = parameter;
                starvationParameter = parameter;
                //writeToTxt(runExperiment(parameters,  1), 1, dataP);
            }
            //writeToTxt(runExperiment(parameters,  2), 2, dataP);
            //writeToTxt(runExperiment(parameters,  3), 3, dataP);
            writeToTxt(runExperiment(parameters,  4), 4, dataP);
        }
        createTraceJSON();
    }

    public static ArrayList<Result> runExperiment(SplaynetParameters parameters, int algorithm) throws Exception {
        // run experiment with Buffer(Distance Algorithm) on Splaynet
        ArrayList<Result> results = new ArrayList<>();
        // monitor results for each Buffersize
        int index = 0;
         timestampCollector = new long[parameters.bufferSizes.size()][parameters.nodeRequestPairs.size()];
        for (Integer bufferSize: parameters.bufferSizes){
            BufferTraces bufferTraces = new BufferTraces(bufferSize);
            if (algorithm == 0) traceCollection.get(traceCollection.size()-1).regroupingTrace.add(bufferTraces);
            if (algorithm == 1) traceCollection.get(traceCollection.size()-1).distanceTrace.add(bufferTraces);
            if (algorithm == 2) traceCollection.get(traceCollection.size()-1).clusterDistanceTrace.add(bufferTraces);
            if (algorithm == 3) traceCollection.get(traceCollection.size()-1).clusterEdgeWeightTrace.add(bufferTraces);
            if (algorithm == 4) traceCollection.get(traceCollection.size()-1).clusterNodeByNodeTrace.add(bufferTraces);
            // Create and initialize Splaynet with Parameters
            SplayNet sn_current = new SplayNet();
            ArrayList<Integer> nodeList = CSVReader.extractNodes(parameters.nodeRequestPairs);
            sn_current.setNumberNodes(nodeList.size());
            sn_current.initializeTimestamps(parameters.nodeRequestPairs.size()+1);
            initializeSplaynet(sn_current, nodeList);
            sn_current.setInsertionOver();
            printLogs("input tree:");
            //sn_current.printPreorder(sn_current.getRoot());
            printLogs("");
            sn_current.setTimestamps(0, timestamp++);
            long start = System.nanoTime();
            if (algorithm == 0 || algorithm == 1){
                runExperimentDistanceSplaynet(sn_current, bufferSize, algorithm, parameters.nodeRequestPairs);
                timestampCollector[index++] = Arrays.copyOfRange(sn_current.getAllTimestamps(), 1, sn_current.getAllTimestamps().length);
                timestamp = 0;
            }else if (algorithm > 1){
                runExperimentClusterSplaynet(sn_current, bufferSize, parameters.nodeRequestPairs, algorithm);
            }
            long end = System.nanoTime();
            sn_current.setExecutionTime((end-start)/1000000);
            System.out.println("For Buffersize " + bufferSize + ":");
            System.out.println("ServingCost: " + sn_current.getServiceCost() + " RotationCost:" + sn_current.getRotationCost() + " partitions:" + sn_current.partitions + "/" + sn_current.clusters + " Time:" + sn_current.getExecutionTime());
            printLogs("Final tree:");
            //sn_current.printPreorder(sn_current.getRoot());
            printLogs("");
            results.add(new Result(bufferSize, sn_current.getServiceCost(), sn_current.getRotationCost(), sn_current.partitions, sn_current.clusters, end-start));

        }
        return results;

    }

    public static void initializeSplaynet(SplayNet sn, ArrayList<Integer> list){
        sn.insertBalancedBST(list);
        sn.assignLastParents(sn.getRoot(), -1, Integer.MAX_VALUE);
    }

    public static void runExperimentDistanceSplaynet(SplayNet sn1, int bufferSize, int algorithm, List<SplayNet.CommunicatingNodes> nodeRequestPairs) throws Exception {
        Buffer buffer = new Buffer(sn1, bufferSize);
        sn1.setBuffer(buffer);

        for (SplayNet.CommunicatingNodes element: nodeRequestPairs){
            if (buffer.isSpace()){
                //buffer.increaseTimestamp();
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
            }else{
                if(buffer.calcPriority()){
                    if (algorithm == 0) popElementFromBuffer(buffer, sn1, true);
                    if (algorithm == 1) popElementFromBuffer(buffer, sn1, false);
                }
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
            }
            printLogs("Incoming Request:" + element.getU() + " " + element.getV());
        }
        while (!buffer.getListBufferNodePairs().isEmpty()){
            buffer.calcPriority();
            if(!buffer.getListBufferNodePairs().isEmpty()){
                if (algorithm == 0) popElementFromBuffer(buffer, sn1, true);
                if (algorithm == 1) popElementFromBuffer(buffer, sn1, false);
            }
        }
        if (algorithm == 0) getBufferTraces(traceCollection.get(traceCollection.size()-1).regroupingTrace, bufferSize).modifiedTrace = buffer.getTraceOrder();
        if (algorithm == 1) getBufferTraces(traceCollection.get(traceCollection.size()-1).distanceTrace, bufferSize).modifiedTrace = buffer.getTraceOrder();
    }

    public static void runExperimentClusterSplaynet(SplayNet sn1, int bufferSize, List<SplayNet.CommunicatingNodes> nodeRequestPairs, int algorithm) throws Exception {
        Buffer buffer = new Buffer(sn1, bufferSize);
        sn1.setBuffer(buffer);
        for (SplayNet.CommunicatingNodes element: nodeRequestPairs){
            if (buffer.isSpace()){
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
                printLogs("Incoming Request:" + element.getU() + " " + element.getV());
            }else{
                if (buffer.calcPriority()){
                    assert buffer.getBufferSize() == buffer.getListBufferNodePairs().size();
                    printLogs("Clustering buffer Start");
                    buffer.startClustering(algorithm);
                }
                sn1.printPreorder(sn1.getRoot());
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
                printLogs("Incoming Request:" + element.getU() + " " + element.getV());
            }
        }
        while (!buffer.getListBufferNodePairs().isEmpty()){
            if (buffer.calcPriority()){
                printLogs("Clustering buffer");
                buffer.startClustering(algorithm);
                printLogs("Clustering buffer done");
            }
        }
        if (algorithm == 2) getBufferTraces(traceCollection.get(traceCollection.size()-1).clusterDistanceTrace, bufferSize).modifiedTrace = buffer.getTraceOrder();
        if (algorithm == 3) getBufferTraces(traceCollection.get(traceCollection.size()-1).clusterEdgeWeightTrace, bufferSize).modifiedTrace = buffer.getTraceOrder();
        if (algorithm == 4) getBufferTraces(traceCollection.get(traceCollection.size()-1).clusterNodeByNodeTrace, bufferSize).modifiedTrace = buffer.getTraceOrder();
    }

    public static void popElementFromBuffer(Buffer buffer, SplayNet sn1, boolean grouping) throws Exception {
        if (!grouping) buffer.sortByPriority();
        printLogs("Elements in Buffer:");
        /*
        for (Buffer.BufferNodePair k: buffer.getListBufferNodePairs()){
            printLogs(String.format("ID:%d U:%d V:%d P:%f DST:%d TS:%d\n", k.nodePair.getId(), k.nodePair.getU(), k.nodePair.getV(), k.getPriority(), k.getDistance(), k.getTimestamp()));
        }
        */
        Buffer.BufferNodePair popNode = buffer.getListBufferNodePairs().get(0);
        buffer.removeListBufferNodePairs(0);
        buffer.modifiedTraceOrderAdd(popNode.nodePair);
        printLogs(String.format("Served Requested %d and %d\n", popNode.nodePair.getU(), popNode.nodePair.getV()));
        sn1.increaseServingCost(popNode.getDistance());
        sn1.commute(popNode.nodePair.getU(), popNode.nodePair.getV());

        sn1.setTimestamps(popNode.getNodePair().getId(), timestamp++);
        buffer.increaseTimestamp();
        printLogs("Zwischenstand:");
        sn1.printPreorder(sn1.getRoot());
        printLogs("ServingCost: " + sn1.getServiceCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
    }

    public static List<SplayNet.CommunicatingNodes> getCSVdata(long maxRequests) throws IOException {
        //String path = "./csvExperiment/zipf-dataset-N15-seq100-a1.0-p0.0.csv";
        File folder = new File("./csvExperiment");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        String path = listOfFiles[0].getAbsolutePath();
        return CSVReader.readCSV(path, maxRequests);
    }

    public static List<SplayNet.CommunicatingNodes> simulationGetCSVdata(String path) throws IOException {
        return CSVReader.readCSV(path, -1);
    }

    public static List<SplayNet.CommunicatingNodes> getCustomCommunicationNodes(Scanner s){
        List<SplayNet.CommunicatingNodes> inputPairs= new ArrayList<>();
        System.out.println("Enter number of requests:");
        int requestNumber = s.nextInt();
        System.out.println("Enter pair of nodes as request:");
        for (int i = 0; i < requestNumber; i++) {
            SplayNet.CommunicatingNodes x = new SplayNet.CommunicatingNodes(i, s.nextInt(), s.nextInt());
            inputPairs.add(x);
        }
        return inputPairs;
    }

    public static boolean printLogQuestionaire(Scanner s) throws Exception {
        boolean log;
        System.out.println("Activate Log (Y/N)?");
        String n = s.next();
        if (n.equalsIgnoreCase("Y")) {
            log = true;
        } else if (n.equalsIgnoreCase("N")){
            log = false;
        } else {
            throw new Exception("wrong input");
        }
        return log;
    }

    public static long limitRequestQuestionnaire(Scanner s) throws Exception {
        long numberRequests;
        System.out.println("Do you want to limit the number of Requests of CSV data (Y/N)?");
        String str = s.next();
        if (str.equalsIgnoreCase("N")) {
            numberRequests = -1;
        } else if (str.equalsIgnoreCase("Y")){
            System.out.println("How Many Requests maximum (f.e. 100000)?");
            numberRequests = s.nextLong();
        } else {
            throw new Exception("wront Input");
        }
        return numberRequests;
    }

    public static SplaynetParameters useParametersQuestionaire(Scanner s) throws Exception {
        System.out.println("Do you want to use CSV data as paramters (Y/N)?");
        String str = s.next();
        List<SplayNet.CommunicatingNodes> inputPairs;
        if (str.equalsIgnoreCase("N")) {
            inputPairs = getCustomCommunicationNodes(s);
        } else if (str.equalsIgnoreCase("Y")){
            long maxRequests = limitRequestQuestionnaire(s);
            inputPairs = getCSVdata(maxRequests);
        } else {
            throw new Exception("wront Input");
        }
        List<Integer> bufferSizes = getBuffersizeQuestionnaire(s);
        return new SplaynetParameters(bufferSizes, inputPairs);
    }

    public static List<Integer> getBuffersizeQuestionnaire(Scanner s) throws Exception {
        System.out.println("Do you want to select custom Buffersizes (Default: 1, 8, 32, 128, 512, 1024, 2048)?");
        String str = s.next();
        List<Integer> bufferSizes;
        if (str.equalsIgnoreCase("N")) {
            bufferSizes = new ArrayList<>(Arrays.asList(1, 8, 32, 128, 512, 1024, 2048));
        } else if (str.equalsIgnoreCase("Y")){
            System.out.println("How many buffersizes do you wanna test?");
            int numberSizes = s.nextInt();
            bufferSizes = getIntegerListInput(s, numberSizes);
        } else {
            throw new Exception("wront Input");
        }
        return bufferSizes;
    }

    public static List<Double> getStarvationParametersQuestionnaire(Scanner s) throws Exception {
        System.out.println("Do you want to track timestamps?");
        String str = s.next();
        List<Double> para;
        if (str.equalsIgnoreCase("Y")) {
            starvationSimulation = true;
            System.out.println("Do you want to test multiple starvation parameters (0.0,0.1,...,1.0)?");
            String str1 = s.next();
            if (str1.equalsIgnoreCase("Y")) {
                para = new ArrayList<>(List.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0));
            } else if (str1.equalsIgnoreCase("N")){
                para = new ArrayList<>(List.of(0.2));
            } else {
                throw new Exception("wront Input");
            }
        } else if (str.equalsIgnoreCase("N")){
            para = new ArrayList<>(List.of(0.2));
        } else {
            throw new Exception("wront Input");
        }
        return para;
    }

    public static List<Integer> getIntegerListInput(Scanner s, int length){
        List<Integer> nodeList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            int x = s.nextInt();
            nodeList.add(x);
        }
        return nodeList;
    }

    public static void writeToTxt(ArrayList<Result> results, int algorithm, DataParameter dataP){
        if ((algorithm == 0 || algorithm == 1) && (dataP.starvation != -1.0 || starvationSimulation)){
            try {
                System.out.println("hayat");
                String path = "./../results/result-" + algorithm + "-" + "n" + dataP.n + "-seq" + dataP.seq + "-a" + dataP.a + "-p" + dataP.p + "-st" + dataP.starvation + "-ts" + ".txt";

                FileWriter myWriter = new FileWriter(path);
                for (int idx = 0; idx < results.size(); idx++){
                    myWriter.write(results.get(idx).bufferSize + ",");
                    myWriter.write(String.valueOf((results.get(idx).serviceCost + results.get(idx).rotationCost)));
                    for (int k = 0; k < timestampCollector[idx].length; k++){
                        myWriter.write("," + timestampCollector[idx][k]);
                    }
                    myWriter.write("\n");
                }
                myWriter.close();
                System.out.println("hayat");
            } catch (IOException e) {
                System.out.println("An error occurred while writing a File");
                e.printStackTrace();
            }
        }
        try {
            String path;
            if (dataP.starvation != -1.0){
                path = "./../results/result-" + algorithm + "-" + "n" + dataP.n + "-seq" + dataP.seq + "-a" + dataP.a + "-p" + dataP.p + "-st" + dataP.starvation + ".txt";
            }else{
                path = "./../results/result-" + algorithm + "-" + "n" + dataP.n + "-seq" + dataP.seq + "-a" + dataP.a + "-p" + dataP.p + ".txt";

            }
            FileWriter myWriter = new FileWriter(path);
            myWriter.write(dataP.n + "," + dataP.seq + "," + dataP.a + "," + dataP.p +"\n");

            for(Result result: results){
                myWriter.write(result.bufferSize + "," + result.serviceCost + "," + result.rotationCost + "," + result.partitions + "," + result.clusters + "," + result.executionTime + "\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing a File");
            e.printStackTrace();
        }
    }

    public static long sum(long[] list) {
        long sum = 0;
        for (long i: list) {
            sum += i;
        }
        return sum;
    }

    public static Node<Integer> transform(SplayNet net){
        Node<Integer> x = new Node<>(net.getRoot().getKey());
        x.left = iterate(net.getRoot().getLeft());
        x.right = iterate(net.getRoot().getRight());
        return x;
    }

    public static Node<Integer> iterate(SplayNet.Node node){
        if (node == null){
            return null;
        }else{
            Node<Integer> x = new Node<>(node.getKey());
            x.left = iterate(node.getLeft());
            x.right = iterate(node.getRight());
            return x;
        }
    }

    public static BufferTraces getBufferTraces(List<BufferTraces> list, int buffersize) throws Exception {
        for (BufferTraces bufferTraces : list) {
            if (bufferTraces.buffersize == buffersize) return bufferTraces;
        }
        throw new Exception("Buffersize of dataset not found in trace object");
    }

    public static void createTraceJSON() throws IOException {
        JSONObject dataset = new JSONObject();
        for (DatasetTraces d: traceCollection){
            int index = 0;
            for (List<BufferTraces> a: d.allTraces){
                for(BufferTraces b: a){
                    String traceString = createTraceString(b.modifiedTrace);
                    JSONObject x = new JSONObject();
                    JSONObject y = new JSONObject();
                    x.put("original-trace", traceString);
                    y.put("trace variants", x);
                    dataset.put(String.format("%d-%d", index, b.buffersize), y);
                }
                index++;
            }
        }
        Files.write(Paths.get("./json/", "traces.json"), Collections.singleton(dataset.toString()));
    }

    public static String createTraceString(List<SplayNet.CommunicatingNodes> list){
        StringBuilder traceString = new StringBuilder("[");
        for (SplayNet.CommunicatingNodes pair: list){
            traceString.append(String.format("('%d', '%d'), ", pair.getU(), pair.getV()));
        }
        traceString.delete(traceString.length()-2, traceString.length());
        traceString.append("]");
        return traceString.toString();
    }
}