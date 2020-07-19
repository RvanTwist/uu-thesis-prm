package prm.ls;

import java.util.ArrayList;
import java.util.Collection;

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
public class DefaultMutationSelector implements MutationSelector 
{
	SimulatedAnnealing solver;

	ArrayList<Mutation> mutations = new ArrayList<Mutation>();
	
	double[] changes;
	
	public DefaultMutationSelector(Mutation[] mutations)
	{
		for(Mutation m : mutations)
		{
			this.mutations.add(m);
		}
	}
	
	public DefaultMutationSelector(Collection<Mutation> mutations) 
	{
		this.mutations.addAll(mutations);
	}

	@Override
	public Mutation getMutation() 
	{
		final double c = solver.random.nextDouble();
		
		for(int m = 0 ; m < changes.length ; m++)
		{
			if(c < changes[m])
			{
				return this.mutations.get(m);
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
		changes = new double[this.mutations.size()];
		
		// Find changes
		{
			double sumC = 0;
			int i = 0;
			for(Mutation m : this.mutations)
			{
				sumC += m.getWeight();
				changes[i] = sumC;
				i++;
			}
			for(i = 0 ; i < changes.length ; i++)
			{
				changes[i] = changes[i] / sumC;
			}
		}
		
	}
	
	public String printSettings() {
		// TODO Auto-generated method stub
		
		String mutatoins_s1 = null;
		for(int i = 0 ; i < mutations.size() ; i++)
		{
			Mutation m = this.mutations.get(i);
			if(mutatoins_s1 != null)
			{
				mutatoins_s1 += "                  "+
							   m.getClass().getSimpleName()+" "+changes[i]+"%\n";
			}
			else
			{
				mutatoins_s1 = ""+m.getClass().getSimpleName()+" "+changes[i]+"%\n";
			}
		}
		
		if(mutatoins_s1 == null)
			mutatoins_s1 = "-\n";
		
		return " Default Mutation Strategy:"+
			   "    - Mutations : "+mutatoins_s1;
	}
}
