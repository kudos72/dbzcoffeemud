package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;

/*
   Copyright 2000-2014 Lee H. Fox

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
public class Orc extends StdMOB
{
	@Override public String ID(){return "Orc";}
	public Orc()
	{
		super();
		username="an Orc";
		setDescription("He\\`s dirty, cranky, and very mean.");
		setDisplayText("An angry Orc marches around.");
		CMLib.factions().setAlignment(this,Faction.Align.EVIL);
		setMoney(10);
		basePhyStats.setWeight(150);
		setWimpHitPoint(0);

		final Weapon d=CMClass.getWeapon("Dagger");
		if(d!=null)
		{
			d.wearAt(Wearable.WORN_WIELD);
			addItem(d);
		}

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,6);
		baseCharStats().setStat(CharStats.STAT_QUICKNESS,2);
		baseCharStats().setMyRace(CMClass.getRace("Orc"));
		baseCharStats().getMyRace().startRacing(this,false);

		basePhyStats().setAbility(0);
		basePhyStats().setLevel(1);
		basePhyStats().setArmor(90);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}




}
