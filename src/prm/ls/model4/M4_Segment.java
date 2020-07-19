package prm.ls.model4;

import java.util.TreeMap;
import java.util.TreeSet;

import gurobi.GRBConstr;
import prm.ls.AbstractBackTrackable;
import prm.ls.Mutation;
import prm.problemdef.*;
import rpvt.lp.ConstraintInterface;
import rpvt.lp.VariableInterface;

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
public class M4_Segment extends AbstractBackTrackable implements Comparable<M4_Segment>
{	
	// Statics
	private static int id_dispenser = 0;
//	public static int id_Tracking = 1009;
	
	// unchangeable data:
	public final Segment segment;
	public final M4_Area area;
	final public int id;
	
	// Dynamic data:
	protected 	int 		start;
	private 	M4_Worker 	worker;
	private 	M4_Segment 	previous;
	private 	M4_Segment 	next;
	private 	boolean 	planned = false;
	
	// Unbacktrackable dynamic data.
	protected 	boolean		fixed_time = false;
	protected 	boolean		is_fixed_worker = false;
	protected 	M4_Worker	fixed_worker = null;
	
	public int robustnessPanelty = 0;
	
	// Backtrack data
	private int 		bt_start;
	private M4_Worker 	bt_worker;
	private M4_Segment 	bt_previous;
	private M4_Segment 	bt_next;
	private boolean 	bt_planned = false;
	private int bt_robustnessPanelty;
	
	// Backtrack data2
	private M4_Worker 	bt_worker2;
	private M4_Segment 	bt_previous2;
	private M4_Segment 	bt_next2;
	private int bt_robustnessPanelty2;
	//	private boolean 	bt_planned = false;
	
	// LP
	public ConstraintInterface c1;
	public ConstraintInterface c2;
	
	public TreeMap<M4_Worker,VariableInterface> vars = new TreeMap<M4_Worker,VariableInterface>(); 
	
	public int local_Id = -1; // Used by M4_CompleteFlowMatching
	public boolean match_changed = false;

	public GRBConstr grb_c1;
	public GRBConstr grb_c2;
	
	public VariableInterface endTime;
	
	public TreeSet<M4_Worker> feasibleTransports = new TreeSet<M4_Worker>();

	protected int fixedtime2;
	
	public M4_Segment(Segment s, M4_Area area)
	{
		super(area.planning.backtracker);
		
		this.segment = s;
		this.area = area;	
		
		this.id = M4_Segment.id_dispenser;
		M4_Segment.id_dispenser++;
		
		//this.updateFeasibleTransports();
	}
	
	public void updateFeasibleTransports()
	{	
		this.feasibleTransports.clear();
		
		for(M4_Worker t : area.workers)
		{
			this.start = this.segment.getEarliestStart();
			final int start_e = this.latestDepartureFrom(t.transport.depot);
			final int end_e  = this.earliestArriveAt(t.transport.depot);
			
			this.start = this.segment.getLatestStart();
			final int start_l = this.latestDepartureFrom(t.transport.depot);
			final int end_l = this.earliestArriveAt(t.transport.depot);
				
			if(! ( (start_l < t.transport.startShift) || (end_e > t.transport.endShift) ))
			{
				feasibleTransports.add(t);
				//System.out.println("Added worker "+t.getTransport().transport_ID+" ["+start_e+","+end_l+"]");
			}
			else
			{
//				System.out.println("Rejected worker "+t.getTransport().transport_ID+" ["+start_e+","+end_e+","+start_l+","+end_l+
//						"] vs ["+t.getTransport().startShift+","+t.getTransport().endShift+"]");
//				System.out.println("Segmentinfo: "+this.segment);
			}
		}
	}

	public boolean canMergeWith(M4_PRM_Segment prmSeg) 
	{
		return false;
	}

	@Override
	public int compareTo(M4_Segment other) 
	{
		return this.id - other.id;
	}

	@Override
	protected void doBacktrack() 
	{
		// Debug
//		if(this.id == id_Tracking)
//		{
//			System.out.println("Back track ("+this.id+") "+this.robustnessPanelty+" => "+this.bt_robustnessPanelty );
//		}
		
		if(worker != bt_worker)
		{
			if(this.worker != null)
				worker.plannedSegments.remove(this);
			
			this.start = bt_start;
			
			if(this.bt_worker != null)
				bt_worker.plannedSegments.add(this);
		}
		else if(start != bt_start)
		{
			if(this.worker != null)
				worker.plannedSegments.remove(this);
			
			this.start = bt_start;
			
			if(this.bt_worker != null)
				bt_worker.plannedSegments.add(this);
		}
		
		start 		= bt_start;
		worker 		= bt_worker;
		previous 	= bt_previous;
		next 		= bt_next;
		planned 	= bt_planned;
		
		robustnessPanelty = bt_robustnessPanelty;
	}
	
	public void makeScheduleCheckpoint() 
	{	
		bt_worker2 	= worker;
		bt_previous2 = previous;
		bt_next2 	= next;
		
		bt_robustnessPanelty = robustnessPanelty;
	}
	
	public void backTrackSchedule() 
	{	
		if(worker != bt_worker2)
		{
			if(this.worker != null)
				worker.plannedSegments.remove(this);
			
			if(this.bt_worker2 != null)
				bt_worker2.plannedSegments.add(this);
		}
		
		worker 		= bt_worker2;
		previous 	= bt_previous2;
		next 		= bt_next2;
		
		final int change = bt_robustnessPanelty2 - robustnessPanelty;
		this.area.planning.robustnessPanelty += change;
		
		robustnessPanelty = bt_robustnessPanelty2;
		
//		if(this.id == id_Tracking)
//		{
//			System.out.println("Do Back track schedule("+this.id+") "+this.robustnessPanelty+" => "+this.bt_robustnessPanelty );
//		}
	}

	@Override
	protected void doMakeCheckpoint() 
	{	
		bt_start 	= start;
		bt_worker 	= worker;
		bt_previous = previous;
		bt_next 	= next;
		bt_planned 	= planned;
		bt_robustnessPanelty = robustnessPanelty;
	}
	
	public int getCapicityRequirement()
	{
		return this.segment.prm.capRequirement;
	}
	
	public int getEndTime() 
	{
		return this.start + segment.segmentTime;
	}

	public M4_Segment getNext()
	{
		return this.next;
	}

	public M4_Segment getPlannableSegment() 
	{
		return this;
	}

	public M4_Segment getPrevious()
	{
		return this.previous;
	}
	
	public int getStartTime() 
	{
		return start;
	}
	
	public M4_Worker getWorker()
	{
		return this.worker;
	}
	
	public M4_Worker getFixedWorker()
	{
		return this.fixed_worker;
	}
	
	public boolean isPlanned() 
	{
		return this.planned;
	}
	
	public M4_MergedSegment mergeWith(M4_PRM_Segment seg)  throws CantPlanException
	{
		throw new Error("Can't merge with this segment!");
	}

	public void setNext(M4_Segment n)
	{
		
		// Debug
//		if(this.id == id_Tracking)
//		{
//			System.out.println("Set next("+this.id+"): "+n);
//		}
//		if(n != null && n.id == id_Tracking )
//		{
//			System.out.println("Set next target ("+n.id+"): "+this);
//		}
		
		if(n != null && n.start < this.start)
		{
			Transport t = this.worker.transport;
			
			throw new Error("Cannot set this as next!\n"+
								" this: "+this+"\n"+
								" next: "+n+"\n"+
								" t w:  ["+t.startShift+","+t.endShift+"]");
		}
		
		if(this.next != null)
		{
			this.next.previous = null;
			this.next.registerChange();
			this.next.updateRobustnessPanelty();
		}
		
		this.next = n;
		if(n != null)
		{
			if(n.previous != null)
			{
				n.previous.next = null;
				n.previous.registerChange();
			}
			
			n.previous = this;
			n.registerChange();
			n.updateRobustnessPanelty();
		}
		
		this.registerChange();
	}

	public void setPlanned(boolean p)
	{
//		System.out.println("setPlanned "+p+" : "+this);
		
//		if(this instanceof M4_MergedSegment)
//			throw new Error(" where?");
		
		this.planned = p;
		
		this.registerChange();
	}

	public void setPrevious(M4_Segment p)
	{
		// Debug
//		if(this.id == id_Tracking)
//		{
//			System.out.println("Set prev("+this.id+"): "+p);
//		}
//		if(p != null && p.id == id_Tracking )
//		{
//			System.out.println("Set prev target ("+p.id+"): "+this);
//		}
		
		
		if(this.previous != null)
		{
			this.previous.next = null;
			this.previous.registerChange();
		}
		
		this.previous = p;
		if(p != null)
		{
			if(p.next != null)
			{
				p.next.previous = null;
				p.next.registerChange();
				p.next.updateRobustnessPanelty();
			}
			
			p.next = this;
			p.registerChange();
		}
		
		this.updateRobustnessPanelty();
		this.registerChange();
	}
	
	public void setStart(int s)  throws CantPlanException
	{
		if(s != this.start)
		{ 
			if(worker != null)
			{
				throw new CantPlanException("Need to detach this segment first before changing the time! old time: "+this.start+" new time: "+s);
			}
		}
		this.start = s;
		
		this.registerChange();
	}
	
	public int getFixedTime() 
	{	
		return this.fixedtime2;
	}
	
	public void setWorker(M4_Worker w) throws CantPlanException
	{
		if(w == this.worker)
			return;
		
//		if(w != null && this.start < this.area.planning.minRescheduleTime)
//		{
//			System.err.println("Warning: Tried planning in the past! "+this.start+"/"+this.area.planning.minRescheduleTime+" ["+this.getEarliestStart()+","+this.getLatestStart()+"] "+this.area.planning.solver.lastMutation);
//			throw new CantPlanException("No can't plan in the past! "+this.start+"/"+this.area.planning.minRescheduleTime);
//		}
		
//		System.out.println("set Worker "+this.id+": "+w);
		if(this.isFixedWorker())
		{
			if(w != null && w != this.getFixedWorker() )
			{
				Mutation lastMutation = this.area.planning.solver.lastMutation;
				M4_SegmentGroup sg = (this instanceof M4_PRM_Segment 
										? ((M4_PRM_Segment)this).getOriSegmentGroup()
										: null);
				
				throw new CantPlanException("This segment is fixed and couldn't be planned on another employee.\n"+
								"Segment: "+this+"\n"+
								"SegmentGroup: "+sg+"\n"+
								"lastMutation: "+lastMutation);
			}
		}
		
		
		if(this.worker != null)
		{
			this.worker.plannedSegments.remove(this);
		}
		
		if(w != null)
		{
			w.plannedSegments.add(this);
			
			if(this.isFixedTime() && this.start != this.getFixedTime())
			{
				throw new CantPlanException("This segment is fixed and couldn't be shifted in time");
			}
		}
		
		this.worker = w;
		
		this.registerChange();
	}
	
	public String toString()
	{
		return this.getClass().getSimpleName()+" "+id+" seg:"+segment.toString()+"("+start+")";
	}
	
	public void unPlan()  throws CantPlanException
	{
		if(this.worker != null)
		{
//DEBUG*/	final M4_Worker w = this.worker;
//DEBUG*/	w.checkConsistancy();
			
			if(this.previous != null)
			{
				this.previous.next = this.next;
				this.previous.registerChange();
			}
			
			if(this.next != null)
			{
				this.next.previous = this.previous;
				this.next.registerChange();
				this.next.updateRobustnessPanelty();
			}
			
			this.setPlanned(false);
			this.setWorker(null);
			
			this.previous = null;
			this.next = null;
			
			this.updateRobustnessPanelty();
			
//DEBUG*/	w.checkConsistancy();
		}
	}

	public String currString() 
	{
		return "(SEG "+id+" "+start+" "+(worker == null ? null : worker)+" "+
				(previous == null ? null : previous.id)+
				" "+(next == null ? null : next.id)+" "+planned+")";
	}
	
	public String backTrackString() 
	{
		return "(SEG "+bt_start+" "+(bt_worker == null ? null : bt_worker)+" "+
				(bt_previous == null ? null : bt_previous.id)+
				" "+(bt_next == null ? null : bt_next.id)+" "+bt_planned+")";
	}

	public int earliestArriveAt(LocationAlias loc) 
	{
		return this.getEndTime() + loc.getArea().getDistance(this.segment.to, loc);
	}
	
	public int latestDepartureFrom(LocationAlias loc) 
	{
		return this.start - loc.getArea().getDistance(loc, this.segment.to);
	}

	public void chainUpdateWorkers() throws CantPlanException
	{
		M4_Segment seg = this.next;
		
		while(seg != null)
		{
			seg.setWorker(this.worker);
			seg = seg.getNext();
		}
	}
	
	public boolean isPlannedAndPrimary()
	{
		return this.isPlanned();
	}

	public boolean canServeNext(M4_Segment seg) 
	{
		final int travelTimeDistance;
		final LocationAlias from = this.segment.to;
		final LocationAlias to   = seg.segment.from;
		
		if(from == to)
		{
			travelTimeDistance = 0;
		}
		else
		{
			travelTimeDistance = segment.supervisingArea.getDistance(from, to);
		}
		
		return this.getEndTime() + travelTimeDistance <= seg.getStartTime();
	}
	
	public boolean isVeryEffectivientNext(M4_Segment seg)
	{
		return false;
	}

	public M4_Worker getBestWorker() 
	{
		return bt_worker;
	}
	
	public int getBestStartTime()
	{
		return this.bt_start;
	}
	
	public int getBestEndTime()
	{
	
		if (!this.segment.to.isSupervised() && this instanceof M4_PRM_Segment)
		{
			M4_PRM_Segment next = ((M4_PRM_Segment) this).nexPRMsegment;
			
			if(next != null)
			{ // May not end till the next is delivered
				return next.getBestStartTime();
			}
		}
		
		
		return bt_start + this.segment.segmentTime;
		
	}

	/**
	 * Get the latest start time of this segment, return value is also depending on current planned segments.
	 * @return
	 */
	public int getLatestStart() 
	{
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Get the earliest start time of this segment, return value is also depending on current planned segments.
	 * @return
	 */
	public int getEarliestStart() 
	{
		return Integer.MIN_VALUE;
	}

	public String getSimpleString() 
	{
		return this.getClass().getSimpleName()+" "+this.id;
	}

	public int getPrevSlack() 
	{	
		if(this.previous == null)
			return M4_CostModel.preferedSlack;
		
		if(this.previous.isVeryEffectivientNext(this))
		{
			return M4_CostModel.preferedSlack;
		}
		
		final LocationAlias from = this.previous.segment.to;
		final LocationAlias to = this.segment.from;
		
		final int travel = this.area.area.getDistance(from, to);
		
		return this.start - previous.start - travel; 
	}
	
	public void updateRobustnessPanelty()
	{
		
		
		final int newRP = M4_CostModel.getRobustnessPanelty(this);
		final int change = newRP - this.robustnessPanelty;
		
		// Debug
//		if(this.id == id_Tracking)
//		{
//			System.out.println("Set update Panelty ("+this.id+"): "+this.robustnessPanelty+"=>"+newRP+" ("+change+") prev: "+this.previous );
//		}
		
		this.robustnessPanelty = newRP;
		this.area.planning.robustnessPanelty += change;
		
		
	}
	
	public void setFixedWorker(boolean f)
	{
		this.is_fixed_worker = f;
		if(f)
		{
			this.fixed_worker = this.worker;
		}
	}
	
	public void setFixedTime(boolean f)
	{
		this.fixed_time = f;
		this.fixedtime2 = this.start;
	}

	public String printTimeWindow() 
	{
		return "[-inf,inf]";
	}

	public boolean isFixedWorker() 
	{
		return this.is_fixed_worker;
	}
	
	public boolean isFixedTime() 
	{
		return this.fixed_time;
	}
}
