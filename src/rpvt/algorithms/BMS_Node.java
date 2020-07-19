package rpvt.algorithms;

import java.util.ArrayList;

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
public class BMS_Node<A>
{
	@SuppressWarnings("unchecked")
	Bipartite_Matching_Solver solver;
	
	A represents;
	ArrayList<BMS_Edge> edges = new ArrayList<BMS_Edge>();
	
	int weight = 0;
	
	BMS_Edge 			sp_edge;
	ShortestPathColor	sp_color = ShortestPathColor.WHITE; // 0 = white, 1 = gray, 3 = black
	int					sp_len; // For extending with weights TODO: Implement this.
	
	@SuppressWarnings("unchecked")
	public BMS_Node(Bipartite_Matching_Solver s) 
	{
		this.solver = s;
	}
	
	public A getRepresents()
	{
		return this.represents;
	}
	
	@SuppressWarnings("unchecked")
	public void delete()
	{
		solver.stored_nodes.push(this);
		
		for(BMS_Edge edge : edges)
		{
			edge.delete();
		}
		
		edges.clear();
	}
	
	@SuppressWarnings("unchecked")
	public void makeEdgeTo(BMS_Node to)
	{
		BMS_Edge edge = solver.requestEdge();
		edge.set(this, to);
	}
}
