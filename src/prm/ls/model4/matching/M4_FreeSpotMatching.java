package prm.ls.model4.matching;

import java.util.ArrayList;
import java.util.TreeSet;

import prm.ls.SolutionFacture;
import prm.ls.model4.*;
import prm.ls.model4.matching.*;
import prm.ls.model4.mutations.*;
import prm.ls.model4.matching.lpflow.*;
import prm.ls.model4.mutationStrategy.*;
import prm.problemdef.Area;
import prm.problemdef.LocationAlias;
import prm.problemdef.Segment;
import prm.problemdef.Transport;

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
public class M4_FreeSpotMatching implements M4_MatchingAlgorithm 
{
	M4_SegmentGroup lastMatched = null;
	
	@Override
	public void acceptSuggestion() 
	{
		lastMatched = null;
	}

	@Override
	public void cancelSuggestion() 
	{ 
		
	}

	@Override
	public void generateSuggestion(M4_SegmentGroup<?> sg, SolutionFacture f) 
	{	
		lastMatched = sg;
		
		sg.setPlanned(true);
		
//		System.out.println("Match SegmentGroup: "+sg.getClass().getSimpleName()+" "+sg.id);
		
		for(M4_Segment seg : sg)
		{	
			if(!seg.isPlanned())
			{
//				System.out.println(" - Add to planned: "+seg);
				this.planSegment(seg, f);
			}
		}
	}
	
	@Override
	public M4_FreeSpotMatching newForMutation()
	{
		return new M4_FreeSpotMatching();
	}

	@Override
	public void clear() 
	{
		lastMatched = null;
	}

	@Override
	public void registerUnplan(M4_SegmentGroup<?> sg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void planSegment(M4_Segment seg, SolutionFacture f) 
	{
		static_planSegment(seg,f);
	}
	
	public static void static_planSegment(M4_Segment seg, SolutionFacture f) 
	{
		if(M4_Planning.DEBUG)
		{
			System.out.println("Attempt plan: "+seg);
		}
		
		seg = seg.getPlannableSegment();
		
		M4_Worker fixedWorker = seg.getFixedWorker();
		
		final Segment seg2 = seg.segment;
			
		final M4_Area area  = seg.area;
		final Area	  area2 = area.getArea(); 
		
		double		best_score 		= Double.MAX_VALUE;
		M4_Worker 	best_worker		= null;
		M4_Segment	best_segment 	= null;
		
		final int start = seg.getStartTime();
		final int end  	= start + seg.segment.segmentTime;
		
		LocationAlias from 	= seg2.from;
		LocationAlias to 	= seg2.to;
		
		// Need to find a worker who can handle this segment
		for(M4_Worker worker : area.workers)
			if(fixedWorker == null || worker == fixedWorker)
			{
				// Code for checking feasibility
				Transport transport = worker.getTransport();
				TreeSet<M4_Segment> plannedSegments = worker.plannedSegments;
				M4_Segment prev = plannedSegments.lower(seg);
				M4_Segment next = (prev == null ? (plannedSegments.isEmpty() ? null : plannedSegments.first()) 
												: prev.getNext());
				
				
				final int earliestArrive = (prev == null ? (transport.startShift + area2.getDistance(transport.depot, from))
														 : (prev.getEndTime() + area2.getDistance(prev.segment.to, from))	);;
				
				
				final int latestFinish = (next == null 	? (transport.endShift - area2.getDistance(to, transport.depot))
														: (next.getStartTime() - area2.getDistance(to, next.segment.from)) );
				
				if(   earliestArrive < worker.getTransport().startShift
				   || latestFinish > worker.getTransport().endShift    )
				{
					continue;
				}
				
	//			System.out.println("Considering worker: "+worker+" ["+earliestArrive+","+latestFinish+"] vs ["+start+","+end+"]");
				
				final int robustPrev = start - earliestArrive;
				
				final int robustNext = latestFinish - end;
				
				final boolean workerFeasible = robustPrev >= 0 && robustNext >= 0;
				
				if(workerFeasible)
				{
					final double robustnessScore = M4_CostModel.getRobustnessPanelty(robustPrev) +
												M4_CostModel.getRobustnessPanelty(robustNext);
					
					if(robustnessScore < best_score)
					{
						best_worker = worker;
						best_segment = prev;
						best_score = robustnessScore;
					}
				}
			}
		
		
		if(M4_Planning.DEBUG)
		{
			System.out.println("Best worker: "+best_worker);
		}
		
		if(best_worker == null)
		{
//			System.out.print(" - Unable to plan "+seg.getClass().getSimpleName()+" "+seg.id+" at worker");
			f.feasible = false;
			return;
		}
		else 
		{
			try 
			{
				best_worker.add(seg);
			} 
			catch (CantPlanException e) 
			{
				f.feasible = false;
			}
		}
	}
}
