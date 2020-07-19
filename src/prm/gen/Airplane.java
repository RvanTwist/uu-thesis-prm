package prm.gen;

import prm.problemdef.LocationAlias;

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
public class Airplane implements Comparable<Airplane>
{
	public final boolean arrival;
	public final Destination destination;
	
	public final Gate gate;
	public final AirplaneParkingSpot planeLocation;
	
	public final int time;
	
	
	// Analysis data
	public int occurance = 0;
	public int[] instance_occurance = new int[12];
	
	public Airplane(Destination d, boolean arriving, int time, Gate g, LocationAlias pl)
	{	
		this.arrival = arriving;
		this.destination = (d == null 	? Destination.getUnknownDestination()
										: d );
		this.time = time;
		this.gate = g;
//		this.planeLocation = pl;
		
		
		final FlightPlan fp = g.terminal.plan;
		AirplaneParkingSpot tps = fp.getPlaneParkingSpot(pl);
		this.planeLocation = tps;
		
		tps.add(this);
		
		
		// Add destination to Planning.
		fp.destinations.add(this.destination);
		
		if(arriving)
		{
			this.destination.addArrivingAirplane(this);
			fp.arrivingPlanes.add(this);
		}
		else
		{
			this.destination.addDepartingAirplane(this);
			fp.departingPlanes.add(this);
		}
		
		g.airplanes.add(this);
	}

	@Override
	public int compareTo(Airplane other) 
	{
		
		int comp1 = this.time - other.time;
		
		if(comp1 == 0)
		{
			int comp2 = this.gate.compareTo(other.gate);
			
			if(comp2 == 0)
			{
				int comp3 = this.planeLocation.compareTo(other.planeLocation);
				
				if(comp3 == 0)
				{
					if(this.arrival == other.arrival)
						return 0;
					else if(this.arrival)
						return -1;
					else
						return 1;
				}
				
				return comp3;
			}
			
			return comp2;
		}
		else
		{
			return comp1;
		}
	}
	
	public String saveString()
	{
		String s = 	time+";"+
					gate.location.getLocationId()+";"+
					gate.location.getArea().id+";"+
					planeLocation.location.getLocationId()+";"+
					planeLocation.location.getArea().id+";"+
					destination.destination+";"+
					(arrival? "T" : "F")+"\n";
		
		return s;
	}
	
	public static Airplane readPlane(FlightPlan fp, String dataString)
	{
		String[] data = dataString.split(";");
		final int time 			= Integer.parseInt(	data[0]);
		final int locId1  		= Integer.parseInt(	data[1]);
		final int areaId1 		= Integer.parseInt(	data[2]);
		final int locId2 		= Integer.parseInt(	data[3]);
		final int areaId2		= Integer.parseInt(	data[4]);
		final String dest 		= 					data[5];
		final boolean arrival 	= 					data[6].equals("T");
		
		LocationAlias gate = fp.instance.getLoction(locId1, areaId1);
		LocationAlias planeLoc = fp.instance.getLoction(locId2, areaId2);
		
		Airplane plane = ( arrival 	? fp.getArrivingPlaneOrCreate(time, gate, planeLoc)
								 	: fp.getDepartingPlaneOrCreate(time, gate, planeLoc) );
		
		return plane;
	}
	
	public String toString()
	{
		return "Plane "+(this.arrival?'a':'d')+"("+time+") "+gate+" stand: "+planeLocation;
	}
}
