package prm.problemdef;

import java.util.ArrayList;

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
public class Location implements Comparable<Location>
{
	// Location info
	String name;
	final int locationId;
	boolean supervised;
	
	ArrayList<LocationAlias> aliases = new ArrayList<LocationAlias>();
	
	Area mainTerminal;
	LocationAlias mainAlias;
	
	LocationKind kind;
	
	public Location(String name, int lid, boolean supervised, Area a, LocationKind k)
	{	
		this.locationId = lid;
		this.supervised = supervised;
		this.name = name;
		this.kind = k;
		
		this.mainTerminal = a;
		this.mainAlias = new LocationAlias(this);
		a.add(this.mainAlias);
	}
	
	public Location(int lid, boolean supervised, Area a, LocationKind k)
	{
		this(""+lid, lid, supervised, a, k);
	}
	
	public String printAliases()
	{
		String s = "Location "+locationId+" (A:"+mainTerminal.id+") copies:{";
		
		for(LocationAlias la : this.aliases)
		{
			s += " "+la.area.id;
		}
		
		return s += " }";
	}
	
	public String toString()
	{
		return printAliases();
	}

	public LocationAlias getAliasOf(LocationAlias l) 
	{
		if(this.aliases.size() != 2)
		{
			throw new Error(" Only aplicaple on locations with 2 aliases.");
		}
		LocationAlias a1 = this.aliases.get(0);
		LocationAlias a2 = this.aliases.get(1);
		
		if(a1 == l)
		{
			return a2;
		}
		else if(a2 == l)
		{
			return a1;
		}
		else
		{
			throw new Error("LocationAlias "+l+" is not a member of this location!");
		}
		
	}

	public boolean isInArea(Area a) 
	{
		for(LocationAlias la : this.aliases)
		{
			if(la.area == a)
			{
				return true;
			}
		}
		
		return false;
	}

	public LocationAlias getAliasOf(Area a) 
	{
		// TODO make a Map for the Aliases.
		for(LocationAlias la : this.aliases)
		{
			if(la.area.id == a.id)
			{
				return la;
			}
		}
		
		return null;
	}
	public LocationKind getKind()
	{
		return this.kind;
	}

	public int getDistanceTo(Location loc2,Area a) 
	{	
		LocationAlias la1 = this.getAliasOf(a);
		LocationAlias la2 = loc2.getAliasOf(a);
		
		if(la1 == null || la2 == null)
		{
			throw new Error("There is no connection between those 2 locations in the given area!");
		}
		
		return la1.getDistanceTo(la2);
	}
	
	/**
	 * 	@Deprecated!
	 */
	@Deprecated
	public int getDistanceTo(Location loc2) 
	{
		if(true)
			throw new Error("Deprecated errornious method");
		
		int distance = Integer.MAX_VALUE;
		
		for(LocationAlias as : aliases)
		{
			LocationAlias as2 = loc2.getAliasOf(as.area);
			if(as2 != null)
			{
				distance = Math.min(distance, as.getDistanceTo(as2));
			}
		}
		return distance;
	}

	public boolean isSupervised() 
	{
		return this.supervised;
	}

	@Override
	public int compareTo(Location other) 
	{
		// TODO Auto-generated method stub
		return this.locationId - other.locationId;
	}

	public String simpleString() 
	{
		return "["+this.locationId+"]";
	}

	public Iterable<LocationAlias> getAliases() 
	{
		return this.aliases;
	}

	public LocationAlias getTerminalAlias() 
	{
		for(LocationAlias la : aliases)
		{
			final int aid = la.getArea().id;
			if(aid != -1 && aid != 0)
			{ // Its not a busStop
				return la;
			}
		}
		return null;
	}
}
