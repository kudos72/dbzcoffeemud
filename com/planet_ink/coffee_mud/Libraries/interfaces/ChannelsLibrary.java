package com.planet_ink.coffee_mud.Libraries.interfaces;
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
public interface ChannelsLibrary extends CMLibrary
{
	public final int QUEUE_SIZE=100;

	public int getNumChannels();
	public CMChannel getChannel(int i);
	public List<ChannelMsg> getChannelQue(int i, int numNewToSkip, int numToReturn);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i, boolean offlineOK);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, Session ses, int i);
	public boolean mayReadThisChannel(MOB M, int i, boolean zapCheckOnly);
	public void channelQueUp(int i, CMMsg msg);
	public int getChannelIndex(String channelName);
	public int getChannelCodeNumber(String channelName);
	public List<String> getFlaggedChannelNames(ChannelFlag flag);
	public String getExtraChannelDesc(String channelName);
	public List<CMChannel> getIMC2ChannelsList();
	public List<CMChannel> getI3ChannelsList();
	public String[] getChannelNames();
	public String findChannelName(String channelName);
	public List<Session> clearInvalidSnoopers(Session mySession, int channelCode);
	public void restoreInvalidSnoopers(Session mySession, List<Session> invalid);
	public int loadChannels(String list, String ilist, String imc2list);
	public boolean channelTo(Session ses, boolean areareq, int channelInt, CMMsg msg, MOB sender);
	public void reallyChannel(MOB mob, String channelName, String message, boolean systemMsg);


	/**
	 * Basic Channel definition
	 * @author Bo Zimmerman
	 */
	public static class CMChannel
	{
		public String name="";
		public String i3name="";
		public String imc2Name="";
		public String mask="";
		public String colorOverride="";
		public String colorOverrideStr="";
		public Set<ChannelFlag> flags=new HashSet<ChannelFlag>();
		public List<ChannelMsg> queue=new SLinkedList<ChannelMsg>();
		public CMChannel(){}
		public CMChannel(String name, String i3name, String imc2name)
		{ this.name=name; this.i3name=i3name; this.imc2Name=imc2name;}
	}

	public static class ChannelMsg
	{
		public final CMMsg msg;
		public long ts;
		public ChannelMsg(CMMsg msg){this.msg=msg; ts=System.currentTimeMillis();}
		public ChannelMsg(CMMsg msg, long tm){this.msg=msg; ts=tm;}
	}

	public static enum ChannelFlag {
		DEFAULT,SAMEAREA,CLANONLY,READONLY,
		EXECUTIONS,LOGINS,LOGOFFS,BIRTHS,MARRIAGES,
		DIVORCES,CHRISTENINGS,LEVELS,DETAILEDLEVELS,DEATHS,DETAILEDDEATHS,
		CONQUESTS,CONCEPTIONS,NEWPLAYERS,LOSTLEVELS,PLAYERPURGES,CLANINFO,
		WARRANTS, PLAYERREADONLY, CLANALLYONLY, ACCOUNTOOC, NOBACKLOG
	}
}
