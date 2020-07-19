package prm.ls.model4.matching.lpflow;

import java.util.ArrayList;
import java.util.TreeMap;

import prm.ls.LocalSearchListener;
import prm.ls.NeedToSetSolver;
import prm.ls.SimulatedAnnealing;
import prm.ls.SolutionFacture;
import prm.problemdef.*;
import prm.ls.model4.*;
import prm.ls.model4.matching.M4_MatchingAlgorithm;

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
public class M4_CFMN_Hungarian implements M4_MatchingAlgorithm, LocalSearchListener, NeedToSetSolver
{
	M4_Planning planning;
	SimulatedAnnealing solver;
	TreeMap<Area,AreaFlowProblemHungarian> areas = new TreeMap<Area,AreaFlowProblemHungarian>();
	
	public M4_CFMN_Hungarian(M4_Planning p)
	{
		planning = p;
	}
	
	public void setAnnealer(SimulatedAnnealing sa)
	{
		solver = sa;
		sa.addLocalSearchListener(this);
	}
	
	public AreaFlowProblemHungarian getAFP(M4_Area a)
	{
		return areas.get(a.getArea());
	}
	
	@Override
	public void acceptSuggestion() 
	{
		
	}

	@Override
	public void cancelSuggestion() 
	{
		
	}

	@Override
	public void clear() 
	{
		
	}

	@Override
	public void generateSuggestion(M4_SegmentGroup<?> sg, SolutionFacture f) 
	{
		sg.setPlanned(true);
		
		for(M4_Segment seg : sg)
		{
			seg.getPlannableSegment().setPlanned(true);
			AreaFlowProblemHungarian afp = this.getAFP(seg.area);
			
			if(seg instanceof M4_MergedSegment)
			{
				for(M4_PRM_Segment prmSeg : ((M4_MergedSegment)seg))
				{
					afp.registerChanged(prmSeg);
				}
			}
			else
			{
				afp.registerChanged(seg);
			}
			
			
		}
	}

	@Override
	public void registerUnplan(M4_SegmentGroup<?> sg) 
	{
		for(M4_Segment seg : sg)
		{
			AreaFlowProblemHungarian afp = this.getAFP(seg.area);
			seg.setPlanned(false);
			
			if(seg instanceof M4_MergedSegment)
			{
				for(M4_PRM_Segment prmSeg : ((M4_MergedSegment)seg))
				{
					afp.registerChanged(prmSeg);
				}
			}
			else
			{
				afp.registerChanged(seg);
			}
		}
	}

	@Override
	public void onAccept(SimulatedAnnealing anealer) 
	{
//		System.out.println(" CFMN_H  onAccept");
		
		for(AreaFlowProblemHungarian afp : this.areas.values())
		{
			afp.acceptChanges();
		}
	}

	@Override
	public void onFinish(SimulatedAnnealing anealer) 
	{
		
	}

	@Override
	public void onInitialise(SimulatedAnnealing anealer) 
	{
		// Create all Areas:
		for(M4_Area a : this.planning.areas.values())
		{
			areas.put(a.getArea(), new AreaFlowProblemHungarian(this,a));
		}
		
		// Create all Segments
		for(M4_PRM prm : this.planning.prms)
		{
			for(M4_Segment s : prm.segments)
			{
				AreaFlowProblemHungarian afp = this.getAFP(s.area);
				afp.addSegment(s);
			}
		}
		
		// Initialise the edges
		for(AreaFlowProblemHungarian afp : this.areas.values())
		{
			afp.initialiseEdges();
		}
		
	}

	@Override
	public void onReject(SimulatedAnnealing anealer) 
	{
//		System.out.println(" CFMN_H  onAccept");
		for(AreaFlowProblemHungarian afp : this.areas.values())
		{
			afp.backtrack();
		}
	}

	@Override
	public void postProcessFacture(SimulatedAnnealing anealer, SolutionFacture f)
	{
		if(!f.feasible)
		{ // Speed up why solving if its infeasible anyways.
			return;
		}
		
//		System.out.println(" Replan All Updated");
//*DEBUG*/		this.planning.testConsistancy();
		
//		long start = System.currentTimeMillis();
		for(AreaFlowProblemHungarian afp : this.areas.values())
		{
			if(afp.needRecalc)
			{
//				long start = System.currentTimeMillis();
				afp.updateAllSegments2();
				afp.solve();
				try 
				{
					afp.convertToSollution();
				} 
				catch (CantPlanException e) 
				{
					f.feasible = false;
				}
				
//				System.out.println("End solving: "+(System.currentTimeMillis()-start)+" feasible: "+afp.isFeasible());
				
				if(!afp.isFeasible())
				{
					f.feasible = false;
					//afp.debug();
					return;
//					throw new Error("sigh: Area: "+afp.area+" "+afp.area.workers.size());
				}
			}
		}
//		throw new Error("Good");
//		System.out.println(" Solved in: "+(System.currentTimeMillis() - start));
		
///*DEBUG*/		this.planning.testConsistancy();
	}
	
	@Override
	public M4_CFMN_Hungarian newForMutation()
	{
		return this;
	}

	@Override
	public void setSA(SimulatedAnnealing sa) 
	{
		System.out.println("Solver set!");
		this.setAnnealer(sa);
	}

	@Override
	public void planSegment(M4_Segment seg, SolutionFacture f) 
	{
		throw new Error("NYI");
		
	}

}
