package prm.ls.model4.mutations;

import java.util.ArrayList;

import prm.ls.*;
import prm.ls.model4.*;
import prm.ls.model4.matching.M4_MatchingAlgorithm;

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
 * 
 * @author rene
 *
 */
public class MutationDeclinePRM implements Mutation
{
	public M4_Planning planning;
	
	M4_PRM prm;
	
	final M4_MatchingAlgorithm alg;
	
	public MutationDeclinePRM(M4_Planning p, M4_MatchingAlgorithm alg)
	{
		this.planning = p;
		this.alg = alg;
	}
	
	@Override
	public void generateMutation(SolutionFacture f) 
	{
		this.prm = this.planning.getRandomAccepted();
		if(prm == null || prm.isFixedPlanned())
		{
			f.feasible = false;
			return;
		}
		
		f.costs += prm.getDeclinePanelty();
	}
	
	@Override
	public void applyMutation() 
	{
		try
		{
			this.prm.unPlan();
		}
		catch(CantPlanException e)
		{
			e.printStackTrace();
			throw new Error(e);
		}
		
		for(M4_SegmentGroup sg : prm.segmentGroups)
		{
			this.alg.registerUnplan(sg);
		}
		
//		String groupS = "";
//		for(M4_PRM_SegmentGroup sg : prm.segmentGroups)
//		{
//			groupS += "\n  - "+sg+" "+sg.printRoute();
//		}
//		
//		System.out.println(" Mutation Declined: "+prm+""+groupS);
		
		planning.makeCheckpoint();
	}

	@Override
	public void debug() 
	{
		
	}

	@Override
	public double getWeight() 
	{
		return 1;
	}

	@Override
	public void rejectMutation() 
	{
		
	}
	
	@Override
	public String toString()
	{
		return "Decline PRM: "+prm;
	}

}
