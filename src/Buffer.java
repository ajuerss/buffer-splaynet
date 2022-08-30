import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Buffer {
    private final SplayNet usedNet;
    private final int bufferSize;
    private List<BufferNodePair> listBufferNodePairs = new ArrayList<>();
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

    public void sort(){
        this.listBufferNodePairs = this.listBufferNodePairs.stream()
                .sorted(Comparator.comparing(BufferNodePair::getPriority))
                .collect(Collectors.toList());
    }
}
