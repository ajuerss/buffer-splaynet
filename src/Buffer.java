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

        private SplayNet.Node lca;

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
            boolean noBuffer = false;
            if (listBufferNodePairs.size() == 1) noBuffer = true;
            if (this.lastLCA != null && element.distance != 0){
                int lastLCA = this.lastLCA.getKey();
                if (lowerbound == -1) lowerbound = findRangeLastLCASubtree()[0];
                if (upperbound == -1) upperbound = findRangeLastLCASubtree()[1];
                //System.out.printf("Bounds calculated, lower: %d upper: %d",lowerbound, upperbound);
                int u = element.nodePair.getU();
                int v = element.nodePair.getV();
                if(!((u < upperbound && u > lowerbound)||(v < upperbound && v > lowerbound)||
                        (u == lastLCA || v == lastLCA))){
                    element.distance = usedNet.calculateDistance(element, element.nodePair.getU(), element.nodePair.getV(), noBuffer);
                }
            } else{
                element.distance = usedNet.calculateDistance(element, element.nodePair.getU(), element.nodePair.getV(), noBuffer);
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
        int upperBound = 100000;
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
        //System.out.printf("RoutingCost increased by %d through finding the range of lastLCA subtree", increaseCost);
        this.usedNet.increaseRoutingCost(increaseCost);
        return new int[]{lowerBound, upperBound};
    }
}
