package prm.ls;

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
public abstract class AbstractBackTrackable implements Backtrackable
{
	public final BackTracker backtracker;
	private boolean noChanges;
	
	public AbstractBackTrackable(BackTracker bt)
	{
		this.backtracker = bt;
		this.noChanges = true;
		
		bt.addSupervised(this);
	}
	
	public void registerChange()
	{
		if(noChanges)
		{
			noChanges = false;
			this.backtracker.addChanged(this);
		}
	}

	@Override
	public final void backtrack() 
	{
		this.noChanges = true;
		this.doBacktrack();
	}

	@Override
	public final void makeCheckpoint() 
	{
		this.noChanges = true;
		this.doMakeCheckpoint();
	}
	
	protected abstract void doBacktrack();
	protected abstract void doMakeCheckpoint();
}
