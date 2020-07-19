package simulation.worker.tasks;

import simulation.objects.SimulatedSegment;
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
public abstract class Task implements Comparable<Task>
{
	
	
	public final boolean isInserted;
	public final double executetime;
	public final SimulatedWorker worker;
	public SimulatedSegment seg;
	
	// Data:
	public boolean completed = false;
	public double  startTime = -1;
	public double  finishTime = -1;
	
	public Task(double e, SimulatedWorker w, boolean i)
	{
		this.executetime = e;
		this.worker = w;
		this.isInserted = i;
	}
	
	public final void startTask()
	{
		if(this.startTime == -1)
		{
			this.startTime = worker.sim.getTime();
		}
		this.startTaskInternal();
	}
	
	public abstract void startTaskInternal();
	public abstract boolean isTaskFinished();
	public abstract int priority();
	
	public int compareTo(Task other)
	{
		if(other.executetime == this.executetime)
		{
			//return this.priority() - other.priority();
			return 0;
		}
		else if(this.executetime <  other.executetime)
		{
			return -1;
		}
		else
		{
			return 1;
		}
			
	}
	
	public boolean isInserted() 
	{
		return this.isInserted;
	}
	
	public abstract boolean equalsTask(Task t);
	
	public void setCompleted()
	{
		if(!this.completed)
		{
			this.completed = true;
			this.finishTime = worker.sim.getTime();
		}
	}
	
	public boolean isCompleted()
	{
		return this.completed;
	}
}
