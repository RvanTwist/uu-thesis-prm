package rpvt.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

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
public class RBTreeSubSet<A extends Comparable<A>> implements SortedSet<A>
{
	final RBTree<A> tree;

	A min;
	A max;
	
	RBTreeNode<A> first;
	RBTreeNode<A> last;
	
	public RBTreeSubSet(RBTree<A> t, A minV, A maxV)
	{
		this.tree = t;
		this.min = minV;
		this.max = maxV;
		
		this.refresh();
	}
	
	int size = 0;
	
	/**
	 * Refresh the view when there are changes done on the actual tree;
	 */
	public void refresh()
	{
		if(this.min == null)
		{
			this.first = tree.first;
		}
		else if(this.first == null || this.first.value != null)
		{
			this.first = this.tree.gethigherNode(min);
		}
		else
		{
			while(this.first.prev != null && this.first.prev.value.compareTo(min) >= 0)
			{
				this.first = this.first.prev;
			}
		}
		
		if(this.max == null)
		{
			this.last = tree.last;
		}
		else if(last == null || this.last.value != null)
		{
			this.last = this.tree.getlowerNode(max);
		}
		else
		{
			while(this.last.next != null && this.last.next.value.compareTo(max) <= 0)
			{
				this.last = this.last.next;
			}
		}
		
		size = -1;
	}
	
	public void setMinMax(A minV, A maxV)
	{
		//System.out.println("set min max: "+minV+" "+maxV);
		this.min = minV;
		this.max = maxV;
		
		this.first = null;
		this.last = null;
		
		this.refresh();
		//System.out.println("min max: "+this.first.value+" "+this.last.value);
	}
	
	@Override
	public Comparator<? super A> comparator() 
	{
		return tree.comparator();
	}

	@Override
	public A first() 
	{
		return (this.first == null ? null : this.first.value );
	}

	@Override
	public SortedSet<A> headSet(A maxV) 
	{
		return new RBTreeSubSet(tree, min, maxV);
	}

	@Override
	public A last() 
	{
		return (last == null ? null : last.value);
	}

	@Override
	public SortedSet<A> subSet(A minV, A maxV) 
	{
		return new RBTreeSubSet(tree, minV, maxV);
	}

	@Override
	public SortedSet<A> tailSet(A minV) 
	{
		return new RBTreeSubSet(tree, minV, max);
	}

	@Override
	public boolean add(A value) 
	{
		throw new Error("can't add in subset!");
	}

	@Override
	public boolean addAll(Collection<? extends A> arg0) 
	{
		throw new Error("can't add in subset!");
	}

	@Override
	public void clear() 
	{
		throw new Error("can't clear in subset!");
	}

	@Override
	public boolean contains(Object o) 
	{
		return this.tree.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) 
	{
		return this.tree.containsAll(c);
	}

	@Override
	public boolean isEmpty() 
	{
		return this.first == null;
	}

	@Override
	public RBTreeIterator<A> iterator() 
	{
		return new RBTreeIterator<A>(tree, first, max);
	}

	@Override
	public boolean remove(Object arg0) 
	{
		throw new Error("can't remove in subset!");
	}

	@Override
	public boolean removeAll(Collection<?> arg0) 
	{
		throw new Error("can't remove in subset!");
	}

	@Override
	public boolean retainAll(Collection<?> arg0) 
	{
		throw new Error("can't retain in subset!");
	}

	@Override
	public int size() 
	{
		if(size == -1)
		{
			// Calculate size O(log(n)) Time
			if(this.first == null)
			{
				this.size = 0;
			}
			else
			{
				int higherCount = (this.last.higher == null ? 0 : this.last.higher.subtreeSize);
				int lowerCount  = (this.first.lower == null ? 0 : this.first.lower.subtreeSize);;
				
				RBTreeNode<A> lowerC = this.first;
				RBTreeNode<A> higherC = this.last;
				
				RBTreeNode<A> lowerP = lowerC.parent;
				RBTreeNode<A> higherP = higherC.parent;
				
				while(lowerP != null)
				{
					if(lowerP.higher == lowerC)
					{
						lowerCount += (lowerP.lower == null ? 1 : 1 + lowerP.lower.subtreeSize);
					}
					
					lowerC = lowerP;
					lowerP = lowerC.parent;
				}
				
				while(higherP != null)
				{
					if(higherP.lower == higherC)
					{
						higherCount += (higherP.higher == null ? 1 : 1 + higherP.higher.subtreeSize);
					}
					
					higherC = higherP;
					higherP = higherC.parent;
				}
				
				this.size = this.tree.size() - lowerCount - higherCount;
			}
			
		}
		
		
		
		return this.size;
	}

	@Override
	public Object[] toArray() 
	{
		throw new Error("unimplemented");
	}

	@Override
	public <T> T[] toArray(T[] arg0) 
	{
		throw new Error("unimplemented");
	}
	
	public RBTreeNode<A> getFirstNode()
	{
		return this.first;
	}
	
	public RBTreeNode<A> getLastNode()
	{
		return this.last;
	}
	
	public void extendSetLower()
	{
		if(this.first == null)
		{
			//System.out.println("Find lower node: "+min);
			this.first = this.tree.getlowerNode(min);
			
//			if(this.first == null)
//			{
//				System.out.println("Can't find lower then "+min);
//				System.out.println("   "+this.tree);
//			}
		}
		else if(this.first.prev != null)
		{
			this.first = this.first.prev;
		}
	}

	public void extendSetHigher() 
	{
		RBTreeNode<A> node = this.tree.gethigherNode(this.max);
		if(node == null)
		{
			return;
		}
		else if(node.value.compareTo(this.max) == 0)
		{
			if(node.next != null)
			{
				this.max = node.next.value;
			}
		}
		else
		{
			this.max = node.value;
		}
	}

	public String printContentents() 
	{
		if(this.first == null)
		{
			return "{}";
		}
		
		String s = "{ ";
		
		boolean first = true;
		for(A a : this)
		{
			if(first)
			{
				first = false;
				s += a.toString();
			}
			else
			{
				s += ", "+a.toString();
			}
		}
		
		return s + " }";
	}
}
