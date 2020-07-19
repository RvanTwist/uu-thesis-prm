package simulation.objects;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.TreeSet;

import prm.ls.model4.M4_Segment;
import prm.ls.model4.M4_Worker;
import prm.problemdef.Location;
import prm.problemdef.LocationAlias;
import prm.problemdef.Transport;
import rpvt.util.RBTree;
import simulation.DynamicWorldSimulation;
import simulation.Event;
import simulation.SimulatedPositionalbe;
import simulation.events.Event_StartTask;
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
public class SimulatedWorker extends SimulatedPositionalbe
{
	
	Transport worker;
	public LinkedList<SimulatedPRM> helping = new LinkedList<SimulatedPRM>();
	
	M4_Segment lastCompletedSegment;
	DynamicWorldSimulation simulation;
	
	int progessId = 0;
	
	public Task currentTask;
	
	// This object previously stored all plannings, turned out to be inefficient to go through.
	// Its still used to analyse the new schedule though, maybe better move it to the completed tasks list.
	public LinkedList<ArrayList<Task>> tasks_History = new LinkedList<ArrayList<Task>>();
	
	public ArrayList<Task> completedAndStartedTasks = new ArrayList<Task>();
	
	public ArrayList<Task> tasks = new ArrayList<Task>();
	
	public int load = 0;
	public final int max_Load;
	
	public boolean plannedUpdateTasks = false;
	
	private void rescheduleTasks()
	{	
		//* Debug:
		final Task oldTask = this.currentTask;
		final int pointerTasks = this.progessId;
		//*/// Debug
		
		final double time = sim.getTime();
		
		// Dynamic content
		SimulatedLocation planning_loc = this.sim.getSimLocation(this.worker.depot);
		
		TreeSet<SimulatedPRM> prms = new TreeSet<SimulatedPRM>();
		
		ArrayList<Task> prev_tasks = (this.tasks_History.size() > 0 ? this.tasks_History.getFirst() : null);
		
		int old_offset = 0;
		
		if(prev_tasks != null)
		{ // Require update of Task handeling.
			for(int i = 0 ; i < this.tasks.size() ; i++)
			{
				final Task task = this.tasks.get(i);
				
				Task prevTask = (i + old_offset < prev_tasks.size() ? prev_tasks.get(i+ old_offset) : null );
				
				while(prevTask != null && prevTask.isInserted)
				{
					old_offset++;
					prevTask = (i + old_offset < prev_tasks.size() ? prev_tasks.get(i+ old_offset) : null );
				}
				
				
				if(i >= this.progessId - 1)
				{ // We are now up to date
//					SimulatedWorker.log("!#"+i+"We are up to date: "+this.progessId+" "+task);
					break;
				}
				else if(prevTask != null)
				{
					if(!task.equalsTask(prevTask))
					{ // Mutation in planning found, update the task pointer.
//						SimulatedWorker.log("!#"+i+" Tasks: "+prevTask+" and "+task+" are not comparable!");
						
						this.progessId = i+1;
						break;
					}
					else
					{
						if(prevTask.isCompleted())
						{
							task.setCompleted();
//							SimulatedWorker.log(" #"+i+" Tasks: "+prevTask+" and "+task+" are comparable!");
						}
						else
						{
//							SimulatedWorker.log(" #"+i+" Tasks: "+prevTask+" and "+task+" are comparable and current!");
							this.progessId = i+1;
							break;
						}
					}
				}
				else
				{
//					SimulatedWorker.log(" #"+i+" This shouldn't happen: "+prevTask+" "+task);
					/// This shouldn't happen
				}
				
				if(task instanceof PickupAndDeliverTask)
				{ // Update PRM list
					PickupAndDeliverTask pdt = (PickupAndDeliverTask)task;
					
					prms.removeAll(pdt.deliver);
					prms.removeAll(pdt.finish);
					prms.addAll(pdt.pickup);
					
				}
				else if(task instanceof TravelTask)
				{ // Update supposed location
					TravelTask tt = (TravelTask)task;
					planning_loc = tt.nextLocation;
				}
			}
			
			// Remove PRMs that shouldn't be on this Employee:
			ArrayList<SimulatedPRM> removePRMs = new ArrayList<SimulatedPRM>();
			
			{
//				String prmss = "";
//				for(SimulatedPRM prm : prms)
//				{
//					prmss += " PRM "+prm.prm.prm_ID;
//				}
//				SimulatedWorker.log("Current PRMS "+this);
//				SimulatedWorker.log("Expected PRMS "+prmss);
			}
			
//			SimulatedWorker.log("Removing PRMS:");
			for(SimulatedPRM prm : this.helping)
				if(!prms.contains(prm))
				{
					removePRMs.add(prm);
//					SimulatedWorker.log(" "+prm);
				}
			
			for(SimulatedPRM prm : removePRMs)
				this.removePRM(prm);
			
			// Adds PRMs that should be on this Employee. (Shouldn't happen!)
			PickupAndDeliverTask pd_task = null; 
			PickupAndDeliverTask next_pd = null;

//			SimulatedWorker.log("Pickup PRMS:");
			if(this.tasks.size() > 0 && this.progessId > 0 && this.progessId-1 < this.tasks.size() && this.tasks.get(this.progessId-1) instanceof PickupAndDeliverTask)
			{
				next_pd = (PickupAndDeliverTask)this.tasks.get(this.progessId-1);
			}
			
			for(SimulatedPRM prm : prms)
				if(!this.helping.contains(prm))
				{
					if(next_pd == null || ! (next_pd.deliver.contains(prm) || next_pd.finish.contains(prm)))
					{
						if(pd_task == null)
						{
							pd_task = new PickupAndDeliverTask(this,sim.getTime(),true);
							this.tasks.add(this.progessId-1, pd_task);
//							System.out.println("Added pickup Task");
							
						}
//						SimulatedWorker.log(" "+prm);
						pd_task.pickup.add(prm);
					}
				}
		
			
//			SimulatedWorker.log("Location check: expected: "+planning_loc+" actually: "+this.location+" travel: "+this.targetLocation);
			/// Change the location of the Employee if he has wandered off due an canceled task.
			if(this.isTraveling())
			{ // Check or he is traveling to a valid location.
				Task currentTask = (this.progessId-1 < this.tasks.size() ? this.tasks.get(this.progessId-1): null);
				if(currentTask instanceof TravelTask)
				{
//					SimulatedWorker.log("Current task is a traveling task: "+currentTask);
					// Traveltask fixes his self.
				}
				else if( this.targetLocation != planning_loc)
				{ // Invalid traveling, go back
//					SimulatedWorker.log("Worker is traveling to the wrong location: redirect!");
					this.tasks.add(Math.max(0,Math.min(this.tasks.size(), this.progessId-1)), new TravelTask(this,sim.getTime(),planning_loc,true));
				}
			}
			else if(planning_loc != this.location)
			{
				this.tasks.add(Math.max(0,Math.min(this.tasks.size(), this.progessId-1)), new TravelTask(this,sim.getTime(),planning_loc,true));
			}
		}
		
		String exp_prms = "";
		for(SimulatedPRM prm :prms)
		{
			exp_prms += " PRM "+prm.prm.prm_ID;
		}
		
//		System.out.println(" updated worker: "+this+" old pid: "+pointerTasks+" new pid: "+this.progessId+" expected location: "+planning_loc+" actual location: "+this.location+" prms: "+exp_prms+" oldTask: ");
	}
	
	/**
	 * Helper function to initialise tasks, don't call twice before resetting!
	 */
	public void initialiseTasks()
	{
//		SimulatedWorker.log("\nInitialise Worker: "+this);
//		SimulatedWorker.log(" Current task: "+this.progessId+" : "+this.currentTask);
//		SimulatedWorker.log(" loc: "+this.location+" traveling to: "+this.targetLocation);
		
		Task oldCurr = this.currentTask;
		
		this.rescheduleTasks();
		
		if(this.progessId == 0)
		{
			this.progessId = 1;
		}
		
		if(this.progessId -1 < this.tasks.size() && this.tasks.size() > 0)
		{
			this.currentTask = this.tasks.get(this.progessId -1);
		}
		else
		{
			this.currentTask = null;
		}
		
		// This might be dubble.
//		SimulatedWorker.log("\nOld task: "+oldCurr);
//		SimulatedWorker.log("New task: "+this.currentTask);
//		SimulatedWorker.log("\n Done worker: "+this+" pointer: "+this.currentTask);
		this.advancePlanning();
	}
	
	
	
	public SimulatedWorker(DynamicWorldSimulation dws, M4_Worker w)
	{
		super(dws, dws.getSimLocation(w.getTransport().depot));
		this.simulation = dws;
		this.worker = w.getTransport();
		this.area = this.worker.getArea();
		
		this.max_Load = w.getTransport().capicity;
		
		if(this.location == null)
		{
			
			for(Location l : dws.locations.keySet())
			{
				System.out.println(l);
			}
			throw new Error("Location can't be null! : "+w.getTransport().depot);
		}
	}
	
	@Override
	public void travelTo(SimulatedLocation loc)
	{
		super.travelTo(loc);
		
		for(SimulatedPRM prm : helping)
		{
			prm.previouslocation = this.location;
			prm.setExpectedArrival(this.expectedArrival);
			prm.targetLocation = this.targetLocation;
		}
	}
	
	@Override
	public void depart() 
	{
		super.depart();
		
		for(SimulatedPRM prm : helping)
		{
			prm.depart();
		}
	}
	
	public void takeOverPRM(SimulatedPRM prm)
	{
//		System.out.println(this+": Taking PRM: "+prm);
		
		SimulatedWorker prevW = prm.transport;
		
		if(prevW != null)
		{
			prevW.removePRM(prm);
		}
		
		prm.setTransport(this);
		
		this.load += prm.loadRequirement;
		
		this.advancePlanning();
	}
	
	public void removePRM(SimulatedPRM prm)
	{
		
		if( this.helping.remove(prm) )
		{
			prm.transport = null;
//			System.out.println(this+": Removing PRM: "+prm);
			this.load -= prm.loadRequirement;
			this.planUpdateTasks();
		}
	}

	@Override
	public double getDynamicTravelingTime(int ott) 
	{ // If workers are slacking they better be fired, so assuming they can make it in time.
		double tt = ott;
		
		tt = Math.max(tt, 0.001);
		
		for(SimulatedPRM prm : this.helping)
		{
			tt = Math.max(tt, prm.getDynamicTravelingTime(ott));
		}
		
		return tt;
	}

	@Override
	public void arriveAtNextLocationInternal() 
	{
		for(SimulatedPRM prm : helping)
		{
			prm.arriveAtNextLocation();
		}
		
		advancePlanning();
	}
	
	public void advancePlanning()
	{	
		plannedUpdateTasks = false;
		
		
		// While there are unfinished tasks
		if(this.currentTask != null)
		{	
			this.startCurrentTask();
		}
		
		while( (this.currentTask == null || this.currentTask.isTaskFinished()) )
		{
			if(this.progessId >= this.tasks.size())
			{
				this.currentTask = null;
				return;
			}
			
			this.currentTask = this.tasks.get(this.progessId);
			//this.currentTask = this.tasks.removeFirst();
//			System.out.println(this+" : Poll task: "+this.currentTask);
			this.progessId++;
			this.startCurrentTask();
		}
		
	}
	
	public void startCurrentTask()
	{
		final Task lastRegistered = (this.completedAndStartedTasks.isEmpty() ? null 
				: this.completedAndStartedTasks.get(this.completedAndStartedTasks.size()-1));
		
		if(lastRegistered == null)
		{
			this.completedAndStartedTasks.add(this.currentTask);
		}
		else if(this.currentTask.equalsTask(lastRegistered))
		{
			if(lastRegistered != this.currentTask)
			{
				this.completedAndStartedTasks.set( this.completedAndStartedTasks.size()-1, 
												   this.currentTask);
				
				this.currentTask.startTime = lastRegistered.startTime;
				this.currentTask.finishTime = lastRegistered.finishTime;
			}
		}
		else
		{
			this.completedAndStartedTasks.add(this.currentTask);
		}
		
		this.currentTask.startTask();
	}

	public boolean canTake(SimulatedPRM prm) 
	{
		if(this.isTraveling())
		{
			return false;
		}
		
		if(!prm.isExpectedWorker(this))
		{
			return false;
		}
		
		return (prm.transport != this && this.load + prm.loadRequirement <= this.max_Load);
	}
	
	@Override
	public String toString()
	{
		String prms = "";
		for(SimulatedPRM prm : helping)
		{
			prms += " PRM "+prm.prm.prm_ID;
		}
		
		return "T"+this.worker.getArea().id+"."+this.worker.transport_ID+" PRMS:"+prms;
	}
	
	public void planUpdateTasks() 
	{
		if(!plannedUpdateTasks)
		{
			plannedUpdateTasks =true;
			final Event e = new Event_StartTask(this.sim.getTime(),this);
			this.sim.addEvent(e);
		}
	}
	
	@Override
	public String getInfo()
	{
		String prms = "";
		String tasks = "";
		String tasks_h = "";
		String tasks_c = "";
		
		for(SimulatedPRM prm : this.helping)
		{
			prms += " PRM "+prm.prm.prm_ID;
		}

		for(Task t : this.completedAndStartedTasks)
		{
			char code = (t.finishTime == -1 ? 's' : 'x' );
			tasks_c += String.format("\n  (%.1f , %.1f , %.1f)("+code+") "+t.toString(),t.executetime,t.startTime,t.finishTime);
//			tasks_c += "\n  T("+t.executetime+" , "+t.startTime+" , "+t.finishTime+")("+code+") "+t.toString();
		}
		
//		int max = 2;
//		for(ArrayList<Task> ts : this.tasks_History)
//		{
//			int count = 0;
//			tasks_h += "\nprev planning:\n";
//			for(Task t : ts)
//			{
//				count++;
//				if(t.isCompleted())
//				{
//					tasks_h +="("+t.executetime+")(x) "+t+"\n";
//				}
//				else if(t == this.currentTask)
//				{
//					tasks_h +="("+t.executetime+")(c) "+t+"\n";
//				}
//				else
//				{
//					tasks_h += "("+t.executetime+")(-) "+t+"\n";
//				}
//				
//			}
//			
//			max--;
//			if(max <= 0)
//			{
//				break;
//			}
//		}
		
		int count = 0;
		for(Task t : this.tasks)
		{
			count++;
			if(count < this.progessId)
			{
				tasks +="("+t.executetime+")(x) "+t+"\n";
			}
			else if(count == this.progessId)
			{
				tasks +="("+t.executetime+")(c) "+t+"\n";
			}
			else
			{
				tasks += "("+t.executetime+")(-) "+t+"\n";
			}
			
		}
		
		return this+"\n"+
			   super.getInfo()+"\n"+
			   "PRMS: "+prms+"\n"+
			   "\n"+
			   "current task: "+this.currentTask+"\n\n"+
			   "tasks:\n"+tasks+"\n\n"+
			   "Completed Tasks:"+tasks_c;
//			   "History Tasks:\n"+tasks_h;
		
	}



	public String simpleString() 
	{
		return "T"+this.worker.getArea().id+"."+this.worker.transport_ID;
	}
	
	
	// Logger
	
	public static BufferedWriter logger;
	
	public static void log(String log)
	{
		if(true)
		{
			System.err.println("Stop logging!");
			return;
		}
		
		if(logger == null)
		{
			try
			{ logger = new BufferedWriter(new FileWriter(new File("worker_update_log.txt"))); }
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if(logger != null)
		{
			try
			{ logger.write(log+"\n"); }
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void logflush()
	{
		if(logger != null)
		{
			try 
			{ logger.flush(); } catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public void finishSegments() 
	{
		for(SimulatedPRM prm : this.helping)
		{
			prm.attemptFinish();
		}
		
	}
	
	
}

