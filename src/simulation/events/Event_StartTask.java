package simulation.events;

import simulation.Event;
import simulation.EventQueue;
import simulation.objects.SimulatedWorker;
import simulation.worker.tasks.Task;

/*
 * This program has been developed by Rene van Twist for his master's thesis (under supervision of Han Hoogeveen) within the master program Computing Science at Utrecht University  
 * ęCopyright Utrecht University (Department of Information and Computing Sciences)
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
public class Event_StartTask extends Event 
{
	final SimulatedWorker worker;
	
	public Event_StartTask(double time, SimulatedWorker w)
	{
		super(time);
		
		this.worker = w;
	}
	
	@Override
	public void execute(EventQueue queue) 
	{
		// Make sure its the currentTask otherwise an update ocured and a new one is in effect.
		worker.advancePlanning();
	}
	
	public String toString2()
	{
		return "Event StartTask: "+worker+" task: "+worker.currentTask;
	}

}
