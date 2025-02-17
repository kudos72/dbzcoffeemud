package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Commands.*;
import com.planet_ink.coffee_mud.Commands.Inventory.InventoryList;

import java.util.*;

/*
	Written by Robert Little - The Looking Glass
   Copyright 2010-2014 Bo Zimmerman

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
public class Wealth extends Inventory
{
	public Wealth(){}

	private final String[] access=I(new String[]{"WEALTH"});
	@Override public String[] getAccessWords(){return access;}


	public StringBuilder getInventory(MOB seer, MOB mob, String mask)
	{
		final StringBuilder msg=new StringBuilder("");
		final InventoryList list = fetchInventory(seer,mob);
		if(list.moneyItems.size()==0)
			msg.append(L("\n\r^HMoney:^N None!\n\r"));
		else
			msg.append(getShowableMoney(list));
		return msg;
	}


	@Override @SuppressWarnings({"unchecked","rawtypes"})
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()==1)&&(commands.get(0) instanceof MOB))
		{
			commands.add(getInventory((MOB)commands.get(0),mob,null));
			return true;
		}
		final StringBuilder msg=getInventory(mob,mob,CMParms.combine(commands,1));
		if(msg.length()==0)
			mob.tell(L("You have no money on you."));
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	@Override public boolean canBeOrdered(){return true;}
}
