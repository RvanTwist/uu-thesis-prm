package prm.gen;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

import prm.problemdef.*;
import rpvt.util.Distributions;
import rpvt.util.LineParser;

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
public class PRMGenerator 
{
	ArrayList<LocationAlias> pickup_locations = new ArrayList<LocationAlias>();
	ArrayList<LocationAlias> pickup_airplane = new ArrayList<LocationAlias>();;
	
	ArrayList<LocationAlias> delivery_locatoins = new ArrayList<LocationAlias>();;
	ArrayList<LocationAlias> delivery_airport = new ArrayList<LocationAlias>();;
	
	TreeMap<LocationAlias,ArrayList<LocationAlias>> bus_terminal_airplane = new TreeMap<LocationAlias,ArrayList<LocationAlias>>();
	TreeMap<LocationAlias,ArrayList<LocationAlias>> bus_airplane_terminal = new TreeMap<LocationAlias,ArrayList<LocationAlias>>();
	
	TreeMap<Area,ArrayList<LocationAlias>> pickup_LocatoinsArea;
	TreeMap<Area,ArrayList<LocationAlias>> delivery_LocatoinsArea 	= new TreeMap< Area, ArrayList<LocationAlias> >();
	
	public final PRM_Instance instance;
	
	public int prm_id_dispenser = 0;
	public int segment_id_dispenser = 0;
	
	public final Random rand;
	
	PRMGenerator(PRM_Instance i, File seedfile, Random r) throws IOException
	{
		this.
		instance = i;
		this.readSeed(seedfile, instance);
		
		extract_LocationsArea();
		
		this.rand = r;
		//this.printResults();
	}
	
	PRMGenerator(File locationsfile, File seedfile, Random r) throws IOException
	{
		this.rand = r;
		instance = new PRM_Instance();
		PRM_Instance_Reader.readLocatoins(locationsfile, instance);
		this.readSeed(seedfile, instance);
		
		extract_LocationsArea();
		//this.printResults();
	}
	
	private void extract_LocationsArea()
	{
		this.pickup_LocatoinsArea 	= new TreeMap< Area, ArrayList<LocationAlias> >();
		this.delivery_LocatoinsArea	= new TreeMap< Area, ArrayList<LocationAlias> >();
		
		for(LocationAlias la : this.pickup_locations)
		{
			ArrayList<LocationAlias> las = this.pickup_LocatoinsArea.get(la.getArea());
			
			if(las == null)
			{
				las = new ArrayList<LocationAlias>();
				this.pickup_LocatoinsArea.put(la.getArea(), las);
			}
			las.add(la);
		}
		
		for(LocationAlias la : this.delivery_locatoins)
		{
			ArrayList<LocationAlias> las = this.delivery_LocatoinsArea.get(la.getArea());
			
			if(las == null)
			{
				las = new ArrayList<LocationAlias>();
				this.delivery_LocatoinsArea.put(la.getArea(), las);
			}
			las.add(la);
		}
	}
	
	public static <A> A getRandomFromList(List<A> list, Random rand)
	{
		return list.get((int)(rand.nextDouble()*list.size()));
	}
	
	private void readSeed(File seedfile, PRM_Instance instance) throws IOException
	{
		LineParser lp = new LineParser(new FileReader(seedfile));
		
		lp.readLine();
		if(!lp.lineStartsWith("Generator Seed"))
		{
			throw new Error("This file doesn't contain a Generator Seed!");
		}
		
		{	// TERMINAL PICKUP LOCATIONS
			lp.readLine();
			
			final int tpl_count = Integer.parseInt(lp.lineReadAfter("Terminal Pickup Locations "));
			for(int i = 0 ; i < tpl_count ; i++)
			{
				LocationAlias a = LocationAlias.readString(lp.readLine(), instance).getTerminalAlias();
				
				this.pickup_locations.add(a);
			}
		}
		
		{	// AIRPLANE PICKUP LOCATIONS
			lp.readLine();
			final int tpl_count = Integer.parseInt(lp.lineReadAfter("Airplane Pickup Locations "));
			for(int i = 0 ; i < tpl_count ; i++)
			{
				final LocationAlias a = LocationAlias.readString(lp.readLine(), instance);
				this.pickup_airplane.add(a);
			}
		}
		
		{	// TERMINAL DELIVERY LOCATIONS
			lp.readLine();
			final int tpl_count = Integer.parseInt(lp.lineReadAfter("Terminal Delivery Locations "));
			for(int i = 0 ; i < tpl_count ; i++)
			{
				final LocationAlias a = LocationAlias.readString(lp.readLine(), instance);
				this.delivery_locatoins.add(a);
			}
		}
		
		{	// AIRPLANE DELIVERY LOCATIONS
			lp.readLine();
			final int tpl_count = Integer.parseInt(lp.lineReadAfter("Airplane Delivery Locations "));
			for(int i = 0 ; i < tpl_count ; i++)
			{
				final LocationAlias a = LocationAlias.readString(lp.readLine(), instance);
				this.delivery_airport.add(a);
			}
		}
		
		{	// TERMINAL => AIRPLANE INVERSE
			lp.readLine();
			final int tak_count = Integer.parseInt(lp.lineReadAfter("Terminal => Airplane (inverse map) "));
			for(int i = 0 ; i < tak_count ; i++)
			{
				final int tav_count = Integer.parseInt(lp.readLine());
				ArrayList<LocationAlias> ls = null;
				for(int j = 0 ; j < tav_count ; j++)
				{
					String[] data = lp.readLine().split("\\|");
					
					//System.out.println("Splitsed '"+lp.readedLine()+"' in "+printArray(data));
					
					final LocationAlias value = LocationAlias.readString(data[1], instance);
					
					if(ls == null)
					{
						final LocationAlias key 	= LocationAlias.readString(data[0], instance);
						ls = new ArrayList<LocationAlias>();
						this.bus_terminal_airplane.put(key, ls);
					}
					
					ls.add(value);
				}
			}
		}
		
		{	// AIRPLANE => TERMINAL
			lp.readLine();
			final int tak_count = Integer.parseInt(lp.lineReadAfter("Airplane => Terminal "));
			for(int i = 0 ; i < tak_count ; i++)
			{
				final int tav_count = Integer.parseInt(lp.readLine());
				ArrayList<LocationAlias> ls = null;
				for(int j = 0 ; j < tav_count ; j++)
				{
					String[] data = lp.readLine().split("\\|");
					final LocationAlias value = LocationAlias.readString(data[1], instance);
					
					if(ls == null)
					{
						final LocationAlias key 	= LocationAlias.readString(data[0], instance);
						ls = new ArrayList<LocationAlias>();
						this.bus_airplane_terminal.put(key, ls);
					}
					
					ls.add(value);
				}
			}
		}
	}
	
	public static <T> String printArray(T[] data) 
	{
		
		if(data.length == 0)
		{
			return data.getClass().getSimpleName()+"{}";
		}
		else
		{
			String s = data.getClass().getSimpleName()+"{ ["+data[0]+"]";
			
			for(int i = 1 ; i < data.length ; i++)
			{
				s = s + ", ["+data[i]+"]";
			}
			return s+" }";
		}
	}

	public PRM generateTotalRandomPRM(int min_start, int max_end)
	{
		final TripType type;
		double rndType = rand.nextDouble();
		
		final LocationAlias pickup;
		final LocationAlias pickupBus;
		
		final LocationAlias deliveryBus;
		final LocationAlias delivery;
		
		final int weight = (int)(rand.nextDouble()*1.5 + 2);
		final boolean embarking;
		
		if(rndType < 0.5)
		{
			type = TripType.Transfer;
			embarking = true;
			
			pickup = getRandomFromList(this.pickup_airplane,rand);
			delivery = getRandomFromList(this.delivery_airport,rand);
			
			// Check or a pickup by bus is neccesairy;
			ArrayList<LocationAlias> busLocationsPickup = this.bus_airplane_terminal.get(pickup);
			pickupBus = (busLocationsPickup == null ? null : getRandomFromList(busLocationsPickup,rand));
			
			// check or a pickup by bus with departure is necesairy.
			ArrayList<LocationAlias> busLocationsDelivery = this.bus_terminal_airplane.get(delivery);
			deliveryBus = (busLocationsDelivery == null ? null : getRandomFromList(busLocationsDelivery,rand));
		}
		else if(rndType < 0.75)
		{
			type = TripType.Departure;
			embarking = true;
			
			pickup = getRandomFromList(this.pickup_locations,rand);
			delivery = getRandomFromList(this.delivery_airport,rand);
			
			// Check or a pickup by bus is neccesairy;
			pickupBus = null;
			
			// check or a pickup by bus with departure is necesairy.
			ArrayList<LocationAlias> busLocationsDelivery = this.bus_terminal_airplane.get(delivery);
			deliveryBus = (busLocationsDelivery == null ? null : getRandomFromList(busLocationsDelivery,rand));
		}
		else
		{
			type = TripType.Arrival;
			embarking = false;
			
			pickup = getRandomFromList(this.pickup_airplane,rand);
			delivery = getRandomFromList(this.delivery_airport,rand);
			
			// Check or a pickup by bus is neccesairy;
			ArrayList<LocationAlias> busLocationsPickup = this.bus_airplane_terminal.get(pickup);
			pickupBus = (busLocationsPickup == null ? null : getRandomFromList(busLocationsPickup,rand));
			
			deliveryBus = null;
		}
		
		
		PRM prm = new PRM(prm_id_dispenser, "Random PRM "+prm_id_dispenser, -1, -1, pickup, delivery, weight,true);
		prm_id_dispenser++;
		
		getBestRoute(pickup, pickupBus, deliveryBus, delivery, true, prm, embarking);
		
		// Traveling window.
		final int min_time = prm.getMinTravelingTime();
		final int time = min_time + (int)(Distributions.randomBeta(1, 5) * (5 * 60));
		final int start = min_start + (int)(rand.nextDouble() * (max_end - min_start - time));
		final int end 	= start + time;
		prm.arrival = start;
		prm.deadline = end;
		prm.initialiseSegments();
		System.out.println("PRM ["+start+","+end+"] duration: "+time+" min_time: "+min_time+" slack: "+(time - min_time));
		
		instance.clients.put(prm.prm_ID, prm);
		
		return prm;
	}
	
	public void getBestRoute(	LocationAlias pickup, 		LocationAlias pickupBus,
								LocationAlias deliveryBus, 	LocationAlias delivery,
							 	boolean lounge, PRM prm, boolean boarding) 
	{
//		System.out.println(	"Create new Route: pickup      : "+pickup		+"\n" +
//							"                  pickupBus   : "+pickupBus 	+"\n" +
//							"                  deliveryBus : "+deliveryBus 	+"\n" +
//							"                  delivery    : "+delivery 	+"\n" +
//							"                  lounge      : "+lounge 		+"\n" +
//							"                  prm         : "+prm 			+"\n" +
//							"                  boarding    : "+boarding 	+"\n" 
//		);
		
		
		ArrayList<Segment> segments = new ArrayList<Segment>();
		Area pickupArea   = (pickupBus == null ?  pickup.getArea() : pickupBus.getTerminalAlias().getArea() );
		Area deliveryArea = (deliveryBus == null ? delivery.getArea() : deliveryBus.getTerminalAlias().getArea() );
		
		LocationAlias currLocation = pickup;
		
		if(pickupBus != null)
		{
			final Area startArea = pickup.getArea();
			segments.add(new Segment(segment_id_dispenser++,prm, pickup, pickupBus.getAlias(startArea), startArea, false));
			
			currLocation = pickupBus.getAlias(pickupArea);
		}
		
		if(pickupArea != deliveryArea)
		{
			if(lounge)
			{ // Should visit a lounge next:
				if(!currLocation.isSupervised())
				{
					// TODO: Might want to select a lounge smarter.
					Area currentArea = currLocation.getArea();
					final List<LocationAlias> lounges = currentArea.getLounges();
					
					if(lounges.size() > 0)
					{
						final LocationAlias loungeLoc = getRandomFromList(lounges,rand);
						
						segments.add(new Segment(segment_id_dispenser++,prm, currLocation, loungeLoc, currLocation.getArea(), false));
						currLocation = loungeLoc;
					}
				}
			}
			
			// Determine bus pickup.
			ArrayList<LocationAlias> busStopsPickup = pickupArea.getTerminalBusStops();
			LocationAlias bestBusPickup = null;
			int busPickupDistance = Integer.MAX_VALUE;
			
			for(LocationAlias la : busStopsPickup)
			{
				final int dist = currLocation.getDistanceTo(la);
				if(dist < busPickupDistance)
				{
					bestBusPickup = la;
					busPickupDistance = dist;
				}
			}
			
			segments.add(new Segment(segment_id_dispenser++,prm, currLocation, bestBusPickup, currLocation.getArea(), false));
			currLocation = bestBusPickup.getAlias(instance.terminalBusses);
			
			// Determine bus delivery
			ArrayList<LocationAlias> busStopsDelivery = deliveryArea.getTerminalBusStops();
			LocationAlias bestBusDelivery = null;
			int busDeliveryDistance = Integer.MAX_VALUE;
			
			// TODO: Could lead to bad choice of bus stop if multiple bus stops. Fix that
			
			for(LocationAlias la : busStopsDelivery)
			{
				final LocationAlias la_a = la.getAlias(currLocation.getArea());
				
				final int dist = currLocation.getDistanceTo(la_a);
				
				if(dist < busDeliveryDistance)
				{
					bestBusDelivery = la;
					busDeliveryDistance = dist;
				}
			}
			
			// Bus segment
			segments.add(new Segment(segment_id_dispenser++,prm, currLocation, bestBusDelivery.getAlias(currLocation.getArea()), currLocation.getArea(), false));
			currLocation = bestBusDelivery.getAlias(deliveryArea);
		}
		
		if(lounge)
		{ // Should visit a lounge next:
			if(!currLocation.isSupervised())
			{
				// TODO: Might want to select a lounge smarter.
				Area currentArea = currLocation.getArea();
				final List<LocationAlias> lounges = currentArea.getLounges();
				
				if(lounges.size() > 0)
				{
					final LocationAlias loungeLoc = getRandomFromList(lounges,rand);
					
					segments.add(new Segment(segment_id_dispenser++,prm, currLocation, loungeLoc, currLocation.getArea(), false));
					currLocation = loungeLoc;
				}
			}
		}
		
		if(deliveryBus != null)
		{
			Area currArea = currLocation.getArea();
			final LocationAlias terminalLoc = deliveryBus.getAlias(currArea);
			
			// To the gate
			segments.add(new Segment(segment_id_dispenser++,prm, currLocation, terminalLoc, currLocation.getArea(), false));
			
			// Embarking
			if(boarding)
			{
				segments.add(new Segment(segment_id_dispenser++,prm, terminalLoc, terminalLoc, terminalLoc.getArea(), true));
			}
			// Busride:
			segments.add(new Segment(segment_id_dispenser++,prm, deliveryBus.getAlias(delivery.getArea()), delivery, delivery.getArea(), false));
		}
		else
		{
			segments.add(new Segment(segment_id_dispenser++,prm, currLocation, delivery, currLocation.getArea(), false));
			
			if(boarding)
			{
				segments.add(new Segment(segment_id_dispenser++,prm, delivery, delivery, currLocation.getArea(), true));
			}
		}
		
		// Translate the segments
		prm.setSegments(segments);
	}

	public void printResults()
	{
		System.out.println();
		System.out.println("==== Terminal Pickup Locations ("+this.pickup_locations.size()+") ====");
		for(LocationAlias a : this.pickup_locations)
		{
			System.out.println(" "+a.toString());
		}
		System.out.println("==== Airplane Pickup Locations ("+this.pickup_airplane.size()+")====");
		for(LocationAlias a : this.pickup_airplane)
		{
			System.out.println(" "+a.toString());
		}
		
		System.out.println("==== Terminal Delivery Locations ("+this.delivery_locatoins.size()+")====");
		for(LocationAlias a : this.delivery_locatoins)
		{
			System.out.println(" "+a.toString());
		}
		
		System.out.println("==== Airplane Delivery Locations ("+this.delivery_airport.size()+")====");
		for(LocationAlias a : this.delivery_airport)
		{
			System.out.println(" "+a.toString());
		}
		
		System.out.println("==== Terminal => Airplane ====");
		for(Entry<LocationAlias, ArrayList<LocationAlias>> e : this.bus_terminal_airplane.entrySet())
		{
			ArrayList<LocationAlias> as = e.getValue();
			
			for(LocationAlias l : as)
			{
				System.out.println("destination: "+e.getKey()+" : "+l+" => "+e.getKey());
			}
		}
		
		System.out.println("==== Airplane => Terminal ====");
		for(Entry<LocationAlias, ArrayList<LocationAlias>> e : this.bus_airplane_terminal.entrySet())
		{
			ArrayList<LocationAlias> as = e.getValue();
			
			for(LocationAlias l : as)
			{
				System.out.println("from: "+e.getKey()+" : "+e.getKey()+" => "+l);
			}
		}
	}
	
	public LocationAlias getRandomPickup(Area a)
	{
		ArrayList<LocationAlias> las = this.pickup_LocatoinsArea.get(a);
		return (las == null ? null : las.get((int)(rand.nextDouble() * las.size())));
	}
	
	public LocationAlias getRandomDelivery(Area a)
	{
		ArrayList<LocationAlias> las = this.delivery_LocatoinsArea.get(a);
		return (las == null ? null : las.get((int)(rand.nextDouble() * las.size())));
	}

	public void setIdDispensertoMax() 
	{
		this.prm_id_dispenser = -1;
		this.segment_id_dispenser = -1;
		
		for(PRM prm : this.instance.clients.values())
		{
			if(this.prm_id_dispenser < prm.prm_ID)
				prm_id_dispenser = prm.prm_ID;
			
			for(Segment seg : prm.route)
			{
				if(seg.original_segmentId > segment_id_dispenser)
				{
					segment_id_dispenser = seg.original_segmentId;
				}
			}
		}
		prm_id_dispenser++;
		segment_id_dispenser++;
	}
}
