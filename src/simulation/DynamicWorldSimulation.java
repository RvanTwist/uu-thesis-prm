package simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import prm.gen.Airplane;
import prm.gen.FlightPlan;
import prm.ls.model4.*;
import prm.problemdef.*;
import simulation.events.Event_NewPRM;
import simulation.events.Event_Someone_Arrived;
import simulation.objects.SimulatedLocation;
import simulation.objects.SimulatedPRM;
import simulation.objects.SimulatedPlane;
import simulation.objects.SimulatedSegment;
import simulation.objects.SimulatedWorker;
import simulation.worker.tasks.BoardingTask;
import simulation.worker.tasks.PickupAndDeliverTask;
import simulation.worker.tasks.Task;
import simulation.worker.tasks.TravelTask;

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
public class DynamicWorldSimulation
{
	public static boolean DISABLE_NEW_PRMS = false;
	
	
	public final static int MIN_ARRIVAL_SLACK = 10;
	
	public EventQueue eventqueue = new EventQueue();
	
	public final M4_DynamicFrameWork world;
	
	public final TreeMap<PRM, SimulatedPRM> prms = new TreeMap<PRM, SimulatedPRM>();
	public final TreeMap<Transport, SimulatedWorker> workers = new TreeMap<Transport, SimulatedWorker>();
	public final TreeMap<Location, SimulatedLocation> locations = new TreeMap<Location,SimulatedLocation>();
	public final TreeMap<Airplane, SimulatedPlane> planes = new TreeMap<Airplane, SimulatedPlane>();
	
	public Random rand;
	
	int rescheduled = 0;
	
	public Sollution current_Solution = null;
	
	public DynamicWorldSimulation(M4_DynamicFrameWork w,Random r)
	{
		this.world = w;
		this.rand = r;
		
		FlightPlan flightplan = w.getFlightPlan();
		
		for(Airplane p : flightplan.getPlanes())
		{
			if(p != null)
			{
				this.planes.put(p, new SimulatedPlane(this,p));
			}
			
		}
		
		for(M4_Area a : world.getAreas())
		{
			for(LocationAlias la : a.area.locations.values())
			{
				Location l = la.getLocation();
				if(locations.get(l) == null)
				{
					locations.put(l, new SimulatedLocation(this,l));
				}
			}
			
			for(M4_Worker t : a.workers)
			{
				workers.put(t.getTransport(), new SimulatedWorker(this,t));
			}
		}
		
		for(M4_PRM prm : world.getPRMs())
		{
			SimulatedPRM sim_prm = new SimulatedPRM(this,prm);
			prms.put(prm.prm, sim_prm);
			this.addEvent(new Event_Someone_Arrived(prm.prm.arrival, sim_prm));
		}
		
		//*
		if(!DISABLE_NEW_PRMS)
		{
			for(PRM prm : world.newPRMs)
			{
				SimulatedPRM sim_prm = new SimulatedPRM(this,prm);
				prms.put(prm, sim_prm);
				this.eventqueue.addEvent(new Event_NewPRM(sim_prm,this));
			}
		}
		// */
		
		
		System.out.println("- Importing planning:");
		this.importPlanning(w.getCurrentPlanning());
		this.rescheduled = 0;
		
		this.checkflights();
	}
	
	private void checkflights() 
	{
		System.out.println("Checkflights!");
		
		for(SimulatedPlane plane : this.planes.values())
		{
			String prms = "";
			
			if(plane.isArrivingPlane())
			{
				for(SimulatedPRM sim_prm : plane.getArrivingPRMs())
				{
					PRM prm = sim_prm.prm;
					if(prm.arrival != plane.expected_arrive)
					{
						throw new Error("This is odd: "+prm.arrival+"/"+plane.expected_arrive+"/"+plane.plane.time);
					}
					prms += " ("+prm.arrival+")PRM "+prm.prm_ID;
				}
				
//				System.out.println(plane+" "+prms);
			}
			else
			{
				for(SimulatedPRM sim_prm : plane.getDepartingPRMs())
				{
					PRM prm = sim_prm.prm;
					if(prm.deadline != plane.expected_depart)
					{
						throw new Error("This is odd: "+prm.deadline+"/"+plane.expected_depart+"/"+plane.plane.time);
					}
					prms += " ("+prm.deadline+")PRM "+prm.prm_ID;
				}
				
//				System.out.println(plane+" "+prms);
			}
		}
	}

	public void runSimulation()
	{
		System.out.println("Simulatoin started!");
		
		this.eventqueue.doSimulation();
		
		System.out.println("Simulation finished!");
		
		// Score
		System.out.println(" aditional waiting (changeover) time: "+this.getWaitTime());
	}
	
	public void importPlanning(M4_Planning planning)
	{
	
		planning.saveBest();
		this.current_Solution = planning.getBestSolution();
		
		rescheduled++;
		
//		SimulatedWorker.log("\n\n====================\nUpdating schedule : "+this.getTime()+"\n====================");
		
		// Reset the tasks :
		for(SimulatedWorker w : this.workers.values())
		{
			if(w.tasks_History.size() == 0)
				w.tasks_History.add(w.tasks);
			else
				w.tasks_History.set(0, w.tasks);
			
			w.tasks = new ArrayList<Task>();
//			w.tasks.clear();
		}
		
		for(SimulatedPRM prm : this.prms.values())
		{
			prm.relevantTasks.clear();
		}
		
		for(M4_Area a : planning.areas.values())
		{
			for(M4_Worker w : a.workers)
			{
				SimulatedWorker sim_w = this.getSimWorker(w.getTransport());
				sim_w.tasks.clear();
				
				PickupAndDeliverTask lastDeliveryTask = null;
				
				LocationAlias currentLocation = w.getTransport().depot;
				
				int lastTime = 0;
				
				for(M4_Segment seg : w.plannedSegments)
				{
//					SimulatedSegment[] simSegs;
					
					if(seg instanceof M4_WorkerSegment)
					{ 
						continue;
					}
					
//					if(seg instanceof M4_PRM_Segment)
//					{
//						M4_PRM_Segment s = (M4_PRM_Segment)seg;
//						
//						SimulatedPRM s_prm = this.getSimPRM(s.prm.prm);
//						
//						SimulatedSegment s_seg = s_prm.getSegment(s.segment);
//						if(s_seg.isEnded())
//						{ // Segment is already finished, nothing to see here.
//							continue;
//						}
//					}
//					else if(seg instanceof M4_MergedSegment)
//					{
//						M4_MergedSegment ms = (M4_MergedSegment) seg;
//						
//						for(M4_PRM_Segment s : ms.getSegments())
//						{
//							SimulatedPRM s_prm = this.getSimPRM(s.prm.prm);
//							
//							SimulatedSegment s_seg = s_prm.getSegment(s.segment);
//							if(s_seg.isEnded())
//							{ // Segment is already finished, nothing to see here.
//								continue;
//							}
//						}
//					}
					
					Segment				o_seg 	 = seg.segment;
					final LocationAlias from 	 = o_seg.from;
					final LocationAlias to		 = o_seg.to;
					final double 		start 	 = seg.getStartTime() + 0.001;
					final int           end		 = seg.getEndTime();
					final int 			duration = o_seg.segmentTime;
					
					final PickupAndDeliverTask pickup;
					final Task				   travel;
					final PickupAndDeliverTask deliver;
					
					
					
					
					if(currentLocation == from)
					{ // No Traveling required
						
						if(lastDeliveryTask == null || from.isSupervised())
						{
							pickup = new PickupAndDeliverTask(sim_w,start);
							sim_w.tasks.add(pickup);
						}
						else
						{
							pickup = lastDeliveryTask;
							pickup.pickupTime = start;
						}
					}
					else
					{
						// Add Travel to location task
						int travelTime = currentLocation.getDistanceTo(from);
						TravelTask toPickup = new TravelTask(	sim_w,
																Math.max( start - MIN_ARRIVAL_SLACK - travelTime,
																		  lastTime ),
																this.getSimLocation(from));
						sim_w.tasks.add(toPickup);
						
						// pickup task executed on sight.
						pickup = new PickupAndDeliverTask(sim_w,start);
						sim_w.tasks.add(pickup);
					}
					
					
					// Add travel task
					if(o_seg.embarkment)
					{
						travel = new BoardingTask(sim_w,start, o_seg);
					}
					else
					{
						travel = new TravelTask(sim_w,start, this.getSimLocation(to));
					}
					sim_w.tasks.add(travel);
					
					
					deliver = new PickupAndDeliverTask(sim_w,end);
					sim_w.tasks.add(deliver);
					
					if(seg instanceof M4_PRM_Segment)
					{
						M4_PRM_Segment p_seg = (M4_PRM_Segment)seg;
						M4_PRM prm = p_seg.prm;
						SimulatedPRM s_prm = this.getSimPRM(prm.prm);
						
						SimulatedSegment s_seg = s_prm.getSegment(p_seg.segment);
						s_seg.setWorker(sim_w);
						
						if(p_seg.segment == prm.prm.route[prm.prm.route.length-1])
						{
							deliver.addFinish(s_prm);
						}
						else
						{
							deliver.addDelivery(s_prm);
						}
						
						pickup.addPickup(s_prm);
						
						
						addSegments(s_prm,pickup,travel,deliver);
						
					}
					else if(seg instanceof M4_MergedSegment)
					{
						M4_MergedSegment m_seg = (M4_MergedSegment)seg;
						for(M4_PRM_Segment p_seg : m_seg.getSegments())
						{
							M4_PRM prm = p_seg.prm;
							SimulatedPRM s_prm = this.getSimPRM(prm.prm);
							
							if(p_seg.segment == prm.prm.route[prm.prm.route.length-1])
							{
								deliver.addFinish(s_prm);
							}
							else
							{
								deliver.addDelivery(s_prm);
							}
							
							SimulatedSegment s_seg = s_prm.getSegment(p_seg.segment);
							s_seg.setWorker(sim_w);
							
							pickup.addPickup(s_prm);
						}
						
						for(M4_PRM_Segment p_seg : m_seg.getSegments())
						{
							M4_PRM prm = p_seg.prm;
							SimulatedPRM s_prm = this.getSimPRM(prm.prm);
							addSegments(s_prm,pickup,travel,deliver);
						}
					}
					else
					{
						throw new Error("Unknown segment kind: "+seg.getClass());
					}
					
					lastDeliveryTask = deliver;
					lastTime = seg.getEndTime();
					currentLocation = seg.segment.to;
					
					if(pickup.isEmpty())
					{
						sim_w.tasks.remove(pickup);
					}
					
				}
				
				sim_w.initialiseTasks();
			}
		}
		
		for(SimulatedPRM prm : this.prms.values())
		{
			prm.sortTasks();
		}
		
		
		// Start all tasks:
		for(SimulatedLocation sim_loc : this.locations.values())
		{
			sim_loc.updateLocationTasks();
		}
		
		SimulatedWorker.logflush();
	}
	
	private void addSegments(SimulatedPRM prm, PickupAndDeliverTask pickup,
			Task travel, PickupAndDeliverTask deliver) 
	{
		List<Task> tasks = prm.relevantTasks;
		if(!tasks.contains(pickup))
		{
			tasks.add(pickup);
		}
		
		if(pickup.isEmpty())
		{
			while(tasks.remove(pickup));
		}
		
		tasks.add(travel);
		if(!deliver.isEmpty())
		{
			tasks.add(deliver);
		}
	}

	/**
	 * This method initialises the simulation.
	 */
	public void initialiseSimulation()
	{
		
	}

	public SimulatedPRM getSimPRM(PRM prm)
	{
		return prms.get(prm);
	}
	
	public SimulatedWorker getSimWorker(Transport t)
	{
		return this.workers.get(t);
	}
	
	public double getTime() 
	{
		return eventqueue.time;
	}

	public void addEvent(Event e) 
	{
		this.eventqueue.addEvent(e);
	}

	public SimulatedLocation getSimLocation(Location l) 
	{
		return this.locations.get(l);
	}
	
	public SimulatedLocation getSimLocation(LocationAlias la) 
	{
		return this.locations.get(la.getLocation());
	}

	public void runThreadSimulation() 
	{
		System.out.println("Threaded Simulatoin started!");
		
		this.eventqueue.runThreadedSimulation();
	}
	
	public boolean isRunning()
	{
		return this.eventqueue.isRunning();
	}
	
	public boolean isPauzed()
	{
		return this.eventqueue.isPauzed();
	}

	public SimulatedPlane getSimPlane(Airplane p) 
	{
		if(p == null)
		{
			return null;
		}
		
		return this.planes.get(p);
	}
	
	public double getWaitTime()
	{
		double wait = 0;
		
		for(SimulatedPRM sim_prm : this.prms.values())
		{
			M4_PRM m4_prm = world.getCurrentPlanning().getPRM(sim_prm.prm);
			
			if(m4_prm != null && m4_prm.isPlanned())
			{
				for(SimulatedSegment sim_seg : sim_prm.getSegments())
				{
					if(!sim_seg.isFinished())
					{
						System.out.println("Non finished Segment! :"+sim_prm+" "+sim_seg);
					}
					else
					{
						double w = sim_seg.getWait();
						
						if(w > 0)
						{
							PRM prm = m4_prm.prm;
							System.out.println("wait PRM "+prm.prm_ID+" of "+w+" "+sim_seg);
						}
						
						wait += w;
					}
					
					
				}
			}
		}
		
		return wait;
	}

	public int getRescheduleCount() 
	{
		// TODO Auto-generated method stub
		return this.rescheduled;
	}

	public int getUpdateRescheduleCount() 
	{
		return this.world.getUpdateRescheduleCount();
	}

	public int getFullRescheduleCount() 
	{
		return this.world.getFullRescheduleCount();
	}

	public String getDenailOfServiceStr() 
	{
		int count = 0;
		String decPRMs = "";
		for(SimulatedPRM sim_prm : this.prms.values())
		{
			PRM prm = sim_prm.prm;
			M4_PRM m4_prm = world.getCurrentPlanning().getPRM(prm);
			
			if(m4_prm == null || !m4_prm.isPlanned())
			{
				count++;
				decPRMs += "\n\t\tPRM "+prm.prm_ID;
			}
		}
		
		return count+" prms declined"+decPRMs;
	}

	public int getDeclinedCountBooked() 
	{
		int count = 0;
		for(SimulatedPRM sim_prm : this.prms.values())
			if(sim_prm.prm.isPrebooked())
			{
				PRM prm = sim_prm.prm;
				M4_PRM m4_prm = world.getCurrentPlanning().getPRM(prm);
				
				if(m4_prm == null || !m4_prm.isPlanned())
				{
					count++;
				}
			}
		
		return count;
	}
	
	public int getDeclinedCountOther() 
	{
		int count = 0;
		for(SimulatedPRM sim_prm : this.prms.values())
			if(!sim_prm.prm.isPrebooked())
			{
				PRM prm = sim_prm.prm;
				M4_PRM m4_prm = world.getCurrentPlanning().getPRM(prm);
				
				if(m4_prm == null || !m4_prm.isPlanned())
				{
					count++;
				}
			}
		
		return count;
	}

	public int getWorkerUpdateCount() 
	{
		return this.rescheduled;
	}
}
