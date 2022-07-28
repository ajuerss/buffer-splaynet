import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


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
        Scanner s = new Scanner(System.in);
        // declare where logs of current iterations of the toplogy are needed
        boolean printLogs = printLogQuestionaire(s);
        SplayNet sn1 = new SplayNet();
        // get parameters for Experiment (Buffersize and Communication pairs). The Splaynet tree will be initialized
        SplaynetParameters parameters = useParametersQuestionaire(s,printLogs, sn1);
        List<SplayNet.CommunicatingNodes> inputPairs = parameters.nodeRequestPairs;
        // set cost counters to zero
        sn1.setInsertionOver();
        if (printLogs) System.out.println("SearchCost: " + sn1.getServiceCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());

        // run experiment with Buffer(Distance Algorithm) on Splaynet
        ArrayList<Result> results = new ArrayList<>();
        for (Integer bufferSize: parameters.bufferSizes){
            runExperimentDistanceSplaynet(sn1, bufferSize, parameters.nodeRequestPairs, printLogs);
            System.out.println("For Buffersize " + bufferSize + ":");
            System.out.println("SearchCost: " + sn1.getServiceCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
            if (printLogs) System.out.println("Final tree:");
            if (printLogs) sn1.printPreorder(sn1.getRoot());
            results.add(new Result(bufferSize, sn1.getServiceCost(), sn1.getRoutingCost(), sn1.getRotationCost()));
            sn1.resetCostCounter();
        }
        // print results in txt
        writeToTxt(results);
    }

    public static void writeToTxt(ArrayList<Result> results){
        try {
            String path = "./result/results.txt";
            File file = new File(path);
            int counter = 1;
            while (file.exists()){
                path = "./result/results" + counter + ".txt";
                file = new File(path);
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

    public static void initializeSplaynet(SplayNet sn, ArrayList<Integer> list){
        sn.insertBalancedBST(list);
    }

    public static List<SplayNet.CommunicatingNodes> getCSVdata(SplayNet sn) throws IOException {
        String path = "./csv/online_data.csv";
        List<SplayNet.CommunicatingNodes> inputPairs = CSVReader.readCSV(path);
        ArrayList<Integer> nodeList = CSVReader.extractNodes(inputPairs);
        initializeSplaynet(sn, nodeList);
        return inputPairs;
    }
    public static List<SplayNet.CommunicatingNodes> getCustomCommunicationNodes(Scanner s, SplayNet sn){
        List<SplayNet.CommunicatingNodes> inputPairs= new ArrayList<>();
        System.out.println("Enter number of requests:");
        int requestNumber = s.nextInt();
        System.out.println("Enter pair of nodes as request:");
        for (int i = 0; i < requestNumber; i++) {
            SplayNet.CommunicatingNodes x = new SplayNet.CommunicatingNodes(i, s.nextInt(), s.nextInt());
            inputPairs.add(x);
        }
        initializeSplaynet(sn, CSVReader.extractNodes(inputPairs));
        return inputPairs;
    }

    public static SplaynetParameters useParametersQuestionaire(Scanner s, boolean printLogs, SplayNet sn1) throws Exception {
        System.out.println("Do you want to use CSV data as paramters (Y/N)?");
        String str = s.next();
        List<SplayNet.CommunicatingNodes> inputPairs;
        if (str.equalsIgnoreCase("N")) {
            inputPairs = getCustomCommunicationNodes(s, sn1);
        } else if (str.equalsIgnoreCase("Y")){
            inputPairs = getCSVdata(sn1);
        } else {
            throw new Exception("wront Input");
        }
        List<Integer> bufferSizes = getBuffersizeQuestionaire(s);

        // logs for better observability
        if (printLogs) System.out.println("input tree:");
        if (printLogs) sn1.printPreorder(sn1.getRoot());
        if (printLogs) System.out.println();
        if (printLogs) BTreePrinter.printNode(transform(sn1));


        return new SplaynetParameters(bufferSizes, inputPairs);
    }

    public static void runExperimentDistanceSplaynet(SplayNet sn1, int bufferSize, List<SplayNet.CommunicatingNodes> nodeRequestPairs, boolean printLogs) throws Exception {
        Buffer buffer = new Buffer (sn1, bufferSize);
        for (SplayNet.CommunicatingNodes element: nodeRequestPairs){
            if (buffer.isSpace()){
                buffer.increaseTimestamp();
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
            }else{
                buffer.calcPriority();
                buffer.sort();
                for (Buffer.BufferNodePair k: buffer.getListBufferNodePairs()){
                    if (printLogs) System.out.println("ID: " + k.nodePair.getId()+ " U: " + k.nodePair.getU() + " V: " + k.nodePair.getV() + " Priorität: " + k.getPriority() + " DIST: "  + (k.getPriority() + k.getTimestamp()) + " TS: " + k.getTimestamp());
                }
                if (printLogs) System.out.println("Prioritätskosten:" + sn1.getRoutingCost());
                Buffer.BufferNodePair popNode = buffer.getListBufferNodePairs().get(0);
                buffer.removeListBufferNodePairs(0);
                sn1.increaseSearchCost(popNode.getPriority() + popNode.getTimestamp());
                sn1.commute(popNode.nodePair.getU(), popNode.nodePair.getV());
                buffer.increaseTimestamp();
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
                if (printLogs) System.out.println("Zwischenstand:");
                if (printLogs) sn1.printPreorder(sn1.getRoot());
                if (printLogs) System.out.println("SearchCost: " + sn1.getServiceCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
            }
            if (printLogs) System.out.println("input:" + element.getU() + " " + element.getV());
        }
        while (!buffer.getListBufferNodePairs().isEmpty()){
            buffer.calcPriority();
            buffer.sort();
            for (Buffer.BufferNodePair k: buffer.getListBufferNodePairs()){
                if (printLogs) System.out.println("ID: " + k.nodePair.getId() + " Priorität: " + k.getPriority() + " TS: " + k.getTimestamp());
            }
            Buffer.BufferNodePair x = buffer.getListBufferNodePairs().get(0);
            buffer.removeListBufferNodePairs(0);
            sn1.commute(x.nodePair.getU(), x.nodePair.getV());
            buffer.increaseTimestamp();
            if (printLogs) System.out.println("SearchCost: " + sn1.getServiceCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
            if (printLogs) System.out.println("Zwischenstand:");
            if (printLogs) sn1.printPreorder(sn1.getRoot());
        }
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