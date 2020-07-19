package prm.ls.model4.mutations;

import java.util.ArrayList;
import java.util.Random;

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
public abstract class MutationPlanPRMAbstr implements Mutation
{
	public ArrayList<M4_MatchingAlgorithm> matching_method = new ArrayList<M4_MatchingAlgorithm>();
	public M4_Planning planning;
	
	// Recoverable Data:
	M4_PRM prm;
	boolean feasible;
	
	Random random;
	
	public MutationPlanPRMAbstr(M4_Planning p, M4_MatchingAlgorithm alg)
	{
		this.matching_method.add(alg);
		this.planning = p;
		
		this.random = p.random;
	}
	
	abstract public M4_PRM getRandomDeclinedPRM();
	
	@Override
	public void generateMutation(SolutionFacture f) throws CantPlanException
	{
		this.prm = this.getRandomDeclinedPRM();
		
		if(this.prm == null)
		{
			if(M4_Planning.DEBUG)
			{
				System.out.println("Somehow this prm is a null pointer!");
			}
			
			f.feasible = false;
			return;
		}

		
		
		this.plan(f);
	}
	
	public void planPRM(M4_PRM prm, SolutionFacture f) throws CantPlanException
	{
		this.prm = prm;
		
		this.plan(f);
	}
	
	private void plan(SolutionFacture f) throws CantPlanException
	{	
		if(!this.prm.prm.allowPlan)
		{
			if(M4_Planning.DEBUG)
			{
				System.out.println("This prm is not allowed to be planned!");
			}
			
			f.feasible = false;
			return;
		}
		
		// Check feasibility to plan PRM
		for(M4_PRM_SegmentGroup sg : prm.segmentGroups)
		{
			if(sg.getLatestStart() < this.planning.minRescheduleTime)
			{
				f.feasible = false;
				return;
			}
		}
		
		f.costs -= prm.getDeclinePanelty();
		
//		String groupS = "";
//		for(M4_PRM_SegmentGroup sg : prm.segmentGroups)
//		{
//			groupS += "\n  - "+sg+" "+sg.printRoute();
//		}
//		
//		System.out.println(" Mutation plan: "+prm+""+groupS);
		
		// Try planning it:
		for(int i = 0 ; i < prm.segmentGroups.length ; i++)
		{
			M4_PRM_SegmentGroup sg = prm.segmentGroups[i];
		
			while(matching_method.size() <= i)
			{ // Need a new one
				matching_method.add(matching_method.get(0).newForMutation());
			}
			
			if(!sg.isPlanned())
			{	
				if( sg.isMerged() && sg.getMergedGroup().isPlanned())
				{ // Is already merged but the individual segments are not planned
					matching_method.get(i).generateSuggestion(sg,f);
				}
				else
				{ // plan this segment Group, check or its merged and plan that one
					final M4_SegmentGroup psg = sg.getPlannedGroup();
					
					final int min = psg.getErliestStart();
					final int max = psg.getLatestStart();
					
					final int start = min + (int)Math.floor(random.nextDouble()*(max - min) + 0.5);
//					System.out.println(" plan "+sg+" to "+start+" ["+min+","+max+"]");
					psg.setStart(start);
					
					matching_method.get(i).generateSuggestion(psg,f);
				}
			}
			else
			{
				// SG is planned;
				matching_method.get(i).clear();
			}
		}
	}
	
	@Override
	public void applyMutation() 
	{	
		prm.setPlanned();
		
		for(int i = 0 ; i < prm.segmentGroups.length ; i++)
		{
			this.matching_method.get(i).acceptSuggestion();
		}
		
		planning.makeCheckpoint();
		
//DEBUG*/ prm.testSGConsistancy();
	}

	@Override
	public void debug() 
	{
		
	}

	@Override
	public double getWeight() 
	{
		return 4;
	}

	@Override
	public void rejectMutation() 
	{	
		planning.backtrack();
		
//DEBUG*/ prm.testSGConsistancy();
	}
	
	
	
	public String toString()
	{
		return "Mutation Plan PRM\n         prm: "+this.prm;
	}
}
