import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainSplayNet {
    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(System.in);
        System.out.println("Activate Log (Y/N)?");
        String n = s.next();
        boolean log;
        if (n.equalsIgnoreCase("Y")) {
            log = true;
        } else if (n.equalsIgnoreCase("N")) {
            log = false;
        } else {
            throw new Exception("wrong input");
        }
        System.out.println("Do you want to use CSV data as paramters (Y/N)?");
        String str = s.next();
        int num_nodes;
        int query_num;
        List<SplayNet.CommunicatingNodes> inputPairs = new ArrayList<>();
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
            System.out.println("Enter number of requests:");
            query_num = s.nextInt();
            System.out.println("Enter pair of nodes as request:");
            for (int i = 0; i < query_num; i++) {
                SplayNet.CommunicatingNodes x = new SplayNet.CommunicatingNodes(i, s.nextInt(), s.nextInt());
                inputPairs.add(x);
            }
        } else if (str.equalsIgnoreCase("Y")) {
            String path = "./src/test.csv";
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
        sn1.setInsertionOver();
        if (log)
            System.out.println("SearchCost: " + sn1.getSearchCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
        for (SplayNet.CommunicatingNodes element : inputPairs) {
            sn1.commute(element.getU(), element.getV());
            if (log) System.out.println("Zwischenstand:");
            if (log) sn1.printPreorder(sn1.getRoot());
            if (log)
                System.out.println("SearchCost: " + sn1.getSearchCost() + " RoutingCost:" + sn1.getRoutingCost() + " RotationCost:" + sn1.getRotationCost());
        }
        System.out.println("SearchCost: "+sn1.getSearchCost()+" RoutingCost:"+sn1.getRoutingCost()+" RotationCost:"+sn1.getRotationCost());
        if(log)System.out.println("Final tree:");
        if(log)sn1.printPreorder(sn1.getRoot());
    }
}
