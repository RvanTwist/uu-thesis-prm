package simulation.worker.tasks;

import java.util.LinkedList;

import prm.ls.model4.M4_PRM;

import simulation.events.Event_StartTask;
import simulation.objects.SimulatedPRM;
import simulation.objects.SimulatedWorker;

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
public class PickupAndDeliverTask extends Task
{
	public LinkedList<SimulatedPRM> deliver = new LinkedList<SimulatedPRM>();
	public LinkedList<SimulatedPRM> pickup = new LinkedList<SimulatedPRM>();
	public LinkedList<SimulatedPRM> finish = new LinkedList<SimulatedPRM>();
	
	public double pickupTime;
	
	boolean eventPlanned = false;
	
	public PickupAndDeliverTask(SimulatedWorker w,double e)
	{
		super(e,w,false);
		this.pickupTime = e;
	}
	
	public PickupAndDeliverTask(SimulatedWorker w,double e, boolean i)
	{
		super(e,w,i);
		this.pickupTime = e;
	}
	
	@Override
	public int priority()
	{
		return 1;
	}
	
	@Override
	public boolean isTaskFinished() 
	{
		for(SimulatedPRM prm : this.finish)
		{
			if(prm.getWorker() == worker)
			{
				return false;
			}
		}
		
		for(SimulatedPRM prm : deliver)
		{
			if(prm.getWorker() == worker)
			{
				return false;
			}
		}
		for(SimulatedPRM prm : pickup)
		{
			if(prm.getWorker() != worker)
			{
				return false;
			}
		}
		
//		System.out.println("Finshed task : "+this);
		this.setCompleted();
		return true;
	}

	@Override
	public void startTaskInternal() 
	{
		attemptDeliver(worker);
		attemptLoad(worker);
		
		if(this.isTaskFinished())
		{
			this.worker.planUpdateTasks();
		}
	}
	
	private void attemptLoad(SimulatedWorker w)
	{
		
		if(w.sim.getTime() < this.pickupTime - 2.0)
		{
			if(!eventPlanned)
			{	
				w.sim.addEvent(new Event_StartTask(this.pickupTime,w));
				eventPlanned = true;
			}
			return;
		}
		
		
		for(SimulatedPRM prm : pickup)
		{
			if(prm.getWorker() != w && w.location.prms.contains(prm) && w.canTake(prm))
			{ // Attempt load
				w.takeOverPRM(prm);
			}
		}
	}
	
	private void attemptDeliver(SimulatedWorker w)
	{
		for(SimulatedPRM prm : finish)
		{
			if(prm.getWorker() == w)
			{ // Deliver PRM
				prm.attemptFinish();
				w.removePRM(prm);
				
			}
		}
		
		// Attempt to deliver the PRM
		if(w.location.isSupervised())
		{
			for(SimulatedPRM prm : deliver)
				if(prm.getWorker() == w)
				{ // Deliver PRM
					w.removePRM(prm);
				}
		}
		else
		{
			// PRMs must be handed over
			for(SimulatedPRM prm : deliver)
				if(prm.getWorker() == w)
				{ 
					for(SimulatedWorker w2 : w.location.workers())
					{
						if(w2.currentTask instanceof PickupAndDeliverTask)
						{
							PickupAndDeliverTask task = (PickupAndDeliverTask) w2.currentTask;
							task.attemptLoad(w2);
						}
					}
				}
			
		}
	}

	public void addPickup(SimulatedPRM prm) 
	{
		// Check or its already present in delivery then just delete it since the PRM
		// doesn't change Travel method
		if(!this.deliver.remove(prm))
		{
			this.pickup.add(prm);
		}
	}
	
	public void addFinish(SimulatedPRM prm)
	{	
		this.finish.add(prm);
	}
	
	public void addDelivery(SimulatedPRM prm) 
	{
		// Check or its already present in delivery then just delete it since the PRM
		// doesn't change Travel method
		if(!this.pickup.remove(prm))
		{
			this.deliver.add(prm);
		}
	}
	
	@Override
	public String toString()
	{
		
		if(this.isEmpty())
		{
			return " Empty Deliver Task? "+this.executetime;
		}
		
		String pickup = "";
		String deliver = "";
		String finish = "";
		
		for(SimulatedPRM prm : this.pickup)
		{
			pickup += " PRM "+prm.prm.prm_ID;
		}
		
		for(SimulatedPRM prm : this.deliver)
		{
			deliver += " PRM "+prm.prm.prm_ID;
		}
		
		for(SimulatedPRM prm : this.finish)
		{
			finish += " PRM "+prm.prm.prm_ID;
		}
		
		if(this.finish.isEmpty())
		{
		
			if(this.deliver.isEmpty())
				return "Pickup Task: "+this.executetime+" "+pickup;
			else if(this.pickup.isEmpty())
				return "Deliver Task: "+this.executetime+" "+deliver;
			else
				return "PickupAndDeliver Task: "+this.executetime+" pickup: "+pickup+" deliver: "+deliver;
		}
		else
		{
			if(this.deliver.isEmpty())
			{
				if(this.pickup.isEmpty())
				{
					return "Finish Task: "+finish;
				}
				else
				{
					return "PickupAndFinish Task: "+this.executetime+" pickup: "+pickup+" finish: "+finish;
				}
			}
			else if(this.pickup.isEmpty())
				return "DeliverAndFinish Task: "+this.executetime+" deliver: "+deliver+" finish: "+finish;
			else
				return "PickupDeliverAndFinish Task: "+this.executetime+" pickup: "+pickup+" deliver: "+deliver+" finish: "+finish;
		}
	}

	@Override
	public boolean equalsTask(Task t) 
	{
		if(t.executetime != this.executetime)
			return false;
		
		if(!(t instanceof PickupAndDeliverTask))
		{
			return false;
		}
		
		PickupAndDeliverTask tt = (PickupAndDeliverTask) t;

		return 	this.deliver.containsAll(tt.deliver) && tt.deliver.containsAll(this.deliver) &&
				this.pickup.containsAll(this.pickup) &&
				this.finish.containsAll(tt.finish) && tt.finish.containsAll(this.finish);
	
	}
	
	public boolean isEmpty() 
	{
		return this.pickup.isEmpty() && this.deliver.isEmpty() && this.finish.isEmpty();
	}
	
}
