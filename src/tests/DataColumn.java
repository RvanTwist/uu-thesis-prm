package tests;

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
public class DataColumn<A> 
{
	final String field;
	final String name;
	final ArrayList<A> list;
	
	boolean bold;
	
	public DataColumn(ArrayList<A> list, String field)
	{
		this.field = field;
		this.list = list;
		this.bold = false;
		this.name = "<?>";
	}
	
	public DataColumn(ArrayList<A> list, String field, String name)
	{
		this.field = field;
		this.list = list;
		this.bold = false;
		this.name = name;
	}
	
	public DataColumn(ArrayList<A> list, String field, String name, boolean b)
	{
		this.field = field;
		this.list = list;
		this.name = name;
		this.bold = b;
	}
	
	public Object getRow(int r)
	{
		A ob = list.get(r);
		
		try 
		{
			return ob.getClass().getField(field).get(ob);
		} 
		catch (Exception e) 
		{
			System.err.println("Object: "+ob);
			throw new Error(e);
		}
	}
}
