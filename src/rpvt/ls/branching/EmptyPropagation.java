package rpvt.ls.branching;

/*
 * This program has been developed by Rene van Twist for his master's thesis (under supervision of Han Hoogeveen) within the master program Computing Science at Utrecht University  
 * �Copyright Utrecht University (Department of Information and Computing Sciences)
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
 * This propagator does absolutly nothing!
 * @author rene
 */
public class EmptyPropagation implements PropagationStrategy
{	
	public static EmptyPropagation empty = new EmptyPropagation();
	
	public EmptyPropagation(){}

	@Override
	public boolean propagate() 
	{
		return false;
	}
}