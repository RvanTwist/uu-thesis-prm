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
public class Segment implements Comparable<Segment>
{
	final public LocationAlias from;
	final public LocationAlias to;
	final public Area supervisingArea;
	final public PRM prm;
	
	
	final public int segmentTime;
	final public int segmentId;
	final public boolean embarkment;
	
	final public int original_segmentId;
	
	public static int id_dispenser;
	
	int earliestStart;
	int latestStart;
	
	public Segment(PRM prm, LocationAlias from, LocationAlias to, Area supervisingArea, boolean em) 
	{
		this(-1,prm,from,to,supervisingArea, em);
		
		throw new Error("This call could cause bugs!");
	}
	
	public Segment(final int id, PRM prm, LocationAlias from, LocationAlias to, Area supervisingArea, boolean em) 
	{	
		if(id < 0)
		{
			throw new Error("ID's must be positive!");
		}
		//System.out.println("new Segment "+from+" => "+to+" in "+supervisingArea);
		
		this.original_segmentId = id;
		
		this.prm = prm;
		this.from = from;
		this.to = to;
		this.supervisingArea = supervisingArea;
		this.embarkment = em;
		
		if(from.area != supervisingArea)
		{
			throw new Error("LocationAlias "+from+" is not a member of area "+supervisingArea.id);
		}
		
		if(to.area != supervisingArea)
		{
			throw new Error("LocationAlias "+to+" is not a member of area "+supervisingArea.id);
		}
		
		// TODO: Check or asumpion + 20 min is correct
		
		int tmpSegmentTime = supervisingArea.getDistance(from, to);
		
		if(em)
		{
			if(from == to)
			{
				tmpSegmentTime = 20;
			}
			else
			{
				tmpSegmentTime += 20;
			}
		}
		
		
		if(tmpSegmentTime == 0 && from != to)
		{ // There is some bug in the data set, to ensure valid answers add a travel time of 1.
			this.segmentTime = 1;
		}
		else
		{
			this.segmentTime = tmpSegmentTime;
		}
		
		this.segmentId = id_dispenser;
		id_dispenser++;
	}

	public Segment(PRM prm, LocationAlias from, LocationAlias to,
			Area area, boolean b, int segmentTime) {
		this.original_segmentId = -1;
		
		this.prm = prm;
		this.from = from;
		this.to = to;
		this.supervisingArea = area;
		this.embarkment = false;
		this.segmentTime = segmentTime;
		
		if(from.area != supervisingArea)
		{
			throw new Error("LocationAlias "+from+" is not a member of area "+supervisingArea.id);
		}
		
		if(to.area != supervisingArea)
		{
			throw new Error("LocationAlias "+to+" is not a member of area "+supervisingArea.id);
		}
		
		// TODO: Check or asumpion + 20 min is correct
		
		int tmpSegmentTime = segmentTime;
		
		this.segmentId = id_dispenser;
		id_dispenser++;
	}

	public LocationAlias getFrom() 
	{
		return from;
	}

	public LocationAlias getTo() 
	{
		return to;
	}

	public Area getSupervisingArea() 
	{
		return supervisingArea;
	}
	
	public int getSegmentTime()
	{
		return this.segmentTime;
	}
	
	public String toString()
	{
		String emb = (this.embarkment?"e":"");
		return "Seg:"+segmentId+"("+this.original_segmentId+") ["+this.supervisingArea.id+":"+from.getLocationId()+","+to.getLocationId()+"]"+emb+"("+segmentTime+")["+earliestStart+","+latestStart+"]";
//		return "Segment: "+segmentId+" from: "+from+" to: "+to+" segmentTime: "+segmentTime;
	}

	public boolean isSingleTransport() 
	{
		return this.supervisingArea.isSingleTransport();
	}

	public int getEarliestStart() 
	{
		return this.earliestStart;
	}

	public int getLatestStart() 
	{
		return this.latestStart;
	}

	public String saveString() 
	{
		return " Segment "+from.getLocationId()+";"+to.getLocationId()+";"+supervisingArea.id+";"+(embarkment ? "1" : "0");
	}

	public String simpleString() 
	{
		return "["+this.supervisingArea.id+":"+from.getLocationId()+","+to.getLocationId()+"]("+segmentTime+")";
	}

	public int compareTo(Segment other) 
	{
		return this.segmentId - other.segmentId;
	}

	public void setWindow(int es, int ls) 
	{
		this.earliestStart = es;
		this.latestStart = ls;
	}

	public double getLatestEnd() 
	{
		return this.latestStart + this.segmentTime;
	}
}
