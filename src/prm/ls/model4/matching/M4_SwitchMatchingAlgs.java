package prm.ls.model4.matching;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import prm.ls.NeedToSetSolver;
import prm.ls.SimulatedAnnealing;
import prm.ls.SolutionFacture;
import prm.ls.model4.*;
import prm.problemdef.Area;
import prm.problemdef.LocationAlias;
import prm.problemdef.Segment;
import prm.problemdef.Transport;

import otheralgorithms.HungarianAlgorithm;
import otheralgorithms.HungarianAlgorithmResizable;

import lpsolve.*;

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
 * Simplest form of matching but not the best. There are smarter options available,
 *  this method is to test and debug.
 * 
 * @author rene
 *
 */
public class M4_SwitchMatchingAlgs implements M4_MatchingAlgorithm, NeedToSetSolver
{	
	int timeTresshold;
	
	final M4_MatchingAlgorithm m1;
	final M4_MatchingAlgorithm m2;
	
	boolean m1Updated = false;
	boolean m2Updated = false;
	
	public M4_SwitchMatchingAlgs(M4_MatchingAlgorithm m1, M4_MatchingAlgorithm m2, int tresshold)
	{
		this.m1 = m1;
		this.m2 = m2;
		
		this.timeTresshold = tresshold;
	}
	
	@Override
	public void acceptSuggestion() 
	{
		if(m1Updated)
			m1.acceptSuggestion();
		
		if(m2Updated)
			m2.acceptSuggestion();
		
		m1Updated = false;
		m2Updated = false;
	}

	@Override
	public void cancelSuggestion() 
	{ 
		if(m1Updated)
			m1.cancelSuggestion();
		
		if(m2Updated)
			m2.cancelSuggestion();
		
		m1Updated = false;
		m2Updated = false;
	}

	@Override
	public void generateSuggestion(M4_SegmentGroup<?> sg, SolutionFacture f) throws CantPlanException
	{	
		sg.setPlanned(true);
		
		for(M4_Segment seg : sg)
		{	
			if(!f.feasible)
			{
				return;
			}
			
			if(!seg.isPlanned())
			{
				//seg = seg.getPlannableSegment();
				this.planSegment(seg, f);
			}
		}
	}
	
	@Override
	public void planSegment(M4_Segment seg, SolutionFacture f) throws CantPlanException
	{	
		if(seg.getStartTime() <= timeTresshold)
		{
			m1.planSegment(seg, f);
			this.m1Updated = true;
		}
		else
		{
			m2.planSegment(seg, f);
			this.m2Updated = true;
		}
	}

	@Override
	public void clear() 
	{
		this.m1.clear();
		this.m2.clear();
	}

	@Override
	public M4_MatchingAlgorithm newForMutation() 
	{
		M4_MatchingAlgorithm m1_new = m1.newForMutation();
		M4_MatchingAlgorithm m2_new = m2.newForMutation();
		
		if(m1 == m1_new && m2 == m2_new)
			return this;
		
		return new M4_SwitchMatchingAlgs(m1_new,m2_new,this.timeTresshold);
	}

	@Override
	public void registerUnplan(M4_SegmentGroup<?> sg) 
	{
		m1.registerUnplan(sg);
		m2.registerUnplan(sg);
		
		m1Updated = true;
		m2Updated = true;
	}

	@Override
	public void setSA(SimulatedAnnealing sa) 
	{
		if(m1 instanceof NeedToSetSolver)
		{
			((NeedToSetSolver)m1).setSA(sa);
		}
		if(m2 instanceof NeedToSetSolver)
		{
			((NeedToSetSolver)m2).setSA(sa);
		}
	}
}
