package rpvt.ls.branching;
import java.util.ArrayList;
import java.util.Stack;

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
 * This object keeps track of changes done to the watched objects in the solver.
 * 
 * @author rene
 */
public class BackTrackEventManager 
{
	private static int id_dispenser = 0;
	Stack<BackTrackEvent> backTrackStack = new Stack<BackTrackEvent>();
	
	public int id;
	
//	BackTrackEvent top;
	ArrayList<Integer> markers = new  ArrayList<Integer>();
	
	/**
	 * Adds a marker on the backtrack stack for backtracking to this depth.
	 * When backtracking to the given depth all BackTrackEvent objects till the current one will be undone.
	 * @param depth
	 */
	public void addMarker(int depth)
	{
//		System.out.println("Set:Marker: "+depth+" h: "+height+" "+top);
		
		if(this.markers.size() == depth)
		{
			this.markers.add(depth, this.backTrackStack.size());
		}
		else
		{
			this.markers.set(depth, this.backTrackStack.size());
		}
	}
	
	/**
	 * Backtrack to stack size h.
	 * @param h
	 */
	public void backTrackTo(int h)
	{
		id = id_dispenser;
		id_dispenser++;
		
		int remove = this.backTrackStack.size() - h;
		for(int i = 0 ; i < remove ; i++)
		{
			backTrackStack.pop().undo();
		}
		
//		System.out.println("BackTrack to: "+h+" "+top);
	}
	
	/**
	 * Backtrack to the marker of depth d.
	 * @param depth
	 */
	public void backTrackToDepth(int depth)
	{
//		System.out.println("Backtrack to depth: "+depth);
		this.backTrackTo(this.markers.get(depth));
	}
	
	/**
	 * Clear the stack and markers.
	 */
	public void clear() 
	{
		this.markers.clear();
		this.backTrackStack.clear();
	}
	
	/**
	 * Return the number of backtrack traces in the stack.
	 * @return
	 */
	public int getHeight()
	{
		return this.backTrackStack.size();
	}
	
	/**
	 * Backtrack to stack size h and creates an RedoTrack which tells how to restore to the current state.
	 * @param h
	 * @return
	 */
	public RedoTrack reversableBackTrackTo(int h)
	{
		id = id_dispenser;
		id_dispenser++;
		
		int remove = this.backTrackStack.size() - h;
		BackTrackEvent[] es = new BackTrackEvent[remove];
		final int remove2 = remove - 1;
		for(int i = 0 ; i < remove ; i++)
		{
			BackTrackEvent e = backTrackStack.pop();
			e.undo();
			es[remove2 - i] = e;
		}
		
		return new RedoEventManager(es,h,this);
//		System.out.println("BackTrack to: "+h+" "+top);
	}

	/**
	 * Backtrack to the marker of depth d and creates an RedoTrack which tells how to restore to the current state.
	 * @param depth
	 * @return
	 */
	public RedoTrack reversableBackTrackToDepth(int depth)
	{
		return this.reversableBackTrackTo(this.markers.get(depth));
	}
}
