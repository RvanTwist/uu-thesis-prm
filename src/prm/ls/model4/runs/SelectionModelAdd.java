package prm.ls.model4.runs;

import javax.swing.DefaultListSelectionModel;

/*
 * This program has been developed by Rene van Twist for his master's thesis (under supervision of Han Hoogeveen) within the master program Computing Science at Utrecht University  
 * ęCopyright Utrecht University (Department of Information and Computing Sciences)
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
public class SelectionModelAdd extends DefaultListSelectionModel
{
	
	@Override
	public void setSelectionInterval(int i1, int i2)
	{
		if(i1 > i2)
		{
			setSelectionInterval(i2,i1);
			return;
		}
		
		boolean all_Selected = true;
		for(int i = i1 ; i <= i2; i++)
		{
			if(!this.isSelectedIndex(i))
			{
				all_Selected = false;
			}
		}
		
		if(all_Selected)
			super.removeSelectionInterval(i1, i2);
		else
			super.addSelectionInterval(i1, i2);
	}
}
