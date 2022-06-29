import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Buffer {
    SplayNet usedNet;
    int bufferSize;
    List<BufferNodePair> listBufferNodePairs = new ArrayList<>();

    public Buffer (SplayNet inputNet, int bufferSize){
        this.usedNet = inputNet;
        this.bufferSize = bufferSize;
    }

    public boolean isFull(){
        if (listBufferNodePairs == null){
            return false;
        }
        return listBufferNodePairs.size() >= bufferSize;
    }

    public static class BufferNodePair{
        SplayNet.CommunicatingNodes nodePair;
        int priority;
        int timestamp;

        public BufferNodePair(SplayNet.CommunicatingNodes commNodes){
            this.nodePair = commNodes;
            this.timestamp = 0;
        }

        public int getPriority() {
            return this.priority;
        }

    }
    public void increaseTimestamp(){
        if (listBufferNodePairs == null){
            return;
        }
        for (BufferNodePair element: listBufferNodePairs){
            element.timestamp++;
        }
    }


    public void calcPriority(){
        for (BufferNodePair element: listBufferNodePairs){
            element.priority = usedNet.cost(element.nodePair.u, element.nodePair.v) - element.timestamp;
        }
    }

    public void sort(){
        this.listBufferNodePairs = this.listBufferNodePairs.stream()
                .sorted(Comparator.comparing(BufferNodePair::getPriority))
                .collect(Collectors.toList());
    }
}
