package prm.ls.model4.matching.lpflow;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeSet;

import otheralgorithms.HungarianAlgorithm;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import prm.ls.model4.CantPlanException;
import prm.ls.model4.M4_Area;
import prm.ls.model4.M4_CostModel;
import prm.ls.model4.M4_MergedSegment;
import prm.ls.model4.M4_PRM_Segment;
import prm.ls.model4.M4_Segment;
import prm.ls.model4.M4_Worker;
import prm.ls.model4.M4_WorkerSegment;
import prm.ls.model4.matching.EdgeCostModel;
import prm.ls.model4.matching.Matching_ShiftFixer;
import prm.problemdef.Area;
import prm.problemdef.Segment;

import java.util.Arrays;

import rpvt.lp.*;

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
/**
 * 
 * Todo Handle merged segments correctly;
 * 
 * @author rene
 */
public class AreaFlowProblemHungarian implements EdgeCostModel 
{
	final M4_CFMN_Hungarian parent;
	
	final static double NoEdge = Double.MAX_VALUE / 100000;
	final static double planBonus = -401;
	final static double samePRMbonus = -800;
	
	M4_Area area;
	
	ArrayList<M4_Segment> segments = new ArrayList<M4_Segment>();
	
	M4_Segment[] sources;
	M4_Segment[] sinks;
	
	int segmentCount;
	int workerCount;
	
	ArrayList<M4_Segment> changedSegments = new ArrayList<M4_Segment>();
	
	// costs[x][y] = costs( x => y )
	double[][] edgeCosts;
	
	boolean feasible = false;
	
	boolean needRecalc = true;
	
	HungarianAlgorithm alg;
	int[] matching;
	
	boolean shifts = true;
	
	Matching_ShiftFixer fixer;
	
	public AreaFlowProblemHungarian(M4_CFMN_Hungarian p, M4_Area a)
	{
		parent = p;
		this.area = a;
		
		this.shifts = a.planning.shifts;
		
		if(shifts)
		{
			fixer = new Matching_ShiftFixer(a,this);
		}
	}
	
	public void addSegment(M4_Segment seg)
	{
		if(seg.local_Id != -1)
		{
			throw new Error("Trying to insert at "+segments.size()+" segment with id"+seg.local_Id+" is already set! "+seg);
		}
		
		seg.local_Id = segments.size();
		segments.add(seg);
		
		this.changedSegments.add(seg);
	}
	
	public void registerChanged(M4_Segment seg)
	{
		
		if(seg.match_changed)
		{
//			System.out.println(" already Registered changed: "+seg);
			return;
		}
		
//		System.out.println(" Registered changed: "+seg);
		if(seg.area != this.area)
			throw new Error("This area is not supported by this Flow Problem!");
		
		this.needRecalc = true;
		
		seg.match_changed = true;
		
		this.changedSegments.add(seg);
	}
	
	public void solve()
	{
		if(segmentCount == 0)
		{
			this.feasible = true;
			return;
		}
		
//		if(!needRecalc)
//			return;
		
		// Reloads and push the info into the algorithm.
		this.alg.reload();
		
		this.needRecalc = false;
		this.matching = this.alg.execute();
		
//		// Check the solution:
//		this.feasible = true;
//		for(int i = 0 ; i < this.segmentCount ; i++)
//		{
//			M4_Segment seg = this.segments.get(i);
//			
//			if(seg.isPlanned())
//			{
//				final int seg1Id = seg.local_Id;
//				final int matchedWith = this.matching[seg1Id];
//				if( matchedWith == -1 || edgeCosts[seg1Id][matchedWith] == NoEdge)
//				{
//					feasible = false;
//					break;
//				}
//			}
//		}
	}
	
	public void backtrack()
	{
		for(M4_Segment seg1 : changedSegments)
		{
			seg1.match_changed = false;
		}
		
		// Clear backtracking;
		this.changedSegments.clear();
	}
	
	public void acceptChanges()
	{
		for(M4_Segment seg1 : changedSegments)
		{
			seg1.match_changed = false;
		}
		
		// Clear backtracking;
		this.changedSegments.clear();
	}
	
	public void updateAllSegments2()
	{
		// TODO: Add same PRM bonus, this is for checking
		if(this.segmentCount == 0)
		{
			return;
		}
		
		for(int i = 0 ; i < edgeCosts.length ; i++)
		{
			M4_Segment seg1 = (i < segmentCount ? this.segments.get(i)
												: this.sources[i-segmentCount] );
			
			if(seg1 instanceof M4_PRM_Segment && seg1.isPlannedAndPrimary())
			{
				M4_PRM_Segment prm_seg1 = (M4_PRM_Segment)seg1;
				if(prm_seg1.isMerged())
				{
					prm_seg1.getMergedWith().local_Id = seg1.local_Id;
				}
			}
			
			for(int j = 0 ; j < this.edgeCosts.length ; j++)
			{
				if(i >= this.segmentCount && j >= this.segmentCount)
				{
					if(i == j)
					{
						
						this.edgeCosts[i][j] = 0;
					}
					else
					{
						this.edgeCosts[i][j] = NoEdge;
					}
				}
				else if(i == j)
				{
					if(seg1.isPlannedAndPrimary())
						this.edgeCosts[i][j] = NoEdge;
					else
						this.edgeCosts[i][j] = 0;
				}
				else
				{
					M4_Segment seg2 = (j < segmentCount ? this.segments.get(j)
														: this.sinks[j-segmentCount] );
					if(seg2.isPlannedAndPrimary())
					{
						if(seg1.isPlannedAndPrimary())
						{
							if(	seg1 instanceof M4_PRM_Segment && seg2 instanceof M4_PRM_Segment &&
								((M4_PRM_Segment)seg1).prm == ((M4_PRM_Segment)seg2).prm && 
								seg1.getEndTime() == seg2.getStartTime())
							{
								this.edgeCosts[i][j] = samePRMbonus;
							}
							else
							{
								final int slack = seg2.getStartTime() - seg1.earliestArriveAt(seg2.segment.from);
								if(slack >= 0)
								{
									this.edgeCosts[i][j] = M4_CostModel.getPosRobustnessPanelty(slack) + planBonus;
								}
								else
								{
									this.edgeCosts[i][j] = NoEdge;
								}
							}
						}
						else
						{
							this.edgeCosts[i][j] = NoEdge;
						}
					}
					else if(seg1.isPlannedAndPrimary())
					{
						this.edgeCosts[i][j] = NoEdge;
					}
					else
					{
						this.edgeCosts[i][j] = 0;
					}
				}
			}
		}
		
//		for(int i = segmentCount ; i < this.edgeCosts.length ; i++)
//		{
//			if(edgeCosts[i][i] != 0)
//			{
//				throw new Error("Why? "+i+" "+edgeCosts[i][i]);
//			}
//		}
	}
	
	public void updateAllSegments()
	{
//		System.out.println("UPdate all!");
		if(segmentCount == 0)
		{
			return;
		}
		
		for(M4_Segment seg1 : this.segments)
		{
			//seg1.match_changed = false;
			boolean schedule = seg1.isPlanned();
			
			if(schedule && seg1 instanceof M4_PRM_Segment)
			{
				final M4_PRM_Segment prm_seg = (M4_PRM_Segment)seg1;
				
				if(prm_seg.isMerged())
				{
					final M4_MergedSegment merged = prm_seg.getMergedWith();
					final M4_PRM_Segment firstMerge = merged.getFirstMerge();
					if(firstMerge != seg1)
					{
						schedule = false;
					}
					else
					{ // Mirror local_Id
						//System.out.println("Mirror local Id: "+merged+" "+seg1);
						merged.local_Id = seg1.local_Id;
					}
				}
			}
			
			if(seg1.isPlannedAndPrimary())
			{ // Enabling the segment
				final Segment pd_seg1 = seg1.segment;
				
				this.edgeCosts[seg1.local_Id][seg1.local_Id] = NoEdge;
				
				// Check the sources
				for(M4_Segment seg2 : this.sources)
				{	
					final int slack21 = seg1.getStartTime() - seg2.earliestArriveAt(pd_seg1.from);
					
					if(slack21 >= 0)
					{
						edgeCosts[seg2.local_Id][seg1.local_Id] = M4_CostModel.getPosRobustnessPanelty(slack21) + planBonus;
					}
				}
				
				// Check the sinks
				for(M4_Segment seg2 : this.sinks)
				{
					final Segment pd_seg2 = seg2.segment;
					
					final int slack12 = seg2.getStartTime() - seg1.earliestArriveAt(pd_seg2.from);
					
					if(slack12 >= 0)
					{
						edgeCosts[seg1.local_Id][seg2.local_Id] = M4_CostModel.getPosRobustnessPanelty(slack12) + planBonus;
					}
				}
				
				
				for(M4_Segment seg2 : this.segments)
				{
					if(seg1 != seg2)
					{
						if(seg2.isPlannedAndPrimary())
						{
							boolean go = true;
							
							if(seg1 instanceof M4_PRM_Segment && seg2 instanceof M4_PRM_Segment)
							{
								M4_PRM_Segment prm_seg1 = (M4_PRM_Segment) seg1;
								M4_PRM_Segment prm_seg2 = (M4_PRM_Segment) seg2;
								
								if(prm_seg1.prm == prm_seg2.prm)
								{
									if(prm_seg1.getEndTime() == prm_seg2.getStartTime())
									{
//										System.out.println(" Same prm "+seg1.local_Id+" => "+seg2.local_Id+" "+seg1.getStartTime() + " "+seg2.getStartTime()+" ? "+seg1.getEndTime());
										edgeCosts[seg1.local_Id][seg2.local_Id] = samePRMbonus;
										edgeCosts[seg2.local_Id][seg1.local_Id] = NoEdge;
										//continue;
										go = false;
									}
								}
							}
							
							if(go)
							{
								final Segment pd_seg2 = seg2.segment;	
								final int slack12 = seg2.getStartTime() - seg1.earliestArriveAt(pd_seg2.from);
								
								if(slack12 >= 0)
								{
									edgeCosts[seg1.local_Id][seg2.local_Id] = M4_CostModel.getPosRobustnessPanelty(slack12) + planBonus;
									edgeCosts[seg2.local_Id][seg1.local_Id] = NoEdge;
								}
							}
						}
						else
						{
							edgeCosts[seg1.local_Id][seg2.local_Id] = NoEdge;
							edgeCosts[seg2.local_Id][seg1.local_Id] = NoEdge;
						}
					}
				}
			}
			else
			{
				for(int i = 0 ; i < edgeCosts.length ; i++)
				{
					M4_Segment seg2 = (i < this.segmentCount	? this.segments.get(i) : this.sources[i - segmentCount]);
					
					if(seg2.isPlannedAndPrimary())
					{
						edgeCosts[i][seg1.local_Id] = NoEdge;
						edgeCosts[seg1.local_Id][i] = NoEdge;
					}
					else
					{	
						edgeCosts[i][seg1.local_Id] = 0;
						edgeCosts[seg1.local_Id][i] = 0;
					}
				}
			}
		}
		
		// Clear, now all changes are propegated.
		//this.changedSegments.clear();
	}
	
	public void updateLPbacktrackable()
	{
		if(true)
			throw new Error("There might be something wrong here!");
		
		if(segmentCount == 0)
		{
			return;
		}
		
		for(M4_Segment seg1 : changedSegments)
		{
			//seg1.match_changed = false;
			boolean schedule = seg1.isPlanned();
			
			if(schedule && seg1 instanceof M4_PRM_Segment)
			{
				final M4_PRM_Segment prm_seg = (M4_PRM_Segment)seg1;
				
				if(prm_seg.isMerged())
				{
					final M4_MergedSegment merged = prm_seg.getMergedWith();
					final M4_PRM_Segment firstMerge = merged.getFirstMerge();
					if(firstMerge != seg1)
					{
						schedule = false;
					}
					else
					{ // Mirror local_Id
						merged.local_Id = seg1.local_Id;
					}
				}
			}
			
			if(schedule)
			{ // Enabling the segment
				final Segment pd_seg1 = seg1.segment;
				
				this.edgeCosts[seg1.local_Id][seg1.local_Id] = NoEdge;
				
				// Check the sources
				for(M4_Segment seg2 : this.sources)
				{	
					final int slack21 = seg1.getStartTime() - seg2.earliestArriveAt(pd_seg1.from);
					
					if(slack21 >= 0)
					{
						edgeCosts[seg2.local_Id][seg1.local_Id] = M4_CostModel.getPosRobustnessPanelty(slack21) + planBonus;
					}
				}
				
				// Check the sinks
				for(M4_Segment seg2 : this.sinks)
				{
					final Segment pd_seg2 = seg2.segment;
					
					final int slack12 = seg2.getStartTime() - seg1.earliestArriveAt(pd_seg2.from);
					
					if(slack12 >= 0)
					{
						edgeCosts[seg1.local_Id][seg2.local_Id] = M4_CostModel.getPosRobustnessPanelty(slack12) + planBonus;
					}
				}
				
				
				for(M4_Segment seg2 : this.segments)
				{
					if(seg1 != seg2)
					{
						if(seg2.isPlanned())
						{
							if(seg1 instanceof M4_PRM_Segment && seg2 instanceof M4_PRM_Segment)
							{
								M4_PRM_Segment prm_seg1 = (M4_PRM_Segment) seg1;
								M4_PRM_Segment prm_seg2 = (M4_PRM_Segment) seg2;
								
								if(prm_seg1.prm == prm_seg2.prm)
								{
									if(prm_seg1.getEndTime() == prm_seg2.getStartTime())
									{
//										System.out.println(" Same prm "+seg1.local_Id+" => "+seg2.local_Id+" "+seg1.getStartTime() + " "+seg2.getStartTime()+" ? "+seg1.getEndTime());
										edgeCosts[seg1.local_Id][seg2.local_Id] = samePRMbonus;
										edgeCosts[seg2.local_Id][seg1.local_Id] = NoEdge;
										continue;
									}
									else if(prm_seg2.getEndTime() == prm_seg1.getStartTime())
									{
//										System.out.println(" Same prm "+seg2.local_Id+" => "+seg1.local_Id+" "+seg2.getStartTime() + " "+seg1.getStartTime());
										edgeCosts[seg2.local_Id][seg1.local_Id] = samePRMbonus;
										edgeCosts[seg1.local_Id][seg2.local_Id] = NoEdge;
										continue;
									}
								}
								
							}
							
							final Segment pd_seg2 = seg2.segment;
							
							final int slack12 = seg2.getStartTime() - seg1.earliestArriveAt(pd_seg2.from);
							
							if(slack12 >= 0)
							{
								edgeCosts[seg1.local_Id][seg2.local_Id] = M4_CostModel.getPosRobustnessPanelty(slack12) + planBonus;
								edgeCosts[seg2.local_Id][seg1.local_Id] = NoEdge;
							}
							else
							{
								final int slack21 = seg1.getStartTime() - seg2.earliestArriveAt(pd_seg1.from);
								
								if(slack21 >= 0)
								{
									edgeCosts[seg1.local_Id][seg2.local_Id] = NoEdge;
									edgeCosts[seg2.local_Id][seg1.local_Id] = M4_CostModel.getPosRobustnessPanelty(slack21) + planBonus;
								}
								else
								{
									edgeCosts[seg1.local_Id][seg2.local_Id] = NoEdge;
									edgeCosts[seg2.local_Id][seg1.local_Id] = NoEdge;
								}
							}	
						}
						else
						{
							edgeCosts[seg1.local_Id][seg2.local_Id] = NoEdge;
							edgeCosts[seg2.local_Id][seg1.local_Id] = NoEdge;
						}
					}
				}
			}
			else
			{
				for(int i = 0 ; i < edgeCosts.length ; i++)
				{
					M4_Segment seg2 = (i < this.segmentCount	? this.segments.get(i) : this.sources[i - segmentCount]);
					
					if(seg2.isPlanned())
					{
						edgeCosts[i][seg1.local_Id] = NoEdge;
						edgeCosts[seg1.local_Id][i] = NoEdge;
					}
					else
					{
						edgeCosts[i][seg1.local_Id] = 0;
						edgeCosts[seg1.local_Id][i] = 0;
					}
				}
			}
		}
		
		// Clear, now all changes are propegated.
		//this.changedSegments.clear();
	}
	
	public void debug()
	{
		System.out.println("Debugging matching area: "+area.getArea().id);
		System.out.println("Active segments:");
		for(M4_Segment seg : this.segments)
		{
			if(seg.isPlanned())
			{
				System.out.println(" "+seg.local_Id+": "+seg+" "+seg.isPlannedAndPrimary());
			}
		}
		
		System.out.println("Active edges:");
		for(int i = 0 ; i < edgeCosts.length ; i++)
		{
			M4_Segment seg1 = ( i < this.segmentCount 	? this.segments.get(i).getPlannableSegment()
					: this.sources[i - this.segmentCount] );
			
			if(seg1.isPlanned())
			{
					for(int j = 0 ; j < edgeCosts.length ; j++)
					{
						if(edgeCosts[i][j] != NoEdge)
						{
							System.out.println(" Edge "+i+" => "+j+" costs: "+edgeCosts[i][j]+" Alg internal: "+alg.costMatrix[i][j]);
						}
					}
			}
		}
		
		System.out.println("Matches:");
		for(int i = 0 ; i < this.edgeCosts.length ; i++)
		{
			M4_Segment seg1 = ( i < this.segmentCount 	? this.segments.get(i).getPlannableSegment()
														: this.sources[i - this.segmentCount] );
			
			if(seg1.isPlanned())
			{
					final int matchId = this.matching[i];
					System.out.println("Match:"+i+" => "+matchId+" / "+segmentCount+" costs: "+edgeCosts[seg1.local_Id][matchId]+
										" Alg internal : "+alg.costMatrix[i][matchId]);
					if(matchId == -1 || this.edgeCosts[i][matchId] == NoEdge)
					{
						System.out.println(" infeasible!");
					}
			}
		}
		
	}
	
	public void initialiseEdges()
	{
		// Make source and sink;
		final Area pd_area = this.area.getArea();
		
		this.segmentCount = this.segments.size();
		this.workerCount = this.area.workers.size();
		final int matrixSize = segmentCount + workerCount;
		
		sources = new M4_Segment[workerCount];
		sinks = new M4_Segment[workerCount];
		
		System.out.println("Matrix size: "+matrixSize);
		
		if(segmentCount == 0)
			return;
		
		this.edgeCosts = new double[matrixSize][matrixSize];
		
		// By default no Edges:
		for(double[] a : this.edgeCosts)
		{
			Arrays.fill(a, NoEdge);
		}
		
		for(int i = this.segmentCount ; i < edgeCosts.length ; i++)
		{
			this.edgeCosts[i][i] = 0;
		}
		
		int w_index = 0;
		for( M4_Worker w : this.area.workers)
		{
			M4_Segment start = w.start;
			M4_Segment end = w.end;
			
			this.sources[w_index] = start;
			this.sinks[w_index]	= end;
			
			final int id = segmentCount + w_index;
			start.local_Id 	= id;
			end.local_Id 	= id;
			w_index++;
			
			this.edgeCosts[id][id] = 0; 
		}
		
		this.alg = new HungarianAlgorithm(this.edgeCosts);
		
//		//Debug check
//		for(int i = 0 ; i < segmentCount ; i++)
//		{
//			M4_Segment seg = this.segments.get(i);
//			if(seg.local_Id != i)
//			{
//				throw new Error(" Local Id incorrect!: "+seg.local_Id+"/"+i);
//			}
//		}
//		for(int i = 0 ; i < this.sources.length ; i++)
//		{
//			final int rId = this.segmentCount + i;
//			M4_Segment sink = sinks[i];
//			M4_Segment source = sources[i];
//			
//			if(sink.local_Id != rId)
//			{
//				throw new Error(" Local Id incorrect!: "+source.local_Id+"/"+i);
//			}
//			if(sink.local_Id != rId)
//			{
//				throw new Error(" Local Id incorrect!: "+source.local_Id+"/"+i);
//			}
//		}
	}
	
	public void convertToSollution() throws CantPlanException
	{	
		if(segmentCount == 0)
		{
			this.feasible = true;
			return;
		}
		
//		System.out.println("Debug: Edges:");
//		for(int i = 0 ; i < edgeCosts.length ; i++)
//		{
//			M4_Segment seg1 = ( i < this.segmentCount 	? this.segments.get(i).getPlannableSegment()
//					: this.sources[i - this.segmentCount] );
//			
//			if(seg1.isPlanned())
//			{
//					for(int j = 0 ; j < edgeCosts.length ; j++)
//					{
//						if(edgeCosts[i][j] != NoEdge)
//						{
//							System.out.println(" Edge "+i+" => "+j+" costs: "+edgeCosts[i][j]+" Alg internal: "+alg.costMatrix[i][j]);
//						}
//					}
//			}
//		}
		
//		System.out.println("Choosen edges area "+area.getArea().id+":");
		this.feasible = true;
		for(int i = 0 ; i < this.edgeCosts.length ; i++)
		{
			M4_Segment seg1 = ( i < this.segmentCount 	? this.segments.get(i)
														: this.sources[i - this.segmentCount] );
			
			if(seg1.isPlannedAndPrimary())
			{
					seg1 = seg1.getPlannableSegment();
					final int matchId = this.matching[i];

					if(matchId == -1 || this.edgeCosts[i][matchId] == NoEdge)
					{
						this.feasible = false;
//						System.out.println("Infeasibility found! "+i+" "+matchId);
						//continue;
						return;
					}
//					System.out.println("Match: seg id "+seg1.id+" "+i+" => "+matchId+" / "+segmentCount+" costs: "+edgeCosts[i][matchId]+
//							" Alg internal : "+alg.costMatrix[i][matchId]);
					
					M4_Segment seg2 = (matchId < this.segmentCount 	? this.segments.get(matchId).getPlannableSegment()
																	: this.sinks[matchId - this.segmentCount]);
					
					if(seg1 == seg2 || seg2.getStartTime() < seg1.getStartTime())
					{
						throw new Error("Invalid start:\n"+
										" match "+i+" => "+matchId+"\n"+
										" costs: "+this.edgeCosts[i][matchId]+" internal: "+alg.costMatrix[i][matchId]+"\n"+
										" r-costs: "+this.edgeCosts[matchId][i]+" internal: "+alg.costMatrix[matchId][i]+"\n"+
										" "+seg1.getStartTime()+" > "+seg2.getStartTime()+"\n"+
										" seg: "+seg1+" "+seg2+" planned and primary : "+seg1.isPlannedAndPrimary()+"|"+seg2.isPlannedAndPrimary()+"\n"+
										" Test: "+seg1.earliestArriveAt(seg2.segment.from)+" "+seg2.getStartTime()+"\n"+
										" last mutation: "+this.parent.solver.lastMutation);
										
					}
					seg1.setNext(seg2);
			}
			else if(i >= this.segmentCount)
			{
				throw new Error(" Apearently a start segment is unplanned: "+i+" "+this.sources[i-segmentCount]);
			}
		}
		
		
		// Consider shift ends
		if(shifts)
		{
			// find the new lasts.
			for(M4_Worker w : this.area.workers)
			{
				M4_Segment prev = w.start;
				M4_Segment seg = prev.getNext();
				
				while(seg != null)
				{
					prev = seg;
					seg = prev.getNext();
				}
				
				w.newEnd = prev;
			}
			
			if(!this.fixer.fix())
			{
				this.feasible = false;
			}
		}
		else
		{
			for(M4_Worker w : this.area.workers)
			{
				M4_Segment prev = w.start;
				M4_Segment seg = w.start.getNext();
				
				int count = this.segments.size()+2;
				while(seg != null)
				{
					//System.out.println("Setting segment: "+seg.local_Id+" start: "+seg.getStartTime()+" to worker "+w+" start: "+w.start.local_Id);
					if(count <= 0)
					{
						feasible = false;
						throw new Error("Loop detected!");
					}
					
					if(!(seg instanceof M4_WorkerSegment))
					{
						seg.setWorker(w);
					}
					
					prev = seg;
					seg = seg.getNext();
					
					count--;
				}
				
				if(prev != w.end)
				{ // Make sure the schedule ends!
					
					final M4_Segment prev1 = prev.getPrevious();
					final M4_Segment prev2 = w.end.getPrevious();
					prev1.setNext(w.end);
					prev2.setNext(prev);
				}
				
				// Debug:
//				w.checkConsistancy();
			}
		}
		
		// Make sure the workers are correct
		//System.out.println("Repairing workers:");
		
		
		//System.out.println("Done repairing workers");
		
	}
	
	public boolean isFeasible() 
	{
		return this.feasible;
	}
	
	public class Node
	{
		M4_Segment rep;
		ArrayList<Edge2> IncommingEdges = new ArrayList<Edge2>();
		ArrayList<Edge2> OutGoingEdges = new ArrayList<Edge2>();
		
		Edge2 incomming;
		
		boolean scheduled = false;
	}
	
	public class Edge2
	{
		boolean hasFlow = false;
		Node from;
		Node to;
		double weight;
	}

	@Override
	public double getCosts(M4_Segment from, M4_Segment to) 
	{
		return this.edgeCosts[from.local_Id][to.local_Id];
	}

	@Override
	public double getNoEdgeValue() 
	{
		return NoEdge;
	}
}
