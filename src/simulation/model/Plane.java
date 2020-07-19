package simulation.model;

import java.util.ArrayList;
import java.util.TreeSet;

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
public class Plane 
{

	int gateArrival;
	int gateDepart;
	Gate gate;
	
	TreeSet<Simulated_PRM> arriving = new TreeSet<Simulated_PRM>();
	TreeSet<Simulated_PRM> departing = new TreeSet<Simulated_PRM>();
	
	//ArrayList<Simulated_PRM> passengers = new ArrayList<Simulated_PRM>();
	
	public Plane(Gate g, int arrive, int depart)
	{
		this.gateArrival = arrive;
		this.gateDepart = depart;
		this.gate = g;
	}
	
	public void addArriving(Simulated_PRM simprm) 
	{
		this.arriving.add(simprm);
	}

	public void addDeparting(Simulated_PRM simprm) 
	{
		this.departing.add(simprm);
	}
	
	public boolean isArriving()
	{
		return arriving.size() > 0;
	}
	
	public boolean isDeparting()
	{
		return departing.size() > 0;
	}
}
