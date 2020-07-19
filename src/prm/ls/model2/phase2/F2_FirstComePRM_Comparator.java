package prm.ls.model2.phase2;

import java.util.Comparator;


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
final public class F2_FirstComePRM_Comparator implements Comparator<F2_PRM>
{
	
	private static F2_FirstComePRM_Comparator instance = null;
	
	public static F2_FirstComePRM_Comparator getInstance()
	{
		if(instance == null)
		{
			instance = new F2_FirstComePRM_Comparator();
		}
		
		return instance;
	}

	@Override
	public int compare(F2_PRM prm1, F2_PRM prm2) 
	{
		if(prm1.currentTime == prm2.currentTime)
		{
			return prm1.prm.compareTo(prm2.prm);
		}
		else if(prm1.currentTime < prm2.currentTime)
		{
			return -1;
		}
		else
		{
			return 1;
		}
	}
}
