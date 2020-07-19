package simulation.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import ui.ResultViewer;

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
public class WorkerGanttPanel extends JPanel implements ActionListener
{
	public SimResultViewer viewer;
	public JButton 		reload = new JButton("Reload");

	public SimulationUI simulationGUI;
	
	public WorkerGanttPanel(SimulationUI ui)
	{
		super(new BorderLayout());
		simulationGUI = ui;
		viewer = new SimResultViewer(ui.dws);
		
		this.add(this.viewer, BorderLayout.CENTER);
		this.add(this.reload,BorderLayout.SOUTH);
		
		this.reload.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		Object src = ae.getSource();
		if(src == this.reload)
		{
			this.actionReload();
		}
	}

	private void actionReload() 
	{
		this.viewer.loadFromSimulation();
	}
}
