package prm.ls;

import java.util.ArrayList;
import java.util.LinkedList;

import prm.ls.model4.M4_MergedSegment;

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
 * Class managing backtracking.
 * 
 * @author rene
 */
public class BackTracker 
{
	private LinkedList<Backtrackable> supervised = new LinkedList<Backtrackable>();
	private ArrayList<Backtrackable> changes = new ArrayList<Backtrackable>();
	
	/**
	 * Register a backtrackable to be supervised by this.
	 * @param b
	 */
	public void addSupervised(Backtrackable b)
	{
		this.supervised.add(b);
	}
	
	/**
	 * Register a backtrackable to be changed.
	 * @param b
	 */
	public void addChanged(Backtrackable b)
	{
		this.changes.add(b);
	}
	
	/**
	 * Make checkpoints of all changed objects.
	 */
	public void makeCheckpoints()
	{
		for(Backtrackable b : this.changes)
		{
			b.makeCheckpoint();
		}
		
		this.changes.clear();
	}
	
	/**
	 * Make checkpoints of all supervised objects.
	 */
	public ArrayList<Backtrackable> supervisedCopy = new ArrayList<Backtrackable>();
	public void makeAllCheckpoints()
	{
		supervisedCopy.clear();
		supervisedCopy.addAll(supervised);
		
		for(Backtrackable b : this.supervisedCopy)
		{
			b.makeCheckpoint();
		}
		
		this.changes.clear();
	}
	
	/**
	 * Backtrack all objects.
	 */
	public void backtrack()
	{
		for(Backtrackable b : this.changes)
		{
			b.backtrack();
		}
		
		this.changes.clear();
	}

	public void remove(Backtrackable bt) 
	{
		this.supervised.remove(bt);
	}
}
