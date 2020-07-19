package prm.ls.model2.phase2;

import java.util.Comparator;

import prm.ls.model2.*;
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
public class F2_PRM implements Comparable<F2_PRM>
{	
	// Data:
	PRM_Planning prm;
	F2_Planner planner;
	
	// Planning & Simulation
	public LocationAlias 	currentLocation;
	public F2_Worker 		currentWorker;
	public int 				currentTime;
	
	int[] segmentStart;
	F2_Worker[] segmentHandler;
	int currentSegment;
	
	public F2_PRM(PRM_Planning prmP, F2_Planner f2Planner) 
	{
		this.prm = prmP;
		this.planner = f2Planner;
		
		segmentStart = new int[prmP.prm.route.length];
		segmentHandler = new F2_Worker[segmentStart.length]; 
		
		this.reset();
	}
	
	public void reset()
	{
		this.currentLocation = prm.prm.checkin;
		this.currentWorker = null;
		
		if(this.isPlanned())
		{
			this.currentTime = prm.getStartTime();
		}
		else
		{
			this.currentTime = prm.prm.arrival;
		}
		
		currentSegment = 0;
	}
	
	public void serviceNextSegment(int time, F2_Worker worker)
	{
		
		final Segment segment = this.prm.prm.route[currentSegment];
		final PlannedSegment pSeg = this.prm.getPlannedSegment(currentSegment);
		
		//System.out.println("    - Planning segment "+segment+" of PRM "+prm.prm.prm_ID);
		
		// Release the previous worker of its PRM if present;
		if(currentWorker != null)
		{
			currentWorker.releasePRM(this, time, this.currentLocation);
		}
		
		worker.serviceSegment(this, time, segment);
		
		// Update time, assume the prm could be serviced an the mimimum time needed.
		this.currentTime 		= time + segment.getSegmentTime();
		this.currentLocation 	= segment.to;
		this.currentWorker 		= worker;
		
		this.currentSegment++;
	}
	
	public boolean isPlanned()
	{
		return this.prm.isPlanned();
	}
	
	public boolean isDeclined()
	{
		return !this.isPlanned() || currentSegment == 0;
	}
	
	public boolean isFullyPlanned()
	{
		return currentSegment == segmentStart.length;
	}

	public PlannedSegment getNextSegment() 
	{
		return this.prm.getPlannedSegment(this.currentSegment);
	}
	
	public void unPlanAll()
	{
		if(this.currentWorker != null)
		{
			this.currentWorker.releasePRM(this, currentWorker.getCurrentTime(), currentWorker.getCurrentLocation());
		}
		this.reset();
	}

	public void releaseWorker() 
	{
		if(this.currentWorker != null)
		{
			this.currentWorker.releasePRM(this, this.currentTime, this.currentLocation);
		}
	}

	public int getCapicityReq() 
	{
		return prm.prm.capRequirement;
	}

	@Override
	public int compareTo(F2_PRM other) 
	{
		return this.prm.compareTo(other.prm);
	}

	public String printRoute() 
	{
		return this.prm.prm.printRoute();
	}
	
	
	public static F2_PRM_Comp getPRMcomparator() 
	{
		return new F2_PRM_Comp();
	}

	public void saveSolution(Sollution s) 
	{
		PRM prm = this.prm.prm;
		if(this.isDeclined())
		{
			for(int i = 0 ; i < prm.route.length ; i++)
			{
				Segment seg = prm.route[i];
				ScheduledSegment sSeg = s.getScheduling(seg);
				sSeg.pickup_time = 0;
				sSeg.delivery_time = 0;
				sSeg.transport = null;
			}
		}
		else
		{
			ScheduledSegment prev = null;
			for(int i = 0 ; i < prm.route.length ; i++)
			{
				Segment seg = prm.route[i];
				ScheduledSegment sSeg = s.getScheduling(seg);
				sSeg.pickup_time = this.segmentStart[i];
				sSeg.delivery_time = sSeg.pickup_time + seg.segmentTime;
				sSeg.transport = segmentHandler[i].transport;
				
				if(prev != null && !seg.from.isSupervised())
				{
					prev.delivery_time = sSeg.pickup_time;
				}
				
				prev = sSeg;
			}
		}
		
	}
}
