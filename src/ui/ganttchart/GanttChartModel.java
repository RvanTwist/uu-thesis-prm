package ui.ganttchart;

import java.util.ArrayList;
import java.util.Vector;

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
public class GanttChartModel 
{
	protected ArrayList<GanttChartRow> rows;
	private Vector<GanttModelChangeListener> modelChangeListeners = new Vector<GanttModelChangeListener>();
	private int units = 0;	
	
	public GanttChartModel()
	{
		rows = new ArrayList<GanttChartRow>();
	}
	
	public void add(GanttChartRow row)
	{
		if(row.model != null)
		{
			row.model.remove(row);
		}
		
		rows.add(row);
		row.model = this;
		
		this.fireGanttModelChangeEvent(new GanttModelChangeEvent(row, GanttModelChangeEvent.RowInserted));
	}
	
	public boolean remove(GanttChartRow row)
	{
		final boolean succes =  rows.remove(row);
		
		if(succes)
		{
			row.model = null;
		}
		
		this.fireGanttModelChangeEvent(new GanttModelChangeEvent(row, GanttModelChangeEvent.RowDeleted));
		
		return succes;
	}
	
	public void addGanttModelChangeListener(GanttModelChangeListener gmcel)
	{
		modelChangeListeners.add(gmcel);
	}
	
	public boolean removeGanttModelChangeListener(GanttModelChangeListener gmcel)
	{
		return modelChangeListeners.remove(gmcel);
	}
	
	protected void fireGanttModelChangeEvent(GanttModelChangeEvent ev)
	{
		units = -1;
		
		for(GanttModelChangeListener l : this.modelChangeListeners)
		{
			l.GanttmodelEvent(ev);
		}
	}

	public int getUnits() 
	{
		if(units == -1)
		{
			for(GanttChartRow row : this.rows)
			{
				for(GanttChartBar bar : row.bars)
				{
					units = Math.max(units, bar.start + bar.lenght);
				}
			}
		}
		
		return units;
	}
	
	public void sortRows()
	{
		GanttChartRow[] arr = new GanttChartRow[this.rows.size()];
		arr = this.rows.toArray(arr);
		
		boolean changed = true;
		for(int i = 0; i < arr.length && changed ; i++)
		{
			//changed = false;
			for(int j = 1 ; j < arr.length - i; j++)
			{
				GanttChartRow n1 = arr[j-1];
				GanttChartRow n2 =	arr[j];
				
				if(n1.toString().compareToIgnoreCase(n2.toString()) > 0)
				{
					arr[j] = n1;
					arr[j-1] = n2;
					changed = true;
				}
			}
		}
		
		this.rows.clear();
		for(int i = 0 ; i < arr.length ; i++)
		{
			this.rows.add(arr[i]);
		}
		
		this.fireGanttModelChangeEvent(new GanttModelChangeEvent(this, GanttModelChangeEvent.ModelChanged));
	}
	
	public void clearSelectedBars()
	{
		for(GanttChartRow r : this.rows)
		{
			for(GanttChartBar b : r.bars)
			{
				b.selected = false;
			}
		}
		
		this.fireGanttModelChangeEvent(new GanttModelChangeEvent(this, GanttModelChangeEvent.ModelChanged));
	}

	public void clear() 
	{
		for(GanttChartRow r : this.rows)
		{
			r.model = null;
		}
		this.rows.clear();
	}
}
