package rpvt.util;

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
public class TupleComp<A extends Comparable<A>,B extends Comparable<B>> implements Comparable<Tuple<A,B>>
{
	public A value1;
    public B value2;
	
	public TupleComp(A v1, B v2)
	{
		this.value1 = v1;
		this.value2 = v2;
	}

	@Override
	public int compareTo(Tuple<A, B> o) 
	{
		final int comp1 = value1.compareTo(o.value1);
		
		if(comp1 == 0)
		{
			return value2.compareTo(o.value2);
		}
		
		return comp1;
	}
	
	public A getFirst()
	{
		return value1;
	}
	
	public B getSecond()
	{
		return value2;
	}
	
	
}
