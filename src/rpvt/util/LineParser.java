package rpvt.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

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
public class LineParser extends BufferedReader
{
	
	String currentLine = null;

	public LineParser(Reader in, int sz) 
	{
		super(in, sz);
	}

	public LineParser(Reader in) {
		super(in);
	}
	
	public LineParser(File f) throws FileNotFoundException 
	{
		super(new FileReader(f));
	}
	
	public LineParser(String f) throws FileNotFoundException 
	{
		this(new File(f));
	}
	
	@Override
	public String readLine() throws IOException
	{
		currentLine = super.readLine();
		return currentLine;
	}
	
	public String skipLines(final int lines) throws IOException
	{
		for(int i = 0 ; i < lines ; i++)
		{
			super.readLine();
		}
		
		return this.readLine();
	}
	
	public String readedLine()
	{
		return this.currentLine;
	}
	
	public String lineReadAfter(String txt) throws IOException
	{
		if( !this.currentLine.startsWith(txt) )
		{
			throw new IOException("Line doesn't start with \""+txt+"\"\n"+
								  " starts with: \""+(currentLine.length() <= 20 ? currentLine+"\"" : currentLine.substring(0, 20)+"...\""));
		}
		
		currentLine = currentLine.substring(txt.length());
		
		return currentLine;
	}
	
	public boolean lineStartsWith(String txt)
	{
		return currentLine.startsWith(txt);
	}
	
	public String skipTillLineStartsWith(String txt) throws IOException
	{
		//int count = 20;
		
		//System.out.println("Start: '"+currentLine+"' | '"+txt+"'");
		while(currentLine != null && !currentLine.startsWith(txt))
		{
//			System.out.println("'"+currentLine+"' | '"+txt+"'");
//			count--;
//			if(count <= 0)
//			{
//				throw new Error("Incorrect code!");
//			}
			this.readLine();
		}
		
		return currentLine;
	}
	
	public String skipTillRegex(String regex) throws IOException
	{
		//int count = 20;
		
		//System.out.println("Start: '"+currentLine+"' | '"+txt+"'");
		while(currentLine != null && !currentLine.matches(regex))
		{
			this.readLine();
		}
		
		return currentLine;
	}

	public String getAfter(String string) 
	{
		if(	this.currentLine != null &&
			this.currentLine.startsWith(string) )
		{
			return this.currentLine.substring(string.length());
		}
		
		return null;
	}
}
