package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.core.*;
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

public class Poison_GoblinDrip extends Poison
{
	@Override public String ID() { return "Poison_GoblinDrip"; }
	private final static String localizedName = CMLib.lang().L("Goblin Drip");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =I(new String[] {"POISONDRIP"});
	@Override public String[] triggerStrings(){return triggerStrings;}

	@Override protected int POISON_TICKS(){return 50;} // 0 means no adjustment!
	@Override protected int POISON_DELAY(){return 5;}
	@Override protected String POISON_DONE(){return "The poison runs its course.";}
	@Override protected String POISON_START(){return "^G<S-NAME> seem(s) weakened!^?";}
	@Override protected String POISON_AFFECT(){return "^G<S-NAME> shiver(s) in weakness.";}
	@Override protected String POISON_CAST(){return "^F^<FIGHT^><S-NAME> poison(s) <T-NAMESELF>!^</FIGHT^>^?";}
	@Override protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	@Override protected int POISON_DAMAGE(){return (invoker!=null)?CMLib.dice().roll(1,3,1):0;}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_ENDURANCE,affectableStats.getStat(CharStats.STAT_ENDURANCE)-1);
		if(affectableStats.getStat(CharStats.STAT_ENDURANCE)<=0)
			affectableStats.setStat(CharStats.STAT_ENDURANCE,1);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)-10);
		if(affectableStats.getStat(CharStats.STAT_STRENGTH)<=0)
			affectableStats.setStat(CharStats.STAT_STRENGTH,1);
	}
}
