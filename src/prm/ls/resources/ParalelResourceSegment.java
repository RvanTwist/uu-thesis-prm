package prm.ls.resources;

import prm.ls.SolutionFacture;
import prm.ls.resources.ParalelResource;
import prm.ls.resources.ParalelResourceNode;
import prm.problemdef.*;
import rpvt.util.RBTreeSubSet;

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
public class ParalelResourceSegment implements Comparable<ParalelResourceSegment> 
{	
	public final ParalelResourceNode start;
	public final ParalelResourceNode end;
	ParalelResource 	resource;

	public boolean planned = false;
	int lenght;
	public final int id;
	
	private static int id_dispenser = 0;
	
	public ParalelResourceSegment(ParalelResource resource, final int cap, final int lenght)
	{
		
		this.start = new ParalelResourceNode(cap);
		this.end = new ParalelResourceNode(-cap);
	
		this.lenght = lenght;
		this.resource = resource;
		
		id = id_dispenser++;
	}
	
	@Override
	public int compareTo(ParalelResourceSegment other) 
	{
		final int time =  other.getStartTime() - this.getStartTime();
		
		if(time == 0)
		{
			return other.id - this.id;
		}
		
		return time;
	}
	
	public ParalelResource getResource()
	{
		return resource;
	}
	
	public void costsUnPlan(SolutionFacture f)
	{	
		if(this.planned)
		{	
			this.resource.getScoreIncreaseBetween(this.start.time, this.start.time, this.start.capChange, f);
		}
	}
	
	public int planAt(int time)
	{	
		if(this.planned && time == this.start.getTime())
		{
			return 0;
		}
		
		int changed = 0;
		
		//System.out.println("setTime: "+time+" this.planned: "+this.planned +"start: "+this.start+" end: "+this.end);
		
		if(this.planned)
		{
			changed += this.unPlan();
		}
		else
		{
			this.start.PrevScore = 0;
			this.end.PrevScore = 0;
		}
		
		planned = true;
		
		this.start.setTime(time);
		this.end.setTime(time + lenght);
		
		this.resource.add(this.start);
		this.resource.add(this.end);
		
		final int c2 = this.resource.updateRange(this.start, this.end);
		changed += c2;
		//System.out.println(" range2   : "+c2+" "+changed);
		//System.out.println( "Planned segment: "+changed);
		
		return changed;
	}

	public int getCapicity() 
	{
		return start.capChange;
	}
	
	public int getStartTime()
	{
		return this.start.getTime();
	}
	
	public void getCostsMove(int newStart, SolutionFacture f)
	{	
		final int travelTime = this.lenght;
		final int newEnd = newStart + travelTime;
		final int capChange = start.capChange;
		
		if(this.planned)
		{
			//System.out.println("Planned "+this.start.capChange);
			
			final int oldStart 	= start.getTime();
			final int oldEnd 	= end.getTime();
			if(newEnd <= oldStart || oldEnd <= newStart)
			{ // Disjoined case
				resource.getScoreIncreaseBetween(oldStart, oldEnd, -capChange, f);
				resource.getScoreIncreaseBetween(newStart, newEnd, capChange, f);
			}
			else if(newStart < oldStart)
			{	// Overlapping shifting to earlier
				resource.getScoreIncreaseBetween(newStart, oldStart, capChange, f);
				resource.getScoreIncreaseBetween(newEnd, oldEnd, -capChange, f);
			}
			else
			{ 	// Overlapping shifting to later
				//System.out.println("Debug get CostsMove");
				resource.getScoreIncreaseBetween(oldEnd, newEnd, capChange, f);
			 	resource.getScoreIncreaseBetween(oldStart, newStart, -capChange, f);
			}
		}
		else
		{
			//System.out.println("UnPlanned "+(((Area)resource.representing).isSingleTransport)+" "+this.start.capChange);
			this.resource.getScoreIncreaseBetween(newStart, newEnd, capChange, f);
		}
		
		//System.out.println("Inspecting range ("+newStart+","+newEnd+") for nodes: "+this.start+" "+this.end+" planned: "+planned);
	}
	
	public int getCostsMove(int newStart)
	{
		final int travelTime = this.lenght;
		final int newEnd = newStart + travelTime;
		final int capChange = start.capChange;
		
		if(this.planned)
		{
			final int oldStart 	= start.getTime();
			final int oldEnd 	= end.getTime();
			if(newEnd <= oldStart || oldEnd <= newStart)
			{ // Disjoined case
				return 	resource.getScoreIncreaseBetween(oldStart, oldEnd, -capChange)
						+ resource.getScoreIncreaseBetween(newStart, newEnd, capChange);
			}
			else if(newStart < oldStart)
			{	// Overlapping shifting to earlier
				return 	resource.getScoreIncreaseBetween(newStart, oldStart, capChange) +
					 	+ resource.getScoreIncreaseBetween(newEnd, oldEnd, -capChange);
			}
			else
			{ 	// Overlapping shifting to later
				//System.out.println("Debug get CostsMove");
				return 	resource.getScoreIncreaseBetween(oldEnd, newEnd, capChange)
			 			+ resource.getScoreIncreaseBetween(oldStart, newStart, -capChange);
			}
		}
		else
		{
			
			return this.resource.getScoreIncreaseBetween(newStart, newEnd, capChange);
		}
	}
	
	public int unPlan()
	{
//		// <DEBUG>
//		this.resource.calculateScore();
//		final int oldScore = this.resource.getScore();
//		
//		String fullSet = "\n fullSet";
//		
//		for(ParalelResourceNode n : this.resource.schedule)
//		{
//			fullSet += n+"("+n.PrevScore+") ";
//		}
//		String debugString = ""+this.start+"("+this.start.PrevScore+")\n"+this.end+"("+this.start.PrevScore+")";
//		// </DEBUG>
		
		
		
		int changed = 0;
		
		if(this.planned)
		{
			changed -= this.start.PrevScore;
			changed -= this.end.PrevScore;
			
			this.resource.score -= this.start.PrevScore;
			this.resource.score -= this.end.PrevScore;
			
			this.resource.remove(this.start);
			this.resource.remove(this.end);
			
			this.start.PrevScore = 0;
			this.end.PrevScore = 0;
			
			final int c1 = this.resource.updateRange(this.start, this.end);
			changed += c1;
			
			this.planned = false;
		}
		
//		// <DEBUG>
//		RBTreeSubSet<ParalelResourceNode> subset2 = this.resource.schedule.subSet(start, end);
//		subset2.extendSetLower();
//		subset2.extendSetHigher();
//		
//		String subset1 = "\n after delte: ";
//		
//		for(ParalelResourceNode n : subset2)
//		{
//			subset1 += n+"("+n.PrevScore+") ";
//		}
//		
//		final int newScore = this.resource.score;
//		this.resource.calculateScore();
//		final int realScore = this.resource.score;
//		
//		if(realScore != newScore)
//		{
//			// Extended debugString
//			RBTreeSubSet<ParalelResourceNode> subset = this.resource.schedule.subSet(start, end);
//			subset.extendSetLower();
//			subset.extendSetHigher();
//			
//			debugString += fullSet;
//			
//			debugString += subset1+"\n after recal: ";
//			
//			for(ParalelResourceNode n : subset)
//			{
//				debugString += n+"("+n.PrevScore+") ";
//			}
//			
//			debugString += "\n first: "+this.resource.schedule.first()+" last: "+this.resource.schedule.last();
//			
//			throw new Error(	"Incorrect Score! "+newScore+"/"+realScore+" ("+oldScore+"+"+changed+")\n"+
//								debugString );
//		}
//		// <DEBUG>
		
		return changed;
	}
	
	public void setResource(ParalelResource r)
	{
		if(this.planned)
		{
			throw new Error("can't assign a new ParalelResource when the segment is already planned!");
		}
		
		this.resource = r;
	}
}
