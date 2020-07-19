package tests;

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
public class SimResultsData 
{
	public int instance;
	public long seed;
	
	public int 		rb_dec_booked;
	public int 		rb_dec_unbooked;
	public double 	rb_wait;
	public int 		rb_worker_updates;
	public int 		rb_updates;
	public int 		rb_reschedule;
	
	public int 		nrb_dec_booked;
	public int 		nrb_dec_unbooked;
	public double 	nrb_wait;
	public int 		nrb_worker_updates;
	public int 		nrb_updates;
	public int 		nrb_reschedule;
}
