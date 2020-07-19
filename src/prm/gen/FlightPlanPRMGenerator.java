package prm.gen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import prm.problemdef.*;
import rpvt.util.RBTree;
import rpvt.util.RBTreeNode;
import simulation.Distributions;
import simulation.model.Plane;

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
public class FlightPlanPRMGenerator implements PRM_Generator
{
	FlightPlan plan;
	
	FlightPlan generatorPlan;
	PRMGenerator generatorData;
	
	public final static int Max_Time = 60 * 24;
	public final static int Min_Time = 60 * 4 + 15;
	
	final Random rand;
	
	
	public FlightPlanPRMGenerator(String locFile, String workerFile, String FlightPlanFile) throws IOException
	{
		this(locFile,workerFile,FlightPlanFile, new Random());
	}
	
	public FlightPlanPRMGenerator(String locFile, String workerFile, String FlightPlanFile, Random r) throws IOException
	{
		PRM_Instance instance = PRM_Instance_Reader.makeInstance(new File(locFile), new File(workerFile));
		instance.addLoungeVisits();
		instance.checkRoutes();
		
		this.rand = r;
		
		plan = new FlightPlan(instance);
		plan.importPlanesFromInstance();
		
		generatorPlan = new FlightPlan(instance);
		generatorPlan.readFlightPlan("allFlightPlans.txt");
		
		generatorData = new PRMGenerator(instance, new File("Generator_Seed2.txt"), new Random(rand.nextLong()));
		generatorData.setIdDispensertoMax();
	}
	
	public FlightPlanPRMGenerator(PRM_Instance instance, String FlightPlanFile, Random r) throws IOException
	{
		this.rand = r;
		
		plan = new FlightPlan(instance);
		plan.importPlanesFromInstance();
		
		generatorPlan = new FlightPlan(instance);
		generatorPlan.readFlightPlan("allFlightPlans.txt");
		
		generatorData = new PRMGenerator(instance, new File("Generator_Seed2.txt"), new Random(rand.nextLong()));
		generatorData.setIdDispensertoMax();
	}
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Generator 2 test");
		FlightPlanPRMGenerator gen = new FlightPlanPRMGenerator("data2/Locations.txt","data2/Workers1.txt","allFlightPlans.txt");
		
		
		gen.printPlan();
		
		System.out.println("Generate new planes");
		gen.generateData();
		gen.printPlan();
		
		gen.generateRandomPlanes();
		
		int prm_generated = 0;
		System.out.println("Test adding PRMs");
		for(int i = 0 ; i < 100 ; i ++)
		{
			PRM prm = gen.generateRandomPRM();
			
			if(prm != null)
			{
				prm_generated++;
			}
		}
		System.out.println("generated prms: "+prm_generated);
		
		System.out.println("Generator 2 test end");
	}
	
	public boolean generateRandomArrivingPlane()
	{
		int time = Min_Time + 
						5 * (
								(int)Math.max(0, Math.min(Distributions.randomNormal(30, 9, rand), 60))+
								(int)(rand.nextDouble() * (15 * 12 + 6)) );
		
		ArrayList<LocationAlias> gates = this.generatorData.pickup_airplane;
		Map<LocationAlias,ArrayList<LocationAlias>> map = this.generatorData.bus_airplane_terminal;
		
		LocationAlias planeLoc = gates.get((int)(gates.size() * rand.nextDouble()));
		
		ArrayList<LocationAlias> busStops = map.get(planeLoc);
		
		LocationAlias gateLoc  = (busStops == null ? planeLoc : busStops.get((int)(rand.nextDouble() * busStops.size())).getAlias());
		
		gateLoc = (gateLoc == null ? planeLoc : gateLoc);
		
		
		if(validNewPlane(true, time, gateLoc, planeLoc))
		{
			plan.getArrivingPlaneOrCreate(time, gateLoc, planeLoc);
			return true;
		}
		
		//System.out.println("Try to generate : "+time+" "+gateLoc+" "+planeLoc);
		
		return false;
	}
	
	public boolean generateRandomDepartingPlane()
	{
		int time = Min_Time + 
						5 * (
								(int)Math.max(0, Math.min(Distributions.randomNormal(30, 9,rand), 60))+
								(int)(rand.nextDouble() * (15 * 12 + 6)) );
		
		ArrayList<LocationAlias> gates = this.generatorData.delivery_airport;
		Map<LocationAlias,ArrayList<LocationAlias>> map = this.generatorData.bus_terminal_airplane;
		
		LocationAlias planeLoc = gates.get((int)(gates.size() * rand.nextDouble()));
		
		ArrayList<LocationAlias> busStops = map.get(planeLoc);
		
		LocationAlias gateLoc  = (busStops == null ? planeLoc : busStops.get((int)(rand.nextDouble() * busStops.size())).getAlias());
		
		gateLoc = (gateLoc == null ? planeLoc : gateLoc);
		
//		System.out.println("Try to generate : "+time+" "+gateLoc+" "+planeLoc);
		
		if(validNewPlane(false, time, gateLoc, planeLoc))
		{
			plan.getDepartingPlaneOrCreate(time, gateLoc, planeLoc);
			return true;
		}
		
		return false;
	}
	
	public void generateData()
	{
		int countArriving = 0;
		int countDeparting = 0;
		
		for(Airplane plane : generatorPlan.arrivingPlanes)
		{
			final int time = plane.time;
			final LocationAlias gLoc = plane.gate.location;
			final LocationAlias pLoc = plane.planeLocation.location;
			
			if(validNewPlane(true, time, gLoc, pLoc))
			{
				plan.getArrivingPlaneOrCreate(time, gLoc, pLoc);
				countArriving++;
			}
		}
		
		for(Airplane plane : generatorPlan.departingPlanes)
		{
			final int time = plane.time;
			final LocationAlias gLoc = plane.gate.location;
			final LocationAlias pLoc = plane.planeLocation.location;
			
			if(validNewPlane(true, time, gLoc, pLoc))
			{
				plan.getDepartingPlaneOrCreate(time, gLoc, pLoc);
				countDeparting++;
			}
		}
		
		System.out.println("Added: "+countArriving+" Arriving Planes");
		System.out.println("Added: "+countDeparting+" Departing Planes");
	}
	
	public boolean validNewPlane(boolean arriving, int time, LocationAlias gateLoc, LocationAlias planeLoc)
	{
		if(time > Max_Time)
			return false;
		
		Gate gate = this.plan.getTerminal(gateLoc.getArea()).getGate(gateLoc);
		AirplaneParkingSpot parking = this.plan.getPlaneParkingSpot(planeLoc);
		
		Airplane_SearchItem planeSearch = new Airplane_SearchItem();
		planeSearch.set(time,gate,planeLoc);
		
		RBTree<Airplane> schedule = (arriving ? plan.arrivingPlanes : plan.departingPlanes);
		Airplane p = schedule.find(planeSearch);
		
		if(p != null) // Plane already exists!
			return false;
		
		// Check parking;
		{
			RBTreeNode<Airplane> node = parking.planes.getlowerNode(planeSearch);
			
			if(node != null && node.getValue().time + 90 > time)
			{ // Parking spot is already taken.
				return false;
			}
		}
		
		// Check gate
		{
			RBTreeNode<Airplane> node = gate.airplanes.getlowerNode(planeSearch);
			if(node != null)
			{
				if(arriving)
				{
					if(gateLoc == planeLoc && node.getValue().time + 90 > time)
					{
						return false;
					}	
				}
				else
				{
					if(node.getValue().time + 90 > time)
					{
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	public void printPlan()
	{
		System.out.println("Flightplan:");
		System.out.println(" * Arriving: ("+this.plan.arrivingPlanes.size()+")");
		for(Airplane p : this.plan.arrivingPlanes)
		{
			Gate 	 g = p.gate;
			Terminal t = g.terminal;
			
			int hours = p.time / 60;
			int minutes = p.time % 60;
			
			String timeString = (hours < 10 ? "0"+hours : hours)+":"+(minutes < 10 ? "0"+minutes : minutes);
			
			System.out.println(" > "+timeString+" Terminal "+t.area.id+" Gate "+g.location.getLocationId()+" Plane: "+p.planeLocation.location.getLocationId()+ " ("+p.occurance+")");
		}
		System.out.println(" * Departing: ("+this.plan.departingPlanes.size()+")");
		for(Airplane p : this.plan.departingPlanes)
		{
			Gate 	 g = p.gate;
			Terminal t = g.terminal;
			
			int hours = p.time / 60;
			int minutes = p.time % 60;
			
			String timeString = (hours < 10 ? "0"+hours : hours)+":"+(minutes < 10 ? "0"+minutes : minutes);
			
			System.out.println(" > "+timeString+" Terminal "+t.area.id+" Gate "+g.location.getLocationId()+" Plane: "+p.planeLocation.location.getLocationId()+ " ("+p.occurance+")");
		}
	}

	public void generateRandomPlanes()
	{
		int newArrivalCount = 0;
		int newDepartCount = 0;
		
		for(int i = 0 ; i < 1000 ; i++)
		{
			if( this.generateRandomArrivingPlane() )
			{
				newArrivalCount++;
			}
			
			if( this.generateRandomDepartingPlane() )
			{
				newDepartCount++;
			}
		}
		
		System.out.println("Total random planes added: "+newArrivalCount+" "+newDepartCount);
	}
	
	@Override
	public PRM generateRandomArrivingPRM() 
	{
		Airplane arrivingPlane = plan.arrivingPlanes.getRandomValue(rand);
		Area arriveTerminal = arrivingPlane.gate.location.getArea();
		
		LocationAlias pickup   = arrivingPlane.planeLocation.location;
		LocationAlias pickupBus = (arrivingPlane.gate.location == arrivingPlane.planeLocation.location 
									? null : arrivingPlane.gate.location.getAlias());
		LocationAlias delivery = this.generatorData.getRandomDelivery(arriveTerminal);
		
		if(delivery == null)
		{
			return null;
		}
		
		int id = generatorData.prm_id_dispenser++;
		final int weight = (int)(rand.nextDouble()*1.5 + 2);
		
		int release 	= arrivingPlane.time;
		int deadline 	= release + 90; // TODO: Did Choose arbitary 1.5 hours to bring the prm to their destination. Find something beter. 
		
		PRM prm = new PRM(id, "Arriving PRM "+id, release, deadline, pickup, delivery, weight,false);
		prm.booking_time = release - 120;
		
		this.generatorData.getBestRoute(pickup, pickupBus, null, delivery, false, prm, false);
		prm.initialiseSegments();
		
		int min_time = prm.getMinTravelingTime();
		int time	 = deadline - release;
		
		if(min_time > time)
		{
			return null;
		}
		
		System.out.println("ARRIVING PRM ["+release+","+deadline+"] duration: "+time+" min_time: "+min_time+" slack: "+(time - min_time));
		
		prm.arrivingPlane = true;
		prm.departingPlane = false;
		
		return prm;
	}
	
	@Override
	public PRM generateRandomDepartingPRM() 
	{
		Airplane departingPlane = plan.departingPlanes.getRandomValue(rand);
		Area departingTerminal = departingPlane.gate.location.getArea();
		
		LocationAlias delivery   = departingPlane.planeLocation.location;
		LocationAlias deliveryBus = (departingPlane.gate.location == departingPlane.planeLocation.location 
									? null : departingPlane.gate.location.getAlias());
		
		
		LocationAlias pickup = this.generatorData.getRandomPickup(departingTerminal);
		
		if(pickup == null)
		{
			return null;
		}
		
		int id = generatorData.prm_id_dispenser++;
		final int weight = (int)(rand.nextDouble()*1.5 + 2);
		
		final int deadline 	= departingPlane.time;
		final int release 	= deadline - 20 - (int)Distributions.randomGamma(5,8, rand);
		
		
		PRM prm = new PRM(id, "Departing PRM "+id, release, deadline, pickup, delivery, weight,true);
		prm.booking_time = release - 5;
		
		this.generatorData.getBestRoute(pickup, null, deliveryBus, delivery, true, prm, true);
		prm.initialiseSegments();
		
		int min_time = prm.getMinTravelingTime();
		int time	 = deadline - release;
		
		if(min_time > time)
		{
			prm.route = null;
			this.generatorData.getBestRoute(pickup, null, deliveryBus, delivery, false, prm, true);
			min_time = prm.getMinTravelingTime();
			
			if(min_time > time)
			{
				return null;
			}
		}
		
		System.out.println("DEPART PRM ["+release+","+deadline+"] duration: "+time+" min_time: "+min_time+" slack: "+(time - min_time));
		
		prm.arrivingPlane = false;
		prm.departingPlane = true;
		
		return prm;
	}

	@Override
	public PRM generateRandomPRM() 
	{
		double rnd = rand.nextDouble();
		
		if(rnd < 0.5)
			return generateRandomTransferPRM() ;
		else if(rnd < 0.75)
			return this.generateRandomArrivingPRM();
		else
			return this.generateRandomDepartingPRM();
	}

	@Override
	public PRM generateRandomTransferPRM() 
	{
		Airplane arrivingPlane = plan.arrivingPlanes.getRandomValue(rand);
		
		LocationAlias pickup   = arrivingPlane.planeLocation.location;
		LocationAlias pickupBus = (arrivingPlane.gate.location == arrivingPlane.planeLocation.location 
									? null : arrivingPlane.gate.location.getAlias());
		
		final int release 	= arrivingPlane.time;
		
		// Find an random connecting Airplane between 1 hour and 5 hour.
		ArrayList<Airplane> connecting = new ArrayList<Airplane>();
		
 		for(Airplane p : plan.departingPlanes)
		{
			if(		release + 60 <= p.time &&
					p.time <= release + 300)
			{
				connecting.add(p);
			}
		}
 		
 		if(connecting.size() == 0)
 		{
 			return null;
 		}
 		
 		Airplane departingPlane = connecting.get(rand.nextInt(connecting.size()));
		
		LocationAlias delivery   = departingPlane.planeLocation.location;
		LocationAlias deliveryBus = (departingPlane.gate.location == departingPlane.planeLocation.location 
									? null : departingPlane.gate.location);
		
		int id = generatorData.prm_id_dispenser++;
		final int weight = (int)(rand.nextDouble()*1.5 + 2);
		
		
		final int deadline = departingPlane.time;
		
		PRM prm = new PRM(id, "Transfer PRM "+id, release, deadline, pickup, delivery, weight,false);
		prm.booking_time = release - 120;
		
		this.generatorData.getBestRoute(pickup, pickupBus, deliveryBus, delivery, true, prm, true);
		prm.initialiseSegments();
		
		int min_time = prm.getMinTravelingTime();
		int time	 = deadline - release;
		
		if(min_time > time)
		{
			return null;
		}
		
		System.out.println("TANSFER PRM ["+release+","+deadline+"] duration: "+time+" min_time: "+min_time+" slack: "+(time - min_time)+" "+arrivingPlane);
		
		prm.arrivingPlane = true;
		prm.departingPlane = true;
		
		return prm;
	}

	public PRM_Instance getInstance() 
	{
		return this.generatorData.instance;
	}

	public FlightPlan getFlightPlan() 
	{
		return this.plan;
	}
}
