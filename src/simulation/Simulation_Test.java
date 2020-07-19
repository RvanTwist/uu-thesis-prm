package simulation;

import java.io.File;
import java.io.IOException;

import prm.ls.Mutation;
import prm.ls.NeedToSetSolver;
import prm.ls.SimulatedAnnealing;
import prm.ls.model4.M4_PRM;
import prm.ls.model4.M4_Planning;
import prm.ls.model4.matching.M4_LocalMatching1;
import prm.ls.model4.matching.M4_MatchingAlgorithm;
import prm.ls.model4.mutations.MutationDeclinePRM;
import prm.ls.model4.mutations.MutationMergeSegmentGroups;
import prm.ls.model4.mutations.MutationMoveSegmentGroup;
import prm.ls.model4.mutations.MutationMoveSegmentGroupUnMerge;
import prm.ls.model4.mutations.MutationPlanDeclinedPRM;
import prm.problemdef.PRM_Instance;
import prm.problemdef.PRM_Instance_Reader;
import prm.problemdef.Sollution;
import simulation.model.Simulation_Instance;

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
public class Simulation_Test 
{
	public static void main(String[] args) throws IOException
	{
		System.out.println("Simulation:");
		
		final File locationsFile = new File("data2/Locations.txt");
		final File workersFile = new File("data2/Workers11.txt");
		
		PRM_Instance instance = PRM_Instance_Reader.makeInstance(locationsFile, workersFile);
		instance.addLoungeVisits();
		instance.checkRoutes();
		Sollution s = Sollution.load(instance, new File("solutions/instance_11_test.txt"));
		
//		Sollution s = solveInstance(locationsFile,workersFile);
//		s.save(new File("solutions/instance_11_test.txt"));
		
		
		
		System.out.println("Load simulation:");
		Simulation_Instance simulation = new Simulation_Instance(s);
		System.out.println("Done loading simulation:");
	}

	private static Sollution solveInstance(File locationsFile, File workersFile) 
	{
		System.out.println("Model 4 run:");
		
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
		
		instance.checkRoutes();
		
		M4_Planning planning = new M4_Planning(instance);
		
		planning.generatePossibleMatches();

		// Test simulated Annealing
		System.out.println("Initialise simulatedAnnealing");
		
		planning.emptySolution();
//		planning.loadSolution(Sollution.load(instance,new File("solutions/sollution07_07_22-11-2013.txt")));
//		planning.loadSolution(Sollution.load(instance,new File("solutions/M2_solution.txt")));
		planning.testConsistancy();
		
//		M4_MatchingAlgorithm matching = new M4_FreeSpotMatching();
//		M4_MatchingAlgorithm matching = new M4_CompleteFlowMatchingNoDelay(planning);
//		M4_MatchingAlgorithm matching = new M4_CFMN_Hungarian(planning);
		M4_MatchingAlgorithm matching = new M4_LocalMatching1(planning);
//		M4_MatchingAlgorithm matching = new M4_LocalMatching2(planning);
		
		Mutation plan 		= new MutationPlanDeclinedPRM(planning, matching.newForMutation());
		Mutation decline 	= new MutationDeclinePRM(planning, matching);
		Mutation merge		= new MutationMergeSegmentGroups(planning, matching.newForMutation());
		Mutation move		= new MutationMoveSegmentGroup(planning, matching.newForMutation());
		Mutation move2		= new MutationMoveSegmentGroupUnMerge(planning, matching.newForMutation());
		
		Mutation[] p1_muts = new Mutation[]{merge,move,move2,decline};
		Mutation[] mutations = new Mutation[]{plan, decline, merge, move, move2};
		
//		MergeFirstMutationStrategy mfms = new MergeFirstMutationStrategy(p1_muts, mutations, 50000);
		//SimulatedAnnealing annealer = new SimulatedAnnealing(planning, mfms, 6, 0.95, 1000, 10000);
//		SimulatedAnnealing annealer = new SimulatedAnnealing(planning, mfms,60, 0.95, 2000, 100000);
		
		SimulatedAnnealing annealer = new SimulatedAnnealing(planning, mutations, 6, 0.95, 1000, 100000);
		
		planning.solver = annealer;
		annealer.setAllowInfeasibility(false);
		annealer.addLocalSearchListener(planning);
		
		if(matching instanceof NeedToSetSolver)
		{
			((NeedToSetSolver)matching).setSA(annealer);
		}
		
		System.out.println("Start solution: "+planning.getScore());
		
		annealer.solve();
		
		System.out.println("End solving: declined: "+planning.declined.size());
		
		planning.testConsistancy();
		System.out.println("reverting to emptySolution:");
		planning.emptySolution();
		System.out.println("End reloading: declined: "+planning.declined.size());
		
		planning.revertBest();
		planning.testConsistancy();
		System.out.println("End reloading: declined: "+planning.declined.size());
		
		System.out.println("Declined:");
		
		for(M4_PRM prm : planning.declined)
		{
			System.out.println("- PRM "+prm.prm.prm_ID+" route: "+prm.prm.printRoute());
		}
		
		return planning.getBestSolution();
	}
}
