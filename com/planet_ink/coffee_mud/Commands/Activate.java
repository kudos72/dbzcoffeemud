package com.planet_ink.coffee_mud.Commands;
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
@SuppressWarnings("rawtypes")
public class Activate extends StdCommand
{
	public Activate(){}

	private final String[] access=I(new String[]{"ACTIVATE","ACT","A",">"});
	@Override public String[] getAccessWords(){return access;}
	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		final Room R=mob.location();
		if((commands.size()<2)||(R==null))
		{
			mob.tell(L("Activate what?"));
			return false;
		}
		commands.remove(0);
		final String what=(String)commands.lastElement();
		final String whole=CMParms.combine(commands,0);
		Item item=null;
		Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whole,Wearable.FILTER_ANY);
		if((!(E instanceof Electronics))||(E instanceof Software))
			E=null;
		if(E==null)
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if((I instanceof Electronics.ElecPanel)
				&&(((Electronics.ElecPanel)I).isOpen()))
				{
					E=R.fetchFromRoomFavorItems(I, whole);
					if((E instanceof Electronics)&&(!(E instanceof Software)))
						break;
				}
			}
		if((!(E instanceof Electronics))||(E instanceof Software))
			E=null;
		else
		{
			item=(Item)E;
			commands.clear();
		}
		if(E==null)
		{
			E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,what,Wearable.FILTER_ANY);
			if((!(E instanceof Electronics))||(E instanceof Software))
				E=null;
			if(E==null)
				for(int i=0;i<R.numItems();i++)
				{
					final Item I=R.getItem(i);
					if((I instanceof Electronics.ElecPanel)
					&&(((Electronics.ElecPanel)I).isOpen()))
					{
						E=R.fetchFromRoomFavorItems(I, what);
						if((E instanceof Electronics)&&(!(E instanceof Software)))
							break;
					}
				}
			if((!(E instanceof Electronics))||(E instanceof Software))
				E=null;
			if((E==null)&&(mob.riding() instanceof Electronics.Computer))
			{
				E=mob.riding();
				item=(Item)E;
			}
			else
			{
				item=(Item)E;
				commands.remove(commands.size()-1);
			}
		}
		if(E==null)
		{
			mob.tell(L("You don't see anything called '@x1' or '@x2' here that you can activate.",what,whole));
			return false;
		}
		else
		if(item==null)
		{
			mob.tell(L("You can't activate @x1.",E.name()));
			return false;
		}

		final String rest=CMParms.combine(commands,0);
		final CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_ACTIVATE,null,CMMsg.MSG_ACTIVATE,(rest.length()==0)?null:rest,CMMsg.MSG_ACTIVATE,null);
		if(R.okMessage(mob,newMsg))
			R.send(mob,newMsg);
		return false;
	}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandCombatActionCost(ID());}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandActionCost(ID());}
	@Override public boolean canBeOrdered(){return true;}
}
