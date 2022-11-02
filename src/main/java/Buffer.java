import org.jgrapht.alg.clustering.KSpanningTreeClustering;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.stream.Collectors;

public class Buffer {
    private final SplayNet usedNet;
    private final int bufferSize;

    private List<BufferNodePair> listBufferNodePairs = new ArrayList<>();
    private List<RequestCount> edgeListCounted = new ArrayList<>();
    ArrayList<ArrayList<BufferNodePair>> edgeList = new ArrayList<>();
    public List<BufferNodePair> getListBufferNodePairs(){return this.listBufferNodePairs;}
    public void addListBufferNodePairs(BufferNodePair element){this.listBufferNodePairs.add(element);}
    public int getBufferSize(){ return this.bufferSize;}
    public void removeListBufferNodePairs(int x){ this.listBufferNodePairs.remove(x);}

    public Buffer (SplayNet inputNet, int bufferSize){
        this.usedNet = inputNet;
        this.bufferSize = bufferSize;
    }

    public boolean isSpace(){
        if (listBufferNodePairs == null) return true;
        return listBufferNodePairs.size() < bufferSize;
    }

    public static class RequestCount{
        private final int u;
        private final int v;
        private int count = 1;

        private int distance;

        public RequestCount(int u, int v, int distance){
            this.u = u;
            this.v = v;
            this.distance = distance;
        }

        public int getCount(){ return this.count;}
        public int getDistance(){ return this.distance;}

    }

    public static class BufferNodePair{
        SplayNet.CommunicatingNodes nodePair;
        private int distance;
        private double priority;
        private int timestamp;

        public BufferNodePair(SplayNet.CommunicatingNodes commNodes){
            this.nodePair = commNodes;
            this.timestamp = 0;
        }

        public int getU(){
            return this.nodePair.getU();
        }
        public int getV(){
            return this.nodePair.getV();
        }
        public double getPriority() {
            return this.priority;
        }

        public SplayNet.CommunicatingNodes getNodePair(){ return this.nodePair;}

        public int getDistance() {
            return this.distance;
        }
        public void setPriority(int k) { this.priority = k; }
        public int getTimestamp() { return this.timestamp; }
        public void setTimestamp(int k) { this.timestamp = k; }

    }
    public void increaseTimestamp(){
        if (listBufferNodePairs == null) return;
        for (BufferNodePair element: listBufferNodePairs){
            element.timestamp++;
        }
    }

    public boolean calcPriority(boolean printLogs) throws Exception {
        boolean fullBuffer = true;
        ArrayList<BufferNodePair> elementsToRemove = new ArrayList<>();
        for (BufferNodePair element: listBufferNodePairs){
            if (element.distance == 0){
                element.distance = usedNet.calculateDistance((listBufferNodePairs.size() == 1), element.nodePair.getU(), element.nodePair.getV());
            }
            element.priority = element.distance - MainBufferSplayNet.starvationParameter*element.timestamp;
            if (element.distance == 1){
                fullBuffer = false;
                if (printLogs) System.out.printf("%d and %d with dist 1 served\n", element.nodePair.getU(), element.nodePair.getV());
                elementsToRemove.add(element);
                this.usedNet.setTimestamps(element.getNodePair().getId(), MainBufferSplayNet.timestamp++);
            }
        }
        if (elementsToRemove.size() > 0){
            this.usedNet.increaseServingCost(elementsToRemove.size());
            for(BufferNodePair element: elementsToRemove){
                this.listBufferNodePairs.remove(element);
            }
        }
        return fullBuffer;
    }

    public int[] assignGroup(int u, int v, int[] a, int[] b,int[] c){
        int uGroup;
        int vGroup;

        if (inBetween(u,a[0],a[1])){
            uGroup = 1;
        } else if (inBetween(u,b[0],b[1])){
            uGroup = 2;
        } else if (inBetween(u,c[0],c[1])){
            uGroup = 3;
        }else {
            uGroup = 4;
        }
        if (inBetween(v,a[0],a[1])){
            vGroup = 1;
        } else if (inBetween(v,b[0],b[1])){
            vGroup = 2;
        } else if (inBetween(v,c[0],c[1])){
            vGroup = 3;
        }else {
            vGroup = 4;
        }

        return new int[]{uGroup, vGroup};
    }

    public void updateDistances(int[] a, int[] b,int[] c) throws Exception {
        for (BufferNodePair element: this.listBufferNodePairs){
            int[] values = assignGroup(element.nodePair.getU(), element.nodePair.getV(), a, b, c);
            int uGroup = values[0];
            int vGroup = values[1];

            if (element.distance == 0) throw new Exception("listBufferNodePairs: distance not calculated but updated");

            if ((uGroup == 1 && vGroup == 3) || (uGroup == 3 && vGroup == 1) ||
                    (uGroup == 2 && vGroup == 4) || (uGroup == 4 && vGroup == 2)){
                element.distance++;
                //System.out.println("Dist von " + u + " und " + v + " um 1 erhöhrt mit " + uGroup + " und " + vGroup);
            }
            if ((uGroup == 1 && vGroup == 4) || (uGroup == 4 && vGroup == 1) ||
                    (uGroup == 2 && vGroup == 3) || (uGroup == 3 && vGroup == 2)){
                element.distance--;
                //System.out.println("Dist von " + u + " und " + v + " um 1 verringert mit " + uGroup + " und " + vGroup);


            }
        }
        for (RequestCount element: this.edgeListCounted){
            int[] values = assignGroup(element.u, element.v, a, b, c);
            int uGroup = values[0];
            int vGroup = values[1];

            if (element.distance == 0) throw new Exception("edgelistcounted: distance not calculated but updated");

            if ((uGroup == 1 && vGroup == 3) || (uGroup == 3 && vGroup == 1) ||
                    (uGroup == 2 && vGroup == 4) || (uGroup == 4 && vGroup == 2)){
                element.distance++;
                //System.out.println("Dist von " + u + " und " + v + " um 1 erhöhrt mit " + uGroup + " und " + vGroup);
            }
            if ((uGroup == 1 && vGroup == 4) || (uGroup == 4 && vGroup == 1) ||
                    (uGroup == 2 && vGroup == 3) || (uGroup == 3 && vGroup == 2)){
                element.distance--;
                //System.out.println("Dist von " + u + " und " + v + " um 1 verringert mit " + uGroup + " und " + vGroup);
            }
        }
        for (ArrayList<BufferNodePair> cluster: this.edgeList){
            for (BufferNodePair element: cluster){
                int[] values = assignGroup(element.nodePair.getU(), element.nodePair.getV(), a, b, c);
                int uGroup = values[0];
                int vGroup = values[1];

                if (element.distance == 0) throw new Exception("edgelist: distance not calculated but updated");

                if ((uGroup == 1 && vGroup == 3) || (uGroup == 3 && vGroup == 1) ||
                        (uGroup == 2 && vGroup == 4) || (uGroup == 4 && vGroup == 2)){
                    element.distance++;
                    //System.out.println("Dist von " + u + " und " + v + " um 1 erhöhrt mit " + uGroup + " und " + vGroup);
                }
                if ((uGroup == 1 && vGroup == 4) || (uGroup == 4 && vGroup == 1) ||
                        (uGroup == 2 && vGroup == 3) || (uGroup == 3 && vGroup == 2)){
                    element.distance--;
                    //System.out.println("Dist von " + u + " und " + v + " um 1 verringert mit " + uGroup + " und " + vGroup);
                }
            }
        }

    }

    private boolean inBetween(int number, int lowerBound, int upperBound){
        return number >= lowerBound && number <= upperBound;
    }

    public void sortByPriority(){
        this.listBufferNodePairs = this.listBufferNodePairs.stream()
                .sorted(Comparator.comparing(BufferNodePair::getPriority))
                .collect(Collectors.toList());
    }

    public boolean calcDistance(boolean printLogs) throws Exception {
        boolean fullBuffer = true;
        ArrayList<BufferNodePair> servedElements = new ArrayList<>();
        for (BufferNodePair element: listBufferNodePairs){
            if (element.distance == 0){
                element.distance = usedNet.calculateDistance((listBufferNodePairs.size() == 1), element.nodePair.getU(), element.nodePair.getV());
            }
            if (element.distance == 1){
                fullBuffer = false;
                if (printLogs) System.out.printf("%d and %d with dist 1 served\n", element.nodePair.getU(), element.nodePair.getV());
                this.usedNet.increaseServingCost(1);
                this.usedNet.setTimestamps(element.getNodePair().getId(), MainBufferSplayNet.timestamp++);
                servedElements.add(element);
            }
        }
        this.listBufferNodePairs.removeAll(servedElements);
        return fullBuffer;
    }

    public void startClustering(boolean printLogs, int algorithm) throws Exception {
        ArrayList<ArrayList<Integer>> components = getClusters(this.listBufferNodePairs, printLogs);
        for(ArrayList<Integer> element: components){
            ArrayList<BufferNodePair> newComponent = new ArrayList<>();
            for(Integer node: element){
                ArrayList<BufferNodePair> found = new ArrayList<>();
                for(BufferNodePair request: this.listBufferNodePairs){
                    if(request.nodePair.getU() == node || request.nodePair.getV() == node){
                        found.add(request);
                        newComponent.add(request);
                    }
                }
                this.listBufferNodePairs.removeAll(found);
            }
            double n = this.usedNet.getNumberNodes();
            double a = 0.01;
            double factor = ((2*n-4)/(((n*n-n)/2)-1))/a;
            double maxComponentSize = factor*n;
            if(element.size() > maxComponentSize){
                if (printLogs) System.out.println("partitioning component");
                this.usedNet.clusters++;
                this.usedNet.partitions++;
                this.edgeList.addAll(Part_Graph.call_part_graph(newComponent, maxComponentSize, printLogs));
            }else{
                this.usedNet.clusters++;
                this.edgeList.add(newComponent);
            }
        }
        if (this.listBufferNodePairs.size() > 0) throw new Exception("this.listBufferNodePairs bigger than one");

        this.edgeList = sortByClusterLength(this.edgeList);
        for (ArrayList<BufferNodePair> element: this.edgeList){
            ArrayList<BufferNodePair> foundE = new ArrayList<>();
            for (BufferNodePair request: element){
                boolean found = false;
                for (RequestCount k: this.edgeListCounted){
                    if ((k.u == request.getU() && k.v == request.getV()) || (k.v == request.getU() && k.u == request.getV())){
                        found = true;
                        k.count++;
                        foundE.add(request);
                        break;
                    }
                }
                if (!found){
                    RequestCount newRequest = new RequestCount(request.getU(), request.getV(), request.distance);
                    edgeListCounted.add(newRequest);
                    foundE.add(request);
                }
            }
            element.removeAll(foundE);
            if (element.size() > 0) throw new Exception("element.size() bigger than one");
            if (this.edgeListCounted.size() > 0){
                if (algorithm == 2){
                    prioritizeInClustersDistance(printLogs);
                }else if (algorithm == 3){
                    prioritizeInClustersEdgeWeight(printLogs);
                }else if (algorithm == 4){
                    prioritizeInClustersNodeByNode(printLogs);
                }else{
                    throw new Exception("wrong priority");
                }
            }
            this.edgeListCounted.clear();
        }
        this.edgeList.clear();

    }

    public void prioritizeInClustersDistance(boolean printLogs) throws Exception {
        this.edgeListCounted = this.edgeListCounted.stream()
                .sorted(Comparator.comparing(RequestCount::getDistance))
                .collect(Collectors.toList());

        for (RequestCount request: this.edgeListCounted){
            if (printLogs) System.out.printf("Request %d-%d with distance %d, %d times\n", request.u, request.v, request.distance, request.count);
            this.usedNet.increaseServingCost(request.distance);
            if (request.count > 1) this.usedNet.increaseServingCost(request.count-1);
            this.usedNet.commute(request.u, request.v);
        }
    }

    public void prioritizeInClustersNodeByNode(boolean printLogs) throws Exception {
        int node = this.edgeListCounted.get(0).u;
        while(this.edgeListCounted.size() != 0){
            int nextNode = 0;
            ArrayList<RequestCount> servedNodes = new ArrayList<>();
            for (RequestCount request: this.edgeListCounted){
                if (request.u == node || request.v == node){
                    if (request.u == node){
                        nextNode = request.v;
                    }else{
                        nextNode = request.u;
                    }
                    if (printLogs) System.out.printf("Request %d-%d with distance %d, %d times\n", request.u, request.v, request.distance, request.count);
                    this.usedNet.increaseServingCost(request.distance);
                    if (request.count > 1) this.usedNet.increaseServingCost(request.count-1);
                    this.usedNet.commute(request.u, request.v);
                    servedNodes.add(request);
                }
            }
            this.edgeListCounted.removeAll(servedNodes);
            if (nextNode == 0 && this.edgeListCounted.size() > 0){
                node = edgeListCounted.get(0).u;
            }else{
                node = nextNode;
                nextNode = 0;
            }
        }
    }

    public void prioritizeInClustersEdgeWeight(boolean printLogs) throws Exception {
        this.edgeListCounted = this.edgeListCounted.stream()
                .sorted(Comparator.comparing(RequestCount::getCount))
                .collect(Collectors.toList());
        for (RequestCount request: this.edgeListCounted){
            if (printLogs) System.out.printf("Request %d-%d with distance %d, %d times\n", request.u, request.v, request.distance, request.count);
            this.usedNet.increaseServingCost(request.distance);
            if (request.count > 1) this.usedNet.increaseServingCost(request.count-1);
            this.usedNet.commute(request.u, request.v);
        }
    }

    // high to small
    public static ArrayList<ArrayList<BufferNodePair>> sortByClusterLength(ArrayList<ArrayList<BufferNodePair>> list){
        list = (ArrayList<ArrayList<BufferNodePair>>) list.stream()
                .sorted(Comparator.comparing(ArrayList<BufferNodePair>::size))
                .collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }

    public static ArrayList<ArrayList<Integer>> getClusters(List<BufferNodePair> edges, boolean printLogs){
        SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        for (BufferNodePair element: edges){
            String node1 = String.valueOf(element.nodePair.getU());
            String node2 = String.valueOf(element.nodePair.getV());
            graph.addVertex(node1);
            graph.addVertex(node2);
            graph.addEdge(node1, node2);
        }
        if (printLogs) System.out.println("start clust");
        KSpanningTreeClustering<String, DefaultEdge> cluster = new KSpanningTreeClustering<>(graph, 1);
        List<Set<String>> x = cluster.getClustering().getClusters();
        if (printLogs) System.out.println(x);
        ArrayList<ArrayList<Integer>> intClust = new ArrayList<>();
        for (Set<String> element: x){
            ArrayList<Integer> arr = new ArrayList<>();
            for(String str: element){
                arr.add(Integer.parseInt(str));
            }
            intClust.add(arr);
        }
        return intClust;
    }

}
