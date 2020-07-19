package rpvt.lp.lpsolve;

import rpvt.lp.VariableInterface;
import lpsolve.LpSolveException;

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
public class Variable<A> implements VariableInterface<A>
{
	final LpModel model;
	
	protected int varId;
	
	protected boolean integer = false;
	protected boolean binary = false;
	
	protected double weight;
	protected A represents;
	
	protected double lowerBound = 0;
	protected double upperBound = Double.MAX_VALUE;
	
	protected double value;
	
	public String name;
	
	protected Variable(LpModel m, int id)
	{
		this.varId = id;
		this.model = m;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
		
		if(this.model.constructed)
		{
			try 
			{
				//System.out.println("Set objective: "+this.varId+" "+weight);
				this.model.solver.setObj(this.varId, weight);
			} 
			catch (LpSolveException e) 
			{
				throw new Error(e);
			}
		}
	}

	public A getRepresents() {
		return represents;
	}

	public void setRepresents(A represents) {
		this.represents = represents;
	}

	public double getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(double lowerBound) 
	{
		this.lowerBound = lowerBound;
		
		if(this.model.constructed)
		{
			try 
			{
				this.model.solver.setLowbo(this.varId, weight);
			} 
			catch (LpSolveException e) 
			{
				throw new Error(e);
			}
		}
	}

	public double getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(double upperBound) 
	{
		this.upperBound = upperBound;
		
		if(this.model.constructed)
		{
			try 
			{
				this.model.solver.setUpbo(this.varId, weight);
			} 
			catch (LpSolveException e) 
			{
				throw new Error(e);
			}
		}
	}

	public double getValue() {
		return value;
	}

	public void setBinary(boolean b) 
	{
		this.binary = b;
		this.lowerBound = 0;
		this.upperBound = 1;
	}
	
	
}
