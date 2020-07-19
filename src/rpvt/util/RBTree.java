package rpvt.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.SortedSet;

import prm.ls.resources.ParalelResourceNode;

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
@SuppressWarnings("unchecked")
public class RBTree<A extends Comparable<A>> implements SortedSet<A>
{
	
	RBTreeNode<A> root;
	RBTreeNode<A> first;
	RBTreeNode<A> last;
	int size = 0;
	
	static int reusableSize = 20;
	RBTreeNode<A>[] reusableObjects = new RBTreeNode[reusableSize];
	int reusableObjects_c = 0;
	
	
	private RBTreeNode<A> satelite = new RBTreeNode<A>(this);
	private RBTreeIterator<A> iterator = new RBTreeIterator<A>(this);
	private RBTreeSubSet<A> subset = new RBTreeSubSet<A>(this,null,null);
	
	protected void destroyNode(RBTreeNode<A> node)
	{
		node.value = null;
		if(reusableObjects_c < reusableSize)
		{
			reusableObjects[reusableObjects_c] = node;
			reusableObjects_c++;
		}
	}
	
	/**
	 * Returns the ith node in this instance
	 * @param i
	 * @return
	 */
	public A get(int i)
	{
		RBTreeNode<A> node = this.getNode(i);
		
		return (node == null ? null : node.value);
	}
	
	
	/**
	 * Returns the ith node in this instance
	 * @param i
	 * @return
	 */
	public RBTreeNode<A> getNode(int i)
	{
		if(i >= this.size || i < 0)
			throw new IndexOutOfBoundsException("IndexOutOfBoundsException in RBTree: index "+i+" not in range of [0,"+this.size+")");
		
		RBTreeNode<A> node = this.root;
		int leftOffset = 0;
		
		while(node != null)
		{
			final int leftSize = (node.lower == null ? 0 : node.lower.subtreeSize);
			final int nodeIndex = leftOffset + leftSize;
			
			if(nodeIndex == i)
				return node;
			else if(i < nodeIndex)
				node = node.lower;
			else
			{
				leftOffset = leftSize + 1;
				node = node.higher;
			}
		}
		
		return null;
	}
	
	public RBTreeNode<A> findNode(Comparable<A> value)
	{
		RBTreeNode<A> curr = root;
		
		while(curr != null)
		{
			//System.out.println("Search node: "+curr+" for "+item);
			
			final int compare = value.compareTo(curr.value);
			if(compare == 0)
			{
				return curr;
			}
			else if(compare < 0)
			{
				curr = curr.lower;
			}
			else
			{
				curr = curr.higher;
			}
		}
		
		return null;
	}
	
	public RBTreeNode<A> getlowerNode(Comparable<A> value)
	{
		RBTreeNode<A> curr = root;
		RBTreeNode<A> lowest = null;
		
		while(curr != null)
		{
			//System.out.println("Search node: "+curr+" for "+item);
			
			final int compare = value.compareTo(curr.value);
			if(compare == 0)
			{
				return curr;
			}
			else if(compare < 0)
			{
				curr = curr.lower;
			}
			else
			{
				lowest = curr;
				curr = curr.higher;
			}
		}
		
		return lowest;
	}
	
	public RBTreeNode<A> gethigherNode(Comparable<A> value)
	{
		RBTreeNode<A> curr = root;
		RBTreeNode<A> highest = null;
		
		while(curr != null)
		{
			//System.out.println("Search node: "+curr+" for "+item);
			
			final int compare = value.compareTo(curr.value);
			if(compare == 0)
			{
				return curr;
			}
			else if(compare < 0)
			{
				highest = curr;
				curr = curr.lower;
			}
			else
			{
				curr = curr.higher;
			}
		}
		
		return highest;
	}
	
	public RBTreeNode<A> getNode(Comparable<A> item)
	{
		RBTreeNode<A> curr = root;
		
		while(curr != null)
		{
			//System.out.println("Search node: "+curr+" for "+item);
			
			final int compare = item.compareTo(curr.value);
			if(compare == 0)
			{
				return curr;
			}
			else if(compare < 0)
			{
				curr = curr.lower;
			}
			else
			{
				curr = curr.higher;
			}
		}
		
		return curr;
	}
	
	@Override
	public boolean add(A item)
	{
		//System.out.println("\ninsert: "+item+" in "+this);
		
		RBTreeNode<A> y = null;
		RBTreeNode<A> x = root;
		
		// Make node
		RBTreeNode<A> insert = this.makeNode();
		insert.value = item;
		
		int compare = 0;
		
		while(x != null)
		{
			compare = insert.compareTo(x);
			if(compare == 0)
			{
				x.value = item;
				this.destroyNode(insert);
				
				//System.out.println("end insert: "+this);
				//this.testTreeConsistancy();
				return false;
			}
			else if(compare < 0)
			{
				y = x;
				x = x.lower;
			}
			else
			{
				y = x;
				x = x.higher;
			}
		}
		
		insert.parent = y;
		insert.lower = null;
		insert.higher = null;
		insert.subtreeSize = 1;
		
		// Update subtreeSize
		RBTreeNode u = insert.parent;
		while(u != null)
		{
			u.subtreeSize++;
			u = u.parent;
		}
		
		if(y == null)
		{
			insert.next = null;
			insert.prev = null;
			this.root = insert;
			this.first = insert;
			this.last = insert;
			insert.red = false;
		}
		else if(compare < 0)
		{
			y.lower = insert;
			insert.next = y;
			insert.prev = y.prev;
			
			if(insert.prev == null)
				this.first = insert;
			else
				insert.prev.next = insert;
			y.prev = insert;
			
			
				
			insert.red = true;
			
			//System.out.println("Before refitting: "+this);
			//this.testTreeConsistancy();
			
			if(y.red)
				rebalanceInsert(insert);
		}
		else
		{
			y.higher = insert;
			insert.prev = y;
			insert.next = y.next;
			
			if(insert.next == null)
				this.last = insert;
			else
				insert.next.prev = insert;
			
			y.next = insert;
			
			insert.red = true;
			
			//System.out.println("Before refitting: "+this);
			//this.testTreeConsistancy();
			
			if(y.red)
				rebalanceInsert(insert);
		}
		
		size++;
		//this.testTreeConsistancy();
		return true;
	}
	
	private void leftRotate(RBTreeNode<A> n) 
	{
		//System.out.println("leftrotate: "+n);
		
		/*
		 * 		 [n]                             [2]
		 * 		/   \          |\               /   \
		 * 	 [1]     [2]    ===  \            [n]   [6]
		 *           / \    ===  /            / \
		 *         [5] [6]     |/           [1] [5]
		 * 
		 * 
		 */
		
		
		final RBTreeNode<A> topParent = n.parent;
		final RBTreeNode<A> newTop = n.higher;
		final RBTreeNode<A> newRight = newTop.lower;
		
		// Update subtreeSizes
		newTop.subtreeSize = n.subtreeSize;
		n.subtreeSize = 1 + (newRight == null ? 0 : newRight.subtreeSize) + 
							(n.lower == null ? 0 : n.lower.subtreeSize);
		
		n.higher = newRight;
		
		if(newRight != null)
			newRight.parent = n;
		
		newTop.lower = n;
		n.parent = newTop;
		
		if(topParent == null)
		{
			this.root = newTop;
			newTop.parent = null;
		}
		else if(topParent.lower == n)
		{
			topParent.lower = newTop;
			newTop.parent = topParent;
		}
		else
		{
			topParent.higher = newTop;
			newTop.parent = topParent;
		}
		
//		this.testTreeConsistancy();
		//System.out.println("result: "+this);
	}

	protected RBTreeNode<A> makeNode()
	{
		if(reusableObjects_c > 0)
		{
			reusableObjects_c--;
			return reusableObjects[reusableObjects_c];
		}
		
		return new RBTreeNode<A>(this);
	}
	
	private void rebalanceDelete(final RBTreeNode<A> node) 
	{
//		System.out.println("Rebalance Delete: "+this);
//		System.out.println("              on: "+node);
//		System.out.println("          parent: "+(node.parent != null ? node.parent : null));
		
		RBTreeNode<A> x = node;
		
		while(x != this.root && !x.red)
		{
			if(x == x.parent.lower)
			{
				RBTreeNode<A> w = x.parent.higher;
				if(w.red)
				{
					w.red = false;
					x.parent.red = true;
					this.leftRotate(x.parent);
					w = x.parent.higher;
				}
				if((w.lower == null || !w.lower.red) && (w.higher == null || !w.higher.red))
				{
					w.red = true;
					x = x.parent;
				}
				else
				{
					if(w.higher == null || !w.higher.red)
					{
						w.lower.red = false;
						w.red = true;
						this.rightRotate(w);
						w = x.parent.higher;
					}
					w.red = x.parent.red;
					x.parent.red = false;
					w.higher.red = false;
					this.leftRotate(x.parent);
					x = this.root;
				}
			}
			else
			{
				RBTreeNode<A> w = x.parent.lower;
				if(w.red)
				{
					w.red = false;
					x.parent.red = true;
					this.rightRotate(x.parent);
					w = x.parent.lower;
				}
				if((w.lower == null || !w.lower.red) && (w.higher == null || !w.higher.red))
				{
					w.red = true;
					x = x.parent;
				}
				else
				{
					if(w.lower == null || !w.lower.red)
					{
						w.higher.red = false;
						w.red = true;
						this.leftRotate(w);
						w = x.parent.lower;
					}
					w.red = x.parent.red;
					x.parent.red = false;
					w.lower.red = false;
					this.rightRotate(x.parent);
					x = this.root;
				}
			}
		}
		x.red = false;
	}

	private void rebalanceInsert(RBTreeNode<A>  node) 
	{
		RBTreeNode<A> z = node;
		
		while(z.parent != null && z.parent.red)
		{
			final RBTreeNode<A> zp = z.parent;
			final RBTreeNode<A> zpp = zp.parent;
			
			if(zp == zpp.lower)
			{
				RBTreeNode<A> y = zpp.higher;
				if(y != null && y.red)
				{
					zp.red = false;
					y.red = false;
					zpp.red = true;
					z = zpp;
				}
				else
				{
					if(z == zp.higher)
					{
						z = zp;
						this.leftRotate(z);
						
						final RBTreeNode<A> zp2 = z.parent;
						final RBTreeNode<A> zpp2 = zp2.parent;
						zp2.red = false;
						zpp2.red = true;
						this.rightRotate(zpp2);
					}
					else
					{
						zp.red = false;
						zpp.red = true;
						this.rightRotate(zpp);
					}
				}
			}
			else
			{
				RBTreeNode<A> y = zpp.lower;
				if(y != null && y.red)
				{
					zp.red = false;
					y.red = false;
					zpp.red = true;
					z = zpp;
				}
				else
				{
					if(z == zp.lower)
					{
						z = zp;
						this.rightRotate(z);
						
						final RBTreeNode<A> zp2 = z.parent;
						final RBTreeNode<A> zpp2 = zp2.parent;
						zp2.red = false;
						zpp2.red = true;
						this.leftRotate(zpp2);
					}
					else
					{
						zp.red = false;
						zpp.red = true;
						this.leftRotate(zpp);
					}
				}
			}
	
		}
		
		if(this.root.red)
		{
			this.root.red = false;
		}
	}
	
	public boolean remove(A item)
	{
		RBTreeNode<A> node = getNode(item);
		
		if(node == null)
		{
//			System.out.println("No node? ");
			return false;
		}
		else
		{
			remove(node);
			return true;
		}
	}
	
	/**
	 * Removes the first element of the list and returns it.
	 * @return
	 */
	public A removeFirst()
	{
		RBTreeNode<A> node = this.first;
		
		if(node == null)
		{
			return null;
		}
		
		this.remove(node);
		return node.value;
	}
	
	/**
	 * Removes the last element of the list and returns it.
	 * @return
	 */
	public A removeLast()
	{
		RBTreeNode<A> node = this.last;
		
		if(node == null)
		{
			return null;
		}
		
		this.remove(node);
		return node.value;
	}

	public void remove(RBTreeNode<A> node)
	{
		
		if(node.prev != null)
			node.prev.next = node.next;
		else
			this.first = node.next;
		if(node.next != null)
			node.next.prev = node.prev;
		else
			this.last = node.prev;
		
		
		if(node.tree != this)
		{
			throw new Error("This node isn't from this tree!");
		}
		
		RBTreeNode<A> x;
		final RBTreeNode<A> y;
		
		if(node.lower == null || node.higher == null)
		{
			y = node;
		}
		else
		{
			y = tree_successor(node);
		}
		
		//System.out.println("y = "+y);
		
		if(y.lower != null)
			x = y.lower;
		else
			x = y.higher;
		
		if(x == null)
		{
			x = this.satelite;
			x.red = false;
			x.parent = y;
			y.higher = x;
		}
			
		x.parent = y.parent;
		
		
		if(y.parent == null)
			this.root = x;
		else if(y == y.parent.lower)
			y.parent.lower = x;
		else
			y.parent.higher = x;
		
		
		if(y != node)
		{
			node.value = y.value;
			node.prev = y.prev;
			node.next = y.next;
			
			if(y.prev != null)
				y.prev.next = node;
			else
				this.first = node;
			if(y.next != null)
				y.next.prev = node;
			else
				this.last = node;
		}
		
		// Update subtreeSizes()
		{
			RBTreeNode u = y.parent;
			while(u != null)
			{
				u.subtreeSize--;
				u = u.parent;
			}
		}
		
		if(! y.red)
		{
//			System.out.println("Rebalance");
			this.rebalanceDelete(x);
		}
		else
		{
//			System.out.println("no Rebalance");
		}
		
		//this.testTreeConsistancy();
		
		if(this.satelite == x)
		{
			if(this.satelite.parent == null)
			{
				this.root = null;
			}
			else if(satelite.parent.higher == satelite)
			{
				satelite.parent.higher = null;
			}
			else
			{
				satelite.parent.lower = null;
			}
		}
		
		this.destroyNode(y);
		size--;
		//this.testTreeConsistancy();
	}

	private void rightRotate(RBTreeNode<A> n) 
	{
		//System.out.println("rightrotate: "+n);
		
		final RBTreeNode<A> topParent = n.parent;
		final RBTreeNode<A> newTop = n.lower;
		final RBTreeNode<A> newLeft = newTop.higher;
		
		/*
		 * 		 [n]                             [1]
		 * 		/   \          |\               /   \
		 * 	 [1]     [2]    ===  \            [3]   [n]
		 *   / \     / \    ===  /                  / \
		 * [3] [4]             |/                 [4] [2]
		 * 
		 * 
		 */
		
		// Update subtreeSizes
		newTop.subtreeSize = n.subtreeSize;
		n.subtreeSize = 1 + (newLeft == null ? 0 : newLeft.subtreeSize) + 
							(n.higher == null ? 0 : n.higher.subtreeSize);
		
		n.lower = newLeft;
		
		if(newLeft != null)
			newLeft.parent = n;
		
		newTop.higher = n;
		n.parent = newTop;
		
		if(topParent == null)
		{
			this.root = newTop;
			newTop.parent = null;
		}
		else if(topParent.lower == n)
		{
			topParent.lower = newTop;
			newTop.parent = topParent;
		}
		else
		{
			topParent.higher = newTop;
			newTop.parent = topParent;
		}
		
		//this.testTreeConsistancy();
		//System.out.println("end-rightrotate: "+this);
	}

	public void testTreeConsistancy()
	{
		if(true)
			throw new Error("Not testing");
		
		//System.out.println("Check Tree: ");
		
		if(this.root != null)
		{
			if(this.root.red)
			{
				throw new Error("Root is red!");
			}
			
			this.root.checkTree(null);
		}
		
		//System.out.println("Tree checked!");
		
	}
	
	public String toString()
	{
		return " "+root;
	}
	
	private RBTreeNode<A> tree_minimum(RBTreeNode<A> min) 
	{
		while(min.lower != null)
		{
			min = min.lower;
		}
		
		return min;
	}
	
	private RBTreeNode<A> tree_successor(RBTreeNode<A> node) 
	{
		
		
		if(node.higher != null)
		{
			return this.tree_minimum(node.higher);
		}
		
		RBTreeNode<A> x = node;
		RBTreeNode<A> y = node.parent;
		while(y != null && x == y.higher)
		{
			x = y;
			y = y.parent;
		}
		
		return y;
	}

	@Override
	public RBTreeIterator<A> iterator() 
	{
		this.iterator.fullreset();
		return this.iterator;
	}

	@Override
	public Comparator<? super A> comparator() 
	{
		return comp;
	}

	@Override
	public A first() 
	{
		return (this.first == null ? null : this.first.value);
	}

	@Override
	public RBTreeSubSet<A> headSet(A higher) 
	{
		subset.setMinMax(null, higher);
		return subset;
	}

	@Override
	public A last() 
	{
		return (this.last == null ? null : this.last.value );
	}

	@Override
	public RBTreeSubSet<A> subSet(A lower, A higher) 
	{
		subset.setMinMax(lower, higher);
		return subset;
	}

	@Override
	public RBTreeSubSet<A> tailSet(A lower) 
	{
		subset.setMinMax(lower, null);
		return subset;
	}

	@Override
	public boolean addAll(Collection<? extends A> c) 
	{
		boolean added = false;
		
		for(A o : c)
		{
			added = this.add(o) || added;
		}
		
		return added;
	}

	@Override
	public void clear() 
	{
		// Save objects
		while(first != null && this.reusableObjects_c < this.reusableSize)
		{
			final RBTreeNode<A> next = first.next; 
			this.destroyNode(first);
			first = next;
		}
		
		this.first = null;
		this.last = null;
		this.root = null;
		this.size = 0;
	}

	@Override
	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		throw new Error("Unfinished method!");
		//return false;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) 
	{
		// TODO Auto-generated method stub
		throw new Error("Unfinished method!");
		//return false;
	}

	@Override
	public boolean isEmpty() 
	{
		return this.size == 0;
	}

	@Override
	public boolean remove(Object arg0) 
	{
		return remove((A)arg0);
	}

	@Override
	public boolean removeAll(Collection<?> c) 
	{
		boolean removed = false;
		
		for(Object o : c)
		{
			removed = this.remove(o) || removed;
		}
		
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) 
	{
		// TODO Auto-generated method stub
		throw new Error("Unfinished method!");
		//return false;
	}

	@Override
	public int size() 
	{
		return this.size;
	}

	@Override
	public Object[] toArray() 
	{
		return this.toArray(new Object[this.size]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) 
	{
		if(a.length != this.size)
		{
			throw new Error("Size of array is incorrect, size must be "+this.size+" currently "+a.length);
		}
		
		RBTreeNode<A> curr = this.first;
		for(int i = 0 ; i < a.length ; i++)
		{
			a[i] = (T) curr.value;
			curr = curr.next;
		}
		
		return a;
		
//		return null;
	}

	public RBTreeNode<A> getFirstNode()
	{
		return this.first;
	}
	
	public RBTreeNode<A> getLastNode()
	{
		return this.last;
	}

	public A lower(Comparable<A> v) 
	{
		final RBTreeNode<A> node = this.getlowerNode(v);
		return (node == null ? null : node.value);
	}
	
	public A higher(Comparable<A> v) 
	{
		final RBTreeNode<A> node = this.gethigherNode(v);
		return (node == null ? null : node.value);
	}
	
	/**
	 * Searches the tree for an object equal to this one, returns the found object.
	 * @param v
	 * @return
	 */
	public A find(Comparable<A> v) 
	{
		final RBTreeNode<A> node = this.findNode(v);
		return (node == null ? null : node.value);
	}
	
	/**
	 * Return a random value in O(log(n)) time
	 * @return
	 */
	public A getRandomValue()
	{
		RBTreeNode<A> n = this.root;
		
		double rand = Math.random();
		double valueNode = 1.0 / this.size;
		
		//System.out.println("Rand: "+rand+" value Node: "+valueNode);
		
		double base = 0;
		
		while(n != null)
		{
			final double leftChance = base + (n.lower == null ? 0 : n.lower.subtreeSize * valueNode);			
			
			if(rand <= leftChance)
			{
				n = n.lower;
			}
			else
			{
				final double selfChance = leftChance + valueNode;
				if(rand <= leftChance + valueNode)
				{
					return n.value;
				}
				else
				{
					base = selfChance;
					n = n.higher;
				}
			}
			
		}
		
		return null;
	}
	
	public A getRandomValue(Random random)
	{
		RBTreeNode<A> n = this.root;
		
		double rand = random.nextDouble();
		double valueNode = 1.0 / this.size;
		
		//System.out.println("Rand: "+rand+" value Node: "+valueNode);
		
		double base = 0;
		
		while(n != null)
		{
			final double leftChance = base + (n.lower == null ? 0 : n.lower.subtreeSize * valueNode);			
			
			if(rand <= leftChance)
			{
				n = n.lower;
			}
			else
			{
				final double selfChance = leftChance + valueNode;
				if(rand <= leftChance + valueNode)
				{
					return n.value;
				}
				else
				{
					base = selfChance;
					n = n.higher;
				}
			}
			
		}
		
		return null;
	}
	
	public static Comparator comp = new Comparator()
	{
		@Override
		public int compare(Object o1, Object o2) 
		{
			return ((Comparable)o1).compareTo(o2);
		}
		
	};
}
