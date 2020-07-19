package prm.problemdef;

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
public class Transport implements Comparable<Transport>
{
	public final int transport_ID;
	public final String name;
	
	public final LocationAlias depot;
	public final int capicity;
	
	public int startShift;
	public int endShift;
	
	public Transport(int id, int c, LocationAlias d, int start, int end)
	{
		this.name = "Transport "+id;
		this.transport_ID = id;
		this.capicity = c;
		this.depot = d;
		
		this.startShift = start;
		this.endShift = end;
		
		depot.area.transporters.add(this);
	}
	
	public Transport(String n, int id, int c, LocationAlias d, int start, int end)
	{
		this.name = n;
		this.transport_ID = id;
		this.capicity = c;
		this.depot = d;
		
		this.startShift = start;
		this.endShift = end;
		
		depot.area.transporters.add(this);
	}

	public Area getArea() 
	{
		return depot.area;
	}

	@Override
	public int compareTo(Transport other) 
	{
		if(this.getArea() == other.getArea())
		{
			return this.transport_ID - other.transport_ID;
		}
		else
		{
			return this.getArea().compareTo(other.getArea());
		}
	}
}
