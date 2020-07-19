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
public class IntDataColumn<A> extends DataColumn<A>
{

	double mean;
	double variance;
	
	public IntDataColumn(ArrayList<A> list, String field) 
	{
		super(list, field);
	}
	
	public IntDataColumn(ArrayList<A> list, String field, String name, boolean b) {
		super(list, field, name, b);
	}

	public IntDataColumn(ArrayList<A> list, String field, String name) {
		super(list, field, name);
	}

	public void calculateStats()
	{
		// Mean
		double mean_sum = 0;
		double varianceSum_p1 = 0;
		
		for(int i = 0 ; i < this.list.size(); i++)
		{
			Object ob = this.getRow(i);
			double value;
			
			if(ob instanceof Integer)
				value = ((Integer)ob).doubleValue();
			else if(ob instanceof Double)
				value = ((Double)ob).doubleValue();
			else
			{
				throw new Error("Data is neither a double or an int! "+ob.getClass());
			}
			
			mean_sum += value;
			varianceSum_p1 += (value * value);
		}
		
		this.mean = mean_sum / this.list.size();
		this.variance = (varianceSum_p1 / this.list.size()) - (this.mean * this.mean);
		
		// Variance
		
		
	}
	
	public double getMean()
	{
		return this.mean;
	}
	
	public double getVar()
	{
		return this.variance;
	}
	
}
