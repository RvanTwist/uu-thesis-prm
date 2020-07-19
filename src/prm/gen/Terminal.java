package prm.gen;

import java.util.TreeMap;

import prm.problemdef.Area;
import prm.problemdef.LocationAlias;
import prm.problemdef.LocationKind;

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
public class Terminal 
{
	TreeMap<LocationAlias,Gate> gates = new TreeMap<LocationAlias,Gate>();
	final Area area;
	
	public final FlightPlan plan;
	
	public Terminal(Area area, FlightPlan fp)
	{
		this.area = area;
		this.plan = fp;
		
		// Fetch the gates
		for(LocationAlias la : area.locations.values())
		{
			if(la.getKind() == LocationKind.Gate)
			{
				Gate g = new Gate(la,this);
			}
		}
	}
	
	protected void addGate(Gate g)
	{
		if(g.terminal == this)
		{
			if(g.location.getAlias(area) != g.location)
			{
				throw new Error("LocationAlias incorrectly assigned in Gate:\n"+
								"Area: "+area+"\n"+
								"is: "+g.location+"\n"+
								"should be: "+g.location.getAlias(area));
			}
			
			this.gates.put(g.location,g);
		}
		else
		{
			throw new Error("Cannot add this terminal.");
		}
	}
	
	public Gate getGate(LocationAlias la)
	{
		LocationAlias l = la.getAlias(area);
		
		Gate g = this.gates.get(l);
		
		if(g == null)
		{
			g = new Gate(la,this);
		}
		
		return g;
	}
	
	public void filterImposibleFlights(boolean report)
	{
		
	}
}
