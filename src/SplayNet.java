import java.util.*;

public class SplayNet {

    private Node root;   // root of the BST
    private long serviceCost = 0;
    private long routingCost = 0;
    private long rotationCost = 0;
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

    public void increaseServingCost(int cost){
        if (insertionOver) this.serviceCost += cost;
    }

    public static class Node {
        private final int key;
        private Node parent, left, right;

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
    public int calculateDistance(Buffer.BufferNodePair element, int u, int v, boolean noBuffer){
        Node node = this.root;
        Node common_ancestor;
        int costLca = 0;                // Cost for finding LCA
        int costDistance = 0;           // Cost for calculating the Distance
        int distance = 0;
        while (node != null && ((u > node.key && v > node.key) || (u < node.key && v < node.key))) {
            if (u > node.key)
            {
                node = node.right;
            } else {
                node = node.left;
            }
            costLca++;
        }
        common_ancestor = node;
        element.setLca(common_ancestor);
        while (node != null && node.key != u)        //Finding Node(u)
        {
            if (u < node.key) {
                node = node.left;
            } else {
                node = node.right;
            }
            costDistance++;
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
            costDistance++;
            distance++;
        }
        this.increaseRoutingCost(costLca);
        //System.out.printf("Routingcost increased by %d for finding LCA %d\n", costLca, common_ancestor.getKey());
        if (!noBuffer) this.increaseRoutingCost(costDistance);
        //System.out.printf("Routingcost increased by %d for finding distance\n", costDistance);
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

    public Node insertionIteration (List<Integer> nodeList, Node parent){
        if (nodeList.isEmpty()) return null;
        int k = nodeList.size()/2;
        Node newNode = new Node(nodeList.get(k));
        newNode.parent = parent;
        newNode.left = insertionIteration(nodeList.subList(0,k), newNode);
        newNode.right = insertionIteration(nodeList.subList(k+1,nodeList.size()), newNode);
        return newNode;
    }

    public void insert ( int key) throws Exception {
        if (root == null) {         //=> Tree is null
            root = new Node(key);
            return;
        }
        Node iterator = root;
        Node lastNode = iterator;
        while (iterator != null) {
            lastNode = iterator;
            if (key < iterator.key) {
                iterator = iterator.left;
            } else if (key > iterator.key) {
                iterator = iterator.right;
            } else {
                throw new Exception("es gibt schon diesen Key");
            }
        }
        Node newNode = new Node(key);
        if (key > lastNode.key) {
            lastNode.right = newNode;
            newNode.parent = lastNode;
        } else if (key < lastNode.key) {
            lastNode.left = newNode;
            newNode.parent = lastNode;
        } else {
            throw new Exception("es gibt schon diesen Key oder es ist ein fehler beim einfügen pasiert");
        }
        splay_new(root, key);

    }

    /***************************************************************************
     *  SplayNet function
     *  => This function communicates between two inputs key u and key v.
     *  => By the end of excution of this function bith u and v are splayed to their common ancestor
     ***************************************************************************/
    public Node commute (Node k, int u, int v) throws Exception
    {
        /*
        Node[] nodeSet = findLCA(u, v);
        Node common_ancestor = nodeSet[0];
        Node parent_CA = nodeSet[1];
        */
        if (k == null) throw new Exception ("Commute wurde ein leerer node als lca gegeben");
        Node common_ancestor = k;
        Node parent_CA;
        parent_CA = Objects.requireNonNullElse(common_ancestor.parent, common_ancestor);
        Node newLCA;
        if (parent_CA.key > common_ancestor.key) {
            newLCA = splay_new(common_ancestor, u);
        } else if (parent_CA.key < common_ancestor.key) {
            newLCA = splay_new(common_ancestor, u);
        } else {
            newLCA = splay_new(this.root, u);
        }
        if (u == v)
            throw new Exception("gleiche Knoten kommunizieren");
        if (u > v) {
            splay_new(newLCA.left, v);
        }
        if (u < v) {
            splay_new(newLCA.right, v);
        }
        this.increaseRoutingCost(1);
        //System.out.println("Routingcost increased by 1 for routing to element v to splay up and initialize splay");
        return newLCA;
    }

    public Node[] findLCA ( int u, int v){
        Node node = this.root;
        Node[] nodeSet = new Node[2];
        Node parent_CA = node;
        int cost = 0;
        while (node != null && ((u > node.key && v > node.key) || (u < node.key && v < node.key))) {
            if (u > node.key)
            {
                parent_CA = node;
                node = node.right;
            } else {
                parent_CA = node;
                node = node.left;
            }
            cost++;
        }
        assert node != null;
        this.increaseRoutingCost(cost);
        nodeSet[1] = parent_CA;
        nodeSet[0] = node;        //nodeSet[0]=common_ancester
        return nodeSet;
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
                //System.out.printf("Routing Cost increased by %d for finding %d from LCA for splay\n", cost, key);
                this.increaseRoutingCost(cost);
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
        System.out.print("Node " + node.key + " has left Child: " + left + " and right Child: " + right + " and Parent: " + parent + "\n");
        printPreorder(node.left);
        printPreorder(node.right);
    }

}