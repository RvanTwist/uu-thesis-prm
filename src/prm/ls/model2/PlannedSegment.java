package prm.ls.model2;

import prm.ls.resources.ParalelResource;
import prm.ls.resources.ParalelResourceNode;
import prm.ls.resources.ParalelResourceSegment;
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
public class PlannedSegment extends ParalelResourceSegment
{	
	public final Segment segment;
	protected SegmentGroup segmentGroup;
	protected int segmentGroupOffset;
	
	// Real planning:
	final ParalelResourceSegment planned;
	
	public PlannedSegment(Segment s, ParalelResource area)
	{
		super(	area,
				s.prm.capRequirement,
				s.getSegmentTime());
		
		//System.out.println(" Area "+s.supervisingArea.id+" "+s.supervisingArea.isSingleTransport+"/"+s.isSingleTransport()+" cap:"+this.start.capChange);
		
		this.segment = s;
		
		this.start.setRepresents(this);
		
		this.planned = new ParalelResourceSegment(	null, 
													(s.prm.capRequirement),
													s.getSegmentTime() );
	}
	
	public String getMovabilityDebugString() 
	{
		final int start = this.getStartTime();
		final int earliestStart = this.segmentGroup.getErliestStart() + this.segmentGroupOffset;
		final int latestStart 	= this.segmentGroup.getLatestStart() + this.segmentGroupOffset;
		
		
		return start+":["+earliestStart+","+latestStart+"]";
	}
}
