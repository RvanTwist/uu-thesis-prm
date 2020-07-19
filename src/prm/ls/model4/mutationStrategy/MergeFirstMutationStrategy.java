package prm.ls.model4.mutationStrategy;

import java.util.ArrayList;
import java.util.Collection;

import prm.ls.Mutation;
import prm.ls.MutationSelector;
import prm.ls.SimulatedAnnealing;

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
public class MergeFirstMutationStrategy implements MutationSelector 
{
	SimulatedAnnealing solver;

	int iterations_p1;
	
	ArrayList<Mutation> mutations1 = new ArrayList<Mutation>();
	ArrayList<Mutation> mutations2 = new ArrayList<Mutation>();
	
	double[] changes1;
	double[] changes2;
	
	public MergeFirstMutationStrategy(Mutation[] m1, Mutation[] m2 , int i)
	{
		this.iterations_p1 = i;
		
		for(Mutation m : m1)
		{
			this.mutations1.add(m);
		}
		for(Mutation m : m2)
		{
			this.mutations2.add(m);
		}
	}

	@Override
	public Mutation getMutation() 
	{
		if(solver.getIteration() < iterations_p1)
		{
			return getMutation(mutations1, changes1);
		}
		else
		{
			return getMutation(mutations2, changes2);
		}
	}

	public Mutation getMutation(ArrayList<Mutation> mutations, double[] chances)
	{
		final double c = solver.random.nextDouble();
		
		for(int m = 0 ; m < chances.length ; m++)
		{
			if(c < chances[m])
			{
				return mutations.get(m);
			}
		}
		
		return null;
	}
	
	@Override
	public void setSolver(SimulatedAnnealing solver) 
	{
		this.solver = solver;
	}

	@Override
	public void initialise() 
	{
		changes1 = new double[this.mutations1.size()];
		
		// Find changes
		{
			double sumC = 0;
			int i = 0;
			for(Mutation m : this.mutations1)
			{
				sumC += m.getWeight();
				changes1[i] = sumC;
				i++;
			}
			for(i = 0 ; i < changes1.length ; i++)
			{
				changes1[i] = changes1[i] / sumC;
			}
		}
		
		changes2 = new double[this.mutations2.size()];
		
		// Find changes
		{
			double sumC = 0;
			int i = 0;
			for(Mutation m : this.mutations2)
			{
				sumC += m.getWeight();
				changes2[i] = sumC;
				i++;
			}
			for(i = 0 ; i < changes2.length ; i++)
			{
				changes2[i] = changes2[i] / sumC;
			}
		}
		
	}

	@Override
	public String printSettings() {
		// TODO Auto-generated method stub
		
		String mutatoins_s1 = null;
		for(int i = 0 ; i < mutations1.size() ; i++)
		{
			Mutation m = this.mutations1.get(i);
			if(mutatoins_s1 != null)
			{
				mutatoins_s1 += "                           "+
							   m.getClass().getSimpleName()+" "+changes1[i]+"%\n";
			}
			else
			{
				mutatoins_s1 = ""+m.getClass().getSimpleName()+" "+changes1[i]+"%\n";
			}
		}
		
		if(mutatoins_s1 == null)
			mutatoins_s1 = "-\n";
		
		String mutatoins_s2 = null;
		for(int i = 0 ; i < mutations2.size() ; i++)
		{
			Mutation m = this.mutations2.get(i);
			if(mutatoins_s2 != null)
			{
				mutatoins_s2 += "                           "+
							   m.getClass().getSimpleName()+" "+changes2[i]+"%\n";
			}
			else
			{
				mutatoins_s2 = ""+m.getClass().getSimpleName()+" "+changes2[i]+"%\n";
			}
		}
		
		if(mutatoins_s2 == null)
			mutatoins_s2 = "-\n";
		
		return " 2 Phase Mutation Strategy:"+
			   "    - Phase 1 Iterations : "+iterations_p1+"\n"+
			   "    - Phase 1 Mutations  : "+mutatoins_s1+
			   "    - Phase 2 Mutations  : "+mutatoins_s2;
	}

}
