import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainBufferSplayNet {
    public static void main (String[]args) throws Exception {
        Scanner s = new Scanner(System.in);
        System.out.println("Activate Log (Y/N)?");
        String n = s.next();
        boolean log;
        if (n.equalsIgnoreCase("Y")) {
            log = true;
        } else if (n.equalsIgnoreCase("N")){
            log = false;
        } else {
            throw new Exception("wrong input");
        }
        System.out.println("Do you want to use CSV data as paramters (Y/N)?");
        String str = s.next();
        int num_nodes;
        int query_num;
        int bufferSize;
        List<SplayNet.CommunicatingNodes> inputPairs= new ArrayList<>();
        ArrayList<Integer> nodeList = new ArrayList<>();
        SplayNet sn1 = new SplayNet();
        if (str.equalsIgnoreCase("N")) {
            System.out.println("Enter the number of nodes to be inserted: ");
            num_nodes = s.nextInt();
            System.out.println("Enter the keys of nodes: ");
            for (int i = 0; i < num_nodes; i++) {
                int x = s.nextInt();
                nodeList.add(x);
            }
            System.out.println("Enter buffer size:");
            bufferSize= s.nextInt();
            System.out.println("Enter number of requests:");
            query_num = s.nextInt();
            System.out.println("Enter pair of nodes as request:");
            for (int i = 0; i < query_num; i++) {
                SplayNet.CommunicatingNodes x = new SplayNet.CommunicatingNodes(i, s.nextInt(), s.nextInt());
                inputPairs.add(x);
            }

        } else if (str.equalsIgnoreCase("Y")){
            System.out.println("Enter buffer size:");
            bufferSize= s.nextInt();
            String path = "./csv/online_data.csv";
            List<SplayNet.CommunicatingNodes> listNodes = CSVReader.readCSV(path);
            nodeList = CSVReader.extractNodes(listNodes);
            inputPairs = listNodes;
        } else {
            throw new Exception("wront Input");
        }
        sn1.insertBalancedBST(nodeList);
        if (log) System.out.println("input tree:");
        if (log) sn1.printPreorder(sn1.getRoot());
        if (log) System.out.println();
        if (log) BTreePrinter.printNode(transform(sn1));

        sn1.setInsertionOver();
        if (log) System.out.println("SearchCost: " + sn1.getSearchCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
        Buffer buffer = new Buffer (sn1, bufferSize);
        for (SplayNet.CommunicatingNodes element: inputPairs){
            if (buffer.isSpace()){
                buffer.increaseTimestamp();
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
            }else{
                buffer.calcPriority();
                buffer.sort();
                for (Buffer.BufferNodePair k: buffer.getListBufferNodePairs()){
                    if (log) System.out.println("ID: " + k.nodePair.getId()+ " U: " + k.nodePair.getU() + " V: " + k.nodePair.getV() + " Priorität: " + k.getPriority() + " DIST: "  + (k.getPriority() + k.getTimestamp()) + " TS: " + k.getTimestamp());
                }
                if (log) System.out.println("Prioritätskosten:" + sn1.getRoutingCost());
                Buffer.BufferNodePair popNode = buffer.getListBufferNodePairs().get(0);
                buffer.removeListBufferNodePairs(0);
                sn1.increaseSearchCost(popNode.getPriority() + popNode.getTimestamp());
                sn1.commute(popNode.nodePair.getU(), popNode.nodePair.getV());
                buffer.increaseTimestamp();
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.addListBufferNodePairs(x);
                if (log) System.out.println("Zwischenstand:");
                if (log) sn1.printPreorder(sn1.getRoot());
                if (log) System.out.println("SearchCost: " + sn1.getSearchCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
            }
            if (log) System.out.println("input:" + element.getU() + " " + element.getV());
        }
        while (!buffer.getListBufferNodePairs().isEmpty()){
            buffer.calcPriority();
            buffer.sort();
            for (Buffer.BufferNodePair k: buffer.getListBufferNodePairs()){
                if (log) System.out.println("ID: " + k.nodePair.getId() + " Priorität: " + k.getPriority() + " TS: " + k.getTimestamp());
            }
            Buffer.BufferNodePair x = buffer.getListBufferNodePairs().get(0);
            buffer.removeListBufferNodePairs(0);
            sn1.commute(x.nodePair.getU(), x.nodePair.getV());
            buffer.increaseTimestamp();
            if (log) System.out.println("SearchCost: " + sn1.getSearchCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
            if (log) System.out.println("Zwischenstand:");
            if (log) sn1.printPreorder(sn1.getRoot());
        }
        System.out.println("SearchCost: " + sn1.getSearchCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
        if (log) System.out.println("Final tree:");
        if (log) sn1.printPreorder(sn1.getRoot());
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