package simulation.worker.tasks;

import simulation.DynamicWorldSimulation;
import simulation.events.Event_StartTask;
import simulation.objects.SimulatedLocation;
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
public class TravelTask extends Task
{

	public final SimulatedLocation nextLocation;
	boolean eventPlanned = false;
	
	@Override
	public int priority()
	{
		return 2;
	}
	
	public TravelTask(SimulatedWorker w,double e, SimulatedLocation n)
	{
		super(e,w,false);
		this.nextLocation = n;
	}
	
	public TravelTask(SimulatedWorker w,double e, SimulatedLocation n, boolean i)
	{
		super(e,w,i);
		this.nextLocation = n;
	}
	
	@Override
	public boolean isTaskFinished() 
	{
		boolean f = worker.location == nextLocation;
		
		if(f)
		{
			this.setCompleted();
		}
		
		return f;
	}

	@Override
	public void startTaskInternal() 
	{
		if(worker.isTraveling() || worker.location == nextLocation)
		{ // Worker is already traveling, wait till he arrives at the location to be redirected
			return;
		}
		
		// Calculate slack
		DynamicWorldSimulation sim = worker.sim;
		double time = sim.getTime();
		
		if(time < this.executetime)
		{ // Postpone event.
			if(eventPlanned)
			{
				return;
			}
//			System.out.println("prostphone task: "+this.executetime);
			sim.addEvent(new Event_StartTask(this.executetime,worker));
			this.startTime = this.executetime;
			
			eventPlanned = true;
			return;
		}
		
//		System.out.println("Start travel task to: "+nextLocation+" of "+worker);
		worker.travelTo(nextLocation);
		
	}
	
	@Override
	public String toString()
	{
		return "TravelTask: "+this.executetime+" "+nextLocation.loc;
	}

	@Override
	public boolean equalsTask(Task t) 
	{
//		if(t.executetime != this.executetime)
//			return false;
		
		if(!(t instanceof TravelTask))
		{
			return false;
		}
		
		TravelTask tt = (TravelTask) t;

		return true;
		//return this.nextLocation == tt.nextLocation;
	
	}

}
