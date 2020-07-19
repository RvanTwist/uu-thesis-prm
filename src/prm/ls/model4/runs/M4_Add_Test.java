package prm.ls.model4.runs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import prm.ls.Mutation;
import prm.ls.NeedToSetSolver;
import prm.ls.SimulatedAnnealing;
import prm.ls.model4.CantPlanException;
import prm.ls.model4.M4_Constants;
import prm.ls.model4.M4_PRM;
import prm.ls.model4.M4_PRM_Segment;
import prm.ls.model4.M4_PRM_SegmentGroup;
import prm.ls.model4.M4_Planning;
import prm.ls.model4.matching.*;
import prm.ls.model4.matching.lpflow.M4_CFMN_Hungarian;
import prm.ls.model4.matching.lpflow.M4_CompleteFlowMatchingNoDelay;
import prm.ls.model4.mutationStrategy.MergeFirstMutationStrategy;
import prm.ls.model4.mutations.*;
import prm.ls.resources.ParalelResource;
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
public class M4_Add_Test 
{
	public static void main(String[] args)
	{	
		test();
	}
	
	public static void test()
	{
//		try {
//			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("output_log.txt")), true));
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		
		System.out.println("Model 4 test:");
		
		final File locationsFile = new File("data2/Locations.txt");
		final File workersFile = new File("data2/Workers1.txt");
		
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
		
		instance.addLoungeVisits();
		instance.fixMatrixReflext();
		
//		instance.generateShifts2();
//		instance.reduceByChance(0.8, 234675235967l);
		
		instance.checkRoutes();
		
		// Not so random he
		Random r = new Random(326894536);
//		Random r = null;
		
		M4_Planning planning = new M4_Planning(instance,r);
		
		planning.generatePossibleMatches();

		// Test simulated Annealing
		System.out.println("Initialise simulatedAnnealing");
		
		planning.emptySolution();
//		planning.loadSolution(Sollution.load(instance,new File("solutions/sollution07_07_22-11-2013.txt")));
//		planning.loadSolution(Sollution.load(instance,new File("solutions/M2_solution.txt")));
		planning.testConsistancy();
		
//		M4_MatchingAlgorithm matching = new M4_FreeSpotMatching();
//		M4_MatchingAlgorithm matching = new M4_CompleteFlowMatchingNoDelay(planning,1);
//		M4_MatchingAlgorithm matching = new M4_CFMN_Hungarian(planning);
		M4_MatchingAlgorithm matching = new M4_LocalMatching1(planning);
		
		Mutation plan 		= new MutationPlanDeclinedPRM(planning, matching.newForMutation());
		Mutation decline 	= new MutationDeclinePRM(planning, matching);
		Mutation merge		= new MutationMergeSegmentGroups(planning, matching.newForMutation());
		Mutation move		= new MutationMoveSegmentGroup(planning, matching.newForMutation());
		Mutation move2		= new MutationMoveSegmentGroupUnMerge(planning, matching.newForMutation());
		
//		MutationMatchAll replanAll = new MutationMatchAll(planning,matching);
		
		Mutation[] p1_muts = new Mutation[]{merge,move,move2,decline};
		Mutation[] mutations = new Mutation[]{	plan
												,decline
												,merge
												,move
												,move2 
//												,replanAll
												};
		
//		MergeFirstMutationStrategy mfms = new MergeFirstMutationStrategy(p1_muts, mutations, 50000);
		//SimulatedAnnealing annealer = new SimulatedAnnealing(planning, mfms, 6, 0.95, 1000, 10000);
//		SimulatedAnnealing annealer = new SimulatedAnnealing(planning, mfms,60, 0.95, 2000, 100000);
		
		//SimulatedAnnealing annealer = new SimulatedAnnealing(planning, mutations, 6, 0.95, 1000, 100000,r); // Ori
		SimulatedAnnealing annealer = new SimulatedAnnealing(	planning, 
																mutations, 
																M4_Constants.T, 
																M4_Constants.a, 
																M4_Constants.iterations/M4_Constants.iterationsQ, 
																M4_Constants.iterations,
																r); // Ori
		
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
		
		System.out.println("End solving: declined: "+planning.declined.size());
		
		planning.testConsistancy();
		System.out.println("Score:              "+planning.getScore()+"|"+planning.getScore2());
		planning.recalculateScore();
		System.out.println("Recalculated score: "+planning.getScore()+"|"+planning.getScore2());
		
		System.out.println("reverting to emptySolution:");
		planning.emptySolution();
		System.out.println("End reloading: declined: "+planning.declined.size());
		
		planning.revertBest();

		planning.testConsistancy();
		System.out.println("End reloading: declined: "+planning.declined.size());
		System.out.println("Score:              "+planning.getScore()+"|"+planning.getScore2());
		System.out.println("Declined:");
		
		for(M4_PRM prm : planning.declined)
		{
			System.out.println("- PRM "+prm.prm.prm_ID+" route: "+prm.prm.printRoute());
		}
		try
		{
			String fileNameRoot = "solutions/default/instance1";
			File f = new File(fileNameRoot+".txt");
			int k = 1;
			while(f.exists())
			{
				k++;
				f = new File(fileNameRoot+"("+k+").txt");
			}
			planning.getBestSolution().save(f);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void printPRMinfoAll(M4_PRM m4prm)
	{
		final PRM prm = m4prm.prm;
		System.out.println("M4_PRM "+prm.prm_ID+" window: ["+prm.arrival+","+prm.deadline+"]("+prm.getMinTravelingTime()+")");
		System.out.println(" - route : "+prm.printRoute());
		System.out.println(" - SegmentGroups:");
		
		for(M4_PRM_SegmentGroup sg : m4prm.segmentGroups)
		{
			System.out.println("   - SegmentGroup start: "+sg.getStart()+" : ["+sg.getErliestStart()+","+sg.getLatestStart()+"] fixed: "+sg.isFixed());
			
			for(M4_PRM_Segment seg : sg)
			{
				System.out.println("     - PRM_Segment start: "+seg.getStartTime()+" offset: "+seg.getOriSegmentGroupOffset()+" segment: "+seg.segment.simpleString()+" area: "+seg.area.area.id);
			}
		}
	}
}
