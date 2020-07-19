package rpvt.util;

import java.util.Iterator;

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
public class RBTreeIterator<A extends Comparable<A>> implements Iterator<A>
{
	final RBTree<A> tree;
	
	RBTreeNode<A> first = null;
	
	RBTreeNode<A> curr = null;
	RBTreeNode<A> next = null;
	
	A maxvalue;
	
	public RBTreeIterator(RBTree<A> tree)
	{
		this.tree = tree;
		this.fullreset();
	}
	
	public RBTreeIterator(RBTree<A> tree, RBTreeNode<A> first, A max)
	{
		this.tree = tree;
		this.first = first;
		this.maxvalue = max;
		
		this.reset();
	}
	
	@Override
	public boolean hasNext() 
	{
		return next != null && (maxvalue == null || next.value.compareTo(maxvalue) <= 0);
	}
	@Override
	public A next() 
	{
		if(this.hasNext())
		{
			
			final A value = next.value;
			curr = next;
			next = next.next;
			return value;
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public void remove() 
	{
		tree.remove(curr);
		curr = null;
	}
	
	public void fullreset()
	{
		this.first = tree.first;
		this.maxvalue = null;
		
		this.reset();
	}
	
	public void reset()
	{
		this.next = this.first;
	}
	
	public void setRange(A min, A max)
	{
		this.first = this.tree.gethigherNode(min);
		this.maxvalue = max;
		
		this.reset();
	}
	
	public void setRange(RBTreeNode<A> min, A max)
	{
		first = min;
		this.maxvalue = max;
		
		this.reset();
	}
}
