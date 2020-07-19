package prm.ls.model4;

import prm.problemdef.Segment;

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
public class M4_WorkerSegment extends M4_Segment 
{

	M4_Worker worker;
	final boolean shiftStart;
	
	public M4_WorkerSegment(Segment s, M4_Worker worker, boolean start) 
	{
		super(s, worker.area);
		
		this.worker = worker;
		this.updateFeasibleTransports();
		
		this.shiftStart = start;
	}
	
	public void updateFeasibleTransports()
	{	
		this.feasibleTransports.clear();
		
		if(this.worker != null)
		{
			this.feasibleTransports.add(this.worker);
		}
	}
	
	@Override
	public void setWorker(M4_Worker w) throws CantPlanException 
	{
		if(this.worker != w)
		{
			throw new Error("Can't do this!");
		}
		else
		{
			super.setWorker(w);
		}
	}
	
	@Override
	public boolean isPlannedAndPrimary()
	{
		return true;
	}
	
	@Override
	public void setNext(M4_Segment seg)
	{
		if(this.shiftStart || seg == null)
		{
			super.setNext(seg);
		}
		else
		{
			throw new Error("You cannot give a shift end segment a next!");
		}
	}
	
	@Override
	public void setPrevious(M4_Segment seg)
	{
		if(!this.shiftStart || seg == null)
		{
			super.setPrevious(seg);
		}
		else
		{
			throw new Error("You cannot give a shift start segment a previous!");
		}
	}
	
	public String printTimeWindow() 
	{
		int time = this.getStartTime();
		return "["+time+","+time+"]";
	}
	
//	@Override
//	public M4_Worker getWorker()
//	{
//		return this.worker;
//	}

}
