package rpvt.lp.lpsolve;

import java.util.ArrayList;

import rpvt.lp.ConstraintInterface;
import rpvt.lp.ConstraintType;
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
public final class Constraint implements ConstraintInterface
{
	int id;
	protected double b;
	protected ConstraintType type;
	protected ArrayList<ConstraintTuple> vars = new ArrayList<ConstraintTuple>();
	
	public String name;
	
	LpModel model;
	
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
			//System.out.println("Set constraint: "+id+":"+name+" "+b);
			try 
			{
				model.solver.setRh(id, b);
			} catch (LpSolveException e) 
			{
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
	public void setVar(VariableInterface v, double a) {
		// TODO Auto-generated method stub
		
	}
}
