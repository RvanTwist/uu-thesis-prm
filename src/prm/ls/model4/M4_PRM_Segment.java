package prm.ls.model4;

import prm.ls.resources.ParalelResourceNode;
import prm.ls.resources.ParalelResourceSegment;
import prm.problemdef.LocationAlias;
import prm.problemdef.ScheduledSegment;
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
public class M4_PRM_Segment extends M4_Segment
{
	// Unchangeable data
	public final M4_PRM prm;
	M4_PRM_SegmentGroup 	segmentGroup;
	
	// Changeable data
	protected M4_MergedSegment 	mergedWith;
	int					segmentGroupOffset;
	
	// Backtrack data
	protected M4_MergedSegment 	bt_mergedWith;
	private int					bt_segmentGroupOffset;
	
	// Cap distribution
	ParalelResourceSegment prSegment;
	
	// Best data:
	ScheduledSegment bestScheduling;
	public M4_PRM_Segment nexPRMsegment;
	public M4_PRM_Segment prevPRMsegment;
	
	public M4_PRM_Segment(Segment s, M4_PRM prm, M4_Area area) 
	{
		super(s, area);
		this.prm = prm;
		
		bestScheduling = prm.planning.bestSollution.getScheduling(s);
		
		if(M4_Constants.useParalelResourceScore)
		{
			this.prSegment = new ParalelResourceSegment(area.resource,s.prm.capRequirement, s.segmentTime);
		}
	}
	
	@Override
	public int getLatestStart()
	{
		int latestStart = this.segmentGroup.getLatestStart() + this.segmentGroupOffset;
		
		if(  this.nexPRMsegment != null &&
				this.nexPRMsegment.getPlannableSegment().getWorker() != null)
		{
			latestStart = Math.min(	this.nexPRMsegment.getStartTime() - segment.segmentTime,
									latestStart );
		}
		
		final M4_Segment prevnext;
		final M4_Segment nextPlanned;
		if	(	this.prevPRMsegment 										!= null &&
				(prevnext    = this.prevPRMsegment.getNext()) 				!= null &&
				(nextPlanned = prevnext.getPlannableSegment()).getWorker() 	!= null		)
		{
			latestStart = Math.min(	nextPlanned.latestDepartureFrom(this.prevPRMsegment.segment.to),
									latestStart );
		}
		
		return latestStart;
	}
	
	@Override
	public int getEarliestStart()
	{
		int earleestStart = this.segmentGroup.getErliestStart() + this.segmentGroupOffset;
		
		if(this.prevPRMsegment != null &&
		   this.prevPRMsegment.getPlannableSegment().getWorker() != null)
		{
			earleestStart = Math.min(	this.prevPRMsegment.getEndTime(),
										earleestStart );
		}
		
		return earleestStart;
	}

	public M4_MergedSegment getMergedWith()
	{
		return mergedWith;
	}
	
	public M4_Segment getPlannedSegment()
	{
		return (mergedWith == null ? this : mergedWith);
	}
	
	public int getOriSegmentGroupOffset()
	{
		return this.segmentGroupOffset;
	}
	
	@Override
	public void setWorker(M4_Worker w) throws CantPlanException
	{
		if(w != null && this.mergedWith != null)
		{
			throw new Error("This shouldn't be happening!");
		}
		
		super.setWorker(w);
	}
	
	public void setPrimarySegmentGroup(M4_PRM_SegmentGroup sg, int i) throws CantPlanException
	{
		if(this.segmentGroup != null)
		{
			throw new Error("This Segment already haves a segmentGroup!");
		}	
		
		this.segmentGroup = sg;
		this.segmentGroupOffset = i;
		
		this.setStart(sg.getStart() + i);
	}
	
	/**
	 * Returns the current Plannable SegmentGroup.</br>
	 * If this object's SegmentGroup is merged return the merged one.
	 * @return
	 */
	public M4_SegmentGroup getSegmentGroup()
	{
		final M4_MergedSegmentGroup msg = this.segmentGroup.getMergedGroup();
		return (msg == null ? this.segmentGroup : msg);
	}
	
	public boolean isMemberOfCombinedSegmentGroup()
	{
		return this.segmentGroup.isMerged();
	}
	
	@Override
	public M4_MergedSegment mergeWith(M4_PRM_Segment seg) throws CantPlanException
	{
//		System.out.println("MergeWith: "+this+" with "+seg);
		
		final M4_Worker worker = this.getWorker();
		final M4_Worker workerSeg = seg.getWorker();
		
//DEBUG*/  if(worker != null) { System.out.println(" this  worker route: "+this.getWorker().routeDebugString()); }
//DEBUG*/  if(workerSeg != null)  { System.out.println(" other worker route: "+seg.getWorker().routeDebugString()); }
		
//DEBUG*/  if(worker != null) { worker.checkConsistancy(); }
//DEBUG*/  if(workerSeg != null)  { workerSeg.checkConsistancy(); }
		
		if(this.mergedWith != null)
		{
			return this.mergedWith.mergeWith(seg);
		}
		
		if(seg.mergedWith != null)
		{
			return seg.mergeWith(this);
		}
		
		final M4_Segment 	prev 	= this.getPrevious();
		final M4_Segment 	next 	= this.getNext();
		final boolean 		planned = this.isPlanned();
		final int 			start 	= this.getStartTime();
		
		final M4_MergedSegment merged = new M4_MergedSegment(this.segment, this.area);
		
		this.unPlan();
		seg.unPlan();
		
		// Initialise the segment;
		merged.setStart(start);
		merged.setWorker(worker);
		merged.setPlanned(planned);
		
		if(planned)
		{
			// Detach this segment and put it on the merged one
			merged.setPrevious(prev);
			merged.setNext(next);
		}
		
		merged.add(this);
		merged.add(seg);
		
//DEBUG*/  if(worker != null) { worker.checkConsistancy(); }
//DEBUG*/  if(workerSeg != null)  { workerSeg.checkConsistancy(); }
		
		return merged;
	}

	@Override
	public boolean canMergeWith(M4_PRM_Segment prmSeg) 
	{
		if(prmSeg == this)
			return false;
		
		if(this.isFixedWorker() || prmSeg.isFixedWorker())
		{
			return false;
		}
		
		if(this.mergedWith != null)
		{
			return this.mergedWith.canMergeWith(prmSeg);
		}
		
		if(prmSeg.mergedWith != null)
		{
			return prmSeg.mergedWith.canMergeWith(this);
		}
		
		Segment s1 = this.segment;   
		Segment s2 = prmSeg.segment; 
		
		if(prmSeg.isFixedWorker() && this.isPlanned() && this.getWorker() != prmSeg.getFixedWorker())
			return false;
		
		if(this.isFixedWorker() && prmSeg.isPlanned() && prmSeg.getWorker() != this.getFixedWorker())
			return false;
		
		return 	this.getStartTime() == prmSeg.getStartTime() &&               
				s1.supervisingArea == s2.supervisingArea && 
				s1.from == s2.from &&                       
				s1.to == s2.to &&                           
				this.segment.prm.capRequirement + prmSeg.segment.prm.capRequirement <= this.area.minCap_workers;
	}
	
	public String toString()
	{
		return 	"[PRM_Seg "+this.id+" "+this.getWorker()+
				" ["+this.segment.supervisingArea.id+":"+this.segment.from.getLocationId()+"=>"+this.segment.to.getLocationId()+"]"+
				" time: "+this.getStartTime()+" end: "+this.getEndTime()+" planned:"+this.isPlanned()+
				" merged: "+(this.mergedWith != null)+"]";
	}

	public M4_PRM_SegmentGroup getOriSegmentGroup() 
	{
		return this.segmentGroup;
	}
	
	@Override
	public M4_Segment getPlannableSegment() 
	{
		return (this.mergedWith == null ? this : this.mergedWith);
	}
	
	@Override
	public void setStart(int start) throws CantPlanException
	{
		super.setStart(start);
		
		if(M4_Constants.useParalelResourceScore && this.isPlanned())
		{
			this.prSegment.planAt(start);
		}
	}
	
	@Override
	public void setPlanned(boolean planned)
	{
		super.setPlanned(planned);
		
		if(this.prSegment != null)
		{
			if(planned)
			{
				this.prSegment.planAt(this.getStartTime());
			}
			else
			{
				this.prSegment.unPlan();
			}
		}
	}
	
	@Override
	public void setFixedTime(boolean f)
	{
		super.setFixedTime(true);
		
		this.segmentGroup.setFixedTime(this.getStartTime() - this.segmentGroupOffset);
	}
	
	@Override
	public void setFixedWorker(boolean f)
	{
		super.setFixedWorker(f);
		
		if(f && this.isMerged())
		{
			this.fixed_worker = this.getMergedWith().getWorker();
		}
	}
	
	@Override
	protected void doBacktrack() 
	{	
		if(mergedWith != bt_mergedWith)
		{
			if(mergedWith != null)
			{
				mergedWith.segments.remove(this);
			}
			
			super.doBacktrack();
			
			if(bt_mergedWith != null)
			{
				bt_mergedWith.segments.add(this);
			}
		}
		else
		{
			super.doBacktrack();
		}
		
		mergedWith			= bt_mergedWith;
		segmentGroupOffset	= bt_segmentGroupOffset;
		
		if(this.prSegment != null)
		{
			if(this.isPlanned())
			{
				prSegment.planAt(this.getStartTime());
			}
			else
			{
				prSegment.unPlan();
			}
		}
	}

	@Override
	protected void doMakeCheckpoint() 
	{	
		super.doMakeCheckpoint();
		
		bt_mergedWith			= mergedWith;
		bt_segmentGroupOffset	= segmentGroupOffset;
	}
	
	@Override
	public M4_Worker getWorker()
	{
		if(this.mergedWith == null)
		{
			return super.getWorker();
		}
		else
		{
			return this.mergedWith.getWorker();
		}
	}

	public boolean isMerged() 
	{
		return this.mergedWith != null;
	}

	public String currString() 
	{
		return "(PRM"+super.currString()+" "+(mergedWith == null ? null : mergedWith.id)+")";
	}
	
	public String backTrackString() 
	{
		return "(PRM"+super.backTrackString()+" "+(bt_mergedWith == null ? null : bt_mergedWith.id)+")";
	}
	
	public boolean isPlannedAndPrimary()
	{
		if(this.isPlanned())
		{
			if(this.mergedWith != null)
			{
				if(this.mergedWith.segments.first() == this)
					return true;
				else
					return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean isVeryEffectivientNext(M4_Segment seg)
	{
		if(this.mergedWith != null)
		{
			return this.mergedWith.isVeryEffectivientNext(seg);
		}
		
		seg = seg.getPlannableSegment();
		
		if(seg instanceof M4_PRM_Segment)
		{
			if(this.getEndTime() == seg.getStartTime() && this.segment.to == seg.segment.from)
			{
				return ((M4_PRM_Segment)seg).prm == this.prm;
			}
		}
		else if(seg instanceof M4_MergedSegment)
		{
			if(this.getEndTime() == seg.getStartTime() && this.segment.to == seg.segment.from)
			{
				for(M4_PRM_Segment seg2 : ((M4_MergedSegment)seg).segments)
				{
					if(seg2.prm == this.prm)
						return true;
				}
			}
		}
		
		return false;
	}
	
	public void updateBest()
	{
		//M4_Segment seg = this.getPlannableSegment();		
		//M4_Worker w = seg.getBestWorker();
		
		M4_Segment seg = (this.bt_mergedWith != null ? this.bt_mergedWith : this );
		M4_Worker w = ( this.bt_mergedWith != null 	? this.bt_mergedWith.getBestWorker()
													: this.getBestWorker()				);
		
		if(w == null && this.prm.bt_planned)
		{
			String PRM_segments = "Debug PRM: ";
			for(M4_Segment prm_seg : this.prm.segments)
			{
				PRM_segments +="\n  # "+prm_seg +" BT: "+prm_seg.getBestWorker();
			}
			
			throw new Error("Something goes wrong with saving! \n"+
							"  segment: "+this+"\n"+
							"p-segment: "+seg+"\n"+
							"worker: "+w+"\n"+
							PRM_segments+"\n"+
							"Last Mutation: "+this.prm.planning.solver.lastMutation.getClass().getSimpleName()+"\n"+
							"Data: "+this.prm.planning.solver.lastMutation);
		}
		
		if(w == null)
		{
			this.bestScheduling.transport = null;
			this.bestScheduling.delivery_time = 0;
			this.bestScheduling.pickup_time = 0;
		}
		else
		{
			this.bestScheduling.transport = w.transport;
			this.bestScheduling.pickup_time = seg.getBestStartTime();
			this.bestScheduling.delivery_time = seg.getBestEndTime();
		}
	}

	public boolean loadSegment(ScheduledSegment plannedSeg) throws CantPlanException
	{
		if(plannedSeg.transport == null)
		{
			return false;
		}
		
		else if(!this.segmentGroup.isPlanned())
		{
			this.segmentGroup.setStart(plannedSeg.pickup_time-this.segmentGroupOffset);
			this.segmentGroup.setPlanned(true);
		}
		
		// Find the worker:
		M4_Worker worker = this.area.getWorker(plannedSeg.transport);
		
		if(worker == null)
		{
			String workers = "";
			for(M4_Worker w : this.area.workers)
			{
				workers += " "+w.transport.transport_ID;
			}
			
			Segment other = null;
			Segment should_be = null;
			
			for(M4_PRM prm : this.area.planning.prms)
			{
				for(Segment seg : prm.prm.route)
				{
					if(seg.segmentId == this.segment.segmentId)
					{
						if(seg != this.segment)
							other = seg;
						else
							should_be = seg;
					}
				}
			}
			
			throw new Error("Could not load worker!\n"+
							"Expected: "+plannedSeg.transport.transport_ID+"\n"+
							"In area: "+workers+"\n"+
							"Area: "+this.area.area.id+"\n"+
							"Segment: ("+this.segment.original_segmentId+") "+this+"\n"+
							"PRM: "+this.prm+"\n"+
							"Other segment: "+other+"\n"+
							"Should be    : "+should_be+"\n");
		}
		
		worker.loadSegment(this, plannedSeg.pickup_time);
		
		return true;
	}
	
	@Override
	public void unPlan() throws CantPlanException
	{		
		if(this.mergedWith != null)
		{
			this.mergedWith.remove(this);
		}
		
		super.unPlan();
	}
	
	@Override
	public int getEndTime() 
	{
		if(!this.segment.to.isSupervised() && this.nexPRMsegment != null)
		{
			return this.nexPRMsegment.getStartTime();
		}
		
		return super.getEndTime();
	}
	
	public String printTimeWindow() 
	{
		int start;
		int end;
		
		if(this.segmentGroup.isMerged())
		{
			M4_MergedSegmentGroup mg = this.segmentGroup.getMergedGroup();
			start = mg.getErliestStart() + this.segmentGroupOffset + this.segmentGroup.getMergedOffset();
			end =   mg.getLatestStart() + this.segmentGroupOffset + this.segmentGroup.getMergedOffset();
		}
		else
		{
			start = this.segmentGroup.getErliestStart() + this.segmentGroupOffset;
			end = this.segmentGroup.getLatestStart() + this.segmentGroupOffset;
		}
		
		return "["+start+","+end+"]";
	}

	public void unfix() 
	{
		this.fixed_time = false;
		this.fixed_worker = null;
		this.is_fixed_worker = false;
	}
}
