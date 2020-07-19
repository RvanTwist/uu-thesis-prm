package simulation.model;

import java.util.TreeSet;

import prm.problemdef.LocationAlias;
import prm.problemdef.Segment;

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
public class Gate {

	Terminal terminal;
	LocationAlias location;
	LocationAlias busLocation;
	
	TreeSet<Plane> planes = new TreeSet<Plane>();
	
	public Gate(LocationAlias la, Terminal terminal) 
	{
		this.terminal = terminal;
		this.location = la;
		
		this.busLocation = la.getAlias(terminal.simulation.instance.airsideBusses);
	}
	
	public boolean isBusSide()
	{
		return busLocation != null;
	}

	public Plane makeArrivePlane(int arrive, Segment segment, LocationAlias arrivingLoc, Simulated_PRM prm) 
	{	
		
		int departExpected = arrive + 90; // Bus gates can be accesed more frequently.
		
		Plane plane = null;
		
		for(Plane p : planes)
		{
			if(p.isArriving() && p.gateArrival == arrive)
			{  // Its the same flight
				plane = p;
				break;
			}
		}
		
		if(plane == null)
		{
			plane = new Plane(this, arrive, departExpected);
		}
		
		
		plane.addArriving(prm);
		
		
		return plane;
	}
	
	public Plane makeDepartPlane(int depart, Segment segment, LocationAlias departingLoc, Simulated_PRM prm) 
	{	
		
		int arriveExpected = depart - 90; // Asume there is like 1.5 hours between flights.
		
		Plane plane = null;
		
		for(Plane p : planes)
		{
			if(p.isDeparting() && p.gateDepart == depart)
			{  // Its the same flight
				plane = p;
				break;
			}
		}
		
		if(plane == null)
		{
			plane = new Plane(this, arriveExpected, depart);
		}
		
		plane.addDeparting(prm);
		
		return plane;
	}

}
