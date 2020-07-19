package rpvt.util;

/*
 * This program has been developed by Rene van Twist for his master's thesis (under supervision of Han Hoogeveen) within the master program Computing Science at Utrecht University  
 * ©Copyright Utrecht University (Department of Information and Computing Sciences)
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
public class RBTreeNode<A extends Comparable<A>> implements Comparable<RBTreeNode<A>>
{
	final RBTree<A> tree;
	
	RBTreeNode<A> next;
	RBTreeNode<A> prev;
	
	RBTreeNode<A> lower;
	RBTreeNode<A> higher;
	
	RBTreeNode<A> parent;
	
	A value;
	
	boolean red;
	
	int subtreeSize;
	
	public RBTreeNode(final RBTree<A> tree)
	{
		this.tree = tree;
	}
	
	
	public RBTreeNode<A> getNext() {
		return next;
	}

	public RBTreeNode<A> getPrev() {
		return prev;
	}

	public RBTreeNode<A> getLower() {
		return lower;
	}

	public RBTreeNode<A> getHigher() {
		return higher;
	}

	public RBTreeNode<A> getParent() {
		return parent;
	}

	public A getValue() {
		return value;
	}

	public boolean isRed() {
		return red;
	}


	@Override
	public int compareTo(RBTreeNode<A> other) 
	{
		return this.value.compareTo(other.value);
	}
	
	public int treeDepth()
	{
		return Math.max((lower == null ? 0 : lower.treeDepth()), (higher == null ? 0 : higher.treeDepth())) + 1;
	}
	
	public String toString()
	{
		final char r = (this.red ? 'R' : 'B');
		
		return "{"+lower+" "+value+r+"("+this.subtreeSize+") "+higher+"}";
	}


	public int checkTree(RBTreeNode<A> head) 
	{
		if(head != this.parent)
		{
			throw new Error(" !!! Parent of node: "+value+" is incorrect is "+this.parent+" must be "+head);
		}
		
		if(this.red && this.parent.red)
		{
			throw new Error("2 red parents in a row in subtree: "+this.parent);
		}
		
		final int leftBld;
		final int rightBld;
		
		if(lower != null)
			leftBld = this.lower.checkTree(this);
		else
			leftBld = 1;
		
		if(higher != null)
			rightBld = this.higher.checkTree(this);
		else
			rightBld = 1;
		
		if(leftBld != rightBld)
		{
			throw new Error(" Blackdept ("+leftBld+"|"+rightBld+") arent the same in: "+this);
		}
		
		if(this.red)
		{
			return leftBld;
		}
		else
		{
			return leftBld + 1;
		}
	}
}
