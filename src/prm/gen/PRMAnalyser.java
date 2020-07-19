package prm.gen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import prm.problemdef.LocationAlias;
import prm.problemdef.LocationKind;
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
public class PRMAnalyser 
{
	
	// Locations
	TreeSet<LocationAlias> pickup_locations = new TreeSet<>();
	TreeSet<LocationAlias> pickup_airplane = new TreeSet<>();;
	
	TreeSet<LocationAlias> delivery_locatoins = new TreeSet<>();;
	TreeSet<LocationAlias> delivery_airport = new TreeSet<>();;
	
	TreeMap<LocationAlias,TreeSet<LocationAlias>> bus_terminal_airplane = new TreeMap<>();
	TreeMap<LocationAlias,TreeSet<LocationAlias>> bus_airplane_terminal = new TreeMap<>();
	
	// planes
	
	public static void main(String[] args) throws IOException
	{
		File f = new File("console_output.txt");
		
		PrintStream out = System.out;
		System.setOut(new PrintStream(f));
		
		
		PRMAnalyser analyser = new PRMAnalyser();
		System.out.println("Start analyser:");
		analyser.analyse();
		
		analyser.analyse_Planes();
		
		try
		{
			BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("Generator_Seed2.txt")));
			analyser.writeResults(bwr);
			bwr.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		System.setOut(out);
		System.out.println("done!");
	}
	
	public void analyse_Planes()
	{
		PRM_Instance instance;
		
		System.out.println("start Plane analysis");
		int prms = 0;
		
		FlightPlan[] plane_schedules = new FlightPlan[12];
		
		FlightPlan combinedFP = null;
		
		try
		{
			for(int i = 1 ; i <= 11 ; i++)
			{
				File locationsFile = new File("data2/Locations.txt");
				File workersFile = new File("data2/Workers"+i+".txt");
				
				instance = PRM_Instance_Reader.makeInstance(locationsFile, workersFile);
				instance.checkRoutes();
				
				if(combinedFP == null) {
					combinedFP = new FlightPlan(instance);
				}
				
				FlightPlan fp = new FlightPlan(instance);
				plane_schedules[i] = fp;
				
				System.out.println("Reviewing file: "+workersFile.getPath()+" ("+instance.clients.size()+")");
				prms += instance.clients.size();
				
				for(PRM prm : instance.clients.values())
				{
					if(prm.route.length > 0)
					{
						final LocationAlias arrival_gate;
						final LocationAlias arrival_planeLocation;
						
						final LocationAlias departure_gate;
						final LocationAlias departure_planeLocation;
						
						final int arrival_time = prm.arrival;
						final int departure_time = prm.deadline;
						
						Segment start = prm.route[0];
						Segment end   = prm.route[prm.route.length-1];
						
						// Pickup
						if(start.supervisingArea == instance.airsideBusses)
						{
							arrival_planeLocation = start.from;
							arrival_gate = prm.route[1].from;
						}
						else if(start.from.getKind() == LocationKind.Gate)
						{
							arrival_planeLocation 	= start.from;
							arrival_gate			= arrival_planeLocation;
						}
						else
						{
							arrival_planeLocation 	= null;
							arrival_gate			= null;
						}
						
						// Delivery
						if(end.embarkment || end.supervisingArea == instance.airsideBusses)
						{
							if(end.supervisingArea == instance.airsideBusses)
							{
								departure_gate = prm.route[prm.route.length - 2].to;
								departure_planeLocation = end.to;
							}
							else
							{
								departure_gate 			= end.to;
								departure_planeLocation = end.to;
							}
						}
						else
						{
							departure_gate 			= null;
							departure_planeLocation = null;
						}
						
						if(arrival_gate != null)
						{
							Airplane p = fp.getArrivingPlaneOrCreate(arrival_time, arrival_gate, arrival_planeLocation);
							p.occurance++;
							Airplane p2 = combinedFP.getArrivingPlaneOrCreate(arrival_time, arrival_gate, arrival_planeLocation);
							p2.occurance++;
							p2.instance_occurance[i]++;
						}
						
						if(departure_gate != null)
						{
							Airplane p = fp.getDepartingPlaneOrCreate(departure_time, departure_gate, departure_planeLocation);
							p.occurance++;
							
							Airplane p2 = combinedFP.getDepartingPlaneOrCreate(departure_time, departure_gate, departure_planeLocation);
							p2.occurance++;
							p2.instance_occurance[i]++;
						}
					}
				}
				
				// Report planes:
				System.out.println("Arriving airplanes: ("+fp.arrivingPlanes.size()+")");
				for(Airplane p : fp.arrivingPlanes)
				{
					Gate 	 g = p.gate;
					Terminal t = g.terminal;
					
					int hours = p.time / 60;
					int minutes = p.time % 60;
					
					String timeString = (hours < 10 ? "0"+hours : hours)+":"+(minutes < 10 ? "0"+minutes : minutes);
					
					System.out.println(" > "+timeString+" Terminal "+t.area.id+" Gate "+g.location.getLocationId()+" Plane: "+p.planeLocation.location.getLocationId()+ " ("+p.occurance+")");
				}
				
				System.out.println("Departing airplanes: ("+fp.departingPlanes.size()+")");
				for(Airplane p : fp.departingPlanes)
				{
					Gate 	 g = p.gate;
					Terminal t = g.terminal;
					
					int hours = p.time / 60;
					int minutes = p.time % 60;
					
					String timeString = (hours < 10 ? "0"+hours : hours)+":"+(minutes < 10 ? "0"+minutes : minutes);
					
					System.out.println(" > "+timeString+" Terminal "+t.area.id+" Gate "+g.location.getLocationId()+" Plane: "+p.planeLocation.location.getLocationId()+ " ("+p.occurance+")");
				}
				
			}
			System.out.println(" Sampled out of "+prms+" prms");
			
			System.out.println(" Combined Flight Plan Analysis");
			
			System.out.println("Arriving airplanes: ("+combinedFP.arrivingPlanes.size()+")");
			for(Airplane p : combinedFP.arrivingPlanes)
			{
				Gate 	 g = p.gate;
				Terminal t = g.terminal;
				
				int hours = p.time / 60;
				int minutes = p.time % 60;
				
				String timeString = (hours < 10 ? "0"+hours : hours)+":"+(minutes < 10 ? "0"+minutes : minutes);
				
				int max_occ = 0;
				for(int i = 0 ; i < 12 ; i++) {
					max_occ = Math.max(max_occ, p.instance_occurance[i]);
				}
				
				System.out.println(" > "+timeString+" Terminal "+t.area.id+" Gate "+g.location.getLocationId()+" Plane: "+p.planeLocation.location.getLocationId()+ " ("+p.occurance+"|"+max_occ+")");
			}
			
			System.out.println("Departing airplanes: ("+combinedFP.departingPlanes.size()+")");
			for(Airplane p : combinedFP.departingPlanes)
			{
				Gate 	 g = p.gate;
				Terminal t = g.terminal;
				
				int hours = p.time / 60;
				int minutes = p.time % 60;
				
				int max_occ = 0;
				for(int i = 0 ; i < 12 ; i++) {
					max_occ = Math.max(max_occ, p.instance_occurance[i]);
				}
				
				String timeString = (hours < 10 ? "0"+hours : hours)+":"+(minutes < 10 ? "0"+minutes : minutes);
				
				System.out.println(" > "+timeString+" Terminal "+t.area.id+" Gate "+g.location.getLocationId()+" Plane: "+p.planeLocation.location.getLocationId()+ " ("+p.occurance+"|"+max_occ+")");
			}
			
			
			writePlanePlanGenerator("allFlightPlans.txt",combinedFP);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
			instance = null;
		}
	}
	
	public PRMAnalyser()
	{	
	}
	
	public void analyse()
	{
		
		PRM_Instance instance;
		
		System.out.println("start Test");
		int prms = 0;
		int transferPRMs = 0;
		
		try
		{
			for(int i = 1 ; i <= 11 ; i++)
			{
				File locationsFile = new File("data2/Locations.txt");
				File workersFile = new File("data2/Workers"+i+".txt");
				
				instance = PRM_Instance_Reader.makeInstance(locationsFile, workersFile);
				
				System.out.println("Reviewing file: "+workersFile.getPath()+" ("+instance.clients.size()+")");
				prms += instance.clients.size();
				for(PRM prm : instance.clients.values())
				{
					if(prm.arrivingPlane && prm.departingPlane) {
						transferPRMs++;
					}
					
					if(prm.route.length > 0)
					{
						Segment start = prm.route[0];
						Segment end   = prm.route[prm.route.length-1];
						
						// Pickup
						if(start.supervisingArea == instance.airsideBusses)
						{
							TreeSet<LocationAlias> bucket = this.bus_airplane_terminal.get(start.from);
							
							if(bucket == null)
							{
								bucket = new TreeSet<>();
								this.bus_airplane_terminal.put(start.from, bucket);
							}
							
							bucket.add(start.to);
							this.pickup_airplane.add(start.from);
						}
						else
						{
							pickup_locations.add(start.from);
						}
						
						// Delivery
						if(end.embarkment || end.supervisingArea == instance.airsideBusses)
						{
							if(end.supervisingArea == instance.airsideBusses)
							{
								TreeSet<LocationAlias> bucket = this.bus_terminal_airplane.get(end.to);
								
								if(bucket == null)
								{
									bucket = new TreeSet<>();
									this.bus_terminal_airplane.put(end.to, bucket);
								}
								
								bucket.add(end.from);
							}
							
							this.delivery_airport.add(end.to);
						}
						else
						{
							this.delivery_locatoins.add(end.to);
						}
					}
				}
				
			}
			
			analysePickups();
			printResults();
			System.out.println(" Sampled out of "+prms+" prms");
			System.out.println(" Tansfer prms: "+transferPRMs + " out of "+prms);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
			instance = null;
		}
		
		// Analyse the contents.
	}
	
	public void analysePickups()
	{
		System.out.println("Analyse Pickups");
		
		Iterator<LocationAlias> it = this.pickup_locations.iterator();
		
		while(it.hasNext())
		{
			LocationAlias loc = it.next();
			
			if(this.delivery_airport.contains(loc))
			{
				// Clearly just left out of an airplane;
				it.remove();
				this.pickup_airplane.add(loc);
			}
		}
		System.out.println("done Analyse Pickups");
	}
	
	public void writePlanePlanGenerator(String file, FlightPlan plan) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
		
		bw.write("Generator FlightPlanData\n");
		bw.write("Arrival "+plan.arrivingPlanes.size()+"\n");
		
		for(Airplane p : plan.arrivingPlanes)
		{
			bw.write(p.saveString());
		}
		
		bw.write("\nDeparture "+plan.departingPlanes.size()+"\n");
		
		for(Airplane p : plan.departingPlanes)
		{
			bw.write(p.saveString());
		}
		
		bw.close();
	}
	
	public void writeResults(BufferedWriter wr) throws IOException
	{
		wr.write("Generator Seed");
		wr.write("\nTerminal Pickup Locations "+this.pickup_locations.size());
		for(LocationAlias a : this.pickup_locations)
		{
			wr.write("\n"+a.saveString());
		}
		wr.write("\nAirplane Pickup Locations "+this.pickup_airplane.size());
		for(LocationAlias a : this.pickup_airplane)
		{
			wr.write("\n"+a.saveString());
		}
		
		wr.write("\nTerminal Delivery Locations "+this.delivery_locatoins.size());
		for(LocationAlias a : this.delivery_locatoins)
		{
			wr.write("\n"+a.saveString());
		}
		
		wr.write("\nAirplane Delivery Locations "+this.delivery_airport.size());
		for(LocationAlias a : this.delivery_airport)
		{
			wr.write("\n"+a.saveString());
		}
		
		wr.write("\nTerminal => Airplane (inverse map) "+this.bus_terminal_airplane.size());
		for(Entry<LocationAlias, TreeSet<LocationAlias>> e : this.bus_terminal_airplane.entrySet())
		{
			TreeSet<LocationAlias> as = e.getValue();
			wr.write("\n"+as.size());
			for(LocationAlias l : as)
			{
				wr.write("\n"+e.getKey().saveString()+"|"+l.saveString());
			}
		}
		
		wr.write("\nAirplane => Terminal "+this.bus_airplane_terminal.size());
		for(Entry<LocationAlias, TreeSet<LocationAlias>> e : this.bus_airplane_terminal.entrySet())
		{
			TreeSet<LocationAlias> as = e.getValue();
			wr.write("\n"+as.size());
			for(LocationAlias l : as)
			{
				wr.write("\n"+e.getKey().saveString()+"|"+l.saveString());
			}
		}
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
		for(Entry<LocationAlias, TreeSet<LocationAlias>> e : this.bus_terminal_airplane.entrySet())
		{
			TreeSet<LocationAlias> as = e.getValue();
			
			for(LocationAlias l : as)
			{
				System.out.println("destination: "+e.getKey()+" : "+l+" => "+e.getKey());
			}
		}
		
		System.out.println("==== Airplane => Terminal ====");
		for(Entry<LocationAlias, TreeSet<LocationAlias>> e : this.bus_airplane_terminal.entrySet())
		{
			TreeSet<LocationAlias> as = e.getValue();
			
			for(LocationAlias l : as)
			{
				System.out.println("from: "+e.getKey()+" : "+e.getKey()+" => "+l);
			}
		}
	}
}
