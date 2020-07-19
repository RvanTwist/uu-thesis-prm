package simulation.events;

import prm.ls.model4.CantPlanException;
import prm.ls.model4.M4_PRM;
import prm.ls.model4.M4_Planning;
import prm.problemdef.PRM;
import simulation.DynamicWorldSimulation;
import simulation.Event;
import simulation.EventQueue;
import simulation.objects.SimulatedPRM;
import simulation.objects.SimulatedPlane;
import simulation.objects.SimulatedSegment;

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
public class Event_PlaneDelayNotify extends Event
{

	final SimulatedPlane plane;
	
	
	public Event_PlaneDelayNotify(SimulatedPlane p, double time)
	{
		super(time);
		
		this.plane = p;
	}
	
	@Override
	public void execute(EventQueue queue) 
	{
		final boolean arrivingPlane = plane.isArrivingPlane();

		if(arrivingPlane)
		{
			this.executeArriving();
		}
		else
		{
			this.executeDepart();
//			throw new Error("NYI");
		}
	}
	

	private void executeDepart() 
	{
		Iterable<SimulatedPRM> prms = plane.getDepartingPRMs();
			
		if(plane.requireRescheduling())
		{ // Plane require suspending
			
			String prms_s = "";
			boolean rooster_update =false;
			
			for(SimulatedPRM sim_prm : prms)
			{
				
				final PRM prm = sim_prm.prm;
				final M4_PRM m4_prm = sim_prm.sim.world.getCurrentPlanning().getPRM(prm);
				
				rooster_update = true;	
				
				// Disable the arrive event:
				{		
					prm.departing_Suspended = true;
					
					if(m4_prm != null)
					{
						prms_s += " PRM "+prm.prm_ID;
						try
						{
							m4_prm.setDepartSuspended();
						}
						catch(CantPlanException e)
						{
							e.printStackTrace();
							throw new Error(e);
						}
					}
				}
			}
			
//			System.out.println("Suspending depart: "+prms_s);
			
			if(rooster_update)
			{
				M4_Planning p = this.plane.sim.world.getCurrentPlanning();
				this.plane.sim.importPlanning(p);
			}
		}
		
	}

	private void executeArriving() 
	{
		Iterable<SimulatedPRM> prms = plane.getArrivingPRMs();
		DynamicWorldSimulation sim = plane.sim;
		
		// Update objects:
		for(SimulatedPRM sim_prm : prms)
		{
			final PRM prm = sim_prm.prm;
			final M4_PRM m4_prm = sim_prm.sim.world.getCurrentPlanning().getPRM(prm);
			
//			System.out.println("Updating  Arrival time: PRM: "+sim_prm);
			
			sim_prm.actual_Arrive = (int) plane.getActualArrive();
			
			sim_prm.setExpectedArrival( sim_prm.actual_Arrive );
			
			sim.addEvent(new Event_Someone_Arrived(sim_prm.actual_Arrive,sim_prm));
			sim_prm.targetLocation = sim_prm.sim.getSimLocation(prm.checkin);
			
//			sim_prm.sim.addEvent(new Event_PRM_Update_Last_Chance(sim_prm));
			
		}
			
		if(plane.requireRescheduling())
		{ // Plane require suspending
			
//			System.out.println("Suspending");
			boolean rooster_update =false;
			
			for(SimulatedPRM sim_prm : prms)
			{
				final PRM prm = sim_prm.prm;
				final M4_PRM m4_prm = sim_prm.sim.world.getCurrentPlanning().getPRM(prm);
				
				rooster_update = true;	
				
				{		
					prm.arrival_Suspended = true;
					
					if(m4_prm != null)
					{
						m4_prm.unfix();
						try 
						{
							m4_prm.unPlan();
						} 
						catch (CantPlanException e) 
						{
							e.printStackTrace();
							throw new Error(e);
						}
						prm.allowPlan = false;
					}
					
					// Make the PRM unable to plan in
					prm.arrival = prm.deadline;
					prm.initialiseSegments();
					
//					if(prm.departingPlane)
//					{
////						System.out.println("Need to handle catching the plane?");
//					}
				}
			}
			
			if(rooster_update)
			{
				M4_Planning p = this.plane.sim.world.savePlanning();
				this.plane.sim.importPlanning(p);
			}
		}
	}
	
	@Override
	public String toString2()
	{
		return "Event Plane Delay Notify: "+plane;
	}

}
