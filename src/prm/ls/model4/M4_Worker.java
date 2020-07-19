package prm.ls.model4;

import java.util.TreeSet;

import prm.ls.SimulatedAnnealing;
import prm.ls.resources.ParalelResource;
import prm.ls.resources.ParalelResourceSegment;
import prm.problemdef.Area;
import prm.problemdef.Segment;
import prm.problemdef.Transport;

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
public class M4_Worker implements Comparable<M4_Worker>
{
	final Transport transport;
	M4_Area area;
	
	public TreeSet<M4_Segment> plannedSegments = new TreeSet<M4_Segment>(new M4_SegmentTimeSorted());
	
	public M4_Segment start;
	public M4_Segment end;
	public M4_Segment newEnd;
	
	public M4_Worker(Transport t, M4_Area area)
	{
		this.transport = t;
		this.area = area;
		
		// Start segment
		
		Segment segStart = new Segment(null, t.depot, t.depot, area.area, false,0);
		Segment segEnd = new Segment(null, t.depot, t.depot, area.area, false,0);
		
		segStart.setWindow(t.startShift, t.startShift);
		segEnd.setWindow(t.endShift, t.endShift);
		
		start 		= new M4_WorkerSegment(segStart, this,true);
		end 	 = new M4_WorkerSegment(segEnd, this,false);
		
		try
		{
			start.setStart(t.startShift);
			end.setStart(t.endShift);
	
			start.setNext(end);
			
			start.setPlanned(true);
			end.setPlanned(true);
			
			
			
			this.add(start);
			this.add(end);
		
		}
		catch(CantPlanException e)
		{
			e.printStackTrace();
			throw new Error(e);
		}
		
		this.newEnd = end;
	}

	public void add(M4_Segment seg) throws CantPlanException
	{
//DEBUG*/ this.checkConsistancy();
		
		if(seg.getWorker() != null)
		{
			throw new Error("Can't plan a segment that is already planned on someone else!\n"+
							seg);
		}
		
		final M4_Segment lower = this.plannedSegments.lower(seg);
		final M4_Segment upper = (lower == null ? (this.plannedSegments.isEmpty() ? null : this.plannedSegments.first()) 
												: lower.getNext());
		
		seg.setPrevious(lower);
		seg.setNext(upper);
		
		seg.setPlanned(true);
		seg.setWorker(this);
		
//DEBUG*/ this.checkConsistancy();
	}
	
	public void remove(M4_Segment seg) throws CantPlanException
	{
//DEBUG*/ this.checkConsistancy();
		seg.unPlan();

	}

	public Transport getTransport() 
	{
		return this.transport;
	}
	
	public void checkConsistancy()
	{
		if(this.plannedSegments.isEmpty())
		{
//			System.out.println("(!)Worker: "+this.toString()+" has nothing to do!");
			return;
		}

//		M4_Segment[] sortedSegments = this.plannedSegments.toArray(new M4_Segment[this.plannedSegments.size()]);
//		
//		//Do bubble sort
//		for(int i = 0 ; i < sortedSegments.length ; i++)
//			for(int j = 1 ; j < sortedSegments.length - i ; j++)
//			{
//				M4_Segment s1 = sortedSegments[j-1];
//				M4_Segment s2 = sortedSegments[j];
//				if(s2.getStartTime() < s1.getStartTime())
//				{
//					sortedSegments[j-1] = s2;
//					sortedSegments[j]	= s1;
//				}
//			}
		
		
		M4_Segment prev = null;
		M4_Segment first = this.plannedSegments.first();
		M4_Segment last  = this.plannedSegments.last();
		
//		M4_Segment first = (sortedSegments.length == 0 ? null : sortedSegments[0]);
//		M4_Segment last  = (sortedSegments.length == 0 ? null : sortedSegments[sortedSegments.length-1]);;
		
		if(first.getPrevious() != null)
		{
			throw new Error("First segment "+first.id+" of worker: "+this+" got a previous!\n"+
							"Debugstring1: "+this.routeDebugString()+"\n"+
							"Debugstring2: "+this.routeDebugString2()+"\n"+
							"start: "+this.start+"\n"+
							"end: "+this.end);
		}
		
		if(last.getNext() != null)
		{
			throw new Error("Last segment "+last.id+" of worker: "+this+" got a followup!\n"+
							"Debugstring1: "+this.routeDebugString()+"\n"+
							"Debugstring2: "+this.routeDebugString2()+"\n"+
							"start: "+this.start+"\n"+
							"end: "+this.end);
		}
		
		for(M4_Segment seg : plannedSegments)
		{
			if(!seg.isPlanned())
			{
				String whole = "";
				for(M4_Segment seg2 : plannedSegments)
				{
					whole += "["+seg2.id+" t:"+seg2.getStartTime()+"]";
				}
				
				String whole2 = "";
				M4_Segment tmp = this.start;
				int segC = 0;
				while(tmp != null || segC == 1000)
				{
					segC++;
					whole2 += "["+tmp.id+" t:"+tmp.getStartTime()+"]";
					tmp = tmp.getNext();
				}
				
				SimulatedAnnealing solver = area.planning.solver;
				
				throw new Error(	" Segment data inconsistancy!\n"+
									" prev mutation: "+solver.lastMutation.getClass().getSimpleName()+"\n"+
									" mutation feasisability: "+solver.facture.feasible+"\n"+
									"   Segment in sequence is unplanend!\n"+
									"   seg:       "+seg+" local_id:"+seg.local_Id+"\n"+
									"   seg.prev:  "+seg.getPrevious()+" \n"+
									"   seg.next:  "+seg.getNext()+" \n"+
									" all : "+whole+"\n"+
									" all2: "+whole2+"\n"+
									" lastMutation: "+this.area.planning.solver.lastMutation
									);
			}
			
			if(prev != null)
			{
				if(prev.getNext() != seg)
				{
					String whole = "";
					for(M4_Segment seg2 : plannedSegments)
					{
						whole += "["+seg2.id+" t:"+seg2.getStartTime()+"]";
					}
					
					String whole2 = "";
					M4_Segment tmp = this.start;
					int segC = 0;
					while(tmp != null || segC == 1000)
					{
						segC++;
						whole2 += "["+tmp.id+" t:"+tmp.getStartTime()+"]";
						tmp = tmp.getNext();
					}
					
					SimulatedAnnealing solver = area.planning.solver;
					
					throw new Error(	" Segment data inconsistancy! \n"+
										" prev mutation: "+solver.lastMutation.getClass().getSimpleName()+"\n"+
										" mutation feasisability: "+solver.facture.feasible+"\n"+
										" prev.next != seg!\n"+
										"   worker:    "+this+" start: "+start.getSimpleString()+" end: "+end.getSimpleString()+"\n"+
										"   "+prev.getNext()+" != "+seg+"\n"+
										"   seg:       "+seg+" local_id:"+seg.local_Id+"\n"+
										"   seg.prev:  "+seg.getPrevious()+" \n"+
										"   prev:      "+prev+"\n local_id:"+prev.local_Id+"\n"+
										"   prev.next: "+prev.getNext()+"\n"+
										" all : "+whole+"\n"+
										" all2: "+whole2+"\n"+
										" lastMutation: "+this.area.planning.solver.lastMutation
										);
				}
				
				if(seg.getPrevious() != prev)
				{
					throw new Error(" seg.prev != prev "+seg+" "+seg.getPrevious()+" != "+prev);
				}
				
				
				final int distance;
				
				if(prev.segment.to == seg.segment.from)
					distance = 0;
				else
					distance = area.area.getDistance(prev.segment.to, seg.segment.from);
				
				if( prev.getEndTime() + distance > seg.getStartTime())
				{
					if(seg instanceof M4_WorkerSegment || prev instanceof M4_WorkerSegment)
					{
						System.err.println("[Warning] "+"Segment "+prev+" and "+seg+" can't be planned after eachother"
											);
					}
					else
					{
						throw new Error("Segment "+prev+" and "+seg+" can't be planned after eachother");
					}
				}
				
				if(seg.getWorker() != this)
				{
					SimulatedAnnealing solver = area.planning.solver;
					
					throw new Error(" Segment data inconsistancy!\n"+
									" prev mutation: "+solver.lastMutation.getClass().getSimpleName()+"\n"+
									" mutation feasisability: "+solver.facture.feasible+"\n"+
									" Worker isn't correct.\n"+
									"   seg:        "+seg+"\n"+
									"   seg.worker: "+seg.getWorker()+" \n"+
									"   worker:      "+this+"\n"+
									" lastMutation: "+this.area.planning.solver.lastMutation
							);
				}
			}
			
			prev = seg;
		}
		
//		System.out.println(" - Worker: "+this.toString()+" passed! segments: "+plannedSegments.size());
	}
	
	public String toString()
	{
		return "T"+area.area.id+" "+transport.transport_ID;
	}

	public String routeDebugString() 
	{
		//this.checkConsistancy();
		String route = "";
		for(M4_Segment seg : this.plannedSegments)
		{
			route += "["+seg.id+": "+seg.getStartTime()+"]";
		}
		
		return this.toString()+" "+route;
	}
	
	public String routeDebugString2() 
	{
		//this.checkConsistancy();
		String route = "";
		
		M4_Segment seg = this.start;
		int i = 0;
		while(seg != null && i < 1000)
		{
			route += "["+seg.id+": "+seg.getStartTime()+"]";
			i++;
			seg = seg.getNext();
		}
		
		return this.toString()+" "+route;
	}

	public void loadSegment(M4_PRM_Segment seg, int pickupTime) throws CantPlanException
	{
		seg.unPlan();
		
		seg.setStart(pickupTime);
		
		M4_Segment prev = this.plannedSegments.lower(seg);
		M4_Segment next = prev.getNext();
		
//		System.out.println("Loading segment: "+seg);
		
		if(prev.canMergeWith(seg))
		{
//			System.out.println(" - merging with prev:"+prev);
			if(prev instanceof M4_PRM_Segment)
			{
				((M4_PRM_Segment) prev).getOriSegmentGroup().mergeWith(seg.getOriSegmentGroup());
			}
			else if(prev instanceof M4_MergedSegment)
			{
				((M4_MergedSegment) prev).segments.first().getOriSegmentGroup().mergeWith(seg.getOriSegmentGroup());
			}
			
			prev.mergeWith(seg);
		}
		else if(next != null && next.canMergeWith(seg))
		{
//			System.out.println(" - merging with next:"+next);
			if(next instanceof M4_PRM_Segment)
			{
				((M4_PRM_Segment) next).getOriSegmentGroup().mergeWith(seg.getOriSegmentGroup());
			}
			else if(next instanceof M4_MergedSegment)
			{
				if(((M4_MergedSegment) next).segments.size() == 0)
				{
					throw new Error("How can this segment be next?\n"+
									" prev: "+prev+"\n"+
									" next: "+next);
				}
				
				((M4_MergedSegment) next).segments.first().getOriSegmentGroup().mergeWith(seg.getOriSegmentGroup());
			}
			
			next.mergeWith(seg);
		}
		else
		{
//			System.out.println(" - just planning: ");
			this.add(seg);
		}
	}

	@Override
	public int compareTo(M4_Worker other) 
	{
		return this.transport.compareTo(other.transport);
	}

	public void printShift() 
	{
		int max = 10000;
		
		M4_Segment seg = start.getNext();
		
		String s = "T"+area.area.id+" "+transport.transport_ID+" : "+start.id+":"+start.getStartTime();
		
		while(seg != null)
		{
			s = s +" > "+seg.id+":"+seg.getStartTime();
			seg = seg.getNext();
			
			max--;
			if(max < 0)
			{
				throw new Error("Loop detected!");
			}
		}
		
		System.out.println(s);
	}
	
}
