package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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

@SuppressWarnings("rawtypes")
public class Construction extends CraftingSkill
{
	@Override public String ID() { return "Construction"; }
	private final static String localizedName = CMLib.lang().L("Construction");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =I(new String[] {"CONSTRUCT"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public String supportedResourceString(){return "WOODEN";}

	protected static final int BUILD_WALL=0;
	protected static final int BUILD_DOOR=1;
	protected static final int BUILD_ROOF=2;
	protected static final int BUILD_GATE=3;
	protected static final int BUILD_FENCE=4;
	protected static final int BUILD_DEMOLISH=5;
	protected static final int BUILD_TITLE=6;
	protected static final int BUILD_DESC=7;
	protected static final int BUILD_SECRETDOOR=8;
	protected static final int BUILD_WINDOW=9;
	protected static final int BUILD_CRAWLWAY=10;
	protected static final int BUILD_STAIRS=11;

	private final static int DAT_NAME=0;
	private final static int DAT_WOOD=1;
	private final static int DAT_ROOF=2;
	private final static int DAT_REQDIR=3;
	private final static int DAT_REQNONULL=4;

	// name, wood, ok=0/roof=1/out=2, req direction=1, ok=0, ok=0, nonull=1, nullonly=2
	private final static String[][] data={
		{"Wall","100","1","1","0"},
		{"Door","125","1","1","0"},
		{"Roof","350","2","0","0"},
		{"Gate","50","2","1","0"},
		{"Fence","50","2","1","0"},
		{"Demolish","0","0","1","0"},
		{"Title","0","0","0","0"},
		{"Description","0","0","0","0"},
		{"Secret Door","200","1","1","0"},
		{"Window","50","1","1","1"},
		{"Crawlway","250","1","1","1"},
		{"Stairs","850","1","0","0"}
	};

	protected Room room=null;
	protected int dir=-1;
	protected int doingCode=-1;
	protected int workingOn=-1;
	protected String designTitle="";
	protected String designDescription="";
	public Construction(){super();}

	public Exit generify(Exit X)
	{
		final Exit E2=CMClass.getExit("GenExit");
		E2.setName(X.name());
		E2.setDisplayText(X.displayText());
		E2.setDescription(X.description());
		E2.setDoorsNLocks(X.hasADoor(),X.isOpen(),X.defaultsClosed(),X.hasALock(),X.isLocked(),X.defaultsLocked());
		E2.setBasePhyStats((PhyStats)X.basePhyStats().copyOf());
		E2.setExitParams(X.doorName(),X.closeWord(),X.openWord(),X.closedText());
		E2.setKeyName(X.keyName());
		E2.setOpenDelayTicks(X.openDelayTicks());
		E2.setReadable(X.isReadable());
		E2.setReadableText(X.readableText());
		E2.setTemporaryDoorLink(X.temporaryDoorLink());
		E2.recoverPhyStats();
		return E2;
	}

	protected void demolishRoom(MOB mob, Room room)
	{
		final LandTitle title=CMLib.law().getLandTitle(room);
		if(title==null)
			return;
		Room returnToRoom=null;
		Room backupToRoom1=null;
		Room backupToRoom2=null;
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			final Room R=room.getRoomInDir(d);
			if(CMLib.law().doesOwnThisLand(mob, R))
			{
				returnToRoom=R;
				break;
			}
			final LandTitle adjacentTitle=CMLib.law().getLandTitle(R);
			if((adjacentTitle==null)||(adjacentTitle.getOwnerName().length()>0))
				backupToRoom1=R;
			else
			if(R.roomID().length()>0)
				backupToRoom2=R;
		}
		if(returnToRoom==null)
			returnToRoom=backupToRoom1;
		if(returnToRoom==null)
			returnToRoom=backupToRoom2;
		if(returnToRoom==null)
			returnToRoom=mob.getStartRoom();
		if(returnToRoom==null)
			returnToRoom=room.getArea().getRandomProperRoom();
		if(returnToRoom==null)
			returnToRoom=room.getArea().getRandomMetroRoom();
		if(returnToRoom==null)
			returnToRoom=CMLib.map().getRandomRoom();
		final Room theRoomToReturnTo=returnToRoom;
		room.eachInhabitant(new EachApplicable<MOB>()
		{
			@Override
			public void apply(MOB a)
			{
				theRoomToReturnTo.bringMobHere(a, false);
			}
		});
		room.eachItem(new EachApplicable<Item>()
		{
			@Override
			public void apply(Item a)
			{
				theRoomToReturnTo.addItem(a,Expire.Player_Drop);
			}
		});
		title.setOwnerName("");
		title.updateLot(null); // this is neat -- this will obliterate leaf rooms around this one.
		if((theRoomToReturnTo!=null)
		&&(theRoomToReturnTo.rawDoors()[Directions.UP]==room)
		&&(theRoomToReturnTo.getRawExit(Directions.UP)!=null))
		{
			theRoomToReturnTo.getRawExit(Directions.UP).destroy();
			theRoomToReturnTo.setRawExit(Directions.UP, null);
		}
		CMLib.map().obliterateRoom(room);
	}

	protected Room convertToPlains(Room room)
	{
		final Room R=CMClass.getLocale("Plains");
		R.setRoomID(room.roomID());
		R.setDisplayText(room.displayText());
		R.setDescription(room.description());
		final Area area=room.getArea();
		if(area!=null)
			area.delProperRoom(room);
		R.setArea(room.getArea());
		for(int a=room.numEffects()-1;a>=0;a--)
		{
			final Ability A=room.fetchEffect(a);
			if(A!=null)
			{
				room.delEffect(A);
				R.addEffect(A);
			}
		}
		for(int i=room.numItems()-1;i>=0;i--)
		{
			final Item I=room.getItem(i);
			if(I!=null)
			{
				room.delItem(I);
				R.addItem(I);
			}
		}
		for(int m=room.numInhabitants()-1;m>=0;m--)
		{
			final MOB M=room.fetchInhabitant(m);
			if(M!=null)
			{
				room.delInhabitant(M);
				R.addInhabitant(M);
				M.setLocation(R);
			}
		}
		CMLib.threads().deleteTick(room,-1);
		for(int d=0;d<R.rawDoors().length;d++)
			R.rawDoors()[d]=room.rawDoors()[d];
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			R.setRawExit(d,room);
		R.startItemRejuv();
		try
		{
			for(final Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				final Room R2=(Room)r.nextElement();
				for(int d=0;d<R2.rawDoors().length;d++)
					if(R2.rawDoors()[d]==room)
					{
						R2.rawDoors()[d]=R;
						if(R2 instanceof GridLocale)
							((GridLocale)R2).buildGrid();
					}
			}
		}catch(final NoSuchElementException e){}
		R.getArea().fillInAreaRoom(R);
		CMLib.database().DBUpdateRoom(R);
		CMLib.database().DBUpdateExits(R);
		room.destroy();
		return R;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(!aborted)
				{
					if((messedUp)&&(room!=null))
					switch(doingCode)
					{
					case BUILD_ROOF:
						commonTell(mob,L("You've ruined the frame and roof!"));
						break;
					case BUILD_WALL:
						commonTell(mob,L("You've ruined the wall!"));
						break;
					case BUILD_DOOR:
						commonTell(mob,L("You've ruined the door!"));
						break;
					case BUILD_SECRETDOOR:
						commonTell(mob,L("You've ruined the secret door!"));
						break;
					case BUILD_FENCE:
						commonTell(mob,L("You've ruined the fence!"));
						break;
					case BUILD_GATE:
						commonTell(mob,L("You've ruined the gate!"));
						break;
					case BUILD_TITLE:
						commonTell(mob,L("You've ruined the titling!"));
						break;
					case BUILD_DESC:
						commonTell(mob,L("You've ruined the describing!"));
						break;
					case BUILD_WINDOW:
						commonTell(mob,L("You've ruined the window!"));
						break;
					case BUILD_CRAWLWAY:
						commonTell(mob,L("You've ruined the crawlway!"));
						break;
					case BUILD_STAIRS:
						commonTell(mob,L("You've ruined the stairs!"));
						break;
					case BUILD_DEMOLISH:
					default:
						commonTell(mob,L("You've failed to demolish!"));
						break;
					}
					else
					switch(doingCode)
					{
					case BUILD_ROOF:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							final Room R=CMClass.getLocale("WoodRoom");
							R.setRoomID(room.roomID());
							R.setDisplayText(room.displayText());
							R.setDescription(room.description());
							if(R.image().equalsIgnoreCase(CMLib.protocol().getDefaultMXPImage(room)))
								R.setImage(null);

							final Area area=room.getArea();
							if(area!=null)
								area.delProperRoom(room);
							R.setArea(area);
							for(int a=room.numEffects()-1;a>=0;a--)
							{
								final Ability A=room.fetchEffect(a);
								if(A!=null)
								{
									room.delEffect(A);
									R.addEffect(A);
								}
							}
							for(int i=room.numItems()-1;i>=0;i--)
							{
								final Item I=room.getItem(i);
								if(I!=null)
								{
									room.delItem(I);
									R.addItem(I);
								}
							}
							for(int m=room.numInhabitants()-1;m>=0;m--)
							{
								final MOB M=room.fetchInhabitant(m);
								if(M!=null)
								{
									room.delInhabitant(M);
									R.addInhabitant(M);
									M.setLocation(R);
								}
							}
							CMLib.threads().deleteTick(room,-1);
							for(int d=0;d<R.rawDoors().length;d++)
							{
								if((R.rawDoors()[d]==null)
								||(R.rawDoors()[d].roomID().length()>0))
									R.rawDoors()[d]=room.rawDoors()[d];
							}
							for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
							{
								if((R.rawDoors()[d]==null)
								||(R.rawDoors()[d].roomID().length()>0))
									R.setRawExit(d,room);
							}
							R.clearSky();
							R.startItemRejuv();
							try
							{
								boolean rebuild=false;
								for(final Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
								{
									final Room R2=(Room)r.nextElement();
									rebuild=false;
									for(int d=0;d<R2.rawDoors().length;d++)
									{
										if(R2.rawDoors()[d]==room)
										{
											rebuild=true;
											R2.rawDoors()[d]=R;
										}
									}
									if((rebuild)&&(R2 instanceof GridLocale))
										((GridLocale)R2).buildGrid();
								}
							}catch(final NoSuchElementException e){}
							try
							{
								for(final Enumeration e=CMLib.players().players();e.hasMoreElements();)
								{
									final MOB M=(MOB)e.nextElement();
									if(M.getStartRoom()==room)
										M.setStartRoom(R);
									else
									if(M.location()==room)
										M.setLocation(R);
								}
							}catch(final NoSuchElementException e){}
							R.getArea().fillInAreaRoom(R);
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
								Log.debugOut("Construction",R.roomID()+" updated.");
							CMLib.database().DBUpdateRoom(R);
							CMLib.database().DBUpdateExits(R);
						}
						room.destroy();
						break;
					}
					case BUILD_STAIRS:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							int floor=0;
							Room upRoom=room;
							while((upRoom!=null)&&(upRoom.ID().length()>0)&&(CMLib.law().getLandTitle(upRoom)!=null))
							{
								upRoom=upRoom.getRoomInDir(Directions.UP);
								floor++;
							}
							upRoom=CMClass.getLocale(CMClass.classID(room));
							upRoom.setRoomID(room.getArea().getNewRoomID(room,Directions.UP));
							if(upRoom.roomID().length()==0)
							{
								commonTell(mob,L("You've failed to build the stairs!"));
								break;
							}
							upRoom.setArea(room.getArea());
							LandTitle newTitle=CMLib.law().getLandTitle(room);
							if((newTitle!=null)&&(CMLib.law().getLandTitle(upRoom)==null))
							{
								newTitle=(LandTitle)((Ability)newTitle).copyOf();
								newTitle.setLandPropertyID(upRoom.roomID());
								newTitle.setOwnerName("");
								newTitle.setBackTaxes(0);
								upRoom.addNonUninvokableEffect((Ability)newTitle);
							}
							room.rawDoors()[Directions.UP]=upRoom;
							final Exit upExit=CMClass.getExit("OpenDescriptable");
							upExit.setMiscText("Upstairs to the "+(floor+1)+CMath.numAppendage(floor+1)+" floor.");
							room.setRawExit(Directions.UP,upExit);

							final Exit downExit=CMClass.getExit("OpenDescriptable");
							downExit.setMiscText("Downstairs to the "+(floor)+CMath.numAppendage(floor)+" floor.");
							upRoom.rawDoors()[Directions.DOWN]=room;
							upRoom.setRawExit(Directions.DOWN,downExit);
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
								Log.debugOut("Lots4Sale",upRoom.roomID()+" created and put up for sale.");
							CMLib.database().DBCreateRoom(upRoom);
							if(newTitle!=null)
								newTitle.updateLot(null);
							upRoom.getArea().fillInAreaRoom(upRoom);
							CMLib.database().DBUpdateExits(upRoom);
							CMLib.database().DBUpdateExits(room);
						}
						break;
					}
					case BUILD_DOOR:
					case BUILD_SECRETDOOR:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							final Exit X=CMClass.getExit("GenExit");
							if(doingCode==BUILD_SECRETDOOR)
								X.basePhyStats().setDisposition(PhyStats.IS_HIDDEN);
							X.setName(L("a door"));
							X.setDescription("");
							X.setDisplayText("");
							X.setOpenDelayTicks(9999);
							X.setExitParams("door","close","open","a closed door");
							X.setDoorsNLocks(true,false,true,false,false,false);
							X.recoverPhyStats();
							X.text();
							room.setRawExit(dir,X);
							if(room.rawDoors()[dir]!=null)
							{
								final Exit X2=(Exit)X.copyOf();
								if(doingCode==BUILD_SECRETDOOR)
									X2.basePhyStats().setDisposition(PhyStats.IS_HIDDEN);
								X2.recoverPhyStats();
								X2.text();
								room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),X2);
								CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
							}
							CMLib.database().DBUpdateExits(room);
						}
						break;
					}
					case BUILD_GATE:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							final Exit X=CMClass.getExit("GenExit");
							X.setName(L("a wooden gate"));
							X.setDescription("");
							X.setDisplayText("");
							X.setOpenDelayTicks(9999);
							X.setExitParams("gate","close","open","a closed gate");
							X.setDoorsNLocks(true,false,true,false,false,false);
							X.text();
							room.setRawExit(dir,X);
							if(room.rawDoors()[dir]!=null)
							{
								final Exit X2=(Exit)X.copyOf();
								room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),X2);
								CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
							}
							CMLib.database().DBUpdateExits(room);
						}
						break;
					}
					case BUILD_WALL:
					case BUILD_FENCE:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							room.setRawExit(dir,null);
							if(room.rawDoors()[dir]!=null)
							{
								room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),null);
								CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
							}
							CMLib.database().DBUpdateExits(room);
							final LandTitle title=CMLib.law().getLandTitle(room);
							if(title != null)
								title.updateLot(null);
						}
						break;
					}
					case BUILD_TITLE:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							room.setDisplayText(designTitle);
							CMLib.database().DBUpdateRoom(room);
						}
						break;
					}
					case BUILD_DESC:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							if(workingOn>=0)
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.getRawExit(workingOn)==E))
								{
									E=generify(E);
									room.setRawExit(workingOn,E);
								}
								E.setDescription(designDescription);
								CMLib.database().DBUpdateExits(room);
							}
							else
							{
								room.setDescription(designDescription);
								CMLib.database().DBUpdateRoom(room);
							}
						}
						break;
					}
					case BUILD_CRAWLWAY:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							if((workingOn>=0)&&(room.getExitInDir(workingOn)!=null))
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.getRawExit(workingOn)==E))
								{
									E=generify(E);
									room.setRawExit(workingOn,E);
								}
								final Ability A=CMClass.getAbility("Prop_Crawlspace");
								if(A!=null)
									E.addNonUninvokableEffect(A);
								CMLib.database().DBUpdateExits(room);
							}
						}
						break;
					}
					case BUILD_WINDOW:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							if((workingOn>=0)&&(room.getExitInDir(workingOn)!=null))
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.getRawExit(workingOn)==E))
								{
									E=generify(E);
									room.setRawExit(workingOn,E);
								}
								final Room R2=room.getRoomInDir(workingOn);
								if(R2!=null)
								{
									final Ability A=CMClass.getAbility("Prop_RoomView");
									if(A!=null)
									{
										A.setMiscText(CMLib.map().getExtendedRoomID(R2));
										E.addNonUninvokableEffect(A);
									}
								}
								CMLib.database().DBUpdateExits(room);
							}
						}
						break;
					}
					case BUILD_DEMOLISH:
					default:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							if(dir<0)
							{

								if(CMLib.law().isHomeRoomUpstairs(room))
								{
									demolishRoom(mob,room);
								}
								else
									convertToPlains(room);
							}
							else
							{
								room.setRawExit(dir,CMClass.getExit("Open"));
								if(room.rawDoors()[dir]!=null)
								{
									room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),CMClass.getExit("Open"));
									CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
								}
								CMLib.database().DBUpdateExits(room);
							}
						}
						break;
					}
					}
				}
			}
		}
		super.unInvoke();
	}

	public boolean isHomePeerRoom(Room R)
	{
		return ifHomePeerLandTitle(R)!=null;
	}

	public boolean isHomePeerTitledRoom(Room R)
	{
		final LandTitle title = ifHomePeerLandTitle(R);
		if(title == null)
			return false;
		return title.getOwnerName().length()>0;
	}

	public LandTitle ifHomePeerLandTitle(Room R)
	{
		if((R!=null)
		&&(R.ID().length()>0)
		&&(CMath.bset(R.domainType(),Room.INDOORS)))
			return CMLib.law().getLandTitle(R);
		return null;
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()==0)
		{
			commonTell(mob,L("Construct what, where? Try Construct list."));
			return false;
		}
		final String str=(String)commands.elementAt(0);
		final int colWidth=ListingLibrary.ColFixer.fixColWidth(20,mob.session());
		if(("LIST").startsWith(str.toUpperCase()))
		{
			final String mask=CMParms.combine(commands,1);
			final StringBuffer buf=new StringBuffer(L("@x1 Wood required\n\r",CMStrings.padRight(L("Item"),colWidth)));
			for(int r=0;r<data.length;r++)
			{
				if(((r!=BUILD_SECRETDOOR)||(mob.charStats().getCurrentClass().baseClass().equals("Thief")))
				&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(CMStrings.padRight(data[r][DAT_NAME],colWidth),mask)))
				{
					final int woodRequired=adjustWoodRequired(CMath.s_int(data[r][DAT_WOOD]),mob);
					buf.append(CMStrings.padRight(data[r][DAT_NAME],colWidth)+" "+woodRequired+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}

		designTitle="";
		designDescription="";
		String startStr=null;
		int duration=35;
		doingCode=-1;
		workingOn=-1;
		dir=-1;

		room=null;
		messedUp=false;
		helpingAbility=null;

		if(str.equalsIgnoreCase("help"))
		{
			messedUp=!proficiencyCheck(mob,0,auto);
			duration=25;
			commands.removeElementAt(0);
			final MOB targetMOB=getTarget(mob,commands,givenTarget,false,true);
			if(targetMOB==null)
				return false;
			if(targetMOB==mob)
			{
				commonTell(mob,L("You can not do that."));
				return false;
			}
			helpingAbility=targetMOB.fetchEffect(ID());
			if(helpingAbility==null)
			{
				commonTell(mob,L("@x1 is not constructing anything.",targetMOB.Name()));
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			{
				helpingAbility=null;
				return false;
			}
			helping=true;
			verb=L("helping @x1 with @x2",targetMOB.name(),helpingAbility.name());
			startStr=L("<S-NAME> start(s) @x1",verb);
			final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr+".");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,duration);
			}
			return true;
		}

		boolean canBuild=CMLib.law().doesOwnThisLand(mob,mob.location());
		final String firstWord=(String)commands.firstElement();
		for(int r=0;r<data.length;r++)
		{
			if((data[r][0].toUpperCase().startsWith(firstWord.toUpperCase()))
			&&((r!=BUILD_SECRETDOOR)||(mob.charStats().getCurrentClass().baseClass().equals("Thief"))))
				doingCode=r;
		}
		if(doingCode<0)
		{
			commonTell(mob,L("'@x1' is not a valid construction project.  Try LIST.",firstWord));
			return false;
		}
		if((mob.location()!=null)
		&&((mob.location() instanceof BoardableShip) || (mob.location().getArea() instanceof BoardableShip)))
		{
			commonTell(mob,L("You may not do construction projects here."));
			return false;
		}
		final String dirName=(String)commands.lastElement();
		dir=Directions.getGoodDirectionCode(dirName);
		if((doingCode==BUILD_DEMOLISH)&&(dirName.equalsIgnoreCase("roof"))||(dirName.equalsIgnoreCase("ceiling")))
		{
			final Room upRoom=mob.location().getRoomInDir(Directions.UP);
			if(isHomePeerRoom(upRoom))
			{
				commonTell(mob,L("You need to demolish the upstairs rooms first."));
				return false;
			}
			if(mob.location().domainType() == Room.DOMAIN_INDOORS_CAVE)
			{
				commonTell(mob,L("A cave can not have its roof demolished."));
				return false;
			}
			if(!CMath.bset(mob.location().domainType(), Room.INDOORS))
			{
				commonTell(mob,L("There is no ceiling here!"));
				return false;
			}
			if(CMLib.law().isHomeRoomUpstairs(mob.location()))
			{
				commonTell(mob,L("You can't demolish upstairs ceilings.  Try demolishing the room."));
				return false;
			}
			dir=-1;
		}
		else
		if((doingCode==BUILD_DEMOLISH)&&(dirName.equalsIgnoreCase("room")))
		{
			final LandTitle title=CMLib.law().getLandTitle(mob.location());
			if((!CMLib.law().doesOwnThisLand(mob, mob.location()))
			&&(title!=null)
			&&(title.getOwnerName().length()>0))
			{
				commonTell(mob,L("You can't demolish property you don't own."));
				return false;
			}
			if((title==null)||(!title.allowsExpansionConstruction()))
			{
				commonTell(mob,L("You aren't permitted to demolish this room."));
				return false;
			}
			if(!CMLib.law().isHomeRoomUpstairs(mob.location()))
			{
				commonTell(mob,L("You can only demolish upstairs rooms.  You might try just demolishing the ceiling/roof?"));
				return false;
			}
			int numAdjacentProperties=0;
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				final Room adjacentRoom=mob.location().getRoomInDir(d);
				if(isHomePeerTitledRoom(adjacentRoom))
				{
					numAdjacentProperties++;
				}
			}
			if(numAdjacentProperties>1)
			{
				mob.tell(L("You can not demolish a room if there is more than one room adjacent to it.  Demolish those first."));
				return false;
			}
			dir=-1;
			canBuild=true;
		}
		else
		if(((dir<0)||(dir==Directions.UP)||(dir==Directions.DOWN))
		   &&(data[doingCode][DAT_REQDIR].equals("1")))
		{
			commonTell(mob,L("A valid direction in which to build must also be specified."));
			return false;
		}
		if(data[doingCode][DAT_REQNONULL].equals("1")
		   &&(dir>=0)
		   &&(mob.location().getExitInDir(dir)==null))
		{
			commonTell(mob,L("There is a wall that way that needs to be demolished first."));
			return false;
		}


		int woodRequired=adjustWoodRequired(CMath.s_int(data[doingCode][DAT_WOOD]),mob);
		if(((mob.location().domainType()&Room.INDOORS)==0)
		   &&(data[doingCode][DAT_ROOF].equals("1")))
		{
			commonTell(mob,L("That can only be built after a roof, which includes the frame."));
			return false;
		}
		else
		if(((mob.location().domainType()&Room.INDOORS)>0)
		   &&(data[doingCode][DAT_ROOF].equals("2")))
		{
			commonTell(mob,L("That can only be built outdoors!"));
			return false;
		}

		if(doingCode==BUILD_STAIRS)
		{
			final LandTitle title=CMLib.law().getLandTitle(mob.location());
			if((title==null)||(!title.allowsExpansionConstruction()))
			{
				commonTell(mob,L("The title here does not permit the building of stairs."));
				return false;
			}
			if(!CMath.bset(mob.location().domainType(), Room.INDOORS))
			{
				commonTell(mob,L("You need to build a ceiling (or roof) first!"));
				return false;
			}
			if((mob.location().getRoomInDir(Directions.UP)!=null)||(mob.location().rawDoors()[Directions.UP]!=null))
			{
				commonTell(mob,L("There are already stairs here."));
				return false;
			}
		}

		if(doingCode==BUILD_WALL)
		{
			final Room nextRoom=mob.location().getRoomInDir(dir);
			if((nextRoom!=null)&&(CMLib.law().getLandTitle(nextRoom)==null))
			{
				commonTell(mob,L("You can not build a wall blocking off the main entrance!"));
				return false;
			}
			if(mob.location().getExitInDir(dir)==null)
			{
				commonTell(mob,L("There is already a wall in that direction!"));
				return false;
			}
		}

		if(doingCode==BUILD_TITLE)
		{
			final String title=CMParms.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,L("A title must be specified."));
				return false;
			}
			final TrackingLibrary.TrackingFlags flags=new TrackingLibrary.TrackingFlags();
			final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,20);
			for (final Room room2 : checkSet)
			{
				final Room R=CMLib.map().getRoom(room2);
				if(R.displayText(mob).equalsIgnoreCase(title))
				{
					commonTell(mob,L("That title has already been taken.  Choose another."));
					return false;
				}
			}
			designTitle=title;
		}
		else
		if(doingCode==BUILD_DESC)
		{
			if(commands.size()<3)
			{
				commonTell(mob,L("You must specify an exit direction or room, followed by a description for it."));
				return false;
			}
			if(Directions.getGoodDirectionCode((String)commands.elementAt(1))>=0)
			{
				dir=Directions.getGoodDirectionCode((String)commands.elementAt(1));
				if(mob.location().getExitInDir(dir)==null)
				{
					commonTell(mob,L("There is no exit @x1 to describe.",Directions.getInDirectionName(dir)));
					return false;
				}
				workingOn=dir;
				commands.removeElementAt(1);
			}
			else
			if(!((String)commands.elementAt(1)).equalsIgnoreCase("room"))
			{
				commonTell(mob,L("'@x1' is neither the word room, nor an exit direction.",((String)commands.elementAt(1))));
				return false;
			}
			else
				commands.removeElementAt(1);

			final String title=CMParms.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,L("A description must be specified."));
				return false;
			}
			designDescription=title;
		}
		else
		if((doingCode==BUILD_WINDOW)||(doingCode==BUILD_CRAWLWAY))
			workingOn=dir;

		final int[] pm={RawMaterial.MATERIAL_WOODEN};
		final int[][] idata=fetchFoundResourceData(mob,
											woodRequired,"wood",pm,
											0,null,null,
											false,
											0,
											null);
		if(idata==null)
			return false;
		woodRequired=idata[0][FOUND_AMT];

		if(!canBuild)
		{
			if((dir>=0)
			&&((data[doingCode][DAT_REQDIR].equals("1")||(workingOn==dir))))
			{
				final Room R=mob.location().getRoomInDir(dir);
				if((R!=null)&&(CMLib.law().doesOwnThisLand(mob,R)))
					canBuild=true;
			}
		}
		if(!canBuild)
		{
			commonTell(mob,L("You'll need the permission of the owner to do that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		room=mob.location();
		if(woodRequired>0)
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,idata[0][FOUND_CODE],0,null);

		switch(doingCode)
		{
		case BUILD_ROOF:
			verb=L("building a frame and roof");
			break;
		case BUILD_WALL:
			verb=L("building the @x1 wall",Directions.getDirectionName(dir));
			break;
		case BUILD_FENCE:
			verb=L("building the @x1 fence",Directions.getDirectionName(dir));
			break;
		case BUILD_TITLE:
			verb=L("giving this place a title");
			break;
		case BUILD_DESC:
			verb=L("giving this place a description");
			break;
		case BUILD_GATE:
			verb=L("building the @x1 gate",Directions.getDirectionName(dir));
			break;
		case BUILD_DOOR:
			verb=L("building the @x1 door",Directions.getDirectionName(dir));
			break;
		case BUILD_SECRETDOOR:
			verb=L("building a hidden @x1 door",Directions.getDirectionName(dir));
			break;
		case BUILD_WINDOW:
			verb=L("building a window @x1",Directions.getDirectionName(dir));
			break;
		case BUILD_CRAWLWAY:
			verb=L("building a crawlway @x1",Directions.getDirectionName(dir));
			break;
		case BUILD_STAIRS:
			verb=L("building another floor");
			break;
		case BUILD_DEMOLISH:
		default:
			if(dir<0)
			{
				if((mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
				   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
						verb=L("demolishing the pool");
				else
				if((mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
				{
					commonTell(mob,null,null,L("You must demolish a pool from above."));
					return false;
				}
				else
				if(mob.location().domainType()!=Room.DOMAIN_INDOORS_WOOD)
				{
					commonTell(mob,null,null,L("There are no wooden constructs to demolish here!"));
					return false;
				}
				else
				if(CMLib.law().isHomeRoomUpstairs(mob.location()))
					verb=L("demolishing the room");
				else
					verb=L("demolishing the roof");
			}
			else
				verb=L("demolishing the @x1 wall",Directions.getDirectionName(dir));
			break;
		}
		messedUp=!proficiencyCheck(mob,0,auto);
		startStr=L("<S-NAME> start(s) @x1",verb);
		playSound="hammer.wav";
		if(duration<25)
			duration=25;

		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
