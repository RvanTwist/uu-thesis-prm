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
public class MutationMoveSegmentGroupUnMerge implements Mutation
{
	public M4_MatchingAlgorithm matching_method;
	public M4_Planning planning;
	
	// Recoverable Data:
	M4_SegmentGroup<?> segmentGroup;
	
	Random random;
	
	public MutationMoveSegmentGroupUnMerge(M4_Planning p, M4_MatchingAlgorithm alg)
	{
		this.matching_method = alg;
		this.planning = p;
		
		this.random = p.random;
	}
	
	@Override
	public void generateMutation(SolutionFacture f) throws CantPlanException
	{
		this.segmentGroup = planning.getRandomPlannedSegmentGroup();
		
		if(this.segmentGroup == null || this.segmentGroup.isFixed())
		{
			f.feasible = false;
			return;
		}

		
		this.plan(f);
	}
	
	public void planPRM(M4_SegmentGroup sg, SolutionFacture f) throws CantPlanException
	{
		this.segmentGroup = sg;
		
		this.plan(f);
	}
	
	private void plan(SolutionFacture f) throws CantPlanException
	{	
		
		if(segmentGroup instanceof M4_PRM_SegmentGroup)
		{
			M4_PRM_SegmentGroup sg = (M4_PRM_SegmentGroup) segmentGroup;
			
			if(sg.isMerged())
			{
				sg.getMergedGroup().removeSegmentGroup(sg);
			}
		}
		
		// plan this segment Group, check or its merged and plan that one
		
		segmentGroup.unPlan();
		
		final int min = segmentGroup.getErliestStart();
		final int max = segmentGroup.getLatestStart();
		
		final int start = min + (int)Math.floor(random.nextDouble()*(max - min) + 0.5);
//		System.out.println(" move "+segmentGroup+" to "+start+" ["+min+","+max+"]");
		segmentGroup.setStart(start);
		
		matching_method.generateSuggestion(segmentGroup,f);
	}
	
	@Override
	public void applyMutation() 
	{	
		this.matching_method.acceptSuggestion();
		
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
		return 2;
	}

	@Override
	public void rejectMutation() 
	{	
		planning.backtrack();
	}
	
	public String toString()
	{
		String members = "";
		for(M4_Segment seg : segmentGroup)
		{
			members += seg.toString();
		}
		
		return 	"Moving: "+segmentGroup+"\n"+
				" members: "+members;
	}

}
