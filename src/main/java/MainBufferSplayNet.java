import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MainBufferSplayNet {

    public static long[][] timestampCollector;

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
        public DataParameter(double a, double p, int n, int seq){
            this.a = a;
            this.p = p;
            this.n = n;
            this.seq = seq;
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
            boolean printLogs = printLogQuestionaire(s);
            // get parameters for Experiment (Buffersize and Communication pairs). The Splaynet tree will be initialized
            SplaynetParameters parameters = useParametersQuestionaire(s);

            runExperiment(parameters, printLogs, 0);
            //runExperiment(parameters, printLogs, 1);
            //runExperiment(parameters, printLogs, 2);
            //runExperiment(parameters, printLogs, 3);
            //runExperiment(parameters, printLogs, 4);
        } else {
            throw new Exception("wrong input");
        }
    }

    public static void simulation(Scanner s) throws Exception {
        File folder = new File("./csvExperiment");
        File[] listOfFiles = folder.listFiles();
        List<Integer> bufferSizes = getBuffersizeQuestionaire(s);
        assert listOfFiles != null;
        for (File file: listOfFiles){
            String[] k = file.getAbsolutePath().split("-");
            DataParameter dataP = new DataParameter(Double.parseDouble(k[5].substring(1)),Double.parseDouble(k[6].substring(1, 4)),Integer. parseInt(k[3].substring(1)),Integer. parseInt(k[4].substring(3)));
            List<SplayNet.CommunicatingNodes> inputPairs = simulationGetCSVdata(file.getAbsolutePath());
            SplaynetParameters parameters = new SplaynetParameters(bufferSizes, inputPairs);
            System.out.printf("Dataset (a = %f, p = %f, n = %d, seq = %d):\n", dataP.a, dataP.p, dataP.n, dataP.seq);
            writeToTxt(runExperiment(parameters, false, 0), 0, dataP);
            //writeToTxt(runExperiment(parameters, false, 1), 1, dataP);
            //writeToTxt(runExperiment(parameters, false, 2), 2, dataP);
            //writeToTxt(runExperiment(parameters, false, 3), 3, dataP);
            //writeToTxt(runExperiment(parameters, false, 4), 4, dataP);
        }
    }

    public static ArrayList<Result> runExperiment(SplaynetParameters parameters, boolean printLogs, int algorithm) throws Exception {
        // run experiment with Buffer(Distance Algorithm) on Splaynet
        ArrayList<Result> results = new ArrayList<>();
        // monitor results for each Buffersize
        int index = 0;
         timestampCollector = new long[parameters.bufferSizes.size()][parameters.nodeRequestPairs.size()];
        for (Integer bufferSize: parameters.bufferSizes){
            // Create and initialize Splaynet with Parameters
            SplayNet sn_current = new SplayNet();
            ArrayList<Integer> nodeList = CSVReader.extractNodes(parameters.nodeRequestPairs);
            sn_current.setNumberNodes(nodeList.size());
            sn_current.initializeTimestamps(parameters.nodeRequestPairs.size()+1);
            initializeSplaynet(sn_current, nodeList);
            sn_current.setInsertionOver();
            if (printLogs) System.out.println("input tree:");
            if (printLogs) sn_current.printPreorder(sn_current.getRoot());
            if (printLogs) System.out.println();
            sn_current.setTimestamps(0, System.nanoTime());
            long start = System.nanoTime();
            if (algorithm == 0 || algorithm == 1){
                runExperimentDistanceSplaynet(sn_current, bufferSize, algorithm, parameters.nodeRequestPairs, printLogs);
                timestampCollector[index++] = Arrays.copyOfRange(sn_current.getAllTimestamps(), 1, sn_current.getAllTimestamps().length);
            }else if (algorithm > 1){
                runExperimentClusterSplaynet(sn_current, bufferSize, parameters.nodeRequestPairs, printLogs, algorithm);
            }
            long end = System.nanoTime();
            sn_current.setExecutionTime((end-start)/1000000);
            System.out.println("For Buffersize " + bufferSize + ":");
            System.out.println("ServingCost: " + sn_current.getServiceCost() + " RotationCost:" + sn_current.getRotationCost() + " partitions:" + sn_current.partitions + "/" + sn_current.clusters + " Time:" + sn_current.getExecutionTime());
            if (printLogs) System.out.println("Final tree:");
            if (printLogs) sn_current.printPreorder(sn_current.getRoot());
            if (printLogs) System.out.println();
            //if (printLogs) BTreePrinter.printNode(transform(sn_current));
            results.add(new Result(bufferSize, sn_current.getServiceCost(), sn_current.getRotationCost(), sn_current.partitions, sn_current.clusters, end-start));
        }
        return results;

    }

    public static void initializeSplaynet(SplayNet sn, ArrayList<Integer> list){
        sn.insertBalancedBST(list);
        sn.assignLastParents(sn.getRoot(), -1, Integer.MAX_VALUE);
    }

    public static void runExperimentDistanceSplaynet(SplayNet sn1, int bufferSize, int algorithm, List<SplayNet.CommunicatingNodes> nodeRequestPairs, boolean printLogs) throws Exception {
        Buffer buffer = new Buffer(sn1, bufferSize);
        sn1.setBuffer(buffer);
        for (SplayNet.CommunicatingNodes element: nodeRequestPairs){
            if (buffer.isSpace()){
                buffer.increaseTimestamp();
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
            }else{
                if(buffer.calcPriority(printLogs)){
                    if (algorithm == 0) popElementFromBuffer(buffer, printLogs, sn1, false);
                    if (algorithm == 1) popElementFromBuffer(buffer, printLogs, sn1, true);
                }
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
            }
            if (printLogs) System.out.println("Incoming Request:" + element.getU() + " " + element.getV());
        }
        while (!buffer.getListBufferNodePairs().isEmpty()){
            buffer.calcPriority(printLogs);
            if(!buffer.getListBufferNodePairs().isEmpty()){
                if (algorithm == 0) popElementFromBuffer(buffer, printLogs, sn1, false);
                if (algorithm == 1) popElementFromBuffer(buffer, printLogs, sn1, true);
            }
        }
    }

    public static void runExperimentClusterSplaynet(SplayNet sn1, int bufferSize, List<SplayNet.CommunicatingNodes> nodeRequestPairs, boolean printLogs, int algorithm) throws Exception {
        Buffer buffer = new Buffer(sn1, bufferSize);
        sn1.setBuffer(buffer);
        for (SplayNet.CommunicatingNodes element: nodeRequestPairs){
            if (buffer.isSpace()){
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
                if (printLogs) System.out.println("Incoming Request:" + element.getU() + " " + element.getV());
            }else{
                if (buffer.calcDistance(printLogs)){
                    assert buffer.getBufferSize() == buffer.getListBufferNodePairs().size();
                    if (printLogs) System.out.println("Clustering buffer Start");
                    buffer.startClustering(printLogs, algorithm);
                }
                if (printLogs) sn1.printPreorder(sn1.getRoot());
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
                if (printLogs) System.out.println("Incoming Request:" + element.getU() + " " + element.getV());
            }
        }
        while (!buffer.getListBufferNodePairs().isEmpty()){
            if (buffer.calcDistance(printLogs)){
                if (printLogs) System.out.println("Clustering buffer");
                buffer.startClustering(printLogs, algorithm);
                if (printLogs) System.out.println("Clustering buffer done");
            }
        }
    }

    public static void popElementFromBuffer(Buffer buffer, boolean printLogs, SplayNet sn1, boolean grouping) throws Exception {
        if (!grouping) buffer.sortByPriority();
        if (printLogs) System.out.println("Elements in Buffer:");
        for (Buffer.BufferNodePair k: buffer.getListBufferNodePairs()){
            if (printLogs) System.out.printf("ID:%d U:%d V:%d P:%d DST:%d TS:%d\n", k.nodePair.getId(), k.nodePair.getU(), k.nodePair.getV(), k.getPriority(), k.getDistance(), k.getTimestamp());
        }
        Buffer.BufferNodePair popNode = buffer.getListBufferNodePairs().get(0);
        buffer.removeListBufferNodePairs(0);
        if (printLogs) System.out.printf("Served Requested %d and %d\n", popNode.nodePair.getU(), popNode.nodePair.getV());
        sn1.increaseServingCost(popNode.getDistance());
        sn1.commute(popNode.nodePair.getU(), popNode.nodePair.getV());

        sn1.setTimestamps(popNode.getNodePair().getId(), System.nanoTime()-sn1.getTimestamp(0));
        buffer.increaseTimestamp();
        if (printLogs) System.out.println("Zwischenstand:");
        if (printLogs) sn1.printPreorder(sn1.getRoot());
        if (printLogs) System.out.println("ServingCost: " + sn1.getServiceCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
    }

    public static List<SplayNet.CommunicatingNodes> getCSVdata(long maxRequests) throws IOException {
        String path = "./csvExperiment/zipf-dataset-N15-seq100-a1.0-p0.0.csv";
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
        List<Integer> bufferSizes = getBuffersizeQuestionaire(s);
        return new SplaynetParameters(bufferSizes, inputPairs);
    }

    public static List<Integer> getBuffersizeQuestionaire(Scanner s) throws Exception {
        System.out.println("Do you want to select custom Buffersizes (Default: 1, 8, 32, 128, 512, 1024)?");
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

    public static List<Integer> getIntegerListInput(Scanner s, int length){
        List<Integer> nodeList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            int x = s.nextInt();
            nodeList.add(x);
        }
        return nodeList;
    }

    public static void writeToTxt(ArrayList<Result> results, int algorithm, DataParameter dataP){
        if (algorithm == 0){
            try {
                String path = "./../results/result-" + algorithm + "-" + "n" + dataP.n + "-seq" + dataP.seq + "-a" + dataP.a + "-p" + dataP.p + "-ts" + ".txt";

                FileWriter myWriter = new FileWriter(path);
                for (int idx = 0; idx < results.size(); idx++){
                    myWriter.write(String.valueOf(results.get(idx).bufferSize));
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
            String path = "./../results/result-" + algorithm + "-" + "n" + dataP.n + "-seq" + dataP.seq + "-a" + dataP.a + "-p" + dataP.p + ".txt";

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

    public static Node transform(SplayNet net){
        Node<Integer> x = new Node<>(net.getRoot().getKey());
        x.left = iterate(net.getRoot().getLeft());
        x.right = iterate(net.getRoot().getRight());
        return x;
    }

    public static Node iterate(SplayNet.Node node){
        if (node == null){
            return null;
        }else{
            Node<Integer> x = new Node<>(node.getKey());
            x.left = iterate(node.getLeft());
            x.right = iterate(node.getRight());
            return x;
        }
    }
}