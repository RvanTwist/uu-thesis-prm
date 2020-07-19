package simulation.ui;

import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import simulation.*;
import simulation.objects.SimulatedLocation;
import simulation.objects.SimulatedPRM;
import simulation.objects.SimulatedWorker;

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
public class SimulatedObjectsPanel extends JPanel implements ListSelectionListener, SimulationUIListener
{
	SimulationUI simUI;
	HistoryList<Object> history = new HistoryList<Object>(100);
	
	// Col1
	JTabbedPane c1_tabbedPlane;
	JList		c1_prms;
	JList		c1_workers;
	JList		c1_locations;
	
	//Col 2 
	ReferencePanel c2_dynamicContent;
	
	//Col3
	JTextArea c3_info;
	
	public SimulatedObjectsPanel(SimulationUI simulationUI) 
	{
		this.simUI = simulationUI;
		
		this.makeUI();
		this.reloadInfo();
		
		this.simUI.addSimulationUIListener(this);
	}
	
	private void makeUI()
	{
		this.setLayout(new GridLayout(1,3));
		
		// Tab1
		c1_tabbedPlane = new JTabbedPane();
		
		c1_prms = new JList();
		this.c1_tabbedPlane.addTab("prms",new JScrollPane(c1_prms));
		c1_prms.setModel(new DefaultListModel());
		c1_prms.addListSelectionListener(this);
		
		c1_workers = new JList();
		this.c1_tabbedPlane.addTab("workers",new JScrollPane(c1_workers));
		c1_workers.setModel(new DefaultListModel());
		c1_workers.addListSelectionListener(this);
		
		c1_locations = new JList();
		this.c1_tabbedPlane.addTab("locations",new JScrollPane(c1_locations));
		c1_locations.setModel(new DefaultListModel());
		c1_locations.addListSelectionListener(this);
		
		this.add(c1_tabbedPlane);
		
		// Tab2
		this.c2_dynamicContent = new ReferencePanel(this);
		this.add(this.c2_dynamicContent);
		
		// Tab3
		this.c3_info = new JTextArea();
		this.add(new JScrollPane(this.c3_info));
	}
	
	public void reloadInfo()
	{
		DefaultListModel prm_model = (DefaultListModel)this.c1_prms.getModel();
		prm_model.clear();
		for(SimulatedPRM prm : this.simUI.dws.prms.values())
		{
			prm_model.addElement(prm);
		}
		
		DefaultListModel worker_model = (DefaultListModel)this.c1_workers.getModel();
		worker_model.clear();
		for(SimulatedWorker w : this.simUI.dws.workers.values())
		{
			worker_model.addElement(w);
		}
		
		DefaultListModel location_model = (DefaultListModel)this.c1_locations.getModel();
		location_model.clear();
		for(SimulatedLocation l : this.simUI.dws.locations.values())
		{
			location_model.addElement(l);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse) 
	{
		Object src = lse.getSource();
		
		final Object selected;
		
		if( src instanceof JList)
		{
			selected = ((JList)src).getSelectedValue();
		}
		else 
		{
			selected = null;
		}
		
		if(selected != null)
		{
			this.setInfo(selected, true);
		}
	}

	public void setInfo(Object o, boolean b) 
	{
		if(o == null || o instanceof String)
		{
			return;
		}
		
		if(b)
		{
			this.history.add(o);
		}
		
		if(o instanceof InfoPrintable)
		{
			this.c3_info.setText(((InfoPrintable)o).getInfo());
		}
		this.c2_dynamicContent.setInfo(o);
	}
	
	public void showPrevious()
	{
		if(history.hasPrevious())
		{
			setInfo(history.previous(),false);
		}
	}
	
	public void showNext()
	{
		if(history.hasNext())
		{
			setInfo(history.next(),false);
		}
	}

	@Override
	public void simulationChanged() 
	{
		this.reloadInfo();
	}
}
