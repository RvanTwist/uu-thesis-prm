package prm.ls.model2;

import prm.ls.*;

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
public class MutationUnPlanPRM implements Mutation
{
	static int WEIGHT = 1;
	
	final SimplePlanning solution;
	
	// Mutation settings
	PRM_Planning prm;
	
	public MutationUnPlanPRM(SimplePlanning s)
	{
		this.solution = s;
	}
	
	@Override
	public void applyMutation() 
	{
		if(prm != null)
		{
			prm.unPlan();
		}
	}

	@Override
	public void generateMutation(SolutionFacture f) 
	{	
		// Generate mutation.
		prm = solution.getRandomAccepted();
		
		if(prm != null)
		{
			prm.costsUnPlan(f);
		}
	}

	@Override
	public double getWeight() 
	{
		return WEIGHT;
	}
	
	public void debug()
	{
		System.out.println("No implementation of debugging jet");
	}

	@Override
	public void rejectMutation() {
		// TODO Auto-generated method stub
		
	}
	
}
