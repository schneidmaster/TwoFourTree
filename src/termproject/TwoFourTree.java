package termproject;

import java.util.Random;

/**
* This class implements the (2,4)-Tree ADT using the Dictionary interface.
*
* Title: Term Project 2-4 Trees
* @author Kyle Girtz
* @author Zach Schneider
* @version 1.0
* File: TwoFourTree.java
* Created: Dec 2011
* Summary of Modifications:
* Description: This ADT satisfies the Dictionary interface by holding data in a
* sorted way such that any item can be found via its key. The class offers a
* findElement() method for finding an element by its key, an insert() method for
* inserting data, and a removeElement() method for removing an element of the
* given key, as well as other basic ADT helper methods. TwoFourTree does not
* inherit from any other class nor is it expected to be derived from.
*/

public class TwoFourTree implements Dictionary {

    // Variable declaration
    private Comparator treeComp;
    private int size;
    private TFNode treeRoot;
    private static final Object NO_SUCH_KEY = "NO_SUCH_KEY";

    /**
	* Constructor
	* @param comp 
	*/
    public TwoFourTree(Comparator comp) {
        treeComp = comp;
	   treeRoot = null;
	   size = 0;
    }

    /**
	* Get method for root
	* @return root of the tree
	*/
    private TFNode root() {
        return treeRoot;
    }

    /**
	* Set method for root
	* @param root 
	*/
    private void setRoot(TFNode root) {
        treeRoot = root;
    }

    /**
	* Returns size of the tree
	* @return size of the tree
	*/
    public int size() {
        return size;
    }

    /**
	* Returns true is the tree has no elements in it
	* @return true is size is zero
	*/
    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Searches dictionary to determine if key is present
     * @param key to be searched for
     * @return object corresponding to key; null if not found
     */
    public Object findElement(Object key) {
	    // Find the TFNode that contains the correct object by calling
	    // findElementHere using the root
	    TFNode foundNode = findNode(key, root());
	    
	    // Check if a sentinel node was returned by findElementHere, 
	    // indicating that no match was found
	    if(isSentinel(foundNode)) {
		    return null;
	    }
	    
	    // If a valid TFNode is returned by findElementHere, we know that
	    // node must contain a correct result (since otherwise a sentinel would
	    // be returned), so FFGTE will always find an equal value
	    int theKey = FFGTE(foundNode, key);
	    
	    // Return the item
	    return foundNode.getItem(theKey);
    }
    
    /**
     * Recursively searches dictionary to determine if key is present
     * @param key to be searched for
	* @param topNode 
     * @return object corresponding to key; null if not found
     */
    private TFNode findNode(Object key, TFNode topNode) {
	    // Check if the TFNode contains the object we want
	    int index = FFGTE(topNode, key);
	    
	    // Check if the FFGTE result is equal to the desired key
	    if(index != topNode.getNumItems() && 
			  treeComp.isEqual(topNode.getItem(index).key(), key)) {
		    return topNode;
	    }
	    else {
		    // Check if the node has any children
		    if(isSentinel(topNode.getChild(index))) {
			    // If not, return the sentinel node
			    return topNode.getChild(index);
		    }
		    else {
			    // If so, recursively call the function at the correct child
			    return findNode(key, topNode.getChild(index));
		    }
	    }
    }

    /**
     * Inserts provided element into the Dictionary
     * @param key of object to be inserted
     * @param element to be inserted
     */
    public void insertElement(Object key, Object element) {
	    // Check for root
	    if(isEmpty()) {
		    //  Create a new TFNode
		    TFNode newRoot = new TFNode();
		    
		    // Put the first Item at its 0 index
		    newRoot.addItem(0, new Item(key, element));
		    
		    // Create two sentinels and add them to the root
		    TFNode sen1 = new TFNode();
		    TFNode sen2 = new TFNode();
		    sen1.addItem(0, new Item(NO_SUCH_KEY, null));
		    sen2.addItem(0, new Item(NO_SUCH_KEY, null));
		    newRoot.setChild(0, sen1);
		    newRoot.setChild(1, sen2);
		    sen1.setParent(newRoot);
		    sen2.setParent(newRoot);
		    
		    // Set as new root
		    setRoot(newRoot);
	    }
	    else {
		    // Find the correct place to put the node
		    TFNode insertNode = findNode(key, root());

		    // Make a childIndex to fill with the position of the new element
		    int childIndex = 0;

		    // Check if we got a sentinel back or if we have a tie
		    if(isSentinel(insertNode)) {
			    // Determine what child the sentinel is
			    childIndex = WCAI(insertNode);
			    insertNode = insertNode.getParent();

			    // Perform a shifting insert of the new Item
			    insertNode.insertItem(childIndex, new Item(key, element));
		    }
		    
		    // If not a sentinel insert at the inorder successor
		    else {			    
			    // Find inorder successor
			    insertNode = findIOS(insertNode, key);
			    
			    // Insert the item
			    childIndex = FFGTE(insertNode, key);
			    insertNode.insertItem(childIndex, new Item(key, element));
		    }

		    // Insert a new sentinel child
		    TFNode newSent = new TFNode();
		    newSent.addItem(0, new Item(NO_SUCH_KEY, null));
		    insertNode.setChild(childIndex, newSent);
		    newSent.setParent(insertNode);

		    // Check for overflow
		    if(insertNode.getNumItems() == 4) {
			    overflow(insertNode);
		    }
	    }
	    
	    // Increment size
	    size++;
    }

    /**
     * Searches dictionary to determine if key is present, then
     * removes and returns corresponding object
     * @param key of data to be removed
     * @return object corresponding to key
     * @exception ElementNotFoundException if the key is not in dictionary
     */
    public Object removeElement(Object key) throws ElementNotFoundException {
	    // If tree is empty throw exception
	    if(isEmpty()) {
		    throw new TwoFourTreeException();
	    }
	    
	    // Find node to delete from
	    TFNode deleteNode = findNode(key, root());
	    
	    // Check for missing element
	    if(isSentinel(deleteNode)) {
		    throw new ElementNotFoundException();
	    }
	    
	    // Extract the data to return
	    Object removedObject = (deleteNode.getItem(FFGTE(deleteNode, key))).element();
	    
	    // If leaf, delete and fix sentinels
	    if(isSentinel(deleteNode.getChild(0))) {
		    deleteNode.removeItem(FFGTE(deleteNode, key));
	    }
	    
	    // If internal, move inorder successor to parent
	    else {
		    TFNode nodeIOS = findIOS(deleteNode, key);
		    
		    // Move the in-order successor up to replace the deleted item
		    deleteNode.replaceItem(FFGTE(deleteNode, key), nodeIOS.getItem(0));
		    
		    // Delete the inorder successor
		    nodeIOS.removeItem(0);
		    
		    // Set deleteNode equal to nodeIOS to check for underflow
		    deleteNode = nodeIOS;
	    }
	    
	    // Check for underflow
	    if(deleteNode.getNumItems() == 0) {
			underflow(deleteNode);
	    }
	    
	    // Decrement size
	    size--;
	    
	    // Return the deleted object
	    return removedObject;
    }
    
    /**
	* Determines if the node is a sentinel
	* @param node to be checked
	* @return true if the node is a sentinel
	*/
    private boolean isSentinel(TFNode node) {
	    return ((node.getItem(0)).key() == "NO_SUCH_KEY");
    }
    
    /**
	* Finds the first item which is larger or equal to the given key
	* @param thisNode
	* @param key
	* @return 
	*/
    private int FFGTE(TFNode thisNode, Object key) {
	    // Cycle through items to find first greater than or equal to
	    for(int i = 0; i < thisNode.getNumItems(); i++) {
		    if(treeComp.isGreaterThanOrEqualTo((thisNode.getItem(i)).key(), 
														key)) {
			    return i;
		    }
	    }
	    
	    // Return last child index if key is larger than all items
	    return thisNode.getNumItems();
    }

    /**
	* Returns the index of the passed child node in the parent's array
	* @param thisNode
	* @return index of child
	*/
    private int WCAI(TFNode thisNode) {
	    TFNode parent = thisNode.getParent();
	    
	    // Cycle through children of the parent and return index of thisNode
	    for(int i = 0; i <= parent.getNumItems(); i++) {
		   if(parent.getChild(i) == thisNode) {
			   return i;
		   } 
	    }
	    
	    // Return -1 on error
	    return -1;
    }
    
    /**
	* Returns node in which the inorder successor is located
	* @param node
	* @param key
	* @return node of inorder successor
	*/
    private TFNode findIOS(TFNode node, Object key) {
	    int childIndex = 0;
	    
	    // Check if the equal item is at a leaf or internal node
	    if(!isSentinel(node.getChild(0))) {
		    // At an internal node find the inorder successor
		    int pathToIOS = FFGTE(node, key);
		    if(pathToIOS == node.getNumItems()) {
			    childIndex = pathToIOS;
		    }
		    else {
			    childIndex = pathToIOS + 1;
		    }

		    // Move down to inorder successor
		    node = node.getChild(childIndex);
		    while(!isSentinel(node.getChild(0))) {
			    node = node.getChild(0);
		    }
	    }
	    
	    return node;
    }
    
    /**
	* Fix tree on overflow
	* @param oFNode 
	*/
    private void overflow(TFNode oFNode) {
	    // Split the node
	    TFNode splitNode = new TFNode();
	    splitNode.addItem(0, oFNode.getItem(3));
	    
	    // Add the children
	    splitNode.setChild(0, oFNode.getChild(3));
	    splitNode.setChild(1, oFNode.getChild(4));
	    
	    // Set the children to point to the parents
	    (splitNode.getChild(0)).setParent(splitNode);
	    (splitNode.getChild(1)).setParent(splitNode);
	    
	    // Remove and record the third item and its child
	    TFNode childC = oFNode.getChild(2);
	    Item promotedItem = oFNode.removeItem(2);
	    
	    // Remove the fourth item which has been split off
	    oFNode.removeItem(2);
	    
	    // Reattach the lost child
	    oFNode.setChild(2, childC);
	    (oFNode.getChild(2)).setParent(oFNode);
	    
	    // Check if we're at the root and need to create a new root
	    if(oFNode == root()) {
		    TFNode newRoot = new TFNode();
		    newRoot.addItem(0, promotedItem);
		    
		    // Set as new root
		    setRoot(newRoot);
		    
		    // Set the children
		    newRoot.setChild(0, oFNode);
		    newRoot.setChild(1, splitNode);
		    
		    // Set the children to point to the parents
		    (newRoot.getChild(0)).setParent(newRoot);
		    (newRoot.getChild(1)).setParent(newRoot);
	    }
	    else {
		    // We're not at the root, so shove the item up to the parent
		    TFNode parentNode = oFNode.getParent();
		    parentNode.insertItem(WCAI(oFNode), promotedItem);
		    
		    // Hook up the new child
		    parentNode.setChild((WCAI(oFNode) + 1), splitNode);
		    
		    // Hook the new child to point back to the parent
		    splitNode.setParent(parentNode);
		    
		    // Check if the new parent node is overflowed
		    if(parentNode.getNumItems() == 4) {
			    overflow(parentNode);
		    }
	    }
    }
    
    /**
	* Fix tree on underflow
	* @param uFNode 
	*/
    private void underflow(TFNode uFNode) {
	    // Special case if the root has underflowed
	    if(uFNode == root()) {
		    setRoot(uFNode.getChild(0));
		    root().setParent(null);
	    }
	    // Check that uFNode isn't the first child, then check if the left
	    // sibling of uFNode has two or more items (permitting a transfer)
	    else if(WCAI(uFNode) > 0 && 
			((uFNode.getParent()).getChild(WCAI(uFNode) - 1)).getNumItems() > 1) {
		    
		    leftTransfer(uFNode);
	    }
	    // Check that uFNode isn't the last child, then check if the right
	    // sibling of uFNode has two or more items (permitting a transfer)
	    else if(WCAI(uFNode) < (uFNode.getParent()).getNumItems() && 
			((uFNode.getParent()).getChild(WCAI(uFNode) + 1)).getNumItems() > 1) {
		    
		    rightTransfer(uFNode);
	    }
	    // Check that uFNode isn't the first child, permitting a left fusion
	    else if(WCAI(uFNode) != 0) {
		    leftFusion(uFNode);
	    }
	    // If uFNode is the first child, perform a right fusion instead
	    else  {
		    rightFusion(uFNode);
	    }
    }
    
    /**
	* Transfers left sibling's item with parent and emptyNode
	* @param emptyNode 
	*/
    private void leftTransfer(TFNode emptyNode) {
	    TFNode parentNode = emptyNode.getParent();
	    TFNode siblingNode = parentNode.getChild(WCAI(emptyNode) - 1);
	    
	    // Copy down the correct parent item into the newly empty node
	    emptyNode.addItem(0, parentNode.getItem(WCAI(emptyNode) - 1));
	    
	    // Move the child at the 0 index into the 1 index
	    emptyNode.setChild(1, emptyNode.getChild(0));
	    
	    // Set the 0 child of emptyNode to its sibling's largest child
	    emptyNode.setChild(0, siblingNode.getChild(siblingNode.getNumItems()));
	    
	    // Set the displaced child to look at its new parent
	    (emptyNode.getChild(0)).setParent(emptyNode);
	    
	    // Null the sibling's largest child
	    siblingNode.setChild(siblingNode.getNumItems(), null);
	    
	    // Copy the item from the sibling child up to replace the parent
	    parentNode.replaceItem(WCAI(emptyNode) - 1, 
					siblingNode.getItem(siblingNode.getNumItems() - 1));
	    
	    // Delete the sibling's largest item
	    siblingNode.deleteItem(siblingNode.getNumItems() - 1);
    }
    
    /**
	* Transfers right sibling's item with parent and emptyNode
	* @param emptyNode 
	*/
    private void rightTransfer(TFNode emptyNode) {
	    TFNode parentNode = emptyNode.getParent();
	    TFNode siblingNode = parentNode.getChild(WCAI(emptyNode) + 1);
	    
	    // Copy down the correct parent item into the newly empty node
	    emptyNode.addItem(0, parentNode.getItem(WCAI(emptyNode)));
	    
	    // Copy the item from the sibling child up to replace the parent
	    parentNode.replaceItem(WCAI(emptyNode), siblingNode.getItem(0));
	    
	    // Set the 1 child of emptyNode to its sibling's smallst child
	    emptyNode.setChild(1, siblingNode.getChild(0));
	    
	    // Set the displaced child to look at its new parent
	    (emptyNode.getChild(1)).setParent(emptyNode);
	    
	    // Shifting delete the sibling's smallest item
	    siblingNode.removeItem(0); 
    }
    
    
    /**
	* Fuses emptyNode with left sibling
	* @param emptyNode 
	*/
    private void leftFusion(TFNode emptyNode) {
            int emptyIndex = WCAI(emptyNode);
	    TFNode parentNode = emptyNode.getParent();
	    TFNode siblingNode = parentNode.getChild(emptyIndex - 1);
	    
	    // Copy down the parent at WCAI - 1 into the left sibling
	    siblingNode.addItem(1, parentNode.getItem(emptyIndex - 1));
	    
	    // Set the empty node child to belong to the left sibling
	    siblingNode.setChild(2, emptyNode.getChild(0));
	    
	    // Set the parent of the child to the left sibling
	    (siblingNode.getChild(2)).setParent(siblingNode);
	    
	    // Delete the parent item that's now in the left sibling
	    parentNode.removeItem(emptyIndex - 1);
	    
	    // Set the "empty" node to the newly fused sibling node
	    parentNode.setChild(emptyIndex - 1, siblingNode);
	    
	    // Check if we've made the parent underflow and recursively call if so
	    if(parentNode.getNumItems() == 0) {
		    underflow(parentNode);
	    }
    }
    
    /**
	* Fuses thisNode with right sibling
	* @param emptyNode 
	*/
    private void rightFusion(TFNode emptyNode) {
	    TFNode parentNode = emptyNode.getParent();
	    TFNode siblingNode = parentNode.getChild(WCAI(emptyNode) + 1);
	    
	    // Copy down the parent at 0 into the right sibling
	    siblingNode.insertItem(0, parentNode.getItem(0));
	    
	    // Set the empty node child to belong to the left sibling
	    siblingNode.setChild(0, emptyNode.getChild(0));
	    
	    // Set the parent of the child to the left sibling
	    (siblingNode.getChild(0)).setParent(siblingNode);
	    
	    // Delete the parent item that's now in the left sibling
	    // This also deletes the newly empty node's pointer
	    parentNode.removeItem(0);
	    
	    // Check if we've made the parent underflow and recursively call if so
	    if(parentNode.getNumItems() == 0) {
		    underflow(parentNode);
	    }
    }
    
    /**
	* Prints the current state of the tree
	*/
    public void printAllElements() {
        int indent = 0;
        if (root() == null) {
            System.out.println("The tree is empty");
        }
        else {
            printTree(root(), indent);
        }
    }

    /**
	* Prints tree with start as root at indent level
	* @param start
	* @param indent 
	*/
    public void printTree(TFNode start, int indent) {
        if (start == null) {
            return;
        }
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
        printTFNode(start);
        indent += 4;
        int numChildren = start.getNumItems() + 1;
        for (int i = 0; i < numChildren; i++) {
		   // Do not print sentinels
		   if(start.getChild(i)!=null && !isSentinel(start.getChild(i))) {
			   printTree(start.getChild(i), indent);
		   }
        }
    }

    /**
	* Prints the element at node
	* @param node 
	*/
    public void printTFNode(TFNode node) {
        int numItems = node.getNumItems();
        for (int i = 0; i < numItems; i++) {
            System.out.print(((Item) node.getItem(i)).element() + " ");
        }
        System.out.println();
    }

    // checks if tree is properly hooked up, i.e., children point to parents
    public void checkTree() {
        checkTreeFromNode(treeRoot);
    }

    private void checkTreeFromNode(TFNode start) {
        if (start == null) {
            return;
        }

        if (start.getParent() != null) {
            TFNode parent = start.getParent();
            int childIndex = 0;
            for (childIndex = 0; childIndex <= parent.getNumItems(); childIndex++) {
                if (parent.getChild(childIndex) == start) {
                    break;
                }
            }
            // if child wasn't found, print problem
            if (childIndex > parent.getNumItems()) {
                System.out.println("Child to parent confusion");
                printTFNode(start);
            }
        }

        if (start.getChild(0) != null) {
            for (int childIndex = 0; childIndex <= start.getNumItems(); childIndex++) {
                if (start.getChild(childIndex) == null) {
                    System.out.println("Mixed null and non-null children");
                    printTFNode(start);
                }
                else {
                    if (start.getChild(childIndex).getParent() != start) {
                        System.out.println("Parent to child confusion");
                        printTFNode(start);
                    }
                    for (int i = childIndex - 1; i >= 0; i--) {
                        if (start.getChild(i) == start.getChild(childIndex)) {
                            System.out.println("Duplicate children of node");
                            printTFNode(start);
                        }
                    }
                }
            }
        }

        int numChildren = start.getNumItems() + 1;
        for (int childIndex = 0; childIndex < numChildren; childIndex++) {
            checkTreeFromNode(start.getChild(childIndex));
        }

    }
    /**
	* Main testing
	* @param args 
	*/
    public static void main(String[] args) {
	    
	   // Create the tree and comparator
        Comparator myComp = new IntegerComparator();
        TwoFourTree myTree = new TwoFourTree(myComp);
        final int TEST_SIZE = 10000;

	   // Insert elements
        for (int i = 0; i < TEST_SIZE; i++) {
            myTree.insertElement(new Integer(i), new Integer(i));
            myTree.checkTree();
        }
//	   myTree.printAllElements();
	   System.out.println("Correct size? " + (myTree.size() == TEST_SIZE));
	   
	   // Remove elements
        for (int i = 0; i < TEST_SIZE; i++) {
//		  System.out.println(myTree.size());
            int out = (Integer) myTree.removeElement(new Integer(i));
            if (out != i) {
                throw new TwoFourTreeException("main: wrong element removed");
            }
        }
	   myTree.printAllElements();
	   System.out.println("Correct size? " + myTree.isEmpty());
	   
	   // Repeat the test
	   // Insert elements
        Random rand = new Random(1);
        int [] nums = new int[TEST_SIZE];
        for (int i = 0; i < TEST_SIZE; i++) {
            int num = rand.nextInt(1000);
            nums[i] = num;
            myTree.insertElement(new Integer(num), new Integer(num));
            myTree.checkTree();
        }
	   myTree.printAllElements();
	   System.out.println("Correct size? " + (myTree.size() == TEST_SIZE));
	   
	   // Remove elements
        for (int i = 0; i < TEST_SIZE; i++) {
            int num = nums[TEST_SIZE-i-1];
            myTree.checkTree();
            System.out.println("removing "+num);
		//  System.out.println(myTree.size());
            int out = (Integer) myTree.removeElement(new Integer(num));
            if (out != num) {
                throw new TwoFourTreeException("main: wrong element removed");
            }
            if (i > TEST_SIZE - 15) {
                myTree.printAllElements();
            }
        }
	   myTree.printAllElements();
	   System.out.println("Correct size? " + myTree.isEmpty());
	   
        System.out.println("done");
    }
}
