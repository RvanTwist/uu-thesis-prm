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
public class MutationPlanSegment implements Mutation
{
	static int WEIGHT = 8;
	
	final SimplePlanning solution;
	
	// Mutation settings
	SegmentGroup group;
	int time;
	
	
	public MutationPlanSegment(SimplePlanning s)
	{
		this.solution = s;
	}
	
	@Override
	public void applyMutation() 
	{
		if(this.group != null)
		{
			this.group.planAt(this.time);
		}
	}

	@Override
	public void generateMutation(SolutionFacture f) 
	{	
		// Generate mutation.
		this.group = solution.getRandomPlanableSegment();
		
		if(group.isPlanned())
		{
			final int min = group.getErliestStart();
			final int max = group.getLatestStart();
			
			if(min > max)
			{
				final PRM_Planning prm = group.prm;
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
				
				throw new Error(	"Impossible PRM can't plan it: fixed:"+group.isFixed()+" \n" +
									"Method: ["+min+","+max+"] l: "+group.lenght+" \n" +
									"  PRM: "+prm_r.name+"\n" +
									"  ["+prm_r.arrival+","+prm_r.deadline+"] cap: "+prm_r.capRequirement +
									seg_s+"\n" +
									"  "+time+"/"+(prm_r.deadline - prm_r.arrival)+"\n" +
									"SegmentGroups: "+segg_s);
									
			}
			
			this.time = min + (int)(Math.random()*(max - min));
			
			group.getCostsMove(time, f);
		}
		else
		{
			this.group = null;
			f.feasible = false;
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
		
		
		{
			System.out.println("Plan("+time+"): "+group);
		}
		
		{
			System.out.println(group.printPlanning());
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
		System.out.println(group.prm.prm.saveString());
	}

	@Override
	public void rejectMutation() {
		// TODO Auto-generated method stub
		
	}
}
