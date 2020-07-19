package prm.ls.model2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import prm.ls.Mutation;
import prm.ls.SimulatedAnnealing;
import prm.ls.model2.phase2.F2_Planner;
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
public class Model2Test 
{
	public static void main(String[] args)
	{	
		runTests();
		//test2();
	}
	
	static long time_sum;
	static long time_min;
	static long time_max;
	
	static int firstDec_sum;
	static int firstDec_min;
	static int firstDec_max;
	
	static int dec_sum;
	static int dec_min;
	static int dec_max;
	
	public static void runTests()
	{
		BufferedWriter wr;
		
		try
		{
			wr = new BufferedWriter(new FileWriter(new File("M2 results x20.txt")));
			wr.write("prefix \t time(ms)\t p1 dec\t p2 dec\n");
		}
		catch(IOException e)
		{
			System.out.println("Failed to create writer!");
			return;
		}
		
		final int exp_num = 20;
		
		for(int i = 1 ; i <= 11 ; i++)
		{
			time_sum = 0;
			time_min = Long.MAX_VALUE;
			time_max = 0;
			
			firstDec_sum = 0;
			firstDec_min = Integer.MAX_VALUE;
			firstDec_max = 0;
			
			dec_sum = 0;
			dec_min = Integer.MAX_VALUE;
			dec_max = 0;
			
			try 
			{
				if(i == 1)
					wr.write("\n");
				wr.write("Instance: "+i+"\n");
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int r = 0 ; r < exp_num ; r++)
			{
				runTest(wr,"data2/Locations.txt","data2/Workers"+i+".txt", "i:"+i+"("+r+")");
			}
			
			try 
			{
				wr.write("\n");
				wr.write("AVG\t "+(time_sum/exp_num)+"\t "+(firstDec_sum/exp_num)+"\t "+(dec_sum/exp_num)+"\n");
				wr.write("MIN\t "+(time_min)+"\t "+(firstDec_min)+"\t "+(dec_min)+"\n");
				wr.write("MAX\t "+(time_max)+"\t "+(firstDec_max)+"\t "+(dec_max)+"\n");
				
				wr.flush();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	public static void runTest(BufferedWriter wr, String locations, String workers, String prefix)
	{
		System.out.println("Collecting Garbage");
		System.gc();
		
		final File locationsFile = new File(locations);
		final File workersFile = new File(workers);
		
		PRM_Instance instance;
		
		System.out.println("loading instance ( "+locations+", "+workers+" )");
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
		instance.fixMatrixReflext();
//		instance.fixTimeUnfeasibleWindows();
		
//		System.out.println("Check instance");
//		for(PRM prm : instance.clients.values())
//		{
//			if(prm.route.length > 0)
//			{
//				final int window = prm.deadline - prm.arrival;
//				int lenght = 0;
//				
//				for(Segment s : prm.route)
//				{
//					lenght += s.supervisingArea.getDistance(s.from, s.to);
//				}
//				
//				System.out.println("("+(window-lenght)+")("+window+"-"+lenght+") PRM: "+prm+" ");
//			}
//		}
		
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
		
		long start_time = System.currentTimeMillis();
		annealer.solve();
		
		System.out.println("analyse solution:");
		System.out.println("Feasible: "+planning.isFeasible());
		planning.analyse();
		
		final int firstDeclined = planning.declined.size();
		
		System.out.println("test Fase 2");
		F2_Planner planner = new F2_Planner(planning);
		
		planner.planAll();
		
		long time = System.currentTimeMillis() - start_time;
		
		System.out.println("end Test");
		
		int declined = firstDeclined + planner.getDeclinedCount();
		System.out.println(" All declined: "+(firstDeclined + planner.getDeclinedCount())+" p1: "+firstDeclined);
		
		// Update statistics;
		
		time_sum = time_sum + time;
		time_max = Math.max(time_max, time);
		time_min = Math.min(time_min, time);
		
		firstDec_sum = firstDec_sum + firstDeclined;
		firstDec_max = Math.max(firstDec_max, firstDeclined);
		firstDec_min = Math.min(firstDec_min, firstDeclined);
		
		dec_sum = dec_sum + declined;
		dec_max = Math.max(dec_max, declined);
		dec_min = Math.min(dec_min, declined);
		
		
		try 
		{
			wr.write(prefix+"\t "+time+"\t "+firstDeclined+"\t "+declined+"\n");
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void test2()
	{
		System.out.println("test2");
		
		final File locationsFile = new File("data2/Locations.txt");
		final File workersFile = new File("data2/Workers1.txt");
		
		PRM_Instance instance;
		
		System.out.println("start Test");
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
		
//		System.out.println("Check instance");
//		for(PRM prm : instance.clients.values())
//		{
//			if(prm.route.length > 0)
//			{
//				final int window = prm.deadline - prm.arrival;
//				int lenght = 0;
//				
//				for(Segment s : prm.route)
//				{
//					lenght += s.supervisingArea.getDistance(s.from, s.to);
//				}
//				
//				System.out.println("("+(window-lenght)+")("+window+"-"+lenght+") PRM: "+prm+" ");
//			}
//		}
		
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
		
		System.out.println("analyse solution:");
		System.out.println("Feasible: "+planning.isFeasible());
		planning.analyse();
		
		final int firstDeclined = planning.declined.size();
		
		System.out.println("test Fase 2");
		F2_Planner planner = new F2_Planner(planning);
		
		planner.planAll();
		
		System.out.println("end Test");
		
		System.out.println(" All declined: "+(firstDeclined + planner.getDeclinedCount())+" p1: "+firstDeclined);
		try 
		{
			planner.saveSolution(new File("solutions/M2_solution.txt"));
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
