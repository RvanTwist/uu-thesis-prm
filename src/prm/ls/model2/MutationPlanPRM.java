package prm.ls.model2;

import java.util.Map.Entry;

import prm.ls.*;
import prm.ls.resources.ParalelResource;
import prm.problemdef.Area;
import prm.problemdef.PRM;
import prm.problemdef.Segment;

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
public class MutationPlanPRM implements Mutation
{
	static int WEIGHT = 1;
	
	final SimplePlanning solution;
	
	// Mutation settings
	PRM_Planning prm;
	int[] time = new int[]{};
	
	public MutationPlanPRM(SimplePlanning s)
	{
		this.solution = s;
	}
	
	@Override
	public void applyMutation() 
	{
//		for(ParalelResource pr : solution.resourceMap.values())
//		{
//			pr.beforeUpdate = pr.debugString();
//		}
		
		if(prm != null)
		{
			prm.plan(time);
		}
	}

	@Override
	public void generateMutation(SolutionFacture f) 
	{	
		// Generate mutation.
		prm = solution.getRandomDeclined();
		
		if(prm == null)
		{
			return;
		}
		
		// Make sure the array is big enough.
		if(this.time.length < prm.segmentGroups.length + 1)
		{
			this.time = new int[prm.segmentGroups.length + 2];
		}
		
		// Remove Decline panelty:
		f.costs -= Model2Constants.declinePenalty;
		
		// Must be finished before the deadline.
		time[prm.segmentGroups.length] = prm.prm.deadline;
		
		
		for(int i = prm.segmentGroups.length - 1 ; i >= 0 ; i--)
		{	
			SegmentGroup sg = prm.segmentGroups[i];
			if(sg.fixed)
			{
				time[i] = sg.fixedTime;
				sg.getCostsMove(sg.fixedTime, f);
			}
			else
			{
				final Segment s = sg.segments[0].segment;
				final int max = Math.min(time[i+1]-sg.lenght, s.getLatestStart());
				final int min = s.getEarliestStart();
				
				if(min > max)
				{
					final PRM prm_r = prm.prm;
					String seg_s = "";
					int time = 0;
					for(Segment seg : prm_r.route)
					{
						time += seg.getSegmentTime();
						seg_s += "\n    "+seg+" ["+seg.getEarliestStart()+","+seg.getLatestStart()+"] ";
					}
					
					String segg_s = "";
					
					for(SegmentGroup segg : prm.segmentGroups)
					{
						segg_s += "\n SegmentGroup "+segg.start+(segg.fixed ? "(fixed: "+segg.fixedTime+") " : " ")+" len: "+segg.lenght;
						
						for(PlannedSegment pseg : segg.segments)
						{
							segg_s += "\n     start: "+pseg.start.getTime()+" "+pseg.segment;
						}
					}
					
					throw new Error(	"Impossible PRM can't plan it: fixed:"+sg.isFixed()+" \n" +
										"Method: ["+min+","+max+"] l: "+sg.lenght+" \n" +
										"  PRM: "+prm_r.name+"\n" +
										"  ["+prm_r.arrival+","+prm_r.deadline+"] cap: "+prm_r.capRequirement +
										seg_s+"\n" +
										"  "+time+"/"+(prm_r.deadline - prm_r.arrival)+"\n" +
										"SegmentGroups: "+segg_s);
										
				}
			
				final int t = min + (int)(Math.random()*(max-min));
				sg.getCostsMove(t, f);
				
				time[i] = t;
			}
		}
	}

	@Override
	public double getWeight() 
	{
		return WEIGHT;
	}
	
	public void debug()
	{
		System.out.println("Find infeasiblity: ");
		
		for(int i = 0 ; i < prm.segmentGroups.length ; i++)
		{
			System.out.println("Plan("+time[i]+"): "+prm.segmentGroups[i]);
		}
		
		for(SegmentGroup sg :prm.segmentGroups )
		{
			System.out.println(sg.printPlanning());
		}
		
		ParalelResource badSeed = null;
		Area badSeedArea = null;
		
		for(Entry<Area, ParalelResource> entry : solution.resourceMap.entrySet())
		{
			final ParalelResource pr = entry.getValue();
			System.out.println(" resource: "+pr+" "+pr.isFeasible());
			if(! pr.isFeasible())
			{
				badSeed = pr;
				badSeedArea = entry.getKey();
				break;
			}
		}
	
		if(badSeed != null)
		{
			System.out.println("\nFound bad ParalelResource: "+ badSeedArea.id);
			System.out.println(" "+badSeed.debugString());
			System.out.println("Before: "+badSeed.beforeUpdate);
			System.out.println("Before: "+badSeed.debugString);
			badSeed.calculateScore();
			System.out.println(" after recalc: ("+badSeed.isFeasible()+")\n"+badSeed.debugString());
		}
		else
		{
			System.out.println("No bad ParalelResource found!");
		}
		
		System.out.println(prm.prm.saveString());
	}

	@Override
	public void rejectMutation() {
		// TODO Auto-generated method stub
		
	}
	
}
