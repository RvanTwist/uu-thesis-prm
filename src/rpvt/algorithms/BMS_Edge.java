package rpvt.algorithms;

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
public class BMS_Edge 
{
	protected Bipartite_Matching_Solver solver;
	
	protected BMS_Node from;
	protected BMS_Node to;
	
	int flow   = 0;
	int maxFlow = 1;
	//int weight = 0;
	
	public void set(BMS_Node f, BMS_Node t)
	{
		this.flow 	= 0;
		this.maxFlow = 1;
		//this.weight = w;
		
		this.from 	= f;
		this.to 	= t;
		
		from.edges.add(this);
		to.edges.add(this);
	}
	
	public BMS_Edge(Bipartite_Matching_Solver s)
	{
		this.solver = s;
	}
	
	public void setMaxFlow(int mf)
	{
		this.maxFlow = mf;
	}
	
	@SuppressWarnings("unchecked")
	public void delete()
	{
		if(this.from != null)
		{
			this.from 	= null;
			this.to		= null;
			solver.stored_edges.push(this);
		}
	}
}
