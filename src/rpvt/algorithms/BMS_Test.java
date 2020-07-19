package rpvt.algorithms;

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
public class BMS_Test 
{
	public static void main(String[] args)
	{
		System.out.println("Running Bipartite Matching test");
		
		Bipartite_Matching_Solver solver = new Bipartite_Matching_Solver<String, Integer>();
		
		String[] strings = new String[]{"S1","S2","S3","S4","S5"};
		Integer[] ints	 = new Integer[]{1,2,3,4};
		
		BMS_Node<String>[] bms_strings = new BMS_Node[strings.length];
		BMS_Node<String>[] bms_ints = new BMS_Node[ints.length];
		
		for(int i = 0 ; i < strings.length ; i++)
		{
			if(i == 4)
			{
				bms_strings[i] = solver.makeLeftNode(strings[i],5);
			}
			else
			{
				bms_strings[i] = solver.makeLeftNode(strings[i]);
			}
		}
		
		for(int i = 0 ; i < ints.length ; i++)
		{
			bms_ints[i] = solver.makeRightNode(ints[i]);
		}
		
		// Make possible matchings
		
		bms_strings[0].makeEdgeTo(bms_ints[0]);
		
		bms_strings[1].makeEdgeTo(bms_ints[0]);
		bms_strings[1].makeEdgeTo(bms_ints[2]);
		
		bms_strings[2].makeEdgeTo(bms_ints[1]);
		bms_strings[2].makeEdgeTo(bms_ints[2]);
		bms_strings[2].makeEdgeTo(bms_ints[3]);
		
		bms_strings[3].makeEdgeTo(bms_ints[2]);
		
		bms_strings[4].makeEdgeTo(bms_ints[2]);
		System.out.println("End initialisation");
		
		solver.findPrioritisedMaximumMatching();
		
		System.out.println("End finding matching");
		
		BMS_Matching<String,Integer>[] matching = solver.getMatching();
		System.out.println("Matching("+matching.length+") :");
		
		for(BMS_Matching<String,Integer> m : matching)
		{
			System.out.println("Match: "+m.left+" => "+m.right);
		}
		
		System.out.println("End test");
	}
}
