package prm.problemdef;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import prm.ls.model4.M4_Constants;

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
public class PRM_Instance 
{
	
	public TreeMap<Integer, Area> world 			= new TreeMap< Integer, Area>();
	public TreeMap<Integer, PRM>  clients 		= new TreeMap< Integer, PRM>();
	public ArrayList<PRM> faultyClients = new ArrayList<PRM>();
	public TreeMap<Integer, Location> locations 	= new TreeMap< Integer, Location>();
	
	public Area airsideBusses;
	public Area terminalBusses;
	
	private int segmentIdDispenser = 0;
	
	public String instanceLoadString = "";
	
	public void addClient(int id, int release, LocationAlias pickup, int deadline, LocationAlias delivery, int capR, boolean booked)
	{
		PRM newPRM = new PRM(id, "PRM_"+id, release, deadline, pickup, delivery, capR,booked);
		clients.put(id, newPRM);
	}

	public PRM_Instance()
	{
		
	}

	public Location getLoction(int lidFrom) 
	{
		return this.locations.get(lidFrom);
	}
	
	public LocationAlias getLoction(int lid, int aid) 
	{
		final Area a = this.getArea(aid);
		return a.getLocation(lid);
	}
	
	public Area getArea(int aid)
	{
		return world.get(aid);
	}
	
	public boolean save(File dir) throws IOException
	{
		if(!dir.exists())
		{
			if(!dir.mkdir())
			{
				return false;
			}
		}
		
		if(!dir.isDirectory())
		{
			return false;
		}
		
		
		File prms = new File( dir.getAbsolutePath()+"/prms.txt");
		
		savePRMS(prms);
		
		return true;
	}

	private void savePRMS(File prms) throws IOException
	{
		BufferedWriter bwr = new BufferedWriter(new FileWriter(prms));
		bwr.write("PRMS: "+this.clients.size()+"\n");
		for(PRM prm : this.clients.values())
		{
			bwr.write(prm.saveString()+"\n");
		}
		
		bwr.close();
	}

	public Iterable<Area> getAreas()
	{
		return world.values();
	}

	/**
	 * Heuristic for adding lounges.
	 */
	public void addLoungeVisits()
	{
		for(PRM prm : clients.values())
			for(Segment seg : prm.route)
			{
				final int segid = seg.segmentId;
				if(segid > segmentIdDispenser)
				{
					segmentIdDispenser = segid;
				}
			}
		segmentIdDispenser++;
		
		for(PRM prm : clients.values())
		{
			if(prm.arrivingPlane)
			{
				if(prm.departingPlane)
				{
					addLoungeTransfer(prm);
				}
				else
				{
					addLoungeArrival(prm);
				}
			}
			else if(prm.departingPlane)
			{
				addLoungeDeparture(prm);
			}
		}
	}
	
	private void addLoungeTransfer(PRM prm)
	{
//		if(true)
//		{
//			addLoungeDeparture(prm);
//			return;
//		}
		
//		System.out.println("Improving route: "+prm.printRoute());
		
		int indexArrival = 0;
		int indexDeparture = 0;
		
		Area departureArea = null;
		Area arrivalArea = null;
		
		// Check or lounge isn't planned already
		boolean arriveLounge = false;
		boolean departLounge = false;
		
		for(int i = 0 ; i < prm.route.length ; i++)
		{	
			Segment seg = prm.route[i];
			
			if(arrivalArea == null && seg.supervisingArea != airsideBusses)
			{
				indexArrival = i;
				arrivalArea = seg.supervisingArea;
				if(seg.to.isSupervised())
					arriveLounge = true; // Already having an lounge at arrival
			}
			
			if(seg.embarkment)
			{
				if(i != 0)
				{
					if(prm.route[i-1].from.isSupervised())
					{
						departLounge = true;
					}
				}
				
				indexDeparture = i-1;
				departureArea = seg.supervisingArea;
			}
		}
		
		Segment[] newSegs;
		
		final int slack = ( prm.deadline - prm.arrival - prm.getMinTravelingTime());
		
		if(slack < 20)
		{ // The slack don't allows us to do 2 lounges
			arriveLounge = true;
		}
		
		if(slack < 10)
		{ // The slack don' allows us to do a single lounge, its better to wait with the PRM.
			return;
		}
		
		if(departureArea == arrivalArea)
		{
			arriveLounge = true;
		}
		
		if(arriveLounge)
		{
			if(departLounge)
				return;
			else
				newSegs = new Segment[prm.route.length+1];
		}
		else
		{
			if(departLounge)
				newSegs = new Segment[prm.route.length+1];
			else
				newSegs = new Segment[prm.route.length+2];
		}
		
		int insertIndex = 0;
		for(int i = 0 ; i < prm.route.length ; i++)
		{
			if(i == indexArrival && !arriveLounge)
			{ // Plan lounge in arriving terminal
				Segment seg = prm.route[i];
				LocationAlias loungeLoc = arrivalArea.getLounges().get(0);
				newSegs[insertIndex] = new Segment(segmentIdDispenser++,prm, seg.from, loungeLoc, arrivalArea, false);
				newSegs[insertIndex+1] = new Segment(segmentIdDispenser++,prm, loungeLoc, seg.to, arrivalArea, false);
				insertIndex += 2;
			}
			else if(i == indexDeparture && ! departLounge)
			{ // Plan lounge in departing terminal
				Segment seg = prm.route[i];
				LocationAlias loungeLoc = departureArea.getLounges().get(0);
				newSegs[insertIndex] = new Segment(segmentIdDispenser++,prm, seg.from, loungeLoc, departureArea, false);
				newSegs[insertIndex+1] = new Segment(segmentIdDispenser++,prm, loungeLoc, seg.to, departureArea, false);
				insertIndex += 2;
			}
			else
			{
				newSegs[insertIndex] = prm.route[i];
				insertIndex++;
			}
		}
		
		prm.route = newSegs;
//		System.out.println(" - result: "+prm.printRoute());
		prm.initialiseSegments();
	}
	
	
	private void addLoungeDeparture(PRM prm)
	{
		
		int indexDeparture = 0;
		Area departureArea = null;
		
		// Check or lounge isn't planned already
		boolean lounge = false;
		for(int i = 0 ; i < prm.route.length ; i++)
		{
			Segment seg = prm.route[i];
			
			if(seg.from.isSupervised())
			{
				lounge = true;
			}
			
			if(seg.to.location.supervised)
			{
				lounge = true;
			}
			
			if(seg.embarkment)
			{
				indexDeparture = i;
				departureArea = seg.supervisingArea;
			}
		}
		
		if(	lounge || 														  // Lounge already planned
			indexDeparture == 0 || 											  // Route starts with departing
			(indexDeparture == 1 && prm.route[0].from == prm.route[0].to) ||  // Route starts near departing.
			(prm.deadline - prm.arrival < prm.getMinTravelingTime() + 10 ))  // Don't make sense if you have to wait very shortly
		{
			return;
		}
		
		// Try to add a lounge before taking off:
		Segment[] newSegs = new Segment[prm.route.length+1];
		LocationAlias loungeLoc = departureArea.getLounges().get(0);
		
		if(loungeLoc == null)
		{
			throw new Error("Why isn't there a lounge here? "+departureArea);
		}
		
		for(int i = 0 ; i < indexDeparture - 1 ; i++)
		{
			newSegs[i] = prm.route[i];
		}
		
		// Paste the lounge:
		final Segment prev = prm.route[indexDeparture-1];
		
		newSegs[indexDeparture - 1] = new Segment(segmentIdDispenser++,prm, prev.from, loungeLoc, departureArea, false);
		newSegs[indexDeparture]		= new Segment(segmentIdDispenser++,prm, loungeLoc,  prev.to, departureArea, false);
		
		for(int i = indexDeparture ; i < prm.route.length; i++)
		{
			newSegs[i + 1] = prm.route[i];
		}
		
		prm.route = newSegs;
		prm.initialiseSegments();
	}
	
	private void addLoungeArrival(PRM prm)
	{	
		// Currently do nothing, we ain't sure how the passeger wants to be handeled.
		
	}
	
	/**
	 * This method classifies the routes and stores that data on the PRM.
	 */
	public void checkRoutes() 
	{
		if(M4_Constants.ReportMessages)
		{
			System.out.println("Checking route:");
		}
		
		int arrivalCount = 0;
		int departureCount = 0;
		int transferCount = 0;
		int otherCount = 0;
		
		for(PRM prm : clients.values())
		{
			// Check route
			final Segment firstSeg = prm.route[0];
			final boolean startGate = firstSeg.from.location.kind == LocationKind.Gate && firstSeg.from != firstSeg.to;
			boolean embarking = false;
			
			String route = "["+firstSeg.from.location.locationId+":"+firstSeg.from.location.kind+"]";
			
			for(Segment seg : prm.route)
			{
				route = route + "["+seg.to.location.locationId+":"+seg.to.location.kind+"]";
				
				if(seg.embarkment)
				{
					route = route + "(embarking)";
					embarking = true;
				}
			}
			
			String routeKind;
			
			if(startGate)
			{
				if(embarking)
				{
					routeKind = "TRANSFER";
					transferCount++;
				}
				else
				{
					routeKind = "ARRIVAL";
					arrivalCount++;
				}
			}
			else if(embarking)
			{
				routeKind = "DEPARTURE";
				departureCount++;
			}
			else
			{
				routeKind = "OTHER";
				otherCount++;
			}
		
			prm.arrivingPlane = startGate;
			prm.departingPlane = embarking;
			
			if(M4_Constants.ReportMessages)
			{
				System.out.println(" - "+routeKind+" ("+prm.getMinTravelingTime()+"/"+(prm.deadline-prm.arrival)+") prm: "+prm.prm_ID+" "+route+"  |  "+prm.printRoute());
			}
		}
		
		if(M4_Constants.ReportMessages)
		{
			System.out.println(" Arrivals   : "+arrivalCount);
			System.out.println(" Departures : "+departureCount);
			System.out.println(" Transfers  : "+transferCount);
			System.out.println(" Other      : "+otherCount);
		}
	}
	
	public String getInstanceLoadString()
	{
		return instanceLoadString;
	}
	
	/**
	 * 
	 */
	public void generateShifts2()
	{
		System.out.println("Generating shifts v 2");
		
		for(Area a : this.world.values())
		{
			int num = a.transporters.size();
			
			int transition_before = 0;
			int transition_after  = 20;
			
			if(num > 0)
			{
				int day_start = 0;
				int mor_start = 3 * 60 + 45;
				int mid_start = mor_start + 8 * 60;
				int eve_start = mid_start + 8 * 60;
				int eve_end	  = eve_start + 8 * 60;
			
				Transport t0 = a.transporters.get(0);
				
				for(Transport t : a.transporters)
				{
					t.startShift = mor_start 	+ transition_before;
					t.endShift 	 = mid_start 	+ transition_after;
				}
				
				for(int i = 0 ; i < num ; i++)
				{
					Transport t = new Transport(10000+a.transporters.size(), t0.capicity, t0.depot, 
												mid_start 	+ transition_before, 
												eve_start	+ transition_after						);
				}
				
				for(int i = 0 ; i < num ; i++)
				{
					Transport t = new Transport(10000+a.transporters.size(), t0.capicity, t0.depot, 
												eve_start 	+ transition_before, 
												eve_end		+ transition_after						);
				}
			
			}
		}
	}
	
	/**
	 * Split in shifts
	 */
	public void generateShifts()
	{
		System.out.println("Generating shifts");
		
		for(Area a : this.world.values())
		{
			int num = a.transporters.size();
			int num2 = num/2;
			int num3 = num - num2;
			
			if(num > 0)
			{
				Transport t0 = a.transporters.get(0);
				int mor_start = t0.startShift;
				int end = t0.endShift;
				
				final int thirth = (end - mor_start)/3;
				
				int mid_start = mor_start + thirth;
				int eve_start = end - thirth;
				
				int i = 0;
				for(Transport t : a.transporters)
				{
					if(i < num2)
					{
						t.startShift = mor_start;
						t.endShift = mid_start;
					}
					else
					{
						t.startShift = mor_start;
						t.endShift = eve_start;
					}
					i++;
				}
				
				for(i = 0 ; i < num2 ; i++)
				{
					Transport t = new Transport(10000+a.transporters.size(), t0.capicity, t0.depot, mid_start, end);
				}
				
				for(i = 0 ; i < num3 ; i++)
				{
					Transport t = new Transport(10000+a.transporters.size(), t0.capicity, t0.depot, eve_start, end);
				}	
			}
		}	
	}

	public void reduceByChance(double d, long seed) 
	{
		int reduceCount = 0;
		final int oldSize = this.clients.size();
		
		Iterator i = clients.entrySet().iterator();
		
		Random r = new Random(seed);
		
		while(i.hasNext())
		{
			final Object next = i.next();
			if(r.nextDouble() < d)
			{
				i.remove();
				reduceCount++;
			}
		}
		
		System.out.println("Reduced the ammount of PRM's randomly by: "+reduceCount+" "+clients.size()+"/"+oldSize+" using seed: "+seed);
	}

	/**
	 * Apearently matrix reflexion could fail, which causes employees not able to transport the same PRM over 2 segments.
	 */
	public void fixMatrixReflext() 
	{
		for(Area a : this.world.values())
		{
			for(int i = 0 ; i < a.distanceMatrix.length ; i++)
			{
				a.distanceMatrix[i][i] = 0;
			}
		}
	}

	public void fixTimeUnfeasibleWindows() 
	{
		// Maybe fix some windows
		
	}
	
	/**
	 *  In the origional data Employees got schedules where they have seas of time at front and in the back.
	 *  This method makes the schedules for Employees tighter without Compromizing the Workflow.
	 *  It makes sure every employee starts at earliest 20 minutes before the first PRM
	 *  and 20 minutes after the last PRM including transport time.
	 */
	public void TightenEmployeeWindows()
	{
		int min_start = Integer.MAX_VALUE;
		int max_end = Integer.MIN_VALUE;
		
		for(PRM prm : this.clients.values())
		{
			final Segment first = prm.route[0];
			final Segment last  = prm.route[prm.route.length-1];
			final Area first_a = first.supervisingArea;
			final Area last_a  = last.supervisingArea;
			final Transport first_t = first_a.transporters.get(0);
			final Transport last_t  = last_a.transporters.get(0);
			
			final int min = prm.arrival - first_a.getDistance(first_t.depot, first.from) - 20;
			final int max = prm.deadline + last_a.getDistance(last.from, last_t.depot) + 20;
			
			if(min < min_start)
				min_start = min;
			
			if(max > max_end)
				max_end = max;
		}
		
		System.out.println("tighten workers: "+min_start+","+max_end);
		
		for(Area a : this.world.values())
		{
			for(Transport t : a.transporters)
			{
				if(t.startShift < min_start)
				{
					t.startShift = min_start;
				}
				if(t.endShift > max_end)
				{
					t.endShift = max_end;
				}
			}
		}
	}
}
