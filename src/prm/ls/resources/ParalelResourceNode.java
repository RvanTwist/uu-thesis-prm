package prm.ls.resources;

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
public class ParalelResourceNode<A> implements Comparable<ParalelResourceNode<A>>
{
	private static int id_dispenser = 0;
	
	public final int id;
	public int capChange;
	
	final String name;
	
	A represents;
	
	// Variables
	int time;
	int capicity = 0;
	int PrevScore;
	
	public ParalelResourceNode(int id, int capChange, A represents)
	{
		time = 0;
		
		this.capChange = capChange;
		this.id = id;
		
		name = "node_"+id;
		this.represents = represents;
	}
	
	public ParalelResourceNode(int id, int capChange)
	{
		this(id, capChange, null);
	}
	
	public ParalelResourceNode(int capChange)
	{
		this(id_dispenser++, capChange,null);
	}
	
	public ParalelResourceNode(int capChange, A represents)
	{
		this(id_dispenser++, capChange, represents);
	}

	@Override
	public int compareTo(ParalelResourceNode other) 
	{
		final int startDiff = this.time - other.time;
		
		if(startDiff == 0)
		{
			final int capDiff = this.capChange - other.capChange;
			
			if(capDiff == 0)
			{
				return this.id - other.id;
			}
			else
			{
				return capDiff;
			}
		}
		else
		{
			return startDiff;
		}
	}
	
	public String toString()
	{
		return "<"+name+" "+time+" "+capChange+" "+capicity+">";
	}
	
	public void setTime(int time)
	{
		this.time = time;
		this.capicity = 0;
	}
	
	public int getTime()
	{
		return this.time;
	}
	
	public void setRepresents(A r)
	{
		this.represents = r;
	}
	
	public A getRepresents()
	{
		return this.represents;
	}
}
