package rpvt.lp.gurobi;

import rpvt.lp.VariableInterface;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBVar;
import gurobi.GRB.CharAttr;
import gurobi.GRB.DoubleAttr;
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
	int varId;
	LpModel model;
	GRBVar var;
	A represents;
	
//	protected Variable(LpModel m, int id)
//	{
//		this.varId = id;
//		this.model = m;
//	}
	
	protected Variable(LpModel m, int id)
	{
		this(m,id,0,Double.MAX_VALUE,0,GRB.CONTINUOUS,"");
	}
	
	protected Variable(LpModel m, int id, double lowerBound, double upperBound, double weight, char type, String name)
	{
		this.varId = id;
		this.model = m;
		
		try 
		{
			this.var = model.solver.addVar(lowerBound, upperBound, weight, type, name);
		} 
		catch (GRBException e) 
		{
			throw new Error(e);
		}
	}

	public double getWeight() 
	{
		try 
		{
			return var.get(DoubleAttr.Obj);
		} 
		catch (GRBException e) 
		{
			throw new Error(e);
		}
	}

	public void setWeight(double weight) 
	{	
		try 
		{
			var.set(DoubleAttr.Obj, weight);
		} 
		catch (GRBException e) 
		{
			e.printStackTrace();
		}
	}

	public A getRepresents() 
	{
		return represents;
	}

	public void setRepresents(A represents) {
		this.represents = represents;
	}

	public double getLowerBound() 
	{
		try 
		{ return this.var.get(DoubleAttr.LB); } 
		catch (GRBException e) 
		{ throw new Error(e); }
	}

	public void setLowerBound(double lowerBound) 
	{
		try 
		{ this.var.set(DoubleAttr.LB, lowerBound); } 
		catch (GRBException e) 
		{ throw new Error(e); }
	}

	public double getUpperBound() {
		try 
		{ return this.var.get(DoubleAttr.UB); } 
		catch (GRBException e) 
		{	throw new Error(e);	}
	}

	public void setUpperBound(double upperBound) 
	{
			try 
			{ this.var.set(DoubleAttr.UB, upperBound); } 
			catch (GRBException e) 
			{	throw new Error(e);	}
	}

	public double getValue() 
	{
		final double value;
		
		try
		{
			value = this.var.get(DoubleAttr.X);
		}
		catch(GRBException e)
		{
			throw new Error(e);
		}
		
		return value;
	}

	public void setBinary(boolean b) 
	{
		try
		{	var.set(CharAttr.VType, GRB.BINARY); }
		catch(Exception e){ throw new Error(e);}
		this.setLowerBound(0);
		this.setUpperBound(1);
	}

	protected void generate() 
	{
		
	}
	
	
}

/* Old class
{	
	protected int varId;
	
	protected boolean integer = false;
	protected boolean binary = false;
	
	protected double weight;
	protected A represents;
	
	protected double lowerBound = 0;
	protected double upperBound = Double.MAX_VALUE;
	
	public String name;
	
	LpModel model;
	GRBVar var;
	
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
			//System.out.println("Set objective: "+this.varId+" "+weight);
			try 
			{
				var.set(DoubleAttr.Obj, weight);
			} 
			catch (GRBException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public A getRepresents() {
		return represents;
	}

	public void setRepresents(A represents) {
		this.represents = represents;
	}

	public double getLowerBound() 
	{
		return lowerBound;
	}

	public void setLowerBound(double lowerBound) 
	{
		this.lowerBound = lowerBound;
		
		if(this.model.constructed)
		{
			try 
			{
				this.var.set(DoubleAttr.LB, lowerBound);
			} 
			catch (GRBException e) 
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
		if(this.upperBound == upperBound)
			return;
		
		this.upperBound = upperBound;
		
		if(this.model.constructed)
		{
			try 
			{
				this.var.set(DoubleAttr.UB, upperBound);
			} 
			catch (GRBException e) 
			{
				throw new Error(e);
			}
		}
	}

	public double getValue() 
	{
		final double value;
		
		try
		{
			value = this.var.get(DoubleAttr.X);
		}
		catch(GRBException e)
		{
			throw new Error(e);
		}
		
		return value;
	}

	public void setBinary(boolean b) 
	{
		if(b)
		{
			this.setLowerBound(0);
			this.setUpperBound(1);
		}
		
		this.binary = b;
	}

	protected void generate() 
	{
		final char type;
		if(this.binary)
			type = GRB.BINARY;
		else if(this.integer)
			type = GRB.INTEGER;
		else 
			type = GRB.CONTINUOUS;
		
		try 
		{
			this.var = model.solver.addVar(this.lowerBound, this.upperBound, this.weight, type, this.name);
		}
		catch (GRBException e) 
		{
			throw new Error(e);
		}
		
		
	}
	
	
}
//*/
