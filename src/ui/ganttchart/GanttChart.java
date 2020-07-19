package ui.ganttchart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

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
@SuppressWarnings("serial")
public class GanttChart extends JPanel implements GanttModelChangeListener, MouseMotionListener
{
	public static Color VeryLightGray = new Color(220,220,220);
	public static Color SelectionColor = new Color(0,140,255);
	
	Font font = new Font("sansserif", Font.BOLD, 16);
	
	GanttChartModel model;
	BufferedImage image;
	boolean update = true;
	
	int xLabelInterval = 1;
	double xScale = 1;
	
	int rowSize = 30;
	int barOffset = 3;
	
	public GanttChart(GanttChartModel model)
	{
		this.setModel(model);
		this.setBackground(VeryLightGray);
		
		//this.addMouseMotionListener(this);
	}
	
	public GanttChart() 
	{
		this.setBackground(VeryLightGray);
	}

	int xOffset = 0;
	int yOffset = 0;
	
	@Override
	public void paint(Graphics g)
	{
		if(update)
		{
			this.drawChart();
		}
		update = false;
		
		g.setColor(GanttChart.VeryLightGray);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		if(image == null || model == null)
		{
			return;
		}
		
		g.drawImage(image, 0, 0, null);
	}
	
	private void drawChart()
	{
		if(model == null)
		{
			return;
		}
		
		// Offset
		final int yOff = 5;
		int xOff = 0;
		
		// Analyse the labels:
		
		final FontRenderContext frc = ((Graphics2D)this.getGraphics()).getFontRenderContext();
		
		for(GanttChartRow row : this.model.rows)
		{
			final int fontW = (int)font.getStringBounds(row.name, frc).getWidth();
			if( xOff < fontW )
			{
				xOff = fontW;
			}
		}
		
		xOff += 8;
		
		
		// Rows and units to draw;
		final int rows = model.rows.size();
		final int units = model.getUnits();
		
		final int imageWidth = xOff + (int)(xScale * units)+5;
		final int imageHeight = yOff + (rows * this.rowSize + 20);
		
		// Check or a new BufferedImage should be created.
		if(image == null || ( image.getHeight() != imageHeight || image.getWidth() != imageWidth) )
		{
			image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
			Dimension d = new Dimension(image.getWidth(), image.getHeight());
			this.setPreferredSize(d);
			
			this.setSize(d);
		}
		else
		{
			image.flush();
		}
		
		Graphics2D g = image.createGraphics();
		g.setBackground(GanttChart.VeryLightGray);
		g.setColor(GanttChart.VeryLightGray);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		
		
		// Draw the lines
		
		int line = 0;
		
		g.setColor(Color.gray);
		while(line * xLabelInterval <= units)
		{
			final int linePos = xOff + (int)(xScale * (line*xLabelInterval));
			g.drawLine(linePos, 0, linePos, imageHeight);
			line++;
		}
		
		// Draw the chart
		int rowNummer = 0;		
		
		final int barheight = rowSize - 2* barOffset;
		
		for(GanttChartRow row : model.rows)
		{
			int y1 = yOff + rowNummer * this.rowSize;
			int y2 = y1 + rowSize;
			
		
			g.setColor(Color.black);
			g.drawLine(xOff, y2, imageWidth, y2);
			
			if(row.hasOverLap())
			{
				g.setColor(Color.red);
			}
			g.setFont(font);
			final int stringHeight = (int)font.getStringBounds(row.name, frc).getHeight();
			g.drawString(row.name, 5, y2 - ((this.rowSize - stringHeight) / 2) - 1);
			
			for(GanttChartBar bar : row.bars)
			{
				int x1 = xOff + (int)(xScale * bar.start );
				int l1 = (int)(xScale * bar.lenght) -1;
				
				if(bar.selected)
				{
					g.setColor(GanttChart.SelectionColor);
					g.fillRect(x1, y1 + 1, l1+1 , rowSize -1 );
				}
				
				g.setColor(bar.getColor());
				//System.out.println(bar.getColor());
				g.fillRect(x1, y1 + barOffset, l1 , barheight);
				g.setColor(Color.black);
				g.drawRect(x1, y1 + barOffset, l1 , barheight);
			}
			rowNummer++;
		}
		
		this.xOffset = xOff;
		this.yOffset = yOff;
	}
	
	public void setScale(double scale)
	{
		this.xScale = scale;
	}
	
	public void updateGanttChart()
	{
		this.update = true;
		this.repaint();
	}
	
	@Override
	public void GanttmodelEvent(GanttModelChangeEvent event) 
	{
		this.update = true;
		this.repaint();
	}
	
	public void setModel(GanttChartModel m)
	{
		if(this.model != null)
		{
			this.model.removeGanttModelChangeListener(this);
		}
		
		this.model = m;
		
		if(m != null)
		{
			m.addGanttModelChangeListener(this);
		}
		
		this.update = true;
		this.repaint();
	}

	public void setLabelInterval(int i) 
	{
		this.xLabelInterval = i;
		
		this.update = true;
	}

	public GanttChartModel getModel() 
	{
		return this.model;
	}
	
	private int lastBarRow;
	private GanttChartBar lastBar;
	private GanttChartBar searchBar = new GanttChartBar(0,-1);
	
	public int getUnitAt(int x)
	{
		return (int)((x - xOffset) / this.xScale);
	}
	
	public GanttChartBar getBarAt(int x, int y)
	{
		if(model == null)
		{
			return null;
		}
		
		int row = (y - yOffset) / this.rowSize;
		int unit = this.getUnitAt(x);
		
		if(row < 0 || unit < 0 || row >= model.rows.size())
		{
			lastBar = null;
			return null;
		}
		
		if(lastBar != null)
		{
			if(lastBarRow == row && lastBar.start <= unit && unit < lastBar.start + lastBar.lenght)
			{
				return lastBar;
			}
			else
			{
				lastBar = null;
			}
		}
		
		GanttChartRow rowD = model.rows.get(row);
		
//		for(GanttChartBar bar : rowD.bars)
		{
			searchBar.start = unit + 1;
			GanttChartBar bar = rowD.bars.lower(searchBar);
			
			if(bar != null && bar.start <= unit && unit < bar.start + bar.lenght)
			{
				lastBarRow = row;
				lastBar = bar;
				return bar;
			}
		}
		
		return null;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) 
	{
		
	}

	@Override
	public void mouseMoved(MouseEvent me) 
	{
//		GanttChartBar bar = this.getBarAt(me.getX(), me.getY());
//		
//		if(bar != null)
//		{
//			System.out.println("bar found: "+bar.start+" "+bar.lenght+" "+bar.userObject);
//		}
	}
}
