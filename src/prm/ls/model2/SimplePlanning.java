package prm.ls.model2;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import prm.ls.*;
import prm.ls.resources.ParalelResource;
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
public class SimplePlanning implements LS_Solution
{
	public TreeMap<Area, ParalelResource> resourceMap = new TreeMap<Area, ParalelResource>();
	ArrayList<PRM_Planning> prms = new ArrayList<PRM_Planning>();
	
	ArrayList<SegmentGroup> planableSegments = new ArrayList<SegmentGroup>();
	
	ArrayList<PRM_Planning> declined = new ArrayList<PRM_Planning>();
	ArrayList<PRM_Planning> accepted = new ArrayList<PRM_Planning>();
	
	final PRM_Instance instance;
	private int bestScore;
	
	public SimplePlanning(PRM_Instance instance)
	{
		this.instance = instance;
		
		// Generate resources:
		for( Area a : instance.world.values() )
		{
			final int capicity;
			capicity = a.getMaxAreaCapicity();
			resourceMap.put(a, new ParalelResource(a,capicity));
		}
		
		// Generate PRM's
		for(PRM prm : instance.clients.values())
		{
			if(prm.route.length > 0)
			{
				PRM_Planning prm_p = new PRM_Planning(this, prm);
				prms.add(prm_p);
				this.declined.add(prm_p);
			}
		}
		
		int sg_count = 0;
		int movable_sg_count = 0;
		for(PRM_Planning prm : this.prms)
		{
			int segs = 0;
			for(SegmentGroup sg : prm.segmentGroups)
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
		int score = 0 ;
		for(ParalelResource r : this.resourceMap.values())
		{
			score += r.getScore();
		}
		
		for(PRM_Planning prm : this.prms)
		{
			// TODO: Divide the costs of not taking by prebooked and none Prebooked.
			if(!prm.planned)
			{
				score += Model2Constants.declinePenalty;
			}
		}
		
		return score;
	}

	public ParalelResource getResource(Area supervisingArea) 
	{
		return resourceMap.get(supervisingArea);
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
		for(ParalelResource pr : this.resourceMap.values())
		{
			pr.calculateScore();
		}
		
		return this.calculateScore();
	}

	@Override
	public int getScore() 
	{
		// TODO: Check or tracking is benefital;
		return this.calculateScore();
	}

	@Override
	public void revertBest() 
	{
		System.out.println("Saving and reverting best is not Implemented Jet!");
		// TODO Auto-generated method stub
	}

	@Override
	public void saveBest() 
	{
		this.bestScore = this.getScore();
		// TODO Auto-generated method stub
	}

	public SegmentGroup getRandomPlanableSegment()
	{
		return this.planableSegments.get((int)(Math.random()*this.planableSegments.size()));
	}

	public PRM_Planning getRandomDeclined() 
	{
		final int declinedCount = this.declined.size(); 
		
		if(declinedCount == 0)
			return null;
		
		return this.declined.get((int)(Math.random()*declinedCount));
	}
	
	public PRM_Planning getRandomAccepted() 
	{
		final int acceptCount = this.accepted.size();
		
		if(acceptCount == 0)
			return null;
		
		return this.accepted.get((int)(Math.random()*acceptCount));
	}
	
	public void analyse()
	{
		for(Entry<Area, ParalelResource> e : this.resourceMap.entrySet())
		{
			final Area a  = e.getKey();
			final ParalelResource r = e.getValue();
			
			int maxCap = r.getHighestCap();
			
			System.out.println("Capicity of Area "+a.id+" "+a.isSingleTransport+" is in range of [0,"+maxCap+"] max:"+r.getMaxCapicity());
			//System.out.println("   "+r.debugString());

		}
		
		System.out.println(" "+this.accepted.size()+" accepted, "+this.declined.size()+" rejected, total: "+this.prms.size());
	}

	@Override
	public boolean isFeasible() 
	{	
		for(ParalelResource r : this.resourceMap.values())
		{
			if(!r.isFeasible())
			{
				return false;
			}
		}
		
		return true;
	}

	public void emptySolution() 
	{
		for(PRM_Planning prm : this.prms)
		{
			prm.unPlan();
		}
		
		for(ParalelResource r : this.resourceMap.values())
		{
			r.calculateScore();
		}
		
		//System.out.println("Debug:");
		//this.analyse();
	}
	
	public void convertToRealSolution()
	{
		
		throw new Error("Method not finished jet!");
	}

	public Iterable<PRM_Planning> getPlannedPRMs() 
	{
		return this.prms;
	}
	
	public PRM_Instance getInstance()
	{
		return this.instance;
	}

	@Override
	public String getStatusText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void testConsistancy() {
		// TODO Auto-generated method stub
		System.out.println("Debug option not implemented!");
	}

	@Override
	public boolean isImprovedSinceBest() 
	{
		return this.getScore() < this.bestScore;
	}

	@Override
	public int getScore2() {
		return 0;
	}

	@Override
	public boolean isOptimum() 
	{
		// TODO Auto-generated method stub
		return false;
	}
}
