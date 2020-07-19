package ui.ganttchart;

import java.util.TreeSet;

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
public class GanttChartRow 
{
	protected String name;
	protected TreeSet<GanttChartBar> bars;
	protected GanttChartModel model;
	
	public GanttChartRow(String name)
	{
		this.name = name;
		bars = new TreeSet<GanttChartBar>();
	}
	
	public void add(GanttChartBar bar)
	{
		if(bar.row != null)
		{
			bar.row.remove(bar);
		}
		
		bars.add(bar);
		bar.row = this;
		
		if(this.model != null)
		{
			this.model.fireGanttModelChangeEvent(new GanttModelChangeEvent(bar, GanttModelChangeEvent.BarInserted));
		}
	}
	
	public boolean remove(GanttChartBar bar)
	{
		if(bar.row != this)
		{
			return false;
		}
		
		//final GanttChartRow row = bar.row;
		
		bar.row = null;
		final boolean succes =  bars.remove(bar);
		
		if(this.model != null)
		{
			this.model.fireGanttModelChangeEvent(new GanttModelChangeEvent(bar, this, this.model, GanttModelChangeEvent.BarDeleted));
		}
		
		return succes;
	}

	public GanttChartModel getModel() 
	{
		return this.model;
	}
	
	public void clear()
	{
		for(GanttChartBar bar : this.bars)
		{
			bar.row = null;
		}
		
		this.bars.clear();
		
		if(this.model != null)
		{
			this.model.fireGanttModelChangeEvent(new GanttModelChangeEvent(this, GanttModelChangeEvent.RowChanged));
		}
	}
	
	public boolean hasOverLap()
	{
		int max = Integer.MIN_VALUE;
		
		// This method makes use of the fact that bars are ordered.
		for(GanttChartBar bar : this.bars)
		{
			if(bar.start < max)
			{
				return true;
			}
			else
			{
				max = bar.start + bar.lenght;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString()
	{
		return name;
	}

	public void removeSelf() 
	{
		if(this.model != null)
		{
			this.model.remove(this);
		}
	}

	public void setName(String n) 
	{
		this.name = n;
		
		if(this.model != null)
		{
			this.model.fireGanttModelChangeEvent(new GanttModelChangeEvent(this, GanttModelChangeEvent.RowChanged));
		}
	}
}
