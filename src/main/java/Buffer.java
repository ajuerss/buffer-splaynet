import org.jgrapht.alg.clustering.KSpanningTreeClustering;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import java.util.*;
import java.util.stream.Collectors;
import static java.lang.Math.sqrt;

/***************************************************************************
 * Buffer Class.
 * => This class implements the buffer and its priority algorithms.
 * **********************************************************************/
public class Buffer {
    private final SplayNet usedNet;
    private final int bufferSize;
    private final List<SplayNet.CommunicatingNodes> traceOrder = new ArrayList<>();
    private List<BufferNodePair> listBufferNodePairs = new ArrayList<>();
    private List<RequestCount> edgeListCounted = new ArrayList<>();
    ArrayList<ArrayList<BufferNodePair>> edgeList = new ArrayList<>();
    public List<BufferNodePair> getListBufferNodePairs(){return this.listBufferNodePairs;}
    public void addListBufferNodePairs(BufferNodePair element){this.listBufferNodePairs.add(element);}
    public int getBufferSize(){ return this.bufferSize;}
    public void removeListBufferNodePairs(int x){ this.listBufferNodePairs.remove(x);}
    public void modifiedTraceOrderAdd(SplayNet.CommunicatingNodes pair){this.traceOrder.add(pair);}

    public List<SplayNet.CommunicatingNodes> getTraceOrder(){return this.traceOrder;}

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
    /***************************************************************************
     * Buffer Item.
     * => This class contains the information regarding one Request in the buffer
     * => Information about the node pair, its distance and timestamp is given
     * **********************************************************************/
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
    }
    public void increaseTimestamp(){
        if (listBufferNodePairs == null) return;
        for (BufferNodePair element: listBufferNodePairs){
            element.timestamp++;
        }
    }
    /***************************************************************************
     * Calculate Priority of Buffer Queue.
     * => This function calculates the priority of each request in the buffer
     * => If a request is optimal, it is immediately served.
     * => The calculation of priority: Priority = distance - starvationParameter * Timestamp
     * **********************************************************************/
    public boolean calcPriority() throws Exception {
        boolean fullBuffer = true;
        ArrayList<BufferNodePair> servedElements = new ArrayList<>();
        for (BufferNodePair element: listBufferNodePairs){
            if (element.distance == 0){
                element.distance = usedNet.calculateDistance(element.nodePair.getU(), element.nodePair.getV());
            }
            element.priority = element.distance - MainBufferSplayNet.starvationParameter*element.timestamp;
            if (element.distance == 1){
                fullBuffer = false;
                servedElements.add(element);
                if (MainBufferSplayNet.monitoreTraceOrder) this.modifiedTraceOrderAdd(new SplayNet.CommunicatingNodes(0,element.getU(), element.getV()));
                this.usedNet.setTimestamps(element.getNodePair().getId(), MainBufferSplayNet.timestamp++);
            }
        }
        if (servedElements.size() > 0){
            this.usedNet.increaseServingCost(servedElements.size());
            for(BufferNodePair element: servedElements){
                this.listBufferNodePairs.remove(element);
            }
        }
        return fullBuffer;
    }
    /***************************************************************************
     * Helper Function
     * => Given the 4 subtrees of changed distances in a rotation, this function assignes each
     *      node u and v the subtree is lies in.
     * **********************************************************************/
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
    /***************************************************************************
     * Update Distances in Buffer Function.
     * => After a rotation is performed, the distance of node pairs need to be updates
     *      which lie in different of the 4 subtrees
     * => Since different Buffer queues exist depending on the prioritization algorithm, each
     *      priority queue is updated.
     * **********************************************************************/
    public void updateDistances(int[] a, int[] b,int[] c) throws Exception {
        for (BufferNodePair element: this.listBufferNodePairs){
            int[] values = assignGroup(element.nodePair.getU(), element.nodePair.getV(), a, b, c);
            int uGroup = values[0];
            int vGroup = values[1];

            if (element.distance == 0) throw new Exception("listBufferNodePairs: distance not calculated but updated");
            if ((uGroup == 1 && vGroup == 3) || (uGroup == 3 && vGroup == 1) ||
                    (uGroup == 2 && vGroup == 4) || (uGroup == 4 && vGroup == 2)){
                element.distance++;
            }
            if ((uGroup == 1 && vGroup == 4) || (uGroup == 4 && vGroup == 1) ||
                    (uGroup == 2 && vGroup == 3) || (uGroup == 3 && vGroup == 2)){
                element.distance--;
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
            }
            if ((uGroup == 1 && vGroup == 4) || (uGroup == 4 && vGroup == 1) ||
                    (uGroup == 2 && vGroup == 3) || (uGroup == 3 && vGroup == 2)){
                element.distance--;
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
                }
                if ((uGroup == 1 && vGroup == 4) || (uGroup == 4 && vGroup == 1) ||
                        (uGroup == 2 && vGroup == 3) || (uGroup == 3 && vGroup == 2)){
                    element.distance--;
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
    /***************************************************************************
     * Cluster Function
     * => Input is the algorithm according to which is the buffer reordered
     * => First the Buffer queue is clustered
     * => If a cluster is too large, it is partitioned
     * => Then the requests in a cluster are grouped and then the groups are reordered
     * => Then each request group is served
     * **********************************************************************/
    public void startClustering(int algorithm) throws Exception {
        ArrayList<ArrayList<Integer>> components = getClusters(this.listBufferNodePairs);
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
            double a = 3;
            double maxComponentSize = a*sqrt(n);
            if(element.size() > maxComponentSize){
                this.usedNet.clusters++;
                this.usedNet.partitions++;
                this.edgeList.addAll(Part_Graph.call_part_graph(newComponent, maxComponentSize));
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
                    prioritizeInClustersDistance();
                }else if (algorithm == 3){
                    prioritizeInClustersEdgeWeight();
                }else if (algorithm == 4){
                    prioritizeInClustersNodeByNode();
                }else{
                    throw new Exception("wrong priority");
                }
            }
            this.edgeListCounted.clear();
        }
        this.edgeList.clear();

    }
    /***************************************************************************
     * Prioritization by Distance
     * => Reorders the buffer queue based the distance of requests. Smallest distance first.
     * **********************************************************************/
    public void prioritizeInClustersDistance() throws Exception {
        this.edgeListCounted = this.edgeListCounted.stream()
                .sorted(Comparator.comparing(RequestCount::getDistance))
                .collect(Collectors.toList());
        for (RequestCount request: this.edgeListCounted){
            this.usedNet.increaseServingCost(request.distance);
            if (request.count > 1) this.usedNet.increaseServingCost(request.count-1);
            if (MainBufferSplayNet.monitoreTraceOrder){
                for(int a = 0; a < request.count; a++){
                    this.modifiedTraceOrderAdd(new SplayNet.CommunicatingNodes(0,request.u, request.v));
                }
            }
            this.usedNet.commute(request.u, request.v);
        }
    }
    /***************************************************************************
     * Prioritization Sequential
     * => Reorders the buffer queue based on routing sequential in the tree.
     * **********************************************************************/
    public void prioritizeInClustersNodeByNode() throws Exception {
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
                    this.usedNet.increaseServingCost(request.distance);
                    if (request.count > 1) this.usedNet.increaseServingCost(request.count-1);
                    if (MainBufferSplayNet.monitoreTraceOrder){
                        for(int a = 0; a < request.count; a++){
                            this.modifiedTraceOrderAdd(new SplayNet.CommunicatingNodes(0,request.u, request.v));
                        }
                    }
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
    /***************************************************************************
     * Prioritization of Frequency
     * => Reorders the buffer depending on the number of occurences. Highest frequency first.
     * **********************************************************************/
    public void prioritizeInClustersEdgeWeight() throws Exception {
        this.edgeListCounted = this.edgeListCounted.stream()
                .sorted(Comparator.comparing(RequestCount::getCount))
                .collect(Collectors.toList());
        Collections.reverse(this.edgeListCounted);
        for (RequestCount request: this.edgeListCounted){
            this.usedNet.increaseServingCost(request.distance);
            if (request.count > 1) this.usedNet.increaseServingCost(request.count-1);
            if (MainBufferSplayNet.monitoreTraceOrder){
                for(int a = 0; a < request.count; a++){
                    this.modifiedTraceOrderAdd(new SplayNet.CommunicatingNodes(0,request.u, request.v));
                }
            }
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
    /***************************************************************************
     * Helper Function
     * => Given a list of requests (demand graph) this function identifies connected components
     *      and returns them together.
     * **********************************************************************/
    public static ArrayList<ArrayList<Integer>> getClusters(List<BufferNodePair> edges){
        SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        for (BufferNodePair element: edges){
            String node1 = String.valueOf(element.nodePair.getU());
            String node2 = String.valueOf(element.nodePair.getV());
            graph.addVertex(node1);
            graph.addVertex(node2);
            graph.addEdge(node1, node2);
        }
        KSpanningTreeClustering<String, DefaultEdge> cluster = new KSpanningTreeClustering<>(graph, 1);
        List<Set<String>> x = cluster.getClustering().getClusters();
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
