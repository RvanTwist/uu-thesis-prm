package prm.ls.model4.matching;

import java.util.Comparator;
import java.util.TreeSet;

import prm.ls.model4.*;
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
public class Matching_ShiftFixer 
{
	M4_Worker[] workers;
	
	EdgeCostModel costModel;
	
	final double NoEdge;
	
	public Matching_ShiftFixer(M4_Area a, EdgeCostModel m)
	{
		this.costModel = m;
		
		NoEdge = m.getNoEdgeValue();
		
		workers = new M4_Worker[a.workers.size()];
		workers = a.workers.toArray(workers);
		
		// Bubblesort
		for(int i = 0 ; i < workers.length ; i++)
			for(int j = 1 ; j < workers.length - i ; j++)
			{
				final M4_Worker w1 = workers[j-1];
				final M4_Worker w2 = workers[j];
				final Transport t1 = w1.getTransport();
				final Transport t2 = w2.getTransport();
				if(t1.endShift > t2.endShift)
				{
					workers[j-1] = w2;
					workers[j] = w1;
				}
			}
	}
	
	public boolean fix() throws CantPlanException
	{
		// TODO:Make fix smarter.
//		System.out.println("Start fixing!");
		
		// Enforce Preconditions
		for(M4_Worker w : this.workers)
		{
			M4_Segment seg = w.start;
			M4_Segment next = seg.getNext();
			
			while(next != null)
			{
				seg  = next;
				next = seg.getNext();
			}
			
//			if(w.newEnd != seg)
//			{
//				throw new Error("Huh? "+w.newEnd+" Actual seg:"+seg);
//			}
			
			w.newEnd = seg;
		}
		
		for(int i = 0 ; i < workers.length ; i++)
		{	
			M4_Worker w1 = workers[i];
			Transport t1 = w1.getTransport();
			if(w1.newEnd.getStartTime() != t1.endShift)
			{
//				System.out.println("Fix recombinated: "+w1+" "+w1.routeDebugString2());
//				System.out.println("                : "+w1.newEnd+"  ~= "+t1.endShift);
				
				M4_Worker best_w = null;
				double bestScore = Double.MAX_VALUE;
				M4_Segment best_seg1 = null;
				M4_Segment best_seg2 = null;
				
				for(int j = i+1 ; j < workers.length ; j++)
				{
					M4_Worker w2 = workers[j];
					Transport t2 = w2.getTransport();
					
					if(w2.newEnd.getStartTime() == t1.endShift)
					{ // Its a candidate! TODO: Could be smarter to consider higher ends but with work below the current shiftend.
						M4_Segment seg1 = w1.start;
						M4_Segment seg2 = w2.start;
						M4_Segment seg1_next = seg1.getNext();
						M4_Segment seg2_next = seg2.getNext();
						
						while(seg1_next != null && seg2_next != null)
						{	
							final double v1 = costModel.getCosts(seg1, seg2_next);
							final double v2 = costModel.getCosts(seg2, seg1_next);
							
							if(v1 != NoEdge && v2 != NoEdge)
							{
								final double c = 	v1 + v2 
													- costModel.getCosts(seg1, seg1_next) 
													- costModel.getCosts(seg2, seg2_next);
								
								if(c < bestScore)
								{
									bestScore = c;
									best_seg1 = seg1;
									best_seg2 = seg2;
									best_w = w2;
								}
							}
							
							//Advance rules
							// TODO: This needs checking
							if(	seg2.getEndTime() > seg1_next.getStartTime() )
							{
								seg1 = seg1_next;
								seg1_next = seg1.getNext();
							}
							else if( seg1.getEndTime() > seg2_next.getStartTime() )
							{
								seg2 = seg2_next;
								seg2_next = seg2.getNext();
							}
							else if(seg1.getStartTime() < seg2.getStartTime())
							{
								seg1 = seg1_next;
								seg1_next = seg1.getNext();
							}
							else
							{
								seg2 = seg2_next;
								seg2_next = seg2.getNext();
							}
						}
					}
				}	
				
				if(best_w == null)
				{
//					throw new Error("Fixing failed!");
					return false;
				}
				else
				{
//					System.out.println(" Switch: "+w1.routeDebugString2());
//					System.out.println("       : "+best_w.routeDebugString2());
//					System.out.println("         ["+t1.startShift+","+t1.endShift+"]");
					
					final M4_Segment best_seg1_next = best_seg1.getNext();
					final M4_Segment best_seg2_next = best_seg2.getNext();
					
					//Debug:
					final double c1 = costModel.getCosts(best_seg2, best_seg1_next);
					final double c2 = costModel.getCosts(best_seg1, best_seg2_next);
//					System.out.println("Costs: "+c1+"+"+c2+"="+(c1 + c2)+"/"+bestScore+" ("+NoEdge+")");
					
					best_seg1.setNext(best_seg2_next);
					best_seg2.setNext(best_seg1_next);
					
					// Store the old newEnd in the previous
					final M4_Segment tmp = best_w.newEnd;
					best_w.newEnd = w1.newEnd;
					w1.newEnd = tmp;
				}
			}
		}
		
		// Now fix the endsegments:
		for(M4_Worker w : this.workers)
		{
			M4_Segment seg = w.start.getNext();
			
			while(!(seg instanceof M4_WorkerSegment))
			{
				seg.setWorker(w);
				seg = seg.getNext();
			}
			
			if(seg != w.end)
			{
				final M4_Segment end_prev = w.end.getPrevious();
				final M4_Segment newend_prev = seg.getPrevious();
				final M4_Worker w2 = seg.getWorker();
				
				end_prev.setNext(seg);
				newend_prev.setNext(w.end);
			}
			
			w.newEnd = w.end;
			
			
		}
		
//		// DEBUG
//		for(M4_Worker w : this.workers)
//		{
//			w.checkConsistancy();
//		}
		
		return true;
	}
}
