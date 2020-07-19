package prm.ls.model4.matching.lpflow;

import gurobi.GRB;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;

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
import prm.problemdef.Area;
import prm.problemdef.Segment;

import rpvt.lp.LpModelAbstr;
import rpvt.lp.VariableInterface;
import rpvt.lp.gurobi.*;

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
 * @author rene
 */

public class AreaFlowProblem_ILP3 implements AreaFlowProblem_IF
{
	double M = 24*60*60;
	
	final static double planBonus = -1;
	final static double unplanPanelty = 5;
	final static double samePRMbonus = -1000;
	
	boolean needRecalc = true;
	protected LpModel model;
	
	private M4_Area area;
	//private Area area;
	
	int modelSegments = 0;
	ArrayList<M4_Segment> segments = new ArrayList<M4_Segment>();
	
	ArrayList<M4_Segment> changedSegments = new ArrayList<M4_Segment>();
	
	Edge[][] edgeMatrix;
	//Edge[] edges;
	
	boolean feasible = false;
	
	int segmentCount;
	
	boolean shifts = true;
	
	public AreaFlowProblem_ILP3(M4_Area a)
	{
		this.area = a;
		
		this.shifts = a.planning.shifts;
	}
	
	public void addSegment(M4_Segment seg)
	{
		seg.local_Id = segments.size();
		segments.add(seg);
		this.changedSegments.add(seg);
	}
	
	public void registerChanged(M4_Segment seg)
	{
		if(seg.match_changed)
			return;
		
		if(seg.area != this.area)
			throw new Error("This area is not supported by this Flow Problem!");
		
		this.needRecalc = true;
		
		seg.match_changed = true;
		
		this.changedSegments.add(seg);
	}
	
	public void solve()
	{
		if(!needRecalc)
			return;
		
		this.needRecalc = false;
		try 
		{
//			System.out.println("Start solving!");
//			long time_Start = System.currentTimeMillis();
			
			
			//PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
			//System.setOut(out);
			//this.model.solver.setOutputfile("lp_output");
			//this.model.solver.printLp();
			//out.close();
			
			//model.solver.resetBasis();
			this.model.solve();
//			System.out.println("End solving area "+this.area.getArea().id+" time: "+(System.currentTimeMillis() - time_Start)+" ms"+
//								" feasible : "+this.model.isFeasible()+" sollution: ?");
			//throw new Error("?");
//			
		} 
		catch (Exception e) 
		{
			throw new Error(e);
		}
	}
	
	public void backtrack()
	{
		// TODO: This might messes up stacking changes.
		for(M4_Segment seg1 : changedSegments)
		{
			seg1.match_changed = false;
		}
		
		// Clear backtracking;
		this.changedSegments.clear();
		// Do nothing
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
	
	public void setCosts(int i, int j, double c)
	{
		final Edge e = this.edgeMatrix[i][j];
		if(e != null)
		{
			e.setActivated(true);
			e.setCosts(c);
		}
	}
	
	public void disableEdge(int i, int j)
	{
		final Edge e = this.edgeMatrix[i][j];
		if(e != null)
		{
			e.setActivated(false);
		}
	}
	
	public void updateAllSegments2()
	{
		// TODO: Add same PRM bonus, this is for checking
		if(this.segments.size() == 0)
		{
			return;
		}
		
		for(int i = 0 ; i < segments.size() ; i++)
		{
			M4_Segment seg1 = this.segments.get(i);
			
			if(seg1.isPlannedAndPrimary())
			{
				if(seg1 instanceof M4_PRM_Segment && ((M4_PRM_Segment)seg1).isMerged())
				{
					((M4_PRM_Segment)seg1).getMergedWith().local_Id = seg1.local_Id;
				}
				
				if(seg1.c1 != null)
					seg1.c1.setB(1);
				if(seg1.c2 != null)
					seg1.c2.setB(1);
			}
			else
			{
				if(seg1.c1 != null)
					seg1.c1.setB(0);
				if(seg1.c2 != null)
					seg1.c2.setB(0);
			}
			
			for(int j = 0 ; j < segments.size() ; j++)
			{
				if(i >= this.segmentCount && j >= this.segmentCount)
				{
//					if(i == j)
//					{
//						setCosts(i,j,0);
//					}
				}
				else if(i == j)
				{
//					if(!seg1.isPlannedAndPrimary())
//						setCosts(i,j,0);
				}
				else if(seg1.isPlannedAndPrimary())
				{
					M4_Segment seg2 = this.segments.get(j);
					if(seg2.isPlannedAndPrimary())
					{
						if(seg1 instanceof M4_PRM_Segment && seg2 instanceof M4_PRM_Segment &&
							((M4_PRM_Segment)seg1).prm == ((M4_PRM_Segment)seg2).prm	&&
							seg1.getEndTime() == seg2.getStartTime()	)
						{
							setCosts(i,j, samePRMbonus);
						}
						else
						{
							final int slack = seg2.getStartTime() - seg1.earliestArriveAt(seg2.segment.from);
							if(slack >= 0)
							{
								setCosts(i,j, M4_CostModel.getPosRobustnessPanelty(slack) + planBonus);
							}
							else
							{
								disableEdge(i,j);
							}
						}
						
					}
					else
					{
						disableEdge(i,j);
					}
				}
				else
				{
					disableEdge(i,j);
				}
			}
		}
		
		// Check the worker edges:
		//System.out.println(" Debug:");
		for(Edge[] ea : this.edgeMatrix)
			for(Edge e : ea)
				if(e != null)
				{
					if(e.from instanceof M4_WorkerSegment && e.to instanceof M4_WorkerSegment)
					{
						e.setActivated(true);
						e.setCosts(0);
//						System.out.println("segment: "+((M4_WorkerSegment)e.from).getWorker()+" => "+
//								((M4_WorkerSegment)e.to).getWorker()+": "+e.isActivated());
					}
				}
	}
	
	public void updateLPbacktrackable()
	{
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
				if(seg1.c1 != null)
					seg1.c1.setB(1);
				
				if(seg1.c2 != null)
					seg1.c2.setB(1);
				
				final Segment pd_seg1 = seg1.segment;
				//final int seg1_start = seg1.getStartTime();
				
				// Recalculate every edge;
				for(M4_Segment seg2 : this.segments)
				{
					if(seg1 != seg2)
					{
						final Segment pd_seg2 = seg2.segment;
						
						final Edge e12;
						final Edge e21;
						
						final int slack12;
						final int slack21;
						
						if(seg1.local_Id < seg2.local_Id)
						{
							e12 = this.edgeMatrix[seg1.local_Id][seg2.local_Id];
							e21 = this.edgeMatrix[seg2.local_Id][seg1.local_Id];
							
							slack12 = seg2.getStartTime() - seg1.earliestArriveAt(pd_seg2.from);
							slack21 = seg1.getStartTime() - seg2.earliestArriveAt(pd_seg1.from);
						}
						else
						{
							e12 = this.edgeMatrix[seg2.local_Id][seg1.local_Id];
							e21 = this.edgeMatrix[seg1.local_Id][seg2.local_Id];
							
							slack12 = seg1.getStartTime() - seg2.earliestArriveAt(pd_seg1.from);
							slack21 = seg2.getStartTime() - seg1.earliestArriveAt(pd_seg2.from);
						}
						
						if(e12 != null && e12.bothPlanned())
							if(slack12 >= 0)
							{
//								System.out.println("Activating edge: "+e12.from+" => "+e12.to+" slack: "+slack12);
								e12.setActivated(true);
								e12.setCosts(planBonus + M4_CostModel.getRobustnessPanelty(slack12));
							}
							else
							{
//								System.out.println("Declined edge: "+e12.from+" => "+e12.to+" slack:"+slack12);
								e12.setActivated(false);
								e12.setCosts(unplanPanelty);
							}
						
						if(e21 != null && e21.bothPlanned())
							if(slack21 >= 0)
							{
//								System.out.println("Activating edge: "+e21.from+" => "+e21.to+" slack: "+slack21);
								e21.setActivated(true);
								e21.setCosts(planBonus + M4_CostModel.getRobustnessPanelty(slack21));
							}
							else
							{
//								System.out.println("Declined edge: "+e21.from+" => "+e21.to+" slack:"+slack21);
								e21.setActivated(false);
								e21.setCosts(unplanPanelty);
							}
					}
				}
			}
			else
			{
				// Disabling the segment. Constraints now sais there may not be any incomming and leaving edges.
				if(seg1.c1 != null)
					seg1.c1.setB(0);
				
				if(seg1.c2 != null)
					seg1.c2.setB(0);
			}
		}
		
		// Clear, now all changes are propegated.
	}
	
	public void initialiseEdges()
	{
		final Area pd_area = this.area.getArea();
		
		if(this.model != null)
			throw new Error("There is already a model made, memory overflow is likely!");
		
		this.model = new LpModel();
		this.model.setSilenced(true);
		
		segmentCount = this.segments.size();
		
		for(M4_Worker w : this.area.workers)
		{
			double deadline = w.getTransport().endShift;
			VariableInterface v = model.makeVar(w.start,deadline,deadline,0);
			w.start.endTime = v;
			
//			v.setLowerBound(deadline);
//			v.setUpperBound(deadline);
			
			VariableInterface v2 = model.makeVar(w.end, deadline,deadline,0);
//			v2.setLowerBound(deadline);
//			v2.setUpperBound(deadline);
			w.end.endTime = v2;
		}
		
		// Make 2 constraints for each segment.
		for(M4_Segment seg : this.segments)
		{
			// Departing edge
			seg.c1 = this.model.makeEQConstraint();
			seg.c1.setB(1);
			//seg.c1.name = "seg "+seg.id+" out";
			
			// Incomming edge : Forces that every edge needs to be planned
			seg.c2 = this.model.makeEQConstraint();
			seg.c2.setB(1);
			//seg.c2.name = "seg "+seg.id+" in";
			
			if(this.shifts)
			{	
				VariableInterface v = this.model.makeVar();
				seg.endTime = v;
				
				// Create transport assignment variables.
//				for(M4_Worker w : seg.feasibleTransports)
//				{
//					Variable v = this.model.makeIntVar();
//					v.setUpperBound(1);
//					v.setBinary(true);
//					seg.vars.put(w,v);
//					
//					if(seg.feasibleTransports.size() == 1)
//					{ // There is only one option
//						v.setLowerBound(1);
//					}
//				}
			}
		}
		
		this.modelSegments = this.segments.size();
		
		// Make dummy segments: 
		for( M4_Worker w : this.area.workers)
		{
			final M4_Segment seg = w.start;
			this.addSegment(seg);
			seg.c1 = this.model.makeEQConstraint();
			seg.c1.setB(1);
			//seg.c1.name = "W seg "+seg.id+" out";

			final M4_Segment seg2 = w.end;
			this.addSegment(seg2);
			seg2.c2 = this.model.makeEQConstraint();
			seg2.c2.setB(1);
		}
		
		final int size = segments.size();
		this.edgeMatrix = new Edge[size][size];
		
		for(int i = 0 ; i < segments.size() ; i++)
		{
			M4_Segment seg1 =  this.segments.get(i);
			
			for(int j = 0 ; j < segments.size() ; j++)
			{
				M4_Segment seg2 = this.segments.get(j);
				if(	seg1 != seg2 && 
					seg1.c1 != null && 
					seg2.c2 != null)
				{
					// Check or both segments have overlap
					Segment pd_seg1 = seg1.segment;
					Segment pd_seg2 = seg2.segment;
					
					// Seg1 => Seg2
					if(pd_seg1.getEarliestStart() + pd_seg1.segmentTime + pd_area.getDistance(pd_seg1.to, pd_seg2.from) 
						<=	pd_seg2.getLatestStart() 
						&&  !(seg1 instanceof M4_WorkerSegment && seg1 == seg1.getWorker().end)
						&&  !(seg2 instanceof M4_WorkerSegment && seg2 == seg2.getWorker().start)
//						
							)
					{
						Edge e = new Edge(seg1,seg2,this);
						this.edgeMatrix[i][j] = e;
						
						// Shift constraints
						if(this.shifts)
						{
							// seg2.endTime - M1 * (1-e) <= seg1.endTime
							// seg2.endTime - M1 + M1*e <= seg1.endTime
							// seg2.endTime + M1*e - seg1.endTime <= M1
							Constraint c = model.makeLEConstraint();
							c.setVar(seg1.endTime, -1);
							c.setVar(seg2.endTime, 1);
							c.setVar(e.var,M);
							c.setB(M);
							e.constraints.add(c);
							
							// 2270 + M1 - 0 <= M1
							
							
/*							System.out.println("Create shifts for "+seg1+" "+seg2);
							java.util.Iterator<Entry<M4_Worker, VariableAbstr>> i1 = seg1.vars.entrySet().iterator();
							java.util.Iterator<Entry<M4_Worker, VariableAbstr>> i2 = seg2.vars.entrySet().iterator();
							
							Entry<M4_Worker, VariableAbstr> e1 = (i1.hasNext() ? i1.next() : null);
							Entry<M4_Worker, VariableAbstr> e2 = (i2.hasNext() ? i2.next() : null);
							
							int count = 0;
							while(e1 != null || e2 != null)
							{
								
								count++;
								if(e2 == null || (e1 != null && e1.getKey().compareTo(e2.getKey()) < 0))
								{ // e1 < e2 Transporter e1 can't be in e2;
									Constraint c = model.makeLEConstraint();
									c.setVar(e.var, 1);
									c.setVar(e1.getValue(), 1);
									c.setB(1);
									e.constraints.add(c);
									
									e1 = (i1.hasNext() ? i1.next() : null);
//									System.out.println("Create exluding (X,_): "+e1.getKey().getTransport());
								}
								else if(e1 == null || e1.getKey().compareTo(e2.getKey()) > 0)
								{ // e1 > e2 Transporter e2 can't be in e1
									Constraint c = model.makeLEConstraint();
									c.setVar(e.var, 1);
									c.setVar(e2.getValue(), 1);
									c.setB(1);
									e.constraints.add(c);
									
									e2 = (i2.hasNext() ? i2.next() : null);
//									System.out.println("Create exluding (_,X): "+e2.getKey().getTransport());
								}
								else
								{ // equals They must take one of those: x_{i,j} + v_{i,t} - v_{j,t} <= 1
									Constraint c = model.makeLEConstraint();
									c.setVar(e.var, 1);
									c.setVar(e1.getValue(), 1);
									c.setVar(e2.getValue(), -1);
									c.setB(1);
									e.constraints.add(c);
									
									e1 = (i1.hasNext() ? i1.next() : null);
									e2 = (i2.hasNext() ? i2.next() : null);
//									System.out.println("Create simular (X,X): "+e2.getKey().getTransport());
								}
							}
							/*/
							//System.out.println("Made "+count+"constraints "+seg1.vars.size()+"|"+seg2.vars.size());
						}
						
					}	
				}
			}
		}
		
		// Construct the solver;
		try 
		{
			this.model.constructSolver();
		} 
		catch (Exception e) 
		{
			throw new Error(e);
		}
		
		this.updateAllSegments2();
		this.acceptChanges();
		System.out.println("Done constructing:");
		this.needRecalc = false;
	}
	
	public void convertToSollution() throws CantPlanException
	{
		this.feasible = true;
		
		if(this.segmentCount == 0)
			return;
		
		if(!model.isFeasible())
		{
			this.feasible = false;
			return;
		}
		
		//Debug:
//		System.out.println("Debug!");
//		for(int i = 0 ; i < edgeMatrix.length ; i++)
//			for(int j = 0 ; j < edgeMatrix.length ; j++)
//			{
//				Edge e = this.edgeMatrix[i][j];
//				if(e != null && Math.round(e.var.getValue()) == 1)
//				{
//					System.out.println("- planned robust["+i+"]["+j+"]: "+e.getCosts()+" edge: "+e.from.getPlannableSegment()+" => "+e.to.getPlannableSegment());
//				}
//			}
//		System.out.println("End debug");
		
		// Loose the last parts
		
		for(M4_Worker w : this.area.workers)
		{
			w.end.setPrevious(null);
		}
		
//		System.out.println("Make sollution for area: "+area.getArea().id);
		// Checking sollution:
		for(int i = 0 ; i < this.segments.size() ; i++)
		{
			M4_Segment seg1 = this.segments.get(i);
			
			// Make sure its detached properly
			//seg1.unPlan();
			//seg1.setNext(null);
			
			if(seg1.isPlannedAndPrimary())
			{
				seg1 = seg1.getPlannableSegment();
				
				boolean unmatched = true;
				for(int j = 0 ; j < this.segments.size() ; j++)
				{ // Find a incomming edge
					Edge e = this.edgeMatrix[j][i];
					if(e != null && e.isChosen())
					{
						M4_Segment seg2 = this.segments.get(j).getPlannableSegment();
						unmatched = false;
						//System.out.println(" Link made: "+seg2+" => "+seg1+" costs: "+e.getCosts()+" active: "+e.isActivated()+" value: "+e.var.getValue());
						seg2.setNext(seg1);
						break;
					}
				}
				
				if(!(seg1 instanceof M4_WorkerSegment) && unmatched)
				{
					this.feasible = false;
//					System.out.println("Warning segment["+i+"]: "+seg1+" isn't matched! ");
					
//					if(true)
//						throw new Error("Test");
					return;
				}
			}
		}
		
		// Make sure the workers are correct
		for(M4_Worker w : this.area.workers)
		{
			// DEBUG
//			{
//				M4_Segment seg = w.start;
//				M4_Segment next = w.start.getNext();
//				String s = "Edge var debug "+w+":";
//				while(next != null)
//				{
//					Edge e = this.edgeMatrix[seg.local_Id][next.local_Id];
//					
//					s+= " "+e.from.id+"=>"+e.to.id+"("+e.from.endTime.getValue()+","+e.to.endTime.getValue()+","+e.var.getValue()+")";
//					
//					if(!e.isChosen())
//					{
//						throw new Error("Huh? edge "+e+" is not chozen?"+
//										" debug string: "+s);
//					}
//					
//					seg = next;
//					next = seg.getNext();
//				}
//				
//				System.out.println(s);
//			}
			// DEBUG dir 2
//			{
//				M4_Segment seg = w.end;
//				M4_Segment prev = w.end.getPrevious();
//				String s = "      rev "+w+":";
//				while(prev != null)
//				{
//					Edge e = this.edgeMatrix[prev.local_Id][seg.local_Id];
//					s+= " "+e.from.id+"=>"+e.to.id+"("+e.from.endTime.getValue()+","+e.to.endTime.getValue()+","+e.var.getValue()+")";
//					seg = prev;
//					prev = seg.getPrevious();
//				}
//				
//				System.out.println(s);
//			}
			// DEBUG 3
//			{
//				for(Edge e : edgeMatrix[w.start.local_Id])
//					if(e != null && e.isChosen())
//					{
//						System.out.println(" start edge : "+e.from+" => "+e.to);
//					}
//			}
			
			M4_Segment prev = w.start;
			M4_Segment seg = w.start.getNext();
			
			while(seg != null)
			{
				if(!(seg instanceof M4_WorkerSegment))
				{
					seg.setWorker(w);
				}
				
				prev = seg;
				seg = seg.getNext();
			}
			
			if(prev != w.end)
			{ 
				if(!(prev instanceof M4_WorkerSegment))
				{
					prev.setNext(w.end);
				}
				else if(prev == w.start)
				{
					prev.setNext(w.end);
				}
				else
				{
					prev.getPrevious().setNext(w.end);
				}
				// Make sure the schedule ends!
				// TODO: This algorithm cannot make sure that shift ends are been accounted for.
				
			}
			
//			w.printShift();
		}
		
		// Check consistancy:
		for(M4_Worker w : this.area.workers){ w.checkConsistancy(); }
	}
	
	public boolean isFeasible() 
	{
		return this.feasible;
	}

	@Override
	public boolean needRecalc() 
	{
		return this.needRecalc;
	}

	@Override
	public LpModelAbstr getModel() 
	{
		return this.model;
	}
}
