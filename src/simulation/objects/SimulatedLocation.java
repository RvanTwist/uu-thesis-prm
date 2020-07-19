package simulation.objects;

import java.util.LinkedList;
import java.util.TreeSet;

import prm.problemdef.Area;
import prm.problemdef.Location;
import prm.problemdef.LocationAlias;
import simulation.DynamicWorldSimulation;
import simulation.Event;
import simulation.SimulatedPositionalbe;
import simulation.events.Event_UpdateLocationTasks;
import simulation.ui.InfoPrintable;

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
public class SimulatedLocation implements InfoPrintable
{
	public Location loc;
	DynamicWorldSimulation simulation;
	
	
	public TreeSet<SimulatedPRM> prms = new TreeSet<SimulatedPRM>();
	public TreeSet<SimulatedWorker> workers = new TreeSet<SimulatedWorker>();
	public TreeSet<SimulatedPlane> planes = new TreeSet<SimulatedPlane>();
	
	boolean updatePlanned = false;
	
	public SimulatedLocation(DynamicWorldSimulation sim, Location l)
	{
		this.simulation = sim;
		this.loc = l;
	}
	
	public void add(SimulatedPositionalbe pos)
	{
		if(pos instanceof SimulatedWorker)
		{
			workers.add((SimulatedWorker)pos);
		}
		else if(pos instanceof SimulatedPlane)
		{
			planes.add((SimulatedPlane)pos);
		}
		else if(pos instanceof SimulatedPRM)
		{
			prms.add((SimulatedPRM)pos);
		}
		
		
		this.planUpdateLocationTasks();
	}
	
	public boolean hasPRM(SimulatedPRM prm)
	{
		return this.prms.contains(prm);
	}

	public int getDistanceTo(SimulatedLocation loc2,Area area) 
	{
		return this.loc.getDistanceTo(loc2.loc,area);
	}
	
	public int getDistanceTo(SimulatedLocation loc2) 
	{
		if(true)
			throw new Error("Errornious method!");
		
		return loc.getDistanceTo(loc2.loc);
	}

	public void remove(SimulatedPositionalbe pos) 
	{
		if(pos instanceof SimulatedWorker)
		{
			workers.remove((SimulatedWorker)pos);
		}
		else if(pos instanceof SimulatedPlane)
		{
			planes.remove((SimulatedPlane)pos);
		}
		else if(pos instanceof SimulatedPRM)
		{
			prms.remove((SimulatedPRM)pos);
		}
		
	}

	public Iterable<SimulatedWorker> workers() 
	{
		return this.workers;
	}

	public boolean isSupervised() 
	{
		return this.loc.isSupervised();
	}

	public void planUpdateLocationTasks()
	{
		if(!this.updatePlanned)
		{
			this.updatePlanned = true;
			final Event e = new Event_UpdateLocationTasks(this.simulation.getTime(),this);
			this.simulation.addEvent(e);
		}
	}
	
	public void updateLocationTasks() 
	{
		updatePlanned = false;
		
		for(SimulatedWorker w : this.workers)
		{
			w.advancePlanning();
		}
	}
	
	public String toString()
	{
		return "Location "+this.loc.simpleString()+" "+this.loc.getKind();
	}

	@Override
	public String getInfo() 
	{
		String prms = "";
		for(SimulatedPRM prm : this.prms)
		{
			prms += " "+prm.toString()+"\n";
		}
		
		String workers = "";
		for(SimulatedWorker worker : this.workers)
		{
			workers += " "+worker.toString()+"\n";
		}
		
		String aliases = "";
		for(LocationAlias la : this.loc.getAliases())
		{
			aliases += " "+la+"\n";
		}
		
		return 	this+"\n"+
				"\nPRMS:\n"+prms+
				"\nWorkers:\n"+workers+
				"\nAliases:\n"+aliases;
	}

	public boolean hasAlias(LocationAlias la) 
	{
		return la.isAliasOf(this.loc);
	}
}
