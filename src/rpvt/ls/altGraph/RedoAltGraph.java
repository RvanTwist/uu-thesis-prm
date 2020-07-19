package rpvt.ls.altGraph;

import java.util.ArrayList;

import rpvt.ls.branching.RedoTrack;

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
/**
 * This object can undo changes done in the alternative graph while backtracking.
 * @author rene
 */
public class RedoAltGraph extends RedoTrack
{
	/**
	 * Object to store edge - state assignments.
	 * 
	 * @author rene
	 */
	private class RedoEdgeSel
	{
		final Edge e;
		final EdgeState st;
		final int depth;
		
		RedoEdgeSel(Edge e)
		{
			this.e = e;
			this.st = e.state;
			this.depth = e.setDepth;
		}
		
		/**
		 * Restores state.
		 */
		public void redo()
		{
			e.setDir(st, depth);
		}
	}
	
	ArrayList<RedoEdgeSel> es = new ArrayList<RedoEdgeSel>();
	
	/**
	 * Reverts an alternative edge to its unselected form and store the state of the edge in this object to redo.
	 * @param e
	 */
	public void addRedo(Edge e)
	{
		es.add(new RedoEdgeSel(e));
		e.state = EdgeState.EdgeStateUnknown;
		e.setDepth = -1;
		e.graph.unsetEdges++;
		
//		e.graph.checkCount();
	}
	
	@Override
	public void restore() 
	{
		for(RedoEdgeSel e : es)
		{
			e.redo();
		}
	}
}
