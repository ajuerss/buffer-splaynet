import java.util.Scanner;


public class SplayNet {
    private Node root;   // root of the BST

    // BST helper node data type
    private class Node {
        private int key;            // key
        private Node left, right;   // left and right subtrees

        public Node(int key) {
            this.key = key;
        }
    }

    /***************************************************************************
     *  Splay tree insertion.
     *  => Insert a node in the tree with key ='key'
     *  => Note: New node is always inserted at the root
     *         : This function does nothing if key already exists
     ***************************************************************************/
    public void insert(int key) {
        if (root == null) {         //=> Tree is null
            root = new Node(key);
            return;
        }
        //New node will always be inserted in the root.
        root = splay(root, key);
        int cmp = key - root.key;
        // Insert new node at root
        if (cmp < 0) {          //Go to left subtree
            Node n = new Node(key);
            n.left = root.left;
            n.right = root;
            root.left = null;
            root = n;
        }

        // Insert new node at root
        else if (cmp > 0) {     //Go to right subtree
            Node n = new Node(key);
            n.right = root.right;
            n.left = root;
            root.right = null;
            root = n;
        }
    }

    /***************************************************************************
     *  Splay tree deletion.
     ***************************************************************************/
    /* This splays the key, then does a slightly modified Hibbard deletion on
     * the root (if it is the node to be deleted; if it is not, the key was
     * not in the tree). The modification is that rather than swapping the
     * root (call it node A) with its successor, it's successor (call it Node B)
     * is moved to the root position by splaying for the deletion key in A's
     * right subtree. Finally, A's right child is made the new root's right
     * child.
     */
    public void remove(int key) {
        if (root == null) return; // empty tree

        root = splay(root, key);

        int cmp = key - root.key;

        if (cmp == 0) {
            if (root.left == null) {
                root = root.right;
            } else {
                Node x = root.right;
                root = root.left;
                splay(root, key);
                root.right = x;
            }
        }
        // else: it wasn't in the tree to remove
    }

    /***************************************************************************
     *  SplayNet function
     *  => This function communicates between two inputs key u and key v.
     *  => By the end of excution of this function bith u and v are splayed to their common ancestor
     ***************************************************************************/
    public void commute(int u, int v)        //Assuming u amd v always exist in the tree && u<=v
    {

        Node[] nodeSet = findNodes(u, v);    //Node[0]=common_ancestor; Node[1]=Node(u); NodeSet[2]=parent of common_ancestor
        Node common_ancester = nodeSet[0];
        Node uNode = nodeSet[1];
        Node parent_CA=nodeSet[2];
        if(parent_CA.key>common_ancester.key) {
            parent_CA.left = splay(common_ancester, u);
        }
        else if(parent_CA.key<common_ancester.key)
        {
            parent_CA.right=splay(common_ancester,u);
        }
//        System.out.println("after splay1:");
//        this.printPreorder(this.root);          //Print tree in preorder fashion
//        System.out.println();
        if (u == v)
            return;
        uNode.right = splay(uNode.right, v);          //if v is not there, node closest to v will come to uNode
//        System.out.println("after splay2:");
//        this.printPreorder(this.root);
//        System.out.println();
    }

    public Node[] findNodes(int u, int v)        // Returns an array with common ancester of u an v and Node of u
    {
        Node node = this.root;
        Node[] nodeSet = new Node[3];
        Node parent_CA=node;
        //Property used => u<=common_ancester<=v always
        while (node != null && ((u > node.key && v > node.key) || (u < node.key && v < node.key))) {
            if (u > node.key && v > node.key)     // if current_node<u<=v ... Go right
            {
                parent_CA=node;
                node = node.right;
            } else if (u < node.key && v < node.key)//if u<=v<current_node ... Go left
            {
                parent_CA=node;
                node = node.left;
            }
        }
        nodeSet[2]=parent_CA;
        nodeSet[0] = node;        //nodeSet[0]=common_ancester
        while (node != null && node.key != u)        //Finding Node(u)
        {
            if (u < node.key) {
                node = node.left;
            } else {
                node = node.right;
            }
        }
        nodeSet[1] = node;    //nodeSet[1]=uNode
        return nodeSet;
    }


    /***************************************************************************
     * Splay tree function.
     * =>splay key in the tree rooted at Node h.
     * =>If a node with that key exists, it is splayed to the root of the tree.
     * => If it does not, the last node along the search path for the key is splayed to the root.
     * **********************************************************************/
    private Node splay(Node h, int key) {
        if (h == null) return null;     //Node h does not exist

        int cmp1 = key - h.key;

        if (cmp1 < 0) {
            if (h.left == null) {
                return h;       // key not in tree, so we're done
            }
            int cmp2 = key - h.left.key;
            if (cmp2 < 0) {     //Left-left case => 2 times right rotate
                h.left.left = splay(h.left.left, key);
                h = rotateRight(h); //Right rotate
            } else if (cmp2 > 0) {//Left-Right case => Right rotate then Left rotate
                h.left.right = splay(h.left.right, key);
                if (h.left.right != null)
                    h.left = rotateLeft(h.left); //Left rotate
            }

            if (h.left == null) return h;
            else
                return rotateRight(h);   //Right Rotate
        } else if (cmp1 > 0) {

            if (h.right == null) {      // key not in tree, so we're done
                return h;
            }

            int cmp2 = key - h.right.key;
            if (cmp2 < 0) {             //Right-Left case
                h.right.left = splay(h.right.left, key);
                if (h.right.left != null)
                    h.right = rotateRight(h.right);  //Right Rotate
            } else if (cmp2 > 0) {        //Right-Right case
                h.right.right = splay(h.right.right, key);
                h = rotateLeft(h);      //Left rotate
            }

            if (h.right == null) return h;
            else
                return rotateLeft(h);   //Left rotate
        } else return h;
    }


    /***************************************************************************
     *  Helper functions.
     ***************************************************************************/

    // right rotate
    private Node rotateRight(Node h) {
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        return x;
    }

    // left rotate
    private Node rotateLeft(Node h) {
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        return x;
    }

    public void printPreorder(Node node) {
        if (node == null){
            return;
        }
        int left = 0;
        int right = 0;
        if (node.right != null){
            right = node.right.key;
        }
        if (node.left != null){
            left = node.left.key;
        }
        /* first print data of node */
        System.out.print("Knoten " + node.key + " hat linkes Kind: " + left + " und rechtes Kind: " + right + "\n");

        /* then recur on left sutree */
        printPreorder(node.left);

        /* now recur on right subtree */
        printPreorder(node.right);
    }

    public static void main(String args[]) {
        /* input type:-
        line 1--> total number of nodes in the tree (say n)
        line 2--> list of nodes ( n integer values)
        line 3--> total number of commute (or SplayNet) queries (say m)
        next corresponding m lines will each contain a pair of integers (between which communication will happen)
        Eg of input:-
        5
        11 5 9 13 1
        2
        5 11
        1 13
        Corresponding output:-
        input tree:
        1 5 11 9 13
        Final tree:
        1 13 11 5 9

        Scanner s = new Scanner(System.in);
        int num_nodes = s.nextInt();      //total number of nodes
        SplayNet sn1 = new SplayNet();
        for (int i = 0; i < num_nodes; i++) {
            sn1.insert(s.nextInt());
        }
        System.out.println("input tree:");
        sn1.printPreorder(sn1.root);
        System.out.println();
        int query_num = s.nextInt();      //total number of commute queries
        long start = System.currentTimeMillis();
        for (int i = 0; i < query_num; i++) {
            sn1.commute(s.nextInt(), s.nextInt());
        }
        long end = System.currentTimeMillis();
        System.out.println("Berechnungszeit: " + (end-start));
        System.out.println("Final tree:");
        sn1.printPreorder(sn1.root);
        System.out.println();
        */


    }

}
