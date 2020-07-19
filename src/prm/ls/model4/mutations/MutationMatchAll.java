package prm.ls.model4.mutations;

import java.util.TreeMap;

import prm.ls.Mutation;
import prm.ls.SolutionFacture;
import prm.ls.model4.CantPlanException;
import prm.ls.model4.M4_Area;
import prm.ls.model4.M4_PRM;
import prm.ls.model4.M4_Planning;
import prm.ls.model4.M4_Segment;
import prm.ls.model4.matching.M4_MatchingAlgorithm;
import prm.ls.model4.matching.lpflow.AreaFlowProblem_IF;
import prm.ls.model4.matching.lpflow.AreaFlowProblem_ILP3;
import prm.ls.model4.matching.lpflow.M4_CompleteFlowMatchingNoDelay;
import prm.problemdef.Area;
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
public class MutationMatchAll implements Mutation
{

	public TreeMap<Area,AreaFlowProblem_IF> areas;
	
	public M4_Planning planning;
	
	public MutationMatchAll(M4_Planning p, M4_MatchingAlgorithm matching)
	{	
		this.planning = p;
		
		if( matching instanceof M4_CompleteFlowMatchingNoDelay)
		{
			M4_CompleteFlowMatchingNoDelay cfm = (M4_CompleteFlowMatchingNoDelay) matching;
			this.areas = cfm.areas;
		}
		else
		{	
			this.areas = new TreeMap<Area,AreaFlowProblem_IF>();
			
			// Create all Areas:
			for(M4_Area a : p.areas.values())
			{
				areas.put(a.getArea(), new AreaFlowProblem_ILP3(a));
			}
			
			// Create all Segments
			for(M4_PRM prm : p.prms)
			{
				for(M4_Segment s : prm.segments)
				{
					AreaFlowProblem_IF afp = this.getAFP(s.area);
					afp.addSegment(s);
				}
			}
			
			// Initialise the edges
			for(AreaFlowProblem_IF afp : this.areas.values())
			{
				afp.initialiseEdges();
			}
		}
		
	}
	
	private AreaFlowProblem_IF getAFP(M4_Area area) 
	{
		return this.areas.get(area.getArea());
	}

	@Override
	public void applyMutation() 
	{
		planning.makeCheckpoint();
	}

	@Override
	public void debug() 
	{
		
	}

	@Override
	public void generateMutation(SolutionFacture f) throws CantPlanException
	{
		System.out.println("Match All Muation");
		for(AreaFlowProblem_IF afp : this.areas.values())
		{
			afp.updateAllSegments2();
			afp.solve();
			afp.convertToSollution();
			
			if(!afp.isFeasible())
			{
				f.feasible = false;
				return;
			}
		}
	}

	@Override
	public double getWeight() 
	{
		return 0.01;
	}

	@Override
	public void rejectMutation() 
	{
		planning.backtrack();
	}
	
}
