package ui;

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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import prm.ls.model4.*;
import prm.problemdef.*;
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
public class ResultViewer extends JPanel implements ActionListener, ItemListener
{	
	public static void main(String[] args)
	{ 
		Frame f = new JFrame();
		f.add(new ResultViewer());
		f.setVisible(true);
	}
	
	JPanel sidePanel;
	
	JButton loadSolution = new JButton("load sollution");
	
	M4_Planning planning;
	PRM_Instance instance;
	Sollution solution;
	
	JComboBox areas = new JComboBox();
	
	GanttChartModel gantt = new GanttChartModel();
	GanttChart gantt_panel = new GanttChart(gantt);
	JScrollPane gantt_scroll = new JScrollPane(gantt_panel);
	
	SegmentDataViewer segmentData = new SegmentDataViewer();
	
	public ResultViewer()
	{
		super();
		
		this.setLayout(new BorderLayout());
		this.setSize(1200,800);
		
		this.initSidePanel();
		this.add(gantt_scroll,BorderLayout.CENTER);
		this.add(segmentData,BorderLayout.SOUTH);
		gantt_panel.addMouseMotionListener(this.segmentData);
	}

	private void initSidePanel() 
	{
		sidePanel = new JPanel();
		this.add(sidePanel,BorderLayout.WEST);
		sidePanel.setLayout(new BoxLayout(sidePanel,BoxLayout.Y_AXIS));
		
		sidePanel.add(loadSolution);
		loadSolution.addActionListener(this);
		
		sidePanel.add(new Label("Areas"));
		sidePanel.add(this.areas);
		areas.addItemListener(this);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		Object src = e.getSource();
		
		if(src == loadSolution)
			actionLoadSolution();
	}

	private void actionLoadSolution() 
	{
		FileDialog d = new FileDialog(this.getFrame(),"Open sollution File",FileDialog.LOAD);
		d.setDirectory(".");
		d.setVisible(true);
		String fileName = d.getFile();
		
		if(fileName != null)
		{
			fileName = d.getDirectory()+fileName;
			try 
			{
				this.setSollution(Sollution.load(new File(fileName)));
				
				// Post load alterations:
//				instance.TightenEmployeeWindows();
				
				
				
			} catch (Exception e) 
			{
				e.printStackTrace();
				System.out.println("Seems like an incorrect File! ("+fileName+")");
			}
		}
		
		
		
	}

	public void setSollution( Sollution s)
	{
		this.solution = s;
		this.instance = s.instance;
		
		this.planning = new M4_Planning(instance);
		try
		{
			this.planning.loadSolution(solution);
		}
		catch(CantPlanException e){}
//		this.planning.testConsistancy();
		
		this.initialiseNewPlanning();
	}
	
	private void initialiseNewPlanning() 
	{
		this.areas.removeAllItems();
		for(M4_Area a : this.planning.areas.values())
		{
			this.areas.addItem(a);
		}
		
		this.gantt.clear();
		
		this.areas.setSelectedIndex(0);
		
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
			GanttChartRow row = new GanttChartRow(t.toString());
			
			M4_Segment seg = t.start;
			
			while(seg != null)
			{ // create GanttChart Bars
				Color c = GanttChartBar.DefaultColor;
				if(seg instanceof M4_WorkerSegment)
					c = Color.yellow;
				if(seg instanceof M4_PRM_Segment)
					c = Color.blue;
				if(seg instanceof M4_MergedSegment)
					c = Color.magenta;
				
				GanttChartBar bar = new GanttChartBar(seg,seg.getStartTime(),Math.max(2,seg.segment.getSegmentTime()),c);
				row.add(bar);
				
				seg = seg.getNext();
			}
			
			
			gantt.add(row);
		}
		
		GanttChartRow declined = new GanttChartRow("Declined");
		int declinedCount = 0;
		for(M4_PRM prm : planning.prms)
		{
			for(M4_PRM_Segment seg : prm.segments)
			{
				if(seg.area == a && !seg.isPlanned())
				{
					try
					{
						seg.setStart(seg.getEarliestStart());
					}
					catch(CantPlanException e){}
					GanttChartBar bar = new GanttChartBar(seg,seg.getStartTime(),Math.max(2,seg.segment.getSegmentTime()),Color.red);
					declined.add(bar);
					declinedCount++;
				}
			}
		}
		if(declinedCount > 0)
		{
			gantt.add(declined);
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
