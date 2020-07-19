package prm.ls.model4.matching.lpflow;



import java.util.ArrayList;

import prm.ls.model4.M4_Segment;
import rpvt.lp.VariableInterface;
import rpvt.lp.gurobi.*;

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
 * This object represents the link between the matcher and the LP
 * @author rene
 *
 */
public class Edge 
{	
	final M4_Segment from;
	final M4_Segment to;
	final VariableInterface<Edge> var;
	final AreaFlowProblem_IF superviser;
	
	ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	
	private boolean isActivated = false;
	private double costs = 0;
	
	
	
	public Edge(M4_Segment from, M4_Segment to, AreaFlowProblem_IF superviser)
	{	
		this.from = from;
		this.to = to;
		this.superviser = superviser;
		
		var = superviser.getModel().makeIntVar(this,0,0,0);
//		var.setLowerBound(0);
//		var.setUpperBound(0);
		//var.name = from.id+" => "+to.id;
		
		from.c1.setVar(var, 1);
		
		to.c2.setVar(var, 1);
	}
	
	
	public void setActivated(boolean b)
	{
//		if(b == isActivated)
//			return;
		
		if(b)
		{
			var.setUpperBound(1);
			var.setWeight(this.costs);
		}
		else
		{
			var.setUpperBound(0);
			var.setWeight(1000);
		}
		
		this.isActivated = b;
	}
	
	public void setCosts(double c)
	{
		if(this.costs == c)
			return;
		
		this.costs = c;
		
		var.setWeight(c);
	}
	
	public boolean isChosen()
	{
		return Math.round(this.var.getValue()) == 1;
	}
	
	public boolean isActivated()
	{
		return this.isActivated;
	}
	
	public double getCosts()
	{
		return this.costs;
	}


	public boolean bothPlanned() 
	{
		return from.isPlanned() && to.isPlanned();
	}
}
