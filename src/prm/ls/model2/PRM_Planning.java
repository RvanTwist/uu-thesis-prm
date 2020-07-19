package prm.ls.model2;

import java.util.ArrayList;
import java.util.LinkedList;

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
public class PRM_Planning implements Comparable<PRM_Planning>
{
	public final PRM prm;
	public final SimplePlanning planning;
	
	PlannedSegment[] segments;
	SegmentGroup[] segmentGroups;
	
	boolean planned = false;
	
	public static final int DeclineCosts = 600;
	
	public PRM_Planning(SimplePlanning planning, PRM prm)
	{
		this.prm = prm;
		this.planning = planning;
		
		final int segment_count = prm.route.length;
		this.segments = new PlannedSegment[segment_count];
		
		for( int i = 0 ; i < segment_count ; i++)
		{
			final Segment s = prm.route[i];
			final ParalelResource area = planning.getResource(s.supervisingArea);
			this.segments[i] = new PlannedSegment(s,area);
		}
		
		// Construct segmentGroups
		LinkedList<PlannedSegment> segmentsG = new LinkedList<PlannedSegment>();
		LinkedList<SegmentGroup> groups = new LinkedList<SegmentGroup>();
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
					groups.addFirst(	new SegmentGroup( this,	segmentsG.toArray(new PlannedSegment[segmentsG.size()]),
															true, fixedTime));
				}
				else
				{
					//System.out.println("Create Free SegmentGroup");
					groups.addFirst(	new SegmentGroup( this, segmentsG.toArray(new PlannedSegment[segmentsG.size()])));
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
				groups.addFirst(	new SegmentGroup( this, segmentsG.toArray(new PlannedSegment[segmentsG.size()]),
														true, fixedTime));
//				System.out.println("Create Last Fixed SegmentGroup");
			}
			else
			{
				groups.addFirst(	new SegmentGroup( this, segmentsG.toArray(new PlannedSegment[segmentsG.size()])));
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
			this.segmentGroups = new SegmentGroup[groups.size()];
			int i = 0;
			SegmentGroup prev = null;
			for(SegmentGroup sg : groups)
			{
				sg.prev = prev;
				if(prev != null)
					prev.next = sg;
				
				if(!sg.fixed)
				{ // List for mutation
					this.planning.planableSegments.add(sg);
				}
				
				for(int k = 0 ; k < sg.segments.length ; k++)
				{
					final PlannedSegment seg = sg.segments[k];
					seg.segmentGroup = sg;
					seg.segmentGroupOffset = sg.offset[k];
				}
				
				this.segmentGroups[i] = sg;
				i++;
			}
			
			// TODO: Check or its from an arriving airplane or it works
			if(prm.arrivingPlane)
			{ // The planning should be fixed on arriving and handeled by that
				final SegmentGroup sg = this.segmentGroups[0];
				sg.setFixedTime(sg.getErliestStart());
			}
		}	
	}
	
	public void fullyPlanRandom()
	{
		if(this.planned)
		{
			this.unPlanInternal();
		}
		else
		{
			this.planning.declined.remove(this);
			this.planning.accepted.add(this);
			this.planned = true;
		}
		
		//System.out.println("SegmentGroups: "+this.segmentGroups.length);
		for(int i = this.segmentGroups.length - 1 ; i >= 0 ; i--)
		{
			SegmentGroup sg = this.segmentGroups[i];
			
			if(sg.fixed)
			{
				sg.planAt(sg.fixedTime);
			}
			else
			{
				final int min = sg.getErliestStart();
				final int max = sg.getLatestStart();
				
				final int time = min + (int)(Math.random()*(max - min + 1));
				
				//System.out.println("Move Segment("+sg.start+"): ("+time+") in ["+min+","+max+"] "+sg);
				
				sg.planAt(time);
			}
		}
	}

	public void costsUnPlan(SolutionFacture f)
	{
		if(this.planned)
		{
			f.addCosts(Model2Constants.declinePenalty);
			
			for(PlannedSegment ps : this.segments)
			{
				ps.costsUnPlan(f);
			}
		}
	}
	
	public void costsPlanAt(int[] times, SolutionFacture f)
	{
		if(!this.planned)
		{
			f.addCosts(-Model2Constants.declinePenalty);
		}
		
		for(int i = 0 ; i < this.segmentGroups.length ; i++)
		{
			this.segmentGroups[i].getCostsMove(times[i], f);
		}
	}
	
	public void unPlan() 
	{
		if(this.planned)
		{
			this.planned = false;
			this.unPlanInternal();
			
			this.planning.declined.add(this);
			this.planning.accepted.remove(this);
			this.planned = false;
			
			for(PlannedSegment ps : this.segments)
			{
				this.planning.planableSegments.remove(ps);
			}
		}
	}
	
	private void unPlanInternal() 
	{
		for(PlannedSegment ps : this.segments)
		{
			ps.unPlan();
		}
	}

	@Override
	public int compareTo(PRM_Planning other) 
	{
		// TODO Auto-generated method stub
		return this.prm.compareTo(other.prm);
	}

	public void plan(int[] time) 
	{
		if(!this.planned)
		{
			this.planning.declined.remove(this);
			this.planning.accepted.add(this);
			this.planned = true;
		}
		
		for(int i = 0 ; i < segmentGroups.length ; i++)
		{
			SegmentGroup sg = segmentGroups[i];
			sg.planAt(time[i]);
			this.planning.planableSegments.add(sg);
		}
	}

	public PlannedSegment getPlannedSegment(int i) 
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
}
