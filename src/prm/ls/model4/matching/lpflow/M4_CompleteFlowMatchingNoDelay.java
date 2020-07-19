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
public class M4_CompleteFlowMatchingNoDelay implements M4_MatchingAlgorithm, LocalSearchListener, NeedToSetSolver
{
	M4_Planning planning;
	SimulatedAnnealing solver;
	public TreeMap<Area,AreaFlowProblem_IF> areas = new TreeMap<Area,AreaFlowProblem_IF>();
	
	int load_ILP;
	
	public M4_CompleteFlowMatchingNoDelay(M4_Planning p, int ILP)
	{
		planning = p;
		this.load_ILP = ILP;
	}
	
	@Override
	public void planSegment(M4_Segment seg, SolutionFacture f) 
	{
		throw new Error("NYI");
		
	}
	
	public void setAnnealer(SimulatedAnnealing sa)
	{
		solver = sa;
		sa.addLocalSearchListener(this);
	}
	
	public AreaFlowProblem_IF getAFP(M4_Area a)
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
			AreaFlowProblem_IF afp = this.getAFP(seg.area);
			
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
			AreaFlowProblem_IF afp = this.getAFP(seg.area);
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
		for(AreaFlowProblem_IF afp : this.areas.values())
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
			switch(this.load_ILP)
			{
			case 1 : areas.put(a.getArea(), new AreaFlowProblem_ILP1(a)); break;
			case 2 : areas.put(a.getArea(), new AreaFlowProblem_ILP2(a)); break;
			case 3 : areas.put(a.getArea(), new AreaFlowProblem_ILP3(a)); break;
			default: throw new Error(" Unknown ILP");
			}
		}
		
		// Create all Segments
		for(M4_PRM prm : this.planning.prms)
		{
			for(M4_Segment s : prm.segments)
			{
				AreaFlowProblem_IF afp = this.getAFP(s.area);
				afp.addSegment(s);
			}
		}
		
		// Initialise the edges
		for(AreaFlowProblem_IF afp : this.areas.values())
		{
			afp.initialiseEdges();
		}
		
	}

	@Override
	public void onReject(SimulatedAnnealing anealer) 
	{
		for(AreaFlowProblem_IF afp : this.areas.values())
		{
			afp.backtrack();
		}
	}

	@Override
	public void postProcessFacture(SimulatedAnnealing anealer, SolutionFacture f) 
	{
		for(AreaFlowProblem_IF afp : this.areas.values())
		{
//			System.out.println("Updating terminal: "+afp);
			
			if(afp.needRecalc())
			{
				afp.updateAllSegments2();
				afp.solve();
				try
				{
					afp.convertToSollution();
				}
				catch(CantPlanException e)
				{
					f.feasible = false;
				}
			
				if(!afp.isFeasible())
				{
					f.feasible = false;
//					throw new Error("sigh");
					return;
				}
			}
			
		}
		
//		planning.testConsistancy();
//		throw new Error("Good");
	}
	
	@Override
	public M4_CompleteFlowMatchingNoDelay newForMutation()
	{
		return this;
	}

	@Override
	public void setSA(SimulatedAnnealing sa) 
	{
		this.setAnnealer(sa);
	}

}
