package prm.ls.model4.mutations;

import java.util.ArrayList;
import java.util.Random;

import prm.ls.*;
import prm.ls.model4.*;
import prm.ls.model4.matching.M4_MatchingAlgorithm;
import prm.ls.model4.mutations.*;
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
public class MutationMergeSegmentGroupsSinglePRM extends MutationMergeSegmentGroupsAbstr
{

	M4_PRM prm;
	ArrayList<M4_PossibleMatch> matches = new ArrayList<M4_PossibleMatch>();
	
	public MutationMergeSegmentGroupsSinglePRM(M4_PRM prm, M4_Planning p, M4_MatchingAlgorithm a) 
	{
		super(p, a);
		
		this.prm = prm;
		
		for(M4_PossibleMatch m : planning.matches)
		{
			if(m.g1.prm == prm || m.g2.prm == prm)
			{
				matches.add(m);
			}
		}
	}

	@Override
	public M4_PossibleMatch getRandomMatching() 
	{
		if(matches.size() == 0)
			return null;
		
		Random r = this.planning.random;
		
		return matches.get((int)(r.nextDouble()*matches.size()));
		
	}
	
}
