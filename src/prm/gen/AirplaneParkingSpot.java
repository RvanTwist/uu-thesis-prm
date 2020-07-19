package prm.gen;

import java.util.TreeSet;

import prm.problemdef.LocationAlias;
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
public class AirplaneParkingSpot implements Comparable<AirplaneParkingSpot>
{
	public final LocationAlias location;
	public final Terminal terminal;
	
	RBTree<Airplane> planes = new RBTree<Airplane>();
	
	public AirplaneParkingSpot(Terminal t, LocationAlias l)
	{
		this.location = l;
		this.terminal = t;
	}
	
	@Override
	public int compareTo(AirplaneParkingSpot other) 
	{
		return this.location.compareTo(other.location);
	}
	
	public AirplaneParkingSpot add(Airplane p)
	{
		if(p.planeLocation != this)
		{
			throw new Error("");
		}
		
		this.planes.add(p);
		
		return this;
	}
	
	public String toString()
	{
		return "PlaneLoc: "+location;
	}

}
