package prm.ls.resources;

import java.util.SortedSet;
import java.util.TreeSet;

import prm.ls.SolutionFacture;
import rpvt.util.RBTree;
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
/**
 * @author rene
 */
public class ParalelResourceV2<A> 
{
	private ParalelResourceNode<A> util_min = new ParalelResourceNode<A>(Integer.MIN_VALUE,Integer.MIN_VALUE);
	private ParalelResourceNode<A> util_max = new ParalelResourceNode<A>(Integer.MAX_VALUE,Integer.MAX_VALUE);
	
	RBTree<ParalelResourceNode<A>> schedule;
	//TreeSet<ParalelResourceNode> schedule;
	
	int score = 0;
	int maxCapicity;
	boolean feasible = true;
	
	final public Object representing;
	
	public String beforeUpdate;
	
	public String debugString;
	
	public ParalelResourceV2(int maxCap)
	{
		this(null, maxCap);
	}
	
	public ParalelResourceV2(Object r, int maxCap)
	{
		this.representing = r;
		
		this.maxCapicity = maxCap;
		this.feasible = true;
		
		this.schedule = new RBTree<ParalelResourceNode<A>>();
	}
	
	public void checkScore()
	{
		final int oldScore = this.score;
		
		score = 0;
		this.feasible = true;
		
		ParalelResourceNode prev = null;
		int cap = 0;
		
		// go through all Resources
		for(ParalelResourceNode r : schedule)
		{
			// calculate score
			// calculate score
			if(prev != null && prev.time != r.time)
			{
				final int timespan = r.time - prev.time;
				if(timespan < 0)
				{
					throw new Error("Something went wrong, found timespan of "+timespan);
				}
				else
				{
					final int scoreChange = getScore(timespan, cap);
					if(r.PrevScore != scoreChange)
					{
						throw new Error(	"PrevScore of : "+r+" is incorrect\n"+
											"  PrevScore: "+r.PrevScore+" must be: "+scoreChange+"\n"	);
					}
					r.PrevScore = scoreChange;
					score += scoreChange;
				}
			}
			else
			{
				if(r.PrevScore != 0)
				{
					throw new Error(	"PrevScore of : "+r+" is incorrect\n"+
										"  PrevScore: "+r.PrevScore+" must be: "+0	);
				}
				r.PrevScore = 0;
			}
			
			cap += r.capChange;
			
			if(r.capicity != cap)
			{
				throw new Error(	"Capicity of : "+r+" is incorrect\n"+
									"  Capicity:  "+r.capicity+" must be: "+cap	);
			}
			
			r.capicity = cap;		
			
			// r is the new prev.
			prev = r;
		}
		
		if(oldScore != score)
		{
			throw new Error("Newly calculated score: "+score+" oldScore: "+oldScore);
		}
	}
	
	public void calculateScore()
	{
		this.score = 0;
		this.feasible = true;
		
		ParalelResourceNode<A> prev = null;
		int cap = 0;
		
		// go through all Resources
		for(ParalelResourceNode<A> r : schedule)
		{
			// calculate score
			if(prev != null && prev.time != r.time)
			{
				final int timespan = r.time - prev.time;
				if(timespan < 0)
				{
					throw new Error("Something went wrong, found timespan of "+timespan);
				}
				else
				{
					final int scoreChange = getScore(timespan, cap);
					r.PrevScore = scoreChange;
					score += scoreChange;
				}
				
				// Check or capicity constraint is hold.
				if(cap > this.maxCapicity)
				{
					feasible = false;
					//*DB Infeasible Detector*/ throw new Error("Infeasible");
				}
			}
			else
			{
				r.PrevScore = 0;
			}
			
			cap += r.capChange;
			r.capicity = cap;
			
			// r is the new prev.
			prev = r;
		}
		
		// Check the last entry.
		if(cap > this.maxCapicity)
		{
			feasible = false;
			//*DB Infeasible Detector*/ throw new Error("Infeasible");
		}
	}
	
	public String debugString()
	{	
		String printOut = "";
		
		for(ParalelResourceNode r : schedule)
		{
			printOut += r.toString();
		}
		
		return "Resource score:"+score+" maxCap: "+this.maxCapicity+"\n" + printOut;
	}
	
	
	public int getScore(int timespan, int capicity)
	{
		return capicity * capicity * timespan;
	}

	/**
	 * Removes the paralelResourceNode from the schedule, no calculation is done.
	 * @param n
	 * @return
	 */
	public boolean remove(ParalelResourceNode n) 
	{
		return this.schedule.remove(n);
	}
	
	/** Adds the paralelResourceNode to the schedule, no calculation is done.
	 * 
	 * @param n
	 * @return
	 */
	public boolean add(ParalelResourceNode n)
	{
		return this.schedule.add(n);
	}
	
	public int getScore()
	{
		return this.score;
	}
	
	public boolean isFeasible()
	{
		return this.feasible;
	}
	
	public int getMaxCapicity()
	{
		return this.maxCapicity;
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @param capChange
	 * @return
	 */
	public int getScoreIncreaseBetween(int from, int to, int capChange)
	{	
		throw new Error("Depriciated!");
	}
	
	public void getScoreIncreaseBetween(int from, int to, int capChange, SolutionFacture f)
	{	
		//System.out.println("GetScoreIncreaseBetween("+from+","+to+","+capChange+")");
		
		// <DEBUG>
//		final int oldFCosts = f.costs;
//		final boolean oldFFeasible = f.feasible;
//		final int oldRScore = this.score;
//		final boolean wasFeasible = this.feasible;
//		
//		this.calculateScore();
//		
//		if(oldRScore != this.score)
//		{
//			throw new Error(" Invalid start condition when checking increase in score: "+oldRScore+"/"+score);
//		}
//		
//		if(wasFeasible != this.feasible)
//		{
//			throw new Error(" Invalid start condition when checking increase in feasibility "+wasFeasible+" / "+feasible);
//		}
		// </DEBUG>
		
		
		if(from == to)
			return;
		
		final ParalelResourceNode<A> n1 = util_min;
		final ParalelResourceNode<A> n2 = util_max;
		
		n1.time = from;
		n2.time = to;
		
		n1.capChange = capChange;
		n2.capChange = -capChange;
		
		SortedSet<ParalelResourceNode<A>> subset = this.schedule.subSet(n1, n2);		
		
		ParalelResourceNode<A> prev = null;
		int change = 0;
		
		for( ParalelResourceNode<A> n : subset)
		{
			if(n.capicity + capChange > this.maxCapicity)
			{
				f.feasible = false;
			}
			
			if(prev != null)
			{
				if( prev.time != n.time)
				{
					final int timespan = n.time - prev.time;
					final int newScore = this.getScore(timespan, prev.capicity + capChange);
					change += newScore - n.PrevScore;
					
					//System.out.println("  for-Prev Scorechange: "+newScore+" - "+n.PrevScore+" = "+(newScore - n.PrevScore));
				}
			}
			else if(from < n.time)
			{
				final int timespan = n.time - from;
				final int prevCap = n.capicity - n.capChange;
				final int oldScore = this.getScore(timespan, prevCap);
				final int newScore = this.getScore(timespan, prevCap + capChange);
				change += newScore - oldScore;
				
				if(prevCap + capChange > this.maxCapicity)
				{
					f.feasible = false;
				}
				
				//System.out.println("  start Scorechange: "+newScore+" - "+oldScore+" = "+(newScore - oldScore));
			}
			
			prev = n;
		}
		
		if(prev == null)
		{ // No data in this range jet
			prev = this.schedule.lower(n1); 
			
			if(prev == null)
			{
				if(capChange > this.maxCapicity)
				{
					f.feasible = false;
				}
				change += this.getScore(to - from, capChange);
				//System.out.println("  no Data Scorechange: "+this.getScore(to - from, capChange));
			}
			else
			{
				final int oldScore = this.getScore(to - from, prev.capicity);
				final int newScore = this.getScore(to - from, prev.capicity + capChange);
				//System.out.println("  empty range prev Scorechange: "+newScore+" - "+oldScore+" = "+(newScore - oldScore));
				
				if(prev.capicity + capChange > this.maxCapicity)
				{
					f.feasible = false;
				}
				
				change += newScore - oldScore;
			}
			
		}
		else if(prev.time < to)
		{
			final int timespan = to - prev.time;
			final int oldScore = this.getScore(timespan, prev.capicity);
			final int newScore = this.getScore(timespan, prev.capicity + capChange);
			change += newScore - oldScore;
			
			if(prev.capicity + capChange > this.maxCapicity)
			{
				f.feasible = false;
			}
			
			//System.out.println("  end Scorechange: "+newScore+" - "+oldScore+" = "+(newScore - oldScore));
		}
		
		f.costs += change;
		
		
		// <DEBUG>
//			final int costFChange = f.costs - oldFCosts;
//			n1.capChange = capChange;
//			n2.capChange = -capChange;
//			
//			n1.PrevScore = 0;
//			n2.PrevScore = 0;
//			
//			// Test insertion
//			this.add(n1);
//			this.add(n2);
//			this.updateRange(n1, n2);
//			
//			if(this.feasible == false && wasFeasible)
//			{
//				if(f.feasible)
//				{
//					throw new Error("Result of capchange caused infeasiblity.");
//				}
//			}
//			
//			if(this.score - oldRScore != costFChange)
//			{
//				throw new Error("Calculation of Score was wrong! ("+(this.score - oldRScore)+" / "+costFChange+")");
//			}
//			
//			// Revert changes:
//			this.remove(n1);
//			this.remove(n2);
//			
//			this.calculateScore();
//			
//			if(oldRScore != this.score || wasFeasible != feasible)
//			{
//				throw new Error("Reverting failed while debugging for some odd reason!");
//			}
		// </DEBUG>
	}

	public int updateRange(ParalelResourceNode<A> start, ParalelResourceNode<A> end) 
	{
		if(start.compareTo(end) > 0)
		{ // Make sure they are
			final ParalelResourceNode<A> tmp = start;
			start = end;
			end   = tmp;
		}
		
		//System.out.println("Update range: ["+start+" "+end+"] maxcap: "+this.maxCapicity+" first: "+this.schedule.first()+" last: "+this.schedule.last());
//		debugString = "Update range: ["+start+" "+end+"] maxcap: "+this.maxCapicity+" first: "+this.schedule.first()+" last: "+this.schedule.last();
		
		RBTreeSubSet<ParalelResourceNode<A>> subset = this.schedule.subSet(start, end);
		subset.extendSetLower();
		subset.extendSetHigher();
		
		//System.out.println("  subset: "+subset.printContentents());
//		debugString += "\n  subset: "+subset.printContentents();
		
		final ParalelResourceNode<A> first = subset.first();
		if(first == null)
			return 0;
		
		
		ParalelResourceNode<A> prev = null;
		int changed = 0;
		int cap;
		
		// Check the first item
		ParalelResourceNode<A> scheduleFirst  = this.schedule.first();
		if(scheduleFirst == first)
		{ // Update the first
			this.score -= scheduleFirst.PrevScore;
			changed -= scheduleFirst.PrevScore;
			scheduleFirst.PrevScore = 0;
			cap = first.capChange;
			first.capicity = cap;
		}
		else
		{
			cap = first.capicity;
		}
		
		//System.out.println("nodes in subset:");
		for( ParalelResourceNode<A> n : subset)
		{
			
			
			if(prev != null)
			{
				if( prev.time != n.time)
				{
					final int timespan = n.time - prev.time;
					final int newScore = this.getScore(timespan, cap);
					this.score += newScore - n.PrevScore;
					
					changed += newScore - n.PrevScore;
					n.PrevScore = newScore;
					
					// Check or capicity constraint is hold.
					if(cap > this.maxCapicity)
					{
						feasible = false;
						//*DB Infeasible Detector*/ throw new Error("Infeasible");
					}
				}
				else
				{
					this.score -= n.PrevScore;
					changed -= n.PrevScore;
					n.PrevScore = 0;
				}
				cap += n.capChange;
			}
			
			n.capicity = cap;
			prev = n;
		}
		
		// Check or capicity constraint is hold.
		if(cap > this.maxCapicity)
		{
			feasible = false;
			//*DB Infeasible Detector*/ throw new Error("Infeasible");
		}
		
		//System.out.println("   after: "+subset.printContentents());
//		debugString += "\n   after: "+subset.printContentents();
		
		return changed;
	}

	public int getHighestCap() 
	{
		int max = 0;
		
		ParalelResourceNode<A> prev = null;
		
		for(ParalelResourceNode<A> n : this.schedule)
		{
			if (prev != null && prev.time < n.time && prev.capicity > max)
			{
				max = prev.capicity;
			}
			
			prev = n;
		}
		
		if(prev != null && prev.capicity > max)
		{
			max = prev.capicity;
		}
			 
		return max;
	}
	
	public int getLowestCap() 
	{
		int min = Integer.MAX_VALUE;
		
		for(ParalelResourceNode<A> n : this.schedule)
		{
			if (n.capicity < min)
			{
				min = n.capicity;
			}
		}
		return min;
	}

	public void planEarliestFrom(final ParalelResourceSegment rs, final int startTime, final int deadline) 
	{
		util_min.time = startTime;
		util_max.time = deadline;
		
		final int cap = rs.getCapicity();
		
		RBTreeSubSet<ParalelResourceNode<A>> subset = this.schedule.subSet(this.util_min, this.util_max);
		
		int start;
		boolean candidate;
		
		ParalelResourceNode first = subset.first();
		
		if(first == null)
		{
			if(cap <= this.maxCapicity)
			{
				start = startTime;
				candidate = true;
			}
			else
			{
				candidate = false;
			}
		}
		else if(first.capicity + cap <= this.maxCapicity)
		{ // this item can be planned alongside the other one;
			
		}
		else if(first.time >= startTime + rs.lenght)
		{ // It might be planned before the first one starts.
			if(first.capicity - first.capChange + cap <= this.maxCapicity)
			{
				start = startTime;
				candidate = true;
			}
			else
			{ // can't be planned before the first item.
				candidate = false;
			}
		}
		
		throw new Error("Unfinished!");
		
	}
}
