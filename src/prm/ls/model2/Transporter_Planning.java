package prm.ls.model2;

import prm.ls.resources.ParalelResource;
import prm.ls.resources.ParalelResourceSegment;
import prm.problemdef.Transport;

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
public class Transporter_Planning 
{
	final Transport transport;
	
	ParalelResource planning;
	
	public Transporter_Planning(Transport t)
	{
		this.transport = t;
		planning = new ParalelResource(t.capicity);
	}
	
	public void addSegment(PlannedSegment ps)
	{
		ParalelResourceSegment rs = ps.planned;
		
		//planning.planEarliestFrom(rs,ps.getStartTime());
	}
}
