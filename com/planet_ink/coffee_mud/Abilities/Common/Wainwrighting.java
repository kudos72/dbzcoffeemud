package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/*
   Copyright 2003-2014 Bo Zimmerman

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
public class Wainwrighting extends CraftingSkill implements ItemCraftor
{
	@Override public String ID() { return "Wainwrighting"; }
	private final static String localizedName = CMLib.lang().L("Wainwrighting");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =I(new String[] {"WAINWRIGHTING"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public String supportedResourceString(){return "WOODEN";}
	@Override
	public String parametersFormat(){ return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tLID_LOCK\tCONTAINER_CAPACITY\tRIDE_CAPACITY\tCONTAINER_TYPE\t"
		+"CODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_CAPACITY=7;
	protected static final int RCP_NUMRIDERS=8;
	protected static final int RCP_CONTAINMASK=9;
	protected static final int RCP_SPELL=10;

	protected Item key=null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(buildingI==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override public String parametersFile(){ return "wainwright.txt";}
	@Override protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	@Override public boolean supportsDeconstruction() { return true; }

	@Override
	public boolean mayICraft(final Item I)
	{
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(isANativeItem(I.Name()))
			return true;
		if(I instanceof Container)
		{
			final Container C=(Container)I;
			if((C.containTypes()==Container.CONTAIN_BODIES)
			||(C.containTypes()==Container.CONTAIN_CAGED)
			||(C.containTypes()==(Container.CONTAIN_BODIES|Container.CONTAIN_CAGED)))
				return false;
		}
		if(I instanceof Rideable)
		{
			final Rideable R=(Rideable)I;
			final int rideType=R.rideBasis();
			switch(rideType)
			{
			case Rideable.RIDEABLE_AIR:
			case Rideable.RIDEABLE_LAND:
			case Rideable.RIDEABLE_WAGON:
				return true;
			default:
				return false;
			}
		}
		return (isANativeItem(I.Name()));
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((buildingI!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(activity == CraftingActivity.LEARNING)
							commonEmote(mob,L("<S-NAME> fail(s) to learn how to make @x1.",buildingI.name()));
						else
							commonEmote(mob,L("<S-NAME> mess(es) up building @x1.",buildingI.name()));
						buildingI.destroy();
					}
					else
					if(activity==CraftingActivity.LEARNING)
					{
						deconstructRecipeInto( buildingI, recipeHolder );
						buildingI.destroy();
					}
					else
					{
						dropAWinner(mob,buildingI);
						if(key!=null)
						{
							dropAWinner(mob,key);
							if(buildingI instanceof Container)
								key.setContainer((Container)buildingI);
						}
					}
				}
				buildingI=null;
				key=null;
			}
		}
		super.unInvoke();
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_WOOD );
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;

		final CraftParms parsedVars=super.parseAutoGenerate(auto,givenTarget,commands);
		givenTarget=parsedVars.givenTarget;

		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,parsedVars.autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,L("Wainwright what? Enter \"wainwright list\" for a list, \"wainwright learn <item>\" to gain recipes, or \"wainwright stop\" to cancel."));
			return false;
		}
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String str=(String)commands.elementAt(0);
		String startStr=null;
		int duration=4;
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final int[] cols={
					ListingLibrary.ColFixer.fixColWidth(25,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(5,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(8,mob.session())
				};
			final StringBuffer buf=new StringBuffer(L("@x1 @x2 @x3 Wood required\n\r",CMStrings.padRight(L("Item"),cols[0]),CMStrings.padRight(L("Level"),cols[1]),CMStrings.padRight(L("Capacity"),cols[2])));
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					final int capacity=CMath.s_int(V.get(RCP_CAPACITY));
					if(((level<=xlevel(mob))||allFlag)
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRight(""+capacity,cols[2])+" "+wood+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		activity = CraftingActivity.CRAFTING;
		buildingI=null;
		key=null;
		messedUp=false;
		int amount=-1;
		if((commands.size()>1)&&(CMath.isNumber((String)commands.lastElement())))
		{
			amount=CMath.s_int((String)commands.lastElement());
			commands.removeElementAt(commands.size()-1);
		}
		final String recipeName=CMParms.combine(commands,0);
		List<String> foundRecipe=null;
		final List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			final List<String> V=matches.get(r);
			if(V.size()>0)
			{
				final int level=CMath.s_int(V.get(RCP_LEVEL));
				if((parsedVars.autoGenerate>0)||(level<=xlevel(mob)))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,L("You don't know how to build a '@x1'.  Try \"list\" as your parameter for a list.",recipeName));
			return false;
		}

		final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
		final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName),parsedVars.autoGenerate);
		if(componentsFoundList==null)
			return false;
		int woodRequired=CMath.s_int(woodRequiredStr);
		woodRequired=adjustWoodRequired(woodRequired,mob);

		if(amount>woodRequired)
			woodRequired=amount;
		final int[] pm={RawMaterial.MATERIAL_WOODEN};
		final String misctype=foundRecipe.get(RCP_MISCTYPE);
		final int[][] data=fetchFoundResourceData(mob,
											woodRequired,"wood",pm,
											0,null,null,
											false,
											parsedVars.autoGenerate,
											null);
		if(data==null)
			return false;
		woodRequired=data[0][FOUND_AMT];
		bundling=misctype.equalsIgnoreCase("BUNDLE");
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final int lostValue=parsedVars.autoGenerate>0?0:
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],0,null)
			+CMLib.ableMapper().destroyAbilityComponents(componentsFoundList);
		buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
		if(buildingI==null)
		{
			commonTell(mob,L("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
			return false;
		}
		duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
		String itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
		if(bundling)
			itemName="a "+woodRequired+"# "+itemName;
		else
			itemName=CMLib.english().startWithAorAn(itemName);
		buildingI.setName(itemName);
		startStr=L("<S-NAME> start(s) building @x1.",buildingI.name());
		displayText=L("You are building @x1",buildingI.name());
		verb=L("building @x1",buildingI.name());
		playSound="hammer.wav";
		buildingI.setDisplayText(L("@x1 lies here",itemName));
		buildingI.setDescription(itemName+". ");
		buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired,bundling));
		buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
		buildingI.setMaterial(data[0][FOUND_CODE]);
		buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
		buildingI.setSecretIdentity(getBrand(mob));
		final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
		final long canContain=getContainerType(foundRecipe.get(RCP_CONTAINMASK));
		final int riders=CMath.s_int(foundRecipe.get(RCP_NUMRIDERS));
		final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
		addSpells(buildingI,spell);
		key=null;
		if(buildingI instanceof Rideable)
		{
			((Rideable)buildingI).setRideBasis(Rideable.RIDEABLE_WAGON);
			((Rideable)buildingI).setRiderCapacity(riders);
		}

		if((buildingI instanceof Container)
		&&(!(buildingI instanceof Armor)))
		{
			if(capacity>0)
			{
				((Container)buildingI).setCapacity(capacity+woodRequired);
				((Container)buildingI).setContainTypes(canContain);
			}
			if(misctype.equalsIgnoreCase("LID"))
				((Container)buildingI).setDoorsNLocks(true,false,true,false,false,false);
			else
			if(misctype.equalsIgnoreCase("LOCK"))
			{
				((Container)buildingI).setDoorsNLocks(true,false,true,true,false,true);
				((Container)buildingI).setKeyName(Double.toString(Math.random()));
				key=CMClass.getItem("GenKey");
				((DoorKey)key).setKey(((Container)buildingI).keyName());
				key.setName(L("a key"));
				key.setDisplayText(L("a small key sits here"));
				key.setDescription(L("looks like a key to @x1",buildingI.name()));
				key.recoverPhyStats();
				key.text();
			}
		}
		if(bundling)
			buildingI.setBaseValue(lostValue);
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();


		messedUp=!proficiencyCheck(mob,0,auto);

		if(bundling)
		{
			messedUp=false;
			duration=1;
			verb=L("bundling @x1",RawMaterial.CODES.NAME(buildingI.material()).toLowerCase());
			startStr=L("<S-NAME> start(s) @x1.",verb);
			displayText=L("You are @x1",verb);
		}

		if(parsedVars.autoGenerate>0)
		{
			commands.addElement(buildingI);
			return true;
		}

		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return true;
	}
}
