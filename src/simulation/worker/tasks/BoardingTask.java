package simulation.worker.tasks;

import prm.problemdef.Segment;
import simulation.events.Event_Boarding;
import simulation.events.Event_SomeoneDeparted;
import simulation.objects.SimulatedPRM;
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
public class BoardingTask extends Task
{
	final Segment segment;
	
	public BoardingTask(SimulatedWorker w,double time, Segment segment)
	{
		super(time,w,false);
		this.segment = segment;
	}
	
	public BoardingTask(SimulatedWorker w,double time, Segment segment, boolean i)
	{
		super(time,w,i);
		this.segment = segment;
	}
	
	@Override
	public boolean isTaskFinished() 
	{
		final double time = worker.sim.getTime();
		final boolean f = time >= segment.getLatestEnd();
		if(f)
		{
			// Fix the segments
			for(SimulatedPRM prm : worker.helping)
			{
				SimulatedSegment boardingSeg = prm.getBoardingSegment();
				
				if(!boardingSeg.isStarted())
				{// We are late.
					SimulatedSegment prev = boardingSeg.getPrev();
					if(prev != null && !prev.isFinished())
						prev.registerSegmentTakeover((int)time);
					
					boardingSeg.registerSegmentStart((int)time);
				}
				if(!boardingSeg.isEnded())
				{
					// Handle the segment stuff
					prm.arriveAtNextLocationInternal();
				}
			}
			
			this.setCompleted();
		}
		return f;
	}

	@Override
	public int priority() 
	{
		return 3;
	}

	@Override
	public void startTaskInternal() 
	{
		
		double finishTime = segment.getLatestEnd();
		double time = worker.sim.getTime();
		
		
		if(!worker.isTraveling() && time < finishTime)
		{
			if(finishTime - time > 45)
			{	
				throw new Error("Boarding Task too long: "+this+"\n"
								+ "\t time: "+time+"\n"
								+ "\t finishTime: "+finishTime+"\n"
								+ "\t PRM deadline: "+segment.prm.deadline+"\n"
								);
			}
			
			worker.sim.addEvent(new Event_Boarding(finishTime,this,worker));
			worker.targetLocation = worker.location;
			worker.setExpectedArrival(finishTime);
			for(SimulatedPRM prm : worker.helping)
			{
				prm.targetLocation = worker.location;
				prm.setExpectedArrival( finishTime );
			}
			
			worker.sim.addEvent(new Event_SomeoneDeparted(worker.sim.getTime(),worker));
		}
	}

	@Override
	public String toString()
	{
		return "BoardingTask "+this.executetime;
	}
	
	@Override
	public boolean equalsTask(Task t) 
	{
		if(t.executetime != this.executetime)
			return false;
		
		if(!(t instanceof BoardingTask))
		{
			return false;
		}
		
		BoardingTask tt = (BoardingTask) t;

		return 	tt.segment == this.segment;
	
	}
}
