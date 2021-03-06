package prm.ls.model1;

import prm.problemdef.Segment;

/*
 * This program has been developed by Rene van Twist for his master's thesis (under supervision of Han Hoogeveen) within the master program Computing Science at Utrecht University  
 * ęCopyright Utrecht University (Department of Information and Computing Sciences)
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
public abstract interface PRM_Node 
{
	/**
	 * Returns the maximum pickup time according the schedule.
	 * 
	 * @return
	 */
	public int getPickupTime();
	
	/**
	 * Returns the minimum delivery time according the schedule.
	 * 
	 * @return
	 */
	public int getDeliveryTime();
	
	/**
	 * Returns the previous node of the PRM's trip.<br/>
	 * If this node is the first node it returns null.
	 * @return
	 */
	public PRM_Node getPrevNode();
	
	/**
	 * Returns the next node of the PRM's trip.<br/>
	 * If this node is the last node it returns null.
	 * @return
	 */
	public PRM_Node getNextNode();
	
	public PRM_Journey getJourney();
	
	/**
	 * Returns the segment this object is representing.
	 * @return
	 */
	public Segment getSegment();
}
