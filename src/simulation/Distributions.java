package simulation;

import java.util.Random;

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
public final class Distributions 
{
	private static int NormalDist_Draws = 24;
	private static double NormalDist_Min = NormalDist_Draws / 2.0;
	
	public static double randomExp(double lambda)
	{	
		return -(Math.log(Math.random()))/lambda;
	}
	
	public static double randomExp(double lambda, Random r)
	{	
		return -(Math.log(r.nextDouble()))/lambda;
	}
	
	public static double randomGamma(int k, double theta)
	{
		double g = 0;
		
		for(int i = 0 ; i < k ; i++)
		{
			g += -Math.log(Math.random());
		}
		
		return theta * g;
	}
	
	public static double randomGamma(int k, double theta, Random rand)
	{
		double g = 0;
		
		for(int i = 0 ; i < k ; i++)
		{
			g += -Math.log(rand.nextDouble());
		}
		
		return theta * g;
	}
	
	public static double randomUniform(double min, double max)
	{
		return min + (max - min) * Math.random();
	}
	
	public static double randomNormalVar(double mean, double var)
	{
		// Get a value u = Norm(0,1)
		double u = 0;
		
		for(int i = 0 ; i < Distributions.NormalDist_Draws ; i++)
		{
			u += Math.random();
		}
		
		u -= Distributions.NormalDist_Min;
		
		// Normalise
		return Math.sqrt(var)*u + mean;
	}
	
	public static double randomNormalVar(double mean, double var,Random random)
	{
		// Get a value u = Norm(0,1)
		double u = 0;
		
		for(int i = 0 ; i < Distributions.NormalDist_Draws ; i++)
		{
			u += random.nextDouble();
		}
		
		u -= Distributions.NormalDist_Min;
		
		// Normalise
		return Math.sqrt(var)*u + mean;
	}
	
	public static double randomNormal(double mean, double standartDev)
	{
		// Get a value u = Norm(0,1)
		double u = 0;
		
		for(int i = 0 ; i < Distributions.NormalDist_Draws ; i++)
		{
			u += Math.random();
		}
		
		u -= Distributions.NormalDist_Min;
		
		// Normalise
		return standartDev*u + mean;
	} 
	
	public static double randomNormal(double mean, double standartDev, Random r)
	{
		// Get a value u = Norm(0,1)
		double u = 0;
		
		for(int i = 0 ; i < Distributions.NormalDist_Draws ; i++)
		{
			u += r.nextDouble();
		}
		
		u -= Distributions.NormalDist_Min;
		
		// Normalise
		return standartDev*u + mean;
	}
}
