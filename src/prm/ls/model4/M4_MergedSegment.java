package prm.ls.model4;

import java.util.Iterator;
import java.util.TreeSet;

import prm.ls.model4.matching.lpflow.AreaFlowProblem_IF;
import prm.problemdef.LocationAlias;
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
public class M4_MergedSegment extends M4_Segment implements Iterable<M4_PRM_Segment>
{
	TreeSet<M4_PRM_Segment> segments;
	
	// Dynamic variables
	private int capSum = 0;
	
	// BackTrack variables
	private int bt_capSum = 0;
	private boolean bt_created = true;
	
	public M4_MergedSegment(Segment s, M4_Area area) 
	{
		super(s, area);
		
		segments = new TreeSet<M4_PRM_Segment>();
	}
	
	public void add(M4_PRM_Segment seg) throws CantPlanException
	{
//		System.out.println(" Add "+seg+" to "+this);
		
		if(seg.isPlanned())
		{
			seg.unPlan();
		}
		
		{
			final M4_Worker fw1 = this.getFixedWorker();
			final M4_Worker fw2 = seg.getFixedWorker();
			if(fw1 != null && fw2 != null && fw1 != fw2)
			{
				throw new Error("A merged segment can't have 2 diffrent fixed workers!");
			}
		}
		
		if(this.isPlanned())
		{
			if(seg.isFixedWorker() && this.getWorker() != seg.getFixedWorker())
			{
				throw new Error("Can't add a fixed worker segment to a planned segment that isn't planned on that worker1");
			}
		}
		
		seg.setStart(this.getStartTime());
		seg.setPlanned(this.isPlanned());
		
		capSum += seg.segment.prm.capRequirement;
		
		if(capSum > this.area.minCap_workers)
		{
			throw new Error("The segment required capacity is too great! ("+capSum+"/"+this.area.minCap_workers+")");
		}
		
		if(seg.mergedWith != null)
		{
			throw new Error("Can't merge with something that is already merged!");
		}
		
		seg.mergedWith = this;
		this.segments.add(seg);
		
		seg.registerChange();
		this.registerChange();
		seg.updateRobustnessPanelty();
		
	}
	
	@Override
	public M4_Worker getFixedWorker()
	{
		this.fixed_worker = null;
		
		for(M4_Segment seg : this.segments)
		{
			M4_Worker w = seg.getFixedWorker();
			if(w != null)
			{
				this.fixed_worker = w;
				return w;
			}
		}
		
		return null;
		
	}
	
	@Override
	public void setWorker(M4_Worker w) throws CantPlanException
	{
		this.fixed_worker = this.getFixedWorker();
		super.setWorker(w);
		
		if(w != null)
		{
			for(M4_PRM_Segment seg : this.segments)
			{
				if(seg.isFixedTime() && seg.getStartTime() != seg.getFixedTime())
				{
						throw new Error("This segment is fixed and couldn't be shifted in time");
				}
				if(seg.isFixedWorker() && seg.getFixedWorker() != w)
				{
					throw new Error("Worker is fixed!");
				}
			}
		}
	}
	
	/**
	 * Removes the segment from the merge, if there is only 1 segment left return true;
	 * @param seg
	 * @return
	 * @throws CantPlanException 
	 */
	public  boolean remove(M4_PRM_Segment seg) throws CantPlanException
	{
		this.registerChange();
		seg.registerChange();
		
//		System.out.println("- Removing "+seg+" from "+this);
		
		if(seg.mergedWith != this)
		{
			throw new Error("Can't delete a segement that isn't managed with this object!");
		}
		
		this.capSum -= seg.segment.prm.capRequirement;
		
		seg.mergedWith = null;
		this.segments.remove(seg);
		seg.setWorker(null);
		seg.setPlanned(false);
		
		
		
		if(this.segments.size() <= 1)
		{
			
//			System.out.println(" - Deleting merged segment");
			// Auto handle last Seg
			final M4_PRM_Segment lastSegment = this.segments.first();
			this.capSum = 0;
			this.segments.clear();
			lastSegment.mergedWith = null;
			
			if(this.isPlanned())
			{
//				System.out.println(" - Was planned");
				final M4_Worker worker = this.getWorker();
				
//DEBUG*/ 		worker.checkConsistancy();	
				final M4_Segment prev = this.getPrevious();
				final M4_Segment next = this.getNext();
				
				this.setNext(null);
				this.setPrevious(null);
				
				if(lastSegment.getNext() != null)
				{
					throw new Error("Huh?");
				}
				
				if(lastSegment.getPrevious() != null)
				{
					throw new Error("Huh?");
				}
				
				lastSegment.setPrevious(prev);
				lastSegment.setNext(next);
				
				

				this.setWorker(null);
				lastSegment.setWorker(worker);
				
//DEBUG*/ 		worker.checkConsistancy();	
			}
			else if(this.getWorker() != null)
			{ 
				throw new Error("This shouldn't happen!");
				//this.setWorker(null);
			}
			
			
			seg.updateRobustnessPanelty();
			return true;
		}
		
		seg.updateRobustnessPanelty();
		return false;
	}
	
	@Override
	public M4_MergedSegment mergeWith(M4_PRM_Segment seg) throws CantPlanException
	{	
		final M4_Worker worker = this.getWorker();
		final M4_Worker segWorker = seg.getWorker();
		
//DEBUG*/ 	if(worker != null) { worker.checkConsistancy(); }
//DEBUG*/ 	if(segWorker != null ) { segWorker.checkConsistancy(); }
		
		if(seg.isMerged())
		{ // merged
			M4_MergedSegment mseg = seg.mergedWith;
			
			if(mseg.isPlanned())
			{
				mseg.unPlan();
			}
			
			for(M4_PRM_Segment seg2 : mseg.segments)
			{
				seg2.mergedWith = null;
				this.add(seg2);
			}
			
			mseg.segments.clear();
		}
		else
		{ // Not merged
			
			if(seg.isPlanned())
			{
				seg.unPlan();
			}
			
			this.add(seg);
			
		}
		
//DEBUG*/ 	if(worker != null) { worker.checkConsistancy(); }
//DEBUG*/ 	if(segWorker != null ) { segWorker.checkConsistancy(); }
		
		return this;
	}
	
	@Override
	public boolean canMergeWith(M4_PRM_Segment prmSeg) 
	{
		if(prmSeg.getMergedWith() == this)
			return false;
		
		if(prmSeg.isFixedWorker() || this.isFixedWorker())
			return false;
		
		Segment s1 = this.segment;
		Segment s2 = prmSeg.segment;
		
		final int capReq; 
		
		if(prmSeg.isMerged())
			capReq = prmSeg.mergedWith.capSum;
		else
			capReq = prmSeg.segment.prm.capRequirement;
		
		M4_Segment seg = prmSeg.getPlannableSegment();
		
		if(seg.isFixedWorker() && this.isPlanned() && this.getWorker() != seg.getFixedWorker())
			return false;
		
		if(this.isFixedWorker() && seg.isPlanned() && seg.getWorker() != this.getFixedWorker())
			return false;
		
		return 	this.getStartTime() == prmSeg.getStartTime() &&
				s1.supervisingArea == s2.supervisingArea &&
				s1.from == s2.from &&
				s1.to == s2.to &&
				capSum + capReq <= this.area.minCap_workers;
	}
	
	@Override
	public int getCapicityRequirement()
	{
		return this.capSum;
	}
	
	@Override
	public void setPlanned(boolean planned)
	{
		//System.out.println("setPlanned "+planned+" : "+this);
		super.setPlanned(planned);
		
		for(M4_Segment seg : this.segments)
		{
			seg.setPlanned(planned);
		}
	}
	
	@Override
	public void setStart(int s) throws CantPlanException
	{
		super.setStart(s);
		
		for(M4_Segment seg : this.segments)
		{
			seg.setStart(s);
		}
	}
	
	public String toString()
	{
		String segs = null;
		for(M4_PRM_Segment seg : this.segments)
		{
			if(segs == null)
				segs = "{"+seg.id;
			else
				segs = segs + ","+seg.id;
		}
		
		if(segs == null)
			segs = "{}";
		else
			segs = segs + "}";
		
		return 	"[MErged_Seg "+this.id+": worker: "+this.getWorker()+
				" ["+this.segment.supervisingArea.id+":"+this.segment.from.getLocationId()+"=>"+this.segment.to.getLocationId()+"]"+
				" time: "+this.getStartTime()+" planned:"+this.isPlanned()+
				" merged: "+segments.size()+" "+segs+"]";
	}
	
	@Override
	protected void doBacktrack() 
	{	
		super.doBacktrack();
		capSum = bt_capSum;
		
		if(bt_created)
		{
//			System.out.println("Removing created MergedSegment seg: "+this);
			// This object have to be destroyed from the overviewer
			this.backtracker.remove(this);
		}
	}

	@Override
	protected void doMakeCheckpoint() 
	{	
		super.doMakeCheckpoint();
		
		bt_created = false;
		bt_capSum = capSum;
		
		if(this.segments.size() == 0)
		{ // This object is bound to be destroyed:
//			System.out.println("Removing disbanded MergedSegment seg: "+this);
			this.backtracker.remove(this);
		}
	}

	public M4_PRM_Segment getFirstMerge() 
	{
		return this.segments.first();
	}

	@Override
	public Iterator<M4_PRM_Segment> iterator() 
	{
		return this.segments.iterator();
	}
	
	public boolean isVeryEffectivientNext(M4_Segment seg)
	{
		seg = seg.getPlannableSegment();
		
		if(seg instanceof M4_PRM_Segment)
		{
			if(this.getEndTime() == seg.getStartTime() && this.segment.to == seg.segment.from)
			{
				final M4_PRM_Segment prmSeg = ((M4_PRM_Segment)seg);
				
				for(M4_PRM_Segment seg2 : this.segments)
				{
					return prmSeg.prm == seg2.prm;
				}
			}
		}
		else if(seg instanceof M4_MergedSegment)
		{
			if(this.getEndTime() == seg.getStartTime() && this.segment.to == seg.segment.from)
			{
				final M4_MergedSegment mergedSeg = ((M4_MergedSegment)seg);
				
				for(M4_PRM_Segment seg1 : this.segments)
					for(M4_PRM_Segment seg2 : mergedSeg.segments)
					{
						if(seg1.prm == seg2.prm)
							return true;
					}
			}
		}
		
		return false;
	}
	
	public int getEndTime() 
	{
		int endTime = 0;
		
		if(this.segments == null)
		{
			return 0;
		}
		
		for(M4_PRM_Segment seg : this.segments)
		{
			final int segEnd = seg.getEndTime();
			
			if(segEnd > endTime)
				endTime = segEnd;
		}
		
		return endTime;
	}
	
	@Override
	public void updateRobustnessPanelty()
	{
		for(M4_Segment seg : this.segments)
		{
			seg.updateRobustnessPanelty();
			seg.registerChange();
		}
	}
	
	@Override
	public int getPrevSlack() 
	{	
		throw new Error("Shouldn't be called!");
	}
	
	public String printTimeWindow() 
	{
		int start;
		int end;
		
		{
			M4_PRM_Segment firstSeg = this.segments.first();
			M4_MergedSegmentGroup mg = firstSeg.segmentGroup.getMergedGroup();
			start = mg.getErliestStart() + firstSeg.segmentGroupOffset + firstSeg.segmentGroup.getMergedOffset();
			end =   mg.getLatestStart() + firstSeg.segmentGroupOffset + firstSeg.segmentGroup.getMergedOffset();
		}
		
		return "["+start+","+end+"]";
	}
	
	@Override
	public boolean isFixedWorker() 
	{	
		for(M4_Segment seg : this.segments)
		{
			if(seg.isFixedWorker())
			{
				this.is_fixed_worker = true;
				return true;
			}
		}
		this.is_fixed_worker = false;
		return false;
	}
	
	@Override
	public boolean isFixedTime() 
	{	
		for(M4_Segment seg : this.segments)
		{
			if(seg.isFixedTime())
			{
				this.fixed_time = true;
				return true;
			}
		}
		this.fixed_time = false;
		return false;
	}
	
	@Override
	public int getFixedTime() 
	{	
		for(M4_Segment seg : this.segments)
		{
			if(seg.isFixedTime())
			{
				return seg.getFixedTime();
			}
		}
		return -1;
	}

	public Iterable<M4_PRM_Segment> getSegments() 
	{
		return this.segments;
	}
}
