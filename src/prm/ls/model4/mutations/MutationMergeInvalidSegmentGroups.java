package prm.ls.model4.mutations;

import java.util.ArrayList;
import java.util.Random;

import prm.ls.*;
import prm.ls.model4.*;
import prm.ls.model4.matching.M4_MatchingAlgorithm;
import prm.ls.model4.mutations.*;
import prm.problemdef.Segment;

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
public class MutationMergeInvalidSegmentGroups extends MutationMergeSegmentGroupsAbstr
{
	
	public boolean invalidOnlyMode;
	public boolean replanOnlyMode;
	public M4_SegmentGroup<?> segmentGroup;

	public MutationMergeInvalidSegmentGroups(M4_Planning p, M4_MatchingAlgorithm a) 
	{
		super(p, a);
	}

	@Override
	public M4_PossibleMatch getRandomMatching() 
	{
		if(invalidOnlyMode)
		{
			this.segmentGroup = planning.getRandomInvalidSG();
		}
		else if(this.replanOnlyMode)
		{
			this.segmentGroup = planning.getRandomReplanSG();
		}
		else	
		{
			int s1 = planning.invalidSegmentGroups.size();
			int s2 = planning.suspendedSegmentGroupsReplan.size();
			
			if(s1 + s2 == 0)
			{
				return null;
			}
			
			int i = planning.random.nextInt(s1 + s2);
			if( i < s1)
			{
				this.segmentGroup = planning.invalidSegmentGroups.get(i);
			}
			else
			{
				this.segmentGroup = planning.suspendedSegmentGroupsReplan.get(i-s1);
			}
		}
		
		if(this.segmentGroup == null)
		{
			return null;
		}
		
		if(segmentGroup instanceof M4_PRM_SegmentGroup)
		{
			M4_PRM_SegmentGroup prmSG = (M4_PRM_SegmentGroup)segmentGroup;
			if(prmSG.possibleMatches.size() > 0)
			{
				return prmSG.possibleMatches.get(planning.random.nextInt(prmSG.possibleMatches.size()));
			}
		}
		
		return null;
	}
	
	@Override
	public void generateMutation(SolutionFacture f) throws CantPlanException
	{
		if(this.match == null)
		{
			f.feasible = false; 
			return;
		}
		
		M4_PRM_SegmentGroup sg1 = this.match.g1;
		M4_PRM_SegmentGroup sg2 = this.match.g2;
		
		
		boolean wasValid1 	= sg1.isValid();
		boolean wasPlanned1 = sg1.isPlanned();
		
		boolean wasValid2 	= sg2.isValid();
		boolean wasPlanned2 = sg2.isPlanned();
		
		if(!wasValid1)
			sg1.unPlan();
		if(!wasValid2)
			sg2.unPlan();
		
		// Old generateMutation
		super.generateMutation(f);
		
		if(f.feasible)
		{
			f.accept = true;
			sg1.setPlanned(true);
			sg2.setPlanned(true);
			
			sg1.checkValid();
			sg2.checkValid();
			
			if(!wasPlanned1)
				planning.suspendedSegmentGroupsReplan.remove(sg1);
			if(!wasPlanned2)
				planning.suspendedSegmentGroupsReplan.remove(sg2);
		}
	}
	
}
