package prm.ls.model4;

import prm.ls.SimulatedAnnealing;

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
public class M4_Constants 
{
	public static int declinePenalty = 400; // Old 400
	public static int declinePenaltyBooked = 1200; // Old 1200
	
	// random.nextDouble() < Math.exp(-costs/T)
	public final static double T = 400;
	public final static double a = 0.95;
	public final static int iterations = 50000; //500000; 
	public final static int iterationsQ = 100;
	
	public static boolean useParalelResourceScore = false; // Note declinePanelty should be higher if using this option or scale the score.
	public static boolean optimizeRobustness = false;
	public static long timeLimit = 2 * SimulatedAnnealing.MINUTES;
	
	public static final boolean Debug = false;
	public static final boolean ReportMessages = false; 
	
	public static boolean StopAtOptimum = true;
//	public static boolean UseRobustness = false;
}
