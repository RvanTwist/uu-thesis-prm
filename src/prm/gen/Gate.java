package prm.gen;

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
public class Gate implements Comparable<Gate>
{
	public final RBTree<Airplane> airplanes = new RBTree<Airplane>();
	
	public final LocationAlias location;
	public final Terminal terminal;
	
	
	public Gate(LocationAlias location, Terminal t)
	{
		this.location = location.getAlias(t.area);
		this.terminal = t;
		
		if(t.area.id == 0 || t.area.id == -1)
		{
			throw new Error("Gate at terminal 0 or -1?");
		}
		
		if(t.area != location.getArea())
		{
			throw new Error(" Location and Terminal doesn't share the same area!");
		}
		
		t.addGate(this);
	}
	
	public int compareTo(Gate gate) 
	{
		return location.compareTo(gate.location);
	}
	
	public Terminal getTerminal()
	{
		return this.terminal;
	}

	public Airplane getArrivingPlane(int arrival) 
	{
		for(Airplane pl : airplanes)
		{
			if(pl.arrival && pl.time == arrival)
			{
				return pl;
			}
		}
		return null;
	}

	public Airplane getDepartingPlane(int dep) 
	{
		for(Airplane pl : airplanes)
		{
			if(!pl.arrival && pl.time == dep)
			{
				return pl;
			}
		}
		return null;
	}
	
	public String toString()
	{
		return "Gate "+location;
	}
	 
}
