package simulation.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import simulation.*;
import simulation.objects.SimulatedLocation;
import simulation.objects.SimulatedPRM;
import simulation.objects.SimulatedWorker;
import simulation.worker.tasks.PickupAndDeliverTask;

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
public class ReferencePanel extends JPanel implements ActionListener
{
	SimulatedObjectsPanel ui;
	
	// MainPanel
	JList releventItems = new JList(new DefaultListModel());
	
	
	// HistoryPanel
	JPanel history = new JPanel();
	JButton history_prev = new JButton("Previous");
	JButton history_next = new JButton("Next");
	
	
	public ReferencePanel(SimulatedObjectsPanel sui)
	{
		this.ui = sui;
		this.setLayout(new BorderLayout());
	
		this.add(new JScrollPane(this.releventItems), BorderLayout.CENTER);
		
		// History
		history.setLayout(new GridLayout(1,2));
		history.add(history_prev);
		history.add(history_next);
		history_prev.addActionListener(this);
		history_next.addActionListener(this);
		
		this.add(history,BorderLayout.SOUTH);
		
		this.releventItems.addListSelectionListener(this.ui);
	}

	public void setInfo(Object o)
	{
		releventItems.clearSelection();
		DefaultListModel model = (DefaultListModel) this.releventItems.getModel();
		model.clear();
		
		if(o instanceof SimulatedPRM)
		{
			SimulatedPRM prm = (SimulatedPRM) o;
			model.addElement("Location:");
			model.addElement(prm.location);
			model.addElement(" ");
			model.addElement("Transport:");
			model.addElement(prm.getWorker());
		}
		if(o instanceof SimulatedWorker)
		{
			SimulatedWorker w = (SimulatedWorker) o;
			model.addElement("Location:");
			model.addElement(w.location);
			model.addElement(" ");
			model.addElement("PRMs:");
			for(SimulatedPRM prm : w.helping)
			{
				model.addElement(prm);
			}
			model.addElement(" ");
			model.addElement("Relevent for task:");
			if(w.currentTask instanceof PickupAndDeliverTask)
			{
				
				PickupAndDeliverTask t = (PickupAndDeliverTask) w.currentTask;
				model.addElement(" ");
				model.addElement("Deliver:");
				for(SimulatedPRM prm : t.deliver)
				{
					model.addElement(prm);
				}
				model.addElement(" ");
				model.addElement("Finish:");
				for(SimulatedPRM prm : t.finish)
				{
					model.addElement(prm);
				}
				model.addElement(" ");
				model.addElement("Pickup:");
				for(SimulatedPRM prm : t.pickup)
				{
					model.addElement(prm);
				}
			}
		}
		if(o instanceof SimulatedLocation)
		{
			SimulatedLocation l = (SimulatedLocation) o;
			model.addElement("Workers:");
			for(SimulatedWorker w : l.workers)
			{
				model.addElement(w);
			}
			model.addElement(" ");
			model.addElement("PRMs:");
			for(SimulatedPRM prm : l.prms)
			{
				model.addElement(prm);
			}
		}
	}
	

	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		Object src = ae.getSource();
		
		if(src == this.history_next)
		{
			this.ui.showNext();
		}
		else if(src == this.history_prev)
		{
			this.ui.showPrevious();
		}
		
	}
	

}
