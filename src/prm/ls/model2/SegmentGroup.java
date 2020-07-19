package prm.ls.model2;

import prm.ls.SolutionFacture;
import prm.ls.resources.ParalelResource;

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
public class SegmentGroup 
{
	PlannedSegment[] segments;
	int[] offset;
	int lenght;
	
	SegmentGroup prev;
	SegmentGroup next;
	
	boolean fixed;
	int fixedTime;
	int start;
	
	final PRM_Planning prm;
	
	public SegmentGroup(PRM_Planning prm, PlannedSegment[] segments, boolean fixed, int fixedTime)
	{
		this.prm = prm;
		
		if(segments.length == 0)
			throw new Error("Debug");
		
		this.segments = segments;
		this.fixed = fixed;
		this.fixedTime = fixedTime;
		
		this.lenght = 0;
		this.offset = new int[segments.length];
		
		for(int i = 0 ; i < this.offset.length ; i++)
		{
			offset[i] = lenght;
			lenght += segments[i].segment.segmentTime;
		}
	}
	
	public SegmentGroup(PRM_Planning prm,PlannedSegment[] segments)
	{
		this(prm,segments, false, -1);
	}
	
	/**
	 * Attempt to 'optimise this object by merging 2 subsequent travels throug one area, can be done by the current helper anyways.'
	 */
	public void optimiseMerge()
	{
		ParalelResource prev = null;
		int merged = 0;
		for(int i = 0 ; i < segments.length ; i++)
		{
			final PlannedSegment ps = this.segments[i];
			if(ps.getResource() == prev)
			{
				
			}
			prev = ps.getResource();
		}
	}
	
	public void planAt(int time)
	{
		if(this.fixed)
		{
			time = this.fixedTime;
		}
		
		if(this.prev != null && time < this.prev.start + prev.lenght)
		{
			throw new Error("This object can't start before the previous segmentGroup is finished!");
		}
		if(this.next != null && time + this.lenght > next.start)
		{
			throw new Error("This segment can't finish before the next segmentGroup have started!");
		}
		
		this.start = time;
		
//		System.out.println( "SG start at "+time+" "+this.segments.length +"("+this.getErliestStart()+","+this.getLatestStart()+") "+
//							"("+this.segments[0].segment.getEarliestStart()+","+this.segments[0].segment.getLatestStart()+")");
		
		for(PlannedSegment ps : this.segments)
		{
			ps.unPlan();
		}
		
		for(int i = 0 ; i < this.segments.length ; i++)
		{
			PlannedSegment s = this.segments[i];
			s.planAt(time + offset[i]);
		}
	}
	
	public int getCostsMove(int newStart)
	{
		int costs = 0;
		
		for(int i = 0 ; i < this.segments.length ; i++)
		{
			PlannedSegment s = this.segments[i];
			costs += s.getCostsMove(newStart);
		}
		
		return costs;
	}
	
	public void getCostsMove(int newStart, SolutionFacture f)
	{
		for(int i = 0 ; i < this.segments.length ; i++)
		{
			PlannedSegment s = this.segments[i];
			s.getCostsMove(newStart+offset[i], f);
		}
	}
	
	public boolean isFixed()
	{
		return this.fixed;
	}
	
	public int getErliestStart()
	{
		if(this.fixed)
			return fixedTime;
		
		return (this.prev == null ? this.segments[0].segment.getEarliestStart() : this.prev.start + this.prev.lenght);
	}
	
	public int getLatestStart()
	{
		if(this.fixed)
			return fixedTime;
		
		return (this.next == null ? this.segments[0].segment.getLatestStart() : this.next.start - this.lenght);
	}

	public boolean isPlanned() 
	{
		return this.prm.planned;
	}
	
	public String printPlanning()
	{
		String s = "SegmentGroup start: "+start+(fixed ? "(fixed) " : "");
		for(PlannedSegment ps : segments)
		{
			s += "\n "+ps.start+" "+ps.segment.toString()+"resource: "+ps.getResource().toString();
		}
		
		return s;
	}
	
	public void setFixedTime(int time)
	{
		this.fixed = true;
		this.fixedTime = time;
	}
	
	public void dropFixedTime()
	{
		this.fixed = false;
		this.fixedTime = -1;
	}
}
