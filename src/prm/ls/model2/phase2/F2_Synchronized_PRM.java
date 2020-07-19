package prm.ls.model2.phase2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import prm.problemdef.Segment;

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
public class F2_Synchronized_PRM 
{
	Segment seg;
	int segStart;
	ArrayList<F2_PRM> prms;
	int capReq;
	
	boolean matched = false;
	
	public F2_Synchronized_PRM()
	{
		
	}
	
	public F2_Synchronized_PRM(Segment s, int t, F2_PRM prm) 
	{
		this.seg = s;
		this.segStart = t;
		this.prms = new ArrayList<F2_PRM>();
		prms.add(prm);
		
		capReq = prm.getCapicityReq();
	}
	
	public F2_Synchronized_PRM(Segment s, int t, Collection<F2_PRM> prms) 
	{
		this.seg = s;
		this.segStart = t;
		this.prms = new ArrayList<F2_PRM>(prms);
		
		this.capReq = 0;
		
		for(F2_PRM prm : prms)
		{
			this.capReq += prm.getCapicityReq();
		}
	}

	public String printPRMs() 
	{
		String rtrn = "{";
		
		for(F2_PRM prm : this.prms)
		{
			rtrn += " prm: "+prm.prm.prm.prm_ID;
		}
		
		return rtrn+" }";
	}
}
