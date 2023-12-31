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

import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private TreeNode tree;
    private int[] freq;
    private int compressionBits;
    private int headerType;
    private HuffmanTree encoding;
    private String[] codes;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
    	//Creating variables to use
        boolean canRead = true;
        headerType = headerFormat;
        int origCounter = 0;
        int compressCounter = 0;
        //Adds + 1 for PSUEDO_EOF
        freq = new int[ALPH_SIZE + 1];
        BitInputStream bitsIn =  new BitInputStream(in);
        //Reading each chunk and assigning a frequency to every value in the array
        while(canRead) {
        	int currBit = bitsIn.readBits(BITS_PER_WORD);
        	if(currBit == -1) {
        		canRead = false;
        	}
        	else {
        		freq[currBit]++;
                origCounter += BITS_PER_WORD;
        	}
        }
        //Creating a Huffman tree using a priority queue
        freq[ALPH_SIZE] = 1;
        createTree();
        bitsIn.close();
    	compressCounter +=  2 * BITS_PER_INT;
    	
    	//Calculating the number of compressed bits based on header type
    	if(headerType == STORE_COUNTS) {
    		compressCounter += ALPH_SIZE * BITS_PER_INT;
        	compressCounter += compressionCounter(tree, 0);
    	}
    	else {
    		compressCounter += (encoding.numOfLeaf(encoding.getTree()) * (BITS_PER_WORD + 1))
    				+ (encoding.size(encoding.getTree())) + BITS_PER_INT;
    		compressCounter += compressionCounter(tree, 0);
    	}
        return origCounter - compressCounter;
    }
    
    
    //Helper method to count how many bits are created in the newly
    //compressed file data to be return in preprocessCompress
    //@param curr = the tree we are traversing through to count
    //@param counter = the counter of the amount of bits
    private int compressionCounter(TreeNode curr, int counter) {
    	int ret = 0;
    	if(curr.getValue() != -1) {
    		ret += curr.getFrequency() * counter;
    	}
    	else {
    		ret += compressionCounter(curr.getLeft(), counter + 1);
        	ret +=compressionCounter(curr.getRight(), counter + 1);
    	}
    	return ret;
    }
    
    /*
     * Helper method that creates a priority queue using the frequencies
     * of each character. This method then makes a tree based on the 
     * priority queue
     */
    private void createTree() {
    	PriorityQueue314 pq = new PriorityQueue314();
    	for(int i = 0; i < freq.length; i++) {
    		if(freq[i] != 0) {
    			TreeNode add = new TreeNode(i, freq[i]);
    			pq.add(add);
    		}
    	}
    	while(pq.size() > 1) {
    		TreeNode first = pq.remove();
    		TreeNode second = pq.remove();
    		TreeNode newParent = new TreeNode(first, -1, second);
    		pq.add(newParent);
    	}
    	tree = pq.remove();
    	encoding = new HuffmanTree(tree);
    }

    /**
	 * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
    	compressionBits = 0;
    	if(!force) {
    		if(preprocessCompress(in, headerType) < 0) {
    			myViewer.showMessage("More bits for compression than actual file");
    			return compressionBits;
    		}
    	}
    	//Writing out the header and magic number into compressed file
    	BitOutputStream bitsWritten =  new BitOutputStream(out);
    	bitsWritten.writeBits(BITS_PER_INT, MAGIC_NUMBER);
    	compressionBits += BITS_PER_INT * 2;
    	createHeader(bitsWritten);
    	
    	//Creating a table of all paths in the Huffman tree (increases efficiency)
    	codes = new String[ALPH_SIZE + 1];
    	addCode(tree, "");
    	
    	//Writing the Huffman code for each chunk in the file
    	boolean canRead = true;
    	BitInputStream bitsIn =  new BitInputStream(in);
        while(canRead) {
        	int currBit = bitsIn.readBits(BITS_PER_WORD);
        	if(currBit == -1) {
        		canRead = false;
        	}
        	else {
        		writePath(bitsWritten, currBit);
        	}
        }
        //Adding the pseudo value to the compressed file
        writePath(bitsWritten, PSEUDO_EOF);
        bitsIn.close();
        bitsWritten.close();
        return compressionBits;
    }
    
    /*
     * A helper method that checks if the current node being traversed is 
     * a leaf and adds a 0 or 1 to the encoded bit based on the value
     * @param curr = the current node to check
     * @param encode = the String of bits that is currently being encoded
     */
    private void addCode(TreeNode curr, String encode) {
    	if(curr.isLeaf()) {
			codes[curr.getValue()] = encode;
    	}
    	else if(curr != null){
    		String newVal = encode + "0";
    		addCode(curr.getLeft(), newVal);
    		String newValTwo = encode + "1";
    		addCode(curr.getRight(), newValTwo);
    	}
    }
    
    /*
     * A helper method that traverses through the tree in pre-order to write
     * bits in the createHeader method for tree count files
     * @param out = the bit output stream that is being written onto
     * @param curr = the current tree node being checked
     */
    private void treeTraversal(BitOutputStream out, TreeNode curr) {
    	if(curr.isLeaf()) {
    		out.writeBits(1, 1);
    		compressionBits++;
    		out.writeBits(BITS_PER_WORD + 1, curr.getValue());
    		compressionBits += BITS_PER_WORD + 1;
    	}
    	else {
    		out.writeBits(1, 0);
    		compressionBits++;
    		treeTraversal(out, curr.getLeft());
        	treeTraversal(out, curr.getRight());
    	}
    }
    
    /*
     * A helper method that creates the header for either a store count
     * or a store tree based on the header variable. Prints out the values, frequencies,
     * and or size of the data
     * @param bitsWritten = a bit output stream to write onto
     */
    private void createHeader(BitOutputStream bitsWritten) {
    	if(headerType == STORE_COUNTS) {
        	bitsWritten.writeBits(BITS_PER_INT, STORE_COUNTS);
    		for(int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {  	    
        		bitsWritten.writeBits(BITS_PER_INT, freq[k]);
        		showString("Value:" + k + "  Frequency: " + freq[k]);
        	    compressionBits += BITS_PER_INT;
        	}
    	}
    	else {
        	bitsWritten.writeBits(BITS_PER_INT, STORE_TREE);
    		int size = (encoding.numOfLeaf(encoding.getTree()) * (BITS_PER_WORD + 1))
    				+ encoding.size(encoding.getTree());
    		bitsWritten.writeBits(BITS_PER_INT, size);
    		compressionBits += size;
    		treeTraversal(bitsWritten, tree);
    	}
    }
    
    /*
     * A helper method that writes a path of bits based on the encoded
     * bitString
     * @param out = the BitOutputStream being written onto
     * @param val = the value that is being written
     */
    private void writePath(BitOutputStream out, int val) {
    	String encode = codes[val];
		char[] bits = encode.toCharArray();
		for(int i = 0; i < bits.length; i++) {
			out.writeBits(1, bits[i]);
			compressionBits++;
		}
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
    	BitInputStream bitsRead  = new BitInputStream(in);
    	int magic = bitsRead.readBits(BITS_PER_INT);
    	if(magic != MAGIC_NUMBER) {
    		myViewer.showError("Error reading compressed file. Magic number is incorrect");
    		return -1;
    	}
    	//Reading the number of bits for the header
    	int header = bitsRead.readBits(BITS_PER_INT); 
    	// +1 for pseudo EOF
    	freq = new int[ALPH_SIZE + 1];
    	//Creating a frequency list based on the counts of the alphabet given
    	if(header == STORE_COUNTS) {
    		//Create frequency count list
    		for(int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
    			freq[i] = bitsRead.readBits(BITS_PER_INT);
    			showString("Value:" + i + "  Frequency: " + freq[i]);
    		}
    		//Create tree
            freq[ALPH_SIZE] = 1;
        	createTree();
    	}
    	//Creating a Huffman tree with the STORE_TREE format
    	else if(header == STORE_TREE){
    		int size = bitsRead.readBits(BITS_PER_INT);
    		showString("Size of tree: " + size);
    		tree = findTree(bitsRead);
    	}
    	else {
    		myViewer.showError("Error reading compressed file. Header type does not exist");
    		return -1;
    	}
    	//Writing out each uncompressed value based on Huffman encoding 
    	//using the Huffman tree
    	BitOutputStream bitsOut = new BitOutputStream(out);
    	int ret = writeUncompressedFile(bitsOut, bitsRead);
    	bitsRead.close();
    	bitsOut.close();
    	return ret;
    }
    
    /*
     * Create a Huffman Tree using the STORE_TREE format
     * Uses pre-order traversal to find every bit until every node 
     * without a value has two children
     * @param in = a BitInputStream to read encoded bits from
     */
    private TreeNode findTree(BitInputStream in) throws IOException {
    	int bit = in.readBits(1);
    	//Node does not contain a value - need to find children
    	if(bit == 0) {
    		TreeNode val = new TreeNode(-1, 0);
    		val.setLeft(findTree(in));
    		val.setRight(findTree(in));
    		return val;
    	}
    	//Base case - node has value so node is added as a leaf
    	else if(bit == 1) {
    		int leafNode = in.readBits(BITS_PER_WORD + 1);
        	TreeNode val = new TreeNode(leafNode, 0);
        	return val;
    	}
    	else {
    		throw new IOException("Error reading compressed file. \n" +
                    "unexpected end of input.");
    	}
    }

    /*
     * A helper method that loops through the bits of an input stream
     * to turn the compressed bits into their original form by finding the tree
     * path and corresponding bits
     * @param out = the BitOutputStream being written on
     * @param in = the BitInputStream being read
     */
    private int writeUncompressedFile(BitOutputStream out, BitInputStream in) throws IOException {
    	int ret = 0;
    	TreeNode curr = tree;
    	boolean canRead = true;
    	while(canRead) {
    		int path = in.readBits(1);
    		if(path == 0) {
    			curr = curr.getLeft();
    		}
    		else if(path == 1) {
    			curr = curr.getRight();
    		}
    		else if(path == -1) {
    	    	canRead = false;
    	    	throw new IOException("Error reading compressed file. \n" +
                        "unexpected end of input. No PSEUDO_EOF value.");
    	    }
    		if(curr.getValue() != -1) {
    			if(curr.getValue() == PSEUDO_EOF) {
    	    		canRead = false;
    	    	}
    	    	else {
    	    		out.writeBits(BITS_PER_WORD, curr.getValue());
    	    		ret += BITS_PER_WORD;
    	    		curr = tree;
    	    	}
    		}
    	}
    	return ret;
    }
    
    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s){
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
    

    
}



