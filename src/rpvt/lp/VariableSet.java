package rpvt.lp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
public class VariableSet<A> implements Collection<A>
{
	Collection<A> set = new ArrayList<A>();
	
	public VariableSet(List<A> list)
	{
		
	}

	@Override
	public boolean add(A arg0) 
	{
		return set.add(arg0);
	}

	@Override
	public boolean addAll(Collection<? extends A> arg0) 
	{
		return set.addAll(arg0);
	}

	@Override
	public void clear() 
	{
		set.clear();
	}

	@Override
	public boolean contains(Object arg0) 
	{
		return set.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) 
	{
		return containsAll(arg0);
	}

	@Override
	public boolean isEmpty() 
	{		
		return set.isEmpty();
	}

	@Override
	public Iterator<A> iterator() 
	{
		return set.iterator();
	}

	@Override
	public boolean remove(Object arg0) 
	{
		return set.remove(arg0);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) 
	{
		return set.removeAll(arg0);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) 
	{
		return set.retainAll(arg0);
	}

	@Override
	public int size() 
	{
		return set.size();
	}

	@Override
	public Object[] toArray() 
	{
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) 
	{
		return set.toArray(arg0);
	}
	
}
