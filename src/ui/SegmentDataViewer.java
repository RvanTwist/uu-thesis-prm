package ui;

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

import prm.ls.model4.M4_MergedSegment;
import prm.ls.model4.M4_PRM_Segment;
import prm.ls.model4.M4_Segment;
import prm.ls.model4.M4_WorkerSegment;
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
public class SegmentDataViewer extends JPanel implements MouseMotionListener
{
	//Panel 1
	
	JPanel p1;
	Label kind = new Label("Segment");
	Label time = new Label("time");
	Label window = new Label("window");
	Label groupM = new Label("Group merged: ");
	Label segmentTime = new Label("segment time");
	Label from = new Label("From");
	Label to = new Label("To");
	
	Label prms = new Label("prms");
	JTextArea prms_area = new JTextArea();
	JScrollPane prms_scroll = new JScrollPane(prms_area);
	
	M4_Segment segment;
	
	public SegmentDataViewer()
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
		p1.add(segmentTime);
		p1.add(from);
		p1.add(to);
		
		this.add(p1);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add(prms,BorderLayout.NORTH);
		p2.add(prms_scroll,BorderLayout.CENTER);
		prms_area.setEditable(false);
		
		this.add(p2);
		
	}
	
	public void setData(M4_Segment seg)
	{
		if(seg == null)
		{
			p1.setVisible(false);
			return;
		}
		else
		{
			p1.setVisible(true);
		}
		
		boolean groupMerged = false;
		
		this.segment = seg;
		
		this.kind.setText(seg.getClass().getSimpleName());
		this.time.setText("start: "+seg.getStartTime());
		this.window.setText("window: "+seg.printTimeWindow());
		this.segmentTime.setText("time: "+seg.segment.getSegmentTime());
		
		this.from.setText("from: "+seg.segment.from);
		this.to.setText("to  : "+seg.segment.to);
		
		if(seg instanceof M4_WorkerSegment)
		{
			prms_area.setText("Worker: "+((M4_WorkerSegment)seg).getWorker());
		}
		else if(seg instanceof M4_PRM_Segment)
		{
			M4_PRM_Segment segprm = (M4_PRM_Segment) seg;
			groupMerged = segprm.getOriSegmentGroup().isMerged();
			prms_area.setText("PRM "+segprm.segment.prm.prm_ID+" (segid: "+segprm.segment.original_segmentId+")");
		}
		else if(seg instanceof M4_MergedSegment)
		{
			M4_MergedSegment segmerged = (M4_MergedSegment)seg;
			groupMerged = true;
			String prms = "PRMS";
			for(M4_PRM_Segment segprm : segmerged)
			{
				prms += "\nPRM "+segprm.segment.prm.prm_ID+" (segid: "+segprm.segment.original_segmentId+")";
			}
			
			prms_area.setText(prms);
		}
		else
		{
			prms_area.setText("<Unknown>");
		}
		
		this.groupM.setText("GroupMerged: "+groupMerged);
		
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
				if(represents instanceof M4_Segment)
				{
					this.setData((M4_Segment)represents);
				}
			}
			else
			{
				this.setData(null);
			}
		}
		
	}
}
