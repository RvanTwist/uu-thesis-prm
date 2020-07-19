package prm.ls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import prm.ls.model2.SimplePlanning;
import prm.ls.model4.CantPlanException;
import prm.ls.resources.ParalelResource;

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
public class SimulatedAnnealing 
{	
	public final static long SECONDS = 1000;
	public final static long MINUTES = SECONDS * 60;
	public final static long HOURS   = MINUTES * 60;
	
	
	public Random random;
	
	public final LS_Solution solution;
	MutationSelector mutationSelector;
	
	double Tstart;
	double a;
	int Q;
	int I;
	
	long timesLimit = Long.MAX_VALUE;
	
	boolean allowInfeasibility = false;
	
	ArrayList<LocalSearchListener> listeners = new ArrayList<LocalSearchListener>();
	
	// Runtime variables
	int iteration;
	double T;
	int scoreBest;
	int score;
	int score2;
		
	public SolutionFacture facture = new SolutionFacture();
	public Mutation lastMutation;
	
	long solving_start;
	long solving_end;
	
	boolean not_interupted = true;
	boolean running = true;
	
	public void setTimeLimit(long time)
	{
		this.timesLimit = time;
	}
	
	public void addLocalSearchListener(LocalSearchListener l)
	{
		this.listeners.add(l);
	}
	
	public void removeLocalSearchListener(LocalSearchListener l)
	{
		this.listeners.remove(l);
	}
	
	public void removeAllLocalSearchListeners()
	{
		this.listeners.clear();
	}
	
	public SimulatedAnnealing(LS_Solution s, MutationSelector ms, double T, double a, int q, int i)
	{
		this(s, ms, T, a, q, i, new Random());
	}
	
	public SimulatedAnnealing(LS_Solution s, MutationSelector ms, double T, double a, int q, int i, Random r  )
	{
		this.solution = s;
		this.mutationSelector = ms;
		ms.setSolver(this);
		this.Tstart = T;
		this.T = T;
		this.a = a;
		this.Q = q;
		this.I = i;
		
		this.random = this.random = (r == null ? new Random() : r);;
	}
	
	public SimulatedAnnealing(LS_Solution s, Collection<Mutation> mutations, double T, double a, int q, int i)
	{
		this(s,mutations,T,a,q,i,new Random());
	}
	
	public SimulatedAnnealing(LS_Solution s, Collection<Mutation> mutations, double T, double a, int q, int i, Random r )
	{
		this.solution = s;
		this.mutationSelector = new DefaultMutationSelector(mutations);
		this.mutationSelector.setSolver(this);
		this.Tstart = T;
		this.T = T;
		this.a = a;
		this.Q = q;
		this.I = i;
		
		this.random = this.random = (r == null ? new Random() : r);;
	}
	
	public SimulatedAnnealing(LS_Solution s, Mutation[] mutations, double T, double a, int q, int i)
	{
		this(s,mutations,T,a,q,i,new Random());
	}
	
	public SimulatedAnnealing(LS_Solution s, Mutation[] mutations, double T, double a, int q, int i, Random r )
	{
		this.solution = s;
		this.mutationSelector = new DefaultMutationSelector(mutations);
		this.mutationSelector.setSolver(this);
		
		this.Tstart = T;
		this.T = T;
		this.a = a;
		this.Q = q;
		this.I = i;
		
		this.random = this.random = (r == null ? new Random() : r);;
	}
	
	public void setAllowInfeasibility(boolean b)
	{
		this.allowInfeasibility = b;
	}
	
	public void setIterations(int I)
	{
		this.I = I;
	}
	
	public void setDecrease(int Q)
	{
		this.Q = Q;
	}
	
	public void setStartTemprature(double T)
	{
		this.Tstart = T;
	}
	
	public void setAlpha(double a)
	{
		this.a = a;
	}
	
	public int getIterations()
	{
		return I;
	}
	
	public int getIteration()
	{
		return this.iteration;
	}
	
	public double getDecrease()
	{
		return Q;
	}
	
	public double getStartTemprature()
	{
		return Tstart;
	}
	
	public double getTemprature()
	{
		return T;
	}
	
	public double getAlpha()
	{
		return a;
	}
	
	
	public void solve()
	{
		this.running = true;
		this.not_interupted = true;
		
		solving_start = System.currentTimeMillis();
		
//		System.out.println("I: "+I);
		
		T = this.Tstart;
		
		this.mutationSelector.initialise();
		
		this.iteration = 0;

		// Fire listeners:
		for(int li = 0 ; li < this.listeners.size() ; li++)
		{
			LocalSearchListener l = this.listeners.get(li);
			l.onInitialise(this);
		}
		
		while(iteration < this.I && not_interupted)
		{
			for(int i = 0 ; i < this.Q && iteration < this.I && not_interupted; i++)
			{
				
				iteration++;
				if(!this.solution.isFeasible())
				{
					throw new Error("Starting with infeasible solution!");
				}
				
//				this.solution.testConsistancy();
				
				final Mutation mutation = this.mutationSelector.getMutation();
				this.doMutation(mutation);
				
//				this.solution.testConsistancy();
				
				if(!this.solution.isFeasible())
				{
					throw new Error("Ending with infeasible solution!");
				}
				
				if(System.currentTimeMillis() - this.solving_start >= this.timesLimit)
				{
					System.out.println("Interupted: out of time!");
					this.not_interupted = false;
				}
				
			}
			
			// 1 Q round finished!
			T = T * a;
			System.out.println(" Q round finished: "+this.solution.getScore()+"|"+this.solution.getScore2()+" T: "+T+" "+this.solution.getStatusText() +" "+((iteration * 100) /this.I)+"% finished");
			System.gc();
			System.out.println(" - Memory usage: free:"+Runtime.getRuntime().freeMemory()+" runtime: "+formatTime(System.currentTimeMillis()-solving_start));
			//System.out.println(" ? "+iteration+" / "+I);
		}
		
		//if(score < this.scoreBest)
		if(solution.isImprovedSinceBest())
		{
			this.scoreBest = score;
			solution.saveBest();
		}
		
		solving_end = System.currentTimeMillis();
		System.out.println("Solving end time: "+formatTime(solving_end-solving_start)+" iterations: "+this.iteration);
		for(int li = 0 ; li < this.listeners.size() ; li++)
		{
			LocalSearchListener l = this.listeners.get(li);
			l.onFinish(this);
		}
		
		running = false;
	}
	
	public String formatTime(long t)
	{
		long mil = t % 1000;
		long sec = (t / 1000)%60;
		long min = (t / 60000)%60;
		long hour = (t / 3600000);
		 
		return formatZeros(hour,2)+" hours "+formatZeros(min,2)+" minutes "+formatZeros(sec,2)+" seconds "+formatZeros(mil,4)+" miliseconds";
	}
	
	public String formatZeros(long t, int z)
	{
		String s = ""+t;
		long treshold = 10;
		
		while(z > 1)
		{
			if(t < treshold)
				s = "0"+s;
			treshold *= 10;
			z--;
		}
		
		return s;
	}
	
	public void doMutation(Mutation m)
	{	
//		System.out.println("Do mutation: "+m.getClass().getSimpleName());
		
		this.lastMutation = m;
		
		this.facture.clear();
		try
		{
			m.generateMutation(this.facture);
		}
		catch(CantPlanException e)
		{
			this.facture.feasible = false;
		}
		
		this.lastMutation = m;
		for(int li = 0 ; li < this.listeners.size() ; li++)
		{
			LocalSearchListener l = this.listeners.get(li);
			l.postProcessFacture(this, this.facture);
		}
		
		if(this.facture.feasible)
		{	
			final int costs = facture.getCosts();
			if(facture.accept || costs <= 0 || random.nextDouble() < Math.exp(-costs/T))
			{
				if(costs > 0)
				{
					// accepting an worse state
					if(solution.isImprovedSinceBest())
					{
						this.scoreBest = score;
						solution.saveBest();
					}
					
					m.applyMutation();
					score += this.facture.costs;
					
				}
				else
				{
					// Score improved
					
					m.applyMutation();
					score += this.facture.costs;
					
					// Check or we found an optimum:
					if(solution.isOptimum())
					{
						System.out.println("Found optimum!");
						this.not_interupted = false;
					}
				}
				
//				System.out.println("Mutation Accepted!");
//				if(!this.solution.isFeasible())
//				{
//					throw new Error("Sollution is infeasible beforehand.");
//				}
				
				// bug checks.
				if(this.facture.feasible && !this.solution.isFeasible())
				{
					if(this.solution instanceof SimplePlanning)
					{
						SimplePlanning sp = (SimplePlanning)this.solution;
						
						m.debug();
						
						for(ParalelResource r : sp.resourceMap.values())
						{
							r.calculateScore();
						}
						
						
						System.out.println("Incorrect prediction detected!");
						sp.analyse();
						
						if(sp.isFeasible())
						{
							throw new Error(	"SP: Incorrect mutation prediction is unfeasible: "+m+"\n" +
												"    Recalculating is feasible.");
						}
						else
						{
							throw new Error(	"SP: Incorrect mutation prediction is unfeasible: "+m);
						}
					}
					
					
					throw new Error("Incorrect mutation prediction is unfeasible: "+m);
				}
				
				for(int li = 0 ; li < this.listeners.size() ; li++)
				{
					LocalSearchListener l = this.listeners.get(li);
					l.onAccept(this);
				}
			}
			else
			{
//				System.out.println("Mutation Rejected!");
				m.rejectMutation();
				
				for(int li = 0 ; li < this.listeners.size() ; li++)
				{
					LocalSearchListener l = this.listeners.get(li);
					l.onReject(this);
				}
			}
		}
		else
		{
//			System.out.println("Mutation Rejected!");
			m.rejectMutation();
			for(int li = 0 ; li < this.listeners.size() ; li++)
			{
				LocalSearchListener l = this.listeners.get(li);
				l.onReject(this);
			}
		}
	}

	public String printSettings() 
	{
		return 	"Simulated Annealing\n"+
				" - Iterations    : "+this.I+"\n"+
				" - T start       : "+this.Tstart+"\n"+
				" - T delta per Q : "+this.a+"\n"+
				" - Q             : "+this.Q+"\n"+
				" - Mutation Strategy: "+this.mutationSelector.printSettings()+"\n"+
				" - bestScore     : "+this.scoreBest+"\n"+
				" - solving time  : "+this.formatTime(this.solving_end - this.solving_start);
	}

	public long getLastSolveTime() 
	{
		return this.solving_end - this.solving_start;
	}
}
