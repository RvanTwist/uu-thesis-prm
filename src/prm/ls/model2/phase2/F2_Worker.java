package prm.ls.model2.phase2;

import java.util.TreeSet;

import prm.ls.model2.PRM_Planning;
import prm.ls.model2.PlannedSegment;
import prm.problemdef.Area;
import prm.problemdef.Location;
import prm.problemdef.LocationAlias;
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
public class F2_Worker
{	
	public  final static int SCORE_MAX	 						= 0x400000 - 1;
	public  final static int SCORE_PRIORITY 					= 0x400000; // Binary
	public  final static int SCORE_ALREADY_SERVING_PRM 			= SCORE_PRIORITY + 0x000001;
	public  final static int SCORE_SYNCHRONIZED_SEGMENT_SERVING = SCORE_PRIORITY + 0x000002;
	
	
	final Transport transport;
	final Area 		area;
	final F2_Planner planner;
	
	//F2_PRM 			currentClient;
	private LocationAlias 	currentLocation;
	private int 			currentTime = 0;
	private int 			expectedRelease = 0;
	private LocationAlias	expectedLocation;
			int 			currentCap = 0;
	
	// More info
	TreeSet<F2_PRM> currentPRMs = new TreeSet<F2_PRM>();
	private int				lastEndTime = 0;
	private LocationAlias	lastLocation;
	
	Segment			currentSegment;
	
	
	int max_cap;
	
	public F2_Worker(Transport t, F2_Planner planner)
	{
		this.transport = t;
		this.area = t.getArea();
		this.planner = planner;
		
		this.max_cap = t.capicity;
		this.reset();
	}

	public int canService(F2_PRM prm, PlannedSegment pseg) 
	{
//		if(this.currentClient != null)
//		{
//			return -1;
//		}
		
		if(prm.currentWorker == this)
		{ // Easy we are already supporting this client give it a high priority, only beaten by serving 2 at the same time.
			return F2_Worker.SCORE_ALREADY_SERVING_PRM;
		}
		
		// Check if both segments can be served simultaneously.
		final int start = pseg.getStartTime();
		final Segment seg = pseg.segment;
		
		if(currentSegment != null && this.currentCap + prm.getCapicityReq() <= this.max_cap && start == this.currentTime)
		{
			if(seg.from == currentSegment.from && seg.to == currentSegment.to)
			{ // All conditions are set for the 2 segments to be handled simultaneously
//				System.out.println("Simultan segments found: "+seg+","+currentSegment);
				return F2_Worker.SCORE_SYNCHRONIZED_SEGMENT_SERVING;
			}
		}
//		System.out.println("Worker: T"+transport.getArea().id+" "+transport.transport_ID+" expectedLocation"+expectedLocation);
		final int earliestArrive = this.expectedRelease + this.transport.getArea().getDistance(expectedLocation, pseg.segment.from);
		final int slack = start - earliestArrive;
		
//		if(this.expectedLocation == pseg.segment.from)
//		{
//			return slack + 10000;
//		}
		
//		if(slack < 0 )
//		{
//			return -1;
//		}
		
		return slack;
		
	}

	public boolean isHelpingSomeone() 
	{
		return this.currentCap > 0;
	}

	public void releasePRM(F2_PRM prm, int time, LocationAlias loc) 
	{
		this.currentPRMs.remove(prm);
		
//		if(this.transport.getArea().id == -1)
//		{
//			System.out.println(" Release prm: "+prm.prm.prm.prm_ID+" of: T"+transport.getArea().id+" "+transport.transport_ID);
//		}
		
		//this.currentClient = null;
		this.lastEndTime = time;
		this.lastLocation = loc;
		
		this.currentTime = time;
		this.currentLocation = loc;
		
		this.expectedRelease = time;
		this.expectedLocation = loc;
		this.currentCap = this.currentCap - prm.getCapicityReq();
		
		if(loc.getArea() != this.area)
		{
			throw new Error("Transport "+this.workerName()+" placed on invalid location: "+loc);
		}
		
		this.currentSegment = null;
	}

	public void serviceSegment(F2_PRM prm, int time, Segment seg) 
	{	
		
		String prms_s = "{";
		
		for(F2_PRM p_prm : this.currentPRMs)
		{
			prms_s = prms_s +" "+ p_prm.prm.prm.prm_ID;
		}
		
//		System.out.println(	"        Pre Adding T:"+transport.getArea().id+" "+transport.transport_ID+
//							" currSeg: "+this.currentSegment+" ("+this.currentTime+")"+this.currentLocation+"=>"+this.expectedLocation+"("+this.expectedRelease+")"+
//							" cap: "+currentCap+"/"+max_cap+
//							" prms: "+prms_s+" }");
		// If he is already serving a segment the segment should be from and to the same locations
		if(time == this.currentTime && this.currentSegment != null)
		{
			if(!(this.currentSegment.to == seg.to && this.currentSegment.from == seg.from))
			{
				throw new Error("Can't plan 2 diffrent segments at the same time: new seg: "+seg+", currently: "+currentSegment);
			}
		}
		else if(currentSegment != null && this.expectedRelease <= time)
		{	// TODO: Fix this quick hack, works for case that prms do not need to wait ever.
			
			
			for(F2_PRM c_prm : this.currentPRMs)
			{
//				if(this.transport.getArea().id == -1)
//				{
//					System.out.println(" Release prm: "+c_prm.prm.prm.prm_ID+" of: T"+transport.getArea().id+" "+transport.transport_ID);
//				}
				c_prm.currentWorker = null;
				//c_prm.currentTime = this.expectedRelease;
				//c_prm.currentLocation = this.expectedLocation;
			}
			this.currentPRMs.clear();
			this.currentCap = 0;
		}
		else if(currentSegment == null && this.currentPRMs.size() > 0)
		{
			// TODO: Quick hack, something went wrong if this happens
			
			System.out.println("Warning the program had to clear ghost PRMS on this worker!");
			for(F2_PRM c_prm : this.currentPRMs)
			{
//				if(this.transport.getArea().id == -1)
//				{
//					System.out.println(" Release prm: "+c_prm.prm.prm.prm_ID+" of: T"+transport.getArea().id+" "+transport.transport_ID);
//				}
				c_prm.currentWorker = null;
				//c_prm.currentTime = this.expectedRelease;
				//c_prm.currentLocation = this.expectedLocation;
			}
			this.currentPRMs.clear();
			this.currentCap = 0;
		}
		
		this.currentPRMs.add(prm);
		
		// TODO Check or it can be serviced
		
		final int segmentNr = prm.currentSegment;
		prm.segmentStart[segmentNr] = time;
		prm.segmentHandler[segmentNr] = this;
		
		// The worker stays at the location till the PRM(s) are delivered.
		this.currentLocation = seg.from;
		this.currentTime = time;
		this.expectedRelease = time + seg.getSegmentTime();
		this.expectedLocation	= seg.to;
		this.currentCap = this.currentCap + prm.getCapicityReq();
		
		if(seg.to.getArea() != this.area)
		{
			throw new Error("Transport "+this.workerName()+" serving invalid segment: "+seg);
		}
		
		this.currentSegment = seg;
		
	//	System.out.println("    - Worker T"+transport.getArea().id+" "+transport.transport_ID+" Planned: "+seg+"/"+currentSegment+" "+currentLocation+"/"+seg.from+"=>"+expectedLocation+"/"+seg.to);
	}

	public void reset() 
	{	
		currentLocation = transport.depot;
		currentTime 	= transport.startShift;
		
		expectedLocation = currentLocation;
		expectedRelease = currentTime;
		
		this.lastEndTime = currentTime;
		this.lastLocation = currentLocation;
		
		if(expectedLocation.getArea() != this.area)
		{
			throw new Error("Transport "+this.workerName()+" start on invalid location: "+expectedLocation);
		}
		
		currentCap 		= 0;	
	}

	public Transport getTransport() 
	{
		return transport;
	}

	public LocationAlias getCurrentLocation() 
	{
		return this.currentLocation;
	}
	
	public int  getCurrentCapacity()
	{
		return this.currentCap;
	}
	
	public int getCurrentTime() 
	{
		return this.currentTime;
	}
	
	public Segment getCurrentSegment()
	{
		return this.currentSegment;
	}

	public String workerName() 
	{
		return "T"+this.transport.getArea().id+" "+this.transport.transport_ID;
	}
	
	public void setExpectedLocatoin(LocationAlias loc, int time)
	{
		this.expectedLocation = loc;
		this.expectedRelease  = time;
		
		if(loc.getArea() != this.area)
		{
			throw new Error("Transport "+this.workerName()+" placed on invalid location: "+loc);
		}
	}
	
	public void setCurrentLoction(LocationAlias loc, int time)
	{
		this.currentLocation = loc;
		this.currentTime = time;
		
		if(loc.getArea() != this.area)
		{
			throw new Error("Transport "+this.workerName()+" placed on invalid location: "+loc);
		}
	}

	public int getExpectedRelease() 
	{
		return expectedRelease;
	}

	public LocationAlias getExpectedLocation() 
	{
		return expectedLocation;
	}

	public int getCurrentCap() 
	{
		return currentCap;
	}

	public TreeSet<F2_PRM> getCurrentPRMs() 
	{
		return currentPRMs;
	}

	public int getLastEndTime() 
	{
		return lastEndTime;
	}

	public LocationAlias getLastLocation() 
	{
		return lastLocation;
	}

	public int getMax_cap() 
	{
		return max_cap;
	}
}
