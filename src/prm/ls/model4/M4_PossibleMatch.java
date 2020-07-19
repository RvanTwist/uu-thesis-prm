package prm.ls.model4;

import java.util.ArrayList;

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
public final class M4_PossibleMatch implements Comparable<M4_PossibleMatch>
{
	public final M4_PRM_SegmentGroup g1;
	public final M4_PRM_SegmentGroup g2;
	
	public final int offset;
	public final int matchID;
	
	public ArrayList<M4_SegmentMatch> segments = new ArrayList<M4_SegmentMatch>();
	
	private static int id_dispenser = 0;
	
	public M4_PossibleMatch(M4_PRM_SegmentGroup g1, M4_PRM_SegmentGroup g2, int offset) 
	{
		this.g1 = g1;
		this.g2 = g2;
		this.offset = offset;
		
		g1.possibleMatches.add(this);
		g2.possibleMatches.add(this);
		
		this.matchID = id_dispenser;
		id_dispenser++;
	}

	public void merge() throws CantPlanException
	{
		if(g1.isPlanned())
		{
			throw new Error("G1 is planned!");
		}
		
		if(g2.isPlanned())
		{
			throw new Error("G1 is planned!");
		}
		
		if(g1.isMerged())
		{
			if(g2.isMerged())
			{
				throw new Error("Unfinished case 2 MergedSegmentGroups merging");
			}
			else
			{
				System.out.println(" Merge where g1 is already merged g2 not!");
				M4_MergedSegmentGroup msg = (M4_MergedSegmentGroup) g1.getMergedGroup();
				msg.addSegmentGroup(g2, offset);
			}
		}
		else if(g2.isMerged())
		{
			System.out.println(" Merge where g2 is already merged g1 not!");
			M4_MergedSegmentGroup msg = (M4_MergedSegmentGroup) g2.getMergedGroup();
			msg.addSegmentGroup(g1, -offset);
		}
		else
		{
			System.out.println("Make new segmentGroup!");
			final M4_MergedSegmentGroup msg = new M4_MergedSegmentGroup(g1,g2,offset);
		}
		
	}

	@Override
	public int compareTo(M4_PossibleMatch other) 
	{
		final int c1 = g1.compareTo(other.g1);
		
		if(c1 == 0)
		{
			final int c2 = g2.compareTo(other.g2);
			
			if(c2 == 0)
			{
				return this.offset - other.offset;
			}
			
			return c2;
		}
		
		return c1;
	}
	
	public String toString()
	{
		return "Match: prm: "+g1.prm.prm.prm_ID+":"+g1.id+" with "+g2.prm.prm.prm_ID+":"+g2.id+" ("+offset+")";
	}
	
	
	
	
	
}
