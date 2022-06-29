import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainCSV {
    public static void main (String[]args) throws Exception {
        System.out.println("Do you want to select custom paramters (Y/N)?");
        Scanner s = new Scanner(System.in);
        String str = s.next();
        ArrayList<Integer> nodeList = new ArrayList<>();
        int num_nodes;
        int query_num;
        int bufferSize;
        List<SplayNet.CommunicatingNodes> inputPairs= new ArrayList<>();
        SplayNet sn1 = new SplayNet();
        if (str.equalsIgnoreCase("Y")) {
            num_nodes = s.nextInt();      //total number of nodes
            for (int i = 0; i < num_nodes; i++) {
                int x = s.nextInt();
                nodeList.add(x);
            }
            bufferSize= s.nextInt();
            query_num = s.nextInt();
            for (int i = 0; i < query_num; i++) {
                SplayNet.CommunicatingNodes x = new SplayNet.CommunicatingNodes(i, s.nextInt(), s.nextInt());
                inputPairs.add(x);
            }

        } else{
            String path = "./src/test.csv";
            List<SplayNet.CommunicatingNodes> listNodes = CSVReader.readCSV(path);
            nodeList = CSVReader.extractNodes(listNodes);
            bufferSize = 3;
            inputPairs = listNodes;
        }
        for (Integer element: nodeList){
            sn1.insert(element);
            System.out.println("Zwischenstand: Key " + element);
            sn1.printPreorder(sn1.root);
        }
        System.out.println("input tree:");
        sn1.printPreorder(sn1.root);
        System.out.println();
        BTreePrinter.printNode(transform(sn1));

        Buffer buffer = new Buffer (sn1, bufferSize);
        for (SplayNet.CommunicatingNodes element: inputPairs){
            System.out.println("input:" + element.u + " " + element.v);
            if (!buffer.isFull()){
                buffer.increaseTimestamp();
                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.listBufferNodePairs.add(x);
            }else{
                buffer.calcPriority();
                buffer.sort();
                for (Buffer.BufferNodePair k: buffer.listBufferNodePairs){
                    System.out.println("ID: " + k.nodePair.id + " Priorität: " + k.priority + " TS: " + k.timestamp);
                }
                Buffer.BufferNodePair popNode = buffer.listBufferNodePairs.get(0);
                buffer.listBufferNodePairs.remove(0);
                sn1.commute(popNode.nodePair.u, popNode.nodePair.v);
                buffer.increaseTimestamp();

                Buffer.BufferNodePair x = new Buffer.BufferNodePair(element);
                buffer.listBufferNodePairs.add(x);
                System.out.println("Zwischenstand:");
                sn1.printPreorder(sn1.root);
            }
        }
        while (!buffer.listBufferNodePairs.isEmpty()){
            buffer.calcPriority();
            buffer.sort();
            for (Buffer.BufferNodePair k: buffer.listBufferNodePairs){
                System.out.println("ID: " + k.nodePair.id + " Priorität: " + k.priority + " TS: " + k.timestamp);
            }
            Buffer.BufferNodePair x = buffer.listBufferNodePairs.get(0);
            buffer.listBufferNodePairs.remove(0);
            sn1.commute(x.nodePair.u, x.nodePair.v);
            buffer.increaseTimestamp();

            System.out.println("Zwischenstand:");
            sn1.printPreorder(sn1.root);
        }
        System.out.println("Final tree:");
        sn1.printPreorder(sn1.root);
        System.out.println();
    }

    public static Node transform(SplayNet net){
        Node<Integer> x = new Node<>(net.root.key);
        x.left = iterate(net.root.left);
        x.right = iterate(net.root.right);
        return x;
    }

    public static Node iterate(SplayNet.Node node){
        if (node == null){
            return null;
        }else{
            Node<Integer> x = new Node<>(node.key);
            x.left = iterate(node.left);
            x.right = iterate(node.right);
            return x;
        }
    }
}