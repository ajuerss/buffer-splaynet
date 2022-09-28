import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SplayNet {

    private Node root;   // root of the BST
    private long serviceCost = 0;
    private long routingCost = 0;
    private long rotationCost = 0;

    public int TF = 0;
    public int total = 0;
    private Buffer buffer;
    private boolean insertionOver = false;


    public void setInsertionOver(){
        this.insertionOver = true;
    }

    public void resetCostCounter() {
        this.serviceCost = 0;
        this.routingCost = 0;
        this.rotationCost = 0;
    }

    public Node getRoot(){
        return this.root;
    }

    public long getRotationCost(){
        return this.rotationCost;
    }

    public void increaseRotationCost(int cost){
        if (insertionOver) this.rotationCost += cost;
    }

    public long getRoutingCost(){
        return this.routingCost;
    }

    public void increaseRoutingCost(int cost){
        if (insertionOver) this.routingCost += cost;
    }

    public long getServiceCost(){
        return this.serviceCost;
    }

    public void increaseServingCost(int cost) throws Exception {
        if (cost < 0) throw new Exception("distance < 0");
        if (cost == 0) throw new Exception("distance = 0");
        if (insertionOver) this.serviceCost += cost;
    }

    public void setBuffer(Buffer buffer) {this.buffer = buffer; }

    public Buffer getBuffer(){ return this.buffer;}

    public static class Node {
        private final int key;
        private Node parent, left, right;

        private int lastLeftParent = 0;

        private int lastRightParent = Integer.MAX_VALUE;

        public Node(int key) {
            this.key = key;
        }

        public int getKey(){
            return this.key;
        }

        public Node getLeft(){
            return this.left;
        }

        public Node getRight(){
            return this.right;
        }
        public Node getParent(){ return this.parent; }
    }

    public static class CommunicatingNodes {
        private final int id;
        private final int u;
        private final int v;

        public CommunicatingNodes(int id, int u, int v){
            this.id = id;
            this.u = u;
            this.v = v;
        }
        public int getId(){
            return this.id;
        }
        public int getU(){
            return this.u;
        }
        public int getV(){
            return this.v;
        }
    }
    public int calculateDistance(boolean noBuffer, int u, int v){
        Node node = this.root;
        Node common_ancestor;
        int distance = 0;
        while (node != null && ((u > node.key && v > node.key) || (u < node.key && v < node.key))) {
            if (u > node.key)
            {
                node = node.right;
            } else {
                node = node.left;
            }
        }
        common_ancestor = node;
        while (node != null && node.key != u)        //Finding Node(u)
        {
            if (u < node.key) {
                node = node.left;
            } else {
                node = node.right;
            }
            distance++;
        }
        node = common_ancestor;
        while (node != null && node.key != v)        //Finding Node(v)
        {
            if (v < node.key) {
                node = node.left;
            } else {
                node = node.right;
            }
            distance++;
        }
        if (!noBuffer) this.increaseRoutingCost(distance);
        return distance;
    }
    /***************************************************************************
     *  Splay tree insertion.
     *  => Insert a node in the tree with key ='key'
     *  => Note: New node is always inserted at the root
     *         : This function does nothing if key already exists
     ***************************************************************************/
    public void insertBalancedBST(ArrayList<Integer> nodeList) {
        Collections.sort(nodeList);
        int k = nodeList.size()/2;
        this.root = new Node(nodeList.get(k));
        this.root.left = insertionIteration(nodeList.subList(0,k), this.root);
        this.root.right = insertionIteration(nodeList.subList(k+1,nodeList.size()), this.root);
    }

    public void assignLastParents(Node node, int left, int right){
        if (node != null){
            node.lastLeftParent = left;
            node.lastRightParent = right;
            assignLastParents(node.left, left, node.key);
            assignLastParents(node.right, node.key, right);
        }
    }

    public void checkLastParents(Node node, int left, int right) throws Exception {
        if (node != null){
            if (!(node.lastRightParent == right)){
                System.out.printf("Node %d has lastRightParent %d, but should have %d", node.getKey(), node.lastRightParent, right);
                this.printPreorder(this.root);
                throw new Exception("last parents wrong");
            }
            if (!(node.lastLeftParent == left)){
                System.out.printf("Node %d has lastLeftParent %d, but should have %d\n", node.getKey(), node.lastLeftParent, left);
                this.printPreorder(this.root);
                throw new Exception("last parents wrong");
            }
            assignLastParents(node.left, left, node.key);
            assignLastParents(node.right, node.key, right);
        }
    }

    public Node insertionIteration (List<Integer> nodeList, Node parent){
        if (nodeList.isEmpty()) return null;
        int k = nodeList.size()/2;
        Node newNode = new Node(nodeList.get(k));
        newNode.parent = parent;
        newNode.left = insertionIteration(nodeList.subList(0,k), newNode);
        newNode.right = insertionIteration(nodeList.subList(k+1,nodeList.size()), newNode);
        return newNode;
    }
    /***************************************************************************
     *  SplayNet function
     *  => This function communicates between two inputs key u and key v.
     *  => By the end of excution of this function bith u and v are splayed to their common ancestor
     ***************************************************************************/
    public void commute (int u, int v) throws Exception
    {
        int uNode = u;
        int vNode = v;
        Node common_ancestor = findLCA(uNode, vNode);
        // We move u always as LCA, if v is already LCA, we switch v to u and v to u
        if (common_ancestor.getKey() == vNode){
            int temp = uNode;
            uNode = vNode;
            vNode = temp;
        }
        Node parent_CA = Objects.requireNonNullElse(common_ancestor.parent, common_ancestor);
        Node newLCA;
        if (parent_CA.key > common_ancestor.key) {
            newLCA = splay_new(common_ancestor, uNode);
        } else if (parent_CA.key < common_ancestor.key) {
            newLCA = splay_new(common_ancestor, uNode);
        } else {
            newLCA = splay_new(this.root, uNode);
        }
        if (uNode == vNode)
            throw new Exception("gleiche Knoten kommunizieren");
        if (uNode > vNode) {
            splay_new(newLCA.left, vNode);
        }
        if (uNode < vNode) {
            splay_new(newLCA.right, vNode);
        }
        //this.increaseRoutingCost(1);
    }

    public Node findLCA (int u, int v){
        Node node = this.root;
        int cost = 0;
        while (node != null && ((u > node.key && v > node.key) || (u < node.key && v < node.key))) {
            if (u > node.key)
            {
                node = node.right;
            } else {
                node = node.left;
            }
            cost++;
        }
        assert node != null;
        //this.increaseRoutingCost(cost);
        return node;
    }


    /***************************************************************************
     * Splay tree function.
     * =>splay key in the tree rooted at Node h.
     * =>If a node with that key exists, it is splayed to the root of the tree.
     * => If it does not, the last node along the search path for the key is splayed to the root.
     * **********************************************************************/
    private Node splay_new(Node h, int key) throws Exception {
        if (h == null) throw new Exception("Node in Splay() does not exist");
        Node node = h;
        int cost = 0;
        while (node != null){
            if (node.getKey() > key){
                node = node.left;
                cost++;
            }else if (node.getKey() < key){
                node = node.right;
                cost++;
            }else if (node.getKey() == key){
                //this.increaseRoutingCost(cost);
                splay_up(h, node);
                return node;
            }
        }
        throw new Exception("Node in Splay() not found");
    }
    private Node splay_up(Node h, Node k) throws Exception {
        if (h == k){
            return k;
        } else if (k.getParent() == h){
            if(h.getLeft() == k){
                rotateRight(h);
            }else if (h.getRight() == k){
                rotateLeft(h);
            }else{
                throw new Exception("H should be parent, but not has k as child");
            }
            return k;
        }else{
            boolean found = (h == k.getParent().getParent());
            if (k.getParent().getParent().getRight() == k.getParent()){
                if (k.getParent().getRight() == k){
                    rotateLeft(k.getParent().getParent());
                }else if (k.getParent().getLeft() == k){
                    rotateRight(k.getParent());
                }else{
                    throw new Exception("k.p has not k as child");
                }
                rotateLeft(k.getParent());
            }else if (k.getParent().getParent().getLeft() == k.getParent()){
                if (k.getParent().getRight() == k){
                    rotateLeft(k.getParent());
                }else if (k.getParent().getLeft() == k){
                    rotateRight(k.getParent().getParent());
                }else{
                    throw new Exception("k.p has not k as child");
                }
                rotateRight(k.getParent());
            } else{
                throw new Exception("k.p.p has not k.p as child");
            }
            if(found) return k;
        }
        return splay_up(h,k);
    }

    /***************************************************************************
     *  Helper functions.
     ***************************************************************************/

    // right rotate
    private Node rotateRight (Node h) throws Exception {
        if (h.left == null) throw new Exception("kein linkes kind bei rechtsrotation");
        Node x = h.left;
        h.left = x.right;
        int cost = 0;
        if (x.right != null) cost+=2;
        if (h.parent != null) cost+=2;
        if (h.left != null) h.left.parent = h;
        x.right = h;
        x.parent = h.parent;
        h.parent = x;
        if (x.parent != null){
            if (x.parent.left == h){
                x.parent.left = x;
            }else if (x.parent.right == h){
                x.parent.right = x;
            }else{
                throw new Exception("x.p hatte h nicht als kind");
            }
        }else{
            this.root = x;
        }
        cost+=2;
        increaseRotationCost(cost);

        x.lastRightParent = h.lastRightParent;
        h.lastLeftParent = x.key;

        int[] a = {x.lastLeftParent+1, x.key};
        int[] b = {h.key, h.lastRightParent-1};
        int[] c;
        if (h.left != null){
            c = new int[]{h.left.lastLeftParent + 1, h.left.lastRightParent - 1};
        }else{
            c = new int[]{-1, -1};
        }
        this.buffer.updateDistances(a, b, c);

        return x;
    }

    // left rotate
    private Node rotateLeft (Node h) throws Exception {
        if (h.right == null) throw new Exception("kein rechtes kind bei linksrotation");
        Node x = h.right;
        h.right = x.left;
        int cost = 0;
        if (x.left != null) cost+=2;
        if (h.parent != null) cost+=2;
        if (h.right != null) h.right.parent = h;
        x.left = h;
        x.parent = h.parent;
        h.parent = x;
        if (x.parent != null){
            if (x.parent.left == h){
                x.parent.left = x;
            }else if (x.parent.right == h){
                x.parent.right = x;
            }else{
                throw new Exception("x.p hatte h nicht als kind");
            }
        }else{
            this.root = x;
        }
        cost+=2;
        increaseRotationCost(cost);

        x.lastLeftParent = h.lastLeftParent;
        h.lastRightParent = x.key;
        int[] a = {x.key, x.lastRightParent-1};
        int[] b = {h.lastLeftParent+1, h.key};
        int[] c;
        if (h.right != null){
            c = new int[]{h.right.lastLeftParent + 1, h.right.lastRightParent - 1};
        }else{
            c = new int[]{-1, -1};
        }
        this.buffer.updateDistances(a, b, c);

        return x;
    }

    public void printPreorder (Node node){
        if (node == null) {
            return;
        }
        int left = 0;
        int right = 0;
        int parent = 0;
        if (node.right != null) right = node.right.key;
        if (node.left != null) left = node.left.key;
        if (node.parent != null) parent = node.parent.key;
        System.out.printf("Node %d has left Child: %d and right Child: %d and Parent: %d... lastleft %d lastright %d\n", node.key, left, right, parent, node.lastLeftParent, node.lastRightParent);
        printPreorder(node.left);
        printPreorder(node.right);
    }

    private Node splay (Node h, int key) throws Exception {
        if (h == null) throw new Exception("Node in Splay() does not exist");

        int cmp1 = key - h.key;
        if (cmp1 < 0) {
            if (h.left == null) throw new Exception("Key not in tree");
            int cmp2 = key - h.left.key;
            this.increaseRoutingCost(1);
            //System.out.printf("First iteration in splay +1 for %d\n", h.getKey());
            if (cmp2 < 0) {     //Left-left case => 2 times right rotate
                if (h.left.left == null) throw new Exception("Key not in tree");
                this.increaseRoutingCost(1);
                h.left.left = splay(h.left.left, key);
                h = rotateRight(h); //Right rotate
            } else if (cmp2 > 0) {//Left-Right case => Right rotate then Left rotate
                h.left.right = splay(h.left.right, key);
                this.increaseRoutingCost(1);
                if (h.left.right != null)
                    h.left = rotateLeft(h.left); //Left rotate
            }

            return rotateRight(h);   //Right Rotate
        } else if (cmp1 > 0) {
            if (h.right == null) throw new Exception("Key not in tree");
            int cmp2 = key - h.right.key;
            this.increaseRoutingCost(1);
            if (cmp2 < 0) {             //Right-Left case
                h.right.left = splay(h.right.left, key);
                this.increaseRoutingCost(1);
                if (h.right.left != null)
                    h.right = rotateRight(h.right);  //Right Rotate
            } else if (cmp2 > 0) {        //Right-Right case
                h.right.right = splay(h.right.right, key);
                this.increaseRoutingCost(1);
                h = rotateLeft(h);      //Left rotate
            }
            return rotateLeft(h);   //Left rotate
        } else return h;
    }


}