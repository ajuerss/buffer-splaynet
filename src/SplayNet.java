import java.util.*;

public class SplayNet {

    private Node root;   // root of the BST
    private long searchCost = 0;
    private long routingCost = 0;
    private long rotationCost = 0;
    private boolean insertionOver = false;


    public void setInsertionOver(){
        this.insertionOver = true;
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

    public long getSearchCost(){
        return this.searchCost;
    }

    public void increaseSearchCost(int cost){
        if (insertionOver) this.searchCost += cost;
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
    public int cost(int u, int v){
        Node node = this.root;
        Node common_ancestor;
        while (node != null && ((u > node.key && v > node.key) || (u < node.key && v < node.key))) {
            if (u > node.key)
            {
                node = node.right;
            } else {
                node = node.left;
            }
            this.routingCost++;
        }
        int cost = 0;
        common_ancestor = node;
        while (node != null && node.key != u)        //Finding Node(u)
        {
            if (u < node.key) {
                node = node.left;
            } else {
                node = node.right;
            }
            cost++;
            this.routingCost++;
        }
        node = common_ancestor;
        while (node != null && node.key != v)        //Finding Node(u)
        {
            if (v < node.key) {
                node = node.left;
            } else {
                node = node.right;
            }
            cost++;
            this.routingCost++;
        }
        return cost;
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
        this.root.left = insertionIteration(nodeList.subList(0,k));
        this.root.right = insertionIteration(nodeList.subList(k+1,nodeList.size()));
    }

    public Node insertionIteration (List<Integer> nodeList){
        if (nodeList.isEmpty()) return null;
        int k = nodeList.size()/2;
        Node newNode = new Node(nodeList.get(k));
        newNode.left = insertionIteration(nodeList.subList(0,k));
        newNode.right = insertionIteration(nodeList.subList(k+1,nodeList.size()));
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
        root = splay(root, key);

    }

    /***************************************************************************
     *  SplayNet function
     *  => This function communicates between two inputs key u and key v.
     *  => By the end of excution of this function bith u and v are splayed to their common ancestor
     ***************************************************************************/
    public void commute ( int u, int v) throws Exception        //Assuming u amd v always exist in the tree && u<=v
    {
        Node[] nodeSet = findNodes(u, v);
        Node common_ancestor = nodeSet[0];
        Node parent_CA = nodeSet[1];
        Node uNode = nodeSet[2];
        Node vNode = nodeSet[3];
        if (parent_CA.key > common_ancestor.key) {
            parent_CA.left = splay(common_ancestor, u);
            this.increaseRoutingCost(1);
        } else if (parent_CA.key < common_ancestor.key) {
            parent_CA.right = splay(common_ancestor, u);
            this.increaseRoutingCost(1);
        } else {
            this.root = splay(this.root, u);
        }
        if (u == v)
            throw new Exception("gleiche Knoten kommunizieren");

        if (uNode.key > vNode.key) {
            uNode.left = splay(uNode.left, v);
        }
        if (uNode.key < vNode.key) {
            uNode.right = splay(uNode.right, v);
        }
    }

    public Node[] findNodes ( int u, int v){
        Node node = this.root;
        Node[] nodeSet = new Node[4];
        Node parent_CA = node;

        while (node != null && ((u > node.key && v > node.key) || (u < node.key && v < node.key))) {
            if (u > node.key)
            {
                parent_CA = node;
                node = node.right;
            } else {
                parent_CA = node;
                node = node.left;
            }
            this.increaseRoutingCost(1);
        }
        nodeSet[1] = parent_CA;
        nodeSet[0] = node;        //nodeSet[0]=common_ancester
        Node uNode = node;
        Node vNode = node;
        while (uNode != null && uNode.key != u)
        {
            if (u < uNode.key) {
                uNode = uNode.left;
            } else {
                uNode = uNode.right;
            }
            this.increaseRoutingCost(1);
        }
        while (vNode != null && vNode.key != v)
        {
            if (v < vNode.key) {
                vNode = vNode.left;
            } else {
                vNode = vNode.right;
            }
            this.increaseRoutingCost(1);
        }
        if (Objects.requireNonNull(uNode).getKey() != u || Objects.requireNonNull(vNode).getKey() != v){
            try {
                throw new Exception("U/V haben nicht die richtigen keys");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        nodeSet[2] = uNode;
        nodeSet[3] = vNode;
        return nodeSet;
    }


    /***************************************************************************
     * Splay tree function.
     * =>splay key in the tree rooted at Node h.
     * =>If a node with that key exists, it is splayed to the root of the tree.
     * => If it does not, the last node along the search path for the key is splayed to the root.
     * **********************************************************************/
    private Node splay (Node h, int key) throws Exception {
        if (h == null) return null;     //Node h does not exist

        int cmp1 = key - h.key;

        if (cmp1 < 0) {
            if (h.left == null) throw new Exception("Key not in tree");
            int cmp2 = key - h.left.key;
            this.increaseRoutingCost(1);
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
        if (x.right != null) this.increaseRotationCost(2);
        if (h.parent != null) this.increaseRotationCost(2);
        if (h.left != null) h.left.parent = h;
        x.right = h;
        x.parent = h.parent;
        h.parent = x;
        this.increaseRotationCost(2);
        return x;
    }

    // left rotate
    private Node rotateLeft (Node h) throws Exception {
        if (h.right == null) throw new Exception("kein rechtes kind bei linksrotation");
        Node x = h.right;
        h.right = x.left;
        if (x.left != null) this.increaseRotationCost(2);
        if (h.parent != null) this.increaseRotationCost(2);
        if (h.right != null) h.right.parent = h;
        x.left = h;
        x.parent = h.parent;
        h.parent = x;
        this.increaseRotationCost(2);
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