import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class SplayNet {

    public Node root;   // root of the BST

    public static class Node {
        public final int key;            // key
        public Node parent, left, right;   // left and right subtrees

        public Node(int key) {
            this.key = key;
        }
    }

    public static class CommunicatingNodes {
        int id;
        int u;
        int v;

        public CommunicatingNodes(int id, int u, int v){
            this.id = id;
            this.u = u;
            this.v = v;
        }
    }
    public int cost(int u, int v){
        Node node = this.root;
        Node common_ancestor;
        Node[] nodeSet = new SplayNet.Node[2];
        //Property used => u<=common_ancester<=v always
        while (node != null && ((u > node.key && v > node.key) || (u < node.key && v < node.key))) {
            if (u > node.key && v > node.key)     // if current_node<u<=v ... Go right
            {
                node = node.right;
            } else if (u < node.key && v < node.key)//if u<=v<current_node ... Go left
            {
                node = node.left;
            }
        }
        int cost = 0;
        common_ancestor = node;        //nodeSet[0]=common_ancester
        while (node != null && node.key != u)        //Finding Node(u)
        {
            if (u < node.key) {
                node = node.left;
            } else {
                node = node.right;
            }
            cost++;
        }
        node = common_ancestor;    //nodeSet[1]=uNode
        // System.out.println("line 92 common ancestor="+common_ancestor.key);
        while (node != null && node.key != v)        //Finding Node(u)
        {
            if (v < node.key) {
                node = node.left;
            } else {
                node = node.right;
            }
            cost++;
        }
        return cost;
    }
    /***************************************************************************
     *  Splay tree insertion.
     *  => Insert a node in the tree with key ='key'
     *  => Note: New node is always inserted at the root
     *         : This function does nothing if key already exists
     ***************************************************************************/
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

        //New node will always be inserted in the root.
        root = splay(root, key);

    }

    /***************************************************************************
     *  SplayNet function
     *  => This function communicates between two inputs key u and key v.
     *  => By the end of excution of this function bith u and v are splayed to their common ancestor
     ***************************************************************************/
    public void commute ( int u, int v) throws Exception        //Assuming u amd v always exist in the tree && u<=v
    {
        Node[] nodeSet = findNodes(u, v);    //Node[0]=common_ancestor; Node[1]=Node(u); NodeSet[2]=parent of common_ancestor
        Node common_ancester = nodeSet[0];
        Node parent_CA = nodeSet[1];
        Node uNode = nodeSet[2];
        Node vNode = nodeSet[3];
        if (parent_CA.key > common_ancester.key) {
            parent_CA.left = splay(common_ancester, u);
        } else if (parent_CA.key < common_ancester.key) {
            parent_CA.right = splay(common_ancester, u);
        } else {
            this.root = splay(this.root, u);
        }
        if (u == v)
            throw new Exception("gleiche Knoten kommunizieren");

        // check if v is left or right of u
        if (uNode.key > vNode.key) {
            uNode.left = splay(uNode.left, v);
        }
        if (uNode.key < vNode.key) {
            uNode.right = splay(uNode.right, v);
        }
    }

    public Node[] findNodes ( int u, int v)        // Returns an array with common ancester of u an v and Node of u
    {
        Node node = this.root;

        Node[] nodeSet = new Node[4];
        Node parent_CA = node;
        //Property used => u<=common_ancester<=v always

        while (node != null && ((u > node.key && v > node.key) || (u < node.key && v < node.key))) {
            if (u > node.key)     // if current_node<u<=v ... Go right
            {
                parent_CA = node;
                node = node.right;
            } else if (u < node.key)//if u<=v<current_node ... Go left
            {
                parent_CA = node;
                node = node.left;
            }
        }
        nodeSet[1] = parent_CA;
        nodeSet[0] = node;        //nodeSet[0]=common_ancester
        Node uNode = node;
        Node vNode = node;
        while (uNode != null && uNode.key != u)        //Finding Node(u)
        {
            if (u < uNode.key) {
                uNode = uNode.left;
            } else {
                uNode = uNode.right;
            }
        }
        while (vNode != null && vNode.key != v)        //Finding Node(v)
        {
            if (v < vNode.key) {
                vNode = vNode.left;
            } else {
                vNode = vNode.right;
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
    private Node splay (Node h,int key) throws Exception {
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
    private Node rotateRight (Node h) throws Exception {
        if (h.left == null) {
            throw new Exception("kein linkes kind bei rechtsrotation");
        }
        Node x = h.left;
        h.left = x.right;
        if (h.left != null) h.left.parent = h;
        x.right = h;
        x.parent = h.parent;
        h.parent = x;
        return x;
    }

    // left rotate
    private Node rotateLeft (Node h) throws Exception {
        if (h.right == null) {
            throw new Exception("kein rechtes kind bei linksrotation");
        }
        Node x = h.right;
        h.right = x.left;
        if (h.right != null) h.right.parent = h;
        x.left = h;
        x.parent = h.parent;
        h.parent = x;
        return x;
    }

    public void printPreorder (Node node){
        if (node == null) {
            return;
        }
        int left = 0;
        int right = 0;
        int parent = 0;
        if (node.right != null) {
            right = node.right.key;
        }
        if (node.left != null) {
            left = node.left.key;
        }
        if (node.parent != null) {
            parent = node.parent.key;
        }
        /* first print data of node */
        System.out.print("Knoten " + node.key + " hat linkes Kind: " + left + " und rechtes Kind: " + right + " und Parent: " + parent + "\n");

        /* then recur on left sutree */
        printPreorder(node.left);

        /* now recur on right subtree */
        printPreorder(node.right);
    }

}