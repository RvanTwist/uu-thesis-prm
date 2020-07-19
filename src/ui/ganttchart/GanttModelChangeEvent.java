package ui.ganttchart;

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
public class GanttModelChangeEvent 
{
	public static final int RowDeleted 	= 0;
	public static final int RowInserted = 1;
	public static final int RowChanged 	= 2;
	
	public static final int BarDeleted 	= 10;
	public static final int BarInserted = 11;
	public static final int BarChanged 	= 12;
	
	public static final int ModelChanged = 20;
	
	public final GanttChartModel model;
	public final GanttChartRow row;
	public final GanttChartBar bar;
	public final int cause;
	
	public GanttModelChangeEvent(GanttChartBar bar, int cause)
	{
		this.cause = cause;
		this.bar = bar;
		this.row = bar.row;
		this.model = this.row.model;
	}
	
	GanttModelChangeEvent(GanttChartBar bar)
	{
		this(bar, BarChanged);
	}
	
	public GanttModelChangeEvent(GanttChartRow row, int cause)
	{
		this.cause = cause;
		this.bar = null;
		this.row = row;
		this.model = row.model;
	}
	
	GanttModelChangeEvent(GanttChartRow row)
	{
		this(row, RowChanged);
	}
	
	public GanttModelChangeEvent(GanttChartModel model, int cause)
	{
		this.cause = cause;
		this.bar = null;
		this.row = null;
		this.model = model;
	}
	
	GanttModelChangeEvent(GanttChartModel model)
	{
		this(model, ModelChanged);
	}

	public GanttModelChangeEvent(GanttChartBar bar,
			GanttChartRow row, GanttChartModel model, int cause) 
	{
		this.bar = bar;
		this.row = row;
		this.model = model;
		this.cause = cause;
	}

	public GanttChartModel getModel() {
		return model;
	}

	public GanttChartRow getRow() {
		return row;
	}

	public GanttChartBar getBar() {
		return bar;
	}

	public int getCause() {
		return cause;
	}
}
