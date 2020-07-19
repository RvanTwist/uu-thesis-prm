package simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import javax.swing.JFrame;

import prm.gen.FlightPlanPRMGenerator;
import prm.ls.model4.M4_DynamicFrameWork;
import prm.problemdef.PRM_Instance;
import prm.problemdef.PRM_Instance_Reader;
import prm.problemdef.Sollution;
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
/**
 * Note: SimlulatedInstance was first version but supports planes 
 * @author rene
 *
 */
public class Simulation_Runner 
{
	public static void main(String[] args)
	{
		/// Settings
		int instanceID = 2;
		long seed = -6319824281865829502l;
		
		if(args.length > 2)
		{
			instanceID = Integer.parseInt(args[0]);
			seed = Long.parseLong(args[1]);
		}
			
		System.out.println("Instance: "+instanceID+" Seed: "+seed);
		
		
		String locationsFile = "data2/Locations.txt";
		String workersFile = "data2/Workers"+instanceID+".txt";
		String solutionFile = "solutions/default/instance"+instanceID+".txt";
		String flightPlanFile = "allFlightPlans.txt";
		
		JFrame tmpFrame = new JFrame();
		
		
		
		execute(locationsFile, workersFile, flightPlanFile, solutionFile, seed, instanceID);
		
	}
	
	public static void execute(String locationsFile, String workersFile, String flightplanFile, String solutionFile, long seed, int iid)
	{
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
		
		
		
		SimulationUI ui = new SimulationUI(sim);
		ui.mainSeed = seed;
		ui.simSeed = simSeed;
		ui.genSeed = genSeed;
		ui.instanceID = iid;
		
		ui.setVisible(true);
		
		
		System.out.println("Loading simulation complete!");
//		sim.runSimulation();
		
	}
}
