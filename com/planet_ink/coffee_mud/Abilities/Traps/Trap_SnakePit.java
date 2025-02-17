package com.planet_ink.coffee_mud.Abilities.Traps;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class Trap_SnakePit extends Trap_RoomPit
{
	@Override public String ID() { return "Trap_SnakePit"; }
	private final static String localizedName = CMLib.lang().L("snake pit");
	@Override public String name() { return localizedName; }
	@Override protected int canAffectCode(){return Ability.CAN_ROOMS;}
	@Override protected int canTargetCode(){return 0;}
	@Override protected int trapLevel(){return 10;}
	@Override public String requiresToSet(){return "some caged snakes";}

	protected Vector monsters=null;

	protected Item getCagedAnimal(MOB mob)
	{
		if(mob==null)
			return null;
		if(mob.location()==null)
			return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if(I instanceof CagedAnimal)
			{
				final MOB M=((CagedAnimal)I).unCageMe();
				if((M!=null)&&(M.baseCharStats().getMyRace().racialCategory().equalsIgnoreCase("Serpent")))
					return I;
			}
		}
		return null;
	}

	@Override
	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(P==null)
			return null;
		Item I=getCagedAnimal(mob);
		final StringBuffer buf=new StringBuffer("<SNAKES>");
		int num=0;
		while((I!=null)&&((++num)<6))
		{
			buf.append(((CagedAnimal)I).cageText());
			I.destroy();
			I=getCagedAnimal(mob);
		}
		buf.append("</SNAKES>");
		setMiscText(buf.toString());
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final Vector V=new Vector();
		final Item I=CMClass.getItem("GenCaged");
		((CagedAnimal)I).setCageText(text());
		I.recoverPhyStats();
		I.text();
		V.addElement(I);
		return V;
	}

	@Override
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if(getCagedAnimal(mob)==null)
		{
			if(mob!=null)
				mob.tell(L("You'll need to set down some caged snakes first."));
			return false;
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_TRAP_RESET)&&(getReset()>0))
		{
			// recage the motherfather
			if((tickDown<=1)&&(monsters!=null))
			{
				for(int i=0;i<monsters.size();i++)
				{
					final MOB M=(MOB)monsters.elementAt(i);
					if(M.amDead()||(!M.isInCombat()))
						M.destroy();
				}
				monsters=null;
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.phyStats().weight()<5))
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> float(s) gently into the pit!"));
		else
		{
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> hit(s) the pit floor with a THUMP!"));
			final int damage=CMLib.dice().roll(trapLevel()+abilityCode(),6,1);
			CMLib.combat().postDamage(invoker(),target,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,-1,null);
		}
		final Vector snakes=new Vector();
		String t=text();
		int x=t.indexOf("</MOBITEM><MOBITEM>");
		while(x>=0)
		{
			snakes.addElement(t.substring(0,x+10));
			t=t.substring(x+10);
			x=t.indexOf("</MOBITEM><MOBITEM>");
		}
		if(t.length()>0)
			snakes.addElement(t);
		if(snakes.size()>0)
			monsters=new Vector();
		for(int i=0;i<snakes.size();i++)
		{
			t=(String)snakes.elementAt(i);
			final Item I=CMClass.getItem("GenCaged");
			((CagedAnimal)I).setCageText(t);
			final MOB monster=((CagedAnimal)I).unCageMe();
			if(monster!=null)
			{
				monsters.addElement(monster);
				monster.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				monster.bringToLife(target.location(),true);
				monster.setVictim(target);
				if(target.getVictim()==null)
					target.setVictim(monster);
			}
		}
		CMLib.commands().postLook(target,true);
	}
}
