package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;

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
public class Ogre extends StdMOB
{
	@Override public String ID(){return "Ogre";}
	public Ogre()
	{

		super();
		username="an Ogre";
		setDescription("Nine foot tall and with skin that is a covered in bumps and dead yellow in color..");
		setDisplayText("An ogre stares at you while he clenches his fists.");
		CMLib.factions().setAlignment(this,Faction.Align.EVIL);
		setMoney(10);
		basePhyStats.setWeight(350);
		setWimpHitPoint(0);
		basePhyStats().setDamage(12);

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,8);
		baseCharStats().setStat(CharStats.STAT_QUICKNESS,2);
		baseCharStats().setStat(CharStats.STAT_STRENGTH,22);
		baseCharStats().setMyRace(CMClass.getRace("Giant"));
		baseCharStats().getMyRace().startRacing(this,false);

		basePhyStats().setAbility(0);
		basePhyStats().setLevel(4);
		basePhyStats().setArmor(80);
		basePhyStats().setSpeed(3.0);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

}
