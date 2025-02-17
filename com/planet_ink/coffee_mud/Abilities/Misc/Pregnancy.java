package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Pregnancy extends StdAbility implements HealthCondition
{
	@Override public String ID() { return "Pregnancy"; }
	private final static String localizedName = CMLib.lang().L("Pregnancy");
	@Override public String name() { return localizedName; }
	protected long monthsRemaining=-1;
	protected long daysRemaining=-1;

	@Override
	public String getHealthConditionDesc()
	{
		final int x=text().indexOf('/');
		if(x>0)
		{
			final TimeClock C=CMLib.time().localClock(affected);
			final int y=text().indexOf('/',x+1);
			if(y<0)
				return "";
			final long start=CMath.s_long(text().substring(0,x));
			final long divisor=CMProps.getTickMillis()*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
			final long days=(System.currentTimeMillis()-start)/divisor; // down to days;
			final long months=days/C.getDaysInMonth();
			if(days<1)
				return "less than 1 day pregnant";
			else
			if(months<1)
				return days+" day(s) pregnant";
			else
				return months+" month(s) pregnant";
		}
		return "";
	}

	@Override
	public String displayText()
	{
		final String text=getHealthConditionDesc();
		if(text.length()>0)
			return "(is "+text+")";
		return "";
	}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	@Override public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings =I(new String[] {"IMPREGNATE"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public boolean canBeUninvoked(){return false;}
	@Override public boolean isAutoInvoked(){return false;}
	@Override public boolean isSavable(){ return true; }
	@Override public int classificationCode(){return Ability.ACODE_PROPERTY;}
	protected int ticksInLabor=0;


	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.target()==affected)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(CMLib.flags().canBeSeenBy(affected,msg.source()))
		&&(affected instanceof MOB)
		&&((daysRemaining>0)&&(monthsRemaining<=3)))
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,
										  CMMsg.MSG_OK_VISUAL,L("\n\r<S-NAME> <S-IS-ARE> obviously with child.\n\r"),
										  CMMsg.NO_EFFECT,null,
										  CMMsg.NO_EFFECT,null));
		super.executeMsg(host,msg);
	}

	public Race getRace(MOB babe, String race1, String race2)
	{
		if(race1.indexOf(race2)>=0)
			return CMClass.getRace(race1);
		else
		if(race2.indexOf(race1)>=0)
			return CMClass.getRace(race2);

		Race R=null;
		if(race1.equalsIgnoreCase("Human")||race2.equalsIgnoreCase("Human"))
		{
			String halfRace=(race1.equalsIgnoreCase("Human")?race2:race1);
			R=CMClass.getRace(halfRace);
			if((R!=null)&&(!R.ID().toUpperCase().startsWith("HALF")))
			{
				halfRace="Half"+CMStrings.capitalizeAndLower(R.ID().toLowerCase());
				final Race testR=CMClass.getRace(halfRace);
				if(testR!=null)
					R=testR;
				else
				{
					R=R.mixRace(CMClass.getRace("Human"),halfRace,"Half "+CMStrings.capitalizeAndLower(R.name()));
					if(R.isGeneric() && (!R.ID().equals(race1))&& (!R.ID().equals(race2)))
					{
						CMClass.addRace(R);
						CMLib.database().DBCreateRace(R.ID(),R.racialParms());
					}
				}
			}
		}
		else
		if(race1.equalsIgnoreCase("Halfling")||race2.equalsIgnoreCase("Halfling"))
		{
			String halfRace=(race1.equalsIgnoreCase("Halfling")?race2:race1);
			R=CMClass.getRace(halfRace);
			if((R!=null)&&(!R.ID().endsWith("ling")))
			{
				halfRace=R.ID()+"ling";
				final Race testR=CMClass.getRace(halfRace);
				if(testR!=null)
					R=testR;
				else
				{
					R=R.mixRace(CMClass.getRace("Halfling"),halfRace,CMStrings.capitalizeAndLower(R.name())+"ling");
					if(R.isGeneric() && (!R.ID().equals(race1))&& (!R.ID().equals(race2)))
					{
						CMClass.addRace(R);
						CMLib.database().DBCreateRace(R.ID(),R.racialParms());
					}
				}
			}
		}
		else
		{
			String first=null;
			if(race1.length()==race2.length())
				first=(race1.compareToIgnoreCase(race2)<0)?race1:race2;
			else
			if(race1.length()>race2.length())
				first=race1;
			else
				first=race2;
			final String second=(first.equals(race1)?race2:race1);
			final String halfRace=(race1.compareToIgnoreCase(race2)<0)?race1+race2:race2+race1;
			final Race testR=CMClass.getRace(halfRace);
			final Race FIRSTR=CMClass.getRace(first);
			final Race SECONDR=CMClass.getRace(second);
			if(testR!=null)
				R=testR;
			else
			{
				R=FIRSTR.mixRace(SECONDR,halfRace,FIRSTR.name()+"-"+SECONDR.name());
				if(R.isGeneric() && (!R.ID().equals(race1))&& (!R.ID().equals(race2)))
				{
					CMClass.addRace(R);
					CMLib.database().DBCreateRace(R.ID(),R.racialParms());
				}
			}
		}
		return R;
	}


	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB)
		&&(CMLib.flags().isInTheGame((MOB)affected,true)))
		{
			final MOB mob=(MOB)affected;
			final int x=text().indexOf('/');
			if(x>0)
			{
				final int y=text().indexOf('/',x+1);
				if(y>x)
				{
					final TimeClock C=CMLib.time().localClock(mob.getStartRoom());
					final int z=text().indexOf('/',y+1);
					final long end=CMath.s_long(text().substring(x+1,y));
					final long divisor=CMProps.getTickMillis()*CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
					daysRemaining=(end-System.currentTimeMillis())/divisor; // down to days
					monthsRemaining=daysRemaining/C.getDaysInMonth(); // down to months
					if(CMLib.dice().roll(1,200,0)==1)
					{
						final Ability A=CMClass.getAbility("Mood");
						if(A!=null)
							A.invoke(mob,new XVector("RANDOM"),mob,true,0);
					}
					if(daysRemaining<7) // BIRTH!
					{
						if(CMLib.flags().isSleeping(mob))
							mob.enqueCommand(CMParms.parse("WAKE"),Command.METAFLAG_FORCED,0);
						if((CMLib.dice().rollPercentage()>50)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5))
							mob.location().show(mob,null,CMMsg.MSG_NOISE,L("<S-NAME> moan(s) and scream(s) in labor pain!!"));
						ticksInLabor++;
						if(ticksInLabor>=45)
						{
							ticksInLabor=0;
							final String race1=mob.baseCharStats().getMyRace().ID();
							char gender='F';
							String sondat="daughter";
							final MOB babe=CMClass.getMOB("GenMOB");
							if(CMLib.dice().rollPercentage()>50)
							{
								gender='M';
								sondat="son";
							}
							String desc="The "+sondat+" of "+mob.Name();
							babe.addTattoo(new MOB.Tattoo("PARENT:"+mob.Name()));
							String race2=mob.baseCharStats().getMyRace().ID();
							MOB otherParentM=null;
							if(z>y)
							{
								race2=text().substring(z+1).trim();
								otherParentM=CMLib.players().getLoadPlayer(text().substring(y+1,z));
								desc+=" and "+text().substring(y+1,z);
								if(otherParentM != null)
									babe.addTattoo(new MOB.Tattoo("PARENT:"+otherParentM.Name()));
							}
							desc+=".";
							mob.curState().setMovement(0);
							mob.curState().setHitPoints(mob.curState().getHitPoints()/2);
							mob.location().show(mob,null,CMMsg.MSG_NOISE,L("***** <S-NAME> !!!GIVE(S) BIRTH!!! ******"));
							if(CMLib.dice().rollPercentage()>5)
							{
								Ability A=mob.fetchEffect(ID());
								while(A!=null)
								{
									mob.delEffect(A);
									A.setAffectedOne(null);
									A=mob.fetchEffect(ID());
								}
								A=mob.fetchAbility(ID());
								while(A!=null)
								{
									mob.delAbility(A);
									A.setAffectedOne(null);
									A=mob.fetchAbility(ID());
								}
							}
							Race R=getRace(babe,race1,race2);
							if(R==null)
								R=mob.baseCharStats().getMyRace();
							String name=CMLib.english().startWithAorAn(R.makeMobName(gender, 2)).toLowerCase();
							babe.setName(name);
							CMLib.factions().setAlignment(babe,Faction.Align.GOOD);
							for(final Pair<Clan,Integer> p : CMLib.clans().findRivalrousClans(mob))
								babe.setClan(p.first.clanID(),p.first.getAutoPosition());
							babe.setLiegeID(mob.getLiegeID());
							babe.setDescription(desc);
							babe.setDisplayText(L("@x1 is here.",name));
							CMLib.beanCounter().clearZeroMoney(babe,null);
							babe.baseCharStats().setMyRace(R);
							babe.baseCharStats().setStat(CharStats.STAT_QUICKNESS,10);
							babe.baseCharStats().setStat(CharStats.STAT_ENDURANCE,6);
							babe.baseCharStats().setStat(CharStats.STAT_DEXTERITY,2);
							babe.baseCharStats().setStat(CharStats.STAT_GENDER,gender);
							babe.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,2);
							babe.baseCharStats().setStat(CharStats.STAT_STRENGTH,1);
							babe.baseCharStats().setStat(CharStats.STAT_SPIRIT,1);
							babe.baseCharStats().getMyRace().startRacing(babe,false);
							babe.basePhyStats().setHeight(babe.basePhyStats().height()/10);
							babe.basePhyStats().setWeight(babe.basePhyStats().weight()/10);
							babe.baseState().setHitPoints(1);
							babe.baseState().setMana(0);
							babe.baseState().setMovement(0);
							babe.recoverCharStats();
							if(!CMLib.flags().isAnimalIntelligence(babe))
							{
								if(CMLib.dice().rollPercentage()>50)
								{
									Ability A=mob.fetchEffect("Allergies");
									if(A!=null)
									{
										A=(Ability)A.copyOf();
										babe.addNonUninvokableEffect(A);
									}
									else
									{
										A=CMClass.getAbility("Allergies");
										if(A!=null)
											A.invoke(babe,babe,true,0);
									}
								}
								final Ability STAT=CMClass.getAbility("Prop_StatTrainer");
								if(STAT!=null)
								{
									STAT.setMiscText("CHA=10 CON=6 DEX=2 INT=2 STR=1 WIS=1");
									babe.addNonUninvokableEffect(STAT);
								}
							}
							final Ability SAFE=CMClass.getAbility("Prop_SafePet");
							if(SAFE!=null)
								babe.addNonUninvokableEffect(SAFE);
							final Ability AGE=CMClass.getAbility("Age");
							if(AGE!=null)
							{
								AGE.setMiscText(""+System.currentTimeMillis());
								babe.addNonUninvokableEffect(AGE);
							}
							babe.recoverCharStats();
							babe.recoverPhyStats();
							babe.recoverMaxState();
							babe.resetToMaxState();
							final Item I=CMClass.getItem("GenCaged");
							((CagedAnimal)I).cageMe(babe);
							I.basePhyStats().setAbility(CagedAnimal.ABILITY_MOBPROGRAMMATICALLY);
							if(AGE != null)
								I.addNonUninvokableEffect((Ability)AGE.copyOf());
							name=CMLib.english().startWithAorAn(R.makeMobName((char)babe.baseCharStats().getStat(CharStats.STAT_GENDER), 1)).toLowerCase();
							I.setName(name);
							I.setDisplayText(L("@x1 is here.",name));
							I.recoverPhyStats();
							mob.location().addItem(I);
							final Behavior B=CMClass.getBehavior("Emoter");
							B.setParms(Age.happyBabyEmoter);
							I.addBehavior(B);
							I.text();
							if((!mob.isMonster())&&(mob.soulMate()==null))
							{
								if((CMLib.dice().rollPercentage()<20)&&(mob.fetchEffect("Disease_Depression")==null))
								{
									final Ability A=CMClass.getAbility("Disease_Depression");
									if(A!=null)
										A.invoke(mob,mob,true,0);
								}
							}
							if((mob.playerStats()!=null)||((otherParentM!=null)&&(otherParentM.playerStats()!=null)))
							{
								CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_BIRTHS);
								final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.BIRTHS);
								for(int i=0;i<channels.size();i++)
									CMLib.commands().postChannel(mob,channels.get(i),mob.name()+" has just given birth to "+I.name()+"!",true);
								String parent=mob.Name();
								if(mob.isMonster()&&(otherParentM!=null))
									parent=otherParentM.Name();
								if(AGE!=null)
									CMLib.database().DBCreateData(parent,"HEAVEN",parent+"/HEAVEN/"+AGE.text(),I.ID()+"/"+I.basePhyStats().ability()+"/"+I.text());
							}
						}
						else
							mob.tell(L("You are in labor!!"));

					}
					else
					{
						// pregnant folk get fatigued more often.
						if(mob.maxState().getFatigue()>Long.MIN_VALUE/2)
							mob.curState().adjFatigue(monthsRemaining*100,mob.maxState());
						if((monthsRemaining<=1)&&(CMLib.dice().rollPercentage()==1))
						{
							if(CMLib.flags().isSleeping(mob))
								mob.enqueCommand(CMParms.parse("WAKE"),Command.METAFLAG_FORCED,0);
							mob.tell(L("Oh! You had a contraction!"));
						}
						else
						if((monthsRemaining<=3)&&(CMLib.dice().rollPercentage()==1)&&(CMLib.dice().rollPercentage()==1))
							mob.tell(L("You feel a kick in your gut."));
						else
						if((monthsRemaining>8)&&(mob.location()!=null)&&(mob.location().getArea().getTimeObj().getHourOfDay()<2)&&(CMLib.dice().rollPercentage()==1))
						{
							if(CMLib.dice().rollPercentage()>25)
								mob.tell(L("You feel really sick this morning."));
							else
								mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("**BLEH** <S-NAME> just threw up."));
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);
		long start=System.currentTimeMillis();
		final Race R=target.charStats().getMyRace();
		long tickspermudmonth=CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
		final TimeClock C=CMLib.time().localClock(target.getStartRoom());
		tickspermudmonth=tickspermudmonth*C.getDaysInMonth();
		int birthmonths=(int)Math.round(CMath.mul((R.getAgingChart()[1]-R.getAgingChart()[0])*C.getMonthsInYear(),0.75));
		if(birthmonths<=0)
			birthmonths=5;
		final long ticksperbirthperiod=tickspermudmonth*birthmonths;
		final long millisperbirthperiod=ticksperbirthperiod*CMProps.getTickMillis();

		long end=start+millisperbirthperiod;
		if(success)
		{
			if(!auto)
			{
				end=start;
				start-=millisperbirthperiod;
			}
			if(mob.location().show(mob,target,this,CMMsg.TYP_GENERAL,auto?null:L("<S-NAME> imgregnate(s) <T-NAMESELF>.")))
			{
				setMiscText(start+"/"+end+"/"+mob.Name()+"/"+mob.charStats().getMyRace().ID());
				final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONCEPTIONS);
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel(channels.get(i),mob.clans(),target.name()+" is now in a 'family way'.",true);
				target.addNonUninvokableEffect((Ability)copyOf());
			}
		}
		else
		if(!auto)
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to impregnate <T-NAMESELF>, but fail(s)!"));
		return success;
	}
}
