import java.util.ArrayList;

public class test {

    public static void main(String[] args) throws Exception {
        SplayNet.CommunicatingNodes x1 = new SplayNet.CommunicatingNodes(1, 1, 2);
        SplayNet.CommunicatingNodes x5 = new SplayNet.CommunicatingNodes(5, 1, 2);
        SplayNet.CommunicatingNodes x2 = new SplayNet.CommunicatingNodes(2, 1, 3);
        SplayNet.CommunicatingNodes x3 = new SplayNet.CommunicatingNodes(3, 2, 3);
        SplayNet.CommunicatingNodes x4 = new SplayNet.CommunicatingNodes(4, 3, 2);

        Buffer.BufferNodePair y1 = new Buffer.BufferNodePair(x1);
        Buffer.BufferNodePair y5 = new Buffer.BufferNodePair(x5);
        Buffer.BufferNodePair y2 = new Buffer.BufferNodePair(x2);
        Buffer.BufferNodePair y3 = new Buffer.BufferNodePair(x3);
        Buffer.BufferNodePair y4 = new Buffer.BufferNodePair(x4);

        ArrayList<Buffer.BufferNodePair> list = new ArrayList<>();
        list.add(y1);
        list.add(y2);
        list.add(y3);
        list.add(y4);
        list.add(y5);
    }
}
