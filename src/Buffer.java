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
        private int priority;
        private int timestamp;

        public BufferNodePair(SplayNet.CommunicatingNodes commNodes){
            this.nodePair = commNodes;
            this.timestamp = 0;
        }

        public int getPriority() {
            return this.priority;
        }
        public void setPriority(int k) { this.priority = k; }
        public int getTimestamp() {
            return this.timestamp;
        }
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
            element.priority = usedNet.cost(element.nodePair.getU(), element.nodePair.getV()) - element.timestamp;
        }
    }

    public void sort(){
        this.listBufferNodePairs = this.listBufferNodePairs.stream()
                .sorted(Comparator.comparing(BufferNodePair::getPriority))
                .collect(Collectors.toList());
    }
}
