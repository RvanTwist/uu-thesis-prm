package prm.problemdef;

import simulation.model.Plane;

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
public class LocationAlias implements Comparable<LocationAlias>
{
	Location location;
	
	// Area info
	Area area;
	int vertexId;

	public LocationAlias(Location loc)
	{
		if(loc == null)
		{
			throw new Error("Can't give null value as argument for location");
		}
		
		this.location = loc;
		loc.aliases.add(this);
	}
	
	@Override
	public int compareTo(LocationAlias other) 
	{
		if(this.area == other.area)
		{
			return this.vertexId - other.vertexId;
		}
		else
		{
			return this.area.id - other.area.id;
		}
		
	}

	public int getAreaID() 
	{
		return this.vertexId;
	}

	public boolean isSupervised() 
	{
		return this.location.supervised;
	}

	public Area getArea() 
	{
		return this.area;
	}
	
	public int getLocationId()
	{
		return this.location.locationId;
	}
	
	public String toString()
	{
//		return "["+area.id+":"++"]"
		return "[Location:"+location.name+":"+area.id+":"+this.location.supervised+"]";
	}

	public LocationAlias getAlias(Area a)
	{
		return this.location.getAliasOf(a);
	}
	
	public String saveString() 
	{
		return area.id+";"+location.locationId;
	}
	
	public static LocationAlias readString(String string, PRM_Instance instance)
	{
		String[] data =string.split(";");
		final int aid = Integer.parseInt(data[0]);
		final int lid = Integer.parseInt(data[1]);
		final Area a = instance.getArea(aid);
		
		if(a == null)
		{
			throw new Error("There doens't exist an area with id: "+aid);
		}
		
		LocationAlias la = a.getLocation(lid);
		
		if(la == null)
		{
			throw new Error("There doens't exist a location with id: "+lid+" in area "+a);
		}
		
		return la;
	}

	public Location getLocation() 
	{
		return this.location;
	}

	public int getDistanceTo(LocationAlias l) 
	{
		return this.area.getDistance(this, l);
	}

	public LocationAlias getAlias() 
	{
		return this.location.getAliasOf(this);
	}

	public LocationKind getKind() 
	{
		return this.location.getKind();
	}

	public boolean isAliasOf(Location l) 
	{
		return this.location == l;
	}
	
	public LocationAlias getTerminalAlias()
	{
		return this.location.getTerminalAlias();
	}
}
