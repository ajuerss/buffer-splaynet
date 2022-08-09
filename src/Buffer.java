import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Buffer {
    private final SplayNet usedNet;
    private final int bufferSize;

    private SplayNet.Node lastLCA;

    private List<BufferNodePair> listBufferNodePairs = new ArrayList<>();

    public void setLastLCA(SplayNet.Node element){this.lastLCA = element;}

    public List<BufferNodePair> getListBufferNodePairs(){return this.listBufferNodePairs;}

    public void addListBufferNodePairs(BufferNodePair element){this.listBufferNodePairs.add(element);}

    public void removeListBufferNodePairs(int x){ this.listBufferNodePairs.remove(x);}

    public Buffer (SplayNet inputNet, int bufferSize){
        this.usedNet = inputNet;
        this.bufferSize = bufferSize;
    }

    public boolean isSpace(){
        if (listBufferNodePairs == null) return true;
        return listBufferNodePairs.size() < bufferSize;
    }

    public static class BufferNodePair{
        SplayNet.CommunicatingNodes nodePair;
        private int distance;
        private int priority;
        private int timestamp;

        private SplayNet.Node lca = null;

        public BufferNodePair(SplayNet.CommunicatingNodes commNodes){
            this.nodePair = commNodes;
            this.timestamp = 0;
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
        public SplayNet.Node getLca() {
            return this.lca;
        }
        public void setLca(SplayNet.Node k) { this.lca = k; }

    }
    public void increaseTimestamp(){
        if (listBufferNodePairs == null) return;
        for (BufferNodePair element: listBufferNodePairs){
            element.timestamp++;
        }
    }

    public void calcPriority(){
        int lowerbound = -1;
        int upperbound = -1;
        for (BufferNodePair element: listBufferNodePairs){
            boolean noBuffer = listBufferNodePairs.size() == 1;
            if (this.lastLCA != null && element.distance != 0){
                int lastLCA = this.lastLCA.getKey();
                if (lowerbound == -1 || upperbound == -1) {
                    int[] temp = findRangeLastLCASubtree();
                    lowerbound = temp[0];
                    upperbound = temp[1];
                }
                //System.out.printf("LB: %d, UB: %d\n", lowerbound, upperbound);
                int u = element.nodePair.getU();
                int v = element.nodePair.getV();
                if((u < upperbound && u > lowerbound)||(v < upperbound && v > lowerbound)||
                        (u == lastLCA || v == lastLCA)){
                    element.distance = usedNet.calculateDistance(element, element.nodePair.getU(), element.nodePair.getV(), noBuffer);
                    //System.out.printf("Distance recalculated for %d and %d\n", u, v);
                }
            } else{
                element.distance = usedNet.calculateDistance(element, element.nodePair.getU(), element.nodePair.getV(), noBuffer);
                //System.out.printf("Distance calculated for %d and %d\n", element.nodePair.getU(), element.nodePair.getV());
            }
            element.priority = element.distance - element.timestamp;
        }
    }

    public void sort(){
        this.listBufferNodePairs = this.listBufferNodePairs.stream()
                .sorted(Comparator.comparing(BufferNodePair::getPriority))
                .collect(Collectors.toList());
    }

    public int[] findRangeLastLCASubtree(){
        SplayNet.Node node = this.usedNet.getRoot();
        int key = this.lastLCA.getKey();
        int lowerBound = 0;
        int upperBound = Integer.MAX_VALUE;
        int increaseCost = 0;
        while (node != null && node.getKey() != key){
            if (key < node.getKey()) {
                upperBound = node.getKey();
                node = node.getLeft();
            } else {
                lowerBound = node.getKey();
                node = node.getRight();
            }
            increaseCost++;
        }
        //System.out.printf("RoutingCost increased by %d through finding the range of lastLCA subtree\n", increaseCost);
        this.usedNet.increaseRoutingCost(increaseCost);
        return new int[]{lowerBound, upperBound};
    }
}
