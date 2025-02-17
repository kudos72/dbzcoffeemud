package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2001-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Prayer_Deathfinger extends Prayer
{
	@Override public String ID() { return "Prayer_Deathfinger"; }
	private final static String localizedName = CMLib.lang().L("Deathfinger");
	@Override public String name() { return localizedName; }
	@Override public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_VEXING;}
	@Override public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	@Override public long flags(){return Ability.FLAG_UNHOLY;}
	@Override protected int overrideMana(){return 150;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?L("A finger of death rages at <T-NAME>."):L("^S<S-NAME> point(s) in rage at <T-NAMESELF> and @x1!^?",prayWord(mob)));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_UNDEAD|(auto?CMMsg.MASK_ALWAYS:0),null);
			final Room R=target.location();
			if((R.okMessage(mob,msg))&&((R.okMessage(mob,msg2))))
			{
				R.send(mob,msg);
				R.send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					int harming=CMLib.dice().roll(1,adjustedLevel(mob,asLevel),48 + 16);
					final int levelDiff=(mob.phyStats().level() - target.phyStats().level());
					if(levelDiff> 0)
						harming += (int)Math.round(CMath.mul(CMath.div(target.baseState().getHitPoints(),10),levelDiff));
					CMLib.combat().postDamage(mob,target,this,harming,CMMsg.MASK_ALWAYS|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,L("The finger of DEATH <DAMAGE> <T-NAME>!"));
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> point(s) in rage at <T-NAMESELF> and @x1, but @x2 does nothing.",prayWord(mob),hisHerDiety(mob)));


		// return whether it worked
		return success;
	}
}
