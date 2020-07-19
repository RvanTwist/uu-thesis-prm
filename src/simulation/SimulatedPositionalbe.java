package simulation;

import prm.problemdef.Area;
import prm.problemdef.LocationAlias;
import simulation.events.Event_SomeoneDeparted;
import simulation.events.Event_Someone_Arrived;
import simulation.objects.SimulatedLocation;
import simulation.ui.InfoPrintable;

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
public abstract class SimulatedPositionalbe implements Comparable<SimulatedPositionalbe>, InfoPrintable
{
	public final int id;
	public final DynamicWorldSimulation sim;
	public SimulatedLocation location;
	public Area area;
	
	
	// Travel data
	public SimulatedLocation previouslocation = null;
	public SimulatedLocation targetLocation = null;
	double timeDepart;
	protected double expectedArrival;
	
	private static int id_dispenser = 0;
	
	public SimulatedPositionalbe(DynamicWorldSimulation sim,SimulatedLocation loc)
	{
		this.sim = sim;
		this.location = loc;
		
		if(loc != null)
		{
			loc.add(this);
		}
		this.id = id_dispenser++;
	}
	
	public SimulatedLocation getLocation()
	{
		return this.location;
	}
	
	public boolean isTraveling()
	{
		return this.targetLocation != null;
	}
	
	public void travelTo(SimulatedLocation loc)
	{
//		System.out.println("TravelTo "+this+" => "+loc.loc+" from: "+this.location.loc);
		
		if(this.isTraveling())
		{
			throw new Error("This person is already traveling. The journey could be interupted and travel towards the new location.");
		}
		
		if(loc == null)
		{
			throw new Error("Can't travel to an void location.");
		}
		
		Area a = this.getArea();
		
		int ori_travelTime = location.getDistanceTo(loc,area);
		double dynamicTravelTime = this.getDynamicTravelingTime(ori_travelTime);
		double expectedArrivalTime = this.sim.getTime() + dynamicTravelTime;
		
		this.previouslocation = this.location;
		this.targetLocation  = loc;
		this.timeDepart      = sim.getTime(); 
		this.expectedArrival = expectedArrivalTime;
		
		Event e1 = new Event_SomeoneDeparted(this.sim.getTime(), this);
		sim.addEvent(e1);
		
		Event e2 = new Event_Someone_Arrived(expectedArrivalTime,this);
		sim.addEvent(e2);
		
	}
	
	public void depart()
	{
		if(this.location != null)
		{
			this.location.remove(this);
		}
		
	}
	
	public void arriveAtNextLocation() 
	{
		final double time = this.sim.getTime();
		if(time != this.expectedArrival || this.targetLocation == null)
		{
//			System.out.println("Dropping event!: "+time+" "+this.expectedArrival);
			return;
		}
		else
		{
			this.location = this.targetLocation;
			this.targetLocation = null;
			this.location.add(this);
			
			this.arriveAtNextLocationInternal();
		}
	}
	
	public abstract double getDynamicTravelingTime(int ott);
	public abstract void arriveAtNextLocationInternal();
	
	@Override
	public int compareTo(SimulatedPositionalbe other)
	{
		return this.id - other.id;
		
	}
	
	@Override
	public String getInfo()
	{
		return "current location: "+this.location+"\n"+
			   "target  location: "+this.targetLocation+"\n"+
			   "expected arrival: "+this.expectedArrival;
		
	}
	
	public Area getArea()
	{
		return this.area;
	}
	
	public double getExpectedArrival()
	{
		return this.expectedArrival;
	}
	
	public void setExpectedArrival(double a)
	{
		if(this.expectedArrival != -1)
		{
			if(a == -1)
			{
				throw new Error("here?");
			}
		}
		
		this.expectedArrival = a;
	}
	
}
