package prm.ls.model1;

import java.util.List;

import prm.problemdef.LocationAlias;
import rpvt.util.SingleThreadResources;

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
public abstract class Node 
{
	Node next;
	Node prev;
	NodeContainer container;
	
	// Data
	int cumTime;
	int cumCap;
	
	public Node()
	{
		
	}
	
	public void addAfterThis(Node n)
	{
		if(n.getContainer() != null)
		{
			throw new Error("Node is already linked!");
		}
		
		final Node oldNext = this.next;
		this.next = n;
		n.prev = this;
		n.next = oldNext;
		
		n.setContainer(this.container);
		
		if(oldNext == null)
		{
			container.setLast(n);
		}
		else
		{
			oldNext.prev = n;
		}
		
		this.updateNodes();
	}
	
	private void updateNodes() 
	{
		List<Node> list = SingleThreadResources.updateList;
		
		// Debug.
		if(list.size() > 0)
		{
			throw new Error("List is not cleared!");
		}
			
	}

	public NodeContainer getContainer()
	{
		return this.container;
	}
	
	protected void setContainer(NodeContainer nc)
	{
		this.container = nc;
	}
	
	/**
	 * Updates the nodes and adds nodes to the list to be updated. 
	 * Returns true if feasible, false if infeasible.
	 * @param updateList
	 * @return
	 */
	protected abstract boolean updateNode(List<Node> updateList);
	
	public abstract LocationAlias getStartLocation();
	public abstract LocationAlias getEndLocation();
}
