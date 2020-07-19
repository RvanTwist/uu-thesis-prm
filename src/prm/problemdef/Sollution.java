package prm.problemdef;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

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
public class Sollution 
{
	public PRM_Instance instance;
	TreeMap<Segment,ScheduledSegment> scheduledSegments = new TreeMap<Segment,ScheduledSegment>();
	public String solverSettings;
	
	public Sollution(PRM_Instance instance, File sollutionFile)
	{
		this(instance);
		
		try 
		{
			loadFile(sollutionFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Sollution(File sollutionFile)
	{	
		try 
		{
			loadFile(sollutionFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initSolution()
	{
		for(PRM prm : instance.clients.values())
		{
			for(Segment s : prm.route)
			{
				this.scheduledSegments.put(s, new ScheduledSegment(s));
			}
		}
	}
	
	private void loadFile(File sollutionFile) throws IOException
	{
		LineParser parser = new LineParser(sollutionFile);
		parser.readLine();
		parser.skipTillLineStartsWith("Instance:");
		String[] instanceData = parser.readLine().split(";");
		
//		if(instanceData.length <= 1)
//		{
//			// Nothing to load;
//		}
//		else if(instanceData[0].equals("1"))
//		{
//			PRM_Instance_Reader.makeInstance(new File(instanceData[1]), new File(instanceData[2]));
//		}
		
		
		
		
		parser.skipTillLineStartsWith("Schedule: ");
		int entries = Integer.parseInt(parser.lineReadAfter("Schedule: "));
		parser.readLine();
		
		
		for(int i = 0 ; i < entries ; i++)
		{	
			final String lineString = parser.readLine();
			if(lineString == null || lineString.equals(""))
			{
				System.out.println("Warning linestring is null at "+i+"/"+entries);
				throw new Error("Warning linestring is null at "+i+"/"+entries);
			}
			
			final String[] data = lineString.trim().split(";");
			
			final int segmentID = Integer.parseInt(data[0].trim());
			final int start 	= Integer.parseInt(data[1].trim());
			final int end	 	= Integer.parseInt(data[2].trim());
			ScheduledSegment scheduled = null;
			for(ScheduledSegment seg : this.scheduledSegments.values())
			{
				if(seg.segment.segmentId == segmentID)
				{
					scheduled = seg;
					break;
				}
			}
			
			if(scheduled == null)
				throw new Error("Segment with id: "+segmentID+" don't exists!");
			
			scheduled.pickup_time = start;
			scheduled.delivery_time = end;
			
			if(data[3].equals("-"))
			{
				scheduled.transport = null;
			}
			else
			{
				final int transportID = Integer.parseInt(data[3]);
				Transport transport = null;
				for(Transport t : scheduled.segment.supervisingArea.transporters)
				{
					if(t.transport_ID == transportID)
					{
						transport = t;
						break;
					}
				}
				
				if(transport == null)
				{ // Maybe it has generated shifts:
					instance.generateShifts();
					for(Transport t : scheduled.segment.supervisingArea.transporters)
					{
						if(t.transport_ID == transportID)
						{
							transport = t;
							break;
						}
					}
					
					if(transport == null)
						throw new Error("Transport with id: "+transportID+" isn't found!");
				}
				
				scheduled.transport = transport;
				
			}
		}
		
	}
	
	public Sollution(PRM_Instance instance)
	{
		this.instance = instance;
		this.initSolution();
	}
	
	public ScheduledSegment getScheduling(Segment seg)
	{
		ScheduledSegment s = this.scheduledSegments.get(seg);
		
		if(s == null)
		{
			s = new ScheduledSegment(seg);
			this.scheduledSegments.put(seg, s);
		}
		
		return s;
	}
	
	public Iterable<ScheduledSegment> getSegments()
	{
		return scheduledSegments.values();
	}
	
	public static DateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
	public void save(File f) throws IOException
	{
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		
		w.write("Sollution file: "+formatter.format(new Date(System.currentTimeMillis()))+"\n");
		
		w.write("Instance:\n");
		w.write(instance.getInstanceLoadString()+"\n");
		
		w.write("\nSolver Settings:\n");
		w.write(solverSettings+"\n");
		
		w.write("\nSchedule: "+this.scheduledSegments.size()+"\n"+
				"<segment id>;<pickup time>;<delivery time>;<transport>\n");
		
		for(ScheduledSegment s : this.scheduledSegments.values())
		{
			w.write("\t"+s.saveString()+"\n");
		}
		
		w.flush();
	}
	
	public static Sollution load(PRM_Instance instance,File file) 
	{
		return new Sollution(instance, file);
	}
	
	public static Sollution load(File file) throws IOException
	{
		LineParser lp = new LineParser(file);
		
		lp.readLine();
		lp.skipTillLineStartsWith("Instance:");
		String[] data = lp.readLine().split(";");
		
		File locationsFile;
		File workersFile;
		
		if(data.length == 3)
		{
			locationsFile = new File(data[1]);
			workersFile = new File(data[2]);
		}
		else
		{
			throw new IOException("Can't read instance data!\n"+lp.readedLine());
		}
		
		lp.close();
		
		PRM_Instance instance = PRM_Instance_Reader.makeInstance(locationsFile, workersFile);
		instance.addLoungeVisits();
		instance.checkRoutes();
		instance.fixMatrixReflext();
		return load(instance, file);
		
	}
}
