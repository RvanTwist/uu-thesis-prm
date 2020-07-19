package prm.ls.model4;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import prm.ls.*;
import prm.ls.resources.ParalelResource;
import prm.problemdef.*;
import rpvt.util.RBTree;

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
public class M4_Planning implements LS_Solution, LocalSearchListener
{
	public static boolean DEBUG = false;
	
	//Randomness
	public Random random;
	
	public final BackTracker backtracker = new BackTracker();
	
	public RBTree<M4_PRM> prms = new RBTree<M4_PRM>();
	public TreeMap<Area,M4_Area> areas = new TreeMap<Area,M4_Area>();
	
	public RBTree<M4_PRM> declined = new RBTree<M4_PRM>();
	RBTree<M4_PRM> accepted = new RBTree<M4_PRM>();
	
	public RBTree<M4_SegmentGroup<?>> plannedSegmentGroups = new RBTree<M4_SegmentGroup<?>>();
	
	public RBTree<M4_PossibleMatch> matches = new RBTree<M4_PossibleMatch>();
	
	public RBTree<M4_SegmentGroup<?>> invalidSegmentGroups = new RBTree<M4_SegmentGroup<?>>(); 
	public RBTree<M4_SegmentGroup<?>> suspendedSegmentGroupsReplan = new RBTree<M4_SegmentGroup<?>>();
	
	final PRM_Instance instance;
	
	// Score;
	
	public int declinePanelty = 0;
	public int serviceDelayPanelty = 0; // Unused;
	public int robustnessPanelty = 0;   // Robustness Panelty is only used in the matching, not real score of sollution.
	
	// Backtrack Score;
	public int bt_declinePanelty = 0;
	public int bt_serviceDelayPanelty = 0; // Unused;
	public int bt_robustnessPanelty = 0;
	public int bt_invalidSegments = 0;
	
	public SimulatedAnnealing solver;
	
	// Actually an old panelty from origoinal algorithm not sure or it is used anymore.
	private int distributionPanelty = 0;
	private int newDistributionPanelty;
	
	// Best
	Sollution bestSollution;
	
	int bestScore = Integer.MAX_VALUE;
	int bestRobustnessPanelty = Integer.MAX_VALUE;
	int best_invalidSegments = 0;
	
	public final boolean shifts;
	
	public double score2_lowerBound = 0;

	public int score1_lowerBound = 0;

	public int minRescheduleTime;

	
	
	public M4_Planning(PRM_Instance instance)
	{
		this(instance, new Random());
	}
	
	public M4_Planning(PRM_Instance instance, Random r)
	{
		this.random = (r == null ? new Random() : r);
		
		this.instance = instance;
		
		this.bestSollution = new Sollution(instance);
		
		boolean shifts = false;
		
		// Generate resources:
		for( Area a : instance.world.values() )
		{
			final M4_Area area = new M4_Area(a,this);
			this.areas.put(a,area);
			
			if(area.workers.size() > 0)
			{
				M4_Worker w1 = area.workers.get(0);
				final int start = w1.getTransport().startShift;
				final int end  = w1.getTransport().endShift;
				
				for(M4_Worker w : area.workers)
				{
					if(start != w.getTransport().startShift || 
					   end   != w.getTransport().endShift		)
					{
						shifts = true;
					}
				}
			}
		}
		
		this.shifts = shifts;
		System.out.println("shifts: "+this.shifts);
		
		// Generate PRM's
		for(PRM prm : instance.clients.values())
		{
			if(prm.route.length > 0)
			{
				M4_PRM prm_p = new M4_PRM(this, prm);
				prms.add(prm_p);
				this.declined.add(prm_p);
			}
		}
		
		int sg_count = 0;
		int movable_sg_count = 0;
		for(M4_PRM prm : this.prms)
		{
			int segs = 0;
			for(M4_PRM_SegmentGroup sg : prm.segmentGroups)
			{
				segs = segs + sg.segments.length;
				sg_count++;
				if(!sg.fixed)
				{
					movable_sg_count++;
				}
			}
			
			if(segs != prm.prm.route.length)
			{
				throw new Error("Not All segments are present!");
			}
		}
		
		System.out.println(" "+movable_sg_count+"/"+sg_count+" segmentGroups are movable. prms: "+prms.size());
	}
	
	public int calculateScore()
	{
		this.declinePanelty = 0;
		this.serviceDelayPanelty = 0;
		
		// Recalculate score
		for(M4_PRM prm : this.prms)
		{
			// TODO: Divide the costs of not taking by prebooked and none Prebooked.
			if(prm.isPlanned())
			{
				// Calculate service delay Panelty
				int prevEnd = prm.prm.arrival;
				boolean supervised = true;
				
				for(M4_PRM_Segment seg : prm.segments)
				{
					if(!supervised)
					{
						final int sdp = M4_CostModel.getDelayPenalty(seg.getStartTime() - prevEnd);
						if(sdp < 0)
							throw new Error("DelayPenalty can't go below 0! "+sdp);
						
						this.serviceDelayPanelty += sdp;
					}
				}
			}
			else
			{
				this.declinePanelty += prm.getDeclinePanelty();
			}
		}
		
		// Recalculate Robustness
		this.robustnessPanelty = 0;
		
		for(M4_Area a : this.areas.values())
		{
			for(M4_Worker w : a.workers)
			{
				w.start.robustnessPanelty = 0;
				w.end.robustnessPanelty = 0;
				w.start.updateRobustnessPanelty();
				w.end.updateRobustnessPanelty();
			}
		}
		
		for(M4_PRM prm : this.prms)
		{
			for(M4_PRM_Segment seg : prm.segments)
			{
				seg.robustnessPanelty = 0;
				seg.updateRobustnessPanelty();
			}
		}
		
		
		return this.declinePanelty + this.serviceDelayPanelty + this.distributionPanelty;
	}

	public M4_Area getResource(Area a) 
	{
		return this.areas.get(a);
	}
	
	public void generateInitialSolution()
	{
//		for(PRM_Planning prm : this.prms)
//		{
//			prm.fullyPlanRandom();
//		}
	}

	public int recalculateScore() 
	{
		// TODO: Recalculate score;
		return this.calculateScore();
	}

	@Override
	public int getScore() 
	{
		return this.declinePanelty + this.serviceDelayPanelty + this.distributionPanelty;
	}
	
	public int getScore2() 
	{
		return this.robustnessPanelty;
	}

	@Override
	public void revertBest()
	{
		try
		{
			this.loadSolution(this.bestSollution);
		}
		catch(CantPlanException e)
		{
			e.printStackTrace();
			throw new Error(e);
		}
	}

	@Override
	public void saveBest() 
	{
		this.bestScore = this.getScore();
		this.bestRobustnessPanelty = this.robustnessPanelty;
		this.best_invalidSegments = this.invalidSegmentGroups.size();
		
//		System.out.println("Savebest! declined: "+this.declined.size()+" score:"+this.getScore()+"|"+this.getScore2());
		
		// Debug
//		this.testConsistancy();
//		this.recalculateScore();
//		System.out.println(" Huh? declined: "+this.declined.size()+" score:"+this.getScore()+"|"+this.getScore2());
		
		for(M4_PRM prm : this.prms)
			for(M4_PRM_Segment seg : prm.segments)
			{
				seg.updateBest();
			}
		
//		 DEBUG
//		 if(this.declined.size() == 1)
//		 {
//			System.out.println("      Debug loading:");
//			this.loadSolution(this.bestSollution);
//			System.out.println("      score: "+this.getScore()+"|"+this.getScore2());
//			for(M4_PRM prm : this.declined)
//			{
//				System.out.println(" declined: "+prm);
//			}
//			this.testConsistancy();
//			
//		 }
	}

	public M4_PRM getRandomDeclined() 
	{
		return this.declined.getRandomValue(random);
	}
	
	public M4_PRM getRandomAccepted() 
	{
		return this.accepted.getRandomValue(random);
	}
	
	public void analyse()
	{	
		System.out.println(" "+this.accepted.size()+" accepted, "+this.declined.size()+" rejected, total: "+this.prms.size());
	}

	@Override
	public boolean isFeasible() 
	{	
		// TODO: add feasible check;
		return true;
	}

	public void emptySolution()
	{
		for(M4_PRM prm : this.prms)
		{
			try
			{
				prm.unPlan();
			}
			catch(CantPlanException e)
			{
				e.printStackTrace();
				throw new Error(e);
			}
		}
		
		this.recalculateScore();
		
		this.makeAllCheckpoints();
		
		//System.out.println("Debug:");
		//this.analyse();
	}

	public Iterable<M4_PRM> getPlannedPRMs() 
	{
		return this.prms;
	}
	
	public PRM_Instance getInstance()
	{
		return this.instance;
	}
	
	/**
	 * Generate matchings of trips who can be merged and handled by a same transporter.
	 */
	public void generatePossibleMatches()
	{
		// Clear current Matches!
		this.matches.clear();
		for(M4_PRM prm : prms)
			for(M4_PRM_SegmentGroup sg : prm.segmentGroups)
				sg.possibleMatches.clear();
		
		
		ArrayList<M4_PossibleMatch> newMatches = new ArrayList<M4_PossibleMatch>();
		
		int matchesCount = 0;
		int fixedmatchesCount = 0;
		
		M4_PRM[] prms = this.prms.toArray(new M4_PRM[this.prms.size()]);
		
		final int size = this.prms.size();
		for(int i = 0 ; i < size ; i++)
		{
			M4_PRM prm1 = prms[i];
			for(int j = i + 1 ; j < size ; j++)
			{
				M4_PRM prm2 = prms[j];
				
				if(prm1 == prm2)
				{
					throw new Error("This shouldn't be happening!");
				}
				
				if(prm2.prm.deadline < prm1.prm.arrival || prm1.prm.deadline < prm2.prm.arrival)
				{	// No possible match
					continue;
				}
				
				final int combinedCapReq = prm1.prm.capRequirement + prm2.prm.capRequirement;
				
				// Analyse the groups:
				for(M4_PRM_SegmentGroup g1 : prm1.segmentGroups)
				{
					for(M4_PRM_SegmentGroup g2 : prm2.segmentGroups)
					{
						// Try general feasibility
						final int g1_es = g1.getErliestStart();
						final int g1_ls = g1.getLatestStart();
						final int g2_es = g2.getErliestStart();
						final int g2_ls = g2.getLatestStart();
						
						newMatches.clear();
						for(M4_PRM_Segment seg1 : g1.segments)
						{
							for(M4_PRM_Segment seg2 : g2.segments)
							{
								final Segment aseg1 = seg1.segment;
								final Segment aseg2 = seg2.segment;
								
								if(		seg1.area == seg2.area && 
										combinedCapReq <= seg1.area.getMinCapWorkers() && // check or they can be helped by the same transport
										aseg1.from == aseg2.from && 
										aseg1.to == aseg2.to			)
								{ 
									final int seg1_es = seg1.getOriSegmentGroupOffset() + g1_es;
									final int seg1_ls = seg1.getOriSegmentGroupOffset() + g1_ls;
									final int seg2_es = seg2.getOriSegmentGroupOffset() + g2_es;
									final int seg2_ls = seg2.getOriSegmentGroupOffset() + g2_ls;
									
									if( seg1_es <= seg2_ls && seg2_es <= seg1_ls)
									{ // There is overlap!;
										// seg1.offset = offset + seg2.offset
										int offset = seg1.getOriSegmentGroupOffset() - seg2.getOriSegmentGroupOffset();
										boolean newMatch = true;
										for(M4_PossibleMatch match : newMatches)
										{
											if(match.offset == offset)
											{ // this match is already present.
												newMatch = false;
												
												// Add it to existing match
												match.segments.add(new M4_SegmentMatch(seg1,seg2));
											}
										}
										
										if(newMatch)
										{
											//System.out.println("Able to serve prm: "+prm1.prm.prm_ID+" and prm: "+prm2.prm.prm_ID+" offset: "+offset+" on segment "+aseg1+"|"+aseg2);
											final M4_PossibleMatch match = new M4_PossibleMatch(g1,g2,offset);
										
											match.segments.add(new M4_SegmentMatch(seg1,seg2));
											this.matches.add(match);
											newMatches.add(match);
											matchesCount++;
											
											if(g1.fixed || g2.fixed)
											{
												fixedmatchesCount++;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		System.out.println(" matches: "+matchesCount + " of which fixed: "+fixedmatchesCount);
	}
	
	public void testConsistancy()
	{
		System.out.println("Checking planning consistancy! "+this.getScore()+"|"+this.getScore2());
		
		// Debug:
		
		final int p1 = this.getScore();
		final int p2 = this.robustnessPanelty;
		int sp2 = 0;
		
		for(M4_Area a : this.areas.values())
		{
			for(M4_Worker w : a.workers)
			{
				sp2 += w.start.robustnessPanelty;
				sp2 += w.end.robustnessPanelty;
			}
		}
		
		// Recalculate Robustness
		this.robustnessPanelty = 0;
		
		for(M4_PRM prm : this.prms)
		{
			for(M4_PRM_Segment seg : prm.segments)
			{
				sp2 += seg.robustnessPanelty;
				final int oldPanelty = seg.robustnessPanelty;
				
				seg.robustnessPanelty = 0;
				seg.updateRobustnessPanelty();
				
				if(seg.robustnessPanelty != oldPanelty)
				{
					throw new Error("Something went wrong with robustness: "+this.robustnessPanelty+" old: "+p2+"\n" +
							"In segment: "+seg+"\n"+
							"Old: "+oldPanelty+" | new: "+seg.robustnessPanelty+"\n"+
							"Prev: "+seg.getPrevious()+"\n"+
							"Slack: "+seg.getPrevSlack()+"\n"+
							"LastMutation: "+solver.lastMutation.getClass().getSimpleName()+"\n"+
							"Data: "+solver.lastMutation+"\n"+
							"Feasible: "+solver.facture.feasible);
				}
			}
		}
		
		if(p2 != sp2)
		{
			throw new Error("Something went wrong with robustness: "+this.robustnessPanelty+" old: "+p2+"\n" +
							"Counted: "+sp2+"\n"+
							"LastMutation: "+solver.lastMutation.getClass().getSimpleName()+"\n"+
							"Data: "+solver.lastMutation+"\n"+
							"Feasible: "+solver.facture.feasible);
		}
		
		this.recalculateScore();
		
		if(p1 != this.getScore())
		{
			throw new Error("Something went wrong with scoring: "+this.getScore()+" old: "+p1+"\n" +
					"Parts: "+this.declinePanelty+" "+this.distributionPanelty+"\n"+
					"LastMutation: "+solver.lastMutation.getClass().getSimpleName()+"\n"+
					"Data: "+solver.lastMutation+"\n"+
					"Feasible: "+solver.facture.feasible);
		}
		
		if(p2 != this.robustnessPanelty)
		{
			throw new Error("Something went wrong with robustness: "+this.robustnessPanelty+" old: "+p2+"\n" +
							"LastMutation: "+solver.lastMutation.getClass().getSimpleName()+"\n"+
							"Data: "+solver.lastMutation+"\n"+
							"Feasible: "+solver.facture.feasible);
			
		}
		
		
		int segments_count1 = 0; 
		
		for(M4_Area a : this.areas.values())
		{
			for(M4_Worker w : a.workers)
			{
				w.checkConsistancy();
				segments_count1 += w.plannedSegments.size();
			}
		}
		
		int segments_count2 = 0;
		for(M4_PRM prm : this.accepted)
		{
			prm.checkConsistancy();
			segments_count2 += prm.segments.length;
		}
		
		System.out.println("Planned segments: "+segments_count1+"/"+segments_count2);
	}

	public M4_PossibleMatch getRandomMatch() 
	{
		return this.matches.getRandomValue(random);
	}
	
	public void backtrack()
	{
//		testConsistancy();
		
		this.backtracker.backtrack();
		
		//this.declinePanelty = this.bt_declinePanelty;
		this.serviceDelayPanelty = this.bt_serviceDelayPanelty;
		this.robustnessPanelty = this.bt_robustnessPanelty;
		
//		testConsistancy();
	}
	
	public void makeCheckpoint()
	{
//		testConsistancy();
		this.bt_declinePanelty = this.declinePanelty;
		this.bt_serviceDelayPanelty = this.serviceDelayPanelty;
		this.bt_robustnessPanelty = this.robustnessPanelty;
		this.bt_invalidSegments = this.invalidSegmentGroups.size();
		
		this.distributionPanelty = this.newDistributionPanelty;
		this.backtracker.makeCheckpoints();
		
//		testConsistancy();
	}
	
	public void makeAllCheckpoints()
	{
		this.bt_declinePanelty = this.declinePanelty;
		this.bt_serviceDelayPanelty = this.serviceDelayPanelty;
		this.bt_robustnessPanelty = this.robustnessPanelty;
		this.bt_invalidSegments = this.invalidSegmentGroups.size();
		
		this.distributionPanelty = this.newDistributionPanelty;
		
		this.backtracker.makeAllCheckpoints();
	}

	public M4_SegmentGroup getRandomPlannedSegmentGroup() 
	{
		return this.plannedSegmentGroups.getRandomValue(random);
	}

	@Override
	public String getStatusText() 
	{	
		return "declined: "+this.declined.size()+"/"+this.prms.size();
	}

	@Override
	public void onAccept(SimulatedAnnealing anealer) {
		// TODO Auto-generated method stub
		
	}

	public static DateFormat formatter = new SimpleDateFormat("hh_mm_dd-MM-yyyy");
	@Override
	public void onFinish(SimulatedAnnealing anealer) 
	{
		this.bestSollution.solverSettings = this.getSolverSettings();
		
		System.out.println("Writing sollution!");
		try 
		{
			File dir = new File("solutions");
			if(!dir.exists())
			{
				dir.mkdir();
			}
			
			this.bestSollution.save(new File("solutions/sollution"+formatter.format(new Date(System.currentTimeMillis()))+".txt"));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}

	@Override
	public void onInitialise(SimulatedAnnealing anealer) 
	{
		this.recalculateScore();
		this.bestSollution.solverSettings = this.getSolverSettings();
	}

	@Override
	public void onReject(SimulatedAnnealing anealer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postProcessFacture(SimulatedAnnealing anealer, SolutionFacture f) 
	{
		// Automated robustness change collector;
		
		if(this.invalidSegmentGroups.size() < this.bt_invalidSegments)
		{
			f.accept = true;
		}
		
		if(M4_Constants.optimizeRobustness)
		{
			f.costs2 = (this.robustnessPanelty - this.bt_robustnessPanelty) * 3;
		}
		
		if(M4_Constants.useParalelResourceScore)
		{
			newDistributionPanelty = 0;
			
			for(M4_Area a : this.areas.values())
			{
				newDistributionPanelty += a.resource.getScore();
			}
			
			final int change = this.newDistributionPanelty - this.distributionPanelty;
//			System.out.println("calculated distribution panelty diffrence: "+change);
			f.costs += change;
		}
	}
	
	public void loadSolution(Sollution s) throws CantPlanException
	{
		M4_MergedSegmentGroup.ENABLE_AUTOMERGING = false;
		
		System.out.println("Loading sollution");
		// Unplan everything
		for(M4_PRM prm : this.prms)
		{
			prm.unPlan();
		}
		
		for(M4_PRM prm : this.prms)
		{	
			boolean planned = true;
			
			for(M4_PRM_Segment seg : prm.segments)
			{
				ScheduledSegment plannedSeg = s.getScheduling(seg.segment);
				
				if(plannedSeg == null)
				{
					planned = false;
				}
				else if(!seg.loadSegment(plannedSeg))
				{
					planned = false;
				}
				
				
			}
			
			if(planned)
			{
				prm.setPlanned();
				for(M4_PRM_SegmentGroup sg : prm.segmentGroups)
				{
					sg.getPlannedGroup().setPlanned(true);
				}
			}
			else
			{
				prm.unPlan();
			}
		}
		this.recalculateScore();
		System.out.println("Done loading! score: "+this.getScore() +" declined: "+this.declined.size()+"/"+this.prms.size());
		M4_MergedSegmentGroup.ENABLE_AUTOMERGING = true;
		
		this.makeAllCheckpoints();
		
		for(M4_PRM m4_prm : this.prms)
		{
			PRM prm = m4_prm.prm;
			
			if(prm.arrival_Suspended)
			{
				m4_prm.setArrivalSuspended();
			}
			if(prm.departing_Suspended)
			{
				m4_prm.setDepartSuspended();
			}
		}
	}
	
	public String getSolverSettings()
	{
		return 	"Algorithm Base: M4\n"+
				" - Decline Panelty: "+M4_Constants.declinePenalty+"\n"+
				" - Use ParalelResourceScore: "+M4_Constants.useParalelResourceScore+"\n"+
				"Solver: "+this.solver.printSettings()+"\n"+
				"\n"+
				"PRMs          : "+this.prms.size()+"\n"+
				"Declined PRMs : "+this.declined.size()+"\n"
				;
	}

	public Sollution getBestSolution() 
	{
		return this.bestSollution;
	}

	@Override
	public boolean isImprovedSinceBest() 
	{	
		final int score = this.bt_declinePanelty + this.bt_serviceDelayPanelty;
		
		if(this.invalidSegmentGroups.size() < best_invalidSegments )
		{ // Validity above everything else
			return true;
		}
		else if(score < this.bestScore)
		{
			return true;
		}
		else if(score == this.bestScore)
		{
			if(this.bt_robustnessPanelty < this.bestRobustnessPanelty)
				return true;
		}
		
		return false;
	}

	@Override
	public boolean isOptimum() 
	{
		if(M4_Constants.StopAtOptimum)
		{
			// Replanning is succesfull!
			if(!this.invalidSegmentGroups.isEmpty())
			{
				return false;
			}
			if(!suspendedSegmentGroupsReplan.isEmpty())
			{
				return false;
			}
			
			if(this.declinePanelty + this.serviceDelayPanelty <= this.score1_lowerBound)
			{
				if(this.robustnessPanelty <= this.score2_lowerBound)
				{
					return true;
				}
			}
		}
		
		return false;
		
		// Optimum is reached when everything is 0;
	}

	/**
	 * Fix all time and workers of segments planned before or at time i.
	 * Also fix all PRM's starting their journey before that time.
	 * @param i
	 */
	public void fixSegmentsTillTime(int i) 
	{
		for(M4_Area a : this.areas.values())
			for(M4_Worker t : a.workers)
				for(M4_Segment seg : t.plannedSegments)
				{
					if(seg.getStartTime() <= i)
					{
						seg.setFixedTime(true);
						seg.setFixedWorker(true);
					}
				}
		
		for(M4_PRM prm : this.prms)
		{
			if(prm.isPlanned() && prm.getStartTime() <= i)
			{
				prm.setFixedPlanned();
				
				for(M4_PRM_SegmentGroup sg : prm.segmentGroups)
				{
					final int start = sg.getStart();
					if(start <= i)
					{
						sg.setFixedTime(start);
					}
				}
			}
		}
	}

	public void setMoveableWindowTresshold(int time) 
	{
		ArrayList<M4_PRM> removePRMs = new ArrayList<M4_PRM>();
		
		this.minRescheduleTime = time;
		
		for(M4_PRM prm : this.prms)
		{
			for(M4_PRM_SegmentGroup sg : prm.segmentGroups)
			{
				if(!sg.isFixed())
				{
					sg.setWindowTresshold(time);
					
					if(prm.isPlanned() && sg.getStart() <= time)
					{
						sg.setFixedTime(sg.getStart());
						System.out.println(" Warnig, segmentGroup is not fixed while it should be!");
					}
				}
			}
			
			if(!prm.isPlanned())
			{ // Check feasibility
				for(M4_PRM_SegmentGroup sg : prm.segmentGroups)
				{
					if ( sg.getLatestStart() < this.minRescheduleTime )
					{
						// Its impossible to plan this PRM
						removePRMs.add(prm);
					}
				}
			}
		}
		
		for(M4_PRM prm : removePRMs)
		{
			this.prms.remove(prm);
			this.declined.remove(prm);
		}
		
	}

	/**
	 * Get the lowerbound robustness score of all past events, which the planner can't touch.
	 * @return
	 */
	public int calculate_Score2LB()
	{
		int panelty = 0;
		
		for(M4_Area a : this.areas.values())
			for(M4_Worker w : a.workers)
				for(M4_Segment seg : w.plannedSegments)
				{
					M4_Segment prev = seg.getPrevious();
					if(		prev != null && 
							(prev.isFixedWorker() || prev instanceof M4_WorkerSegment) &&
							(seg.isFixedWorker() || seg instanceof M4_WorkerSegment) )
					{
						panelty += seg.robustnessPanelty;
					}
				}
		
		this.score2_lowerBound = panelty;
		
		return panelty;
	}
	
	/**
	 * Remove all additional fixed time and worker constraints.
	 * Also resets the minimum Time for updates to 0;
	 */
	public void unfix() 
	{
		this.minRescheduleTime = 0;
		
		for(M4_PRM prm : this.prms)
		{
			prm.unfix();
		}
		
	}

	public M4_PRM getPRM(PRM prm) 
	{
		for(M4_PRM m4prm : this.prms)
		{
			if(m4prm.prm == prm)
			{
				return m4prm;
			}
		}
		return null;
	}

	public void checkForInvalidSegments() 
	{
		for(M4_PRM prm : this.prms)
		{
			for(M4_SegmentGroup<?> sg : prm.segmentGroups)
			{
				sg.checkValid();
			}
		}
	}
	
	public M4_PRM_SegmentGroup getRandomInvalidSG()
	{
		if(this.invalidSegmentGroups.isEmpty())
		{ // GOOD!
			return null;
		}
		else
		{
			return (M4_PRM_SegmentGroup)this.invalidSegmentGroups.getRandomValue(this.random);
		}
	}

	/**
	 * Completly reloads the instance from the instance Data and updates every PRM and segment.
	 */
	public void reloadInstance() 
	{
		// Update and remove any PRM not in this instance.
		Iterator<M4_PRM> prms = this.prms.iterator();
		
		while(prms.hasNext())
		{
			M4_PRM m4_prm = prms.next();
			PRM prm = m4_prm.prm;
			
			if(this.instance.clients.containsValue(prm))
			{ // PRM is a valid member
				m4_prm.reloadPRM();
			}
			else
			{ // Remove the PRM from the instance.
				prms.remove();
				try
				{
					m4_prm.unPlan();
				}
				catch(CantPlanException e)
				{
					e.printStackTrace();
					throw new Error(e);
				}
			}
		}
		
		for(PRM prm : this.instance.clients.values())
		{
			M4_PRM m4_prm = this.getPRM(prm);
			if(m4_prm == null)
			{
				m4_prm = new M4_PRM(this, prm);
				this.prms.add(m4_prm);
				this.declined.add(m4_prm);
			}
		}
	}

	public M4_SegmentGroup<?> getRandomReplanSG() 
	{
		if(this.suspendedSegmentGroupsReplan.isEmpty())
		{
			return null;
		}
		
		return this.suspendedSegmentGroupsReplan.get(random.nextInt(this.suspendedSegmentGroupsReplan.size()));
	}

	/**
	 * Calculate the lowerbound of the decline penalty.
	 * @return
	 */
	public int calculate_Score1DecLB() 
	{
		int lb = 0;
		
		for(M4_PRM prm : this.declined)
		{
			if(!prm.prm.allowPlan)
			{
				lb += prm.getDeclinePanelty();
			}
		}
		return lb;
	}

	public M4_Segment getSegment(Segment seg) 
	{
		M4_PRM prm = this.getPRM(seg.prm);
		if(prm != null)
		{
			for(M4_PRM_Segment m4_seg : prm.segments)
			{
				if(m4_seg.segment == seg)
					return m4_seg;
			}
		}
		return null;
	}
}
