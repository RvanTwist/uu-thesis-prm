package prm.ls.model4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import prm.gen.FlightPlan;
import prm.gen.FlightPlanPRMGenerator;
import prm.gen.PRM_Generator;
import prm.ls.LocalSearchListener;
import prm.ls.LocalSearchListenerAdaptor;
import prm.ls.Mutation;
import prm.ls.NeedToSetSolver;
import prm.ls.SimulatedAnnealing;
import prm.ls.SolutionFacture;
import prm.ls.model4.matching.M4_FreeSpotMatching;
import prm.ls.model4.matching.M4_LocalMatching1;
import prm.ls.model4.matching.M4_MatchingAlgorithm;
import prm.ls.model4.matching.M4_SwitchMatchingAlgs;
import prm.ls.model4.mutations.MutationDeclinePRM;
import prm.ls.model4.mutations.MutationMergeInvalidSegmentGroups;
import prm.ls.model4.mutations.MutationMergeSegmentGroups;
import prm.ls.model4.mutations.MutationMergeSegmentGroupsSinglePRM;
import prm.ls.model4.mutations.MutationMoveInvalidSegmentGroup;
import prm.ls.model4.mutations.MutationMoveSegmentGroup;
import prm.ls.model4.mutations.MutationMoveSegmentGroupUnMerge;
import prm.ls.model4.mutations.MutationPlanCertainPRM;
import prm.ls.model4.mutations.MutationPlanDeclinedPRM;
import prm.ls.model4.mutations.MutationReplanSegmentGroup;
import prm.problemdef.*;

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
public class M4_DynamicFrameWork 
{
	private int rescheduled = 0;
	private int rescheduledUpdate = 0;
	private int rescheduledTotal = 0;
	
	// Inital
	public PRM_Instance 	instance;
	
	// Current
	private Sollution	current_solution;
	private M4_Planning current_planning = null;
	
	// New
	Sollution		new_solution;
	
	// Assests.
	public FlightPlan flightPlan;
	PRM_Generator prmGenerator;
	
	Random rand;
	
	// RandomlyGenerated PRMs
	public TreeSet<PRM> newPRMs = new TreeSet<PRM>(
					new Comparator<PRM>(){
											public int compare(PRM prm1, PRM prm2) 
											{
												int comp1 = prm1.booking_time - prm2.booking_time;
												return (comp1 == 0 ? prm1.compareTo(prm2) : comp1);
											}
										 });
	
	public TreeSet<PRM> newDeclined = new TreeSet<PRM>();
	// Dynamic values
	int time;
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Dynamic Framework test");
		
		Random main = new Random(1000);
		Random rand1 = new Random(main.nextLong());
		Random rand2 = new Random(main.nextLong());
		
		int instanceID = 1;
		
		PRM_Instance instance = PRM_Instance_Reader.makeInstance(	new File("data2/Locations.txt"), 
																	new File("data2/Workers"+instanceID+".txt"));
		instance.addLoungeVisits();
		instance.fixMatrixReflext();
		instance.checkRoutes();
		
		FlightPlanPRMGenerator gen = new FlightPlanPRMGenerator(instance,"allFlightPlans.txt",rand1);
		gen.generateData();
		gen.generateRandomPlanes();
		
		Sollution solution = Sollution.load(instance, new File("solutions/default/instance"+instanceID+".txt"));
		
		M4_DynamicFrameWork dfw = new M4_DynamicFrameWork(instance, solution, gen,  gen.getFlightPlan(), rand2);
		
		dfw.generateRandomPRMs(100);
		
		System.out.println("Loading done, starting up dynamic world.");
		dfw.runDynamicWorld();
		
		System.out.println("Done solving. New declined: "+dfw.newDeclined.size()+"/"+dfw.newPRMs.size());
		
	}
	
	public static M4_DynamicFrameWork TheOne;
	
	public M4_DynamicFrameWork(PRM_Instance i, Sollution s, PRM_Generator gen, FlightPlan pl,Random r) 
	{
		current_solution = s;
		instance = i;
		this.rand = r;
		
		this.prmGenerator = gen;
		
		time = 0;
		this.flightPlan = pl;
		
		if(TheOne != null)
		{
//			throw new Error("Why did I made this, so only one copy could exist?");
		}
		TheOne = this;
		
		current_planning = new M4_Planning(instance);
		try 
		{
			current_planning.loadSolution(s);
		} 
		catch (CantPlanException e) 
		{
			e.printStackTrace();
			throw new Error(e);
		}
	}
	
	public void generateRandomPRMs(int amount)
	{
		int c = 0;
		while(c < amount)
		{
			PRM prm = this.prmGenerator.generateRandomPRM();
			if(prm != null)
			{
				prm.booked = false;
				this.newPRMs.add(prm);
				c++;
			}
		}
	}
	
	public void runDynamicWorld()
	{
		for(PRM prm : this.newPRMs)
		{
			instance.clients.put(prm.prm_ID, prm);
			instance.checkRoutes();
			
			int time = prm.booking_time;
			
			this.solveIncrement(time, prm);
		}
	}
	
	
	/**
	 * TODO : Add removal PRMS from planning if something is teribly wrong.
	 * @param time
	 */
	public void reschedule(int time)
	{
		
		this.rescheduled++;
		this.rescheduledTotal++;
		
		Random rand = new Random(this.rand.nextLong());
		
		System.out.println("\nReschedule ");
		final int fixedTimeTresshold = time + 20;
		
		final M4_Planning planning = new M4_Planning(instance);
		current_planning = planning;
		try
		{
			planning.loadSolution(current_solution);
		}
		catch(CantPlanException e)
		{
			e.printStackTrace();
			throw new Error(e);
		}
		
		planning.checkForInvalidSegments();
		
		planning.saveBest();
		planning.fixSegmentsTillTime(fixedTimeTresshold);
		planning.setMoveableWindowTresshold(time);
		
		
		int lb = planning.calculate_Score2LB();
		
		
		System.out.println(" Lowerbound of score2: "+lb);
		
		planning.generatePossibleMatches();
		
		
		
		//DEBUG
//		planning.testConsistancy();
		
		double prev_score2 = planning.getScore2();
		
//		M4_MatchingAlgorithm matching = new M4_FreeSpotMatching();
//		M4_MatchingAlgorithm matching = new M4_CompleteFlowMatchingNoDelay(planning,1);
//		M4_MatchingAlgorithm matching = new M4_CFMN_Hungarian(planning);
		M4_MatchingAlgorithm insertMatching = new M4_FreeSpotMatching();
		M4_MatchingAlgorithm localmatching = new M4_LocalMatching1(planning);
		
		M4_MatchingAlgorithm matching = new M4_SwitchMatchingAlgs(insertMatching,localmatching, fixedTimeTresshold);
		
		Mutation plan 		= new MutationPlanDeclinedPRM(planning, matching.newForMutation());
		Mutation decline 	= new MutationDeclinePRM(planning, matching);
		Mutation merge		= new MutationMergeSegmentGroups(planning, matching.newForMutation());
		Mutation move		= new MutationMoveSegmentGroup(planning, matching.newForMutation());
		Mutation move2		= new MutationMoveSegmentGroupUnMerge(planning, matching.newForMutation());
		MutationMoveInvalidSegmentGroup move3		= new MutationMoveInvalidSegmentGroup(planning,matching.newForMutation());
		
		// Try to fix segments:
		M4_SegmentGroup[] segs = planning.invalidSegmentGroups.toArray(new M4_SegmentGroup[planning.invalidSegmentGroups.size()]);
		
		System.out.println("Try fixing: "+planning.invalidSegmentGroups.size());
		SolutionFacture f = new SolutionFacture();
		for(M4_SegmentGroup segg : segs)
			if(segg instanceof M4_PRM_SegmentGroup)
			{
				f.clear();
				try
				{
					move3.planPRM(segg, f);
				}
				catch(CantPlanException e)
				{
					e.printStackTrace();
					f.feasible = false;
				}
				
				if(f.feasible)
				{
					move3.applyMutation();
				}
				else
				{
					move3.rejectMutation();
				}
			}
		System.out.println("End fixing: "+planning.invalidSegmentGroups.size());
		planning.saveBest();
		planning.makeAllCheckpoints();
		
		
		
		final Mutation[] mutations = new Mutation[]{plan,decline,merge,move,move2,move3};
		
//		if(!planning.isOptimum())
		{
			final int iterations = M4_Constants.iterations;
			
			SimulatedAnnealing annealer = new SimulatedAnnealing(	planning, 
							mutations, 
							M4_Constants.T, 
							M4_Constants.a, 
							iterations/M4_Constants.iterationsQ, 
							iterations,
							rand); // Ori
			
////			Debug LS
//			annealer.addLocalSearchListener(new LocalSearchListenerAdaptor(){
//				public void onAccept(SimulatedAnnealing anealer){planning.testConsistancy();}
//				public void onReject(SimulatedAnnealing anealer){planning.testConsistancy();}
//			});
			
			annealer.setTimeLimit(M4_Constants.timeLimit);
			
			planning.solver = annealer;
			annealer.setAllowInfeasibility(false);
			annealer.addLocalSearchListener(planning);
			
			if(matching instanceof NeedToSetSolver)
			{
			((NeedToSetSolver)matching).setSA(annealer);
			}
			
			//planning.recalculateScore();
			System.out.println("Start solution: "+planning.getScore()+"|"+planning.getScore2());
			
			annealer.solve();
			
			if(planning.getRandomInvalidSG() != null)
			{
				throw new Error("Failed to fix the segmentGroup: handle this case!");
			}
			
			this.current_planning = planning;
			this.current_solution = planning.getBestSolution();
		}
	}
	
	public static List<M4_PRM_SegmentGroup> emptyList = new ArrayList();
	
	public void solveIncrement(int time, PRM prm)
	{	
		ArrayList<PRM> newPRMs = new ArrayList<PRM>();
		newPRMs.add(prm);
		
		this.reschedule(time, this.current_planning, emptyList, newPRMs );
	}
	
	public Iterable<M4_Area> getAreas()
	{
		return this.current_planning.areas.values();
	}

	public Iterable<M4_PRM> getPRMs() 
	{
		return this.current_planning.prms;
	}

	public FlightPlan getFlightPlan() 
	{
		return this.flightPlan;
	}

	public void reschedule(int rescheduleTime, M4_Planning planning, Collection<M4_PRM_SegmentGroup> susp_segs, Collection<PRM> newPRMs)
	{	
		
		
		// Add 5 more minutes reschedule time
//		rescheduleTime += 5;
		
		// Set some general parameters and variables:
		Random rand = new Random(this.rand.nextLong());
		final int fixedTimeTresshold = rescheduleTime + 20;
		this.current_planning = planning;
		this.rescheduled++;
		
		System.out.println("Reschedule sequence 2! "+rescheduleTime+" threshold: "+fixedTimeTresshold);
		
		boolean requireFullReschedule = false;
		
		// Add suspended segments that require replaning:
		planning.suspendedSegmentGroupsReplan.addAll(susp_segs);
		
		// Reload the instance sequence
		planning.unfix();
		
		try
		{
			planning.loadSolution(this.current_solution);
		}
		catch(CantPlanException e)
		{
			e.printStackTrace();
			throw new Error(e);
		}
		
		planning.reloadInstance();
		planning.recalculateScore();
		planning.generatePossibleMatches();
		planning.fixSegmentsTillTime(fixedTimeTresshold);
		planning.setMoveableWindowTresshold(rescheduleTime);
		planning.makeAllCheckpoints();
		planning.saveBest();
		planning.checkForInvalidSegments();
		
		int lb = planning.calculate_Score2LB();
		int lb1 = planning.calculate_Score1DecLB();
		planning.score2_lowerBound = lb;
		planning.score1_lowerBound = lb1;
		
		// Load Required Mutations and strategies
		ArrayList<Mutation> mutations = new ArrayList<Mutation>();
		
		M4_MatchingAlgorithm insertMatching = new M4_FreeSpotMatching();
		M4_MatchingAlgorithm localmatching = new M4_LocalMatching1(planning);
		M4_MatchingAlgorithm matching = new M4_SwitchMatchingAlgs(insertMatching,localmatching, fixedTimeTresshold);
		
		MutationPlanDeclinedPRM 			plan 		 = new MutationPlanDeclinedPRM(planning, matching.newForMutation());
		MutationMergeSegmentGroups 			merge		 = new MutationMergeSegmentGroups(planning, matching.newForMutation());
		MutationMoveSegmentGroup 			move		 = new MutationMoveSegmentGroup(planning, matching.newForMutation());
		MutationMoveSegmentGroupUnMerge 	move2		 = new MutationMoveSegmentGroupUnMerge(planning, matching.newForMutation());
		
		MutationMoveInvalidSegmentGroup		moveInvalid  = new MutationMoveInvalidSegmentGroup(this.current_planning,matching.newForMutation());
		MutationMergeInvalidSegmentGroups	mergeInvalid = new MutationMergeInvalidSegmentGroups(this.current_planning,matching.newForMutation());
		
		mutations.add(plan); mutations.add(merge); mutations.add(move); mutations.add(move2); 
		mutations.add(moveInvalid); mutations.add(mergeInvalid);
		
		
		{// Attempt insertions:
			//New PRM insertions
			for(PRM prm : newPRMs)
			{
				if(!this.attemptInsertPRM(prm))
				{
					requireFullReschedule = true;
				}
			}
		
			// Move invalid segmentGroups
			if(!this.attemptMoveInvalid())
			{
				requireFullReschedule = true;
			}
			
			// Replan Suspended Segments
			if(!this.attemptReplanSuspendedSegmentGroups(susp_segs))
			{
				requireFullReschedule = true;
			}
		}
		
		if(requireFullReschedule)
		{
			this.rescheduledTotal++;
			
			final int iterations = M4_Constants.iterations;
			
			SimulatedAnnealing annealer = new SimulatedAnnealing(	planning, 
							mutations, 
							M4_Constants.T, 
							M4_Constants.a, 
							iterations/M4_Constants.iterationsQ, 
							iterations,
							rand); // Ori
			
			annealer.setTimeLimit(M4_Constants.timeLimit);
			
			planning.solver = annealer;
			annealer.setAllowInfeasibility(false);
			annealer.addLocalSearchListener(planning);
			
			if(matching instanceof NeedToSetSolver)
			{
				((NeedToSetSolver)matching).setSA(annealer);
			}
			
			System.out.println("Start solution: "+planning.getScore()+"|"+planning.getScore2());
			
			annealer.solve();
		}
		else
		{
			this.rescheduledUpdate++;
		}	
		
		int notplanned = 0;
		for(PRM prm : newPRMs)
		{
			M4_PRM m4_prm = planning.getPRM(prm);
			if(m4_prm == null || !m4_prm.isPlanned())
				notplanned++;
		}
		
		planning.checkForInvalidSegments();
		
		System.out.println("Done unplanned PRMs: "+notplanned+" unplanned SGs: "+planning.suspendedSegmentGroupsReplan.size()+" invalidSegments: "+planning.invalidSegmentGroups.size());
		
		if(planning.getRandomInvalidSG() != null)
		{
			for(M4_SegmentGroup sg : planning.invalidSegmentGroups)
			{
				if(sg instanceof M4_PRM_SegmentGroup)
				{	
					M4_PRM_SegmentGroup prmsg = (M4_PRM_SegmentGroup)sg;
					M4_PRM m4_prm = prmsg.prm;
					PRM prm = m4_prm.prm;
					
					// Allow unplanning
					m4_prm.unfix();
					try 
					{
						m4_prm.unPlan();
					} catch (CantPlanException e) 
					{
						e.printStackTrace();
						throw new Error(e);
					}
					prm.allowPlan = false;
					System.out.println("Removed PRM: "+prm+" because he has invalid segments.");
				}
			}
		}
		
		if(!planning.suspendedSegmentGroupsReplan.isEmpty())
		{
			for(M4_SegmentGroup sg : planning.suspendedSegmentGroupsReplan)
			{
				if(sg instanceof M4_PRM_SegmentGroup)
				{
					M4_PRM_SegmentGroup prmsg = (M4_PRM_SegmentGroup)sg;
					M4_PRM m4_prm = prmsg.prm;
					PRM prm = m4_prm.prm;
					
					m4_prm.unfix();
					try
					{
						m4_prm.unPlan();
					}
					catch(CantPlanException e)
					{
						e.printStackTrace();
						throw new Error(e);
					}
					prm.allowPlan = false;
					System.out.println("Removed PRM: "+prm+" because he has unplanned suspended segments.");
				}
			}
		}
		
		
		planning.makeAllCheckpoints();
		planning.saveBest();
		this.current_planning = planning;
		this.current_solution = planning.getBestSolution();
		
	}
	
	private boolean attemptReplanSuspendedSegmentGroups( Collection<M4_PRM_SegmentGroup> suspSegs) 
	{	
		if(suspSegs.size() == 0)
			return true;
		
		current_planning.suspendedSegmentGroupsReplan.clear();
		current_planning.suspendedSegmentGroupsReplan.addAll(suspSegs);
		
		int start_Susp = this.current_planning.suspendedSegmentGroupsReplan.size();
		int tries = 300 * start_Susp;
		
		System.out.println("# Attempt Replanning "+start_Susp+" suspended Segmentgroups in the Schedule.");
		
		for(M4_SegmentGroup sg : current_planning.suspendedSegmentGroupsReplan)
		{
			if(sg instanceof M4_PRM_SegmentGroup)
			{
				M4_PRM prm = ((M4_PRM_SegmentGroup)sg).prm;
				System.out.println("   PRM "+prm.prm.prm_ID+" "+sg);
			}
			else
			{
				System.out.println("   PRM ? "+sg);
			}
		}
		
		SolutionFacture f = new SolutionFacture();
		
		M4_MatchingAlgorithm		insertMatching 	= new M4_FreeSpotMatching();
		MutationMoveInvalidSegmentGroup 	replanSG		= new MutationMoveInvalidSegmentGroup(this.current_planning,insertMatching);
		replanSG.replanOnlyMode = true;
		MutationMergeInvalidSegmentGroups	mergeInvalid = new MutationMergeInvalidSegmentGroups(this.current_planning,insertMatching);
		mergeInvalid.replanOnlyMode = true;
		
		for(int i = 0 ; i < tries && current_planning.invalidSegmentGroups.size() > 0; i++)
		{
			f.clear();
			try
			{
				replanSG.generateMutation(f);
			}
			catch(CantPlanException e)
			{
				f.feasible = false;
			}
			
			if(f.feasible)
				replanSG.applyMutation();
			else
				replanSG.rejectMutation();
			
			f.clear();
			try
			{
				mergeInvalid.generateMutation(f);
			}
			catch(CantPlanException e)
			{
				f.feasible = false;
			}
			if(f.feasible)
				mergeInvalid.applyMutation();
			else
				mergeInvalid.rejectMutation();
		}
		
		int new_Replan = this.current_planning.suspendedSegmentGroupsReplan.size();
		
		if( new_Replan < start_Susp)
		{
			this.current_planning.makeAllCheckpoints();
			this.current_planning.saveBest();
		}
		
		System.out.println("  Done Replanning "+(start_Susp - new_Replan)+"/"+start_Susp+
		" suspended Segmentgroups in the Schedule.");
		
		return current_planning.suspendedSegmentGroupsReplan.isEmpty();
	}

	private boolean attemptMoveInvalid() 
	{
		int start_Invalid = this.current_planning.invalidSegmentGroups.size();
		int tries = 300 * start_Invalid;
		
		if(tries == 0)
			return true;
		
		System.out.println("# Attempt Moving "+this.current_planning.invalidSegmentGroups.size()+
							" invalid Segmentgroups in the Schedule.");
		
		for(M4_SegmentGroup sg : current_planning.invalidSegmentGroups)
		{
			if(sg instanceof M4_PRM_SegmentGroup)
			{
				M4_PRM prm = ((M4_PRM_SegmentGroup)sg).prm;
				System.out.println("   PRM "+prm.prm.prm_ID+" "+sg);
			}
			else
			{
				System.out.println("   PRM ? "+sg);
			}
		}
		
		SolutionFacture f = new SolutionFacture();
		
		M4_MatchingAlgorithm insertMatching = new M4_FreeSpotMatching();
		MutationMoveInvalidSegmentGroup moveInvalid	= new MutationMoveInvalidSegmentGroup(this.current_planning,insertMatching);
		moveInvalid.invalidOnlyMode = true;
		MutationMergeInvalidSegmentGroups	mergeInvalid = new MutationMergeInvalidSegmentGroups(this.current_planning,insertMatching);
		mergeInvalid.invalidOnlyMode = true;
		
		for(int i = 0 ; i < tries && current_planning.invalidSegmentGroups.size() > 0; i++)
		{
			f.clear();
			try
			{
				moveInvalid.generateMutation(f);	
			}
			catch(CantPlanException e)
			{
				f.feasible = false;
			}
			
			
			if(f.feasible)
				moveInvalid.applyMutation();
			else
				moveInvalid.rejectMutation();
			
			f.clear();
			try
			{
				mergeInvalid.generateMutation(f);
			}
			catch(CantPlanException e)
			{
				f.feasible = false;
			}
			if(f.feasible)
				mergeInvalid.applyMutation();
			else
				mergeInvalid.rejectMutation();
		}
		
		int new_Invalid = this.current_planning.invalidSegmentGroups.size();
		
		if( new_Invalid < start_Invalid)
		{
			this.current_planning.makeAllCheckpoints();
			this.current_planning.saveBest();
		}
		
		System.out.println("  Done "+new_Invalid+"/"+start_Invalid+
		" invalid Segmentgroups left in the Schedule.");
		
		return new_Invalid == 0;
	}

	private boolean attemptInsertPRM(PRM prm) 
	{
		final boolean prevDebug = M4_Planning.DEBUG;
//		M4_Planning.DEBUG = true;
		
		System.out.println("# Attempt inserting PRM: "+prm+" into the Schedule.");
		SolutionFacture f = new SolutionFacture();
		
		M4_PRM newPRM = this.current_planning.getPRM(prm);
		
		if(newPRM == null)
		{
			prm.allowPlan = false;
			
			System.out.println(" Impossible to insert PRM: "+prm+" into the Schedule.");			
			return true;
//			throw new Error("New PRM is null how are we supposed to plan it?");
		}
		
		M4_MatchingAlgorithm insertMatching = new M4_FreeSpotMatching();
		MutationPlanCertainPRM 				plan_s 	   = new MutationPlanCertainPRM(newPRM,current_planning, insertMatching);
		MutationMergeSegmentGroupsSinglePRM merge_s    = new MutationMergeSegmentGroupsSinglePRM(newPRM, current_planning, insertMatching);
		
		for(int i = 0 ; i < 300 && !newPRM.isPlanned() ; i++)
		{
			f.clear();
			try
			{
				merge_s.generateMutation(f);
			}
			catch(CantPlanException e)
			{
//				e.printStackTrace();
				f.feasible = false;
			}
			
			if(M4_Planning.DEBUG)
			{
				System.out.println("Merge feasible: "+f.feasible);
			}
			
			if(f.feasible)
			{
				merge_s.applyMutation();
				break;
			}
			else
			{
				merge_s.rejectMutation();
			}
			
			f.clear();
			try
			{
				plan_s.generateMutation(f);
			}
			catch(CantPlanException e)
			{
				f.feasible = false;
			}
			
			if(M4_Planning.DEBUG)
			{
				System.out.println("Plan feasible: "+f.feasible);
			}
			
			if(f.feasible)
			{
				plan_s.applyMutation();
				break;
			}
			else
			{
				plan_s.rejectMutation();
			}
		}
		
		
		M4_Planning.DEBUG = prevDebug;
		
		if(newPRM.isPlanned())
		{
			this.current_planning.makeAllCheckpoints();
			this.current_planning.saveBest();
			
			System.out.println(" succesfull inserting PRM: "+prm+" into the Schedule.");
			return true;
		}
		else
		{
			System.out.println(" failed inserting PRM: "+prm+" into the Schedule.");
			return false;
		}
		
		
		
	}

	public void resetCounters()
	{
		this.rescheduled = 0;
		this.rescheduledTotal = 0;
		this.rescheduledUpdate = 0;
	}

	public int getUpdateRescheduleCount() 
	{
		return this.rescheduledUpdate;
	}

	public int getFullRescheduleCount() 
	{
		return this.rescheduledTotal;
	}
	
	public int getRescheduleCount() 
	{
		return this.rescheduled;
	}
	
	public M4_Planning getCurrentPlanning()
	{
		return this.current_planning;
	}
	
	public Sollution getCurrentSollution()
	{
		return this.current_solution;
	}
	
	/**
	 * Saves the current plannings objects as best!
	 */
	public M4_Planning savePlanning()
	{
		this.current_planning.makeAllCheckpoints();
		this.current_planning.saveBest();
		this.current_solution = this.current_planning.getBestSolution();
		
		return this.current_planning;
	}
}
