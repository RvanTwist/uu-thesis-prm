package prm.ls.model4;

import java.util.ArrayList;
import java.util.Iterator;

import prm.ls.SolutionFacture;
import prm.ls.resources.ParalelResource;
import prm.problemdef.PRM;
import prm.problemdef.Segment;
import rpvt.util.ArrayIterator;

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
public class M4_PRM_SegmentGroup extends M4_SegmentGroup<M4_PRM_Segment>
{
//	public boolean fixedArrival = false;
//	public boolean fixedDepart  = false;
	
	M4_PRM_Segment[] segments;
	M4_PRM_Segment[] planorder;
	
	int[] offset;
	int lenght;
	
	M4_PRM_SegmentGroup prev;
	M4_PRM_SegmentGroup next;
	
	public final M4_PRM prm;
	
	public boolean fixStart = true;
	public final boolean origional_fixed;
	public int origional_fixedTime;
	int timeWindowEarliest_Start = -1;
	
	// Possible Merges;
	public ArrayList<M4_PossibleMatch> possibleMatches = new ArrayList<M4_PossibleMatch>();
	
	// Dynamic variable
	private M4_MergedSegmentGroup 	mergedGroup = null;
	private int 					mergedGroupOffset = 0;
	
	// Backtrack variables
	private M4_MergedSegmentGroup 	bt_mergedGroup = null;
	private int 					bt_mergedGroupOffset = 0;
	
	public boolean suspended = false;
	
	public M4_PRM_SegmentGroup(M4_PRM prm, M4_PRM_Segment[] segments, boolean fixed, int fixedTime)
	{
		super(prm.planning);
		
		this.prm = prm;
		
		if(segments.length == 0)
			throw new Error("Debug");
		
		this.segments = segments;
		
		this.origional_fixed = fixed;
		this.origional_fixedTime = fixedTime;
		
		this.fixed = fixed;
		this.fixedTime = fixedTime;
		
		this.lenght = 0;
		this.offset = new int[segments.length];
		
		for(int i = 0 ; i < this.offset.length ; i++)
		{
			offset[i] = lenght;
			
//			if(segments[i].segment.segmentTime == 0)
//			{
//				Segment pd_seg = segments[i].segment;
//				
//				System.out.println(" Odd segmentime = 0, "+pd_seg+" recalc: "+pd_seg.supervisingArea.getDistance(pd_seg.from, pd_seg.to));
//			}
			
			lenght += segments[i].segment.segmentTime;
		}
	}
	
	@Override
	public void reload()
	{	
		super.reload();
		
//		this.lenght = 0;
//		for(M4_Segment m4_seg : this.segments)
//		{
//			this.lenght += m4_seg.segment.segmentTime;
//		}
		
		PRM prm = this.prm.prm;
		if(prm.arrivingPlane)
		{
			if(this.prev == null)
			{
				this.origional_fixedTime = prm.arrival;
				this.fixedTime = prm.arrival;
			}
		}
		if(prm.departingPlane)
		{
			if(this.next == null)
			{ // This is the depart segment
				this.origional_fixedTime = prm.deadline - this.lenght;
				this.fixedTime = this.origional_fixedTime;
				this.setFixedTime(this.fixedTime);
			}
		}
		
		// TODO: Check or more resets are neccesairy.
		
		this.checkValid();
	}
	
	@Override
	/**
	 * Drops the fixed time. However if it was origionally fixed in time it stays fixed.
	 */
	public void dropFixedTime()
	{
		if(!this.origional_fixed)
		{
			super.dropFixedTime();
		}
	}
	
	public M4_PRM_SegmentGroup(M4_PRM prm, M4_PRM_Segment[] segments)
	{
		this(prm,segments, false, -1);
	}
	
	public M4_PRM_SegmentGroup(M4_PRM prm, M4_PRM_Segment[] segs, boolean fixed,
			int fixedTime, boolean c) 
	{
		this(prm, segs, fixed, fixedTime);
		this.fixStart = c;
	}

	public boolean isFixed()
	{
		return this.fixed;
	}
	
	public int getErliestStart()
	{
		if(this.fixed)
			return fixedTime;
		
		if(this.prev == null)
		{
			return  Math.max(this.segments[0].segment.getEarliestStart(), timeWindowEarliest_Start);
		}
		else
		{
			if(this.prev.isPlanned())
			{
				return Math.max(this.prev.getStart() + this.prev.lenght, timeWindowEarliest_Start);
			}
			else
			{
				return Math.max( Math.max(this.segments[0].segment.getEarliestStart(), timeWindowEarliest_Start),
						         this.prev.getErliestStart() + this.prev.lenght );
			}
		}
	}
	
	public int getLatestStart()
	{	
		if(this.fixed)
			return fixedTime;
		
		
		if(this.next == null)	
		{
			return this.segments[0].segment.getLatestStart();
		}
		else
		{
			if(this.next.isPlanned())
			{
				return this.next.getPlannedGroup().getStart() - this.lenght;
			}
			else
			{
				return Math.min(	this.segments[0].segment.getLatestStart(),
									this.next.getLatestStart() - this.lenght);
			}
		}
	}

	public boolean isMerged() 
	{
		return this.mergedGroup != null;
	}

	public void setStart(int s) throws CantPlanException
	{
		if(this.getStart() == s)
			return;
		
		super.setStart(s);
		
		for(M4_PRM_Segment seg : this.segments)
		{
			seg.getPlannedSegment().setStart(s + seg.getOriSegmentGroupOffset());
		}
	}

	@Override
	public void setPlanned(boolean p)
	{
		if(this.isPlanned())
		{
			if(!p)
			{
				this.prm.planning.plannedSegmentGroups.remove(this);
			}
		}
		else if(p)
		{
			this.suspended = false;
			this.prm.planning.plannedSegmentGroups.add(this);
		}
		
		super.setPlanned(p);
	}
	
	public Iterator<M4_PRM_Segment> iterator() 
	{
		return new ArrayIterator<M4_PRM_Segment>(this.segments);
	}

	public M4_MergedSegmentGroup getMergedGroup() 
	{
		return this.mergedGroup;
	}

	public M4_SegmentGroup getPlannedGroup() 
	{
		return (this.mergedGroup == null ? this : this.mergedGroup);
	}
	
	@Override
	public void testConsistancy()
	{
		super.testConsistancy();
		
		if(this.mergedGroup != null)
		{
			if(this.isPlanned() != this.mergedGroup.isPlanned())
			{
				throw new Error("SegmentGroup "+id+" and Merged SegmentGroup "+mergedGroup.id+" are out of sync in planned: "+this.isPlanned()+" != "+this.mergedGroup.isPlanned());
			}
			
			this.mergedGroup.checkAllMemberships();
		}
	}
	
	public String toString()
	{
		return "[PRM_SegmentGroup "+id+" "+this.isPlanned()+" "+this.isMerged()+"]";
	}
	
	@Override
	protected void doBacktrack()
	{
		if(mergedGroup != bt_mergedGroup)
		{
			if(mergedGroup != null)
				mergedGroup.segmentGroups.remove(this);
			
			if(bt_mergedGroup != null)
				bt_mergedGroup.segmentGroups.add(this);
		}
		
		final boolean bf_planned = this.isPlanned();
		
		super.doBacktrack();
		
		if(this.isPlanned())
		{
			if(!bf_planned)
			{
				this.prm.planning.plannedSegmentGroups.add(this);
			}
		}
		else if(bf_planned)
		{
			this.prm.planning.plannedSegmentGroups.remove(this);
		}
			
		mergedGroup			= bt_mergedGroup;
		mergedGroupOffset 	= bt_mergedGroupOffset;
	}
	
	public void setMergedSegmentGroup(M4_MergedSegmentGroup msg)
	{
		if(this.mergedGroup != null)
			this.mergedGroup.segmentGroups.remove(this);
		
		if(msg != null)
			msg.segmentGroups.add(this);
		
		this.mergedGroup = msg;
		this.registerChange();
	}
	
	public void setMergedSegmentGroup(M4_MergedSegmentGroup msg, int offset)
	{
		if(this.mergedGroup != null)
			this.mergedGroup.segmentGroups.remove(this);
		
		if(msg != null)
			msg.segmentGroups.add(this);
		
		this.mergedGroup = msg;
		this.mergedGroupOffset = offset;
		this.registerChange();
	}
	
	@Override
	protected void doMakeCheckpoint()
	{
		super.doMakeCheckpoint();
		
		bt_mergedGroup			= mergedGroup;
		bt_mergedGroupOffset 	= mergedGroupOffset;
	}
	
	public int getMergedOffset()
	{
		if(this.mergedGroup == null)
			return 0;
		else
			return this.mergedGroupOffset;
	}

	/**
	 * Unsavely set this objects mergedSegmentGroup to null, without checking out with his current merged Group.
	 */
	protected void unSaveRemoveMergedSegmentGroup() 
	{
		this.mergedGroup = null;
	}

	public void unPlan() throws CantPlanException
	{	
		// If this is a member of a segmentGroup remove it from the group:
		if(this.mergedGroup != null)
		{
			this.mergedGroup.removeSegmentGroup(this);
		}
		
		this.setPlanned(false);
		
		// Unplann all segments:
		for(M4_PRM_Segment seg : this.segments)
		{
			seg.unPlan();
		}
	}

	public String printRoute() 
	{
		String route = "";
		
		for(M4_PRM_Segment seg : this.segments)
		{
			route += "["+seg.id+" "+seg.isPlanned()+"]";
		}
		
		return route;
	}

	@Override
	public void mergeWith(M4_PRM_SegmentGroup segmentGroup) throws CantPlanException
	{
		if(this.mergedGroup != null)
		{
			mergedGroup.mergeWith(segmentGroup);
		}
		else if(segmentGroup.mergedGroup != null)
		{
			segmentGroup.mergedGroup.mergeWith(this);
		}
		else
		{
			new M4_MergedSegmentGroup(this,segmentGroup, segmentGroup.getStart() - this.getStart());
		}
	}
	
	public void makePlanOrder()
	{
		if(this.fixStart)
		{
			this.planorder = segments;
		}
		else
		{
			this.planorder = new M4_PRM_Segment[this.segments.length];
			for(int i = 0 ; i < this.segments.length ; i++)
			{
				this.planorder[this.planorder.length - 1 - i ] = this.segments[i];
			}
		}
	}

	public void setWindowTresshold(int time) 
	{
		this.timeWindowEarliest_Start = time;
	}
	
	@Override
	public void setValid(boolean b)
	{
		if(this.valid != b)
		{
			if(b)
			{
				this.planning.invalidSegmentGroups.remove(this);
			}
			else
			{
				this.planning.invalidSegmentGroups.add(this);
			}
		}
		super.setValid(b);
	}

	public void setSuspended() throws CantPlanException
	{
		this.suspended = true;
		this.unPlan();
	}

	public M4_PRM_SegmentGroup getNext() 
	{
		return this.next;
	}

	@Override
	public void unfix() 
	{
		this.fixed = this.origional_fixed;
		this.fixedTime = this.origional_fixedTime;
		
		for(M4_PRM_Segment seg : this.segments)
		{
			seg.unfix();
		}
		
	}
}
