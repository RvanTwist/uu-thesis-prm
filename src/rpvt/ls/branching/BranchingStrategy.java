package rpvt.ls.branching;

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
 * Template for a Branching Strategy.
 * @author rene
 *
 */
public abstract class BranchingStrategy<A>
{	
	/**
	 * Tries to branch, returns true if this object wields an sollution.
	 * 
	 * @param overviewer
	 * @return
	 */
	public abstract boolean branch();
	
	/**
	 * Look into the pending branches.
	 * Returns true if there are no branches left and we are done.
	 * @return
	 */
	public abstract boolean continueSearch();
	
	/**
	 * Returns the sollution of the state of this branch.
	 * @return
	 */
	public abstract A getSollution();
	
	/**
	 * Resets the object to its initial position.
	 */
	public abstract void clear();
	
	/**
	 * Get the number of open branches that are queued to process.
	 * @return
	 */
	public abstract int getOpenBranches();
	
	/**
	 * Do some onload stuff, required for look ahead strategies.
	 */
	public void onLoad(){}
}
