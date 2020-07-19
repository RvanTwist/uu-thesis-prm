package rpvt.ls.altGraph;

import java.util.ArrayList;
import java.util.TreeMap;

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
/**
 * This object represents the alternative graph for solving the no-wait job shop problem.
 * 
 * @author rene
 */
public class AltGraph<A> 
{	
	public static int HALF_MIN_VALUE = Integer.MIN_VALUE/2 - 1;
	
	public Node end;
	
	public int[][] lpFW;
	
	// LongestPath
	int longestPath;
	
	public boolean lpUnfeasible = false;
	public ArrayList<Node<A>> nodes       = new ArrayList<Node<A>>();
	
	public ArrayList<Edge> normalEdges = new ArrayList<Edge>();
	public ArrayList<Edge> altEdges    = new ArrayList<Edge>();
	
	public Node start;
	
	int unsetEdges;
	
	int unsetFW;
	int unsetLP;
	
	// Dispensing nodes
	int id_dispenser;
	TreeMap<A,Node<A>> nodeMap;
	
	public AltGraph()
	{
		start = new Node<A>(0, null, this);
		end = new Node<A>(1, null, this);
		id_dispenser = 2;
		
		nodeMap = new TreeMap<A,Node<A>>();
	}
	
	public Node<A> addNode(A item)
	{
		Node<A> n = new Node(id_dispenser, item, this);
		id_dispenser++;
		
		if(nodeMap.put(item, n) != null)
		{
			throw new Error("There is aready one representative of the given object in the map!\n object: "+item);
		}
		
		this.nodes.add(n);
		
		return n;
	}
	
	public Edge<A> addEdge(Node<A> from, Node<A> to, int ll)
	{	
		// TODO: Its unchecked or the edge already exists or the nodes are null.
		Edge<A> e = new Edge<A>(this,from,to,ll,ll);
		e.setDir(EdgeState.EdgeStateRight, -1);
		this.normalEdges.add(e);
		
		return e;
	}
	
	public Edge<A> addEdge(A from, A to, int ll)
	{
		return this.addEdge(this.nodeMap.get(from), this.nodeMap.get(to), ll);
	}
	
	public Edge<A> addAltEdge(Node<A> from, Node<A> to, int ll, int rl)
	{	
		// TODO: Its unchecked or the edge already exists or the nodes are null.
		Edge<A> e = new Edge<A>(this,from,to,ll,rl);
		this.altEdges.add(e);
		
		return e;
	}
	
	public Edge<A> addAltEdge(A from, A to, int ll, int rl)
	{
		return this.addAltEdge(this.nodeMap.get(from), this.nodeMap.get(to), ll, rl);
	}
	
	public void initialise()
	{
		this.lpFW = new int[nodes.size()][nodes.size()];
		this.unsetCalcs();
	}
	
	/**
	 * Returns true if the normal Floyd Warshall matrix is filled.
	 * @return
	 */
	public boolean calculatedFW() 
	{
		return this.unsetFW == this.unsetEdges;
	}
	
	/**
	 * Debuggin.
	 */
	public void checkCount()
	{
		int i = 0;
		for(Edge e : this.altEdges)
		{
			if(e.state == EdgeState.EdgeStateUnknown)
			{
				i++;
			}
		}
		
		if(i == this.unsetEdges)
		{
			System.out.println("Unset edges correct. "+this.unsetEdges+"/"+i);
		}
		else
		{
			throw new Error("Incorrect setEdges: "+this.unsetEdges+"/"+i);
		}
	}
	
	/**
	 * Copy state from another alternative graph.
	 * Just because its possible.
	 * @param ori
	 */
	public void copyFrom(AltGraph<A> ori) 
	{
		int lenght = this.altEdges.size();
		for(int i = 0 ; i < lenght ; i++)
		{
			this.altEdges.get(i).copyFrom(ori.altEdges.get(i));
		}
		
		this.unsetEdges = ori.unsetEdges;
	}
	
	/**
	 * Execute default Floyd Warshall for calculating longest paths.
	 */
	public void executeFW()
	{	
		if(this.unsetEdges == this.unsetFW)
		{
			return;
		}
		
		int nodeCount = lpFW.length;
		
		// Initialise:
		for(int i = 0 ; i < nodeCount ; i++)
			for(int j = 0 ; j < nodeCount ; j++)
			{	
				if(i == j)
				{
					this.lpFW[i][i] = 0;
				}
				else
				{
					this.lpFW[i][j] = HALF_MIN_VALUE;
				}
			}
		
		for(Edge e : this.normalEdges)
		{
			final int r = e.right.id;
			final int l = e.left.id;
			this.lpFW[l][r] = e.rightLenght;
			//this.lpFW[r][l] = e.rightLenght;
		}
		
		for(Edge e : this.altEdges)
		{
			final int r = e.right.id;
			final int l = e.left.id;
			switch(e.state)
			{
			case EdgeStateLeft  : this.lpFW[r][l] = e.leftLenght;
			                      //this.lpFW[l][r] = - e.leftLenght; 
			                      break;
			case EdgeStateRight : this.lpFW[l][r] = e.rightLenght; 
								  //this.lpFW[r][l] = - e.info.rightLenght; 
								  break;
			}
			
		}
		
		int l = 0;

		
		for(int k = 0 ; k < nodeCount ; k++)
			for(int i = 0 ; i < nodeCount ; i++)
			{
				for(int j = 0 ; j < nodeCount ; j++)
				{
					final int lp_ik = lpFW[i][k];
					final int lp_kj = lpFW[k][j];
					if( lp_ik != HALF_MIN_VALUE && lp_kj != HALF_MIN_VALUE)
					{
						l = lp_ik + lp_kj;
						if(l > this.lpFW[i][j])
						{
							this.lpFW[i][j] = l;
						}
					}
				}
			}
		
		// store LP
		final int index = this.lpFW.length - 2;
		this.longestPath = this.lpFW[index][index+1];
		
		this.unsetFW = this.unsetEdges;
		this.unsetLP = this.unsetEdges;
		
//		System.out.println("All values:");
//		for(int i = 0 ; i < nodeCount ; i++)
//			for(int j = 0 ; j < nodeCount ; j++)
//			{
//				System.out.println("FW("+i+","+j+") = "+lpFW[i][j]);
//			}
	}
	
	
	/**
	 * Return ths longest path between nodes s and t in the graph.
	 * If the longest path isn't calculated jet, it calculates the longest path, otherwise it returns the stored value.
	 * If there has been an update in the Alternative graph then the longest path get recalculated.
	 * @return
	 */
	public int getLP()
	{
		if(this.unsetLP != this.unsetEdges)
		{
			this.executeFW();
		}
		return this.longestPath;
	}

	/**
	 * @return The number of alternative pairs who are unselected.
	 */
	public int getUnsetEdges() 
	{
		return this.unsetEdges;
	}

	/**
	 * @return True if all alternative pairs of edges are selected.
	 */
	public boolean isReady() 
	{
		return this.unsetEdges == 0;
	}

	/**
	 * @return False if a positive weight cycle is detected in the normal Floyd .
	 */
	public boolean isValidLP()
	{
		if(this.unsetEdges != this.unsetFW)
		{
			this.executeFW();
		}
		
		if(this.lpUnfeasible)
		{
			return false;
		}
		
		int l = this.nodes.size() - 2; // Its not posible to have a loop in the last 2 nodes.
		for(int i = 0 ; i < l ; i++)
		{
			if(this.lpFW[i][i] != 0)
			{
				this.lpUnfeasible = true;
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * String representation of the graph.
	 * @return
	 */
	public String printGraph()
	{
		String s = "AltGraph\n Nodes: "+nodes.size();
		
		for(Node n : nodes)
		{
			s += "\n Node: "+n.id;
		}
		
		s += "\n Normal Edges:";
		
		for(Edge e : normalEdges)
		{
			s += "\n "+e.printEdge();
		}
		
		s += "\n Alt Edges: ";
		
		for(Edge e : altEdges)
		{
			s += "\n "+e.printEdge();
		}
		
		return s;
	}
	
	/**
	 * Resets the graph such that no alternative pair is selected.
	 */
	public void reset() 
	{
		for(Edge e : this.altEdges)
		{
			e.state = EdgeState.EdgeStateUnknown;
			e.setDepth = -1;
		}
		
		this.unsetEdges = this.altEdges.size();
		
		this.longestPath = -1;
		this.unsetCalcs();
	}

	/**
	 * Reset the object to the begin state of the specified depth.
	 * @param depth
	 */
	public void setDepth(int depth)
	{
//		System.out.println("set Depth: "+depth);
//		this.checkCount();
		
		// mark LP as invalid
		this.unsetCalcs();
		
		for(Edge e : this.altEdges)
		{
			if(e.setDepth >= depth)
			{
				if(e.isSet())
				{
//					System.out.println("huh? "+e.isSet()+" "+e.state);
					
//					System.out.println("Unset: "+e.printEdge()+" "+depth);
					e.state = EdgeState.EdgeStateUnknown;
					e.setDepth = -1;
					this.unsetEdges++;
				}
			}
		}
		
		this.unsetCalcs();
//		System.out.println("Unset: "+s+" to "+this.unsetEdges+" change: "+i);
		
//		this.checkCount();
	}

	
	/**
	 * Backtrack the alternative graph to the version of the start of depth d.
	 * This object creates an object where the backtracking can be undone.
	 * @param depth
	 * @return
	 */
	public RedoAltGraph setDepthRedoable(int depth)
	{	
//		System.out.println("set Depth undoable: "+depth);
		
		RedoAltGraph redo = new RedoAltGraph();
		
		// mark LP as invalid
		this.unsetCalcs();
		
		for(Edge e : this.altEdges)
		{
			if(e.setDepth >= depth)
			{
				if(e.isSet())
				{
					redo.addRedo(e);
				}
			}
		}
		
		this.unsetCalcs();
		
		return redo;
	}
	
	public String toString()
	{
		String s = "AltGraph:";
		for(Edge e : this.altEdges)
		{
			s += "\n  "+e.printEdge();
		}
		
		return s;
	}
	
	/**
	 * Unset the results gained by executing the All pairs longest path algorithm.
	 */
	public void unsetCalcs()
	{
		this.unsetFW = -1;
		this.unsetLP = -1;
		this.lpUnfeasible = false;
	}
	
//	/**
//	 * Updates the Floyd Warshall result with given edge. 
//	 * (Used in experiment that wasn't so succesfull)
//	 * @param e
//	 */
//	public void updateFW(Edge e)
//	{
//		if(this.unsetFW - 1 != this.unsetEdges)
//		{
////			System.out.println("unsetFW: "+unsetFW+"/"+unsetEdges+" depth: "+this.strat.currentDepth\);
//			this.executeFW();
//			return;
////			throw new Error("Unset: "+unsetFW+"/"+unsetEdges);
//		}
//		
//		if(this.lpUnfeasible)
//		{ // its still unfeasible	
//			return;
//		}
//		
//		final int n1;
//		final int n2;
//		final int l;
//		
//		switch(e.state)
//		{
//		case EdgeStateRight : 	n1 = e.left.id ;
//								n2 = e.right.id;
//								l  = e.rightLenght; break;
//		case EdgeStateLeft :	n1 = e.right.id;
//								n2 = e.left.id;
//								l = e.leftLenght; break;
//								
//		default: throw new Error("Update FW on an Unset Edge!");
//		}
//		
//		this.unsetFW = this.unsetEdges;
//		
//		final int nodeCount = lpFW.length;
//		
//		if(l > lpFW[n1][n2])
//		{
//			if(lpFW[n2][n1] + l > 0)
//			{
//				this.lpUnfeasible = true;
//				return;
//			}
//			
//			lpFW[n1][n2] = l;
//		}
//		
//		for(int i = 0 ; i < nodeCount ; i++)
//			for(int j = 0; j < nodeCount ; j++)
//			{
////				if(!(i == n1 && n2 == j))
//				{
//					final int lp1 = lpFW[i][n1];
//					final int lp2 = lpFW[n2][j];
//					if(lp1 != JobDomains.HALF_MIN_VALUE && lp2 != JobDomains.HALF_MIN_VALUE)
//					{
//						final int val = lp1 + l + lp2;
//						if(val > lpFW[i][j])
//						{
//							if(lpFW[i][j] + lpFW[j][i] > 0)
//							{ // unfeasible interval detected!
////								System.out.println("Unfeasible interval detected: ("+i+","+j+") "+lpFW[i][j]+" + "+lpFW[j][i]);
////								System.out.println(this.printGraph());
//								this.lpUnfeasible = true;
//								
////								throw new Error("Unfeasible interval!");
//								
//								return;
//							}
//							
//							lpFW[i][j] = val;
//						}
//					}
//				}
//			}
//		
//		final int index = this.lpFW.length - 2;
//		this.longestPath = this.lpFW[index][index+1];
//		
//		this.unsetFW = this.unsetEdges;
//		this.unsetLP = this.unsetEdges;
//	}
}
