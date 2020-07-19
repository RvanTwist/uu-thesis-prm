package ui.ganttchart;

import java.awt.Color;

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
public class GanttChartBar implements Comparable<GanttChartBar>
{
	protected int start;
	protected int lenght;
	protected GanttChartRow row;
	protected Color color;
	protected final Object userObject;
	protected boolean selected = false;
	
	public static final Color DefaultColor = new Color(255,0,0);
	
	public GanttChartBar()
	{
		this(null,0,0,DefaultColor);
	}
	
	public GanttChartBar(Object o)
	{
		this(o,0,0,DefaultColor);
	}
	
	public GanttChartBar(int start, int lenght)
	{
		this(null,start,lenght, DefaultColor);
	}
	
	public GanttChartBar(Object o, int start, int lenght)
	{
		this(o,start,lenght, DefaultColor);
	}

	public GanttChartBar(int start, int lenght, Color c)
	{
		this(null,start,lenght, c);
	}
	
	public GanttChartBar(Object o, int start, int lenght, Color c) 
	{
		this.userObject = o;
		this.start = start;
		this.lenght = lenght;
		this.color = c;
	}

	public Color getColor()
	{
		return this.color;
	}
	
	public void setColor(Color c)
	{
		this.color = c;
		
		GanttChartModel model = this.getModel();
		if(model != null)
		{
			model.fireGanttModelChangeEvent(new GanttModelChangeEvent(this, GanttModelChangeEvent.BarChanged));
		}
		
	}
	 
	
	public int getStart() 
	{
		return start;
	}

	public void setBar(int start, int lenght)
	{
		this.start = start;
		this.lenght = lenght;
		
		GanttChartModel model = this.getModel();
		if(model != null)
		{
			model.fireGanttModelChangeEvent(new GanttModelChangeEvent(this, GanttModelChangeEvent.BarChanged));
		}
	}
	
	public void setBar(int start, int lenght, Color color)
	{
		this.start = start;
		this.lenght = lenght;
		this.color = color;
		
		GanttChartModel model = this.getModel();
		if(model != null)
		{
			model.fireGanttModelChangeEvent(new GanttModelChangeEvent(this, GanttModelChangeEvent.BarChanged));
		}
	}
	 
	
	public void setStart(int start) 
	{
		this.start = start;
		
		GanttChartModel model = this.getModel();
		if(model != null)
		{
			model.fireGanttModelChangeEvent(new GanttModelChangeEvent(this, GanttModelChangeEvent.BarChanged));
		}
	}

	
	
	public int getLenght() 
	{
		return lenght;
	}

	public void setLenght(int lenght) 
	{
		this.lenght = lenght;
		
		GanttChartModel model = this.getModel();
		if(model != null)
		{
			model.fireGanttModelChangeEvent(new GanttModelChangeEvent(this, GanttModelChangeEvent.BarChanged));
		}
	}

	public GanttChartRow getRow() 
	{
		return row;
	}

	@Override
	public int compareTo(GanttChartBar other) 
	{
		if(other.start == this.start)
		{
			return this.lenght - other.lenght;
		}
		else
		{
			return this.start - other.start;
		}
	}
	
	public void updatePosition()
	{
		if(this.row == null)
			return;
		
		GanttChartRow r = this.row;
		r.remove(this);
		r.add(this);
	}
	
	public GanttChartModel getModel()
	{
		if(this.row == null)
		{
			return null;
		}
		
		return this.row.getModel();
	}
	
	public String toString()
	{
		if(userObject == null)
		{
			return "";
		}
		
		return userObject.toString();
	}
	
	public void setSelected(boolean b)
	{
		this.selected = b;
		
		GanttChartModel model = this.getModel();
		if(model != null)
		{
			model.fireGanttModelChangeEvent(new GanttModelChangeEvent(this, GanttModelChangeEvent.BarChanged));
		}
	}
	
	public Object getData()
	{
		return this.userObject;
	}
}
