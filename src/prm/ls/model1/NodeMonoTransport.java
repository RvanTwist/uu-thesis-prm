package prm.ls.model1;

import java.util.List;

import prm.problemdef.Area;
import prm.problemdef.LocationAlias;
import prm.problemdef.Segment;

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
public class NodeMonoTransport  extends Node implements PRM_Node
{
	public PRM_Node prm_prev;
	public PRM_Node prm_next;
	
	public final Segment segment;
	public final PRM_Journey journey;
	
	int slackTime = 0;
	
	public NodeMonoTransport(PRM_Journey j, Segment s)
	{
		this.segment = s;
		this.journey = j;
	}

	@Override
	public int getDeliveryTime() 
	{
		return this.cumTime;
	}

	@Override
	public PRM_Node getNextNode() {
		return prm_next;
	}

	@Override
	public int getPickupTime() {
		throw new Error("This method is not jet implemented and might be avoided.");
	}

	@Override
	public PRM_Node getPrevNode() 
	{
		return this.prm_prev;
	}
	
	@Override
	public boolean updateNode( List<Node> updateList)
	{
		final int oldCumTime = this.cumTime;
		
		if(this.prev == null)
		{ // This node is unscheduled do nothing.
			this.cumTime = -1;
			return true;
		}
		
		// Locations
		final LocationAlias lastVisit 	= this.prev.getEndLocation();
		final LocationAlias from 		= this.segment.getFrom();
		final LocationAlias to 			= this.segment.getTo();
		//final LocationAlias nextVisit 	= this.next.getStartLocation();
		final Area area 				= this.segment.getSupervisingArea();
		
		// time
		final int route_cumTime_arrive = this.prev.cumTime + area.getDistance(lastVisit, from);
		final int segmentLenght = area.getDistance(from, to);
		
		if(this.prm_prev == null)
		{
			this.cumTime = route_cumTime_arrive + segmentLenght;
		}
		else
		{
			final int prm_cumTime_arrive = this.prm_prev.getDeliveryTime();
			this.cumTime = Math.max(	prm_cumTime_arrive, 
										route_cumTime_arrive ) + segmentLenght;
			
			this.slackTime = route_cumTime_arrive - prm_cumTime_arrive;
		}
		
		if(this.cumTime != oldCumTime)
		{
			
		}
		
		return true;
	}

	@Override
	public PRM_Journey getJourney() 
	{
		return this.journey;
	}

	@Override
	public Segment getSegment() 
	{
		return this.segment;
	}

	@Override
	public LocationAlias getEndLocation() 
	{
		return this.segment.getTo();
	}

	@Override
	public LocationAlias getStartLocation() 
	{
		return this.segment.getFrom();
	}

}
