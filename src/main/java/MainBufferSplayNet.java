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
    public static double starvationParameter = 0.1;         // default starvation parameter
    public static boolean starvationSimulation = false;     // if different SP are tested
    public static boolean monitoreTraceOrder = true;        // if order of traces is logged (for complexity map)
    public static boolean log = false;                      // if logs should be available in console
    /***************************************************************************
     * Storing the order of traces for each data set (for complexity map)
     * **********************************************************************/

    public static class DatasetTraces {
        public String name;
        public List<BufferTraces> regroupingTrace = new ArrayList<>();
        public List<BufferTraces> distanceTrace = new ArrayList<>();
        public List<BufferTraces> clusterNodeByNodeTrace = new ArrayList<>();
        public List<BufferTraces> clusterDistanceTrace = new ArrayList<>();
        public List<BufferTraces> clusterEdgeWeightTrace = new ArrayList<>();
        public List<List<BufferTraces>> allTraces = Arrays.asList(regroupingTrace, distanceTrace, clusterDistanceTrace, clusterEdgeWeightTrace, clusterNodeByNodeTrace);

        public DatasetTraces(String name) {
            this.name = name;
        }
    }
    /***************************************************************************
     * Storing the order of traces for each buffer size (for complexity map)
     * **********************************************************************/

    public static class BufferTraces {
        private final int buffersize;
        private List<SplayNet.CommunicatingNodes> modifiedTrace = new ArrayList<>();

        public BufferTraces(int buffersize) {
            this.buffersize = buffersize;
        }
    }
    /***************************************************************************
     * Storing information regarding the network and buffer
     * **********************************************************************/

    private static class SplaynetParameters {
        List<Integer> bufferSizes;
        List<SplayNet.CommunicatingNodes> nodeRequestPairs;
        public SplaynetParameters(List<Integer> bufferSizes, List<SplayNet.CommunicatingNodes> nodeRequestPairs){
            this.bufferSizes = bufferSizes;
            this.nodeRequestPairs = nodeRequestPairs;
        }
    }
    /***************************************************************************
     * Storing data of the trace
     * **********************************************************************/

    private static class DataParameter {
        String name;
        double a;
        double p;
        int n;
        int seq;
        double starvation = -1.0;

        public DataParameter(double a, double p, int n, int seq) {
            this.a = a;
            this.p = p;
            this.n = n;
            this.seq = seq;
        }

        public DataParameter(String name) {
            this.name = name;
        }

        public String getDatasetName(){
            return this.name;
        }
    }
    /***************************************************************************
     * Storing the results of an experiment and meta data
     * **********************************************************************/
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
    /***************************************************************************
     * Simulation
     * => This function checks if the Experiment is a simulation of multiple traces
     *      or an experiment for one csv trace (first in CSV folder) or an input from the console
     * => The simulation has no logs and reads multiple CSVs and has a certain requests limit
     * **********************************************************************/

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
            if (monitoreTraceOrder) traceCollection.add(datasetTrace);
            runExperiment(parameters, 0);
            runExperiment(parameters, 1);
            runExperiment(parameters, 2);
            runExperiment(parameters, 3);
            runExperiment(parameters, 4);
        } else {
            throw new Exception("wrong input");
        }
    }
    /***************************************************************************
     * Start Experiment
     * => This function starts the experiment for evaluated traces from CSVs
     * => This function prepares the experiment by transforming the trace and setting up the
     *      array for logging the order of the served trace
     * => The information regarding the complexity of synthetic traces can be extracted from the name
     * => For Each algorithm the experiment is initiated
     * **********************************************************************/
    public static void simulation(Scanner s) throws Exception {
        File folder = new File("./csvExperiment");
        File[] listOfFiles = folder.listFiles();
        List<Integer> bufferSizes = getBuffersizeQuestionnaire(s);
        List<Double> starvation_parameters = getStarvationParametersQuestionnaire(s);
        assert listOfFiles != null;
        ArrayList<String> traceNames = new ArrayList<>();
        for (File file: listOfFiles){
            DataParameter dataP;
            try {
                String[] k = file.getAbsolutePath().split("-");
                dataP = new DataParameter(Double.parseDouble(k[5].substring(1)),Double.parseDouble(k[6].substring(1, 4)),Integer. parseInt(k[3].substring(1)),Integer. parseInt(k[4].substring(3)));
            }
            catch(Exception e) {
                String k = file.getName();
                dataP = new DataParameter(k.substring(0,k.length()-4));
            }
            DatasetTraces datasetTrace = new DatasetTraces(dataP.getDatasetName());
            if (monitoreTraceOrder) traceCollection.add(datasetTrace);
            List<SplayNet.CommunicatingNodes> inputPairs = simulationGetCSVdata(file.getAbsolutePath());
            SplaynetParameters parameters = new SplaynetParameters(bufferSizes, inputPairs);
            if (dataP.name != null){
                System.out.printf("Dataset %s:\n", dataP.name);
            }else{
                System.out.printf("Dataset (a = %f, p = %f, n = %d, seq = %d):\n", dataP.a, dataP.p, dataP.n, dataP.seq);
            }
            //writeToTxt(runExperiment(parameters, 0), 0, dataP);
            for (Double parameter: starvation_parameters){
                if (starvation_parameters.size() > 1) dataP.starvation = parameter;
                starvationParameter = parameter;
                //writeToTxt(runExperiment(parameters,  1), 1, dataP);
            }
            //writeToTxt(runExperiment(parameters,  2), 2, dataP);
            //writeToTxt(runExperiment(parameters,  3), 3, dataP);
            writeToTxt(runExperiment(parameters,  4), 4, dataP);
            traceNames.add(dataP.name);
            traceNames.add(dataP.name + "-b");
        }
        if (monitoreTraceOrder) createTraceJSON(traceNames);
    }
    /***************************************************************************
     * Prepare Experiment
     * => This function prepares the experiment and collects information regarding buffer size etc.
     * => Further it initiliazes the array for logging the order of requests depending on the buffer sizes (for complexity map)
     * => Then the experiment is started for each algorithm and buffer size
     * **********************************************************************/
    public static ArrayList<Result> runExperiment(SplaynetParameters parameters, int algorithm) throws Exception {
        ArrayList<Result> results = new ArrayList<>();
        // monitor results for each Buffersize
        int index = 0;
         timestampCollector = new long[parameters.bufferSizes.size()][parameters.nodeRequestPairs.size()];
        for (Integer bufferSize: parameters.bufferSizes){
            if (monitoreTraceOrder){
                BufferTraces bufferTraces = new BufferTraces(bufferSize);
                if (algorithm == 0) traceCollection.get(traceCollection.size()-1).regroupingTrace.add(bufferTraces);
                if (algorithm == 1) traceCollection.get(traceCollection.size()-1).distanceTrace.add(bufferTraces);
                if (algorithm == 2) traceCollection.get(traceCollection.size()-1).clusterDistanceTrace.add(bufferTraces);
                if (algorithm == 3) traceCollection.get(traceCollection.size()-1).clusterEdgeWeightTrace.add(bufferTraces);
                if (algorithm == 4) traceCollection.get(traceCollection.size()-1).clusterNodeByNodeTrace.add(bufferTraces);
            }
            // Create and initialize Splaynet with Parameters
            SplayNet sn_current = new SplayNet();
            ArrayList<Integer> nodeList = CSVReader.extractNodes(parameters.nodeRequestPairs);
            sn_current.setNumberNodes(nodeList.size());
            sn_current.initializeTimestamps(parameters.nodeRequestPairs.size()+1);
            initializeSplaynet(sn_current, nodeList);
            sn_current.setInsertionOver();
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
            results.add(new Result(bufferSize, sn_current.getServiceCost(), sn_current.getRotationCost(), sn_current.partitions, sn_current.clusters, end-start));

        }
        return results;

    }

    public static void initializeSplaynet(SplayNet sn, ArrayList<Integer> list){
        sn.insertBalancedBST(list);
        sn.assignLastParents(sn.getRoot(), -1, Integer.MAX_VALUE);
    }
    /***************************************************************************
     * Initiate Distance Experiment (for Distance and Regrouping Algorithm)
     * => This function initiates the Experiment for a network, trace and buffer size
     * => This function extracts requests from the trace and adds them to the buffer
     * => If the buffer is full, the buffer queue is reordered and the first request served
     * **********************************************************************/

    public static void runExperimentDistanceSplaynet(SplayNet sn1, int bufferSize, int algorithm, List<SplayNet.CommunicatingNodes> nodeRequestPairs) throws Exception {
        Buffer buffer = new Buffer(sn1, bufferSize);
        sn1.setBuffer(buffer);

        for (SplayNet.CommunicatingNodes element: nodeRequestPairs){
            if (buffer.isSpace()){
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
        }
        while (!buffer.getListBufferNodePairs().isEmpty()){
            buffer.calcPriority();
            if(!buffer.getListBufferNodePairs().isEmpty()){
                if (algorithm == 0) popElementFromBuffer(buffer, sn1, true);
                if (algorithm == 1) popElementFromBuffer(buffer, sn1, false);
            }
        }
        if (monitoreTraceOrder){
            if (algorithm == 0) getBufferTraces(traceCollection.get(traceCollection.size()-1).regroupingTrace, bufferSize).modifiedTrace = buffer.getTraceOrder();
            if (algorithm == 1) getBufferTraces(traceCollection.get(traceCollection.size()-1).distanceTrace, bufferSize).modifiedTrace = buffer.getTraceOrder();
        }
    }
    /***************************************************************************
     * Initiate Cluster Experiment (For Cluster Algorithms)
     * => This function initiates the Experiment for a network, trace and buffer size
     * => This function extracts requests from the trace and adds them to the buffer
     * => If the buffer is full, the whole block is served according to a prioritization algorithm
     * **********************************************************************/
    public static void runExperimentClusterSplaynet(SplayNet sn1, int bufferSize, List<SplayNet.CommunicatingNodes> nodeRequestPairs, int algorithm) throws Exception {
        Buffer buffer = new Buffer(sn1, bufferSize);
        sn1.setBuffer(buffer);
        for (SplayNet.CommunicatingNodes element: nodeRequestPairs){
            if (buffer.isSpace()){
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
            }else{
                if (buffer.calcPriority()){
                    assert buffer.getBufferSize() == buffer.getListBufferNodePairs().size();
                    buffer.startClustering(algorithm);
                }
                sn1.printPreorder(sn1.getRoot());
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
            }
        }
        while (!buffer.getListBufferNodePairs().isEmpty()){
            if (buffer.calcPriority()){
                buffer.startClustering(algorithm);
            }
        }
        if (monitoreTraceOrder){
            if (algorithm == 2) getBufferTraces(traceCollection.get(traceCollection.size()-1).clusterDistanceTrace, bufferSize).modifiedTrace = buffer.getTraceOrder();
            if (algorithm == 3) getBufferTraces(traceCollection.get(traceCollection.size()-1).clusterEdgeWeightTrace, bufferSize).modifiedTrace = buffer.getTraceOrder();
            if (algorithm == 4) getBufferTraces(traceCollection.get(traceCollection.size()-1).clusterNodeByNodeTrace, bufferSize).modifiedTrace = buffer.getTraceOrder();
        }
    }
    /***************************************************************************
     * Pop Request from Buffer Queue
     * => The first request in the buffer queue is removed and served
     * => If necessary, the timestamp and order is tracked
     * **********************************************************************/

    public static void popElementFromBuffer(Buffer buffer, SplayNet sn1, boolean grouping) throws Exception {
        if (!grouping) buffer.sortByPriority();
        Buffer.BufferNodePair popNode = buffer.getListBufferNodePairs().get(0);
        buffer.removeListBufferNodePairs(0);
        if (monitoreTraceOrder) buffer.modifiedTraceOrderAdd(popNode.nodePair);
        sn1.increaseServingCost(popNode.getDistance());
        sn1.commute(popNode.nodePair.getU(), popNode.nodePair.getV());
        sn1.setTimestamps(popNode.getNodePair().getId(), timestamp++);
        buffer.increaseTimestamp();
    }

    public static List<SplayNet.CommunicatingNodes> getCSVdata(long maxRequests) throws IOException {
        File folder = new File("./csvExperiment");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        String path = listOfFiles[0].getAbsolutePath();
        return CSVReader.readCSV(path, maxRequests);
    }

    public static List<SplayNet.CommunicatingNodes> simulationGetCSVdata(String path) throws IOException {
        return CSVReader.readCSV(path, 100000);
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
                para = new ArrayList<>(List.of(starvationParameter));
            } else {
                throw new Exception("wront Input");
            }
        } else if (str.equalsIgnoreCase("N")){
            para = new ArrayList<>(List.of(starvationParameter));
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
    /***************************************************************************
     * Print Costs
     * => For each buffersize and algorithm, the cost of the network are printed to a txt
     * => Further, the delay of requests is printed
     * **********************************************************************/

    public static void writeToTxt(ArrayList<Result> results, int algorithm, DataParameter dataP){
        if ((algorithm == 0 || algorithm == 1) && (dataP.starvation != -1.0 || starvationSimulation)){
            try {
                String path;
                if (dataP.name != null){
                    path = "./../results/result-"+ dataP.name + "-" + algorithm  + "-st" + dataP.starvation + "-ts" + ".txt";
                }else{
                    path = "./../results/result-" + algorithm + "-" + "n" + dataP.n + "-seq" + dataP.seq + "-a" + dataP.a + "-p" + dataP.p + "-st" + dataP.starvation + "-ts" + ".txt";
                }

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
            } catch (IOException e) {
                System.out.println("An error occurred while writing a File");
                e.printStackTrace();
            }
        }
        try {
            String path;
            if (dataP.starvation != -1.0 && dataP.name == null){
                path = "./../results/result-" + algorithm + "-" + "n" + dataP.n + "-seq" + dataP.seq + "-a" + dataP.a + "-p" + dataP.p + "-st" + dataP.starvation + ".txt";
            }else if (dataP.name == null){
                path = "./../results/result-" + algorithm + "-" + "n" + dataP.n + "-seq" + dataP.seq + "-a" + dataP.a + "-p" + dataP.p + ".txt";
            }else if (dataP.starvation != -1.0){
                path = "./../results/result-" + dataP.name + "-" + algorithm + "-st" + dataP.starvation + ".txt";
            }else{
                path = "./../results/result-" + dataP.name + "-" + algorithm + ".txt";
            }

            FileWriter myWriter = new FileWriter(path);
            try {
                myWriter.write(dataP.n + "," + dataP.seq + "," + dataP.a + "," + dataP.p +"\n");
            }catch (Exception e){
                myWriter.write(dataP.name +"\n");
            }
            for(Result result: results){
                myWriter.write(result.bufferSize + "," + result.serviceCost + "," + result.rotationCost + "," + result.partitions + "," + result.clusters + "," + result.executionTime + "\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing a File");
            e.printStackTrace();
        }
    }
    /***************************************************************************
     * Traces to JSON
     * => For each served trace (with buffer size, priority algorithm etc) the order of the requests
     *      is logged and printed in the JSON
     * => With this File, the complexity map is created
     * **********************************************************************/

    public static BufferTraces getBufferTraces(List<BufferTraces> list, int buffersize) throws Exception {
        for (BufferTraces bufferTraces : list) {
            if (bufferTraces.buffersize == buffersize) return bufferTraces;
        }
        throw new Exception("Buffersize of dataset not found in trace object");
    }
    /***************************************************************************
     * Traces to JSON
     * => For each served trace (with buffer size, priority algorithm etc) the order of the requests
     *      is logged and printed in the JSON
     * => With this File, the complexity map is created
     * **********************************************************************/
    public static void createTraceJSON(ArrayList<String> names) throws IOException {
        JSONObject dataset = new JSONObject();
        int n = 0;
        for (DatasetTraces d: traceCollection){
            for (List<BufferTraces> a: d.allTraces){
                for(BufferTraces b: a){
                    String traceString = createTraceString(b.modifiedTrace);
                    JSONObject x = new JSONObject();
                    JSONObject y = new JSONObject();
                    x.put("original-trace", traceString);
                    y.put("trace variants", x);
                    dataset.put(String.format("%s", names.get(n++)), y);
                }
            }
        }
        Files.write(Paths.get("./json/", "traces.json"), Collections.singleton(dataset.toString()));
    }
    /***************************************************************************
     * Trace to String
     * => Given a trace order, this function converts it into a string
     * **********************************************************************/
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