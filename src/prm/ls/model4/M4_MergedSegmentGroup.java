package prm.ls.model4;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import prm.ls.BackTracker;
import prm.ls.SolutionFacture;
import prm.ls.model4.matching.M4_MatchingAlgorithm;
import prm.ls.model4.mutations.MutationPlanDeclinedPRM;
import rpvt.util.RBTree;

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
public class M4_MergedSegmentGroup extends M4_SegmentGroup<M4_PRM_Segment>
{
	public static boolean ENABLE_AUTOMERGING = true;
	
	TreeSet<M4_PRM_SegmentGroup> segmentGroups = new TreeSet<M4_PRM_SegmentGroup>();
	
	// Backtrack
	private boolean bt_created = true;
	public final M4_Planning planning;
	
	public M4_MergedSegmentGroup(M4_Planning p)
	{
		super(p);
		
		this.planning = p;
	}
	
	public M4_MergedSegmentGroup(M4_PRM_SegmentGroup g1, M4_PRM_SegmentGroup g2, int offset) throws CantPlanException
	{
		super(g1.planning);
		
		this.planning = g1.prm.planning;
		
		if(g1.isPlanned())
		{
			this.setStart(g1.getStart());
			this.setPlanned(true);
		}
		else if(g2.isPlanned())
		{
			this.setStart(g2.getStart() - offset);
			this.setPlanned(true);
		}
		
		this.addSegmentGroup(g1, 0);
		this.addSegmentGroup(g2, offset);
	}
	
	public void optimiseSegment(M4_PRM_Segment seg) throws CantPlanException
	{
		if(!ENABLE_AUTOMERGING)
			return;
		
		M4_PRM_SegmentGroup oriSG = seg.getOriSegmentGroup();
		for(M4_PRM_SegmentGroup sg : this.segmentGroups)
			if(sg != oriSG)
			{
				for(M4_PRM_Segment seg2 : sg.segments)
				{
					if(seg2.canMergeWith(seg))
					{
						seg2.mergeWith(seg);
						return;
					}
				}
			}
	}
	
	/**
	 * Merge segments that runs simutanously with this one and make this one segmentGroup.
	 * 
	 * @param prmSG
	 * @param offset
	 */
	public void addSegmentGroup(M4_PRM_SegmentGroup prmSG, int offset) throws CantPlanException
	{
		
		//System.out.println("Adding sg: "+prmSG.id+" "+prmSG.prm.prm.printRoute());
		if(this.segmentGroups.size() == 0 && prmSG.isPlanned())
		{ // Then this mergedGroup is planned aswell.
			this.setStart( prmSG.getStart() - offset);
			this.setPlanned(true);
		}
		
//		if(this.isPlanned() != prmSG.isPlanned())
//		{
//			if(!this.isPlanned() && prmSG.isPlanned())
//			{
//				throw new Error("The merged Group is not planned and the to be inserted is.");
//			}
//			throw new Error("Both mergedGroup and the to insert SegmentGroup must be either planned or unplanned when not giving a matchmaking method!");
//		}
		
		if( prmSG.isMerged() )
		{
			new Error("This segmentGroup is already member of one mergedSegmentGroup");
		}
		
		// Add group
		prmSG.setMergedSegmentGroup(this, offset);
		
		if(prmSG.isPlanned() && this.getStart() + offset != prmSG.getStart())
		{
			throw new Error("Imposible to add a planned segmentGroup who isn't alraedy starting on the correct time!");
		}
		
		
		// Synchronize the segments with this one.
		prmSG.setStart(this.getStart() + offset);
		
		// Merge common segments (Expansive or not... Assumed that when synchronizing common segments gets reconized)
		
		for(M4_PRM_Segment prmSeg : prmSG.segments)
		{ 
			this.optimiseSegment(prmSeg);
		}
	}
	
	public void removeSegmentGroup(M4_PRM_SegmentGroup prmSG) throws CantPlanException
	{
		if(prmSG.getMergedGroup() != this)
		{
			throw new Error("The merged Group is not this!");
		}
		
		prmSG.setMergedSegmentGroup(null);
		
		// Detach all merged segments;
		for(M4_PRM_Segment seg : prmSG.segments)
		{
			final M4_MergedSegment mergedSeg = seg.mergedWith;
			if(mergedSeg != null)
			{
				mergedSeg.remove(seg);
			}
		}
	}
	
	@Override
	public int getErliestStart() 
	{
		int earliest = Integer.MIN_VALUE;
		
		for(M4_PRM_SegmentGroup mseg : this.segmentGroups)
		{
			final int start = mseg.getErliestStart()-mseg.getMergedOffset();
			
//			if(mseg.isFixed())
//			{
//				System.out.println("Fixed early! "+mseg.id+" : "+mseg.getErliestStart()+" | "+start);
//			}
			
			if(earliest < start)
			{
				earliest = start;
			}
		}
		
		return earliest;
	}

	@Override
	public int getLatestStart() 
	{
		
		int latest = Integer.MAX_VALUE;
		
		for(M4_PRM_SegmentGroup mseg : this.segmentGroups)
		{
			final int start = mseg.getLatestStart()-mseg.getMergedOffset();
			
//			if(mseg.isFixed())
//			{
//				System.out.println("Fixed later! "+mseg.id+" : "+mseg.getErliestStart()+" | "+start);
//			}
			if(latest > start)
			{
				latest = start;
			}
		}
		
		return latest;
	}

	@Override
	public Iterator<M4_PRM_Segment> iterator() 
	{
		return new M4_MergedSegmentGroupIterator(this);
	}

	public void mergeWith(M4_MergedSegmentGroup mergedGroup, int offset) throws CantPlanException
	{
		if(mergedGroup == this)
		{ // Nothing to merge
			return;
		}
		
		if(this.getStart() + offset != mergedGroup.getStart())
		{
			if(mergedGroup.isPlanned())
			{
				throw new Error(" Start times incorrect: "+this.getStart()+"+"+offset+" != "+mergedGroup.getStart());	
			}
			mergedGroup.setStart(this.getStart()+offset);
			
		}
		
		for(M4_PRM_SegmentGroup sg : mergedGroup.segmentGroups)
		{
			final int extraOffset = sg.getMergedOffset();
			// Add group
			sg.unSaveRemoveMergedSegmentGroup();
			sg.setMergedSegmentGroup(this, offset + extraOffset);
			
			// Synchronize the segments with this one. (Unnecesairy really is there for debugging) TODO: remove this.
			sg.setStart(this.getStart() + offset + extraOffset);
			
			// Merge common segments (Expansive or not... Assumed that when synchronizing common segments gets reconized)
			
			for(M4_PRM_Segment prmSeg : sg.segments)
			{ 
				if(prmSeg.isMerged())
				{	
					M4_MergedSegment mseg = prmSeg.getMergedWith();
					if(mseg.segments.size() == 0)
					{
						throw new Error("Mseg segments size = 0\n"+
										"  seg: "+prmSeg+"\n"+
										" mseg: "+mseg);
					}
					
					if(mseg.segments.first() == prmSeg)
					{ // Only need to do it once
						this.optimiseSegment(prmSeg);
					}
				}
				else
				{
					this.optimiseSegment(prmSeg);
				}
			}
		}
		
		mergedGroup.segmentGroups.clear();
		
	}

	@Override
	public void setStart(int start) throws CantPlanException
	{
		super.setStart(start);
		
		for(M4_PRM_SegmentGroup sg : this.segmentGroups)
		{
			sg.setStart(start + sg.getMergedOffset());
		}
	}

	public void unPlan() throws CantPlanException
	{
		this.setPlanned(false);
		
		for(M4_Segment seg : this)
		{
			seg.getPlannableSegment().unPlan();
		}
	}

	@Override
	public boolean isFixed()
	{
		for(M4_PRM_SegmentGroup sgr : this.segmentGroups)
		{
			if(sgr.isFixed())
			{
				this.fixed = true;
				this.fixedTime = sgr.fixedTime - sgr.getMergedOffset();
				return true;
			}
		}
		this.fixed = false;
		return false;
	}
	
	@Override
	public void setPlanned(boolean b) 
	{
		super.setPlanned(b);
		
//		System.out.println("setPlaned ("+b+") Merged SG: "+this.id);
		for(M4_PRM_SegmentGroup prmSG : this.segmentGroups)
		{
			prmSG.setPlanned(b);
//			System.out.println(" setPlaned ("+b+") PRM SG: "+prmSG.id+" "+prmSG.isPlanned());
		}
		
	}

	public void checkAllMemberships() 
	{
//		for(M4_PRM_SegmentGroup sg : this.segmentGroups)
//		{
//			for(M4_PRM_Segment seg : sg.segments)
//			{
//				final M4_Segment equiv = this.segments.find(seg);
//				if(seg.getPlannedSegment() != equiv)
//				{
//					throw new Error("This object doesn't contain segment: "+seg+" found: "+equiv+" instead.");
//				}
//			}
//		}
	}
	
//	@Override
//	public void testConsistancy()
//	{
//		super.testConsistancy();
//		
//		for(M4_PRM_SegmentGroup sg : this.segmentGroups)
//		{
//			sg.testConsistancy();
//		}
//	}

	public class M4_MergedSegmentGroupIterator implements Iterator<M4_PRM_Segment>
	{
		M4_MergedSegmentGroup msg;
		Iterator<M4_PRM_SegmentGroup> it1;
		M4_PRM_SegmentGroup prmSG;
		int index;
		
		public M4_MergedSegmentGroupIterator( M4_MergedSegmentGroup msg) 
		{
			this.msg = msg;
			it1 = msg.segmentGroups.iterator();
			prmSG = (it1.hasNext() ? it1.next() : null);
			int index = 0;
		}

		@Override
		public boolean hasNext() 
		{
			return prmSG != null;
		}

		@Override
		public M4_PRM_Segment next() 
		{
			if(prmSG == null)
			{
				throw new NoSuchElementException();
			}
			
			final M4_PRM_Segment seg = prmSG.segments[index];
			
			index++;
			if(index >= prmSG.segments.length)
			{
				index = 0;
				prmSG = (it1.hasNext() ? it1.next() : null);
			}
			
			return seg;
		}

		@Override
		public void remove() 
		{
			
		}
	}
	public String toString()
	{
		String sgs = "";
		for(M4_PRM_SegmentGroup sg : this.segmentGroups)
		{
			sgs += sg.toString();
		}
		
		return "[Merged_SegmentGroup "+id+" "+sgs+"]";
	}
	
	@Override
	public void doBacktrack()
	{	
		super.doBacktrack();
		
		if(this.bt_created)
		{
			this.backtracker.remove(this);
		}
	}
	
	@Override
	public void doMakeCheckpoint()
	{
		super.doMakeCheckpoint();
		this.bt_created = false;
		
		if(this.segmentGroups.size() == 0)
		{ // This object is bound to be destroyed:
			this.backtracker.remove(this);
		}
	}
	
	@Override
	public void mergeWith(M4_PRM_SegmentGroup segmentGroup) throws CantPlanException
	{
		if(segmentGroup.isMerged())
		{
			M4_MergedSegmentGroup merged = segmentGroup.getMergedGroup();
			this.mergeWith(merged, merged.getStart() - this.getStart() );
		}
		else
		{
			this.addSegmentGroup(segmentGroup, segmentGroup.getStart() - this.getStart());
		}
	}
	
	public boolean isValid()
	{
		for(M4_SegmentGroup sg : this.segmentGroups)
		{
			if(!sg.isValid())
			{
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void unfix() 
	{
		for(M4_SegmentGroup sg : this.segmentGroups)
		{
			sg.unfix();
		}
		
	}

	public Iterable<M4_PRM_SegmentGroup> getSegmentGroups() 
	{
		return this.segmentGroups;
	}
}
