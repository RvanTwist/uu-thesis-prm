package prm.ls.model4.matching.lpflow;

import gurobi.GRB;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import prm.ls.model4.CantPlanException;
import prm.ls.model4.M4_Area;
import prm.ls.model4.M4_CostModel;
import prm.ls.model4.M4_MergedSegment;
import prm.ls.model4.M4_PRM_Segment;
import prm.ls.model4.M4_Segment;
import prm.ls.model4.M4_Worker;
import prm.ls.model4.M4_WorkerSegment;
import prm.problemdef.Area;
import prm.problemdef.Segment;

import rpvt.lp.LpModelAbstr;
import rpvt.lp.VariableInterface;
import rpvt.lp.gurobi.*;

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
/**
 * 
 * @author rene
 */
public interface AreaFlowProblem_IF
{
	
	
	public void addSegment(M4_Segment seg);
	
	public void registerChanged(M4_Segment seg);
	
	public void solve();
	
	public void backtrack();
	
	public void acceptChanges();
	
	public void updateAllSegments2();
	
	public void updateLPbacktrackable();
	
	public void initialiseEdges();
	
	public void convertToSollution() throws CantPlanException;
	
	public boolean isFeasible();

	public boolean needRecalc();

	public LpModelAbstr getModel();
}
