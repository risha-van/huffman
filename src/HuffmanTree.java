/*  Student information for assignment:
 *
 *  On our honor, Olivia Mates and Risha Vankalapati, this programming assignment is our own work
 *  and we have not provided this code to any other student.
 *
 *  Number of slip days used: 1
 *
 *  Student 1 (Student whose Canvas account is being used): Olivia Mates
 *  UTEID: ojm432
 *  email address: ojm432@my.utexas.edu
 *  Grader name: Casey Charleston
 *
 *  Student 2: Risha Vankalapati
 *  UTEID: rv24373
 *  email address: risha.vankalapati@utexas.edu
 *
 */
public class HuffmanTree {

	private TreeNode parent; 
	
	//Constructor of a Huffman Tree
	public HuffmanTree(TreeNode root) {
		parent = root;
	}
	
	//Returns the root of the tree
	public TreeNode getTree() {
		return parent;
	}
	
	//Returns the size of the entire tree 
	public int size(TreeNode curr) {
		int size = 0;
		if(curr == null) {
			return 0;
		}
		size = 1 + size(curr.getLeft()) + size(curr.getRight());
		return size;
	}
	
	//Returns the number of leaves in the entire tree
	public int numOfLeaf(TreeNode curr) {
		if(curr.isLeaf()) {
			return 1;
		}
		return numOfLeaf(curr.getLeft()) + numOfLeaf(curr.getRight());
	}

}
