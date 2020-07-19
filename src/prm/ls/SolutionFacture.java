package prm.ls;

import prm.ls.model4.CantPlanException;

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
public class SolutionFacture 
{
	public int costs;
	public int costs2;
	
	public boolean feasible;
	private Mutation mutation;
	public boolean accept = false;
	
	public void generateNeighbour(Mutation mutation)
	{
		try 
		{
			mutation.generateMutation(this);
		} 
		catch (CantPlanException e) 
		{
			this.feasible = false;
		}
	}
	
	public void addCosts(int c)
	{
		this.costs += c;
	}
	
	public void apply()
	{
		this.mutation.applyMutation();
	}

	public void clear() 
	{
		costs = 0;
		costs2 = 0;
		feasible = true;
		accept = false;
	}
	
	public boolean isMinImprovement()
	{
		if(costs < 0)
		{
			return true;
		}
		if(costs == 0)
		{
			if(costs2 > 0)
			{
				return false;
			}
			
			return true;
		}
		
		return false;
	}

	public int getCosts() 
	{
		if(costs == 0)
			return costs2;
		
		return costs;
	}
}
