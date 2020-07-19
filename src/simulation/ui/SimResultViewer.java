package simulation.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import prm.ls.model4.*;
import prm.problemdef.*;
import simulation.DynamicWorldSimulation;
import simulation.objects.SimulatedWorker;
import simulation.worker.tasks.*;
import ui.ganttchart.GanttChart;
import ui.ganttchart.GanttChartBar;
import ui.ganttchart.GanttChartModel;
import ui.ganttchart.GanttChartRow;

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
public class SimResultViewer extends JPanel implements ActionListener, ItemListener
{	
	
	JPanel sidePanel;
	
	DynamicWorldSimulation sim;
	
	JComboBox areas = new JComboBox();
	
	GanttChartModel gantt = new GanttChartModel();
	GanttChart gantt_panel = new GanttChart(gantt);
	JScrollPane gantt_scroll = new JScrollPane(gantt_panel);
	
	SimSegmentDataViewer segmentData = new SimSegmentDataViewer();
	
	public SimResultViewer(DynamicWorldSimulation sim)
	{
		super();
		
		this.setLayout(new BorderLayout());
		this.setSize(1200,800);
		
		this.initSidePanel();
		this.add(gantt_scroll,BorderLayout.CENTER);
		this.add(segmentData,BorderLayout.SOUTH);
		gantt_panel.addMouseMotionListener(this.segmentData);
		
		this.sim = sim;
	}

	private void initSidePanel() 
	{
		sidePanel = new JPanel();
		this.add(sidePanel,BorderLayout.WEST);
		sidePanel.setLayout(new BoxLayout(sidePanel,BoxLayout.Y_AXIS));
		
		sidePanel.add(new Label("Areas"));
		sidePanel.add(this.areas);
		areas.addItemListener(this);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		Object src = e.getSource();
	}
	
	void loadFromSimulation() 
	{
		M4_Planning planning = sim.world.getCurrentPlanning();
		
		this.areas.removeAllItems();
		for(M4_Area a : planning.areas.values())
		{
			this.areas.addItem(a);
		}
		
		this.gantt.clear();
		
		Object sel = this.areas.getSelectedItem();
		if(sel != null && sel instanceof M4_Area)
		{
			constructGantt((M4_Area)sel);
		}
		else
		{
			this.areas.setSelectedIndex(0);
		}
		
	}

	@Override
	public void itemStateChanged(ItemEvent e) 
	{
		Object src = e.getSource();
		if(src == areas)
		{
			itemEventAreas();
		}
		
	}

	private void itemEventAreas() 
	{
		gantt.clear();
		Object selected = areas.getSelectedItem();
		if(selected instanceof M4_Area)
		{
			this.constructGantt((M4_Area)selected);
		}
		else
		{
			System.out.println("Selected: "+selected);
		}
	}

	private void constructGantt(M4_Area a) 
	{
		System.out.println("Construct Gantt Chart for Area: "+a);
		gantt.clear();
		
		if(a.workers.size() == 0)
		{
			gantt.add(new GanttChartRow("Nobody"));
		}
		
		for(M4_Worker t : a.workers)
		{
			SimulatedWorker sim_worker = sim.getSimWorker(t.getTransport());
			
			GanttChartRow row = new GanttChartRow(t.toString());
			
			for(int i = 0 ; i < sim_worker.completedAndStartedTasks.size() ; i++)
			{ // create GanttChart Bars
				Task task = sim_worker.completedAndStartedTasks.get(i);
				Color c;
				
				if(task instanceof TravelTask)
					c = Color.green;
				else if(task instanceof PickupAndDeliverTask)
					c = Color.yellow;
				else if(task instanceof BoardingTask)
					c = Color.blue;
				else
					c = Color.lightGray;
				
				int start = (int)task.startTime;
				int end;
				if(task.finishTime == -1)
				{
					c = Color.red;
					if(i + 1 < sim_worker.completedAndStartedTasks.size())
					{
						Task nextTask = sim_worker.completedAndStartedTasks.get(i+1);
						end = Math.max((int)(nextTask.startTime-1), start + 1);
					}
					else
					{
						end = (int)Math.ceil(sim.getTime());
					}
				}
				else
				{
					end = (int)Math.max(start + 1, task.finishTime);
				}
				
				GanttChartBar bar = new GanttChartBar(task,start,end-start,c);
				row.add(bar);
			}
			
			gantt.add(row);
		}
		
		this.repaint();
	}
	
	public Frame getFrame()
	{
		Container c = this.getParent();
		
		while(c != null || c instanceof Frame)
		{
			c = c.getParent();
		}
		
		return (Frame)c;
	}
}
