package prm.problemdef;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
public class PRM_Instance_Reader 
{	
	static int debug = 0;
	
	public static PRM_Instance makeInstance(File locationsFile, File workersFile) throws IOException
	{
		System.out.println("Makeinstance Called!");
		
		debug++;
		
//		if(debug == 2)
//		{
//			throw new Error("Sigh!");
//		}
		Segment.id_dispenser = 0;
		PRM_Instance instance = new PRM_Instance();
		
		readLocatoins(locationsFile, instance);
		readOrders(workersFile, instance);
		
		instance.instanceLoadString = "1;"+locationsFile.getAbsolutePath()+";"+workersFile.getAbsolutePath();
		
		return instance;
	}
	
	public static void readLocatoins(	File locationsfile, PRM_Instance instance) throws IOException
	{
		instance.airsideBusses = new Area(instance,0,false);
		instance.terminalBusses = new Area(instance,-1,false);
		
		System.out.println(" Reading locations from: "+locationsfile.getAbsolutePath());
		LineParser reader = new LineParser(new FileReader(locationsfile));
		reader.readLine();
		
		// Get number of terminals
		final String terminalcountRaw = reader.lineReadAfter("Number of Terminals : ");
		final int terminalcount = Integer.parseInt(terminalcountRaw);
		
		reader.readLine();
		
		for(int i = 0 ; i < terminalcount ; i++)
		{
			// TODO: make use of the other data in the line;
			String[] data = reader.readLine().split(";");
			final int aid = Integer.parseInt(data[0].trim());
				
			new Area(instance,aid,true);
//			// System.out.println("Make terminal: |"+data[0].trim()+"|");
		}
		
		//Empty line
		reader.skipLines(1);
		//// System.out.println("Currentline: "+reader.readedLine());
		final int locationCount = Integer.parseInt(reader.lineReadAfter("Number of Locations : "));
		reader.readLine();
		
		// Get Locations
		for(int i = 0 ; i < locationCount ; i++)
		{
		
			final String line = reader.readLine();
			String[] data = line.split(";");
			final int lid = Integer.parseInt(data[0].trim());
			final int aid;
			final Area a;
			final boolean supervised = data.length >= 4 && data[3].trim().equals("Lounge");
			final LocationKind kind;
			
			final String s = (data.length >= 4 ? data[3].trim() : "");
			
			if(s.equals("Lounge"))
			{
				kind = LocationKind.Lounge;
			}
			else if(s.equals(""))
			{
				kind = LocationKind.Gate; // Assume its a gate.
			}
			else if(s.equals("Public"))
			{
				kind = LocationKind.Public;
			}
			else if(s.equals("AirBusGarage"))
			{
				kind = LocationKind.Airbus_Garage;
			}
			else if(s.equals("TermBusGarage"))
			{
				kind = LocationKind.Termbus_Garage;
			}
			else if(s.equals("TermBusstop"))
			{
				kind = LocationKind.TermBusstop;
			}
			else
			{
				throw new Error("Unkown kind: "+s);
			}
			
			
			//// System.out.println("Area: '"+data[1].trim()+"'");
			if(!data[1].equals(""))
			{
				aid = Integer.parseInt(data[1].trim());
				a = instance.getArea(aid);
			}
			else
			{ 
				String type = data[3].trim();
				if(type.equals("AirBusGarage"))
				{
					a = instance.airsideBusses;
				}
				else if(type.equals("TermBusGarage"))
				{
					a = instance.terminalBusses;
				}
				else
				{
					throw new Error("Unknown empty area id: type: '"+data[3]+"'");
				}
			}
			
			if(a == null)
			{
				throw new Error("null area in:|"+data[1].trim()+"|");
			}
	
			Location l = new Location(lid, supervised, a, kind);
			Location l_o = instance.locations.put(lid, l);
			
			if(l_o != null)
			{
				throw new Error(	"Inserting new location with already existing id: \n"+
									" "+l_o+"\n"+
									" "+l);
			}
		}
		
		// Distances:
		for(int j = 0 ; j < terminalcount ; j++)
		{
			reader.skipTillLineStartsWith("[");
			
			String terminal = reader.readedLine().substring(1,reader.readedLine().length()-1);
			
			Area area = instance.getArea(Integer.parseInt(terminal.trim()));
			
			reader.skipTillLineStartsWith("Number of Distances at Terminal : ");
			
			final int distanceCount = Integer.parseInt(reader.lineReadAfter("Number of Distances at Terminal : "));
			reader.readLine();
		
			//int count = 0; 
			
			String line = reader.readLine();
			
			//for(int i = 0 ; i < distanceCount ; i++)
			while(!line.equals(""))
			{
				String[] data = line.split(";");
				//final Location l1 = locations.get(data[0].trim());
				//final Location l2 = locations.get(data[1].trim());
				//// System.out.println(data[0]+";"+data[1]);
				
				if(data[1].trim().equals("419"))
				{
				// System.out.println("Adding (419) in ["+terminal+"]");
				}
				
				area.setDistance(	Integer.parseInt(data[0].trim()),
									Integer.parseInt(data[1].trim()), 
									Integer.parseInt(data[2].trim())	);
				
				line = reader.readLine();
			}
		}
		
		reader.skipTillLineStartsWith("Airside bus distances");
		reader.readLine();
		String line = reader.readLine();
		
		while(line != null && !line.equals(""))
		{
			String[] data = line.split(";");
			instance.airsideBusses.setDistance(	Integer.parseInt(data[0].trim()),
												Integer.parseInt(data[1].trim()),
												Integer.parseInt(data[2].trim())	);
			line = reader.readLine();
		}
		
		reader.skipTillLineStartsWith("Inter Terminal bus distances");
		reader.readLine();
		line = reader.readLine();
		
		while(line != null && !line.equals(""))
		{
			String[] data = line.split(";");
			instance.terminalBusses.setDistance( 	Integer.parseInt(data[0].trim()),
													Integer.parseInt(data[1].trim()),
													Integer.parseInt(data[2].trim())	);
			line = reader.readLine();
		}
		
		// Initialise distanceMatrixes
		for(Area a : instance.world.values())
		{
			a.initDistanceMatrix();
		}
	}
	
	public static void readOrders(	File workersFile, PRM_Instance instance) throws IOException
	{
		System.out.println(" Reading workers and prms from: "+workersFile.getAbsolutePath());
		
		final Area busSite = instance.terminalBusses;
		final Area airSite = instance.airsideBusses;
		
		LineParser reader = new LineParser(new FileReader(workersFile));
		reader.readLine();
		
		// Workers
		final int maxTerminals = instance.world.size()- 2;
		for(int i = 0 ; i < maxTerminals ; i ++)
		{
			reader.skipTillLineStartsWith("Terminal: [");
			final String currLine = reader.readedLine();
			//// System.out.println("Debug: "+currLine);
			final String terminalString = currLine.substring(11, currLine.length()-1);
			
			final int aid = Integer.parseInt(terminalString);
			
			reader.readLine();
			
			Area terminal = instance.getArea(aid);
			
			final int workers = Integer.parseInt(reader.lineReadAfter(" Agents at terminal: "));
			reader.readLine();
			
			//// System.out.println("Terminal: "+terminalString+" ("+workers+")");
			
			for(int w = 0 ; w < workers ; w++)
			{
				String[] data = reader.readLine().split(";");
			
				Transport t = new Transport(	data[0],
												Integer.parseInt(data[0]),
												Integer.parseInt(data[1]),
												terminal.getLocation(Integer.parseInt(data[4].trim())),
												Integer.parseInt(data[2]),
												Integer.parseInt(data[3]));
			}
			
			//// System.out.println("    Actual inserts: "+terminalString+" ("+terminal.transporters.size()+"/"+workers+") ");
		}
		
		// Read Terminal busses
		reader.skipTillLineStartsWith(" Inter Terminal Busses: ");
		final int bussesIT = Integer.parseInt(reader.lineReadAfter(" Inter Terminal Busses: "));
		reader.readLine();
		for(int w = 0 ; w < bussesIT ; w++)
		{
			String[] data = reader.readLine().split(";");
			
			Transport t = new Transport(	data[0],
											Integer.parseInt(data[0]),
											Integer.parseInt(data[1]),
											instance.terminalBusses.getLocation( Integer.parseInt(data[4].trim())),
											Integer.parseInt(data[2]),
											Integer.parseInt(data[3]));
		}
		
		// Read Airside busses
		reader.skipTillLineStartsWith(" Air side Busses: ");
		final int bussesAS = Integer.parseInt(reader.lineReadAfter(" Air side Busses: "));
		reader.readLine();
		for(int w = 0 ; w < bussesAS ; w++)
		{
			String[] data = reader.readLine().split(";");
			
			Transport t = new Transport(	data[0],
											Integer.parseInt(data[0]),
											Integer.parseInt(data[1]),
											instance.airsideBusses.getLocation( Integer.parseInt(data[4].trim())),
											Integer.parseInt(data[2]),
											Integer.parseInt(data[3]));
		}
		
		// Read busses:
		reader.skipTillLineStartsWith(" Number of passengers: ");
		final int passengerC = Integer.parseInt(reader.lineReadAfter(" Number of passengers: "));
		reader.readLine();
		
		int transfer = 0;
		
		for(int p = 0 ; p < passengerC ; p++)
		{
			// Passenger ID(0);type (1);volume (2); starttime(3);start loc(4); 
			// start term(5); endtime(6); end loc(7);end term(8);
			String[] data = reader.readLine().split(";");
			
			Area a1 = instance.getArea(Integer.parseInt(data[5].trim()));
			Area a2 = instance.getArea(Integer.parseInt(data[8].trim())); 
			
			final int id = Integer.parseInt(data[0].trim());
			
			PRM prm = new PRM(	id, data[0].trim(),
								Integer.parseInt(data[3]), Integer.parseInt(data[6]),
								a1.getLocation(Integer.parseInt(data[4].trim())), 
								a2.getLocation(Integer.parseInt(data[7].trim())),
								Integer.parseInt(data[2]),true								);
			
			instance.clients.put(id, prm);
			if(!data[5].equals(data[8]))
			{
				//// System.out.println("Transfer: ["+data[5]+"] to ["+data[8]+"]");
				transfer++;
			}
		}
		// System.out.println("Transfers: "+transfer+"/"+passengerC);
		
		// Parse Segments:
		
		int airBusRoutes = 0;
		int terminalBusRoutes = 0;
		
		int faulty_PRMS = 0;
		
		for(int i = 0 ; i < passengerC ; i++)
		{
			reader.skipTillLineStartsWith("Pasenger : ");
			final int pid = Integer.parseInt(reader.lineReadAfter("Pasenger : ").trim());
			
			PRM prm = instance.clients.get(pid);
			
			
			
			//reader.readLine();
			reader.skipTillLineStartsWith("Segments: ");
			final int segments = Integer.parseInt(reader.lineReadAfter("Segments: "));
			reader.readLine();
			prm.route = new Segment[segments];
		
			//System.out.println("Reading segmenst of passeger: "+prm.prm_ID +" segments:" + segments+"/"+prm.route.length);
			
			boolean faulty = false;
			
			for(int s = 0 ; s < segments ; s++)
			{
				// ID(0);Start location(1); End location(2); Terminal(3); Bus(4);Embarkment(5)
				final String[] data = reader.readLine().split(";");
				
				//// System.out.println("Readeline: "+reader.readedLine());
				
				final boolean embark = (data.length >= 6 ? data[5].trim().equals("1") : false);
				//// System.out.println("Embark: "+embark+" Data? "+(data.length >=6 ? data[5] : "<empty>"));
				final int ori_id = Integer.parseInt(data[0].trim());
				
				// TODO: This is messy code, clean it up. 
				if(data.length < 5 || data[4].equals(""))
				{
					
					final int terminalId = Integer.parseInt( data[3].trim() );
					final int start_lid  = Integer.parseInt( data[1].trim() );
					final int end_lid	 = Integer.parseInt( data[2].trim() );
					
					final Area terminal			 = instance.getArea(terminalId);
					final LocationAlias pickup 	 = terminal.getLocation(start_lid);
					final LocationAlias delivery = terminal.getLocation(end_lid);
					
					if(pickup == null)
					{
						faulty_PRMS++;
						faulty = true;
						prm.route = new Segment[0];
						
						 System.out.println("Warning unknown pickup id: "+data[1]+" in: "+reader.readedLine());
						 System.out.println(" start: "+instance.getLoction(start_lid));
						 System.out.println(" end:   "+instance.getLoction(end_lid));
						 
						break;
//						throw new Error("Warning unknown pickup id: "+data[1]+" in: "+reader.readedLine());
					}
					else if(delivery == null)
					{
						faulty_PRMS++;
						faulty = true;
						prm.route = new Segment[0];
						
						 System.out.println("Warning unknown delivery id: "+data[2]+" in: "+reader.readedLine());
						 System.out.println(" start: "+instance.getLoction(start_lid));
						 System.out.println(" end:   "+instance.getLoction(end_lid));
						break;
//						throw new Error("Warning unknown delivery id: "+data[2]+" in: "+reader.readedLine());
					}
					
					prm.route[s] = new Segment(ori_id,prm,pickup, delivery, terminal,embark);
					
					if(data.length >= 6 && data[5].equals("1"))
					{
						prm.departingPlane = true;
					}
				}
				else
				{
					final String bus = data[4].trim();
					if(bus.equals("terminalbus"))
					{
						final LocationAlias pickup   = busSite.getLocation(Integer.parseInt(data[1].trim()));
						final LocationAlias delivery = busSite.getLocation(Integer.parseInt(data[2].trim()));
						
						prm.route[s] = new Segment(ori_id,prm,pickup, delivery, busSite, embark);
						
						if(pickup == null)
						{
							faulty_PRMS++;
							faulty = true;
							prm.route = new Segment[0];
							
							 System.out.println("Warning unknown pickup id: "+data[1]+" in: "+reader.readedLine());
							break;
//							throw new Error("Warning unknown pickup id: "+data[1]+" in: "+reader.readedLine());
						}
						else if(delivery == null)
						{
							faulty_PRMS++;
							faulty = true;
							prm.route = new Segment[0];
							
							System.out.println("Warning unknown delivery id: "+data[2]+" in: "+reader.readedLine());
							break;
//							throw new Error("Warning unknown delivery id: "+data[2]+" in: "+reader.readedLine());
						}
						
						if(data.length >= 6 && data[5].equals("1"))
						{
							prm.departingPlane = true;
						}
						
						terminalBusRoutes++;
					}
					else if(bus.equals("airsidebus"))
					{
						final LocationAlias pickup = airSite.getLocation(Integer.parseInt(data[1].trim()));
						final LocationAlias delivery = airSite.getLocation(Integer.parseInt(data[2].trim()));
						
						if(pickup == null)
						{
							faulty_PRMS++;
							faulty = true;
							prm.route = new Segment[0];
							
							 System.out.println("Warning unknown pickup id: "+data[1]+" in: "+reader.readedLine());
							break;
//							throw new Error("Warning unknown pickup id: "+data[1]+" in: "+reader.readedLine());
						}
						else if(delivery == null)
						{
							faulty_PRMS++;
							faulty = true;
							prm.route = new Segment[0];
							
							 System.out.println("Warning unknown delivery id: "+data[2]+" in: "+reader.readedLine());
							break;
							//throw new Error("Warning unknown delivery id: "+data[2]+" in: "+reader.readedLine());
						}
						
						prm.route[s] = new Segment(ori_id,prm,pickup, delivery, airSite, embark);
						
						if(data.length >= 6 && data[5].equals("1"))
						{
							prm.departingPlane = true;
						}
						
						airBusRoutes++;
					}
				}
			}
			
			if(faulty)
			{
				// Remove from list
				instance.clients.remove(prm.prm_ID);
				instance.faultyClients.add(prm);
			}
			else
			{
				// Initialise segments prm:
				prm.initialiseSegments();
			}
			
		}
		
		// System.out.println("airBusses: "+airBusRoutes);
		// System.out.println("termBusses: "+terminalBusRoutes);
		 System.out.println("faulty PRM's: "+faulty_PRMS);
		 System.out.println("Total PRM's: "+passengerC);
		
//		// All locations
//		for(Location l : instance.locations.values())
//		{
//			 System.out.println(l);
//		}
	}

	public static void readWorkersOnly(File workersFile, PRM_Instance instance) throws IOException 
	{
		final Area busSite = instance.terminalBusses;
		final Area airSite = instance.airsideBusses;
		
		LineParser reader = new LineParser(new FileReader(workersFile));
		reader.readLine();
		
		// Workers
		final int maxTerminals = instance.world.size()- 2;
		for(int i = 0 ; i < maxTerminals ; i ++)
		{
			reader.skipTillLineStartsWith("Terminal: [");
			final String currLine = reader.readedLine();
			//// System.out.println("Debug: "+currLine);
			final String terminalString = currLine.substring(11, currLine.length()-1);
			
			final int aid = Integer.parseInt(terminalString);
			
			reader.readLine();
			
			Area terminal = instance.getArea(aid);
			
			final int workers = Integer.parseInt(reader.lineReadAfter(" Agents at terminal: "));
			reader.readLine();
			
			//// System.out.println("Terminal: "+terminalString+" ("+workers+")");
			
			for(int w = 0 ; w < workers ; w++)
			{
				String[] data = reader.readLine().split(";");
			
				Transport t = new Transport(	data[0],
												Integer.parseInt(data[0]),
												Integer.parseInt(data[1]),
												terminal.getLocation(Integer.parseInt(data[4].trim())),
												Integer.parseInt(data[2]),
												Integer.parseInt(data[3]));
			}
			
			//// System.out.println("    Actual inserts: "+terminalString+" ("+terminal.transporters.size()+"/"+workers+") ");
		}
		
		// Read Terminal busses
		reader.skipTillLineStartsWith(" Inter Terminal Busses: ");
		final int bussesIT = Integer.parseInt(reader.lineReadAfter(" Inter Terminal Busses: "));
		reader.readLine();
		for(int w = 0 ; w < bussesIT ; w++)
		{
			String[] data = reader.readLine().split(";");
			
			Transport t = new Transport(	data[0],
											Integer.parseInt(data[0]),
											Integer.parseInt(data[1]),
											instance.terminalBusses.getLocation( Integer.parseInt(data[4].trim())),
											Integer.parseInt(data[2]),
											Integer.parseInt(data[3]));
			instance.terminalBusses.add(t);
		}
		
		// Read Airside busses
		reader.skipTillLineStartsWith(" Air side Busses: ");
		final int bussesAS = Integer.parseInt(reader.lineReadAfter(" Air side Busses: "));
		reader.readLine();
		for(int w = 0 ; w < bussesAS ; w++)
		{
			String[] data = reader.readLine().split(";");
			
			Transport t = new Transport(	data[0],
											Integer.parseInt(data[0]),
											Integer.parseInt(data[1]),
											instance.airsideBusses.getLocation( Integer.parseInt(data[4].trim())),
											Integer.parseInt(data[2]),
											Integer.parseInt(data[3]));
			instance.airsideBusses.add(t);
		}	
	}
}
