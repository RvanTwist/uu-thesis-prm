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
 * Smarter version of Matching, including making delays (Not sure or that is implemented).
 *  this method is to test and debug.
 * 
 * Ended up adding shifts to LocalMatching2
 * 
 * @author rene
 *
 */
public class M4_LocalMatching2 implements M4_MatchingAlgorithm 
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
	
	
	public M4_LocalMatching2(M4_Planning p)
	{
		this.parent = p;
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
		this.planningSegment = seg;
		
//		System.out.println("planning segment: "+seg);
		
		final M4_Segment plannedSeg = seg.getPlannableSegment();
//		final Segment seg2 = seg.segment;
		final M4_Area area  = seg.area;
		
		prepareSegments(plannedSeg, area);
		this.makeCheckPoint();
		
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
	
	public void makeCheckPoint()
	{
		if(this.isAnalysing)
		{
			return;
		}
		
		for(M4_Segment seg : this.sources)
		{
			seg.makeScheduleCheckpoint();
		}
		for(M4_Segment seg : this.sinks)
		{
			seg.makeScheduleCheckpoint();
		}
		for(M4_Segment seg : this.between)
		{
			seg.makeScheduleCheckpoint();
		}
	}
	
	public void backTrack()
	{
		for(M4_Segment seg : this.sources)
		{
			seg.backTrackSchedule();
		}
		for(M4_Segment seg : this.sinks)
		{
			seg.backTrackSchedule();
		}
		for(M4_Segment seg : this.between)
		{
			seg.backTrackSchedule();
		}
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
				//analyseFailure();
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
								" seg: "+seg1+" "+seg2+" planned and primary : "+seg1.isPlannedAndPrimary()+"|"+seg2.isPlannedAndPrimary()+"\n"+
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
				
				seg.setWorker(worker);
				prev = seg;
				seg = seg.getNext();
				
//				count--;
			}
			
			if(prev != worker.end)
			{ // Make sure the schedule ends!
				// TODO: This algorithm fails when shift ends are all diffrent.
				worker.end = prev;
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
	
	public void analyseFailure(SolutionFacture f)
	{	
		if(isAnalysing)
			return;
		
		isAnalysing = true;
		
		M4_Segment seg = this.planningSegment;
		
		int latestStart;
		
		if(seg instanceof M4_PRM_Segment)
		{
			M4_PRM_Segment prmSeg = ((M4_PRM_Segment)seg);
			
			M4_PRM_SegmentGroup msg = prmSeg.getOriSegmentGroup();
			
			
			
			if(msg.fixStart)
			{
				latestStart = seg.getLatestStart();
				
				this.analyseFowardPlanning(seg,f, latestStart);
			}
			else
			{
				return;
			}
		}
		else
		{ // Nothing can be done;
			return;
		}
		
		isAnalysing = false;
		throw new Error("Analysed!");
	}
	
	public void analyseFowardPlanning(M4_Segment seg, SolutionFacture f, int latestStart)
	{
		final int start = seg.getStartTime();
		int newStart = start;
		
		M4_Segment[] segments = this.between.toArray(new M4_Segment[this.between.size()]);
		
		boolean canImprove = true;
		
		while(canImprove)
		{
			
		}
	}
	
	private void prepareSegments(M4_Segment seg, M4_Area area)
	{
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
				this.sources.add(worker.start);
				this.sinks.add(worker.start.getNext());
			}
			else if(prev == worker.end)
			{
				this.sources.add(worker.end.getPrevious());
				this.sinks.add(worker.end);
			}
			else
			{
				M4_Segment next = prev.getNext();
				while(prev != null && !prev.canServeNext(seg))
				{
					if(prev != worker.start)
						between.add(prev);
					prev = prev.getPrevious();
				}
				while(next != null && ! seg.canServeNext(next))
				{
					if(next != worker.start)
						between.add(next);
					next = next.getNext();
				}
				
				if(next == null)
					next = worker.end;
				if(prev == null)
					prev = worker.start;
				
				this.sources.add(prev);
				this.sinks.add(next);
			}
		}
	}
	
	
	@Override
	public M4_LocalMatching2 newForMutation()
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
}
