package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2005-2014 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public interface ListingLibrary extends CMLibrary
{

	public String itemSeenString(MOB viewerM, Environmental item, boolean useName, boolean longLook, boolean sysMsgs);
	public int getReps(MOB viewerM, Environmental item, List<? extends Environmental> theRest, boolean useName, boolean longLook);
	public void appendReps(int reps, StringBuilder say, boolean compress);
	public StringBuilder lister(MOB viewerM, List<? extends Environmental> items, boolean useName, String tag, String tagParm, boolean longLook, boolean compress);
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, int ofType);
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these);
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, Room likeRoom);
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, int ofType);
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, int ofType);
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these);
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these);
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, Room likeRoom);
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, Room likeRoom);
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);
	public StringBuilder reallyList2Cols(MOB viewerM, Enumeration<? extends Object> these);
	public StringBuilder reallyList2Cols(MOB viewerM, Enumeration<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);
	public StringBuilder fourColumns(MOB viewerM, List<String> reverseList);
	public StringBuilder fourColumns(MOB viewerM, List<String> reverseList, String tag);
	public StringBuilder threeColumns(MOB viewerM, List<String> reverseList);
	public StringBuilder threeColumns(MOB viewerM, List<String> reverseList, String tag);
	public StringBuilder makeColumns(MOB viewerM, List<String> reverseList, String tag, int numCols);

	public static class ListStringer
	{
		public String stringify(Object o)
		{
			if(o instanceof String)
				return (String)o;
			else
			if(o instanceof Ability)
				return ((Ability)o).ID()+(((Ability)o).isGeneric()?"*":"");
			else
			if(o instanceof CharClass)
				return ((CharClass)o).ID()+(((CharClass)o).isGeneric()?"*":"");
			else
			if(o instanceof Race)
				return ((Race)o).ID()+(((Race)o).isGeneric()?"*":"");
			else
				return CMClass.classID(o);
		}
	}

	public static class ColFixer
	{
		public static final int fixColWidth(final double colWidth, final MOB mob)
		{
			return fixColWidth(colWidth,(mob==null)?null:mob.session());
		}

		public static final int fixColWidth(final double colWidth, final Session session)
		{
			double totalWidth=(session==null)?78.0:(double)session.getWrap();
			if(totalWidth==0.0)
				totalWidth=1024.0;
			return (int)Math.round((colWidth/78.0)*totalWidth);
		}

		public static final int fixColWidth(final double colWidth, final double totalWidth)
		{
			return (int)Math.round((colWidth/78.0)*totalWidth);
		}
	}
}
