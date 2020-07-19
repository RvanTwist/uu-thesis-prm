package rpvt.ls.altGraph;

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
 * Represents an edge in the alternative graph, can both represent normal edges as alternative edge.
 * @author rene
 */
public class Edge<A> 
{
	public final AltGraph<A> graph;
	
	public final Node<A> left; 
	public final Node<A> right;
	
	public final int leftLenght;
	public final int rightLenght;
	
	int setDepth = -1;
	EdgeState state;
	
	public Edge(AltGraph<A> gr, Node<A> l, Node<A> r, int ll, int rl)
	{
		this.graph = gr;
		this.state = EdgeState.EdgeStateUnknown;
		this.left = l;
		this.right = r;
		
		this.leftLenght = ll;
		   
		this.rightLenght = rl;
		   
		l.edges.add(this);
		r.edges.add(this);
	}
	
	public Edge(AltGraph<A> gr, Node<A> l, Node<A> r, int len)
	{
		this(gr,l,r,len,len);	
	}
	
	/**
	 * Because it can.
	 * @param ori
	 */
	public void copyFrom(Edge<A> ori)
	{
		this.state = ori.state;
	}
	
	/**
	 * Return the lenght of the edge. 
	 * If this is an alternative edge and undirected then return the bigest lenght.
	 * @return
	 */
	public int getLenght()
	{
		switch(state)
		{
		case EdgeStateLeft : return this.leftLenght;
		case EdgeStateRight : return this.rightLenght;
		default : return Math.max(this.leftLenght, this.rightLenght);
		}
	}
	
	/**
	 * Returns the Node that this edge is pointing to.
	 * If this object is an alternative edge who isn't directed then returns null.
	 * @return
	 */
	public Node<A> getNext()
	{
		switch(state)
		{
		case EdgeStateLeft:  return  left;
		case EdgeStateRight: return right;
		default:  return null;
		}
	}
	
	/**
	 * Returns the Node that this edge is leaving from.
	 * If this object is an alternative edge who isn't directed then returns null.
	 * @return
	 */
	public Node<A> getPrevious()
	{
		switch(state)
		{
		case EdgeStateLeft:  return right;
		case EdgeStateRight: return left;
		default:  return null;
		}
	}
	
	/**
	 * Returns the direction state of this edge.
	 * @return
	 */
	public EdgeState getState() 
	{
		return this.state;
	}
	
	/**
	 * Returns true if selected.
	 * @return
	 */
	public boolean isSet()
	{
		return this.state != EdgeState.EdgeStateUnknown;
	}
	
	public String printEdge()
	{
		switch(state)
		{
		case EdgeStateLeft : return "Edge: "+left.id+" <- "+right.id+" lenght: "+leftLenght+" setDepth: "+setDepth;
		case EdgeStateRight : return "Edge: "+left.id+" -> "+right.id+" lenght: "+rightLenght+" setDepth: "+setDepth;
		case EdgeStateUnknown : return "Edge: "+left.id+" <-> "+right.id+" left lenght: "+leftLenght+" right lenght: "+rightLenght+" setDepth: "+setDepth;
		default : return "Edge: <Unknown>";
		}
	}
	
	/**
	 * Sets dirrection to this edge.
	 * This method fails if the alternative edge is already directed.
	 * @param st
	 */
	public void setDir(EdgeState st,int depth)
	{		
		if(this.state == EdgeState.EdgeStateUnknown)
		{
			this.state = st;
			this.setDepth = depth;
			
			this.graph.unsetEdges--;
		}
	}
}
