package simulation.events;

import prm.ls.model4.M4_DynamicFrameWork;
import prm.problemdef.Location;
import prm.problemdef.PRM;
import simulation.*;
import simulation.model.*;
import simulation.objects.SimulatedPRM;

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
public class Event_NewPRM extends Event
{
	final SimulatedPRM sim_prm;
	DynamicWorldSimulation sim;
	
	public Event_NewPRM(SimulatedPRM prm, DynamicWorldSimulation s) 
	{
		super(prm.prm.booking_time);
		
		this.sim_prm = prm;
		this.sim = s;
	}

	@Override
	public void execute(EventQueue queue) 
	{
		{
			final PRM prm = sim_prm.prm;
			
			sim.world.instance.clients.put(prm.prm_ID, prm);
			sim.world.solveIncrement((int)sim.getTime(), prm);
			sim.importPlanning(sim.world.getCurrentPlanning());
			
			queue.addEvent(new Event_Someone_Arrived(prm.arrival,sim_prm));
		}
	}
	
	@Override
	public String toString2()
	{
		return "Event New PRM: "+sim_prm;
	}

}
