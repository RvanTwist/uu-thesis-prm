package simulation.objects;

import java.util.TreeSet;

import prm.gen.Airplane;
import simulation.Distributions;
import simulation.DynamicWorldSimulation;
import simulation.SimulatedPositionalbe;
import simulation.events.Event_PlaneDelayLastUpdate;
import simulation.events.Event_PlaneDelayNotify;
import simulation.objects.SimulatedPRM;

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
public class SimulatedPlane extends SimulatedPositionalbe
{
	final static double minTimeBetweenFlights = 45;
	final static double rescheduling_Thresshold = 10;
	
	public Airplane plane;
	
	TreeSet<SimulatedPRM> prms = new TreeSet<SimulatedPRM>();
	
	public final double expected_arrive;
	public final double expected_depart;
	
	// Sneaky future data:
	double actual_arrive = -1;
	double actual_depart = -1;
	
	
	private double delay_Known = -1;
	private double delay_Last_Update = -1;
	
	public SimulatedPlane(DynamicWorldSimulation sim, Airplane p) 
	{
		super(sim, null);
		this.plane = p;
		
		if(p.arrival)
		{
			this.expected_arrive = p.time;
			this.expected_depart = p.time + 60 + 60 * sim.rand.nextDouble();
		}
		else
		{
			this.expected_arrive = p.time - 60 - 60 * sim.rand.nextDouble();
			this.expected_depart = p.time;
		}
		
		this.initialise();
		
	}
	
	private void initialise()
	{	
		if(this.plane.arrival)
		{
			this.initialiseArrival();
		}
		else
		{
			this.initialiseDepart();
		}
	}

	private void initialiseDepart() 
	{

		//this.actual_arrive = this.expected_arrive + Distributions.randomNormalSTD(4, 30, sim.rand);
		this.actual_depart = this.expected_depart + Distributions.randomExp(15, sim.rand);
		
		double TimeBetweenFlights = 60 + (5 * sim.rand.nextInt(12)); 
		
		
		// Calculate aprox last update Call
		this.delay_Last_Update = Math.max(1,	this.actual_depart
												- minTimeBetweenFlights
												- Distributions.randomExp(15, sim.rand));
			
		delay_Known = Math.min(delay_Last_Update, this.expected_depart-minTimeBetweenFlights);
		
		final double delay = this.actual_depart - this.expected_depart;
			
//		System.out.println(this+" departure delay: arrive: "+this.actual_depart+"/"+this.expected_depart+" notify: "+this.delay_Known+" "+this.delay_Last_Update);
		
		
//		if(delay >= rescheduling_Thresshold)
		{
			this.sim.eventqueue.addEvent(new Event_PlaneDelayNotify(this,this.delay_Known-0.0001));
			this.sim.eventqueue.addEvent(new Event_PlaneDelayLastUpdate(this,this.delay_Last_Update));
		}
	}

	private void initialiseArrival() 
	{
		this.actual_arrive = this.expected_arrive + Distributions.randomNormalVar(4, 30, sim.rand);
		
		this.delay_Last_Update = Math.max(1,	this.actual_arrive
												- Math.max(5,Distributions.randomExp(1.0/15.0, sim.rand)));
		delay_Known = Math.min(delay_Last_Update, this.expected_arrive-10-Distributions.randomExp(1.0/15.0,sim.rand));
		
		final double delay = Math.abs(this.actual_depart - this.expected_depart);
		
//		System.out.println(this+" arival delay: arrive: "+this.actual_arrive+"/"+this.expected_arrive+" notify: "+this.delay_Known+" "+this.delay_Last_Update);
		
		this.sim.eventqueue.addEvent(new Event_PlaneDelayNotify(this,this.delay_Known-0.0001));
		this.sim.eventqueue.addEvent(new Event_PlaneDelayLastUpdate(this,this.delay_Last_Update));
	}

	@Override
	public void arriveAtNextLocationInternal() 
	{
		
	}

	@Override
	public double getDynamicTravelingTime(int ott) 
	{
		return 0;
	}

	public Iterable<SimulatedPRM> getDepartingPRMs() 
	{
		return this.prms;
	}

	public boolean isArrivingPlane() 
	{
		return this.plane.arrival;
	}

	public boolean requireSuspending() 
	{
		if(this.requireRescheduling())
		{
			if(this.delay_Known < this.delay_Last_Update)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean requireRescheduling() 
	{	
		if(this.isArrivingPlane())
			return Math.abs(this.actual_arrive - this.expected_arrive) >= rescheduling_Thresshold;
		else
			return Math.abs(this.actual_depart - this.expected_depart) >= rescheduling_Thresshold;
	}

	public Iterable<SimulatedPRM> getArrivingPRMs() 
	{
		return this.prms;
	}

	public double getActualDepart() 
	{
		return this.actual_depart;
	}

	public double getActualArrive() 
	{
		return this.actual_arrive;
	}
	
	@Override
	public String toString()
	{
		return "Sim "+plane;
	}
}
