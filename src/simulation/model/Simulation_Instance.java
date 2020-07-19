package simulation.model;

import java.util.ArrayList;
import java.util.TreeMap;

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
public class Simulation_Instance 
{	
	Sollution solution; 
	PRM_Instance instance;
	
	ArrayList<Terminal> terminals = new ArrayList<Terminal>();
	TreeMap<PRM,Simulated_PRM> prms = new TreeMap<PRM,Simulated_PRM>();
	ArrayList<Plane> planes = new ArrayList<Plane>();
	
	public Simulation_Instance(Sollution solution)
	{
		this.solution = solution;
		this.instance = solution.instance;
		
		this.analyseInstance();
	}
	
	public void loadSollution(Sollution s)
	{
		for(Simulated_PRM prm : this.prms.values())
		{
			prm.loadSollution(s);
		}
	}
	
	public void analyseInstance()
	{
		// Create Terminals:
		for(Area a : this.instance.getAreas())
		{
			if( a != instance.airsideBusses && a != instance.terminalBusses)
			{ // Then a is a terminal:
				terminals.add(new Terminal(a,this));
			}
		}
		
		for(PRM prm : instance.clients.values())
		{
			final Simulated_PRM simprm = new Simulated_PRM(prm);
			prms.put(prm,simprm);
			
			if(prm.arrivingPlane)
			{
				final LocationAlias arrivingLoc = prm.route[0].from;
				final LocationAlias gate = (arrivingLoc.getArea() == instance.airsideBusses ? prm.route[1].from
																							: arrivingLoc		);
				Plane p = this.makeArrivingPlane(prm.arrival, prm.route[0], arrivingLoc, gate, simprm);
				p.addArriving(simprm);
			}
			if(prm.departingPlane)
			{
				final LocationAlias departingLoc = prm.route[prm.route.length-1].to;
				final LocationAlias gate = (departingLoc.getArea() == instance.airsideBusses 
												? prm.route[prm.route.length-2].to
												: departingLoc								);
				Plane p = this.makeDepartPlane(prm.deadline, prm.route[prm.route.length-1], departingLoc, gate, simprm);
				p.addDeparting(simprm);
			}
		}
	}

	public Terminal getTerminal(Area a)
	{
		for(Terminal t : this.terminals)
		{
			if(t.area == a)
				return t;
		}
		
		return null;
	}
	
	private Plane makeDepartPlane(int depart, Segment segment, LocationAlias departingLoc, LocationAlias gate, Simulated_PRM prm) 
	{
		return this.getTerminal(gate.getArea()).getGate(gate).makeDepartPlane(depart, segment, departingLoc, prm);
		
	}

	private Plane makeArrivingPlane(int arrive, Segment segment, LocationAlias arrivingLoc, LocationAlias gate, Simulated_PRM prm) 
	{
		return this.getTerminal(gate.getArea()).getGate(gate).makeArrivePlane(arrive, segment, arrivingLoc, prm);
	}
}
