package simulation.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import rpvt.util.DefaultWindowListener;
import simulation.*;
import simulation.events.*;
import simulation.model.*;
import simulation.worker.tasks.*;

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
public class SimulationUI extends JFrame implements ActionListener, SimulationEventListener
{
	DynamicWorldSimulation dws;
	
	JTabbedPane tabbedPanel; 
	
	SimulatedObjectsPanel simObj;
	EventList			  simEvnt;
	GanttPanel			  ganttPanel;
	WorkerGanttPanel	  workerGanttPanel;
	RunPanel			  runPanel;
	
	JPanel 		controlPanel = new JPanel();
	JButton 	startButton = new JButton("Start Simulation");
	JTextField  simulationSpeed = new JTextField("-1    ");
	JLabel      lastEvent    = new JLabel("Event: Simulation start.");
	
	JCheckBox	stepmode	= new JCheckBox("Stepmode");

	public long genSeed;
	public long simSeed;
	public int 	instanceID;
	public long mainSeed;
	
	LinkedList<SimulationUIListener> simulationUIlisteners = new LinkedList<SimulationUIListener>();
	
	public SimulationUI(DynamicWorldSimulation w)
	{
		this(w,new DefaultWindowListener());
	}
	
	public SimulationUI(DynamicWorldSimulation w, WindowListener wl)
	{
		super();
		this.dws = w;
		
		if(wl != null)
			this.addWindowListener(wl);
		
		this.dws.eventqueue.addSimulationEventListener(this);
		
		this.setLayout(new BorderLayout());
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		// Set UI
		this.tabbedPanel = new JTabbedPane();
		
		simObj = new SimulatedObjectsPanel(this);
		this.tabbedPanel.addTab("Objects", simObj);
		
		simEvnt = new EventList(this);
		this.tabbedPanel.addTab("Events", simEvnt);
		
		ganttPanel = new GanttPanel(this);
		this.tabbedPanel.addTab("gantt", ganttPanel);
		
		workerGanttPanel = new WorkerGanttPanel(this);
		this.tabbedPanel.addTab("Actual execution", workerGanttPanel);
		
		runPanel = new RunPanel(this);
		this.tabbedPanel.addTab("Run Panel", runPanel);
		
		// Set controll Panel
		{
			this.controlPanel.setLayout(new GridLayout(1,2));
			
			JPanel p1 = new JPanel(new GridLayout(1,2));
			
			JPanel p3 = new JPanel(new GridLayout(2,1));
			p3.add(this.startButton);
			p3.add(this.stepmode);
			this.startButton.addActionListener(this);
			this.stepmode.addActionListener(this);
			p1.add(p3);
			
			JPanel p2 = new JPanel(new GridLayout(2,1));
			p2.add(new JLabel("Simulation Speed:"));
			p2.add(this.simulationSpeed);
			this.simulationSpeed.addActionListener(this);
			p1.add(p2);
			
			this.controlPanel.add(p1);
			this.controlPanel.add(this.lastEvent);
			
			this.add(this.controlPanel,BorderLayout.SOUTH);
			
		}
		
		this.add(tabbedPanel);
	}

	public void addSimulationUIListener(SimulationUIListener l)
	{
		this.simulationUIlisteners.add(l);
	}
	

	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		Object src = ae.getSource();
		
		if(src == this.startButton)
		{
			if(! this.dws.isRunning())
			{
				dws.eventqueue.setSpeed(Double.parseDouble(this.simulationSpeed.getText()));
				if(!this.dws.eventqueue.isStepMode())
				{
					this.simulationSpeed.setEditable(false);
				}
				this.dws.runThreadSimulation();
				this.startButton.setText("Pauze");
			}
			else if(this.dws.isPauzed())
			{
				dws.eventqueue.setSpeed(Double.parseDouble(this.simulationSpeed.getText()));
				
				if(!this.dws.eventqueue.isStepMode())
				{
					this.simulationSpeed.setEditable(false);
				}
				this.dws.eventqueue.continueSim();
				this.startButton.setText("Pauze");
			}
			else
			{
				this.simulationSpeed.setEditable(true);
				this.dws.eventqueue.pauzeSim();
				this.startButton.setText("Continue");
			}
		}
		else if(src == this.stepmode)
		{
			if(this.stepmode.isSelected())
			{
				this.dws.eventqueue.enableStepMode();
				this.simulationSpeed.setEditable(true);
			}
			else
			{
				this.dws.eventqueue.disableStepMode();
			}
		}
			
	}


	@Override
	public void simulationEventOccured(Event e) 
	{
		this.lastEvent.setText(e.toString());
		this.repaint();
	}


	@Override
	public void simulationFinished(EventQueue q) 
	{
		System.out.println(" simulation finished!");
		System.out.println(" aditional waiting (changeover) time: "+dws.getWaitTime());
		System.out.println(" shift updates     : "+dws.world.getRescheduleCount());
		System.out.println(" rescheduled:total : "+dws.getUpdateRescheduleCount());
		System.out.println(" rescheduled:update: "+dws.getUpdateRescheduleCount());
		System.out.println(" rescheduled:full  : "+dws.getFullRescheduleCount());
		System.out.println(" denial of service : "+dws.getDenailOfServiceStr());
	}


	public void setSimulation(DynamicWorldSimulation sim) 
	{
		this.dws = sim;
		
		for(SimulationUIListener l : this.simulationUIlisteners)
		{
			l.simulationChanged();
		}
	}
}
