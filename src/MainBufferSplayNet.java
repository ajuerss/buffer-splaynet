import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MainBufferSplayNet {

    private static class SplaynetParameters {
        List<Integer> bufferSizes;
        List<SplayNet.CommunicatingNodes> nodeRequestPairs;
        public SplaynetParameters(List<Integer> bufferSizes, List<SplayNet.CommunicatingNodes> nodeRequestPairs){
            this.bufferSizes = bufferSizes;
            this.nodeRequestPairs = nodeRequestPairs;
        }
    }

    private static class Result {
        int bufferSize;
        long serviceCost;
        long routingCost;
        long rotationCost;
        public Result(int bufferSize, long serviceCost, long routingCost, long rotationCost){
            this.bufferSize = bufferSize;
            this.serviceCost = serviceCost;
            this.routingCost = routingCost;
            this.rotationCost = rotationCost;
        }
    }

    public static void main (String[]args) throws Exception {
        //printRequests();
        Scanner s = new Scanner(System.in);
        // declare where logs of current iterations of the toplogy are needed
        boolean printLogs = printLogQuestionaire(s);
        // get parameters for Experiment (Buffersize and Communication pairs). The Splaynet tree will be initialized
        SplaynetParameters parameters = useParametersQuestionaire(s);

        // run experiment with Buffer(Distance Algorithm) on Splaynet
        ArrayList<Result> results = new ArrayList<>();
        // monitor results for each Buffersize
        for (Integer bufferSize: parameters.bufferSizes){
            // Create and initialize Splaynet with Parameters
            SplayNet sn_current = new SplayNet();
            ArrayList<Integer> nodeList = CSVReader.extractNodes(parameters.nodeRequestPairs);
            initializeSplaynet(sn_current, nodeList);
            sn_current.setInsertionOver();
            if (printLogs) System.out.println("input tree:");
            if (printLogs) sn_current.printPreorder(sn_current.getRoot());
            if (printLogs) System.out.println();
            //if (printLogs) BTreePrinter.printNode(transform(sn_current));
            runExperimentDistanceSplaynet(sn_current, bufferSize, parameters.nodeRequestPairs, printLogs);
            System.out.println("For Buffersize " + bufferSize + ":");
            System.out.println("ServingCost: " + sn_current.getServiceCost() + " RoutingCost:" + sn_current.getRoutingCost() + " RotationCost:" + sn_current.getRotationCost());
            if (printLogs) System.out.println("Final tree:");
            if (printLogs) sn_current.printPreorder(sn_current.getRoot());
            if (printLogs) System.out.println();
            //if (printLogs) BTreePrinter.printNode(transform(sn_current));
            results.add(new Result(bufferSize, sn_current.getServiceCost(), sn_current.getRoutingCost(), sn_current.getRotationCost()));
        }
        // print results in txt
        writeToTxt(results);
    }

    public static void initializeSplaynet(SplayNet sn, ArrayList<Integer> list){
        sn.insertBalancedBST(list);
        sn.assignLastParents(sn.getRoot(), 0, Integer.MAX_VALUE);
    }

    public static void runExperimentDistanceSplaynet(SplayNet sn1, int bufferSize, List<SplayNet.CommunicatingNodes> nodeRequestPairs, boolean printLogs) throws Exception {
        Buffer buffer = new Buffer (sn1, bufferSize);
        sn1.setBuffer(buffer);
        for (SplayNet.CommunicatingNodes element: nodeRequestPairs){
            if (buffer.isSpace()){
                buffer.increaseTimestamp();
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
            }else{
                popElementFromBuffer(buffer, printLogs, sn1);
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
            }
            if (printLogs) System.out.println("Incoming Request:" + element.getU() + " " + element.getV());
        }
        while (!buffer.getListBufferNodePairs().isEmpty()){
            popElementFromBuffer(buffer, printLogs, sn1);
        }
    }

    public static void popElementFromBuffer(Buffer buffer, boolean printLogs, SplayNet sn1) throws Exception {
        buffer.calcPriority();
        buffer.sort();
        if (printLogs) System.out.println("Elements in Buffer:");
        for (Buffer.BufferNodePair k: buffer.getListBufferNodePairs()){
            if (printLogs) System.out.printf("ID:%d U:%d V:%d P:%d DST:%d TS:%d\n", k.nodePair.getId(), k.nodePair.getU(), k.nodePair.getV(), k.getPriority(), k.getDistance(), k.getTimestamp());
        }
        Buffer.BufferNodePair popNode = buffer.getListBufferNodePairs().get(0);
        buffer.removeListBufferNodePairs(0);
        if (printLogs) System.out.printf("Served Requested %d and %d\n", popNode.nodePair.getU(), popNode.nodePair.getV());
        sn1.increaseServingCost(popNode.getDistance());
        sn1.commute(popNode.nodePair.getU(), popNode.nodePair.getV());
        buffer.increaseTimestamp();
        if (printLogs) System.out.println("Zwischenstand:");
        if (printLogs) sn1.printPreorder(sn1.getRoot());
        if (printLogs) System.out.println("ServingCost: " + sn1.getServiceCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
    }

    public static List<SplayNet.CommunicatingNodes> getCSVdata(long maxRequests) throws IOException {
        String path = "./csv/online_data.csv";
        return CSVReader.readCSV(path, maxRequests);
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
        System.out.println("Do you want to select custom Buffersizes (Default: 1, 2, 3, 4, 5, 10, 20, 50, 100)?");
        String str = s.next();
        List<Integer> bufferSizes;
        if (str.equalsIgnoreCase("N")) {
            bufferSizes = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 10, 20, 50, 100));
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

    public static void writeToTxt(ArrayList<Result> results){
        try {
            String path = "./../results/result.txt";
            File file = new File(path);
            int counter = 1;
            while (file.exists()){
                path = "./../results/result" + counter + ".txt";
                file = new File(path);
                counter++;
            }
            FileWriter myWriter = new FileWriter(path);
            for(Result result: results){
                myWriter.write(result.bufferSize + "," + result.serviceCost + "," + result.routingCost + "," + result.rotationCost + "\n");
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
    public static void printRequests(){
        int num = 100;
        int nodes = 31;
        int k = 0;
        while (k < num){
            Random rand = new Random();
            int ran1 = rand.nextInt(nodes) + 1;
            int ran2 = rand.nextInt(nodes) + 1;
            if (ran1 != ran2){
                k++;
                System.out.printf("%d,%d,%d\n",k,ran1,ran2);
            }
        }
    }

}