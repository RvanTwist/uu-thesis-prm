package rpvt.ls.branching;
import java.util.ArrayList;

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
/**
 * This propagation strategy executed a sequence of propagation strategies till no changes can be found.
 * 
 * @author rene
 */
public class PropagationSequence implements PropagationStrategy
{
	private ArrayList<PropagationStrategy> list;
	
	public PropagationSequence()
	{
		this.list = new ArrayList<PropagationStrategy>();;
	}
	
	/**
	 * Makes a propagation sequence of the following strategies.
	 * @param strats
	 */
	public PropagationSequence(PropagationStrategy... strats)
	{
		this();
		for(PropagationStrategy str : strats)
		{
			this.addStrategy(str);
		}
	}
	
	/**
	 * Adds a strategy at the end of the sequence.
	 * @param s
	 */
	public void addStrategy(PropagationStrategy s)
	{
		this.list.add(s);
	}
	
	@Override
	public boolean propagate() 
	{
		
		boolean succes = false;
		boolean anyChange = false;
		
		for(PropagationStrategy ps : this.list)
		{
			if( ps.propagate())
			{
				anyChange = true;
			}
		}
		
		if(anyChange)	
		{
			succes = true;
		}
			
		while(anyChange)
		{
			anyChange = false;
//			int i = 0;
			for(PropagationStrategy ps : this.list)
			{
				if( ps.propagate())
				{
//					System.out.println("succes: "+i);
					anyChange = true;
				}
				
				//while(ps.propagate());
				
//				i++;
			}
		}
		
		return succes;
	}

}
