package rpvt.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

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
public class Bipartite_Matching_Solver<A,B>
{	
	final static int INTEGER_MIN_HALF = Integer.MIN_VALUE/2 + 1;
	
	// Stored Objects
	Stack<BMS_Edge> 	stored_edges 	= new Stack<BMS_Edge>();
	Stack<BMS_Node> 	stored_nodes 	= new Stack<BMS_Node>();
	
	BMS_Node start;
	BMS_Node end;
	
	ArrayList<BMS_Node<A>> leftNodes	= new ArrayList<BMS_Node<A>>();
	ArrayList<BMS_Node<B>> rightNodes	= new ArrayList<BMS_Node<B>>();
	
	// Utilities
	ArrayDeque<BMS_Node> nodeQueue = new ArrayDeque<BMS_Node>();
	
	int matches;
	
	public Bipartite_Matching_Solver()
	{
		start = new BMS_Node(this);
		end   = new BMS_Node(this);
		
		start.represents = "start";
		end.represents = "end";
	}
	
	protected BMS_Node requestNode()
	{
		if(stored_nodes.size() == 0)
		{
			return new BMS_Node(this);
		}
		
		return stored_nodes.pop();
	}
	
	/**
	 * Specialised fast algorithm
	 */
	private void findAugumentingPath_PMMP(BMS_Node firstNode)
	{
//		System.out.println("Find augmenting Path");
		
		// Initialise
		start.sp_color	= ShortestPathColor.BLACK;
		start.sp_edge 	= null;
		start.sp_len 	= 0;
		
		end.sp_color	= ShortestPathColor.WHITE;
		end.sp_edge 	= null;
		end.sp_len 		= INTEGER_MIN_HALF; 
		
		for(BMS_Node n : this.leftNodes)
		{
			n.sp_color = ShortestPathColor.WHITE;
			n.sp_edge = null;
			n.sp_len  = INTEGER_MIN_HALF; 
		}
		
		for(BMS_Node n : this.rightNodes)
		{
			n.sp_color = ShortestPathColor.WHITE;
			n.sp_edge = null;
			n.sp_len  = INTEGER_MIN_HALF; 
		}
		
		firstNode.sp_color = ShortestPathColor.GRAY;
		firstNode.sp_len   = 1;
		
		// Assumes every edge has lenght 1
		this.nodeQueue.clear();
		this.nodeQueue.add(firstNode);
		
		while(!nodeQueue.isEmpty())
		{
			BMS_Node node = nodeQueue.poll();
			node.sp_color = ShortestPathColor.BLACK;
//			System.out.println("  Exploring node: "+node.represents);
			
			for(BMS_Edge e : (ArrayList<BMS_Edge>)node.edges)
			{
//				System.out.println("    Exploring edge: "+e.from.represents+" => "+e.to.represents + " flow: "+e.flow);
				final BMS_Node other;
				final boolean canIncrease;
				
				if(e.from == node)
				{ 
					other = e.to;
					canIncrease = e.flow == 0;
				}
				else
				{
					other = e.from;
					canIncrease = e.flow == 1;
				}
				
//				System.out.println("      CanIncrease: "+canIncrease+" other: "+other.represents +" color: "+other.sp_color);
				
				if(canIncrease && other.sp_color == ShortestPathColor.WHITE)
				{
//					System.out.println("      Adding:");
					
					other.sp_color = ShortestPathColor.GRAY;
					other.sp_edge = e;
					other.sp_len = node.sp_len + 1;
				
					if(other == end)
					{ // Found a possible matching
						this.nodeQueue.clear();
					}
					else
					{
						this.nodeQueue.add(other);
					}
				}
			}
		}
	}
	
	protected BMS_Edge requestEdge()
	{
		if(stored_edges.size() == 0)
		{
			return new BMS_Edge(this);
		}
		
		return stored_edges.pop();
	}
	
	public BMS_Node<A> makeLeftNode(A rep)
	{	
		return this.makeLeftNode(rep, 1);
	}
	
	public BMS_Node<A> makeLeftNode(A rep, int priority)
	{
		BMS_Node<A> node = this.requestNode();
		
		node.weight 	= priority;
		node.represents = rep;
		
		this.leftNodes.add(node);
		
		// Make edge to the source;
		start.makeEdgeTo(node);
		
		return node;
	}
	
	public BMS_Node<B> makeRightNode(B rep)
	{
		BMS_Node<B> node 	= this.requestNode();
		node.weight 		= 0;
		node.represents 	= rep;
		
		rightNodes.add(node);
		
		// Make edge to the sink
		node.makeEdgeTo(end);
		return node;	
	}
	
	public void findPrioritisedMaximumMatching()
	{
		for(BMS_Node n : this.leftNodes)
		{
			for(BMS_Edge e : ((Iterable<BMS_Edge>)n.edges))
			{
				e.flow = 0;
			}
		}
		
		for(BMS_Edge e : ((Iterable<BMS_Edge>)end.edges))
		{
			e.flow = 0;
		}
		
		// Sort leftNodes on priority.
		bubblesort(this.leftNodes);
		
		// Find flow:
		int nodesLeft = leftNodes.size();
		
		final int MaxMatches = rightNodes.size();
		matches = 0;
		for(BMS_Node n : leftNodes)
		{
//			System.out.println("Try planning node: priority: "+n.weight+" object: "+n.represents);
			
//			for(BMS_Edge e : ((Iterable<BMS_Edge>)n.edges))
//			{
//				System.out.println("Edge: "+e.from.represents+" => "+e.to.represents+" flow: "+e.flow);
//			}
			
			this.findAugumentingPath_PMMP(n);
			
			// Check or there is a longest path
			if(end.sp_edge != null)
			{ // The left end could be planned
				// Find route
				
//				System.out.println("Succesfully planned node: "+n.represents+" matches: "+matches);
				BMS_Edge route = end.sp_edge;
				while(route != null)
				{
					final BMS_Node prev;
					if(route.flow == 1)
					{
						route.flow = 0;
						prev = route.to;
					}
					else if(route.flow == 0)
					{
						route.flow = 1;
						prev = route.from;
					}
					else
					{
						throw new Error("Flow should be 0 or 1 for this algorithm!");
					}
					
					route = prev.sp_edge;
				}
				matches++;
			}
			
			if(matches >= MaxMatches)
			{ // Everything is planned already
//				System.out.println("Max matches reached: "+matches+"/"+MaxMatches);
				break;
			}
		}
	}
	
	public<C> void bubblesort(ArrayList<BMS_Node<C>> list)
	{
		// TODO: Make this mergeSort;
		int maxIndex = list.size();
		boolean stop;
		while(maxIndex > 1)
		{
			stop = true;
			for(int i = 1 ; i < maxIndex ; i++)
			{
				final BMS_Node n1 = list.get(i-1);
				final BMS_Node n2 = list.get(i);
				
				if(n1.weight < n2.weight)
				{
					list.set(i-1, n2);
					list.set(i, n1);
					stop = false;
				}
			}
			
			if(stop)
			{ // No more changes
				maxIndex = 0;
			}
		}
	}
	
	/**
	 * Resets the solver
	 */
	public void reset()
	{
		for(BMS_Node<A> node : leftNodes)
		{
			node.delete();
		}
		
		for(BMS_Node<B> node : rightNodes)
		{
			node.delete();
		}
		
		leftNodes.clear();
		rightNodes.clear();
	}
	
	public BMS_Matching<A,B>[] getMatching()
	{
		BMS_Matching<A,B>[] matches = new BMS_Matching[this.matches];
		int i = 0;
		
		for(BMS_Node<A> n : this.leftNodes)
		{
			for(BMS_Edge e : (ArrayList<BMS_Edge>)n.edges)
			{
				if(e.from == n && e.flow == 1)
				{
					BMS_Node<B> other = e.to;
					matches[i] = new BMS_Matching<A,B>(n.represents, other.represents);
					i++;
				}
			}
		}
		
		return matches;
	}
	
	public class BMS_NodeComparator implements Comparator<BMS_Node>
	{
		@Override
		public int compare(BMS_Node o1, BMS_Node o2) 
		{
			return o1.sp_len - o2.sp_len;
		}
		
	}

	public Iterable<BMS_Node<A>> getLeftSide() 
	{
		return this.leftNodes;
	}
	
	public Iterable<BMS_Node<B>> getRightSide() 
	{
		return this.rightNodes;
	}
}
