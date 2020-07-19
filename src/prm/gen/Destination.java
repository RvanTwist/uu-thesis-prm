package prm.gen;

import java.util.ArrayList;
import java.util.TreeSet;

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
public final class Destination  implements Comparable<Destination>
{	
	
	final public String destination;
	
	TreeSet<Airplane> arriving = new TreeSet();
	TreeSet<Airplane> departing = new TreeSet();
	
	public Destination(String destination)
	{
		this.destination = destination;
	}
	
	public void addArrivingAirplane(Airplane p)
	{
		if(p.arrival && p.destination == this)
		{
			this.arriving.add(p);
		}
		else
		{
			throw new Error("Airplane "+p+" doesn't arrive from "+this);
		}
	}
	
	public void addDepartingAirplane(Airplane p)
	{
		if(!p.arrival && p.destination == this)
		{
			this.departing.add(p);
		}
		else
		{
			throw new Error("Airplane "+p+" doesn't depart from "+this);
		}
	}
	
	private static Destination UNKNOWN_DEST;
	
	public static Destination getUnknownDestination()
	{
		if(UNKNOWN_DEST == null)
		{
			UNKNOWN_DEST = new Destination("Unknown");
		}
		
		return UNKNOWN_DEST;
	}

	@Override
	public int compareTo(Destination other) 
	{
		return this.destination.compareTo(other.destination);
	}
}
