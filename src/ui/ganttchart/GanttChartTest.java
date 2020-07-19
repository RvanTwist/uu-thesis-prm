package ui.ganttchart;

import java.awt.Color;
import java.awt.Frame;

import javax.swing.JScrollPane;

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
@SuppressWarnings("serial")
public class GanttChartTest extends Frame
{
	GanttChart gantt;
	GanttChartModel m;
	
	public static void main(String[] args)
	{
		new GanttChartTest().setVisible(true);
	}
	
	public GanttChartTest()
	{
		this.setSize(150, 150);
		
		m = new GanttChartModel();
		
		//rows
		GanttChartRow row1 = new GanttChartRow("Machine 1");
		GanttChartRow row2 = new GanttChartRow("Machine 2");
		m.add(row1);
		m.add(row2);
		
		
		// Job1 
		row1.add(new GanttChartBar(0,1, Color.red));
		row2.add(new GanttChartBar(1,1, Color.red));
		
		// Job2
		row2.add( new GanttChartBar(3,2, Color.blue));
		row1.add( new GanttChartBar(5,2, Color.blue));
		
		gantt = new GanttChart(m);
		
		gantt.setScale(50);
		this.add(new JScrollPane(gantt, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		
	}
}
