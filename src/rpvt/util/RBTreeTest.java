package rpvt.util;

import java.util.Set;
import java.util.TreeSet;

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
public class RBTreeTest 
{
	public static void main(String[] args)
	{
		RBTree<Integer> rb = new RBTree<Integer>();
		
		int[] ints =  new int[]{1,2,3,4,5,6,7,8,9,0};
		
		int[] ints2 = new int[]{2,6,4,9,7,5,1,8,3};
		
		for(int i = 0 ; i < ints.length ; i++)
		{
			rb.add(ints[i]);
			
			System.out.println(rb.toString());
		}
		
		for(int i = 0 ; i < 20 ; i++)
		{
			System.out.println("Random: "+rb.getRandomValue());
		}
	}
	
	public static void doTest(String name, Set<Integer> set, int[] numbers)
	{
		long start_Insert;
		long start_Delete;
		long time_Insert; 
		long time_Delete;
		
		System.out.println("Test: "+name);
		
		start_Insert = System.currentTimeMillis();
		
		for(int i = 0 ; i < numbers.length ; i++)
		{
			set.add(numbers[i]);
		}
		
		time_Insert = System.currentTimeMillis() - start_Insert;
		System.out.println("  - Insert: "+time_Insert);
		
		start_Delete = System.currentTimeMillis();
		
		for(int i = 0 ; i < numbers.length ; i++)
		{
			set.add(numbers[i]);
		}
		
		time_Delete = System.currentTimeMillis() - start_Delete;
		System.out.println("  - Delete: "+time_Delete);
		
		set.clear();
		System.gc();
	}
	
	public static void oldTest(String[] args)
	{
		Integer[] ints = new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18};
		//Integer[] ints = new Integer[]{10,85,15,70,20,60,30,50,65};
		
		RBTree<Integer> tree = new RBTree<Integer>();
		
		for(Integer i : ints)
		{
			tree.add(i);
		}
		
		System.out.println("Debug!");
		System.out.println(""+tree.toString());
		
		System.out.println("Depth: "+tree.root.treeDepth()+" "+(Math.log(ints.length)/Math.log(2)));
		
		
		tree.remove(8);
		System.out.println(" Lower: "+tree.getlowerNode(8));
		System.out.println(" Heigher: "+tree.gethigherNode(8));
		tree.add(8);
		
		int c = 0;

		System.out.println("Iterator test");
		String s = "";
		for(int i : tree)
		{
			s += " "+i;
		}
		System.out.println(" result: "+s);
		
		System.out.println("Iterator test2");
		s = "";
		
		for(int i : tree.subSet(9, 13))
		{
			s += " "+i;
		}
		System.out.println(" result: "+s);
		
		tree.remove(5);
		tree.remove(6);
		
		RBTreeSubSet<Integer> subset = tree.subSet(5, 6);
		subset.extendSetLower();
		subset.extendSetHigher();
		
		System.out.println("Iterator test3 subset 5,8 +1");
		s = "";
		for(int i : subset)
		{
			s += " "+i;
		}
		System.out.println(" result: "+s);
	}
}
