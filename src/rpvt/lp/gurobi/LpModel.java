package rpvt.lp.gurobi;

import gurobi.*;
import gurobi.GRB.CharAttr;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRB.IntParam;

import java.util.ArrayList;

import lpsolve.*;
import rpvt.lp.ConstraintConstructor;
import rpvt.lp.ConstraintType;
import rpvt.lp.LpModelAbstr;
import rpvt.lp.VariableInterface;

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
	static GRBEnv SHARED_ENV;
	
	private ArrayList<Variable> variables = new ArrayList<Variable>();
	private ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	
	GRBEnv env;
	public GRBModel solver;

	double sollution;
	
	boolean clear_constructs1 = true;
	
	public LpModel()
	{
		constructed = false;
		
		if(SHARED_ENV == null)
		{
			try 
			{
				SHARED_ENV = new GRBEnv("gurobi_log.txt");
				SHARED_ENV.set(IntParam.OutputFlag,0);
				
			} catch (GRBException e) 
			{
				throw new Error(e);
			}
		}
		
		this.env = SHARED_ENV;
		
		try
		{ this.solver = new GRBModel(env); }
		catch(GRBException e)
		{ throw new Error(e); }
	}
	
	public LpModel(GRBEnv env)
	{
		this.env = env;
		constructed = false;
	}
	
	public void constructSolver() throws GRBException
	{
		System.out.println("Constructing solver: "+variables.size()+" vars and "+constraints.size()+" constraints");
		
		this.constructed = true;
		
		
//		if(this.solver == null)
//		{
//			this.solver = new GRBModel(env);
//		}
//		else
//		{
//			// TODO: Check or resetting causes trouble.
//			this.solver.reset();
//		}
			
		// Generate variables
		for(Variable v : this.variables)
		{
			v.generate();
		}
		
		// Update the variables.
		solver.update();
		
		// Set Objective
//		System.out.println("Maximise: "+maximise);
		if(maximise)
			solver.setObjective(solver.getObjective(), GRB.MAXIMIZE);
		else
			solver.setObjective(solver.getObjective(), GRB.MINIMIZE);
		
		for(ConstraintConstructor cc : this.constrainConstructors)
		{
			cc.generateAll();
		}
		
		int i = 1;
		for(Constraint c : this.constraints)
		{
			c.id = i;
			i++;
			
			final char type;
			
			switch(c.type)
			{
			case EQ : type = GRB.EQUAL; break;
			case LT : type = GRB.LESS_EQUAL; break;
			case GT : type = GRB.GREATER_EQUAL; break;
			default : type = GRB.EQUAL; 
			}
//			System.out.println(" type: "+c.type+" "+type+" "+c.b);
			c.constraint = solver.addConstr(c.expr, type, c.b, c.name);
			c.expr = null;
		}
		
		solver.update();
		this.constructed = true;
		System.gc();
		
	}
	
/* Old construct solver
public void constructSolver() throws GRBException
	{
		System.out.println("Constructing solver: "+variables.size()+" vars and "+constraints.size()+" constraints");
		
		this.constructed = true;
		
		
		if(this.solver == null)
		{
			this.solver = new GRBModel(env);
		}
		else
		{
			// TODO: Check or resetting causes trouble.
			this.solver.reset();
		}
			
		// Generate variables
		for(Variable v : this.variables)
		{
			v.generate();
		}
		
		// Update the variables.
		solver.update();
		
		// Set Objective
//		System.out.println("Maximise: "+maximise);
		if(maximise)
			solver.setObjective(solver.getObjective(), GRB.MAXIMIZE);
		else
			solver.setObjective(solver.getObjective(), GRB.MINIMIZE);
		
		for(ConstraintConstructor cc : this.constrainConstructors)
		{
			cc.generateAll();
		}
		
		int i = 1;
		for(Constraint c : this.constraints)
		{
			c.id = i;
			i++;
			
			int inserts = 0;
			
			GRBLinExpr expr = new GRBLinExpr();
			
			for(Constraint.ConstraintTuple t : c.vars)
			{
//				System.out.println("Set costs: "+t.var.varId+" : "+t.weight+" "+t.var.var);
				expr.addTerm(t.weight, t.var.var);
				inserts++;
			}
			
			final char type;
			
			switch(c.type)
			{
			case EQ : type = GRB.EQUAL; break;
			case LT : type = GRB.LESS_EQUAL; break;
			case GT : type = GRB.GREATER_EQUAL; break;
			default : type = GRB.EQUAL; 
			}
//			System.out.println(" type: "+c.type+" "+type+" "+c.b);
			c.constraint = solver.addConstr(expr, type, c.b, c.name);
			
			c.vars.clear();
		}
		
		solver.update();
		this.constructed = true;
		System.gc();
		
	}
//*/
	
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
		Variable<A> v = new Variable<A>(this,variables.size()+1,0,Double.MAX_VALUE,0,GRB.INTEGER,"");
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
			this.solver.dispose();
		}
		
		this.solver = null;
		this.constructed = false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean solve() 
	{
		//this.solver.setVerbose(LpSolve.MSG_NONE);
		try
		{
			this.solver.optimize();
			
			//System.out.println(" SolCount: "+this.solver.get(IntAttr.SolCount) +" status: "+this.solver.get(IntAttr.Status) );
			
			if(this.solver.get(IntAttr.SolCount) == 0)
			{
				this.status = LpModel.INFEASIBLE;
			}
			else
			{
				this.status = LpModel.OPTIMAL;
			}
			
			if(this.status == LpModel.INFEASIBLE)
			{
				this.sollution = -1;
			}
			else
			{
				this.sollution = this.solver.get(DoubleAttr.ObjVal);
			}
			//System.out.println("Sollution: "+sollution);
			
		}
		catch(GRBException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
		return true;
	}
	
	@Override
	public<A> Variable<A> makeIntVar(A rep, double lb, double ub, double w)
	{
		char type;
		if(lb == 0 && ub == 1)
			type = GRB.BINARY;
		else
			type = GRB.INTEGER;
		
		Variable<A> v = new Variable<A>(this,variables.size()+1,lb,ub,w,type,"");
		v.represents = rep;
		this.variables.add(v);
		return v;
	}
	
	@Override
	public<A> Variable<A> makeVar(A rep, double lb, double ub, double w)
	{
		Variable<A> v = new Variable<A>(this,variables.size()+1,lb,ub,w,GRB.CONTINUOUS,"");
		v.represents = rep;
		this.variables.add(v);
		return v;
	}
}
