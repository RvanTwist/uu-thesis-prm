package rpvt.lp.lpsolve;

import java.util.ArrayList;

import rpvt.lp.ConstraintConstructor;
import rpvt.lp.ConstraintType;
import rpvt.lp.LpModelAbstr;

import lpsolve.*;

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
public class LpModel extends LpModelAbstr
{	
	private ArrayList<Variable> variables = new ArrayList<Variable>();
	private ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	
	public LpSolve solver;
 	
	public LpModel()
	{
		constructed = false;
	}
	
	public void constructSolver() throws LpSolveException
	{
		//System.out.println("Constructing solver: "+variables.size()+" vars and "+constraints.size()+" constraints");
		
		this.constructed = true;
		
		for(ConstraintConstructor cc : this.constrainConstructors)
		{
			cc.generateAll();
		}
		
		if(this.solver == null)
		{
			this.solver = LpSolve.makeLp(constraints.size(), variables.size());
		}
		else
		{
			this.solver.resizeLp(constraints.size(), variables.size());
		}
		
		
		
		for(Variable v : this.variables)
		{
			solver.setObj(v.varId, v.weight);
			
			if(v.name != null)
				solver.setColName(v.varId, v.name);
			
			if(v.integer)
				solver.setInt(v.varId, true);
			
			if(v.binary)
				solver.setBinary(v.varId, true);
			
			if(v.upperBound != Integer.MAX_VALUE)
			{
				solver.setUpbo(v.varId, v.upperBound);
			}
			
			solver.setLowbo(v.varId, v.lowerBound);
		}
		
		if(this.maximise)
			solver.setMaxim();
		else
			solver.setMinim();
		//solver.solve();
		
		solver.setAddRowmode(true);
		
		double[] costs = new double[this.variables.size()+1];
		int[] indexes = new int[this.variables.size()+1];
		
		int i = 1;
		for(Constraint c : this.constraints)
		{
			c.id = i;
			i++;
			
			int inserts = 0;
//			for(int i = 0 ; i < costs.length ; i++)
//			{
//				costs[i] = 0;
//			}
			
			for(Constraint.ConstraintTuple t : c.vars)
			{
				//System.out.println("Set costs: "+t.var.varId+" : "+t.weight);
				costs[inserts] = t.weight;
				indexes[inserts] = t.var.varId;
				inserts++;
			}
			
			final int type;
			
			switch(c.type)
			{
			case EQ : type = LpSolve.EQ; break;
			case LT : type = LpSolve.LE; break;
			case GT : type = LpSolve.GE; break;
			default : type = LpSolve.EQ; 
			}
			
//			solver.addConstraint(costs, type, c.b);
			solver.setRowex(c.id,inserts, costs, indexes);
			solver.setRh(c.id, c.b);
			solver.setConstrType(c.id, type);
			
			if(c.name != null)
				solver.setRowName(c.id, c.name);
			
			c.vars.clear();
		}
		
		solver.setAddRowmode(false);
		solver.setMinim();
		
		this.constructed = true;
	}
	
	public Variable makeVar()
	{
		return this.makeVar(null);
	}
	
	public<A> Variable<A> makeVar(A rep)
	{
		Variable<A> v = new Variable<A>(this,variables.size()+1);
		v.represents = rep;
		this.variables.add(v);
		return v;
	}
	
	public<A> Variable<A> makeIntVar(A rep)
	{
		Variable<A> v = new Variable<A>(this,variables.size()+1);
		v.integer = true;
		v.represents = rep;
		this.variables.add(v);
		return v;
	}
	
	public Variable makeIntVar()
	{
		return this.makeIntVar(null);
	}
	
	public Constraint makeEQConstraint()
	{
		Constraint c = new Constraint(this);
		c.type = ConstraintType.EQ;
		this.constraints.add(c);
		return c;
	}
	
	public Constraint makeLEConstraint()
	{
		Constraint c = new Constraint(this);
		c.type = ConstraintType.LT;
		this.constraints.add(c);
		return c;
	}
	
	public Constraint makeGEConstraint()
	{
		Constraint c = new Constraint(this);
		c.type = ConstraintType.GT;
		this.constraints.add(c);
		return c;
	}
	
	/**
	 * Clears all variables and constraints, but not the constraint constructors.</br>
	 * Might do some additional clearing for superclasses.
	 */
	public void clear()
	{
		this.variables.clear();
		this.constraints.clear();
		
		if(this.solver != null)
		{
			this.solver.deleteLp();
		}
		
		this.solver = null;
		this.constructed = false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean solve() throws LpSolveException 
	{
		//this.solver.setVerbose(LpSolve.MSG_NONE);
		status = this.solver.solve();
		
		if(status == LpSolve.OPTIMAL)
		{
			double[] result = new double[this.variables.size()];
			
			this.solver.getVariables(result);
			
			for(int i = 0 ; i < result.length ; i++)
			{
				this.variables.get(i).value = result[i];
			}
			
			return true;
		}
		
		return false;
	}
}
