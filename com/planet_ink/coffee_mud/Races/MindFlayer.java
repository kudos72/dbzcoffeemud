package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;


import java.util.*;

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
public class MindFlayer extends Humanoid
{
	@Override public String ID(){	return "MindFlayer"; }
	@Override public String name(){ return "MindFlayer"; }
	protected static List<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}
	@Override public String racialCategory(){return "Illithid";}
	private final String[]culturalAbilityNames={"Spell_MindFog","Spell_Charm","Undercommon"};
	private final int[]culturalAbilityProficiencies={100,50,25};
	@Override public String[] culturalAbilityNames(){return culturalAbilityNames;}
	@Override public int[] culturalAbilityProficiencies(){return culturalAbilityProficiencies;}
	private final String[]racialAbilityNames={"Spell_DetectSentience","Spell_CombatPrecognition"};
	private final int[]racialAbilityLevels={10,30};
	private final int[]racialAbilityProficiencies={50,30};
	private final boolean[]racialAbilityQuals={true,false};
	@Override public String[] racialAbilityNames(){return racialAbilityNames;}
	@Override public int[] racialAbilityLevels(){return racialAbilityLevels;}
	@Override public int[] racialAbilityProficiencies(){return racialAbilityProficiencies;}
	@Override public boolean[] racialAbilityQuals(){return racialAbilityQuals;}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,2,20,110,175,263,350,390,430};
	@Override public int[] getAgingChart(){return agingChart;}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
	}
	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_MAGIC,affectableStats.getStat(CharStats.STAT_SAVE_MAGIC)+50);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+100);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)-5);
		affectableStats.setStat(CharStats.STAT_ENDURANCE,affectableStats.getStat(CharStats.STAT_ENDURANCE)-5);
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)+5);
	}
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources=super.myResources();
				resources.add(makeResource
				("a "+name().toLowerCase()+" tenticle",RawMaterial.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
