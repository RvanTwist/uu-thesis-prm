package prm.ls.model4.runs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import prm.ls.Mutation;
import prm.ls.NeedToSetSolver;
import prm.ls.SimulatedAnnealing;
import prm.ls.model4.*;
import prm.ls.model4.matching.*;
import prm.ls.model4.mutations.*;
import prm.ls.model4.matching.lpflow.*;
import prm.ls.model4.mutationStrategy.*;
import prm.problemdef.PRM_Instance;
import prm.problemdef.PRM_Instance_Reader;
import rpvt.util.Tuple;

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
public class M4_RUNS extends JFrame implements ActionListener, FocusListener, Runnable, ListSelectionListener
{
	JComboBox matchingStrategy;
	
	JButton start;
	
	JTextArea output;
	
	JPanel upperPanel;
	
	JPanel generalPanel;
	JTabbedPane pane;
	JPanel singleRunPanel;
	JPanel multiRunPanel;
	
	
	Label status;
	
	JTextField c_iterations;
	JTextField c_T;
	JTextField c_a;
	JTextField c_qsteps;
	
	JTextField c_declinePanelty;
	JTextField c_declinePaneltyBooked;
	JComboBox  c_matchingStrategy;
	JList	   c_mutations;
	JCheckBox  c_stopAtOptimum;
	JCheckBox  c_checkConsistancy;
	JComboBox  c_Shifts;
	
	// Single:
	JButton run;
	
	// Multiple
	JButton mr_run;
	JTextField mr_Seed;
	JTextField mr_iterations;
	JList	mr_Instances;
	JTextField mr_lastSeed;
	
	double instanceSize = 1.0;
	
	int mr_runs = 30;
	
	// Values:
	int iterations = M4_Constants.iterations;
	double T = M4_Constants.T;
	double a = M4_Constants.a;
	int qsteps = M4_Constants.iterationsQ;
	
	// Simulated Annealing:
	char solveMode = 0;
	public final static char SOLVEMODE_SINGLE = 0;
	public final static char SOLVEMODE_ALL = 1;
	
	SimulatedAnnealing solver;

	public JTextField c_Reduce;

	private boolean checkConsistancy = true;
	
	
	
	public M4_RUNS()
	{
		initialise_UI();
		
		this.setSize(1000,800);
		
		this.addWindowListener(new WindowAdapter(){
									@Override
									public void windowClosed(WindowEvent e)	
									{
										System.exit(1);
									}
									@Override
									public void windowClosing(WindowEvent e)
									{
										System.exit(1);
									}
								});
						
	}

	public static void main(String[] args)
	{
		new M4_RUNS().setVisible(true);
	}
	
	private void initialise_UI() 
	{
		this.setLayout(new BorderLayout());
		
		this.generalPanel = new JPanel();
		this.upperPanel = new JPanel();
		this.status = new Label("Status");
		this.output = new JTextArea();
		
//		try {
//			System.setOut(new TextAreaOutputStream(output, System.out));
//			//System.setErr(new TextAreaOutputStream(output, System.err));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
		this.pane = new JTabbedPane();
		this.singleRunPanel = new JPanel();
		this.multiRunPanel = new JPanel();
		
		this.pane.addTab("Single Run", this.singleRunPanel	);
		this.pane.addTab("Multi Run", this.multiRunPanel	);
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1,2));
		p.add(generalPanel);
		p.add(pane);
		
		this.add(upperPanel, BorderLayout.NORTH);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add(status, BorderLayout.SOUTH);
		
		
		JScrollPane sp1 = new JScrollPane(output);
		sp1.setPreferredSize(new Dimension(600,200));
		p2.add(sp1, BorderLayout.CENTER);
		
		
		this.add(p2, BorderLayout.SOUTH);
		this.add(p);
		
		this.initialiseUpperPanel();
		this.initialiseGeneralPanel();
		this.initialiseSinglePanel();
		this.initialiseMultiplePanel();
	}
	
	private void initialiseUpperPanel()
	{
		
	}
	
	private void initialiseSinglePanel()
	{	
		this.singleRunPanel.setLayout(null);
		
		int x = 15;
		int y = 15;
		int w1 = 150;
		int h = 25;
		int w2 = 250;
		
		this.run = new JButton("Run");
		this.addCompomentTo(singleRunPanel, x+w1+10, y, run, w2, h);
		
	}
	
	private void initialiseMultiplePanel()
	{	
		this.multiRunPanel.setLayout(null);
		
		int x = 15;
		int y = 15;
		int w1 = 150;
		int h = 25;
		int w2 = 250;
		
		this.mr_run = new JButton("Run ML");
		this.addCompomentTo(multiRunPanel, x+w1+10, y, mr_run, w2, h);
		
		this.addCompomentTo(multiRunPanel, x, y+=30, "Seed", w1, h, mr_Seed = new JTextField(), w2, h);
		this.addCompomentTo(multiRunPanel, x, y+=30, "Seed start", w1, h, this.mr_lastSeed = new JTextField(), w2, h);
		mr_lastSeed.setToolTipText("Used only for partially failed runs  due circumstances, draws random numbers till seed is found.");
		this.addCompomentTo(multiRunPanel, x, y+=30, "Runs: ", w1, h,mr_iterations = new JTextField(), w2, h);
		
		
		final String[] instances = new String[11];
		
		for(int i = 0 ; i < instances.length ; i++)
		{
			instances[i] = "Instance "+(i+1);
		}
		
		this.addCompomentTo(multiRunPanel, x, y+=30, "Instances: ", w1, h,
							this.mr_Instances = new JList(instances), w2, 250);
		this.mr_Instances.setSelectionModel(new SelectionModelAdd());
		this.mr_Instances.setSelectionInterval(0, 10);
		
	}
	
	private void initialiseGeneralPanel()
	{
		this.generalPanel.setLayout(null);
		
		int x = 15;
		int y = 15;
		int w1 = 150;
		int h = 25;
		int w2 = 250;
		
		this.c_iterations = new JTextField(""+M4_Constants.iterations);
		addCompomentTo(this.generalPanel, x, y, "Iterations", w1, h, this.c_iterations, w2, h );
		
		y += 30;
		addCompomentTo(this.generalPanel, x, y, "T: ", w1, h, this.c_T = new JTextField(""+M4_Constants.T), w2, h );
		
		y += 30;
		addCompomentTo(this.generalPanel, x, y, "a: ", w1, h, this.c_a = new JTextField(""+M4_Constants.a), w2, h );
		
		y += 30;
		addCompomentTo(this.generalPanel, x, y, "Q steps: ", w1, h, this.c_qsteps = new JTextField(""+M4_Constants.iterationsQ), w2, h );
		
		y += 30;
		addCompomentTo(this.generalPanel, x, y, "Matching Strategy: ", w1, h, this.c_matchingStrategy = new JComboBox(MatchingStrategies.values()), w2, h );
		this.c_matchingStrategy.setSelectedItem(MatchingStrategies.Heuristic_Replan_Local);
		
		y += 30;
		addCompomentTo(this.generalPanel, x, y, "Mutations: ", w1, h, this.c_mutations = new JList(Mutations.values()), w2, 110 );
		this.c_mutations.setSelectionModel(new SelectionModelAdd());
		this.c_mutations.setSelectionInterval(0, 4);
		
		y += 120;
		addCompomentTo(this.generalPanel, x, y, "Decline Panelty: ", w1, h, this.c_declinePanelty = new JTextField(""+M4_Constants.declinePenalty), w2, h );
		
		y += 30;
		addCompomentTo(this.generalPanel, x, y, "Decline Panelty(B): ", w1, h, this.c_declinePaneltyBooked = new JTextField(""+M4_Constants.declinePenaltyBooked), w2, h );
		
		addCompomentTo(this.generalPanel, x, y+=30, "Stop at Optimum: ", w1, h, this.c_stopAtOptimum = new JCheckBox(), w2, h );
		c_stopAtOptimum.setSelected(true);
		
		addCompomentTo(this.generalPanel, x, y+=30, "Check Consistancy: ", w1, h, this.c_checkConsistancy = new JCheckBox(), w2, h );
		c_checkConsistancy.setSelected(true);
		
		addCompomentTo(this.generalPanel, x, y+=30, "Shifts: ", w1, h, this.c_Shifts = new JComboBox(ShiftGeneration.values()), w2, h );
		addCompomentTo(this.generalPanel, x, y+=30, "Instance Size: ", w1, h, this.c_Reduce = new JTextField("1"), w2, h );
	}

	public void addCompomentTo(JPanel p, int x, int y,Component c2, int w2, int h2)
	{
		c2.setBounds(x, y, w2, h2);
		p.add(c2);
		
		if(c2 instanceof JTextField)
		{
			((JTextField)c2).addActionListener(this);
			((JTextField)c2).addFocusListener(this);
		}
		if(c2 instanceof JButton)
		{
			((JButton)c2).addActionListener(this);
		}
	}
	
	public void addCompomentTo(JPanel p, int x, int y, Component c1, int w1, int h1, Component c2, int w2, int h2)
	{
		c1.setBounds(x, y, w1, h1);
		c2.setBounds(x+w1+10, y, w2, h2);
		p.add(c1);
		p.add(c2);
		
		if(c2 instanceof JTextField)
		{
			((JTextField)c2).addActionListener(this);
			((JTextField)c2).addFocusListener(this);
		}
		if(c2 instanceof JButton)
		{
			((JButton)c2).addActionListener(this);
		}
		if(c2 instanceof JComboBox)
		{
			((JComboBox)c2).addActionListener(this);
			((JComboBox)c2).addFocusListener(this);
		}
		if(c2 instanceof JList)
		{
			((JList)c2).addFocusListener(this);
			((JList)c2).addListSelectionListener(this);
		}
		if(c2 instanceof JCheckBox)
		{
			((JCheckBox)c2).addActionListener(this);
			((JCheckBox)c2).addFocusListener(this);
		}
	}
	
	public void addCompomentTo(JPanel p, int x, int y, String s, int w1, int h1, Component c2, int w2, int h2)
	{
		addCompomentTo(p, x, y, new JLabel(s), w1, h1, c2, w2, h2);
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		Object src = e.getSource();
		
		if(src == this.run)
		{
			this.solveMode = M4_RUNS.SOLVEMODE_SINGLE;
			this.startupRunning();
		}
		else if(src == this.mr_run)
		{
			this.solveMode = M4_RUNS.SOLVEMODE_ALL;
			this.startupRunning();
		}
		this.settingChanged(src);
	}

	private void startupRunning() 
	{
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void focusGained(FocusEvent e) 
	{
		this.settingChanged(e.getSource());
	}

	@Override
	public void focusLost(FocusEvent e) 
	{
		this.settingChanged(e.getSource());
	}
	
	public void settingChanged(Object src)
	{
		if(this.solver != null)
		{
			return;
		}
		
		if(src == this.c_checkConsistancy)
		{
			this.checkConsistancy = this.c_checkConsistancy.isSelected();
		}
		if(src == this.c_Reduce)
		{
			this.instanceSize = parseDouble(this.c_Reduce,instanceSize);
		}
		if(src == this.mr_iterations)
		{
			this.mr_runs = parseInt(this.mr_iterations,mr_runs);
		}
		if(src == this.c_stopAtOptimum)
		{
			M4_Constants.StopAtOptimum = c_stopAtOptimum.isSelected();
		}
		if(src == this.c_iterations)
		{
			iterations = parseInt(this.c_iterations, iterations);
		}
		if(src == this.c_a)
		{
			a = parseDouble(this.c_a, a);
		}
		if(src == this.c_T)
		{
			T = parseDouble(this.c_T, T);
		}
		if(src == this.c_qsteps)
		{
			qsteps = parseInt(this.c_qsteps, qsteps);
		}
		if(src == this.c_declinePanelty)
		{
			M4_Constants.declinePenalty = parseInt(this.c_declinePanelty, M4_Constants.declinePenalty);
		}
		if(src == this.c_declinePanelty)
		{
			M4_Constants.declinePenalty = parseInt(this.c_declinePanelty, M4_Constants.declinePenalty);
		}
		if(src == this.c_declinePaneltyBooked)
		{
			M4_Constants.declinePenaltyBooked = parseInt(this.c_declinePaneltyBooked, M4_Constants.declinePenaltyBooked);
		}
	}
	
	public static int parseInt(JTextField f, int def)
	{
		try
		{
			return Integer.parseInt(f.getText());
		}
		catch(Exception e)
		{
			f.setText(""+def);
			return def;
		}
	}
	
	public static double parseDouble(JTextField f, double def)
	{
		try
		{
			return Double.parseDouble(f.getText());
		}
		catch(Exception e)
		{
			f.setText(""+def);
			return def;
		}
	}
	
	public void run()
	{
		if(this.solveMode == M4_RUNS.SOLVEMODE_SINGLE)
		{
			this.runSingle();
		}
		else if(this.solveMode == M4_RUNS.SOLVEMODE_ALL)
		{
			this.runMultiple();
		}
	}

	private static Random random = new Random();
	
	private M4_Planning runSingle() 
	{
		// TODO make this dynamic
		final File locationsFile = new File("data2/Locations.txt");
		final File workersFile = new File("data2/Workers5.txt");
		
		PRM_Instance instance;
		
		try
		{
			instance = PRM_Instance_Reader.makeInstance(locationsFile, workersFile);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
			instance = null;
		}
		
		// Fix the instance
		instance.checkRoutes();
		instance.addLoungeVisits();
		instance.fixMatrixReflext();
		
		if(instanceSize < 1)
		{
			System.out.println("Reducing instance!");
			instance.reduceByChance(1.0-this.instanceSize, random.nextLong());
		}
		
		return runInstance(instance,random.nextLong());
	}
	
	public void runMultiple()
	{
		// TODO Add seed
		
		
		long mainSeed;
		ShiftGeneration shift_g = (ShiftGeneration) this.c_Shifts.getSelectedItem();
		
		
		String seedS = this.mr_Seed.getText();
		
		if(seedS.equals(""))
		{
			mainSeed = random.nextLong();
		}
		else
		{
			try
			{
				mainSeed = Long.parseLong(seedS);
			}
			catch(Exception e)
			{
				System.err.println("Can't extract seed");
				mainSeed = random.nextLong();
			}
		}
		
		
		Random seedGenerator = new Random(mainSeed);
		
		
		if(!mr_lastSeed.getText().equals(""))
		{
			try
			{
				long lastSeed = Long.parseLong(mr_lastSeed.getText());
				
				while(seedGenerator.nextLong() != lastSeed);
			}
			catch(NumberFormatException e)
			{
				
			}
		}
		
		
		final Date date = new Date(System.currentTimeMillis());
		final File outf = new File("results/M4_results.txt");
		
		
		BufferedWriter wr;
		
		try 
		{
			//outf.createNewFile();
			 wr = new BufferedWriter(new FileWriter(outf));
			
			String mutatoins_s = null;
			
			for(Object o : this.c_mutations.getSelectedValues())
			{
				if(mutatoins_s == null)
				{
					mutatoins_s = o.toString();
				}
				else
				{
					mutatoins_s += "\n                      "+o.toString();
				}
			}
			
			if(mutatoins_s == null)
			{
				mutatoins_s = "";
			}
			
			wr.write("M4 Model Results\n"+
					 "Date start: "+date+"\n" +
					 "Main seed: "+mainSeed+"\n"+
					 "\n"+
					 "Solver settings:\n"+
					 "  Max Iterations    : "+this.iterations+"\n"+
					 "  Max Time          : "+M4_Constants.timeLimit+"\n"+
					 "  T start           : "+T+"\n"+
					 "  a                 : "+a+"\n"+
					 "  Q steps           : "+qsteps+"\n"+
					 "\n"+
					 "  Matching Strategy : "+this.c_matchingStrategy.getSelectedItem()+"\n"+
					 "  Mutations         : "+mutatoins_s+"\n"+
					 "  Decline Panelty   : "+M4_Constants.declinePenalty+"\n"+
					 "  Decline Panelty(B): "+M4_Constants.declinePenaltyBooked+"\n"+
					 "  Shift Generation  : "+shift_g+"\n"+
					 "  Instance Size     : "+this.instanceSize+"\n"
					);
		} 
		catch (IOException e1) 
		{
			wr = null;
			e1.printStackTrace();
			System.exit(1);
		}
		
		for(int i = 1; i <= 11 ; i++ )
			if(this.mr_Instances.isSelectedIndex(i-1))
			{
				final File locationsFile = new File("data2/Locations.txt");
				final File workersFile = new File("data2/Workers"+i+".txt");
				
				
				
				PRM_Instance instance;
				
				try
				{
					instance = PRM_Instance_Reader.makeInstance(locationsFile, workersFile);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.exit(1);
					instance = null;
				}
				
				instance.checkRoutes();
				instance.addLoungeVisits();
				instance.fixMatrixReflext();
				
				switch(shift_g)
				{
				case NoShifts 		: break;
				case ShiftsOverlap  : instance.generateShifts(); break;
				case Shifts   		: instance.generateShifts2(); break;
				default: break;
				}
				
				if(this.instanceSize < 1)
				{
					instance.reduceByChance(1.0 - instanceSize, seedGenerator.nextLong());
				}
				
				try
				{
					wr.write(	"\n"+
								"Instance "+i+"\n ("+workersFile.getPath()+")\n"+
								" PRM's: "+instance.clients.size()+"\n"+
								"\n"+
								"runs: "+this.mr_runs+"\n"+
								"seed ; time ; iterations ; total declined ; prebooked declined ; score ; robustness\n");
				}
				catch(IOException e){e.printStackTrace(); System.exit(0);}
				
				for(int r = 0 ; r < this.mr_runs ; r++)
				{
					long seed = seedGenerator.nextLong();
					System.gc();
					
					M4_Planning planning = runInstance(instance,seed);
					SimulatedAnnealing solver = planning.solver;
					
					// Print data
					try
					{
						long time = solver.getLastSolveTime();
						int iterations = solver.getIteration();
						
						int total_declined = planning.declined.size();
						int declined_booked = 0;
						
						for(M4_PRM prm : planning.declined)
						{
							if(prm.prm.isPrebooked())
								declined_booked++;
						}
						
						int score = planning.getScore();
						int robustness = planning.getScore2();
						
						// seed ; time ; iterations ; total declined ; prebooked declined ; score ; robustness
						wr.write(seed+"\t ; "+time+"\t ; "+iterations+"\t ; "+total_declined+"\t ; "+declined_booked+"\t ; "+score+"\t ; "+robustness+"\n");
						wr.flush();
					}
					catch(IOException e){e.printStackTrace(); System.exit(0);}
				}
			}
		
	}
	
	private M4_Planning runInstance(PRM_Instance instance, long seed)
	{
		
		
		// Do the instance mutations.
		
		// Not so random he
//		Random r = new Random(341679783145l);
		Random r = null;
		
		M4_Planning planning = this.makeSolver(instance, new Random(seed));
		SimulatedAnnealing annealer = planning.solver;
		
		System.out.println("Start solution: "+planning.getScore()+"|"+planning.getScore2());
		
		annealer.solve();
		planning.revertBest();
	
		
		if(checkConsistancy )
		{
			planning.testConsistancy();
		}
		
		System.out.println("End solving: declined: "+planning.declined.size());
		System.out.println("Score:              "+planning.getScore()+"|"+planning.getScore2());
		System.out.println("Declined:");
		
		for(M4_PRM prm : planning.declined)
		{
			System.out.println("- PRM "+prm.prm.prm_ID+" route: "+prm.prm.printRoute());
		}
		
		return planning;
	}
	
	public M4_Planning makeSolver(PRM_Instance i, Random r)
	{
		M4_Planning planning = new M4_Planning(i,r);
		
		planning.generatePossibleMatches();

		planning.emptySolution();
		
		MatchingStrategies s = (MatchingStrategies)this.c_matchingStrategy.getSelectedItem();
		M4_MatchingAlgorithm matching;
		
		switch(s)
		{
		case Heuristic_Simple_Insert : matching = new M4_FreeSpotMatching(); break; 
		case Heuristic_Replan_Local	 : matching = new M4_LocalMatching1(planning); break;
		case Optimise_Matching		 : matching = new M4_CFMN_Hungarian(planning); break;
		case Optimise_ILP1			 : matching = new M4_CompleteFlowMatchingNoDelay(planning,1); break;
		case Optimise_ILP2			 : matching = new M4_CompleteFlowMatchingNoDelay(planning,2); break;
		case Optimise_ILP3			 : matching = new M4_CompleteFlowMatchingNoDelay(planning,3); break;
		default : throw new Error("Cannot match: "+s);
		}
		
		ArrayList<Mutation> mutations = new ArrayList<Mutation>();
		
		for(Object mo : this.c_mutations.getSelectedValues())
			if(mo instanceof Mutations)
			{
				Mutations m = (Mutations)mo;
				switch(m)
				{
				case Plan_PRM : mutations.add(new MutationPlanDeclinedPRM(planning, matching.newForMutation())); break;
				case Decline_PRM : mutations.add(new MutationDeclinePRM(planning, matching)); break;
				case Merge_SegmentGroup : mutations.add(new MutationMergeSegmentGroups(planning, matching.newForMutation())); break;
				case Move_SegmentGroup :mutations.add( new MutationMoveSegmentGroup(planning, matching.newForMutation())); break;
				case Move_Split_SegmentGroup : mutations.add(new MutationMoveSegmentGroupUnMerge(planning, matching.newForMutation()));
				}
			}
		
		
		

		SimulatedAnnealing annealer = new SimulatedAnnealing(	planning, 
																mutations, 
																T, 
																a, 
																iterations/this.qsteps, 
																iterations,
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
	public void valueChanged(ListSelectionEvent lse) 
	{
		this.settingChanged(lse.getSource());
	}
}
