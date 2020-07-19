package simulation.events;

import java.util.ArrayList;

import prm.ls.model4.M4_PRM;
import prm.ls.model4.M4_PRM_SegmentGroup;
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
public class Event_PlaneDelayLastUpdate extends Event
{

	final SimulatedPlane plane;
	
	public Event_PlaneDelayLastUpdate(SimulatedPlane p, double time)
	{
		super(time);
		this.plane = p;
	}
	
	public void execute(EventQueue eq)
	{
//		if(plane.requireRescheduling())
		{
			if(plane.isArrivingPlane())
			{
				this.executeArrive();
			}
			else
			{
				this.executeDepart();
			}
		}
	}
	
	public void executeArrive()
	{
		ArrayList<M4_PRM_SegmentGroup> reschedule = new ArrayList<M4_PRM_SegmentGroup>();
		ArrayList<PRM> newPRMs = new ArrayList<PRM>();
		
		DynamicWorldSimulation sim = plane.sim;
		
		boolean replan = false;
		
		for(SimulatedPRM sim_prm : plane.getArrivingPRMs())
		{	
			PRM prm = sim_prm.prm;
			prm.allowPlan = true;
			final int old_arrive = prm.arrival;
			
			prm.arrival_Suspended = false;
			
			if(plane.requireRescheduling())
			{
				prm.arrival = (int)plane.getActualArrive();
			}
			prm.initialiseSegments();
			
			
			// Need a new PRM_Arrival Event
			
//			System.out.println(plane.sim.getTime()+": set new Arrive "+prm+": "+prm.arrival+" / "+old_arrive);
			
			M4_PRM m4_prm = sim.world.getCurrentPlanning().getPRM(prm);
			
			if(m4_prm != null)
			{ 
				if(m4_prm.isPlanned())
				{
					M4_PRM_SegmentGroup first = m4_prm.getFirstGroup();
					first.reload();
					first.checkValid();
					
					if(!first.isPlanned())
						reschedule.add(first);
					
					for(M4_PRM_SegmentGroup sg : m4_prm.segmentGroups)
					{
						sg.reload();
						sg.checkValid();
						
						if(!sg.isValid())
							replan = true;
						
						if(sg != first && sg.getNext() != null)
						{
							if(!sg.isPlanned())
								reschedule.add(sg);
						}
					}
				}
				else
				{
					newPRMs.add(prm);
				}
			}
			else
			{ // Try to schedule the PRM agian 
				sim.world.instance.clients.put(prm.prm_ID, prm);
				newPRMs.add(prm);
			}	
		}
		
		if(replan || reschedule.size() + newPRMs.size() > 0)
		{
			sim.world.reschedule((int)this.time,sim.world.getCurrentPlanning(),reschedule,newPRMs);
			M4_Planning planning = sim.world.getCurrentPlanning();
			
//			sim.world.savePlanning();
			sim.importPlanning(planning);
		}
		else
		{
//			System.out.println("No Trips affected!");
		}
	}
	
	public void executeDepart() 
	{
		
		ArrayList<M4_PRM_SegmentGroup> reschedule = new ArrayList<M4_PRM_SegmentGroup>();
		ArrayList<PRM> newPRMs = new ArrayList<PRM>();
		
		DynamicWorldSimulation sim = plane.sim;
		boolean replan = false;
		
		boolean planeReplan = plane.requireRescheduling();
		
		
		for(SimulatedPRM sim_prm : plane.getDepartingPRMs())
		{	
			PRM prm = sim_prm.prm;
			final int old_deadline = prm.deadline;
			
			prm.deadline = (int)plane.getActualDepart();
			prm.departing_Suspended = false;
			prm.initialiseSegments();
			
//			System.out.println(plane.sim.getTime()+": set new Deadine "+prm+": "+prm.deadline+" / "+old_deadline);
			
			M4_PRM m4_prm = sim.world.getCurrentPlanning().getPRM(prm);
			
			if(prm.arrival_Suspended)
			{
				int lastUpdate = Event_PRM_Update_Last_Chance.getUpdateDeadline(sim_prm);
				if(lastUpdate > time)
				{
					sim_prm.sim.addEvent(new Event_PRM_Update_Last_Chance(sim_prm));
				}
			}
			else
			{	
				if(m4_prm != null)
				{ 
					if(m4_prm.isPlanned())
					{
						M4_PRM_SegmentGroup last = m4_prm.getLastGroup();
						
						if(planeReplan)
						{
							last.checkValid();
							if(!last.isValid())
								replan = true;
						}
						
						if(!last.isPlanned())
						{
							reschedule.add(last);
						}
					}
				}
			}
			
			// Check or we can intervene 
			if(m4_prm == null)
			{ // Attempt to replan him:
				sim.world.instance.clients.put(prm.prm_ID, prm);
			}
			else if(m4_prm.isPlanned())
			{ // Check what segment is already in process.
				SimulatedSegment sim_seg = sim_prm.getCurrentSegment();
				
				if(sim_seg != null && !sim_seg.isStarted())
					sim_seg = sim_seg.getPrev();
				
				if(this.plane.requireRescheduling())
				{
					if(sim_seg != null)
					{
						SimulatedSegment sim_seg2 = sim_seg;
						while(sim_seg2 != null && !sim_seg2.seg.to.isSupervised())
						{
							sim_seg2 = sim_seg2.getNext();
						}
						
						if(sim_seg2 == null || sim_seg2.getNext() == null)
						{ // There is no lounge visit between.
							// TODO: Catch this case.
							throw new Error("There is no lounge visit in between, catch this case.");
						}
					}
				}
			}
		}
		if(reschedule.size() > 0 || newPRMs.size() > 0)
		{
//			sim.world.reschedule((int)this.time+2);
			sim.world.reschedule((int)this.time,sim.world.getCurrentPlanning(),reschedule,newPRMs);
			
			M4_Planning planning = sim.world.getCurrentPlanning();
			sim.importPlanning(planning);
		}
		else
		{
//			System.out.println("No PRMs affected!");
		}
	}
	
	@Override
	public String toString2()
	{
		return "Event Plane Delay Last Update: "+this.plane;
	}

}
