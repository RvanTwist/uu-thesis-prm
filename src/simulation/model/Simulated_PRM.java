package simulation.model;

import prm.problemdef.*;

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
public class Simulated_PRM implements Comparable<Simulated_PRM>
{

	PRM prm;
	
	// Simulation Data
	Location currentLocation = null;
	Transport currentTransport = null;
	int routeProgress = -1;
	
	public Simulated_PRM( prm.problemdef.PRM prm) 
	{
		this.prm = prm;
	}

	@Override
	public int compareTo(Simulated_PRM other) 
	{
		return this.prm.compareTo(other.prm);
	}

	public void arriveAtNextLocation() 
	{
		
	}

	public void loadSollution(Sollution s) 
	{
		// TODO: FINISH THIS.
		
		for(Segment seg : prm.route)
		{
			ScheduledSegment s_seg = s.getScheduling(seg);
		}
	}
}
