package simulation.ui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import simulation.Event;
import simulation.EventQueue;

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
public class EventList extends JPanel implements ListSelectionListener, ActionListener
{
	JList eventlist = new JList(new DefaultListModel());
	JTextArea info = new JTextArea();
	
	SimulationUI simulation;
	
	JButton skipTillEvent = new JButton("SkipTillEvent");
	
	public EventList(SimulationUI sim)
	{
		this.simulation = sim;
		
		this.setLayout(new BorderLayout());
		
		JPanel p1 = new JPanel();
		
		p1.setLayout(new GridLayout(1,2));
		
		p1.add(new JScrollPane(eventlist));
		this.eventlist.addListSelectionListener(this);
		p1.add(info);
		
		this.add(p1,BorderLayout.CENTER);
		
		JPanel p2 = new JPanel();
		p2.add(this.skipTillEvent);
		this.skipTillEvent.addActionListener(this);
		
		this.add(p2,BorderLayout.SOUTH);
		
	}
	
	@Override
	public void paint(Graphics g)
	{
		DefaultListModel m = (DefaultListModel)this.eventlist.getModel();
		m.clear();
		
		EventQueue queue = this.simulation.dws.eventqueue;
		
		m.addElement("Finished events");
		for(Event e : queue.pastEvents)
		{
			m.addElement(e);
		}
		
		m.addElement(" ");
		m.addElement("CurrentEvent");
		if(queue.lastEvent != null)
		{
			m.addElement(queue.lastEvent);
		}
		
		m.addElement(" ");
		m.addElement("Pending events");
		for(Event e : queue.queue)
		{
			m.addElement(e);
		}
		
		super.paint(g);
	}

	@Override
	public void valueChanged(ListSelectionEvent lse) 
	{
		Object src = lse.getSource();
		
		if(src == this.eventlist)
		{
			Object o = this.eventlist.getSelectedValue();
			
			if(o instanceof InfoPrintable)
			{
				this.info.setText(((InfoPrintable)o).getInfo());
			}
			else if(o != null)
			{
				this.info.setText(o.toString());
			}
			else
			{
				this.info.setText("null");
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		Object src = ae.getSource();
		
		if(src == this.skipTillEvent)
		{
			Object o = this.eventlist.getSelectedValue();
			
			if(o instanceof Event)
			{
				simulation.dws.eventqueue.setSkipToEvent((Event)o);
			}
		}
		
	}
}
