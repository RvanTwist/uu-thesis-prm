package simulation.ui;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import javax.swing.*;

import prm.gen.FlightPlanPRMGenerator;
import prm.ls.model4.M4_DynamicFrameWork;
import prm.problemdef.PRM_Instance;
import prm.problemdef.PRM_Instance_Reader;
import prm.problemdef.Sollution;
import rpvt.util.DualStream;
import simulation.DynamicWorldSimulation;

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
public class RunPanel extends JPanel implements ActionListener
{
	public SimulationUI sim;
	
	// Load panel
	JPanel loadPanel;
	JList instance;
	JButton load;
	
	JTextField generatorSeed;
	JButton    randomGeneratorSeed;
	JTextField simulationSeed;
	JButton    randomSimulationSeed;
	
	
	static String[] instances = new String[]{	"Instance  1",
												"Instance  2",
												"Instance  3",
												"Instance  4", 
												"Instance  5",
												"Instance  6",
												"Instance  7",
												"Instance  8",
												"Instance  9",
												"Instance 10",
												"Instance 11"  };
	
	public RunPanel(SimulationUI ui)
	{
		this.setLayout(new GridLayout(1,2));
		
		this.sim = ui;
		
		this.add(this.makeLoadPanel());
		this.add(new Label("Empty space"));
		
	}


	private JPanel makeLoadPanel() 
	{
		this.loadPanel = new JPanel();
		this.loadPanel.setLayout(new BoxLayout(this.loadPanel,BoxLayout.Y_AXIS));
		
		this.loadPanel.add(new Label("Loading panel:"));
		instance = new JList(instances);
		this.loadPanel.add(instance);
		
		this.load = new JButton("Load");
		this.load.addActionListener(this);
		this.loadPanel.add(load);
		
		
		this.generatorSeed = new JTextField(""+sim.genSeed);
		this.randomGeneratorSeed = new JButton("Random seed!");
		this.randomGeneratorSeed.addActionListener(this);
		
		this.loadPanel.add(new Label(""));
		this.loadPanel.add(new Label("Generator seed:"));
		this.loadPanel.add(this.generatorSeed);
		this.loadPanel.add(this.randomGeneratorSeed);
		
		this.simulationSeed = new JTextField(""+sim.simSeed);
		this.randomSimulationSeed = new JButton("Random seed!");
		this.randomSimulationSeed.addActionListener(this);
		
		this.loadPanel.add(new Label(""));
		this.loadPanel.add(new Label("Simulation seed:"));
		this.loadPanel.add(this.simulationSeed);
		this.loadPanel.add(this.randomSimulationSeed);
		
		
		
		
		return this.loadPanel;
	}


	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		Object src = ae.getSource();
		
		if(src == this.load)
		{
			this.loadAction();
		}
		else if(src == this.randomGeneratorSeed)
		{
			this.generatorSeed.setText(""+(new Random()).nextLong());
		}
		else if(src == this.randomSimulationSeed)
		{
			this.simulationSeed.setText(""+(new Random()).nextLong());
		}
	}


	private void loadAction() 
	{
		
		int instanceID = this.instance.getSelectedIndex();
		
		if(instanceID == -1)
		{
			JOptionPane.showMessageDialog(sim, "Please select an instance");
			return;
		}
		
		instanceID++;
		
		System.out.println("Loading instance: "+instanceID);
		
		String locationsFile = "data2/Locations.txt";
		String workersFile = "data2/Workers"+instanceID+".txt";
		String solutionFile = "solutions/default/instance"+instanceID+".txt";
		String flightPlanFile = "allFlightPlans.txt";
		
		try 
		{
			//BufferedWriter bw = new BufferedWriter(new FileWriter(new File("console.txt")));
			
			System.setOut(new PrintStream(new DualStream(System.out, new FileOutputStream(new File("console.txt")))));
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		// Code
		System.out.println("Simulation test");
		
		long genSeed = Long.parseLong(this.generatorSeed.getText());
		long simSeed = Long.parseLong(this.simulationSeed.getText());
		
		sim.genSeed = genSeed;
		sim.simSeed = simSeed;
		
		Random generatorSeed = new Random(genSeed);
		Random simulationSeed = new Random(simSeed);
		
		PRM_Instance instance;
		try{
		instance = PRM_Instance_Reader.makeInstance(	new File(locationsFile), 
																	new File(workersFile));
		}catch(IOException e)
		{
			throw new Error(e);
		}
		instance.addLoungeVisits();
		instance.fixMatrixReflext();
		instance.checkRoutes();
		
		FlightPlanPRMGenerator gen;
		try{
			gen= new FlightPlanPRMGenerator(instance,flightPlanFile,generatorSeed);
		} catch(IOException e)
		{
			throw new Error(e);
		}
		gen.generateData();
		gen.generateRandomPlanes();
		
		Sollution solution = Sollution.load(instance, new File(solutionFile));
		
		M4_DynamicFrameWork dfw = new M4_DynamicFrameWork(instance, solution, gen,  gen.getFlightPlan(), simulationSeed);
		
		dfw.generateRandomPRMs(100);
		
		System.out.println("Loading dynamic world done, initialising simulation");
		
		DynamicWorldSimulation sim = new DynamicWorldSimulation(dfw,simulationSeed);
		
		
		this.sim.setSimulation(sim);
		
		System.out.println("Loading simulation complete!");
		
	}
}
