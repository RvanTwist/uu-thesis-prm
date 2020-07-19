package simulation.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import simulation.worker.tasks.*;
import ui.ganttchart.GanttChart;
import ui.ganttchart.GanttChartBar;

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
public class SimSegmentDataViewer extends JPanel implements MouseMotionListener
{
	//Panel 1
	
	JPanel p1;
	Label kind = new Label("Task");
	Label time = new Label("start");
	Label window = new Label("finish");
	Label groupM = new Label("expected start: ");
	
	Label prms = new Label("prms");
	JTextArea prms_area = new JTextArea();
	JScrollPane prms_scroll = new JScrollPane(prms_area);
	
	Task task;
	
	public SimSegmentDataViewer()
	{
		super();
		this.setLayout(new GridLayout(1,4));
		
		this.setPreferredSize(new Dimension(100,120));
		
		p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1,BoxLayout.Y_AXIS));
		//p1.setPreferredSize(new Dimension(200,150));
		p1.add(kind);
		p1.add(time);
		p1.add(window);
		p1.add(groupM);
		
		this.add(p1);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add(prms,BorderLayout.NORTH);
		p2.add(prms_scroll,BorderLayout.CENTER);
		prms_area.setEditable(false);
		
		this.add(p2);
		
	}
	
	public void setData(Task t)
	{
		if(t == null)
		{
			p1.setVisible(false);
			return;
		}
		else
		{
			p1.setVisible(true);
		}
		
		this.task = t;
		
		this.kind.setText(t.getClass().getSimpleName());
		this.time.setText("start: "+t.startTime);
		this.window.setText("finish: "+t.finishTime);
		this.groupM.setText("expected start: "+t.executetime);
		
		prms.setText("");
		prms_area.setText(task.toString());
		
		this.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) 
	{
		mouseMoved(e);
	}

	@Override
	public void mouseMoved(MouseEvent me) 
	{
		Object src = me.getSource();
		if(src instanceof GanttChart)
		{
			GanttChart gantt = (GanttChart) src;
			
			GanttChartBar bar = gantt.getBarAt(me.getX(), me.getY());
			
			if(bar != null)
			{
				Object represents = bar.getData();
				if(represents instanceof Task)
				{
					this.setData((Task)represents);
				}
			}
			else
			{
				this.setData(null);
			}
		}
		
	}
}
