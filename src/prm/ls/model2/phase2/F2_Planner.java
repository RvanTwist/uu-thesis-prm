package prm.ls.model2.phase2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import prm.ls.model2.*;
import prm.problemdef.*;
import rpvt.algorithms.BMS_Matching;
import rpvt.algorithms.BMS_Node;
import rpvt.algorithms.Bipartite_Matching_Solver;

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
public class F2_Planner 
{
	TreeMap<PRM,F2_PRM> prms = new TreeMap<PRM,F2_PRM>();
	TreeMap<Transport,F2_Worker> workers = new TreeMap<Transport,F2_Worker>();
	TreeMap<Area,ArrayList<F2_Worker>> area2workers = new TreeMap<Area,ArrayList<F2_Worker>>();
	
	SimplePlanning planning;
	
	public TreeSet<F2_PRM> serviceQueue;
	
	TreeSet<F2_PRM> extraDeclined = new TreeSet<F2_PRM>(F2_PRM.getPRMcomparator());
	int extraDeclineCount = 0;
	private int extraDeclinedCount;
	
	public F2_Planner(SimplePlanning planning)
	{
		//TODO Figure out why in the buss area the workers are counted twice;
		
		this.planning = planning;
		
		for(PRM_Planning prm_p : planning.getPlannedPRMs() )
		{
			final F2_PRM prm_f2 = new F2_PRM(prm_p, this);
			prms.put(prm_p.prm, prm_f2);
		}
		
		for(Area a : planning.getInstance().getAreas() )
		{
			//System.out.println("Reading area "+a.id);
			System.out.println("Area "+a.id+" workers: "+a.getWorkerCount());
			
			final ArrayList<F2_Worker> workers = new ArrayList<F2_Worker>();
			
			this.area2workers.put(a, workers);
			
			for(Transport t : a.transporters)
			{
				//System.out.println("  - Adding worker: "+t.transport_ID);
				// TODO: Build in support for multiple PRMS per Transport.
				//System.out.println("Make worker");
				final F2_Worker worker = new F2_Worker(t,this);
				//System.out.println("Make made");
				this.workers.put(t, worker);
				
				workers.add(worker);
			}
		}
		
		this.serviceQueue = new TreeSet<F2_PRM>(F2_FirstComePRM_Comparator.getInstance());
		this.reset();
	}
	
	public void planAll()
	{
		System.out.println("Start planning");
		
		extraDeclined.clear();
		
		while(!serviceQueue.isEmpty())
		{
			F2_PRM prm = serviceQueue.pollFirst();
			//System.out.println("Help prm: ("+prm.currentTime+") "+prm.prm);
			serviceNextPRM(prm);
		}
		
		System.out.println("Plan all end: Extra Declined: "+this.extraDeclined.size());
	}
	
	public void serviceNextPRM(F2_PRM prm)
	{
		if(prm.isFullyPlanned())
		{
			// The prm is fully planned and could release the worker.
			//System.out.println("PRM fully planned! prm "+prm.prm.prm.prm_ID);
			prm.releaseWorker();
			return;
		}
		
		// Determine what area this prm need help in.
		final PlannedSegment pseg = prm.getNextSegment();
		final Area area = pseg.segment.getSupervisingArea();
		
		//System.out.println("Attempt to help PRM: "+prm.prm.prm.prm_ID+" in area "+area.id+" on Segment: "+pseg.segment);
		
		// Find candidates for service
		final ArrayList<F2_Worker> workers = this.area2workers.get(area);
		
		F2_Worker bestWorker = null;
		int bestScore = Integer.MIN_VALUE/2; // The higher the slack the better.
		
		for(F2_Worker worker : workers)
		{
			//System.out.print("  Considering Transport: "+worker.getTransport().transport_ID +"currloc: "+worker.getCurrentLocation());
			int score = worker.canService(prm, pseg);
			
			// MutateScore
			if(score >= 0)
			{
				if( (score < F2_Worker.SCORE_PRIORITY) )
				{ // No Priority is given, flip the score
					score = F2_Worker.SCORE_MAX - score;
				}
			}
			
			
			if(score > bestScore)
			{
				bestWorker = worker;
				bestScore = score;
			}
			
			//System.out.println(" slack: "+score);
		}
		
		// Check or there is an worker who can help if not fix it.
		if(bestScore < 0)
		{
			// TODO: Make some smart decision about what to delete.
			System.out.println("time: "+pseg.getStartTime()+" PRM Declined: prm: "+prm.prm.prm.prm_ID+" (area "+area.id+") Delay: "+bestScore+" on segment: "+prm.getNextSegment().segment.simpleString()+" movability: "+pseg.getMovabilityDebugString());
			
			for( F2_Worker w : workers)
			{
				final LocationAlias currLoc = w.getCurrentLocation();
				final Segment seg = pseg.segment;
				final LocationAlias toLoc = pseg.segment.from;
				final int timeDist = seg.getSupervisingArea().getDistance(currLoc, toLoc);
				System.out.println("   Worker T"+area.id+" "+w.getTransport().transport_ID+" currTime: "+w.getCurrentTime()+" currSegment: "+w.getCurrentSegment()+" currLoc: "+w.getCurrentLocation()+" time dist: "+timeDist+" lastLoc: "+w.getLastLocation()+"("+w.getLastEndTime()+")");
			}
			
//			resolveConflict_DeletePRM(prm);
			this.resolveConflict_OptimiseMatching(prm, pseg, workers);
			
			//throw new Error("Cannot Help this prm unfortunatly maybe with some delay of: "+bestScore);
		}
		else
		{ // Can be helped on time.
			final Segment seg = pseg.segment;
			final PRM pd_prm = prm.prm.prm;
			
//			System.out.println(	"time: "+pseg.getStartTime()+" T"+area.id+" "+bestWorker.getTransport().transport_ID+
//								" Servicing prm: "+pd_prm.prm_ID+" cap:"+bestWorker.getCurrentCapacity()+"/"+bestWorker.getTransport().capicity+
//								" on segment: "+seg+
//								" with slack: "+bestScore+" on area: "+area.id);
			prm.serviceNextSegment(pseg.getStartTime(), bestWorker);
			serviceQueue.add(prm);
		}
	}
	
	ArrayList<F2_Synchronized_PRM> tmp_declined = new ArrayList<F2_Synchronized_PRM>();
	private void resolveConflict_OptimiseMatching(F2_PRM prm, PlannedSegment pseg, ArrayList<F2_Worker> workers) 
	{
		System.out.println("RemoveConflict_OptimiseMatching");
		if(workers.size() == 0)
		{
			System.out.println("  - failed due no workers in the area!");
			this.resolveConflict_DeletePRM(prm);
			return;
		}
		
		// TODO: make this algorithm make less garbage.
		final int workers_lenght = workers.size();
		int[][] weights = new int[workers_lenght+3][workers_lenght+3];
		
		// Initialise;
		ArrayList<F2_Synchronized_PRM> pairs = new ArrayList<F2_Synchronized_PRM>();
		ArrayList<F2_Synchronized_PRM> other_declined = new ArrayList<F2_Synchronized_PRM>();
		
		final Area a = pseg.segment.getSupervisingArea();
		
		for(F2_Synchronized_PRM sprms : tmp_declined)
		{
			sprms.matched = false;
			if(a == sprms.seg.supervisingArea)
			{
				if(sprms.segStart + sprms.seg.segmentTime >= pseg.getStartTime())
				{ // Reconsider this PRM group
					System.out.println("  Reconsidering PRM group: "+sprms.seg.simpleString()+" start: "+sprms.segStart+" prms: "+sprms.printPRMs());
					pairs.add(sprms);
					for(F2_PRM f2_prm : sprms.prms)
					{
						//System.out.println("    - Reconsidering prm: "+f2_prm.prm.prm.prm_ID+" current seg: "+f2_prm.getNextSegment().segment.simpleString()+" route: "+f2_prm.printRoute());
						
						this.extraDeclined.remove(f2_prm);
						extraDeclineCount--;
					}
				}
				else
				{ // Decline permamently
					System.out.println("  Decline PRM group: "+pseg.segment.simpleString()+" start: "+pseg.getStartTime()+" prms: "+sprms.printPRMs());
					for(F2_PRM f2_prm : sprms.prms)
					{
						this.resolveConflict_DeletePRM(f2_prm);
					}
				}
			}
			else
			{
				other_declined.add(sprms);
			}
		}
		
		// Delete tmp_declined
		tmp_declined.clear();
		tmp_declined.addAll(other_declined);
		
		int maxCap = workers.get(0).max_cap;
		
		// Create pairs of already planned PRMS:
		for(F2_Worker worker : workers)
		{
			if(worker.currentSegment != null)
			{ // Worker is serving some people
				final F2_Synchronized_PRM prms = new F2_Synchronized_PRM(worker.currentSegment, worker.getCurrentTime(), worker.currentPRMs);
				pairs.add(prms);
				
				// Remove PRM from planning:
				for(F2_PRM planned_prm : worker.currentPRMs)
				{
					// Remove prm from the queue;
					serviceQueue.remove(planned_prm);
					
					planned_prm.currentSegment --;
					planned_prm.currentTime = worker.getCurrentTime();
					planned_prm.currentLocation = worker.getCurrentLocation();
					planned_prm.currentWorker = null;
				}
				
				worker.currentCap = 0;
				worker.currentPRMs.clear();
				worker.currentSegment = null;
				
				final int last_time = worker.getLastEndTime();
				final LocationAlias last_loc = worker.getLastLocation(); 
				
				worker.setCurrentLoction(last_loc, last_time);
				worker.setExpectedLocatoin(last_loc, last_time);
			}
		}
		
		// Try to add the prm who couldn't be planned somewhere
		boolean added = false;
		for(F2_Synchronized_PRM prms : pairs)
		{
			final Segment seg = pseg.segment;
			if(		pseg.getStartTime() == prms.segStart &&
					prms.capReq + prm.getCapicityReq() <= maxCap &&
					seg.from == prms.seg.from &&
					seg.to == prms.seg.to)
			{ // This segment could be added to this one
				prms.prms.add(prm);
				prms.capReq += prm.getCapicityReq();
				added = true;
				break;
			}
		}
		
		if(!added)
		{
			// Create new F2_Synchronized_PRM
			final F2_Synchronized_PRM prms = new F2_Synchronized_PRM(pseg.segment, pseg.getStartTime(), prm);
			pairs.add(prms);
		}
		
		// Make planning problem
		Bipartite_Matching_Solver<F2_Synchronized_PRM,F2_Worker> bms = new Bipartite_Matching_Solver<F2_Synchronized_PRM,F2_Worker>();
		
		// Make left Nodes
		for(F2_Synchronized_PRM prms : pairs)
		{
			bms.makeLeftNode(prms, prms.prms.size());
		}
		for(F2_Worker w : workers)
		{
			bms.makeRightNode(w);
		}
		
		// Make possible links
		for(BMS_Node<F2_Synchronized_PRM> l : bms.getLeftSide())
		{
			for(BMS_Node<F2_Worker> r : bms.getRightSide())
			{
				final F2_Worker worker = r.getRepresents();
				final F2_Synchronized_PRM s_prm = l.getRepresents();
				final Segment seg	= s_prm.seg;
				final Area currArea = s_prm.seg.supervisingArea;
				
				// Check or the worker can be there on time: TODO: Add robustness
				//System.out.println("Worker "+worker.workerName()+" last location: "+worker.lastLocation +" seg: "+seg+" PRM: "+s_prm.prms.get(0).prm.prm.prm_ID);
				
				if(worker.getLastEndTime() + currArea.getDistance(worker.getLastLocation(), seg.from) <= s_prm.segStart)
				{
					l.makeEdgeTo(r);
				}
			}
		}
		
		// Solve the problem
		bms.findPrioritisedMaximumMatching();
		BMS_Matching<F2_Synchronized_PRM,F2_Worker>[] matches = bms.getMatching();
		
		// Plann all
		for(BMS_Matching<F2_Synchronized_PRM,F2_Worker> match : matches)
		{
			final F2_Worker worker = match.right;
			final F2_Synchronized_PRM s_prm = match.left;
			final Transport transport = worker.transport;
			System.out.println("- Worker: T"+transport.getArea().id+" "+transport.transport_ID+" scheduled segment: "+s_prm.seg+" at time: "+s_prm.segStart);
			
			s_prm.matched = true;
			//System.out.println("Before: ("+worker.currentTime+")"+worker.currentLocation+"=>"+worker.expectedLocation+"("+worker.expectedRelease+") "+worker.currentSegment);
			
			for(F2_PRM p_prm : s_prm.prms)
			{	
				System.out.println("  - PRM: "+p_prm.prm.prm.prm_ID+" "+p_prm.getNextSegment().segment);
				p_prm.serviceNextSegment(s_prm.segStart, worker);
				
				// Make sure he is  serviced:
				serviceQueue.add(p_prm);
			}
			
			//System.out.println("Result: ("+worker.currentTime+")"+worker.currentLocation+"=>"+worker.expectedLocation+"("+worker.expectedRelease+") "+worker.currentSegment);
		}
		
		// Now handle the unmatched:
		for(F2_Synchronized_PRM s_prm : pairs)
		{
			if(!s_prm.matched)
			{
				System.out.println(" Not matched: "+s_prm.seg+" at time: "+s_prm.segStart);
				tmp_declined.add(s_prm);
				for(F2_PRM p_prm : s_prm.prms)
				{
					System.out.println("  - PRM: "+p_prm.prm.prm.prm_ID);
					extraDeclinedCount++;
					extraDeclined.add(p_prm);
				}
			}
		}
	}

	private void resolveConflict_DeletePRM(F2_PRM prm)
	{
		prm.unPlanAll();
		this.extraDeclined.add(prm);
	}
	
	public void reset()
	{
		this.serviceQueue.clear();
		
		for(F2_PRM prm : this.prms.values())
		{
			prm.reset();
			
			if(prm.isPlanned())
			{
				this.serviceQueue.add(prm);
			}
		}
		
		for(F2_Worker w : this.workers.values())
		{
			w.reset();
		}
	}

	public int getDeclinedCount() 
	{
		return this.extraDeclined.size();
	}

	public void saveSolution(File file) throws IOException 
	{
		Sollution s = new Sollution(this.planning.getInstance());
		
		for(F2_PRM prm : this.extraDeclined)
		{
			prm.currentSegment = 0;
		}
		
		for(F2_Synchronized_PRM sprm : this.tmp_declined)
		{
			for(F2_PRM prm : sprm.prms)
			{
				prm.currentSegment = 0;
			}
		}
		
		System.out.println("Saving sollution with declined: "+extraDeclined.size()+"+"+tmp_declined.size());
		
		for(F2_PRM prm : this.prms.values())
		{
			prm.saveSolution(s);
		}
		
		s.save(file);
	}
}
