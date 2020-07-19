package tests;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import lpsolve.LpSolve;

import prm.problemdef.*;

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
public class TestLoading 
{
	public static void main(String[] args) throws Exception
	{
		LpModel constructor = new LpModel();
		
		Variable x = constructor.makeVar();
		Variable y = constructor.makeVar();
		
		x.setWeight(10);
		y.setWeight(1);
		
		// Constraint 1
		Constraint c1 = constructor.makeLEConstraint();
		c1.setB(12);
		c1.setVar(x, 1);
		c1.setVar(y, 4);
		
		// Constraint 2
		Constraint c2 = constructor.makeGEConstraint();
		c2.setB(10);
		c2.setVar(x, 4);
		c2.setVar(y, 1);
		
		try 
		{
			constructor.constructSolver();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		// Test2
		
		System.out.println("test2");
		LpSolve solve = LpSolve.makeLp(0, 2);
		solve.strSetObjFn("10 1");
		solve.strAddConstraint("1 4", LpSolve.LE, 12);
		solve.strAddConstraint("4 1", LpSolve.GE, 10);
		
		solve.printLp();
		
		solve.solve();
		solve.printSolution(1);
		
		
	}
	
	
}
