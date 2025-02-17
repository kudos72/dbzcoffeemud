package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Song extends StdAbility
{
	@Override public String ID() { return "Song"; }
	private final static String localizedName = CMLib.lang().L("a Song");
	@Override public String name() { return localizedName; }
	@Override public String displayText() { return "("+songOf()+")"; }
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings =I(new String[] {"SING","SI"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int classificationCode(){return Ability.ACODE_SONG|Ability.DOMAIN_SINGING;}
	@Override public int maxRange(){return adjustedMaxInvokerRange(2);}

	protected boolean HAS_QUANTITATIVE_ASPECT(){return true;}
	protected boolean skipStandardSongInvoke(){return false;}
	protected boolean mindAttack(){return abstractQuality()==Ability.QUALITY_MALICIOUS;}
	protected boolean skipStandardSongTick(){return false;}
	protected boolean maliciousButNotAggressiveFlag(){return false;}
	protected boolean skipSimpleStandardSongTickToo(){return false;}
	protected String songOf(){return L("Song of ")+name();}
	protected long timeOut = 0;
	protected Vector commonRoomSet=null;
	protected Room originRoom=null;


	@Override
	public int adjustedLevel(MOB mob, int asLevel)
	{
		final int level=super.adjustedLevel(mob,asLevel);
		final int charisma=(invoker().charStats().getStat(CharStats.STAT_QUICKNESS)-10);
		if(charisma>10)
			return level+(charisma/3);
		return level;
	}

	@Override
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		if(this.invoker()==affectedEnv)
			affectableStats.addAmbiance("(?)singing of "+songOf().toLowerCase());
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((affected==invoker)
		&&(msg.amISource(invoker))
		&&(!unInvoked))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)
					||((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)&&(CMath.bset(msg.sourceCode(), CMMsg.MASK_SOUND))))
			&&(!(msg.tool() instanceof Song))
			&&(!msg.sourceMajor(CMMsg.MASK_CHANNEL)))
			{
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,CMMsg.MSG_NOISE,L("<S-NAME> stop(s) singing."));
				unInvoke();
			}
			else
			if((msg.target() instanceof Armor)
			&&(msg.targetMinor()==CMMsg.TYP_WEAR)
			&&(CMath.bset(((Armor)msg.target()).rawProperLocationBitmap(),Wearable.WORN_MOUTH)))
			{
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,CMMsg.MSG_NOISE,L("<S-NAME> stop(s) singing."));
				unInvoke();
			}
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A instanceof Song)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((!super.tick(ticking,tickID))||(!(affected instanceof MOB)))
			return false;

		if(skipSimpleStandardSongTickToo())
			return true;

		final MOB mob=(MOB)affected;
		if((affected==invoker())&&(invoker()!=null)&&(invoker().location()!=originRoom))
		{
			final Vector V=getInvokerScopeRoomSet(null);
			commonRoomSet.clear();
			commonRoomSet.addAll(V);
			originRoom=invoker().location();
		}
		else
		if((abstractQuality()==Ability.QUALITY_MALICIOUS)
		&&(!maliciousButNotAggressiveFlag())
		&&(!mob.amDead())
		&&(mob.isMonster())
		&&(!mob.isInCombat())
		&&(mob.amFollowing()==null)
		&&((!(mob instanceof Rideable))||(((Rideable)mob).numRiders()==0))
		&&(!CMLib.flags().isATrackingMonster(mob))
		&&(CMLib.flags().aliveAwakeMobile(mob,true)))
		{
			if((mob.location()!=originRoom)
			&&(CMLib.flags().isMobile(mob)))
			{
				final int dir=this.getCorrectDirToOriginRoom(mob.location(),commonRoomSet.indexOf(mob.location()));
				if(dir>=0)
					CMLib.tracking().walk(mob,dir,false,false);
			}
			else
			if((mob.location().isInhabitant(invoker()))
			&&(CMLib.flags().canBeSeenBy(invoker(),mob)))
				CMLib.combat().postAttack(mob,invoker(),mob.fetchWieldedItem());
		}

		if((invoker==null)
		||(invoker.fetchEffect(ID())==null)
		||(commonRoomSet==null)
		||(!commonRoomSet.contains(mob.location())))
			return unsingMe(mob,null);

		if(skipStandardSongTick())
			return true;

		if((invoker==null)
		||(!CMLib.flags().aliveAwakeMobile(invoker,true))
		||(!CMLib.flags().canBeHeardSpeakingBy(invoker,mob)))
			return unsingMe(mob,null);
		return true;
	}

	protected void unsingAll(MOB mob, MOB invoker)
	{
		if(mob!=null)
			for(int a=mob.numEffects()-1;a>=0;a--)
			{
				final Ability A=mob.fetchEffect(a);
				if((A instanceof Song)
				&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
					((Song)A).unsingMe(mob,invoker);
			}
	}

	protected void unsingAllByThis(MOB mob, MOB invoker)
	{
		if(mob!=null)
			for(int a=mob.numEffects()-1;a>=0;a--)
			{
				final Ability A=mob.fetchEffect(a);
				if((A instanceof Song)
				&&(!A.ID().equals(ID()))
				&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
					((Song)A).unsingMe(mob,invoker);
			}
	}

	protected boolean unsingMe(MOB mob, MOB invoker)
	{
		if(mob==null)
			return false;
		final Ability A=mob.fetchEffect(ID());
		if((A instanceof Song)
		&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
		{
			final Song S=(Song)A;
			if(S.timeOut==0)
				S.timeOut = System.currentTimeMillis()
						  + (CMProps.getTickMillis() * (((invoker()!=null)&&(invoker()!=mob))?super.getXTIMELevel(invoker()):0));
			if(System.currentTimeMillis() >= S.timeOut)
			{
				A.unInvoke();
				return false;
			}
		}
		return true;
	}

	protected Vector getInvokerScopeRoomSet(MOB backupMob)
	{
		if((invoker()==null)
		||(invoker().location()==null))
		{
			if((backupMob!=null)&&(backupMob.location()!=null))
				 return new XVector(backupMob.location());
			return new Vector();
		}
		final int depth=getXMAXRANGELevel(invoker());
		if(depth==0)
			return new XVector(invoker().location());
		final Vector rooms=new Vector();
		// needs to be area-only, because of the aggro-tracking rule
		TrackingLibrary.TrackingFlags flags;
		flags = new TrackingLibrary.TrackingFlags()
				.plus(TrackingLibrary.TrackingFlag.OPENONLY)
				.plus(TrackingLibrary.TrackingFlag.AREAONLY)
				.plus(TrackingLibrary.TrackingFlag.NOAIR);
		CMLib.tracking().getRadiantRooms(invoker().location(), rooms,flags, null, depth, null);
		if(!rooms.contains(invoker().location()))
			rooms.addElement(invoker().location());
		return rooms;
	}

	protected int getCorrectDirToOriginRoom(Room R, int v)
	{
		if(v<0)
			return -1;
		int dir=-1;
		Room R2=null;
		Exit E2=null;
		int lowest=v;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			R2=R.getRoomInDir(d);
			E2=R.getExitInDir(d);
			if((R2!=null)&&(E2!=null)&&(E2.isOpen()))
			{
				final int dx=commonRoomSet.indexOf(R2);
				if((dx>=0)&&(dx<lowest))
				{
					lowest=dx;
					dir=d;
				}
			}
		}
		return dir;
	}

	protected String getCorrectMsgString(Room R, String str, int v)
	{
		String msgStr=null;
		if(R==originRoom)
			msgStr=str;
		else
		{
			final int dir=this.getCorrectDirToOriginRoom(R,v);
			if(dir>=0)
				msgStr=L("^SYou hear the @x1 being sung @x2!^?",songOf(),Directions.getInDirectionName(dir));
			else
				msgStr=L("^SYou hear the @x1 being sung nearby!^?",songOf());
		}
		return msgStr;
	}

	public Set<MOB> sendMsgAndGetTargets(MOB mob, Room R, CMMsg msg, Environmental givenTarget, boolean auto)
	{
		if(originRoom==R)
			R.send(mob,msg);
		else
			R.sendOthers(mob,msg);
		if(R!=originRoom)
			mob.setLocation(R);
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(R!=originRoom)
		{
			R.delInhabitant(mob);
			mob.setLocation(originRoom);
		}
		if(h==null)
			return null;
		if(R==originRoom)
		{
			if(!h.contains(mob))
				h.add(mob);
		}
		else
			h.remove(mob);
		return h;
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		timeOut=0;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_LEATHER))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(CMLib.dice().rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> hit(s) a foul note on @x1 due to <S-HIS-HER> armor!",name()));
			return false;
		}

		if(skipStandardSongInvoke())
			return true;

		if((!auto)&&(!CMLib.flags().canSpeak(mob)))
		{
			mob.tell(L("You can't sing!"));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);
		unsingAllByThis(mob,mob);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String str=auto?L("^SThe @x1 begins to play!^?",songOf()):L("^S<S-NAME> begin(s) to sing the @x1.^?",songOf());
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str=L("^S<S-NAME> start(s) the @x1 over again.^?",songOf());
			for(int v=0;v<commonRoomSet.size();v++)
			{
				final Room R=(Room)commonRoomSet.elementAt(v);
				final String msgStr=getCorrectMsgString(R,str,v);
				final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					final Set<MOB> h=this.sendMsgAndGetTargets(mob, R, msg, givenTarget, auto);
					if(h==null)
						continue;
					final Song newOne=(Song)this.copyOf();
					for (final Object element : h)
					{
						final MOB follower=(MOB)element;
						final Room R2=follower.location();

						// malicious songs must not affect the invoker!
						int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
						if(auto)
							affectType=affectType|CMMsg.MASK_ALWAYS;
						if((castingQuality(mob,follower)==Ability.QUALITY_MALICIOUS)&&(follower!=mob))
							affectType=affectType|CMMsg.MASK_MALICIOUS;

						if((CMLib.flags().canBeHeardSpeakingBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
						{
							CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
							final CMMsg msg3=msg2;
							if((mindAttack())&&(follower!=mob))
								msg2=CMClass.getMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
							if((R.okMessage(mob,msg2))&&(R.okMessage(mob,msg3)))
							{
								R2.send(follower,msg2);
								if(msg2.value()<=0)
								{
									R2.send(follower,msg3);
									if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
									{
										newOne.setSavable(false);
										if(follower!=mob)
											follower.addEffect((Ability)newOne.copyOf());
										else
											follower.addEffect(newOne);
									}
								}
							}
						}
					}
					R.recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,L("<S-NAME> hit(s) a foul note."));

		return success;
	}
}
