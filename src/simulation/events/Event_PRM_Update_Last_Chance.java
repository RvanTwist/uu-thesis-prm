package simulation.events;

import prm.ls.model4.CantPlanException;
import prm.ls.model4.M4_DynamicFrameWork;
import prm.ls.model4.M4_PRM;
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
public class Event_PRM_Update_Last_Chance extends Event
{
	final SimulatedPRM sim_prm;
	DynamicWorldSimulation sim;
	
	public Event_PRM_Update_Last_Chance(SimulatedPRM prm)
	{
		this(prm,prm.sim);
	}
	
	public Event_PRM_Update_Last_Chance(SimulatedPRM prm, DynamicWorldSimulation s) 
	{
		super(Math.max(getUpdateDeadline(prm), s.getTime()));
		
		this.sim_prm = prm;
		this.sim = s;
	}

	@Override
	public void execute(EventQueue queue) 
	{
		final int deadline = getUpdateDeadline(sim_prm);
		
		if(deadline < time)
		{ 
			return;
		}
		
		if(sim_prm.prm.departing_Suspended)
		{ // How do we know or its viable or not?
			return;
		}
		
		if(time > deadline)
		{ // Too early!
			sim.addEvent(new Event_PRM_Update_Last_Chance(sim_prm, sim));
			return;
		}
		
		PRM prm = sim_prm.prm;
		
		if(prm.arrival_Suspended || prm.arrival > time)
		{ // Impossible to plan PRM
			sim.world.instance.clients.remove(prm.prm_ID);
			
			M4_PRM m4_prm = sim.world.getCurrentPlanning().getPRM(prm);
			m4_prm.unfix();
			try
			{
				m4_prm.unPlan();
			}
			catch(CantPlanException e)
			{
				e.printStackTrace();
				throw new Error(e);
			}
			
			sim.importPlanning(sim.world.savePlanning());
		}
	}
	
	static int getUpdateDeadline(SimulatedPRM prm)
	{
		return prm.prm.deadline - prm.prm.getMinTravelingTime() - 5;
	}
	
	@Override
	public String toString2()
	{
		return "Event PRM Last Chance: "+sim_prm;
	}

}
