package prm.ls.model4.matching;

import prm.ls.SolutionFacture;
import prm.ls.model4.CantPlanException;
import prm.ls.model4.M4_Segment;
import prm.ls.model4.M4_SegmentGroup;

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
public interface M4_MatchingAlgorithm 
{
	public void generateSuggestion(M4_SegmentGroup<?> sg, SolutionFacture f) throws CantPlanException;
	public void planSegment(M4_Segment seg, SolutionFacture f) throws CantPlanException;
	
	public void registerUnplan(M4_SegmentGroup<?> sg);
	
	public void acceptSuggestion();
	public void cancelSuggestion();
	
	public M4_MatchingAlgorithm newForMutation();
	
	/**
	 * Makes sure that this object doesn't backtrack;
	 */
	public void clear();
}
