package simulation.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import prm.gen.Airplane;
import prm.gen.FlightPlan;
import prm.gen.Gate;
import prm.ls.model4.M4_PRM;
import prm.problemdef.LocationAlias;
import prm.problemdef.PRM;
import prm.problemdef.Segment;
import simulation.DynamicWorldSimulation;
import simulation.SimulatedPositionalbe;
import simulation.events.Event_Someone_Arrived;
import simulation.events.Event_UpdateLocationTasks;
import prm.gen.Terminal;
import simulation.worker.tasks.PickupAndDeliverTask;
import simulation.worker.tasks.Task;

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
public class SimulatedPRM extends SimulatedPositionalbe
{
	public PRM prm;

	double delayfact_min;
	double delayfact_max;
	
	final int loadRequirement;
	
	SimulatedWorker transport = null;
	
	public ArrayList<Task> relevantTasks = new ArrayList<Task>();
	
	SimulatedSegment[] segments;
	SimulatedSegment   currentSegment = null; 
	
	SimulatedPlane arrivingPlane;
	SimulatedPlane departingPlane;
	
	public double actual_Arrive = 0;;
	
	public SimulatedPRM(DynamicWorldSimulation sim, M4_PRM prm) 
	{
		this(sim,prm.prm);
		
		this.actual_Arrive = this.prm.arrival;
	}
	
	/**
	 * Be carefull calling this, only call it when really progress needs to be advanced.
	 */
	public void advanceProgress()
	{
		//journeyProgress++;
	}
	
	public SimulatedPRM(DynamicWorldSimulation sim, PRM prm) {
		super(sim, null);
		this.prm = prm;
		
		// Determine delayFactor
		if(this.prm.capRequirement == 4)
		{ // Wheelchair 
			delayfact_min = 0.9;
			delayfact_max = 1.1;
		}
		else
		{
			delayfact_min = 0.9; //+ 0.35 * sim.rand.nextDouble();
			delayfact_max = delayfact_min +  0.4; //+ ((1.5 - delayfact_min)*sim.rand.nextDouble());
		}
		
		this.loadRequirement = prm.capRequirement;
		this.targetLocation = sim.getSimLocation(prm.checkin);
		this.setExpectedArrival(prm.arrival);
		
		
		// Make segments
		this.segments = new SimulatedSegment[prm.route.length];
		
		for(int i = 0 ; i < this.segments.length ; i++)
		{
			final SimulatedSegment sim_seg = new SimulatedSegment(this,prm.route[i]);
			this.segments[i] = sim_seg;
			if(i > 0)
			{
				final SimulatedSegment sim_seg_prev = this.segments[i-1];
				
				sim_seg_prev.next = sim_seg;
				sim_seg.prev = sim_seg_prev;
			}
		}
		
		// Import planes
		FlightPlan plan = sim.world.getFlightPlan();
		Segment first = prm.route[0];
		Segment last  = prm.route[prm.route.length-1];
		Segment secondLast = prm.route[Math.max(0, prm.route.length-2)];
		if(prm.arrivingPlane)
		{
			Gate g;
			LocationAlias gateL;
			gateL = first.from;
			
			Terminal terminal = plan.getTerminal(gateL.getArea());
			
			if(terminal == null)
			{
				gateL = first.to.getTerminalAlias();
				terminal = plan.getTerminal(gateL.getArea());
			}
			g = terminal .getGate(gateL);
			Airplane p = g.getArrivingPlane(prm.arrival);
			
			this.arrivingPlane = sim.getSimPlane(p);
			
			
			Gate g2 = null;
			
			if(arrivingPlane == null)
			{	
				gateL = first.to.getTerminalAlias();
				g2 = plan.getTerminal(gateL.getArea()).getGate(gateL);
				p = g2.getArrivingPlane(prm.arrival);
				this.arrivingPlane = sim.getSimPlane(p);
			}
			//System.out.println("Found airplane: "+arrivingPlane+" => "+p);
			
			if(arrivingPlane == null)
			{
				System.err.println("Failed finding an arriving plane: "+arrivingPlane);
				System.err.println("Arrival : "+prm.arrival);
				System.err.println("Gate loc : "+gateL);
				System.err.println("Gate g   : "+g);
				System.err.println("Gate g2   : "+g);
				System.err.println("First loc : "+first);
				System.err.println("First from : "+first.from+" "+first.from.getTerminalAlias());
				System.err.println("Viable planes: ");
				for(Airplane pl : this.sim.world.getFlightPlan().arrivingPlanes)
				{
					if(pl.time == prm.arrival)
					{
						System.err.println(" *: "+pl);
					}
				}
				System.err.println("Gate planes: ");
				if(g != null)
					for(Airplane pl : g.airplanes)
					{
						System.err.println(" *: "+pl);
					}
				
				System.err.println("All planes: ");
					for(Airplane pl : sim.world.getFlightPlan().arrivingPlanes)
					{
						System.err.println(" *: "+pl);
					}
			}
			
			this.arrivingPlane.prms.add(this);
			
			if(this.arrivingPlane.expected_arrive != prm.arrival)
			{
				throw new Error("Expected arrive is not arrival? "+arrivingPlane.expected_arrive+" / "+prm.arrival);
			}
		}
		
		if(last.embarkment || secondLast.embarkment)
		{
			Gate g;
			LocationAlias gateL;
			gateL = last.from.getTerminalAlias();
			
			g = plan.getTerminal(gateL.getArea()).getGate(gateL);
			Airplane plane = g.getDepartingPlane(prm.deadline);
			this.departingPlane = sim.getSimPlane(plane);
			
			if(departingPlane == null)
			{
				System.err.println("Failed finding a plane: "+departingPlane);
				System.err.println("time : "+prm.deadline);
				System.err.println("Gate loc : "+gateL);
				System.err.println("Gate g   : "+g);
				System.err.println("Last loc : "+last);
				System.err.println("2Last loc : "+secondLast);
				
				System.err.println("Viable planes: ");
				for(Airplane p : this.sim.world.getFlightPlan().departingPlanes)
				{
					if(p.time == prm.deadline)
					{
						System.err.println(" *: "+p);
					}
				}
				System.err.println("Gate planes: ");
				for(Airplane pl : g.airplanes)
				{
					System.err.println(" *: "+pl);
				}
			}
			
			if(this.departingPlane.isArrivingPlane())
			{
				throw new Error("Departing plane is an arriving plane? \n"+
								plane+"\n"+
								this.departingPlane.plane+"\n"+
								this.departingPlane);
			}
			
			this.departingPlane.prms.add(this);
		}
		
	}
	
	@Override
	public void arriveAtNextLocationInternal() 
	{	
		if(this.currentSegment == null)
		{
			this.currentSegment = this.segments[0];
			this.sim.addEvent(new Event_UpdateLocationTasks(this.sim.getTime(), this.location));
		}
		else
		{
			if(this.currentSegment.isEndlocation(this.location))
			{
				// Register time if possible.
				int time = (int)this.sim.getTime();
				if(!this.currentSegment.isEnded())
					this.currentSegment.registerSegmentArrived(time);
				if(this.location.isSupervised() || this.currentSegment.next == null)
				{
					if(!this.currentSegment.isFinished())
						this.currentSegment.registerSegmentTakeover(time);
				}
				this.currentSegment = this.currentSegment.next;
			}
			else
			{
//				throw new Error("Arrive is not location: "+location+" "+this.currentSegment);
			}
		}
	}

	@Override
	public double getDynamicTravelingTime(int ott) 
	{
		double fact = delayfact_min + (sim.rand.nextDouble()*(delayfact_max - delayfact_min));
		return fact * ott;
	}

	public void setTransport(SimulatedWorker w) 
	{
		if(this.transport != null)
		{
			this.transport.removePRM(this);
		}
		
		this.transport = w;
		if(w != null)
		{
			this.area = w.area;
			w.helping.add(this);
		}
	}
	
	public boolean isExpectedWorker(SimulatedWorker w)
	{
		if(this.currentSegment == null)
		{
			this.currentSegment = this.segments[0];
		}
		
		return this.currentSegment.assignedWorker == w;
	}
	
	public String toString()
	{
		return "PRM "+this.prm.prm_ID;
	}
	
	@Override
	public String getInfo()
	{	
		String segs = "";
		String tasks = "";
		
		for(SimulatedSegment s : this.segments)
		{
			segs += s+"\n";
		}
		
		int prog = 0;
		for(Task t : this.relevantTasks)
		{	
			if(t != null)
			{
				String p;
				if(t.worker.currentTask == null )
				{
					p = (t.worker.progessId > 0 ? "x" : "-");
				}
				else
				{
				  p = (t.executetime < t.worker.currentTask.executetime ? "x" : 
										(t.executetime > t.worker.currentTask.executetime ? "-" : "c"));
				}
				
				tasks += "("+t.executetime+")("+p+")"+t.worker.simpleString()+" "+t+"\n";
			}
			else
			{
				tasks +=" null";
			}
			
			prog++;
		}
		
		return this+"\n"+
			   super.getInfo()+"\n"+
			   "Transport: "+this.transport+"\n"+
//			   "Journey progress: "+this.journeyProgress+"\n"+
			   "Current Segment: "+this.currentSegment+"\n"+
			   "Assigned worker: "+(this.currentSegment == null ? " - " : this.currentSegment.assignedWorker )+"\n"+
			   "segments:\n"+segs+"\n"+
			   "tasks: \n"+tasks;
	}

	public void sortTasks() 
	{
		final SimulatedPRM self = this;
		Collections.sort(this.relevantTasks, new Comparator<Task>()
		{
			@Override
			public int compare(Task k1, Task k2) 
			{
				final double t1, t2;
				if(k1 instanceof PickupAndDeliverTask)
				{
					PickupAndDeliverTask pd = (PickupAndDeliverTask)k1;
					t1 = (pd.pickup.contains(self) ? pd.pickupTime : pd.executetime);
				}
				else 
				{
					t1 = k1.executetime;
				}
				if(k2 instanceof PickupAndDeliverTask)
				{
					PickupAndDeliverTask pd = (PickupAndDeliverTask)k2;
					t2 = (pd.pickup.contains(self) ? pd.pickupTime : pd.executetime);
				}
				else 
				{
					t2 = k2.executetime;
					}
				if(t1 == t2){return 0;}else if(t1 < t2){return -1;}else{return 1;}
			}
			
		});
	}
	
	public SimulatedWorker getWorker()
	{
		return this.transport;
	}

//	public boolean isNextTask(Task t) 
//	{
//		if(this.journeyProgress >= this.relevantTasks.size())
//		{
//			return false;
//		}
//		
//		Task current = this.relevantTasks.get(this.journeyProgress);
//		
//		if(this.journeyProgress + 1 == this.relevantTasks.size())
//		{
//			return current == t;
//		}
//		
//		Task next = this.relevantTasks.get(this.journeyProgress+1);
//		
//		if(current instanceof PickupAndDeliverTask && next instanceof PickupAndDeliverTask)
//		{
//			return current == t || next == t;
//		}
//		
//		return current == t;
//			 
//		
//	}
	
	@Override
	public void depart()
	{
		super.depart();
		
		if(this.currentSegment == null)
		{
			if(!this.segments[0].isEnded())
			{
				this.currentSegment = this.segments[0];
			}
			else
			{
				// This PRM is already done....
				if(this.segments[this.segments.length-1].isFinished())
				{
					// Odd we are already finished
					this.setTransport(null);
					return;
				}
				else
				{
					throw new Error("Current Segment is zero while mid journey!");
				}
			}
		}
		
		if(this.currentSegment.isBeginLocation(this.location))
		{
			if(!this.currentSegment.isStarted())
			{
				final int time = (int)(Math.floor(this.sim.getTime()));
				this.currentSegment.registerSegmentStart(time);
				if(!this.location.isSupervised())
				{
					if(this.currentSegment.prev != null)
					{
						this.currentSegment.prev.registerSegmentTakeover(time);
					}
				}
			}
		}
		else
		{
			throw new Error("Depart is not location: "+location+" "+this.currentSegment+" PRM "+this.prm.prm_ID);
		}
		// Update Segment info
		
		
	}
	
	public SimulatedSegment getSegment(Segment s)
	{
		for(SimulatedSegment seg : this.segments)
		{
			if(seg.seg == s)
			{
				return seg;
			}
		}
		
		return null;
	}

	public SimulatedSegment[] getSegments() 
	{
		return this.segments;
	}

	public void attemptFinish() 
	{
		if(this.currentSegment != null && this.currentSegment.next == null)
		{
			double time = this.transport.sim.getTime();
			
			if(!this.currentSegment.isEnded())
				this.currentSegment.registerSegmentArrived((int)time);
			
			if(!this.currentSegment.isFinished())
				this.currentSegment.registerSegmentTakeover((int)time);
		}
	}

	public SimulatedSegment getCurrentSegment() 
	{
		return this.currentSegment;
	}
	
	public SimulatedSegment getBoardingSegment()
	{
		for(SimulatedSegment seg : this.segments)
			if(seg.seg.embarkment)
				return seg;
		
		return null;
		
	}
}
