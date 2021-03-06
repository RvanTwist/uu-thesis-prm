package rpvt.lp.gurobi;

import gurobi.*;
import gurobi.GRB.DoubleAttr;

import java.util.ArrayList;

import rpvt.lp.ConstraintInterface;
import rpvt.lp.ConstraintType;
import rpvt.lp.VariableInterface;

/*
 * This program has been developed by Rene van Twist for his master's thesis (under supervision of Han Hoogeveen) within the master program Computing Science at Utrecht University  
 * ęCopyright Utrecht University (Department of Information and Computing Sciences)
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
public final class Constraint implements ConstraintInterface
{
	
	int id;
	public String name;
	LpModel model;
	GRBConstr constraint;
	GRBLinExpr expr;
	
	ConstraintType type;
	double b;
	
	protected Constraint(LpModel m)
	{
		this.model = m;
		expr = new GRBLinExpr();
	}
	
	public void setB(double b)
	{
		if(b == this.b)
			return;
		
		this.b = b;
		
		if(model.constructed)
		{
			try
			{
//				System.out.println("Set b: "+this.id+" to "+b);
				this.constraint.set(DoubleAttr.RHS, b);
			}
			catch(GRBException e)
			{
				System.out.println( " Error code: "+e.getErrorCode());
				throw new Error(e);
			}
		}
	}
	
	public void setVar(Variable v, double a)
	{
		expr.addTerm(a, v.var);
	}
	
	protected final class ConstraintTuple
	{
		final Variable var;
		final double weight;
		
		public ConstraintTuple(Variable v, double w)
		{
			this.var = v;
			this.weight = w;
		}
	}

	@Override
	public void setVar(VariableInterface v, double a) 
	{
		if(v instanceof Variable)
			this.setVar((Variable)v, a);
		else
			throw new Error("Unsupported var: "+v.getClass().getCanonicalName());
	}
}


/* OLD code
{
	
	int id;
	protected double b;
	protected ConstraintType type;
	protected ArrayList<ConstraintTuple> vars = new ArrayList<ConstraintTuple>();
	
	public String name;
	
	LpModel model;
	GRBConstr constraint;
	
	protected Constraint(LpModel m)
	{
		this.model = m;
	}
	
	public void setB(double b)
	{
		if(b == this.b)
			return;
		
		this.b = b;
		
		if(model.constructed)
		{
			try
			{
//				System.out.println("Set b: "+this.id+" to "+b);
				this.constraint.set(DoubleAttr.RHS, b);
			}
			catch(GRBException e)
			{
				System.out.println( " Error code: "+e.getErrorCode());
				throw new Error(e);
			}
		}
	}
	
	public void setVar(Variable v, double a)
	{
		this.vars.add(new ConstraintTuple(v,a));
	}
	
	protected final class ConstraintTuple
	{
		final Variable var;
		final double weight;
		
		public ConstraintTuple(Variable v, double w)
		{
			this.var = v;
			this.weight = w;
		}
	}

	@Override
	public void setVar(VariableInterface v, double a) 
	{
		if(v instanceof Variable)
			this.setVar((Variable)v, a);
		else
			throw new Error("Unsupported var: "+v.getClass().getCanonicalName());
	}
}
//*/