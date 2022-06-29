import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main (String[]args) throws Exception {
    /* input type:-
    line 1--> total number of nodes in the tree (say n)
    line 2--> list of nodes ( n integer values)
    line 3--> total number of commute (or SplayNet) queries (say m)
    next corresponding m lines will each contain a pair of integers (between which communication will happen)
    Eg of input:-
    5
    11 5 9 13 1
    2
    5 11
    1 13
    Corresponding output:-
    input tree:
    1 5 11 9 13
    Final tree:
    1 13 11 5 9

     */
        System.out.println("Do you want to select custom paramters (Y/N)?");
        Scanner s = new Scanner(System.in);
        String str = s.next();
        ArrayList<Integer> nodeList = new ArrayList<>();
        int num_nodes;
        int query_num;
        List<SplayNet.CommunicatingNodes> inputPairs= new ArrayList<>();
        if (str.equalsIgnoreCase("Y")) {
            num_nodes = s.nextInt();      //total number of nodes
            SplayNet sn1 = new SplayNet();
            for (int i = 0; i < num_nodes; i++) {
                int x = s.nextInt();
                nodeList.add(x);
            }
            query_num = s.nextInt();
            for (int i = 0; i < query_num; i++) {
                SplayNet.CommunicatingNodes x = new SplayNet.CommunicatingNodes(i, s.nextInt(), s.nextInt());
                inputPairs.add(x);
            }

        } else{
            num_nodes = 5;      //total number of nodes
            nodeList.add(11);
            nodeList.add(5);
            nodeList.add(9);
            nodeList.add(13);
            nodeList.add(1);
            query_num = 2;
            SplayNet.CommunicatingNodes x = new SplayNet.CommunicatingNodes(0, 11, 5);
            SplayNet.CommunicatingNodes y = new SplayNet.CommunicatingNodes(1, 13, 1);
            inputPairs.add(x);
            inputPairs.add(y);
        }
        SplayNet sn1 = new SplayNet();
        for (Integer element: nodeList){
            sn1.insert(element);
            System.out.println("Zwischenstand: Key " + element);
            sn1.printPreorder(sn1.root);
        }
        System.out.println("input tree:");
        sn1.printPreorder(sn1.root);
        System.out.println();
        for (SplayNet.CommunicatingNodes element: inputPairs){
            sn1.commute(element.u, element.v);
            System.out.println("Zwischenstand:");
            sn1.printPreorder(sn1.root);
        }
        System.out.println("Final tree:");
        sn1.printPreorder(sn1.root);
        System.out.println();
        /*
        String path = "./src/test.csv";
        List<SplayNet.CommunicatingNodes> listNodes = CSVReader.readCSV(path);
        ArrayList<Integer> inputNodes = CSVReader.extractNodes(listNodes);
        */
    }
}
