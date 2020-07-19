package simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.swing.*;

import prm.gen.FlightPlanPRMGenerator;
import prm.ls.Mutation;
import prm.ls.NeedToSetSolver;
import prm.ls.SimulatedAnnealing;
import prm.ls.model4.M4_Constants;
import prm.ls.model4.M4_DynamicFrameWork;
import prm.ls.model4.M4_Planning;
import prm.ls.model4.matching.M4_FreeSpotMatching;
import prm.ls.model4.matching.M4_LocalMatching1;
import prm.ls.model4.matching.M4_MatchingAlgorithm;
import prm.ls.model4.matching.lpflow.M4_CFMN_Hungarian;
import prm.ls.model4.matching.lpflow.M4_CompleteFlowMatchingNoDelay;
import prm.ls.model4.mutations.MutationDeclinePRM;
import prm.ls.model4.mutations.MutationMergeSegmentGroups;
import prm.ls.model4.mutations.MutationMoveSegmentGroup;
import prm.ls.model4.mutations.MutationMoveSegmentGroupUnMerge;
import prm.ls.model4.mutations.MutationPlanDeclinedPRM;
import prm.ls.model4.runs.MatchingStrategies;
import prm.ls.model4.runs.Mutations;
import prm.problemdef.PRM_Instance;
import prm.problemdef.PRM_Instance_Reader;
import prm.problemdef.Sollution;

import rpvt.util.BufferedLineWriter;
import rpvt.util.DefaultWindowListener;
import rpvt.util.DualStream;
import simulation.ui.SimulationUI;

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
public class SimpleSimulationRunner extends Frame implements ActionListener, Runnable
{
	Label status;
	JComboBox instances;
	
	JTextArea seeds;
	
	JTextField generateSeedsNumber;
	JButton   generateSeeds;
	
	JButton start;
	JButton createInitialSolution;
	
	JTextArea results;
	JCheckBox robustnessEnabled;
	
//	JComboBox finishedSimList;
//	ArrayList<DynamicWorldSimulation> simulations = new ArrayList<DynamicWorldSimulation>();
	JButton showResults;
	
//	JCheckBox robustness;
	BufferedWriter resultWriter;
	String resultStr = "";
	String seedString = "";
	
	File file;
	
	String instanceName;
	
	Thread runner;
	private DynamicWorldSimulation sim;
	
	public static void main(String[] args)
	{
		new SimpleSimulationRunner().setVisible(true);
	}
	
	public SimpleSimulationRunner()
	{
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.addWindowListener(new DefaultWindowListener());
		
		this.add(new Label("Simple Simulation Runner"));
		
		// Instances
		JPanel p0 = new JPanel();
		
		String[] instancesS = new String[12];
		for(int i = 0 ; i < instancesS.length ; i++)
			instancesS[i] = "Instance "+(i+1);
		
		this.instances = new JComboBox(instancesS);
		this.instances.setSelectedIndex(0);
		
		this.createInitialSolution = new JButton("Create Initial Sollution");
		this.createInitialSolution.addActionListener(this);
		
		p0.add(instances);
		p0.add(this.createInitialSolution);
		this.add(p0);
		
		// Robustness Panelty
		robustnessEnabled = new JCheckBox("Optimise Robustness",M4_Constants.optimizeRobustness);
		robustnessEnabled.addActionListener(this);
		this.add(robustnessEnabled);
		
		// Seeds
		this.seeds = new JTextArea();
		this.seeds.setPreferredSize(new Dimension(300,300));
		this.add(new JScrollPane(seeds));
		
		// SeedsGenerator
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,2));
		
		this.generateSeedsNumber = new JTextField("5");
		p1.add(this.generateSeedsNumber);
		
		this.generateSeeds = new JButton("Generate Seeds");
		this.generateSeeds.addActionListener(this);
		p1.add(this.generateSeeds);
		
		this.add(p1);
		
		// Status
		status = new Label("Select an instance to start");
		this.add(status);
		
		// Start button
		this.start = new JButton("Start");
		start.addActionListener(this);
		this.add(start);
	
		// Results
		this.results = new JTextArea();
		this.results.setPreferredSize(new Dimension(300,300));
		this.add(new JScrollPane(results));
		
		// Simulation UI
		JPanel p2 = new JPanel();
//		this.finishedSimList = new JComboBox(new DefaultComboBoxModel());
//		this.finishedSimList.setPreferredSize(new Dimension(175,25));
//		p2.add(this.finishedSimList);
		this.showResults = new JButton("show");
		this.showResults.addActionListener(this);
		p2.add(this.showResults);
		
		this.add(p2);
		
		this.setSize(350, 800);
		
		
	}
	
	public void printResultln(String s)
	{
		final String s2 = s + "\n";
		try
		{
			this.resultWriter.write(s2);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		this.resultStr += s2;
		this.results.setText(this.resultStr);
	}

	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		Object o = ae.getSource();
		
		if(o == this.generateSeeds)
			this.generateSeeds();
		else if(o == this.start)
			this.startRuns();
		else if(o == this.showResults)
			this.showSimulation();
		else if(o == this.createInitialSolution)
			this.createInstance();
		else if(o == this.robustnessEnabled)
		{
			M4_Constants.optimizeRobustness = robustnessEnabled.isSelected();
			System.out.println(" Enable Robustness: "+M4_Constants.optimizeRobustness);
		}
	}

	private void showSimulation() 
	{
		if(this.sim == null)
			return;
		
		new SimulationUI(this.sim,null).setVisible(true);
		
//		int selectedIndex = this.finishedSimList.getSelectedIndex();
		
//		if(selectedIndex >= 0)
//		{
//			new SimulationUI(this.simulations.get(selectedIndex),null).setVisible(true);
//		}
	}

	private void startRuns() 
	{
		this.runner = new Thread(this);
		this.runner.start();
		
	}

	public void createInstance()
	{
		this.instanceName = (String)this.instances.getSelectedItem();
		int instanceID = this.instances.getSelectedIndex() + 1;
		String locationsFile = "data2/Locations.txt";
		String workersFile = "data2/Workers"+instanceID+".txt";
		
		String dir = (M4_Constants.optimizeRobustness ? "default" : "norobust");
		String solutionFile = "solutions/"+dir+"/instance"+instanceID+".txt";
		File solFile = new File(solutionFile);
		
		if(solFile.exists())
		{
			JOptionPane.showMessageDialog(this, "Sollution File already exists!");
			return;
		}
		
		PRM_Instance instance;
		try
		{
			instance = PRM_Instance_Reader.makeInstance(	new File(locationsFile), new File(workersFile));
		}catch(IOException e)
		{
			throw new Error(e);
		}
		instance.addLoungeVisits();
		instance.fixMatrixReflext();
		instance.checkRoutes();
		
		M4_Planning planning;
		
		boolean unhappy = true; // (Unhappy with results)
		while(unhappy)
		{
			planning = this.makeSolver(instance, new Random());
			SimulatedAnnealing annealer = planning.solver;
			
			System.out.println("Start solution: "+planning.getScore()+"|"+planning.getScore2());
			
			annealer.solve();
			planning.revertBest();
			
			int value = JOptionPane.showConfirmDialog(this,  "Do you want to save the following sollution?\n"
				    + "Declined PRMS: "+planning.declined.size()+" ("+planning.getScore()+")\n"
				    + "Robustness Score: "+planning.getScore2());
			
			if (value == JOptionPane.YES_OPTION) 
			{
				unhappy = false;
				
				try 
				{
					planning.getBestSolution().save(solFile);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			} 
			if (value == JOptionPane.NO_OPTION) 
			{
			   // Too bad
			}
		}
		
		
	}
	
	public M4_Planning makeSolver(PRM_Instance i, Random r)
	{
		M4_Planning planning = new M4_Planning(i,r);
		
		planning.generatePossibleMatches();

		planning.emptySolution();
		
		M4_MatchingAlgorithm matching = new M4_LocalMatching1(planning);
		
		ArrayList<Mutation> mutations = new ArrayList<Mutation>();
		
		mutations.add(new MutationPlanDeclinedPRM(planning, matching.newForMutation()));
		mutations.add(new MutationDeclinePRM(planning, matching)); 
		mutations.add(new MutationMergeSegmentGroups(planning, matching.newForMutation()));
		mutations.add( new MutationMoveSegmentGroup(planning, matching.newForMutation()));
		mutations.add(new MutationMoveSegmentGroupUnMerge(planning, matching.newForMutation()));

		
		
		

		SimulatedAnnealing annealer = new SimulatedAnnealing(	planning, 
																mutations, 
																M4_Constants.T, 
																M4_Constants.a, 
																M4_Constants.iterations/M4_Constants.iterationsQ, 
																M4_Constants.iterations,
																r);
		
		annealer.setTimeLimit(M4_Constants.timeLimit);
		
		planning.solver = annealer;
		annealer.setAllowInfeasibility(false);
		annealer.addLocalSearchListener(planning);
		
		if(matching instanceof NeedToSetSolver)
		{
			((NeedToSetSolver)matching).setSA(annealer);
		}
		
		return planning;
	}
	
	@Override
	public void run() 
	{
		this.status.setText("Initialising Runs.");
		
		this.start.setEnabled(false);
		this.seeds.setEditable(false);
		this.instances.setEnabled(false);
		this.generateSeeds.setEnabled(false);
		this.generateSeedsNumber.setEditable(false);
		
		String[] data = this.seeds.getText().split("\n");
		
		int instanceID = this.instances.getSelectedIndex() + 1;
		String seeds = null;
		
		for(int i = 0 ; i < data.length ; i++)
		{
			String str = data[i];
			
			if(str.startsWith("Instance: "))
			{
				if(seeds != null)
				{
					this.doInstanceRuns(instanceID, seeds);
				}
				
				instanceID = Integer.parseInt(str.substring("Instance: ".length()).trim());
				seeds = null;
			}
			else if(str.equals(""))
			{
				// Do nothing
			}
			else
			{
				seeds = (seeds == null ? str : seeds + "\n"+str);
			}
			
			if(i + 1 == data.length)
			{
				this.doInstanceRuns(instanceID, seeds);
			}
		}
		
		if(seeds == null)
			seeds = "";
		
		
		
		
		this.start.setEnabled(true);
		this.seeds.setEditable(true);
		this.instances.setEnabled(true);
		this.generateSeeds.setEnabled(true);
		this.generateSeedsNumber.setEditable(true);
	}
	
	public void doInstanceRuns(int instanceID, String seedStr)
	{
		this.seedString = seedStr;
		
		this.status.setText("Initialising Simulation.");
		
		this.instanceName = "Instance " + instanceID;
		
		String dir = (M4_Constants.optimizeRobustness ? "default" : "norobust");
		
		String locationsFile = "data2/Locations.txt";
		String workersFile = "data2/Workers"+instanceID+".txt";
		String solutionFile = "solutions/"+dir+"/instance"+instanceID+".txt";
		String flightPlanFile = "allFlightPlans.txt";
		
		// Check file pressence
		if(!new File(locationsFile).exists())
		{ status.setText("File not found: "+locationsFile); return; }
		if(!new File(workersFile).exists())
		{ status.setText("File not found: "+workersFile); return; }
		if(!new File(solutionFile).exists())
		{ status.setText("File not found: "+solutionFile); return; }
		if(!new File(flightPlanFile).exists())
		{ status.setText("File not found: "+flightPlanFile); return; }
		
		
		String[] seedData = seedStr.split("\n");
		long[]	 seeds = new long[seedData.length];
		
		try
		{
			for(int i = 0 ; i < seedData.length ; i++)
			{
				seeds[i] = Long.parseLong(seedData[i]);
			}
		}
		catch(NumberFormatException nfe)
		{
			this.status.setText("Unable to read the Seeds!");
			return;
		}
		
		try
		{
			this.setUpResultsWriter();
			
			for(int i = 0 ; i < seeds.length ; i++)
			{
				long seed = seeds[i];
				status.setText("Running simulation: "+(i+1)+" "+seed);
				this.executeSimulation(locationsFile, workersFile, flightPlanFile, solutionFile, seed, instanceID);
			}
		}
		catch(Exception e)
		{ // Dirty top level exception catcher, wanted to hook this code on the error thrower but don't know how.
			e.printStackTrace();
			status.setText("Some error has occured.");
			status.setForeground(Color.red);
			
			if(resultWriter != null)
			{
				try 
				{
					resultWriter.flush();
				} 
				catch (IOException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			SimulationUI ui = new SimulationUI(this.sim);
		}
		
		this.status.setText("Done Simulation");
		
		try 
		{
			this.resultWriter.flush();
			this.resultWriter.close();
			
			FileWriter fw = new FileWriter(this.file);
			fw.write(this.resultStr);
			fw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private void setUpResultsWriter() throws IOException 
	{
		long time = System.currentTimeMillis();
		Date date = new Date(time);
	
		DateFormat format = new SimpleDateFormat("dd-MM-yyyy_HH-mm-SS");
		
		String fileName = "simresults/Simresult_robust_"+this.instanceName+"_"+format.format(date);
		String extension = ".txt";
		
		File file = new File(fileName+extension);
		int fileCounter = 1;
		while(file.exists())
		{
			fileCounter++;
			file = new File(fileName+"("+fileCounter+")"+extension);
		}
		
		file.createNewFile();
		
		this.file = file;
		
		this.resultWriter = new BufferedWriter(new FileWriter(file));
		this.resultStr = "";
		
		this.printResultln("Simulatoin Results ");
		this.printResultln("variant: robust");
		this.printResultln("Instance: "+instanceName);
		this.printResultln("Seeds:");
		this.printResultln(seedString);
		this.printResultln("");
		this.printResultln("Results");
		
		this.printResultln("seed ;declined(booked) ; declined(other) ; waitTime(min) ; worker updates ; reschedule_updates ; full reschedule");
		
		
		resultWriter.flush();
	}
	
	private void registerResults(	long seed,
									int declined_booked, int declined_other, double waitTime, 
									int worker_updates, int reschedule_updates, int reschedule_full ) 
	{
		String s = 	seed+" ; "+
					declined_booked+" ; "+declined_other+" ; "+waitTime+" ; "+
					worker_updates+" ; "+reschedule_updates+" ; "+reschedule_full;
		
		System.out.println(s);

		this.printResultln(s);

	}
	
	private void executeSimulation(String locationsFile, String workersFile, String flightplanFile, String solutionFile, long seed, int iid)
	{		
		// Code
		System.gc();
		System.out.println("Simulation test");
		
		Random main = new Random(seed);
		
		long genSeed = main.nextLong();
		long simSeed = main.nextLong();
		
		Random rand1 = new Random(genSeed);
		Random rand3 = new Random(simSeed);
		
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
			gen= new FlightPlanPRMGenerator(instance,flightplanFile,rand1);
		} catch(IOException e)
		{
			throw new Error(e);
		}
		gen.generateData();
		gen.generateRandomPlanes();
		
		Sollution solution = Sollution.load(instance, new File(solutionFile));
		
		M4_DynamicFrameWork dfw = new M4_DynamicFrameWork(instance, solution, gen,  gen.getFlightPlan(), rand3);
		
		dfw.generateRandomPRMs(100);
		
		System.out.println("Loading dynamic world done, initialising simulation");
		DynamicWorldSimulation sim = new DynamicWorldSimulation(dfw,rand3);
		
		this.sim = sim;
//		this.finishedSimList.addItem("Run "+seed);
//		this.simulations.add(sim);
		
		System.out.println("Loading simulation complete!");
		sim.runSimulation();
		
		// Register results
		
		this.registerResults(	seed, 
								sim.getDeclinedCountBooked(), 
								sim.getDeclinedCountOther(), 
								sim.getWaitTime(), 
								sim.getWorkerUpdateCount(), 
								sim.getUpdateRescheduleCount(), 
								sim.getFullRescheduleCount()
								);
		
	}
	

	private void generateSeeds() 
	{
		int number;
		try
		{	 number = Integer.parseInt(this.generateSeedsNumber.getText()); }
		catch(NumberFormatException e)
		{	
			this.seeds.setText("Failed to generate the seeds!\n"+
							   " '"+this.generateSeedsNumber.getText()+"' is not a number!");
			return;
		}
		
		String seeds = "";
		for(int i = 0 ; i < number ; i++)
		{
			seeds += (i==0 ? "" : "\n")+(new Random()).nextLong();
		}
		
		this.seeds.setText(seeds);
	}
	
}
