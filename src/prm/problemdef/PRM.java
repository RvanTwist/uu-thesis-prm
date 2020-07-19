package prm.problemdef;

import java.util.Collection;

import prm.ls.model2.PRM_Planning;

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
public class PRM implements Comparable<PRM>
{
	public final int prm_ID;
	public final String name;
	
	public int arrival;
	public int deadline;
	
	public final LocationAlias checkin;
	public final LocationAlias destination;
	
	public Segment[] route = null;
	
	public boolean arrival_Suspended = false;
	public boolean departing_Suspended = false;
	
	public final int capRequirement;
	
	public boolean arrivingPlane;
	public boolean departingPlane = false;
	//public boolean departingPlane;
	
	public int booking_time = -1;
	
	public boolean booked;
	public boolean allowPlan = true;
	
	public PRM(int prmID, String name, int arrival, int deadline,
			LocationAlias checkin, LocationAlias destination, int capR, boolean b) 
	{
		super();
		prm_ID = prmID;
		this.name = name;
		this.arrival = arrival;
		this.deadline = deadline;
		this.checkin = checkin;
		this.destination = destination;
		this.capRequirement = capR;
		this.booked = b;
	}
	
	public void initialiseSegments()
	{
		if(this.arrival > 20000)
		{
			throw new Error("Arrival time: "+this.arrival+"? "+this.prm_ID);
		}
		
		int start = this.arrival;
		int end = this.deadline;
	
		final Segment firstSeg = route[0];
		
		this.arrivingPlane = firstSeg.from.location.kind == LocationKind.Gate && firstSeg.from != firstSeg.to;
		
		this.departingPlane = false;
		
		final int l2 = route.length - 1;
		
		for(int i = 0 ; i <= l2 ; i++)
		{
			if(start > 20000)
			{
				throw new Error("start time: "+start+"? "+this.prm_ID);
			}
			
			final Segment s1 = this.route[i];
			final Segment s2 = this.route[l2 - i];
			
			if(s1.embarkment)
			{
				this.departingPlane = true;
			}
			
			s1.earliestStart = start;
			start += s1.segmentTime;
			
			if(start > 20000)
			{
				throw new Error(	"start time: "+start+"? "+this.prm_ID+" "+s1.getSegmentTime()+"\n"+
									s1.from+" => "+s1.to);
			}
			
			end -= s2.segmentTime;
			s2.latestStart = end;
		}

		
	}
	
	public void setSegments(Collection<Segment> segments)
	{
		this.setSegments(segments.toArray(new Segment[segments.size()]));
	}
	
	public void setSegments(Segment[] segments)
	{
		if(this.route != null)
		{
			throw new Error("There is aready Segment Data in this object!");
		}
		
		this.route = segments;
		this.initialiseSegments();
	}

	@Override
	public int compareTo(PRM o) 
	{
		return this.prm_ID - o.prm_ID;
	}
	
	public String saveString()
	{
		String s = "PRM "+prm_ID+";"+name+";"+arrival+";"+deadline+";"+capRequirement+
					";"+checkin.getLocationId()+";"+checkin.getAreaID() +
					";"+destination.getLocationId()+";"+destination.getAreaID() +
					";"+route.length+";"+(this.booked ? "1" : "0");
		for(Segment seg : route)
		{
			s = s + "\n" + seg.saveString();
		}
		
		return s;
	}

	public int getMinTravelingTime() 
	{
		final Segment first = route[0];
		final Segment last = route[route.length-1];
		return last.earliestStart + last.segmentTime - first.earliestStart;
	}

	public String printRoute() 
	{
		String rtrn = "{";
		
		for(Segment s : this.route)
		{
			rtrn = rtrn + s.simpleString();
		}
		return rtrn+"}";
	}
	
	public boolean isPrebooked()
	{
		return this.booked;
	}
	
	public String toString()
	{
		return "PRM "+this.prm_ID;
	}
}
