package prm.ls.model4.runs;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import rpvt.util.LineParser;

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
public class ResultsConverter extends JFrame
{
	ArrayList<InstanceResults> results;
	JTextArea text;
	
	public static void main(String[] args) throws IOException
	{
//		File f = new File("results/paper/M4_results_Simple_Matching.txt");
//		File f = new File("results/paper/M4_results_Match_Local.txt");
//		File f = new File("results/paper/M4_results_Full_ILP.txt");
		
		ResultsConverter r = new ResultsConverter();
		
		FileDialog fd = new FileDialog(r,"Open results file.",FileDialog.LOAD);
		fd.setDirectory(".");
		fd.setVisible(true);
		String fs = fd.getFile();
		
		if(fs == null)
		{
			System.exit(0);
		}
		
		File f = new File(fd.getDirectory()+fs);
		
//		File f = new File("results/paper/M4_results_Shifts1_Reduced_ILP3.txt");
		
		r.readResults(f);
		r.analyseDataAndReport1();
		
		r.setVisible(true);
		
	}
	
	public ResultsConverter()
	{
		this.setLayout(new BorderLayout());
		text = new JTextArea();
		this.add(text);
		
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(1);
			}
			@Override
			public void windowClosed(WindowEvent e)
			{
				System.exit(1);
			}
		});
	}
	
	public void analyseDataAndReport1()
	{
		String s = 	"\\begin{table}[H]\n"+
					"\\begin{tabular}{|p{2cm}|c|c| |c|c| |c|c| |c|c|}\n"+
					"\\hline\n"+
					"\\bf instance 	& \\bf time(ms) & \\bf iterations & \\multicolumn{2}{c||}{\\bf Best } & \\multicolumn{2}{c||}{\\bf Average } &  \\multicolumn{2}{c||}{\\bf Worst }\\\\ \n"+
					"\\hline\n"+
					"~				&			 &			  &	declined	& robust &	declined	& robust &	declined	& robust  \\\\ \n";
		
		for(InstanceResults instance : this.results)
		{
			int bestScore = Integer.MAX_VALUE;
			int bestRobust = Integer.MAX_VALUE;
			int bestDeclinedBooked = 0;
			int bestDeclined = 0;
			
			int worstScore = Integer.MIN_VALUE;
			int worstRobust = Integer.MIN_VALUE;
			int worstDeclined = 0;
			int worstDeclinedBooked = 0;
			
			int score_sum = 0;
			int robust_sum = 0;
			int declined_sum = 0;
			int declinedBooked_sum = 0;
			
			long time_sum = 0;
			int iteration_sum = 0;
			
			final int count = instance.results.size();
			
			for(RunResults r : instance.results)
			{
				if(r.score1 < bestScore || (r.score1 == bestScore && r.score_robustness < bestRobust))
				{
					bestScore = r.score1;
					bestRobust = r.score_robustness;
					bestDeclined = r.declined_total;
					bestDeclinedBooked = r.declined_booked;
				}
				if(r.score1 > worstScore || (r.score1 == worstScore && r.score_robustness > worstRobust))
				{
					worstScore = r.score1;
					worstRobust = r.score_robustness;
					worstDeclined = r.declined_total;
					worstDeclinedBooked = r.declined_booked;
				}
				
				score_sum += r.score1;
				robust_sum += r.score_robustness;
				declined_sum += r.declined_total;
				declinedBooked_sum += r.declined_booked;
				
				time_sum += r.computationTime;
				iteration_sum += r.iterations;
				
			}
			
			s += "\\hline \n"+
		 	 instance.instance+" & "+(time_sum / count)+" & "+(iteration_sum / count) + " &"+
		 	 						 (bestDeclined)+" & "+(bestRobust)+" & "+
		 	 						 String.format("%.1f",(1.0 * declined_sum / count))+" & "+ String.format("%.1f",(1.0 * robust_sum / count))+ "&"+
		 	 						 (worstDeclined)+" & "+(worstRobust)+" \\\\ \n";
			
		}
		
		s += "\\hline \n"+
			 "\\end{tabular}\n"+
			 "\\end{table}";
		
		this.text.setText(s);
	}
	
	public void readResults(File f) throws IOException
	{
		this.results = new ArrayList<InstanceResults>();
		
		LineParser parser = new LineParser(f);
		parser.readLine();
		String instanceline;
		
		while((instanceline = parser.skipTillLineStartsWith("Instance ")) != null)
		{
			String fileName = parser.readLine().replaceAll("[\\(\\) \t]", "");
			
			//System.out.println("Found FileName: ["+fileName+"]");
			InstanceResults ir = new InstanceResults(instanceline,fileName);
			this.results.add(ir);
			
			parser.skipTillLineStartsWith("seed ; time ; iterations ; total declined ; prebooked declined ; score ; robustness");
			
			String data;
			while( (data = parser.readLine()) != null && !data.equals("") )
			{
				ir.addResult(data);
			}
			System.out.println("Readed "+ir.results.size()+" runs of "+ir.instance);
		}
	}
	
	public class InstanceResults
	{
		String instance;
		String file;
		ArrayList<RunResults> results = new ArrayList<RunResults>();
		
		InstanceResults(String i, String f)
		{
			this.instance = i;
			this.file = f;
		}
		
		
		
		public void addResult(String data)
		{
			String[] dataA = data.split(";");
			
			RunResults r = new RunResults(	Long   .parseLong(dataA[0].trim()), //seed
											Long   .parseLong(dataA[1].trim()), //time
											Integer.parseInt (dataA[2].trim()), //iterations
											Integer.parseInt (dataA[3].trim()), //total declined
											Integer.parseInt (dataA[4].trim()), //prebooked declined
											Integer.parseInt (dataA[5].trim()), // score
											Integer.parseInt (dataA[6].trim())  // robustness
											);
			
			this.results.add(r);
		}
	}
	
	public class RunResults
	{
		// seed ; time ; iterations ; total declined ; prebooked declined ; score ; robustness
		
		final public long seed;
		final public long computationTime;
		final public int  iterations;
		
		final public int declined_total;
		final public int declined_booked;
		
		final public int score1;
		final public int score_robustness;
		
		public RunResults(long seed, long computationTime, int iterations,
				int declinedTotal, int declinedBooked, int score1,
				int scoreRobustness) 
		{
			this.seed = seed;
			this.computationTime = computationTime;
			this.iterations = iterations;
			declined_total = declinedTotal;
			declined_booked = declinedBooked;
			this.score1 = score1;
			score_robustness = scoreRobustness;
		}
		
		
	}
}
