package prm.problemdef;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import prm.ls.model4.M4_Constants;

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
/**
 * Assumes transporters in same area uses same speed.
 * 
 * @author rene
 */
public class Area implements Comparable<Area>
{
	public final static int INT_SAFE2MAX = Integer.MAX_VALUE / 2 - 1;
	
	PRM_Instance instance;
	
	public final TreeMap<Integer,LocationAlias> locations;
	public final List<Transport> transporters;
	
	private ArrayList<TmpMatrixEntry> matrixentries = new ArrayList<TmpMatrixEntry>();
	int[][] distanceMatrix;
	
	public final int id;
	
	public final boolean isSingleTransport;
	
	public ArrayList<LocationAlias> terminalBusStops = null;
	public ArrayList<LocationAlias> lounges = null;
	
	public Area(PRM_Instance instance, int id, boolean singleTransport) 
	{
		this.instance = instance;
		this.id = id;
		
		instance.world.put(id, this);
		
		locations = new TreeMap<Integer,LocationAlias>();
		transporters = new ArrayList<Transport>();
		
		this.isSingleTransport = singleTransport;
	}
	
	public void add(LocationAlias l)
	{
		if(distanceMatrix != null)
		{
			throw new Error("Distance Matrix is already initialized! Cannot add a new location.");
		}
		
		if(l.area != null)
		{
			throw new Error("Locatoin "+l+" is already assigned to an Area!");
		}
		
		l.area = this;
		l.vertexId = locations.size();
		locations.put(l.getLocationId(),l);
	}
	
	public void add(Transport t)
	{
		transporters.add(t);
	}
	
	public void initDistanceMatrix()
	{
		if(this.matrixentries == null)
		{
			throw new Error("Distance matrix already initialised!");
		}
		
		final int size = locations.size();
		this.distanceMatrix = new int[size][size];
		
		for(int x = 0 ; x < size ; x++)
		{
			for(int y = 0 ; y < size ; y++)
			{
				this.distanceMatrix[x][y] = Area.INT_SAFE2MAX;
			}
			this.distanceMatrix[x][x] = 0;
		}
		
		// Go through all entries
		for(TmpMatrixEntry tme : this.matrixentries)
		{
			this.setDistance(tme.fromLoc, tme.toLoc, tme.distance);
		}
		
		// Make sure the triangle inequality is met
		for(int k = 0 ; k < size ; k++)
			for(int x = 0 ; x < size ; x++)
				for(int y = 0 ; y < size ; y++)
				{
					final int dist = this.distanceMatrix[x][k] + this.distanceMatrix[k][y];
					if(dist < this.distanceMatrix[x][y])
					{
						
						//System.out.println(	"Found improvement: "+x+"->"+k+"->"+y+" in Area: "+toString()+"\n"+
						//					"  "+dist+" < "+this.distanceMatrix[x][y]);
						this.distanceMatrix[x][y] = dist;
					}
				}
		
		int hasValueCount = 0;
		int hasNotValueCount = 0;
		
		for(int y = 0 ; y < size ; y++)
			for(int x = 0 ; x < size ; x++)
			{
				if(this.distanceMatrix[x][y] == Area.INT_SAFE2MAX)
				{
					
					/*/ Debug
					LocationAlias xla = null;
					LocationAlias yla = null;
					
					for(LocationAlias la : this.locations.values())
					{
						if(la.vertexId == x)
						{
							xla = la;
						}
						else if(la.vertexId == y)
						{
							yla = la;
						}
							
					}
					System.out.println("from (internalids) "+x+" => "+y+" don't have data "+xla+" => "+yla);
					//*/
					
					hasNotValueCount++;
					this.distanceMatrix[x][y] = 0;
				}
				else
				{
					hasValueCount++;
				}
			}
		
		if(hasNotValueCount > 0 && M4_Constants.ReportMessages)
		{
			System.out.println("Area: "+this.id+" No values detected: "+hasNotValueCount+","+hasValueCount);
		}
		
		// close accepting new distances:
		this.matrixentries = null;
	}
	
	public void setDistance(int lidFrom, int lidTo, int distance)
	{
		if(this.matrixentries == null)
		{
			throw new Error("Area don't accept new distance Entries!");
		}
		
		LocationAlias from = this.locations.get(lidFrom);
		LocationAlias to   = this.locations.get(lidTo);
		
		// Create when they don't exist.
		if(from == null)
		{
			from = this.createNewLocation(lidFrom);
		}
		if(to == null)
		{
			to = this.createNewLocation(lidTo);
		}
		
		this.matrixentries.add(new TmpMatrixEntry(from,to,distance));
	}
	
	private LocationAlias createNewLocation(int lid) 
	{
		Location l = this.instance.getLoction(lid);
		if(l == null)
		{ // Odd a new unknown location has been found.
			throw new Error("Location id: "+lid+" don't exists!");
			
//			l = new Location(lid,false,this,null);
//			final Location l_o = instance.locations.put(lid, l);
//			
//			if(l_o != null)
//			{
//				throw new Error(	"Inserting new location with already existing id: \n"+
//									" "+l_o+"\n"+
//									" "+l);
//			}
//			
//			return l.mainAlias;
		}
		else
		{
			LocationAlias from = new LocationAlias(l);
			this.add(from);
			
			return from;
		}
		
	}

	private void setDistance(LocationAlias from, LocationAlias to, int distance)
	{
		if(from.area != this || to.area != this)
		{
			throw new Error("Area of "+from+" or "+to+" isn't this area "+this);
		}
		
		distanceMatrix[from.vertexId][to.vertexId] = distance;
	}
	
	public int getDistance(LocationAlias u, LocationAlias v) 
	{
		if(u.area != v.area)
		{
			throw new Error("LocationAliases don't share a common area: area "+u.area.id+" => area "+v.area.id+" expected area "+this.id);
		}
		
		if(u.area != this)
		{
			throw new Error("LocationAliases aren't from the expected area: area "+u.area.id+" => area "+v.area.id+" expected area "+this.id);
		}
		
		return this.distanceMatrix[u.vertexId][v.vertexId];
	}

	@Override
	public int compareTo(Area other) 
	{
		return this.id - other.id;
	}
	
	public class TmpMatrixEntry
	{
		public final LocationAlias fromLoc; 
		public final LocationAlias toLoc;
		public final int distance;
		
		public TmpMatrixEntry(LocationAlias fromLoc, LocationAlias toLoc, int distance) 
		{
			if(fromLoc == null)
			{
				throw new Error("Something went wrong!");
			}
			
			if(toLoc == null)
			{
				throw new Error("Something went wrong!");
			}
			
			this.fromLoc = fromLoc;
			this.toLoc = toLoc;
			this.distance = distance;
		}
	}

	public LocationAlias getLocation(int lid) 
	{
		return this.locations.get(lid);
	}

	public boolean isSingleTransport() 
	{
		return this.isSingleTransport;
	}

	public int getWorkerCount() 
	{
		return transporters.size();
	}
	
	public int getMaxAreaCapicity()
	{
		int cap = 0;
		for(Transport t : this.transporters)
		{
			cap += t.capicity;
		}
		
		return cap;
	}

	public ArrayList<LocationAlias> getTerminalBusStops() 
	{
		if( terminalBusStops == null )
		{ // Find terminal busses
			final Area busa = this.instance.terminalBusses;
			terminalBusStops = new ArrayList<LocationAlias>();
			
			for(LocationAlias la : this.locations.values())
			{
				if(la.location.isInArea(busa))
				{
					terminalBusStops.add(la);
				}
			}
		}
		
		return terminalBusStops;
	}

	public ArrayList<LocationAlias> getLounges() 
	{
		if(lounges == null)
		{
			lounges = new ArrayList<LocationAlias>();
			for(LocationAlias la : this.locations.values())
			{
				if(la.isSupervised())
				{
					lounges.add(la);
				}
			}
			
			if(lounges.size() == 0)
			{
				System.out.println("\u001B[32m Warning: No lounges found in area "+this);
			}
		}
		
		return lounges;
		
	}
}
