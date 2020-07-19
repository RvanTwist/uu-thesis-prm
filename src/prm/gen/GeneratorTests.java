package prm.gen;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import prm.ls.Mutation;
import prm.ls.SimulatedAnnealing;
import prm.ls.model2.MutationPlanPRM;
import prm.ls.model2.MutationPlanSegment;
import prm.ls.model2.MutationUnPlanPRM;
import prm.ls.model2.SimplePlanning;
import prm.ls.model2.phase2.F2_Planner;
import prm.problemdef.PRM;
import prm.problemdef.PRM_Instance;
import prm.problemdef.PRM_Instance_Reader;
import prm.problemdef.Segment;

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
public class GeneratorTests 
{
	public static void main(String[] args)
	{
		try 
		{
			test1();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void test1() throws IOException
	{
		System.out.println("Start test");
		PRMGenerator gen = new PRMGenerator(new File("data2/Locations.txt"), new File("Generator_Seed2.txt"), new Random());
		
		System.out.println("Random PRMs:");
		for(int i = 0 ; i < 1000 ; i++)
		{
			PRM prm = gen.generateTotalRandomPRM(330,2240);
			System.out.println(prm.saveString());
		}
		
		gen.instance.save(new File("gentest"));
		
		System.out.println("Test solving model 2:");
		
		// Test running.
		PRM_Instance instance = gen.instance;
		PRM_Instance_Reader.readWorkersOnly(new File("data/Workers1.txt"), instance);
		
		System.out.println("Check instance");
		for(PRM prm : instance.clients.values())
		{
			if(prm.route.length > 0)
			{
				final int window = prm.deadline - prm.arrival;
				int lenght = 0;
				
				for(Segment s : prm.route)
				{
					lenght += s.supervisingArea.getDistance(s.from, s.to);
				}
				
				System.out.println("("+(window-lenght)+")("+window+"-"+lenght+") PRM: "+prm+" ");
			}
		}
		
		SimplePlanning planning = new SimplePlanning(instance);
		
		System.out.println("Empty score: "+planning.calculateScore());
		
		for(int i = 0 ; i < 10 ; i ++)
		{
			planning.generateInitialSolution();
			
			final int s1 = planning.calculateScore();
			final int s2 = planning.recalculateScore();
			
			System.out.println(" score "+i+": "+s1+"|"+s2+" "+Integer.MAX_VALUE);
		}
		
		System.out.println(" test Simulated Annealing");
		
		LinkedList<Mutation> mutations = new LinkedList<Mutation>();
		
		mutations.add(new MutationPlanPRM(planning));
		mutations.add(new MutationUnPlanPRM(planning));
		mutations.add(new MutationPlanSegment(planning));
		
		planning.emptySolution();
		planning.calculateScore();
		System.out.println(" empty score: "+planning.getScore());
		
		SimulatedAnnealing annealer = new SimulatedAnnealing(planning, mutations, 6, 0.95, 10000, 1000000);
		annealer.setAllowInfeasibility(false);
		
		annealer.solve();
		
		System.out.println("Done solving: analyse solution:");
		System.out.println("Feasible: "+planning.isFeasible());
		planning.analyse();
		
		System.out.println("test Fase 2");
		F2_Planner planner = new F2_Planner(planning);
		
		planner.planAll();
		
		
		System.out.println("End test");
	}
}
