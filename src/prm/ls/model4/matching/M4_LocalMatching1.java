package prm.ls.model4.matching;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import prm.ls.SolutionFacture;
import prm.ls.model4.*;
import prm.problemdef.Area;
import prm.problemdef.LocationAlias;
import prm.problemdef.Segment;
import prm.problemdef.Transport;

import otheralgorithms.HungarianAlgorithm;
import otheralgorithms.HungarianAlgorithmResizable;

import lpsolve.*;

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
/**
 * Simplest form of matching but not the best. There are smarter options available,
 *  this method is to test and debug.
 * 
 * @author rene
 *
 */
public class M4_LocalMatching1 implements M4_MatchingAlgorithm, EdgeCostModel 
{	
	final static double NoEdge = Double.MAX_VALUE / 100000;
	final static double planBonus = -401;
	final static double samePRMbonus = -800;
	
	HungarianAlgorithmResizable matcher = new HungarianAlgorithmResizable();
	ArrayList<M4_Segment> sources = new ArrayList<M4_Segment>();
	ArrayList<M4_Segment> sinks = new ArrayList<M4_Segment>();
	ArrayList<M4_Segment> between = new ArrayList<M4_Segment>();
	
	int betweenSegments;
	int totalSegments;
	
	M4_Planning parent;
	private M4_Segment planningSegment;
	
	boolean shifts = true;
	
	boolean doInsert = false;
	
	public M4_LocalMatching1(M4_Planning p)
	{
		this.parent = p;
		
		this.shifts = p.shifts;
		
		if(shifts)
		{
			for(M4_Area a : p.areas.values())
			{
				a.shiftfix = new Matching_ShiftFixer(a,this);
			}
		}
	}
	
	@Override
	public void acceptSuggestion() 
	{
		
	}

	@Override
	public void cancelSuggestion() 
	{ 
		
	}

	@Override
	public void generateSuggestion(M4_SegmentGroup<?> sg, SolutionFacture f) throws CantPlanException 
	{	
		sg.setPlanned(true);
		
		for(M4_Segment seg : sg)
		{	
			if(!f.feasible)
			{
				return;
			}
			
			if(!seg.isPlanned())
			{
				//seg = seg.getPlannableSegment();
				this.planSegment(seg, f);
			}
		}
	}
	
	public void planSegment(M4_Segment seg, SolutionFacture f) throws CantPlanException
	{	
		doInsert = false;
		
		this.planningSegment = seg;
		
//		System.out.println("planning segment: "+seg);
		
		final M4_Segment plannedSeg = seg.getPlannableSegment();
//		final Segment seg2 = seg.segment;
		final M4_Area area  = seg.area;
		
		if(plannedSeg.isFixedWorker())
		{
			if(plannedSeg.isPlanned() == false)
			{
				if(plannedSeg.getFixedWorker() == null)
				{
					throw new Error("Can't plan this segment");
				}
				else
				{
					// Try to plan it using another method.
					M4_FreeSpotMatching.static_planSegment(plannedSeg, f);
//					throw new Error("This shouldn't happen! "+plannedSeg);
//					return;
				}
			}
//			System.err.println("Warning tried to plan fixed Worker. Luckly its already planned.");
			return;
		}
		
		prepareSegments(plannedSeg, area);
		
		if(this.doInsert)
		{
//			throw new CantPlanException("This shouldn't happen! "+plannedSeg);
			M4_FreeSpotMatching.static_planSegment(plannedSeg, f);
			this.doInsert = false;
			return;
		}
		
		
		plannedSeg.setPlanned(true);
		
		if(this.sinks.size() == 0)
		{ // No point in solving this;
			f.feasible = false;
			return;
		}
		
		this.InitialiseCostMatrix();
		
		int[] result = this.matcher.execute();
		
		this.processResult(result, f);
		
//		if(!f.feasible)
//		{
//			throw new Error("Bad error!");
//		}
//		else
//		{
//			throw new Error("Good error!");
//		}
	}
	
	private void processResult(int[] matching, SolutionFacture f) throws CantPlanException
	{
//		System.out.println("Debug segments");
//		for(M4_Segment seg : this.between)
//		{
//			System.out.println(" Segment: "+seg);
//		}
//		System.out.println("Debug sources");
//		int workerid = -1;
//		for(M4_Segment seg : this.sources)
//		{
//			workerid++;
//			System.out.println(" Segment: "+seg+" worker: "+seg.area.workers.get(workerid));
//		}
//		System.out.println("Debug sinks");
//		for(M4_Segment seg : this.sinks)
//		{
//			System.out.println(" Segment: "+seg);
//		}
//		
//		System.out.println("Debug: Edges:");
//		for(int i = 0 ; i < this.totalSegments ; i++)
//		{
//			M4_Segment seg1 = ( i < this.betweenSegments 	? this.between.get(i).getPlannableSegment()
//															: this.sources.get(i - this.betweenSegments) );
//			
//			if(seg1.isPlanned())
//			{
//					for(int j = 0 ; j < totalSegments ; j++)
//					{
//						if(matcher.costMatrix[i][j] != NoEdge)
//						{
//							System.out.println(" Edge "+i+" => "+j+" costs: "+matcher.costMatrix[i][j]+" Alg internal: "+matcher.internalCostMatrix[i][j]);
//						}
//					}
//			}
//		}
		
//		System.out.println("Proces results");
		
		for(int i = 0 ; i < this.totalSegments ; i++)
		{
			M4_Segment seg1 = ( i < this.betweenSegments 	? this.between.get(i)
															: this.sources.get(i - this.betweenSegments) );
			
			seg1 = seg1.getPlannableSegment();
			final int matchId = matching[i];

			if(matchId == -1 || this.matcher.costMatrix[i][matchId] == NoEdge)
			{
				f.feasible = false;
//				System.out.println("Infeasibility found! "+i+" "+matchId+" costs: "+this.matcher.costMatrix[i][matchId]);
				//continue;
//				analyseFailure();
				return;
			}
//			System.out.println(	"Match: seg id "+seg1.id+" "+i+" => "+matchId+" / "+this.totalSegments+" costs: "+matcher.costMatrix[i][matchId]+
//								" Alg internal : "+matcher.internalCostMatrix[i][matchId]);
			
			M4_Segment seg2 = (matchId < this.betweenSegments 	? this.between.get(matchId).getPlannableSegment()
																: this.sinks.get(matchId - this.betweenSegments));
			
			if(seg1 == seg2 || seg2.getStartTime() < seg1.getStartTime())
			{
				throw new Error("Invalid start:\n"+
								" match "+i+" => "+matchId+"\n"+
								" costs: "+this.matcher.costMatrix[i][matchId]+" internal: "+matcher.internalCostMatrix[i][matchId]+"\n"+
								" r-costs: "+this.matcher.costMatrix[matchId][i]+" internal: "+matcher.internalCostMatrix[matchId][i]+"\n"+
								" "+seg1.getStartTime()+" > "+seg2.getStartTime()+"\n"+
								" seg1: "+seg1+" planned and primary : "+seg1.isPlannedAndPrimary()+"\n"+
								" seg2: "+seg2+" planned and primary : "+seg2.isPlannedAndPrimary()+"\n"+
								" Test: "+seg1.earliestArriveAt(seg2.segment.from)+" "+seg2.getStartTime()+"\n"+
								" last mutation: "+this.parent.solver.lastMutation);
								
			}
			seg1.setNext(seg2);
		}
		
		// Make sure the workers are correct
		//System.out.println("Repairing workers:");
		
		
		
//		int workernr = -1;
		for(M4_Segment first : sources)
		{	
//			workernr++;
			
//			M4_Worker w = first.area.workers.get(workernr);
			
			M4_Segment prev = first;
			M4_Segment seg = prev.getNext();
			
			M4_Worker worker = first.getWorker();
			
			if(worker == null)
			{
				
				throw new Error(	"Huh? worker is null: "+first+"\n"+
									" facture: "+f.feasible+"\n"+
									" first prev: "+first.getPrevious()+"\n"+
									" first next: "+first.getNext()+"\n"+
//									" should be:  "+w+"\n"+
//									" worker first: "+w.start+"\n"+
//									" worker db1:"+w.routeDebugString()+"\n"+
//									" worker db2:"+w.routeDebugString2()+
									"");
			}
				
//			int count = this.betweenSegments+2;
//			String segmentString = "";
			
			int lastTime = prev.getStartTime();
			
			// TODO: Add beter stop condition for updating the workers or better process for updating the workers.
			while(seg != null)
			{
				if(seg.getStartTime() < lastTime)
				{
					throw new Error(" Loop detected!");
				}
				
				lastTime = seg.getStartTime();
//				segmentString += "["+seg.id+" "+seg.getStartTime()+"]";
				
				if(!(seg instanceof M4_WorkerSegment))
				{
					seg.setWorker(worker);
				}
				
				prev = seg;
				seg = seg.getNext();
				
//				count--;
			}
			
			if(shifts)
			{
				// In case of shifts its the job of the fixer to fix it.
				worker.newEnd = prev;
			}
			else
			{
				seg = prev;
				prev = seg.getPrevious();
				
				if(seg != worker.end)
				{ // Fix the end segment.
					M4_Segment other = worker.end.getPrevious();
					prev.setNext(worker.end);
					other.setNext(seg);
				}
			}
			
//			System.out.println("Debug prev: "+prev);
		}
		
		if(shifts)
		{
			//System.out.println("Fixing shifts.");
			if(! this.between.get(0).area.shiftfix.fix())
			{
				f.feasible = false;
			}
		}
	}
	
	private void InitialiseCostMatrix()
	{
		this.betweenSegments = this.between.size();
		this.totalSegments = this.betweenSegments + this.sources.size();
		
		this.matcher.prepareCostMatrix(totalSegments, totalSegments);
		
		for(int i = 0 ; i < totalSegments ; i++)
		{
			M4_Segment seg1 = (i < betweenSegments ? this.between.get(i)
												   : this.sources.get(i-betweenSegments) );
			
			for(int j = 0 ; j < this.totalSegments ; j++)
			{
				if(i >= this.betweenSegments && j >= this.betweenSegments)
				{
					if(i == j)
					{
						this.matcher.costMatrix[i][j] = 0;
					}
					else
					{
						this.matcher.costMatrix[i][j] = NoEdge;
					}
				}
				else if(i == j)
				{
					this.matcher.costMatrix[i][j] = NoEdge;
				}
				else
				{
					M4_Segment seg2 = (j < betweenSegments ? this.between.get(j)
														   : this.sinks.get(j-betweenSegments) );
					if(	seg1.isVeryEffectivientNext(seg2))
					{
						this.matcher.costMatrix[i][j] = samePRMbonus;
					}
					else
					{
						final int slack = seg2.getStartTime() - seg1.earliestArriveAt(seg2.segment.from);
						if(slack >= 0)
						{
							this.matcher.costMatrix[i][j] = M4_CostModel.getPosRobustnessPanelty(slack) + planBonus;
						}
						else
						{
							this.matcher.costMatrix[i][j] = NoEdge;
						}
					}
				}
			}
		}
	}
	
	boolean  isAnalysing = false;
	
	public void analyseFailure()
	{
		try
		{
			
			if(parent.solver.getIteration() < 30000)
				return;
			
			if(isAnalysing)
				return;
			
			isAnalysing = true;
			
			M4_Segment seg = this.planningSegment;
			
			final int start = seg.getStartTime();
			
			System.out.println("Failed to plan: "+seg);
			
			System.out.println(" - start: "+start);
			
			System.out.println("-Alternative start times: ");
			
			M4_Segment[] segments = this.between.toArray(new M4_Segment[this.between.size()]);
			
			
			SolutionFacture f = new SolutionFacture();
			
			this.parent.makeCheckpoint();
			
			for(M4_Segment seg2 : segments)
			{
				this.parent.backtrack();
				
				final int pos = seg2.earliestArriveAt(seg.segment.from);
				System.out.println(" * "+pos);
				
				seg.setStart(pos);
				
				if(seg instanceof M4_PRM_Segment)
				{
					((M4_PRM_Segment) seg).nexPRMsegment.setStart(pos + seg.segment.segmentTime);
				}
				else if(seg instanceof M4_MergedSegment)
				{
					for(M4_PRM_Segment seg3 : ((M4_MergedSegment)seg))
					{
						seg3.nexPRMsegment.setStart(pos + seg.segment.segmentTime);
					}
				}
				
				f.feasible = true;
				this.planSegment(seg, f);
				
				
				if(f.feasible)
				{
					System.out.println("  # succesfully planned!");
				}
				else
				{
					System.out.println("  ~ failed planning");
				}
			}
			
			isAnalysing = false;
		}
		catch(CantPlanException e)
		{
			e.printStackTrace();
		}
		throw new Error("Analysed!");
	}
	
	private void prepareSegments(M4_Segment seg, M4_Area area)
	{
//		System.out.println("[A"+area.getArea().id+"] Try adding seg: "+seg);
		
		// Clear all information ect.
		this.sources.clear();
		this.sinks.clear();
		this.between.clear();
		
		this.between.add(seg);
		
		if(seg.getPlannableSegment().getWorker() != null)
			throw new Error("Can't plan a segment that already has a worker");
		
		// Need to find a worker who can handle this segment
		for(M4_Worker worker : area.workers)
		{
			Transport transport = worker.getTransport();
			TreeSet<M4_Segment> plannedSegments = worker.plannedSegments;
			
			M4_Segment prev = worker.plannedSegments.lower(seg);
			
			if(prev == null)
			{ // Just add the first and next just in case.
				
				M4_Segment source = worker.start;
				M4_Segment sink = source.getNext();
				while(sink != worker.end && sink.isFixedWorker())
				{
					source = sink;
					sink = source.getNext();
				}
				
				this.sources.add(source);
				this.sinks.add(sink);
				
//				System.out.println("Add source/sink ("+worker.start.getSimpleString()+
//									","+worker.start.getNext().getSimpleString()+")");
			}
			else if(prev == worker.end)
			{
				// If previous is the end then this employee cannot serve this
//				this.sources.add(worker.end.getPrevious());
//				this.sinks.add(worker.end);
				
			
			}
			else
			{
				M4_Segment next = prev.getNext();
				while(prev != null && !prev.isFixedWorker() && !prev.canServeNext(seg))
				{
					if(prev != worker.start)
					{
						between.add(prev);
					}
					prev = prev.getPrevious();
				}
				// Skip fixed segments: Code is probally buggy, next should never be fixed!
////				if(next != null && next.isFixedWorker())
//				{
//					while(next != worker.end && next.isFixedWorker())
//					{
//						prev = next;
//						next = prev.getNext();
//					}
//				}
				
				while(next != null && ! seg.canServeNext(next))
				{
					if(next.isFixedWorker())
					{
						doInsert = true;
						
						System.out.println("A sink may not be a fixed worker! Use backup method");
						return;
					}
					
					if(next != worker.end)
					{
						between.add(next);
					}
					next = next.getNext();
				}
				
				if(next == null)
					next = worker.end;
				if(prev == null)
					prev = worker.start;
				
				this.sources.add(prev);
				this.sinks.add(next);
				
//				System.out.println("Add source/sink ("+prev.getSimpleString()+
//						","+next.getSimpleString()+")");
			}
		}
		
		//Debug sources: 
//		System.out.println("Debug sources:");
//		for(M4_Segment s : sources)
//		{
//			System.out.println(" source: "+s.getSimpleString());
//		}
//		System.out.println("Debug between:");
//		for(M4_Segment s : between)
//		{
//			System.out.println(" between: "+s.getSimpleString());
//		}
//		System.out.println("Debug workers: ");
//		for(M4_Worker w : area.workers)
//		{
//			System.out.println(" w: "+w.getTransport().transport_ID+" start: "+w.start.getSimpleString()+" end: "+w.end.getSimpleString()+" Route: "+w.routeDebugString2());
//		}
	}
	
	
	@Override
	public M4_LocalMatching1 newForMutation()
	{
		return this;
	}

	@Override
	public void clear() 
	{
		
	}

	@Override
	public void registerUnplan(M4_SegmentGroup sg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getCosts(M4_Segment from, M4_Segment to) 
	{
		if(from == to)
			return NoEdge;
		
		if(	from.isVeryEffectivientNext(to))
		{
			return samePRMbonus;
		}
		
		final int slack = to.getStartTime() - from.earliestArriveAt(to.segment.from);
		
		if(slack >= 0)
		{
			return M4_CostModel.getPosRobustnessPanelty(slack) + planBonus;
		}
	
		return NoEdge;
	}

	@Override
	public double getNoEdgeValue() 
	{
		return NoEdge;
	}
}
