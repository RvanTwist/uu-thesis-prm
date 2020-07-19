package rpvt.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

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
public class DualStream extends OutputStream
{
	final OutputStream stream1;
	final OutputStream stream2;
	
	public DualStream(OutputStream s1, OutputStream s2)
	{
		super();
		this.stream1 = s1;
		this.stream2 = s2;
	}

	@Override
	public void write(int b) throws IOException 
	{
		this.stream1.write(b);
		this.stream2.write(b);
	}
	
	public static void main(String[] s)
	{
		System.setOut(new PrintStream(new DualStream(System.out, System.out)));
		
		System.out.println("Tests ");
	}
	
	
}
