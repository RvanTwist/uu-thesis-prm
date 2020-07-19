package tests;

import otheralgorithms.DynamicHungarianAlgorithm;

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
public class GeneralTests 
{
	public static void main(String[] args)
	{
		hungarianTest();
	}
	
	public static void hungarianTest()
	{
		DynamicHungarianAlgorithm dha = new DynamicHungarianAlgorithm();
		dha.prepareCostMatrix(2, 2);
		
		dha.costMatrix[0][0] = 1; dha.costMatrix[0][1] = 2;
		dha.costMatrix[1][0] = 2; dha.costMatrix[1][1] = 8;
		
		int[] matching = dha.execute();
		System.out.println("Matching: ["+matching[0] +", "+matching[1]+"]");
		
		
		dha.costMatrix[1][1] = 1;
		dha.updateRow(1);
		matching = dha.results();
		System.out.println("Matching: ["+matching[0]+", "+matching[1]+"]");
		
		dha.costMatrix[1][1] = 8;
		dha.updateRow(1);
		matching = dha.results();
		System.out.println("Matching: ["+matching[0]+", "+matching[1]+"]");
		
		dha.costMatrix[1][0] = -1;
		dha.costMatrix[1][1] = -1;
		dha.updateRow(1);
		matching = dha.results();
		System.out.println("Matching: ["+matching[0]+", "+matching[1]+"]");
	
		
		dha.prepareCostMatrix(2, 2);
		dha.costMatrix[0][0] = 1; dha.costMatrix[0][1] = 2;
		dha.costMatrix[1][0] = -1; dha.costMatrix[1][1] = -1;
		
		matching = dha.execute();
		System.out.println("Matching: ["+matching[0] +", "+matching[1]+"]");
		
	}
	
	/**
	 * Seriously can't remember where this test is about.
	 */
	public static void forgottenTest()
	{
		for(int i = 0 ; i < 21 ; i++)
		{
			System.out.println(i+":"+calculateRobustness1(i));
		}
		
		final int runs = 100000;
		long time;
		
		time = System.currentTimeMillis();
		for(int i = 0 ; i < runs ; i++)
		{
			runR1();
		}
		System.out.println("Runs1 time "+(System.currentTimeMillis()-time));
		
		time = System.currentTimeMillis();
		for(int i = 0 ; i < runs ; i++)
		{
			runR2();
		}
		System.out.println("Runs2 time "+(System.currentTimeMillis()-time));
		
		time = System.currentTimeMillis();
		for(int i = 0 ; i < runs ; i++)
		{
			runR1();
		}
		System.out.println("Runs1 time "+(System.currentTimeMillis()-time));
		
		time = System.currentTimeMillis();
		for(int i = 0 ; i < runs ; i++)
		{
			runR2();
		}
		System.out.println("Runs2 time "+(System.currentTimeMillis()-time));
	}
	
	public static void runR1()
	{
		for(int i = 0 ; i < 21 ; i++)
		{
			calculateRobustness1(i);
		}
	}
	
	public static void runR2()
	{
		for(int i = 0 ; i < 21 ; i++)
		{
			calculateRobustness2(i);
		}
	}
	
	public static int calculateRobustness1(int slack)
	{
		slack = 20 - Math.min(slack, 20);
		
		return slack * slack; 
	}
	
	static double HALF_PI = Math.PI / 2.0;
	public static double calculateRobustness2(int slack)
	{	
		final double v = 100 * (Math.atan(-0.51 * slack ) + HALF_PI);
		return v;
	}
}
