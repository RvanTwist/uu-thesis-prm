package simulation.objects;

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
public class SimulatedSegment 
{
	public final SimulatedPRM prm;
	
	public final Segment seg;
	
	// Chain
	SimulatedSegment prev;
	SimulatedSegment next;
	
	// Dynamic
	public SimulatedWorker assignedWorker = null;
	
	// Data
	int segmentStart = -1;
	int segmentEnd = -1;
	int segmentTakeOver = -1;
	
	public SimulatedSegment(SimulatedPRM prm, Segment seg)
	{
		this.seg = seg;
		this.prm = prm;
	}
	
	public void registerSegmentStart(int time)
	{
		if(segmentStart != -1)
		{
			throw new Error("Already registered!");
		}
		
		this.segmentStart = time;
		
//		if(this.prev != null)
//		{
//			if(!this.seg.from.isSupervised())
//			{ // This prm is taken over;
//				this.prev.segmentEnd = time;
//			}
//		}
	}
	
	public void registerSegmentArrived(int time)
	{
		if(segmentEnd != -1)
		{
			throw new Error("Already registered!");
		}
		
//		if(this.seg.to.isSupervised())
		{
			this.segmentEnd = time;
			
			if(!seg.embarkment)
			{ // Debug code
				final double segtime = this.segmentEnd - this.segmentStart;
				
				if(segtime > 1 && segtime < this.seg.segmentTime * 0.8)
				{
					SimulatedLocation from = prm.previouslocation;
					SimulatedLocation to   = prm.location;
					throw new Error("Segment is significantly faster then supposed to be: "+segtime+"/"+this.seg.segmentTime+
									"\nFrom: "+from+
									"\nTo: "+to+
									"\nDistance: "+(from != null && to != null && prm.area != null ? from.getDistanceTo(to,this.prm.area) : "NVT"));
				}
			}
			
		}
	}
	
	public void registerSegmentTakeover(int time)
	{
		if(segmentTakeOver != -1)
		{
			throw new Error("Already registered!");
		}
		
		{
			this.segmentTakeOver = time;
		}
	}
	
	public boolean isStarted()
	{
		return this.segmentStart != -1;
	}
	
	public boolean isEnded()
	{
		return this.segmentEnd != -1;
	}

	public boolean isEndlocation(SimulatedLocation location) 
	{
		return location.hasAlias(this.seg.to);
	}
	
	public boolean isBeginLocation(SimulatedLocation location)
	{
		return location.hasAlias(this.seg.from);
	}
	
	@Override
	public String toString()
	{
		return this.seg+" times("+this.segmentStart+","+this.segmentEnd+","+this.segmentTakeOver+")";
	}

	public void setWorker(SimulatedWorker w) 
	{
		this.assignedWorker = w;
		
	}

	public boolean isFinished() 
	{
		return this.segmentEnd != -1 && this.segmentTakeOver != -1;
	}

	public double getWait() 
	{
		return this.segmentTakeOver - this.segmentEnd;
	}
	
	public SimulatedSegment getNext()
	{
		return this.next;
	}
	
	public SimulatedSegment getPrev()
	{
		return this.prev;
	}
}
