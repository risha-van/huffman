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

import java.util.ArrayList;

public class PriorityQueue314 {
    	private ArrayList<TreeNode> list;
    	
    	//Constructor of Priority Queue
    	public PriorityQueue314(){
    		list = new ArrayList<TreeNode>();
    	}
    	
    	//Returns the size of queue
    	public int size() {
    		return list.size();
    	}
    	
    	//Adds a node to the tree where position is based on the frequency 
    	//Breaks ties fairly based on when it was placed
    	public boolean add(TreeNode val) {
    		if(!list.contains(val)) {
    			int weight = val.getFrequency();
    			int index = 0; 
    			while(index < list.size() && weight < list.get(index).getFrequency()) {
    				index++;
    			}
    			list.add(index, val);
    			return true;
    		}
    		return false;
    	}
    	
    	//Removes the last node which is the first node put into the queue
    	public TreeNode remove() {
    		return list.remove(list.size() - 1);
    	}
    	
    	
    }