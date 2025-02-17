package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.core.interfaces.*;
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

public class Disease_PoisonIvy extends Disease
{
	@Override public String ID() { return "Disease_PoisonIvy"; }
	private final static String localizedName = CMLib.lang().L("Poison Ivy");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang().L("(Poison Ivy)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public boolean putInCommandlist(){return false;}

	@Override protected int DISEASE_TICKS(){return 35;}
	@Override protected int DISEASE_DELAY(){return 5;}
	@Override protected String DISEASE_DONE(){return "Your poison ivy clears up.";}
	@Override protected String DISEASE_START(){return "^G<S-NAME> <S-IS-ARE> covered in poison ivy.^?";}
	@Override protected String DISEASE_AFFECT(){return "<S-NAME> scratch(es) <S-HIM-HERSELF>.";}
	@Override public int spreadBitmap(){return DiseaseAffect.SPREAD_CONTACT|DiseaseAffect.SPREAD_STD;}
	@Override public int difficultyLevel(){return 3;}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected==null)
			return false;
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,DISEASE_AFFECT());
			return true;
		}
		return true;
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null)
			return;
		affectableStats.setStat(CharStats.STAT_QUICKNESS,affectableStats.getStat(CharStats.STAT_QUICKNESS)-2);
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)-2);
		if(affectableStats.getStat(CharStats.STAT_QUICKNESS)<=0)
			affectableStats.setStat(CharStats.STAT_QUICKNESS,1);
		if(affectableStats.getStat(CharStats.STAT_DEXTERITY)<=0)
			affectableStats.setStat(CharStats.STAT_DEXTERITY,1);
	}
}
