import org.jgrapht.alg.clustering.KSpanningTreeClustering;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.stream.Collectors;

public class Buffer {
    private final SplayNet usedNet;
    private final int bufferSize;
    private List<BufferNodePair> listBufferNodePairs = new ArrayList<>();
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

        private final int distance;

        public RequestCount(int u, int v, int distance){
            this.u = u;
            this.v = v;
            this.distance = distance;
        }

        public int getCount(){ return this.count;}

    }

    public static class BufferNodePair{
        SplayNet.CommunicatingNodes nodePair;
        private int distance;
        private int priority;
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
        public int getPriority() {
            return this.priority;
        }

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

    public void calcPriority(){
        for (BufferNodePair element: listBufferNodePairs){
            if (element.distance == 0){
                element.distance = usedNet.calculateDistance((listBufferNodePairs.size() == 1), element.nodePair.getU(), element.nodePair.getV());
            }
            element.priority = element.distance - element.timestamp;
        }
    }

    public void updateDistances(int[] a, int[] b,int[] c){
        for (BufferNodePair element: listBufferNodePairs){
            int u = element.nodePair.getU();
            int v = element.nodePair.getV();
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
        for (BufferNodePair element: listBufferNodePairs){
            if (element.distance == 0){
                element.distance = usedNet.calculateDistance((listBufferNodePairs.size() == 1), element.nodePair.getU(), element.nodePair.getV());
                if (element.distance == 1){
                    fullBuffer = false;
                    if (printLogs) System.out.printf("%d and %d with dist 1 served", element.nodePair.getU(), element.nodePair.getV());
                    this.usedNet.increaseServingCost(1);
                    this.listBufferNodePairs.remove(element);
                }
            }
        }
        return fullBuffer;
    }

    public void startClustering(boolean printLogs) throws Exception {
        ArrayList<ArrayList<Integer>> components = getClusters(this.listBufferNodePairs);
        ArrayList<ArrayList<BufferNodePair>> edgeList = new ArrayList<>();
        for(ArrayList<Integer> element: components){
            if(element.size() > this.bufferSize){
                if (printLogs) System.out.println("too big");
            }
            ArrayList<BufferNodePair> component = new ArrayList<>();
            for(Integer node: element){
                for(BufferNodePair request: this.listBufferNodePairs){
                    if(request.nodePair.getU() == node || request.nodePair.getV() == node){
                        this.listBufferNodePairs.remove(request);
                        component.add(request);
                    }
                }
            }
            edgeList.add(component);
        }
        assert this.listBufferNodePairs.size() == 0;

        edgeList = sortByClusterLength(edgeList);
        Collections.reverse(edgeList);

        for (ArrayList<BufferNodePair> element: edgeList){
            ArrayList<RequestCount> edgeListCounted = new ArrayList<>();
            for (BufferNodePair request: element){
                boolean found = false;
                for (RequestCount k: edgeListCounted){
                    if ((k.u == request.getU() && k.v == request.getV()) || (k.v == request.getU() && k.u == request.getV())){
                        found = true;
                        k.count++;
                        element.remove(request);
                        break;
                    }
                }
                if (!found){
                    RequestCount newRequest = new RequestCount(request.getU(), request.getV(), request.distance);
                    edgeListCounted.add(newRequest);
                    element.remove(request);
                }
            }
            assert element.size() == 0;
            edgeListCounted = (ArrayList<RequestCount>) edgeListCounted.stream()
                    .sorted(Comparator.comparing(RequestCount::getCount))
                    .collect(Collectors.toList());
            for (RequestCount request: edgeListCounted){
                this.usedNet.increaseServingCost(request.distance);
                this.usedNet.increaseServingCost(request.count-1);
                this.usedNet.commute(request.u, request.v);
            }
        }
    }

    // small to high
    public static ArrayList<ArrayList<BufferNodePair>> sortByClusterLength(ArrayList<ArrayList<BufferNodePair>> list){
        list = (ArrayList<ArrayList<BufferNodePair>>) list.stream()
                .sorted(Comparator.comparing(ArrayList<BufferNodePair>::size))
                .collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }

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
