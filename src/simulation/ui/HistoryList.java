package simulation.ui;

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
public class HistoryList<A> 
{
	private Object[] history;
	private int index = -1;
	private int min = -1;
	private int max = -1;
	private int capacity;
	
	public HistoryList(int cap)
	{
		this.capacity = cap;
		this.history = new Object[capacity];
	}
	
	public boolean isFull()
	{
		return ((max + 1) % capacity) == min;
	}
	
	public boolean isEmpty()
	{
		return min == -1 && max == -1;
	}
	
	public void add(A item)
	{
		if(item == null)
			return;
		
		if(this.current() == item)
			return;
		
		if(this.isFull())
		{ // Remove first
			min = (min + 1) % capacity;
		}
		
		if(this.isEmpty())
		{
			index = 0;
			min = 0;
			max = 0;
		}
		else
		{
			index = (index + 1 ) % capacity;
			max = index;
		}
		
		history[index] = item;
	}
	
	public A current()
	{
		if(this.isEmpty())
		{
			return null;
		}
		return (A) history[index];
	}
	
	public A previous()
	{
		if(min == index)
		{
			return null;
		}
		index = (index + capacity - 1) % capacity;
		return (A) history[index];
	}
	
	public A next()
	{
		if(index == max)
		{
			return null;
		}
		index = (index + 1) % capacity;
		return (A) history[index];
	}
	
	public boolean hasPrevious()
	{
		return min != index;
	}
	
	public boolean hasNext()
	{
		return max != index;
	}
}
