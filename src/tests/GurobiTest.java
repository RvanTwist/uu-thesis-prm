package tests;

import rpvt.lp.lpsolve.*;

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
public class GurobiTest 
{
	public static void main(String[] args)
	{
		LpModel model = new LpModel();
		
		Variable v1 = model.makeIntVar();
		Variable v2 = model.makeIntVar();
		Variable v3 = model.makeIntVar();
		
		v1.setBinary(true);
		v2.setBinary(true);
		v3.setBinary(true);
		
		v1.setWeight(1);
		v2.setWeight(1);
		v3.setWeight(2);
		
		model.setMaximise();
		
		// Constraints
		// Add constraint: x + 2 y + 3 z <= 4
		Constraint c1 = model.makeLEConstraint();
		c1.setVar(v1, 1);
		c1.setVar(v2, 2);
		c1.setVar(v3, 3);
		c1.setB(4);
		
		
		// Add constraint: x + y >= 1
		Constraint c2 = model.makeGEConstraint();
		c2.setVar(v1, 1);
		c2.setVar(v2, 1);
		c2.setB(1);
		
		try 
		{
			model.constructSolver();
		}
		catch (Exception e) 
		{
			throw new Error(e);
		}
		
		try
		{
			model.solve();
		}
		catch(Exception e)
		{
			throw new Error(e);
		}
		
		System.out.println("v1: "+v1.getValue());
		System.out.println("v2: "+v2.getValue());
		System.out.println("v3: "+v3.getValue());
	}
}
