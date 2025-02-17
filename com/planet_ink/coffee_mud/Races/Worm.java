package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;


import java.util.*;

/*
   Copyright 2002-2014 Bo Zimmerman

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
public class Worm extends StdRace
{
	@Override public String ID(){	return "Worm"; }
	@Override public String name(){ return "Worm"; }
	@Override public int shortestMale(){return 2;}
	@Override public int shortestFemale(){return 2;}
	@Override public int heightVariance(){return 0;}
	@Override public int lightestWeight(){return 1;}
	@Override public int weightVariance(){return 0;}
	@Override public long forbiddenWornBits(){return Integer.MAX_VALUE;}
	@Override public String racialCategory(){return "Worm";}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,1 ,0 ,0 ,0 };
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,2,4,6,8,10,12,14,16};
	@Override public int[] getAgingChart(){return agingChart;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,3);
		affectableStats.setRacialStat(CharStats.STAT_ENDURANCE,8);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,3);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public String arriveStr()
	{
		return "shuffles in";
	}
	@Override
	public String leaveStr()
	{
		return "shuffles";
	}
	@Override
	public String makeMobName(char gender, int age)
	{
		return makeMobName('N',age);
	}
	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a nasty maw"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
		}
		return naturalWeapon;
	}
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" guts",RawMaterial.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
