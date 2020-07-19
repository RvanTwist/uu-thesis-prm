package prm.ls.model4;

import java.util.ArrayList;

import prm.ls.model4.matching.Matching_ShiftFixer;
import prm.ls.resources.ParalelResource;
import prm.problemdef.*;

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
public class M4_Area 
{	
	public final M4_Planning planning;

	public final Area area;
	
	public ArrayList<M4_Worker> workers = new ArrayList<M4_Worker>();
	
	int minCap_workers;
	
	public ParalelResource<M4_PRM_Segment> resource;
	
	
	// Extra's
	public Matching_ShiftFixer shiftfix = null;
	
	public M4_Area(Area a,M4_Planning planning)
	{
		this.planning = planning;
		this.area = a;
		
		this.minCap_workers = Integer.MAX_VALUE;
		
		for(Transport t : a.transporters)
		{
			final M4_Worker worker = new M4_Worker(t,this);
			this.workers.add(worker);
			this.minCap_workers = Math.min(minCap_workers, t.capicity);
		}
		
		if(M4_Constants.useParalelResourceScore)
		{
			int maxCap = 0;
			for(Transport t : a.transporters)
			{
				maxCap += t.capicity;
			}
			
			this.resource = new ParalelResource<M4_PRM_Segment>(maxCap);
		}
	}
	
	public int getMinCapWorkers()
	{
		return this.minCap_workers;
	}

	public Area getArea() 
	{
		return area;
	}

	public M4_Worker getWorker(Transport transport) 
	{
		for(M4_Worker w : this.workers)
		{
			if(w.transport.transport_ID == transport.transport_ID)
			{
				return w;
			}
		}
		
		return null;
	}
	 
	public String toString()
	{
		return "Area "+area.id;
	}
}
