package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;


/*
   Copyright 2004-2014 Bo Zimmerman

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
public class Song_Knowledge extends Song
{
	@Override public String ID() { return "Song_Knowledge"; }
	private final static String localizedName = CMLib.lang().L("Knowledge");
	@Override public String name() { return localizedName; }
	@Override public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null)
			return;
		affectableStats.setStat( CharStats.STAT_SPIRIT, ( affectableStats.getStat( CharStats.STAT_SPIRIT) + 2 + getXLEVELLevel( invoker() ) ) );
		affectableStats.setStat( CharStats.STAT_INTELLIGENCE, ( affectableStats.getStat( CharStats.STAT_INTELLIGENCE ) + 2 + getXLEVELLevel( invoker() ) ) );
	}
}
