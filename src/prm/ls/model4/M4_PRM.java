package prm.ls.model4;

import java.util.ArrayList;
import java.util.LinkedList;

import prm.ls.AbstractBackTrackable;
import prm.ls.Mutation;
import prm.ls.SimulatedAnnealing;
import prm.ls.SolutionFacture;
import prm.ls.resources.ParalelResource;
import prm.problemdef.*;

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
public class M4_PRM extends AbstractBackTrackable implements Comparable<M4_PRM>
{
	public final PRM prm;
	public final M4_Planning planning;
	
	public M4_PRM_Segment[] segments;
	public M4_PRM_SegmentGroup[] segmentGroups;
	
	public static final int DeclineCosts = 600;
	
	// Dynamic variables
	private boolean planned = false;
	private boolean fixedPlanned = false;
	
	// Backtrack variables
	public boolean bt_planned = false;
	
	public void setFixedPlanned()
	{
		this.fixedPlanned = true;
	}
	
	public boolean isFixedPlanned()
	{
		return this.fixedPlanned;
	}
	
	public M4_PRM(M4_Planning planning, PRM prm)
	{
		super(planning.backtracker);
		
		this.prm = prm;
		this.planning = planning;
		
		final int segment_count = prm.route.length;
		this.segments = new M4_PRM_Segment[segment_count];
		
		{
			M4_PRM_Segment prev = null;
			
			for( int i = 0 ; i < segment_count ; i++)
			{
				final Segment s = prm.route[i];
				final M4_Area area = planning.getResource(s.supervisingArea);
				final M4_PRM_Segment newSeg = new M4_PRM_Segment(s,this,area);
				this.segments[i] = newSeg;
				
				if(prev != null)
				{
					newSeg.prevPRMsegment = prev;
					prev.nexPRMsegment = newSeg;
				}
				prev = newSeg;
			}
		}
		
		// Construct segmentGroups
		LinkedList<M4_PRM_Segment> segmentsG = new LinkedList<M4_PRM_Segment>();
		LinkedList<M4_PRM_SegmentGroup> groups = new LinkedList<M4_PRM_SegmentGroup>();
		boolean fixed = false;
		int fixedTime = prm.deadline;
		for( int i = segment_count - 1 ; i >= 0  ; i--)
		{
			final Segment s = prm.route[i];
			if(s.embarkment)
			{
				//System.out.println("Fixed: Segment "+s);
				fixed = true;	
			}
			
			fixedTime -= s.segmentTime;
			
			
			segmentsG.addFirst(this.segments[i]);
			
			if(s.getFrom().isSupervised() && segmentsG.size() > 0)
			{ // Make a segmentGroup from the next set
				if(fixed)
				{
					//System.out.println("Create Fixed SegmentGroup");
					groups.addFirst(	new M4_PRM_SegmentGroup( this,	segmentsG.toArray(new M4_PRM_Segment[segmentsG.size()]),
															true, fixedTime, false));
				}
				else
				{
					//System.out.println("Create Free SegmentGroup");
					groups.addFirst(	new M4_PRM_SegmentGroup( this, segmentsG.toArray(new M4_PRM_Segment[segmentsG.size()])));
				}
			
				fixed = false;
				fixedTime = 0;
				segmentsG.clear();
			}
		}
		
		// SegmentsGroup;
		if(segmentsG.size() > 0)
		{ // Make a segmentGroup from the next set
			if(fixed)
			{
				groups.addFirst(	new M4_PRM_SegmentGroup( this, segmentsG.toArray(new M4_PRM_Segment[segmentsG.size()]),
														true, fixedTime,false));
//				System.out.println("Create Last Fixed SegmentGroup");
			}
			else
			{
				groups.addFirst(	new M4_PRM_SegmentGroup( this, segmentsG.toArray(new M4_PRM_Segment[segmentsG.size()])));
//				System.out.println("Create Last Free SegmentGroup");
			}
		
			fixed = false;
			fixedTime = 0;
			segmentsG.clear();
		}
		else if(groups.size() == 0)
		{
			throw new Error("segmentsG size = 0, "+segment_count+" "+prm.route.length);
		}
		
		// Postproces segments:
		{
			this.segmentGroups = new M4_PRM_SegmentGroup[groups.size()];
			int i = 0;
			M4_PRM_SegmentGroup prev = null;
			for(M4_PRM_SegmentGroup sg : groups)
			{
				sg.prev = prev;
				if(prev != null)
					prev.next = sg;
				
				for(int k = 0 ; k < sg.segments.length ; k++)
				{
					final M4_PRM_Segment seg = sg.segments[k];
					try 
					{
						seg.setPrimarySegmentGroup(sg, sg.offset[k]);
					} 
					catch (CantPlanException e) 
					{
						e.printStackTrace();
						throw new Error(e);
					}
				}
				
				this.segmentGroups[i] = sg;
				i++;
				
				prev = sg;
			}
			
//			// TODO: Check or its from an arriving airplane or it works
			
			
			if(this.prm.arrivingPlane)
			{ // The planning should be fixed on arriving and handeled by that
				final M4_PRM_SegmentGroup sg = this.segmentGroups[0];
				
				if(sg.isFixed() && sg.fixedTime != prm.arrival)
				{	
					int diff = sg.fixedTime - prm.arrival;
					
					if(diff < 0)
					{ // Its impossible to Plan this PRM.
						try
						{
							this.unPlan();
						}
						catch(CantPlanException e)
						{
							e.printStackTrace();
						}
						this.prm.allowPlan = false;
						return;
					}
					
					System.out.println("Adding diff to prm "+prm.prm_ID+": "+diff);
					
					sg.setFixedTime(prm.arrival);
					// Delay the embarking segment:
					boolean embarkingPassed = false;
					for(int j = 0 ; j < prm.route.length ; j++)
					{
						if(prm.route[j].embarkment)
							embarkingPassed = true;
						
						if(embarkingPassed)
						{
							sg.offset[j] = sg.offset[j] + diff;
							sg.segments[j].segmentGroupOffset = sg.segments[j].segmentGroupOffset + diff;
						}
					}
					
					sg.lenght += diff;	
				}
				
				sg.setFixedTime(prm.arrival);
				
			}
			
			
		}	
	}
	
	public void unPlan() throws CantPlanException
	{	
		if(this.planned)
		{
			if(this.fixedPlanned)
			{
				throw new Error("Can't unplan an PRM that is fixed");
			}
			
			this.planning.declinePanelty += this.getDeclinePanelty();
			
			this.planned = false;
			this.unPlanInternal();
			
			this.planning.declined.add(this);
			this.planning.accepted.remove(this);
			this.planned = false;
			
			// Remove itself from merged Group if any:
			for(M4_PRM_SegmentGroup sg : this.segmentGroups)
			{
				sg.unPlan();
				
//				if(sg.isMerged())
//				{
//					final M4_MergedSegmentGroup msg = sg.getMergedGroup();
//					
//					msg.removeSegmentGroup(sg);
//					
//					if(msg.segmentGroups.size() == 0)
//					{
//						this.planning.plannedSegmentGroups.remove(sg.getMergedGroup());
//					}
//				}
//				else
//				{ // Remove from movable segments
//					planning.plannedSegmentGroups.remove(sg);
//				}
			}
			
			this.registerChange();
		}
	}
	
	private void unPlanInternal() throws CantPlanException
	{
		for(M4_Segment ps : this.segments)
		{
			ps.unPlan();
		}
	}

	@Override
	public int compareTo(M4_PRM other) 
	{
		// TODO Auto-generated method stub
		return this.prm.compareTo(other.prm);
	}


	public M4_Segment getPlannedSegment(int i) 
	{
		return this.segments[i];
	}

	public boolean isPlanned()
	{
		return planned;
	}

	public int getStartTime() 
	{
		return this.segments[0].getStartTime();
	}

	public void setPlanned() 
	{
		
		if(!planned)
		{
			if(this.fixedPlanned)
			{
				throw new Error("Although its nice this PRM suddenly could be planned, we already said no to him. Nevertheless an error in the program.");
			}
			
			planning.declinePanelty -= this.getDeclinePanelty();
			
			this.planned = true;
			planning.declined.remove(this);
			planning.accepted.add(this);
			
			this.registerChange();
		}
	}
	
	public int getDeclinePanelty()
	{
		if(prm.booked)
			return M4_Constants.declinePenaltyBooked;
		else
			return M4_Constants.declinePenalty;
	}

	public void checkConsistancy() 
	{
		if(!this.planned)
		{
			// Do not planned checks
			
			for(M4_PRM_SegmentGroup sg : this.segmentGroups)
			{
				if(sg.isPlanned())
				{
					throw new Error(" PRM unplanned SegmentGroup "+sg.id+" is planned!");
				}
				
				sg.testConsistancy();
			}
			
			return;
		}
		
		if(segments[0].getStartTime() < prm.arrival)
		{
			final M4_PRM_SegmentGroup sg0 = this.segmentGroups[0];
			
			final String mergedWindow;
			
			if(sg0.isMerged())
				mergedWindow = " Merged group window: "+sg0.getMergedGroup().getErliestStart()+","+sg0.getMergedGroup().getLatestStart()+"\n";
			else
				mergedWindow = "\n";
			
//			planning.solver.lastMutation.debug();
			
			throw new Error(" PRM start earlier then he/she arrives!\n"+
							" Last Mutation: "+planning.solver.lastMutation.getClass().getSimpleName()+"\n"+
							" feasible : "+planning.solver.facture.feasible+"\n"+
							" PRM :"+prm.prm_ID+"\n"+
							" Arrival : "+prm.arrival+" Deadline: "+prm.deadline+"\n"+
							" Segment 1 start: "+segments[0].getStartTime()+" SG: "+this.segmentGroups[0].getStart()+"\n"+
							" SegmentGroup window: "+sg0.getErliestStart()+","+sg0.getLatestStart()+"\n"+
							mergedWindow+
							" SegmentGroup lenght: "+sg0.lenght+"\n"+
							" MergedWith: "+sg0.getMergedGroup()+"\n"+
							" mutation data: "+planning.solver.lastMutation);
			
		}
		
		if(segments[segments.length-1].getEndTime() > prm.deadline)
		{
			
			M4_PRM_Segment lastSeg = segments[segments.length-1];
			M4_PRM_SegmentGroup lastGroup = segmentGroups[segmentGroups.length-1];
			throw new Error(" PRM ends later then the the deadline: "+segments[segments.length-1].getEndTime()+" > "+prm.deadline+"\n"+
							" PRM: "+prm.prm_ID+"\n"+
							"  debug:\n"+
							"  route: "+prm.printRoute()+"\n"+
							"  lastSegment start: "+lastSeg.getStartTime()+" "+lastSeg.segment+"\n"+
							"  Last SegmentGroup: "+lastGroup.getStart()+" ("+lastGroup.fixed+","+lastGroup.fixedTime+") lenght: "+lastGroup.lenght);
		}
		
		int time = prm.arrival;
		String sg_strings = "";
		for(M4_PRM_SegmentGroup sg : this.segmentGroups)
		{
			if(sg.suspended)
			{ // Nothing to see here.
				continue;
			}
			
			sg_strings = sg_strings +" "+sg.toString();
			
			if(!sg.isPlanned())
			{
				throw new Error(" PRM planned, SegmentGroup "+sg.id+" is not planned!");
			}
			
			if(sg.getStart() < time)
			{
				// SegmentGroup analysis:
				String sg_ana = "";
				
				for(M4_PRM_SegmentGroup sg2 : this.segmentGroups)
				{
					sg_ana = sg_ana +"\n              "+sg2+" prev: "+sg2.prev+" next: "+sg2.next;
				}
				
				M4_MergedSegmentGroup merged = sg.getMergedGroup();
				
				System.out.println(" prev latest start: "+sg.prev.getLatestStart());
				System.out.println(" this latest start: "+sg.getLatestStart());
				System.out.println(" prev lenght: "+sg.prev.lenght);
				System.out.println(" prev earliest start: "+sg.prev.getErliestStart());
				System.out.println("Test!");
				
				System.out.println();
				
				// Debug: affected prm:
				if(M4_DynamicFrameWork.TheOne != null)
				{
					M4_DynamicFrameWork dfw = M4_DynamicFrameWork.TheOne;
					System.out.println("Debug data: ");
					final M4_PRM affectedPRM = sg.prm;
					System.out.println(("SegmentGroup can't start jet! "+sg.getStart()+" < "+time+"\n"+
							" Last Mutation: "+sg.prm.planning.solver.lastMutation+"\n"+
							" Last feasiblity: "+sg.prm.planning.solver.facture.feasible+"\n"+
							" SegmentGroup: "+sg+"\n"+
							" PRM:          "+sg.prm+" release: "+sg.prm.prm.arrival+"\n"+
							" start:        "+sg.getStart()+"\n"+
							" window:        ["+sg.getErliestStart()+","+sg.getLatestStart()+"]\n"+
							" prev Group:   "+sg.prev+"\n"+
							" merged Group: "+(merged == null ? "none" : merged.toString())+"\n"+
							" merged Window: "+(merged == null ? "[]" : "["+merged.getErliestStart()+","+merged.getLatestStart()+"]")+"\n"+
							" SG groups:    "+sg_strings+"\n"+
							" SG Analysis: "+sg_ana+"\n"+
							(sg.prev != null ?  " prev window:  "+sg.prev.getStart()+" ["+sg.prev.getErliestStart()+","+sg.prev.getLatestStart()+"]" : "")));
					System.out.println();
					System.out.println("Reverting sollution");
					dfw.getCurrentPlanning().unfix();
					try
					{
						dfw.getCurrentPlanning().loadSolution(dfw.getCurrentSollution());
					}
					catch(CantPlanException e)
					{
						e.printStackTrace();
						throw new Error(e);
					}
					System.out.println("Current PRM: "+affectedPRM);
					System.out.println(" time: "+affectedPRM.getStartTime()+" planned: "+affectedPRM.isPlanned());
					
				}
				
				
				System.out.println("Breakpoint");
				
				throw new Error("SegmentGroup can't start jet! "+sg.getStart()+" < "+time+"\n"+
									" Last Mutation: "+sg.prm.planning.solver.lastMutation+"\n"+
									" Last feasiblity: "+sg.prm.planning.solver.facture.feasible+"\n"+
									" SegmentGroup: "+sg+"\n"+
									" PRM:          "+sg.prm+" release: "+sg.prm.prm.arrival+"\n"+
									" start:        "+sg.getStart()+"\n"+
									" window:        ["+sg.getErliestStart()+","+sg.getLatestStart()+"]\n"+
									" prev Group:   "+sg.prev+"\n"+
									" merged Group: "+(merged == null ? "none" : merged.toString())+"\n"+
									" merged Window: "+(merged == null ? "[]" : "["+merged.getErliestStart()+","+merged.getLatestStart()+"]")+"\n"+
									" SG groups:    "+sg_strings+"\n"+
									" SG Analysis: "+sg_ana+"\n"+
				(sg.prev != null ?  " prev window:  "+sg.prev.getStart()+" ["+sg.prev.getErliestStart()+","+sg.prev.getLatestStart()+"]" : ""));
			}
			
			if(sg.fixed && sg.getStart() != sg.fixedTime)
			{
				throw new Error("SegmentGroup doesn't start on the given time!\n"+
//											" Last Mutation: "+sg.prm.planning.solver.lastMutation+"\n"+
//											" Last feasiblity: "+sg.prm.planning.solver.facture.feasible+"\n"+
											" SegmentGroup: "+sg+"\n"+
											" PRM:          "+sg.prm+" release: "+sg.prm.prm.arrival+"\n"+
											" start:        "+sg.getStart()+"\n"+
											" window:        ["+sg.getErliestStart()+","+sg.getLatestStart()+"]"+
											" prev Group:   "+sg.prev+"\n"+
						(sg.prev != null ?  " prev window:  "+sg.prev.getStart()+" ["+sg.prev.getErliestStart()+","+sg.prev.getLatestStart()+"]" : ""));
			}
			
			
			for(M4_PRM_Segment seg : sg.segments)
			{
				if(seg.mergedWith != null)
				{
					if(seg.mergedWith.getWorker() == null)
					{
						throw new Error("Planned Worker is null! "+seg.id+" "+seg+"\n"+
								" Segment lenght: "+seg.segment.segmentTime+" local_id:"+seg.local_Id+"\n"+
								" prev: "+seg.getPrevious()+"\n"+
								" next: "+seg.getNext()+"\n"+
								" current:      "+seg.currString()+"\n"+
								" backtrack:    "+seg.backTrackString()+"\n" +
								" segmentGroup: "+seg.getOriSegmentGroup()+" "+seg.getOriSegmentGroup().printRoute()+"\n"+
								" prm:          "+seg.prm);
					}
				}
				else
				{
					if(seg.getWorker() == null)
					{
						final SimulatedAnnealing solver = this.planning.solver;
						final Mutation lastMutation = solver.lastMutation;
						
						
						
						throw new Error("Planned Worker is null! "+seg.id+" "+seg+"\n"+
										" Segment lenght: "+seg.segment.segmentTime+" local_id:"+seg.local_Id+" \n"+
										" prev: "+seg.getPrevious()+"\n"+
										" next: "+seg.getNext()+"\n"+
										" current:      "+seg.currString()+"\n"+
										" backtrack:    "+seg.backTrackString()+"\n" +
										" segmentGroup: "+seg.getOriSegmentGroup()+" "+seg.getOriSegmentGroup().printRoute()+"\n"+
										" prm:          "+seg.prm+"\n"+
										" last Mutation: "+lastMutation+"\n");
					}
				}
				
				if(sg.getStart() + seg.getOriSegmentGroupOffset() != seg.getStartTime())
				{
					// Analyse SegmentGroup
					String segmentStrings = " SG Segments :";
					
					for(M4_PRM_Segment sg_seg : sg)
					{
						segmentStrings += "\n # "+sg_seg+" offset "+sg_seg.getOriSegmentGroupOffset()+"/"+sg_seg.getStartTime();
					}
					
					final Mutation lastMutation = this.planning.solver.lastMutation;
					
					throw new Error("Segment: "+seg+" doesn't start on time!\n"+
									" SG        : "+sg+"\n"+
									" SG start  : "+sg.getStart()+"\n"+
									" seg offset: "+seg.getOriSegmentGroupOffset()+"\n"+
									" seg start : "+seg.getStartTime()+"\n"+
									segmentStrings+"\n"+
									"lastmutation: "+lastMutation);
				}
				
				time = Math.max(seg.getEndTime(),time);
			}
			
		}	
	}
	
	public void testSGConsistancy()
	{
		for(M4_PRM_SegmentGroup sg : this.segmentGroups)
		{
			sg.testConsistancy();
		}
	}
	
	protected void doBacktrack()
	{
		if(bt_planned != planned)
		{
			if(planned)
			{
				this.planning.accepted.remove(this);
				this.planning.declined.add(this);
			}
			else
			{
				this.planning.accepted.add(this);
				this.planning.declined.remove(this);
			}
		}
		
		planned = bt_planned;
	}
	
	protected void doMakeCheckpoint()
	{
		bt_planned = planned;
	}
	
	public String toString()
	{
		return "[PRM "+prm.prm_ID+" "+planned+"]";
	}

	public void unfix() 
	{
		this.fixedPlanned = false;
		for(M4_PRM_SegmentGroup sg : this.segmentGroups)
		{
			sg.unfix();
		}
	}
	
	
	public M4_PRM_SegmentGroup getFirstGroup()
	{
		return this.segmentGroups[0];
	}
	
	public M4_PRM_SegmentGroup getLastGroup()
	{
		return this.segmentGroups[this.segmentGroups.length-1];
	}

	public void setArrivalSuspended() 
	{
		final int imax = Math.max(this.segmentGroups.length-1, 1);
		for(int i = 0 ; i < imax ; i++)
		{
			
		}
	}

	public void setDepartSuspended() throws CantPlanException
	{
		// TODO Auto-generated method stub
		M4_PRM_SegmentGroup sg = this.segmentGroups[this.segmentGroups.length-1];
		sg.unPlan();
		sg.setSuspended();
	}

	public void reloadPRM() 
	{
		this.prm.initialiseSegments();
		
		for(M4_PRM_SegmentGroup sg : this.segmentGroups)
		{
			sg.reload();
		}
	}
}
