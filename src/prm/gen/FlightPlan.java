package prm.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import prm.problemdef.*;

import rpvt.util.LineParser;
import rpvt.util.RBTree;

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
public class FlightPlan 
{
	TreeSet<Destination> destinations = new TreeSet<Destination>();
	
	public RBTree<Airplane> departingPlanes = new RBTree<Airplane>();
	public RBTree<Airplane> arrivingPlanes  = new RBTree<Airplane>();
	
	TreeMap<Area,Terminal> terminals = new TreeMap<Area,Terminal>();
	
	TreeMap<LocationAlias,AirplaneParkingSpot> planespots = new TreeMap<LocationAlias,AirplaneParkingSpot>();
	
	final PRM_Instance instance;
	
	public FlightPlan(PRM_Instance instance)
	{
		this.instance = instance;
		
		// Create virtual terminals
		for(Area a : instance.world.values())
		{
			if(a != instance.airsideBusses && a != instance.terminalBusses)
			{
				Terminal t = new Terminal(a,this);
				this.terminals.put(a, t);
			}
		}
	}
	
	public void importPlanesFromInstance()
	{
		for(PRM prm : instance.clients.values())
		{
			if(prm.route.length > 0)
			{
				final LocationAlias arrival_gate;
				final LocationAlias arrival_planeLocation;
				
				final LocationAlias departure_gate;
				final LocationAlias departure_planeLocation;
				
				final int arrival_time = prm.arrival;
				final int departure_time = prm.deadline;
				
				Segment start = prm.route[0];
				Segment end   = prm.route[prm.route.length-1];
				
				// Pickup
				if(start.supervisingArea == instance.airsideBusses)
				{
					arrival_planeLocation = start.from;
					arrival_gate = prm.route[1].from;
				}
				else if(start.from.getKind() == LocationKind.Gate)
				{
					arrival_planeLocation 	= start.from;
					arrival_gate			= arrival_planeLocation;
				}
				else
				{
					arrival_planeLocation 	= null;
					arrival_gate			= null;
				}
				
				// Delivery
				if(end.embarkment || end.supervisingArea == instance.airsideBusses)
				{
					if(end.supervisingArea == instance.airsideBusses)
					{
						departure_gate = prm.route[prm.route.length - 2].to;
						departure_planeLocation = end.to;
					}
					else
					{
						departure_gate 			= end.to;
						departure_planeLocation = end.to;
					}
				}
				else
				{
					departure_gate 			= null;
					departure_planeLocation = null;
				}
				
				if(arrival_gate != null)
				{
					Airplane p = this.getArrivingPlaneOrCreate(arrival_time, arrival_gate, arrival_planeLocation);
					p.occurance++;
				}
				
				if(departure_gate != null)
				{
					Airplane p = this.getDepartingPlaneOrCreate(departure_time, departure_gate, departure_planeLocation);
					p.occurance++;
				}
			}
		}
	}
	
	public void makeDirectGate(LocationAlias l)
	{
		final Gate g = new Gate(l,terminals.get(l.getArea()));
	}
	
	public Terminal getTerminal(Area a)
	{
		return this.terminals.get(a);
	}
	
	
	/**
	 * Finds a departing Plane or create one
	 * @param time
	 * @param gate
	 * @param planeLoc
	 * @return
	 */
	public Airplane getDepartingPlaneOrCreate(int time, LocationAlias gate, LocationAlias planeLoc)
	{
		
		Area gateArea = gate.getArea();
		
		Terminal t = getTerminal(gateArea);
		Gate g = t.getGate(gate);
		
		Airplane_SearchItem search = new Airplane_SearchItem();
		search.gate = g;
		search.time = time;
		search.planeLocation = planeLoc;
		Airplane p = departingPlanes.find(search);
		
		if(p == null)
		{
			p = new Airplane(null,false,time,g, planeLoc);
		}
		
		return p;
	}
	
	/**
	 * Finds a arriving Plane or create one
	 * @param time
	 * @param gate
	 * @param planeLoc
	 * @return
	 */
	public Airplane getArrivingPlaneOrCreate(int time, LocationAlias gate, LocationAlias planeLoc)
	{
		
		Area gateArea = gate.getArea();
		
		Terminal t = getTerminal(gateArea);
		Gate g = t.getGate(gate);
		
		Airplane_SearchItem search = new Airplane_SearchItem();
		search.gate = g;
		search.time = time;
		search.planeLocation = planeLoc;
		Airplane p = this.arrivingPlanes.find(search);
		
		if(p == null)
		{
			p = new Airplane(null,true,time,g, planeLoc);
		}
		
		return p;
	}
	
	public AirplaneParkingSpot getPlaneParkingSpot(LocationAlias la)
	{
		AirplaneParkingSpot aps = this.planespots.get(la);
		
		if(aps == null)
		{
			aps = new AirplaneParkingSpot(getTerminal(la.getArea()),la);
			this.planespots.put(la, aps);
		}
		
		return aps;
	}
	
	public void readFlightPlan(String file) throws IOException
	{
		LineParser lp = new LineParser(file);
		lp.readLine();
		
		lp.skipTillLineStartsWith("Arrival ");
		int arrival_count = Integer.parseInt(lp.readedLine().substring(8));
		System.out.println("Reading arrival: "+arrival_count);
		
		for(int i = 0 ; i < arrival_count ; i++)
		{
			String data = lp.readLine();
			Airplane.readPlane(this, data);
		}
		
		lp.skipTillLineStartsWith("Departure ");
		int departure_count = Integer.parseInt(lp.readedLine().substring(10));
		System.out.println("Reading departure: "+arrival_count);
		
		for(int i = 0 ; i < arrival_count ; i++)
		{
			String data = lp.readLine();
			Airplane.readPlane(this, data);
		}
	}

	public Iterable<Airplane> getPlanes() 
	{
		TreeSet<Airplane> ps = new TreeSet<Airplane>();
		
		for(Airplane p : this.arrivingPlanes)
		{
			ps.add(p);
		}
		for(Airplane p : this.departingPlanes)
		{
			ps.add(p);
		}
		
		return ps;
	}
}
