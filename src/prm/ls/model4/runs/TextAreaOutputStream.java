package prm.ls.model4.runs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;

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
public class TextAreaOutputStream extends PrintStream
{
	JTextArea area;
	
	public TextAreaOutputStream(JTextArea a, OutputStream out) throws FileNotFoundException
	{
		super(out);
		
		this.area = a;
	}

	@Override
	public void print(int i)
	{
		print(""+i);
	}
	
	@Override
	public void print(boolean b)
	{
		print(""+b);
	}
	
	@Override
	public void print(char c)
	{
		print(""+c);
	}
	
	@Override
	public void print(float f)
	{
		print(""+f);
	}
	
	@Override
	public void print(double d)
	{
		print(""+d);
	}
	
	@Override
	public void print(Object o)
	{
		print(""+o);
	}
	
	@Override
	public void print(long l)
	{
		area.append(""+l);
	}
	
	@Override
	public void print(String s)
	{
		area.append(s);
	}
	
	
	
	@Override
	public void println(String s)
	{
		area.append(s+"\n");
	}
	
	@Override
	public void println()
	{
		area.append("\n");
	}
	
	@Override
	public void println(int i)
	{
		print(i+"\n");
	}
	
	@Override
	public void println(char[] c)
	{
		String s = new String(c);
		print(s+"\n");
	}
	
	@Override
	public void print(char[] c)
	{
		print(new String(c));
	}
	
	@Override
	public void println(boolean b)
	{
		print(b+"\n");
	}
	
	@Override
	public void println(char c)
	{
		print(c+"\n");
	}
	
	@Override
	public void println(float f)
	{
		print(f+"\n");
	}
	
	@Override
	public void println(double d)
	{
		print(d+"\n");
	}
	
	@Override
	public void println(Object o)
	{
		print(o+"\n");
	}
	
	@Override
	public void println(long l)
	{
		area.append(l+"\n");
	}
}
