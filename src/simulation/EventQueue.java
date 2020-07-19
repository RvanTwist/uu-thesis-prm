package simulation;

import java.util.ArrayList;
import java.util.PriorityQueue;

import simulation.ui.SimulationEventListener;

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
public class EventQueue implements Runnable 
{
	public double time = 0;
	
	public PriorityQueue<Event> queue = new PriorityQueue<Event>();
	public ArrayList<Event> pastEvents = new ArrayList<Event>();
	
	private long lastTime = 0;
	private Thread thread = null;
	
	private boolean pauzed = false;
	private double sim_speed = -1;
	
	public Event lastEvent;
	
	boolean running = false;
	boolean stepmode = false;
	
	Event skipToEvent = null;
	
	ArrayList<SimulationEventListener> simEvListeners = new ArrayList<SimulationEventListener>();
	
	public void addEvent(Event e)
	{
		if(e.time < this.time)
			throw new Error("Planning event in the past! "+e.time+" / "+this.time);
		
		this.queue.add(e);
	}
	
	public void addSimulationEventListener(SimulationEventListener sel)
	{
		this.simEvListeners.add(sel);
	}
	
	public void removeSimulationEventListener(SimulationEventListener sel)
	{
		this.simEvListeners.remove(sel);
	}
	
	protected void fireSimulationEventListeners(Event e)
	{
		for(SimulationEventListener sel : this.simEvListeners)
		{
			sel.simulationEventOccured(e);
		}
	}
	
	public <A> PlannedEvent<A> addEvent(double time, EventHandler<A> eh, A event)
	{
		if(time < this.time)
			throw new Error("Planning event in the past!");
		
		final PlannedEvent<A> pe = new PlannedEvent(eh, time, event);
		this.queue.add(pe);
		return pe;
	}
	
	/**
	 * Interupts the current planend event.
	 * @param e
	 */
	public void interuptEvent(PlannedEvent e)
	{
		this.queue.remove(e);
	}
	
	/**
	 * Clears the eventqueue and set time to 0.
	 */
	public void clear()
	{
		this.time = 0;
		this.queue.clear();
	}
	
	public void doSimulation()
	{
		running = true;
		
		while(!queue.isEmpty())
		{
			final Event e = queue.poll();
			
			if(this.time > e.time)
			{
				throw new Error("Event in the past! current: "+this.time+" event: "+e.time);
			}
			
			
			
			if(this.sim_speed > 0)
			{
				double timedif = (e.time - this.time); // In minutes
				long timedifMilis = (long)((timedif * 1000.0 * 60) / this.sim_speed);
				if(!stepmode && timedif > 0)
				{
					long nextTimeStamp = this.lastTime + timedifMilis;
					
					long current_time = System.currentTimeMillis();
					while(!stepmode && current_time < nextTimeStamp)
					{
						try
						{	Thread.sleep(Math.min(100,nextTimeStamp - current_time)); }
						catch(InterruptedException e1)
						{
							e1.printStackTrace();
						}
						
						current_time = System.currentTimeMillis();
					}
					this.lastTime = current_time;
				}
				else
				{
					this.lastTime = System.currentTimeMillis();
				}
			}
			
			while(pauzed)
			{
				try 
				{
					thread.sleep(500);
				} 
				catch (InterruptedException e1) 
				{
					e1.printStackTrace();
				}
			}
			
			this.time = e.time;
//			System.out.println(time+": Event: "+e);
			e.execute(this);
			lastEvent = e;
			pastEvents.add(e);
			
			fireSimulationEventListeners(e);
			
			if(e == skipToEvent)
			{
				this.skipToEvent = null;
			}
			
			if(this.stepmode && skipToEvent == null)
			{
				this.pauzeSim();
			}
		}
		
		running = false;
		
		this.fireSimulationFinishedListeners();
	}

	protected void fireSimulationFinishedListeners() 
	{
		for(SimulationEventListener l : this.simEvListeners)
		{
			l.simulationFinished(this);
		}
		
	}

	public boolean hasEvents() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void runThreadedSimulation()
	{
		if(this.thread == null)
		{
			this.thread = new Thread(this);
			this.thread.start();
		}
	}

	@Override
	public void run() 
	{
		this.doSimulation();
		this.thread = null;
		System.out.println("Simulation finished!");
	}
	
	public void pauzeSim()
	{
		this.pauzed = true;
	}
	
	public void continueSim()
	{
		this.pauzed = false;
	}

	public boolean isRunning() 
	{
		return this.running;
	}
	
	public boolean isPauzed()
	{
		return this.pauzed;
	}

	public void setSpeed(double s) 
	{
		this.sim_speed = s;
	}
	
	public void enableStepMode()
	{
		this.stepmode = true;
	}
	
	public void disableStepMode()
	{
		this.stepmode = false;
	}
	
	public boolean isStepMode()
	{
		return this.stepmode;
	}

	public void setSkipToEvent(Event e) 
	{
		System.out.println("UI: set SkipToEvent: "+e);
		this.skipToEvent = e;
	}
}
