package prm.ls.model4;

import prm.ls.AbstractBackTrackable;
import prm.ls.BackTracker;
import prm.ls.SolutionFacture;
import prm.ls.resources.ParalelResource;
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
public abstract class M4_SegmentGroup<A extends M4_Segment> extends AbstractBackTrackable  implements Comparable<M4_SegmentGroup<?>>, Iterable<A>
{	
	static private int ID_DISPENSER = 0;
	
	protected boolean fixed;
	protected int fixedTime;
	
	public final int id;
	
	// Dynamic variables:
	private int 	start;
	private boolean planned;
	
	// Backtrack variables:
	private int 	bt_start;
	private boolean bt_planned;
	
	protected boolean valid = true;
	
	public final M4_Planning planning;
	
	public M4_SegmentGroup(M4_Planning p)
	{
		super(p.backtracker);
		
		this.planning = p;
		id = ID_DISPENSER;
		ID_DISPENSER++;
	}
	
	public boolean isFixed()
	{
		return this.fixed;
	}
	
	public abstract int getErliestStart();
	
	public abstract int getLatestStart();

	public boolean isPlanned()
	{
		return this.planned;
	}
	
	public void setFixedTime(int time)
	{
		this.fixed = true;
		this.fixedTime = time;
	}
	
	public void reload()
	{
		this.fixed = false;
		this.fixedTime = 0;
	}
	
	public int getFixedTime()
	{
		return this.fixedTime;
	}
	
	public void dropFixedTime()
	{
		this.fixed = false;
		this.fixedTime = -1;
	}
	
	public int compareTo(M4_SegmentGroup other)
	{
		return this.id - other.id;
	}
	
	public int getStart() 
	{
		return this.start;
	}

	public void testConsistancy()
	{
		final boolean planned = this.isPlanned();
		
		int i = 0;
		
		for(M4_Segment seg : this)
		{
			i++;
			
			if(seg.isPlanned() != planned)
			{
				String s = " route: ";
				
				for(M4_Segment seg2 : this)
				{
					Segment pd_seg = seg2.segment;
					s = s +"["+pd_seg.supervisingArea.id+":"+pd_seg.from.getLocationId()+"=>"+pd_seg.to.getLocationId()+"("+seg2.isPlanned()+")]";
				}
				
				throw new Error(	"Planning inconsistancy: \n"+
									" Group: "+this+"\n"+
									" Segment "+i+"planned: "+seg.isPlanned()+" Group planned: "+planned+"\n"+
									" "+this.getClass().getSimpleName()+" : "+seg.getClass().getSimpleName()+"\n"+
									s);
			}
			
			if(seg.isPlanned() && seg.getWorker() == null)
			{
				throw new Error(" Segment "+i+" is planned, but haven't be assigned a worker!");
			}
		}
		
		System.out.println(" SegmentGroup "+id+" passed the test!");
	}
	
	public void setStart(int s) throws CantPlanException
	{
		if(this.planned && s != start)
		{
			throw new CantPlanException("Can't shift the starttime of a segment while been planned! "+s+" / "+start);
		}
		
		this.start = s;
		this.registerChange();
		this.checkValid();
	}
	
	public void setPlanned(boolean p)
	{	
		this.planned = p; 
		this.registerChange();
		this.checkValid();
	}
	
	protected void doBacktrack()
	{
		start	= bt_start;
		planned	= bt_planned;
		
		this.checkValid();
	}
	
	protected void doMakeCheckpoint()
	{
		bt_start	= start;
		bt_planned	= planned;
	}

	public abstract void unPlan() throws CantPlanException;
	
	public abstract void mergeWith(M4_PRM_SegmentGroup segmentGroup) throws CantPlanException;

	public void setValid(boolean b) 
	{
		this.valid = b;
	}
	
	public boolean isValid()
	{
		return this.valid;
	}

	public void checkValid() 
	{
		if(!this.isPlanned())
		{
			this.setValid(true);
			return;
		}
		
		final int start = this.getStart();
		final int start_min = this.getErliestStart();
		final int start_max = this.getLatestStart();
		
		boolean valid = start_min <= start && start <= start_max;
		
		if(this.valid != valid)
		{
			this.setValid(valid);
		}
	}

	public abstract void unfix();
}
