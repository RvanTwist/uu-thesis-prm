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
 * Apply an propagation sequence but only execute it once.
 * Use this object if you are sure that after doing each propagator once, everything is proven.
 * @author rene
 *
 */
public class PropagationSequenceOnce implements PropagationStrategy
{
	private ArrayList<PropagationStrategy> list;
	
	public PropagationSequenceOnce()
	{
		this.list = new ArrayList<PropagationStrategy>();;
	}
	
	public PropagationSequenceOnce(PropagationStrategy... strats)
	{
		this();
		for(PropagationStrategy str : strats)
		{
			this.addStrategy(str);
		}
	}
	
	public void addStrategy(PropagationStrategy s)
	{
		this.list.add(s);
	}
	
	@Override
	public boolean propagate() 
	{
		boolean anyChange = false;
		
		for(PropagationStrategy ps : this.list)
		{
			if( ps.propagate())
			{
				anyChange = true;
			}
		}
		
		return anyChange;
	}

}
