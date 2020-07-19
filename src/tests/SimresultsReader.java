package tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import rpvt.util.DefaultWindowListener;
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
public class SimresultsReader extends JFrame
{
	ArrayList<SimResultsData> data = new ArrayList<SimResultsData>();
	
	JScrollPane tableScroll;
	JTextArea   table;
	
	
	DataColumn<SimResultsData> dc_instance 		= new DataColumn<SimResultsData>(data,"instance","instance", true);
	DataColumn<SimResultsData> dc_seed 			= new DataColumn<SimResultsData>(data,"seed","seed");
	
	IntDataColumn<SimResultsData> dc_rb_dec_b 	= new IntDataColumn<SimResultsData>(data,"rb_dec_booked", "booked");
	IntDataColumn<SimResultsData> dc_rb_dec_ub 	= new IntDataColumn<SimResultsData>(data,"rb_dec_unbooked","unbooked");
	IntDataColumn<SimResultsData> dc_rb_wait 	= new IntDataColumn<SimResultsData>(data,"rb_wait","wait");
	IntDataColumn<SimResultsData> dc_rb_ru 		= new IntDataColumn<SimResultsData>(data,"rb_updates","updates");
	IntDataColumn<SimResultsData> dc_rb_rf 		= new IntDataColumn<SimResultsData>(data,"rb_reschedule","reschedule");
	
	IntDataColumn<SimResultsData> dc_nrb_dec_b 		= new IntDataColumn<SimResultsData>(data,"nrb_dec_booked", "booked");
	IntDataColumn<SimResultsData> dc_nrb_dec_ub 	= new IntDataColumn<SimResultsData>(data,"nrb_dec_unbooked","unbooked");
	IntDataColumn<SimResultsData> dc_nrb_wait 		= new IntDataColumn<SimResultsData>(data,"nrb_wait","wait");
	IntDataColumn<SimResultsData> dc_nrb_ru 		= new IntDataColumn<SimResultsData>(data,"nrb_updates","updates");
	IntDataColumn<SimResultsData> dc_nrb_rf 		= new IntDataColumn<SimResultsData>(data,"nrb_reschedule","reschedule");
	
	
	public static void main(String[] args)
	{
		new SimresultsReader().setVisible(true);
	}
	
	public SimresultsReader()
	{
		this.addWindowListener(new DefaultWindowListener());
		
		this.readInstances();
		
		this.table = new JTextArea();
		this.tableScroll = new JScrollPane(table);
		this.add(tableScroll);
		
		DataColumn[] columns1 = new DataColumn[]{	 this.dc_instance
													,this.dc_seed
													,this.dc_rb_dec_b
													,this.dc_rb_dec_ub
													,this.dc_rb_wait
													,this.dc_nrb_dec_b
													,this.dc_nrb_dec_ub
													,this.dc_nrb_wait
												};
		
		DataColumn[] columns_all = new DataColumn[]{	 this.dc_instance
														,this.dc_seed
														,this.dc_rb_dec_b
														,this.dc_rb_dec_ub
														,this.dc_rb_wait
														,this.dc_rb_ru
														,this.dc_rb_rf
														,this.dc_nrb_dec_b
														,this.dc_nrb_dec_ub
														,this.dc_nrb_wait
														,this.dc_nrb_ru
														,this.dc_nrb_rf
													};
		
//		this.createTable(columns1);
		this.createTableCSV(columns_all);
	}
	
	
	private void createTableCSV(DataColumn[] columns) 
	{
		String tableData = "";
		
		boolean first = true;
		
		for(DataColumn dc : columns)
		{
			
			tableData += (first?"" : ",")+dc.name;
			
			first = false;
			
			if(dc instanceof IntDataColumn)
				((IntDataColumn)dc).calculateStats();
		}
		
		for(int i = 0 ; i < this.data.size() ; i++)
		{
			first = true;
			
			for(DataColumn dc : columns)
			{
				Object ob = dc.getRow(i);
				if(ob instanceof Double)
					tableData += (first? "" : ",")+((Double)ob).intValue();
				else
					tableData += (first? "" : ",")+ob;
				
				first = false;
			}
			
			tableData += "\n";
		}
		
		this.table.setText(tableData);
		
		
	}
	
	private void createTable(DataColumn[] columns) 
	{
		
		String tableHeader = "|";
		String tableData = "\t\\hline\n";
		
		boolean first = true;
		
		for(DataColumn dc : columns)
		{
			tableHeader += "p|";
			
			tableData += (first?"\t" : "\t & ")+"\\bf "+dc.name;
			
			first = false;
			
			if(dc instanceof IntDataColumn)
				((IntDataColumn)dc).calculateStats();
		}
		tableData += " \\\\\n\t\\hline\n";
		
		for(int i = 0 ; i < this.data.size() ; i++)
		{
			first = true;
			
			for(DataColumn dc : columns)
			{
				tableData += (first?"\t" : "\t & ")+(dc.bold ? "\\bf " : "" )+dc.getRow(i);
				
				first = false;
			}
			
			tableData += " \\\\\n\t\\hline\n";
		}
		
		first = true;
		for(DataColumn dc : columns)
		{
			if(first)
				tableData += "\t\\bf Mean:";
			else if(dc instanceof IntDataColumn)
				tableData += "\t & "+((IntDataColumn)dc).getMean();
			else
				tableData +="\t & ~";
			
			first = false;
		}
		tableData += " \\\\\n\t\\hline\n";
		
		first = true;
		for(DataColumn dc : columns)
		{
			if(first)
				tableData += "\t\\bf Variance:";
			else if(dc instanceof IntDataColumn)
				tableData += "\t & "+((IntDataColumn)dc).getVar();
			else
				tableData +="\t & ~";
			
			first = false;
		}
		tableData += " \\\\\n\t\\hline\n";
		
		
		String table = "\\begin{tabular}{"+tableHeader+"}\n"
					   +tableData
					   +"\\end{tabular}";
		
		this.table.setText(table);
		
		
		System.out.println("T test declined booked: "+TTestResult( this.dc_nrb_dec_b.getMean(),
																   this.dc_rb_dec_b.getMean(),
																   this.dc_nrb_dec_b.getVar(),
																   this.dc_rb_dec_b.getVar(),
																   this.data.size()
							));
		
		System.out.println("T test declined unbooked: "+TTestResult(   this.dc_nrb_dec_ub.getMean(),
																	   this.dc_rb_dec_ub.getMean(),
																	   this.dc_nrb_dec_ub.getVar(),
																	   this.dc_rb_dec_ub.getVar(),
																	   this.data.size()
							));
		
		System.out.println("T test declined wait: "+TTestResult( 	   this.dc_nrb_wait.getMean(),
																	   this.dc_rb_wait.getMean(),
																	   this.dc_nrb_wait.getVar(),
																	   this.dc_rb_wait.getVar(),
																	   this.data.size()
));
	}

	public double TTestResult(double m1, double m2, double v1, double v2, int pop)
	{
		final double sx1x2 = Math.sqrt(0.5*(v1 + v2));
		
		final double t = (m1 - m2) / (sx1x2 * Math.sqrt(2.0/pop));
		
		return t;
	}
	
	public void readInstances()
	{
		String directory_robust = "/Users/rene/Master/Thesis_Simulation_Runs/simresults/robust";
		String directory_norobust = "/Users/rene/Master/Thesis_Simulation_Runs/simresults/norobust";
		
		this.readInstances(new File(directory_robust),true);
		this.readInstances(new File(directory_norobust),false);
		
		System.out.println("readed: "+data.size()+" instances");
	}
	
	public void readInstances(File dir, boolean robust)
	{
		for(File f : dir.listFiles())
		{
			if(f.getName().endsWith(".txt"))
			{
				try 
				{
					readInstance(f, robust);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void readInstance(File file, boolean robust) throws IOException
	{
		LineParser parser = new LineParser(new BufferedReader(new FileReader(file)));
		
		parser.readLine();
		parser.skipTillLineStartsWith("Instance: Instance");
		int instance = Integer.parseInt(parser.getAfter("Instance: Instance").trim());
		
		parser.skipTillLineStartsWith("Results");
		parser.readLine();
		
		String line = parser.readLine();
		
		while(line != null)
		{
			
			String[] lineData = line.split(";");
			
			long 	seed 	= Long.parseLong(	lineData[0].trim()); // seed ;
			int  	dec_b 	= Integer.parseInt(	lineData[1].trim()); // declined(booked) ; 
			int  	dec_ub	= Integer.parseInt(	lineData[2].trim());// declined(other) ; 
			double  wait 	= Double.parseDouble(	lineData[3].trim());// waitTime(min) ; 
			int  	w_u 	= Integer.parseInt(	lineData[4].trim());// worker updates ; 
			int  	r_u 	= Integer.parseInt(	lineData[5].trim());// reschedule_updates ; 
			int 	r_f 	= Integer.parseInt(	lineData[6].trim());// full reschedule
			
			SimResultsData srd = this.findData(instance, seed);
			
			if(robust)
			{
				srd.rb_dec_booked 		= dec_b;
				srd.rb_dec_unbooked 	= dec_ub;
				srd.rb_wait				= wait;
				srd.rb_worker_updates	= w_u;
				srd.rb_updates			= r_u;
				srd.rb_reschedule		= r_f;
			}
			else
			{
				srd.nrb_dec_booked 		= dec_b;
				srd.nrb_dec_unbooked 	= dec_ub;
				srd.nrb_wait			= wait;
				srd.nrb_worker_updates	= w_u;
				srd.nrb_updates			= r_u;
				srd.nrb_reschedule		= r_f;
			}
			
			line = parser.readLine();
		}
	}
	
	public SimResultsData findData(int instance, long seed)
	{
		for(SimResultsData srd : data)
			if(srd.instance == instance && srd.seed == seed)
				return srd;
		
		SimResultsData srd = new SimResultsData();
		this.data.add(srd);
		srd.instance = instance;
		srd.seed = seed;
		
		return srd;
	}
	
	
}
