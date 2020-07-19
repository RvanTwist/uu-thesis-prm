package prm.ls.model4.mutations;

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
public abstract class MutationMergeSegmentGroupsAbstr implements Mutation
{
	Random random;
	
	public final M4_Planning planning;
	
	M4_MatchingAlgorithm matchAlg;
	MutationPlanDeclinedPRM plan1;
	MutationPlanDeclinedPRM plan2;
	
	M4_PossibleMatch match;
	
	boolean alreadyMerged1 = false;
	boolean alreadyMerged2 = false;
	
	boolean alreadyPlanned1 = false;
	boolean alreadyPlanned2 = false;
	
	boolean nothingHappend = false;
	//boolean unreverseable = false;
	
	M4_MergedSegmentGroup msg;
	
	public MutationMergeSegmentGroupsAbstr(M4_Planning p, M4_MatchingAlgorithm a)
	{
		this.random = p.random;
		
		this.planning = p;
		this.matchAlg = a;
		
		plan1 = new MutationPlanDeclinedPRM(p,a.newForMutation());
		plan2 = new MutationPlanDeclinedPRM(p,a.newForMutation());
	}
	
	@Override
	public void applyMutation() 
	{
		
		//System.out.println("# accepted match! "+match);
		
		if(plan1.prm != null)
			plan1.applyMutation();
		
		if(plan2.prm != null)
			plan2.applyMutation();
		
		planning.makeCheckpoint();
		
//DEBUG*/ match.g1.testConsistancy();
//DEBUG*/ match.g2.testConsistancy();
	}

	@Override
	public void debug() 
	{
		
	}
	
	public abstract M4_PossibleMatch getRandomMatching();

	@Override
	public void generateMutation(SolutionFacture f) throws CantPlanException 
	{
		
		// Reset reversing and accepting values:
		//this.unreverseable = false;
		this.nothingHappend = false;
		this.plan1.prm = null;
		this.plan2.prm = null;
		this.msg = null;
		
		// Get a random match.
		this.match = this.getRandomMatching();
		
		if(this.match == null)
		{
			f.feasible = false;
			return;
		}
		
//		if(!match.g1.isFixed() || !match.g2.isFixed())
//		{
//			f.feasible = false;
//			return;
//		}
		
//		System.out.println("Watching match: "+match);
		
		final M4_PRM_SegmentGroup sg1 = match.g1;
		final M4_PRM_SegmentGroup sg2 = match.g2;
		final int offset = match.offset;
		
		this.alreadyMerged1 = sg1.isMerged();
		this.alreadyMerged2 = sg2.isMerged();
		
		this.alreadyPlanned1 = sg1.isPlanned();
		this.alreadyPlanned2 = sg2.isPlanned();
		
		
		if(alreadyMerged1 && !alreadyPlanned1)
		{
			alreadyMerged1 = false;
			match.g1.getMergedGroup().removeSegmentGroup(match.g1);
		}
		
		if(alreadyMerged2 && !alreadyPlanned2)
		{
			alreadyMerged2 = false;
			match.g2.getMergedGroup().removeSegmentGroup(match.g2);
		}
		
	//	System.out.println("Generate Mutation: "+match+" planned: "+this.alreadyPlanned1+"|"+this.alreadyPlanned2+" alreadyMerged: "+this.alreadyMerged1+"|"+this.alreadyMerged2);

//DEBUG*/ match.g1.testConsistancy();
//DEBUG*/ match.g2.testConsistancy();
		
		if(this.alreadyMerged1 && this.match.g1.getMergedGroup() == this.match.g2.getMergedGroup())
		{
			//System.out.println(" - "+match+" is already planned!");
			f.feasible = false;
			this.nothingHappend = true;
			return;
		}
		
		// Check or the timeWindow can be actually used:
		{
			final M4_SegmentGroup planned1 = match.g1.getPlannedGroup();
			final M4_SegmentGroup planned2 = match.g2.getPlannedGroup();
			
			// sg1.start + offset = sg2.start
			// 
			// Mergedgroup mg1 [mg1.s,mg1.t] sg1 timewindow [sg1.s-sg1.offset,sg1.t-sg1.offset]
			
			final int p1ES = planned1.getErliestStart() + offset + sg1.getMergedOffset()	;
			final int p1LS = planned1.getLatestStart() + offset + sg1.getMergedOffset();
			final int p2ES = planned2.getErliestStart() + sg2.getMergedOffset() ;
			final int p2LS = planned2.getLatestStart() + sg2.getMergedOffset() ;
			//System.out.println("   w1: ["+p1ES+","+p1LS+"]("+sg1.getMergedOffset()+") w2: ["+p2ES+","+p2LS+"]("+sg2.getMergedOffset()+") off:"+offset);
			
			
			if(	p1LS < p2ES || 
				p2LS < p1ES	)
			{ // Time windows are disjoined.
				f.feasible = false;
				this.nothingHappend = true;
				return;
			}
		}
		
		// Check or there is at least one segment benefiting from this merge.
		boolean canPlan = false;
		for(M4_SegmentMatch segMatch : match.segments)
		{
			final M4_Area area = segMatch.segment1.area;
			final M4_PRM_Segment seg1 = segMatch.segment1;
			final M4_PRM_Segment seg2 = segMatch.segment2;
			
			
			if(	segMatch.segment1.getPlannedSegment().getCapicityRequirement() +
				segMatch.segment2.getPlannedSegment().getCapicityRequirement()
				<=  area.getMinCapWorkers()
				&&
				!(	segMatch.segment1.isFixedWorker() && segMatch.segment2.isFixedWorker() && 
					segMatch.segment1.getFixedWorker() != segMatch.segment2.getFixedWorker())
				)
			{
				canPlan = true;
				break;
			}
		}
		
		
		if(!canPlan)
		{
			//System.out.println(" can't plan this match! "+match);
			f.feasible = false;
			this.nothingHappend = true;
			
			return;
		}
		
		// Decide how to plan this.
		if(this.alreadyPlanned1)
		{
			if(this.alreadyPlanned2)
			{ // Both are planned:
				this.mergeBothPlanned(f);
			}
			else
			{
				//System.out.println("Merge planned1");
				this.mergedPlanned(sg1,sg2,f, match.offset);
			}
			
		}
		else if(this.alreadyPlanned2)
		{
			//System.out.println("Merge planned2");
			this.mergedPlanned(sg2,sg1,f, -match.offset);
		}
		else
		{
			this.mergeUnplanned(f);
		}
		
		//Debug
//		if(msg != null)
//		{
//			System.out.println(" Actual window: ["+msg.getErliestStart()+","+msg.getLatestStart()+"]");
//			//throw new Error("Checkpoint!");
//		}
	}

	private void mergeUnplanned(SolutionFacture f)  throws CantPlanException
	{	
//		System.out.println("Merge unplanned: "+match+" feasible: "+f.feasible);
		
		if(this.alreadyMerged1 || this.alreadyMerged2)
		{
			throw new Error("Both unplanned can't be merged!");
		}
		
		this.msg = new M4_MergedSegmentGroup(match.g1,match.g2, match.offset);
		
		// Plan the segmentgroup
		final int min = msg.getErliestStart();
		final int max = msg.getLatestStart();
		
		final int start = min + (int)Math.floor(random.nextDouble()*(max - min) + 0.5);
//		System.out.println(" planned at "+start+" :["+min+","+max+"]");
		msg.setStart(start);
		matchAlg.generateSuggestion(msg, f);
		
		this.plan1.planPRM(match.g1.prm, f);
		
//		System.out.println(" - After plan1: feasible: "+f.feasible);
		
		this.plan2.planPRM(match.g2.prm, f);
		
		
		
//		System.out.println(" - After plan2: feasible: "+f.feasible);
	}

	private void mergedPlanned(M4_PRM_SegmentGroup sg1, M4_PRM_SegmentGroup sg2, SolutionFacture f, int offset) throws CantPlanException 
	{	
//		System.out.println("mergedPlanned:");
		
		final boolean reschedule;
		final int sg2Start = sg1.getStart() + offset;
		
		if(sg2Start < sg2.getErliestStart() || sg2.getLatestStart() < sg2Start)
			reschedule = true;
		else
			reschedule = false;
		
		
		//int offset = (sg1 == match.g1 ? match.offset : -match.offset);
//		final int sg2_start = sg1.getStart() + offset;
		
//		if( !(sg2.getErliestStart() <= sg2_start && sg2_start <= sg2.getLatestStart() ))
//		{
//			System.out.println("not planned Group out of range" + match);
//			f.feasible = false;
//		}
		
		if(sg1.isMerged())
		{
			int mergedoffset1 = sg1.getMergedOffset();
			msg = sg1.getMergedGroup();
			msg.addSegmentGroup(sg2, mergedoffset1+offset);
		}
		else
		{
			msg = new M4_MergedSegmentGroup(sg1,sg2,offset);
		}
		
		// Check or rescheduling is neccesairy.
		if(reschedule)
		{
			msg.unPlan();
			
			final int min = msg.getErliestStart();
			final int max = msg.getLatestStart();
			
			final int start = min + (int)Math.floor(random.nextDouble()*(max - min) + 0.5);
			msg.setStart(start);
			matchAlg.generateSuggestion(msg, f);
		}
		
		if(!f.feasible)
		{
			return;
		}
		
		if( !this.alreadyPlanned1)
		{
			plan1.planPRM(match.g1.prm, f);
		}
		
		if( !this.alreadyPlanned2)
		{
			//match.g2.setPlanned(false);
			plan2.planPRM(match.g2.prm, f);
		}
	}

	final SolutionFacture dummyFacture = new SolutionFacture();
	
	private void mergeBothPlanned(SolutionFacture f) throws CantPlanException
	{	
//		System.out.println("merged both Planned:");
		
		final M4_PRM_SegmentGroup sg1 = match.g1;
		final M4_PRM_SegmentGroup sg2 = match.g2;
		
		// Check or they are already planned on the correct time or rescheduling is needed:
		final boolean reschedule;
		final boolean fixed1;
		final boolean fixed2;
		if(sg1.getStart() + match.offset == sg2.getStart())
		{
			reschedule = false;
			fixed1 = true;
			fixed2 = true;
		}
		else
		{
			if(sg1.isFixed())
			{
				fixed1 = true;
			}
			else
			{
				fixed1 = false;
				sg1.getPlannedGroup().unPlan();
			}
			if(sg2.isFixed())
			{
				fixed2 = true;
			}
			else
			{
				fixed2 = false;
				sg2.getPlannedGroup().unPlan();
			}
			
			if(fixed1 && fixed2)
			{
				f.feasible = false;
				return;
			}
			
			reschedule = true;
		}
			
		if(alreadyMerged1 && alreadyMerged2)
		{ 
			int mergedoffset1 = sg1.getMergedOffset();
			int mergedoffset2 = sg2.getMergedOffset();
			
			if(fixed1)
			{ // Correct the start time
				sg2.getMergedGroup().setStart(sg1.getStart() -(-match.offset + mergedoffset2));
			}
			if(fixed2)
			{ // Correct the start time
				sg1.getMergedGroup().setStart(sg2.getStart() -(match.offset + mergedoffset1));
			}
			
			if(fixed2)
			{
				this.msg = sg2.getMergedGroup();
				msg.mergeWith(sg1.getMergedGroup(), - match.offset - mergedoffset1 + mergedoffset2);
			}
			else
			{
				this.msg = sg1.getMergedGroup();
				msg.mergeWith(sg2.getMergedGroup(), match.offset + mergedoffset1 - mergedoffset2);
			}
			
		}
		else if(alreadyMerged1)
		{
			// MSG.s + mergedoffset = sg1.s
			// sg2.s = sg1.s + matchOffset.   = > sg2.s = MSG + mergedOffset + matchOffset
			
			this.msg = sg1.getMergedGroup();
			int mergedoffset1 = sg1.getMergedOffset();
			
			if(fixed2)
			{ // Correct the start time
				msg.setStart(sg2.getStart() -(match.offset + mergedoffset1));
			}
			
			msg.addSegmentGroup(sg2, match.offset + mergedoffset1);
		}
		else if(alreadyMerged2)
		{
			// MSG.s + mergedoffset = sg2.s
			// sg2.s = sg1.s + matchOffset.   = > MSG.s + mergedoffset = sg1.s + matchOffset
			int mergedoffset2 = sg2.getMergedOffset();
			this.msg = sg2.getMergedGroup();
			
			if(fixed1)
			{ // Correct the start time
				msg.setStart(sg1.getStart() -(-match.offset + mergedoffset2));
			}
			
			msg.addSegmentGroup(sg1, mergedoffset2 - match.offset);
		}
		else
		{
			if(fixed2)
			{ // Correct the start time
				sg1.setStart(sg2.getStart() -match.offset);
			}
			if(fixed1)
			{ // Correct the start time
				sg2.setStart(sg1.getStart() +match.offset);
			}
			
			this.msg = new M4_MergedSegmentGroup(sg1,sg2,match.offset);
		}
		
		if(reschedule)
		{
			//msg.unPlan();
			final int min = msg.getErliestStart();
			final int max = msg.getLatestStart();
			
			final int start = min + (int)Math.floor(random.nextDouble()*(max - min) + 0.5);
			msg.setStart(start);
			matchAlg.generateSuggestion(msg, f);
		}
	}

	@Override
	public double getWeight() 
	{
		return 5;
	}

	@Override
	public void rejectMutation() 
	{
		
		planning.backtrack();
		
//DEBUG*/ match.g1.testConsistancy();
//DEBUG*/ match.g2.testConsistancy();
	}
	
	public String toString()
	{
		return " Mutation Merge SegmentGroups\n"+
			   "      sg1: "+match.g1+"\n"+
			   "      sg2: "+match.g2+"\n"+
			   "      planned1: "+this.alreadyPlanned1+"\n"+
			   "      planned2: "+this.alreadyPlanned2+"\n"+
			   "      merged1: "+this.alreadyMerged1+"\n"+
			   "      merged2: "+this.alreadyMerged2
			   ;
	}
}
