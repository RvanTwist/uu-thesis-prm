package rpvt.lp;


import java.util.ArrayList;

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
public abstract class LpModelAbstr
{
	static public final int CONSTRUCTING = 0;
	static public final int OPTIMAL = 1;
	static public final int INFEASIBLE = 2;
	
	public boolean maximise = false;
	public boolean constructed;
	public int status = 0;
	public boolean silenced = false;
	
	protected ArrayList<ConstraintConstructor> constrainConstructors = new ArrayList<ConstraintConstructor>();
	
	public abstract void constructSolver() throws Exception;
	
	public abstract VariableInterface makeVar();
	
	public abstract <A> VariableInterface<A> makeVar(A rep);
	public abstract <A> VariableInterface<A> makeIntVar(A rep);
	
	public abstract <A> VariableInterface makeIntVar();
	
	public abstract ConstraintInterface makeEQConstraint();
	
	public abstract ConstraintInterface makeLEConstraint();
	
	public abstract ConstraintInterface makeGEConstraint();
	
	/**
	 * Clears all variables and constraints, but not the constraint constructors.</br>
	 * Might do some additional clearing for superclasses.
	 */
	public abstract void clear();
	
	abstract public boolean solve() throws Exception;

	public void setSilenced(boolean silence)
	{
		this.silenced = silence;
	}
	
	public boolean isConstructed() 
	{
		return this.constructed;
	}
	
	public boolean isFeasible()
	{
		switch(status)
		{
		case OPTIMAL : return true;
		}
		
		return false;
	}
	
	public void setMaximise()
	{
		this.maximise = true;
	}
	
	public void setMinimise()
	{
		this.maximise = false;
	}
	
	public<A> VariableInterface<A> makeIntVar(A rep, double lb, double ub, double w)
	{
		VariableInterface<A> var = this.makeIntVar(rep);
		
		if(lb == 0 && ub == 1)
		{
			var.setBinary(true);
		}
		else
		{
			var.setLowerBound(lb);
			var.setUpperBound(ub);
		}
		
		var.setWeight(w);
		
		return var;
		
	}
	
	public<A> VariableInterface<A> makeVar(A rep, double lb, double ub, double w)
	{
		VariableInterface<A> var = this.makeVar(rep);
		
		var.setLowerBound(lb);
		var.setUpperBound(ub);
		
		var.setWeight(w);
		
		return var;	
	}
}
