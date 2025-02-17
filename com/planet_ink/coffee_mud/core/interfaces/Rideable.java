package com.planet_ink.coffee_mud.core.interfaces;
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

import java.util.Iterator;
import java.util.Set;

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
/**
 * The interface for an item or mob which may be ridden
 * @see com.planet_ink.coffee_mud.core.interfaces.Rider
 * @author Bo Zimmerman
 *
 */
public interface Rideable extends PhysicalAgent
{
	/** constant for the  rideType() method.  Means it is ridden over land*/
	public final static int RIDEABLE_LAND=0;
	/** constant for the  rideType() method.  Means it is ridden over water*/
	public final static int RIDEABLE_WATER=1;
	/** constant for the  rideType() method.  Means it is ridden through the air*/
	public final static int RIDEABLE_AIR=2;
	/** constant for the  rideType() method.  Means it is sat upon*/
	public final static int RIDEABLE_SIT=3;
	/** constant for the  rideType() method.  Means it is slept upon*/
	public final static int RIDEABLE_SLEEP=4;
	/** constant for the  rideType() method.  Means it is sat at*/
	public final static int RIDEABLE_TABLE=5;
	/** constant for the  rideType() method.  Means it is entered into*/
	public final static int RIDEABLE_ENTERIN=6;
	/** constant for the  rideType() method.  Means it is climbed*/
	public final static int RIDEABLE_LADDER=7;
	/** constant for the  rideType() method.  Means it is pulled by others*/
	public final static int RIDEABLE_WAGON=8;
	/** constant for the  rideType() method.  Means it is climbed and travels with player*/
	public final static String[] RIDEABLE_DESCS=
	{
		"LAND-BASED","WATER-BASED","AIR-FLYING","FURNITURE-SIT","FURNITURE-SLEEP","FURNITURE-TABLE",
		"ENTER-IN","LADDER","WAGON"
	};

	/**
	 * Whether the type of rideable is mobile.
	 * @see Rideable
	 * @return Whether this type of rideable is mobile
	 */
	public boolean isMobileRideBasis();

	/**
	 * The type of rideable object this is.
	 * @see Rideable
	 * @return the RIDEABLE_* constant describing how this is ridden
	 */
	public int rideBasis();
	/**
	 * Sets type of rideable object this is.
	 * @see Rideable
	 * @param basis the RIDEABLE_* constant describing how this is ridden
	 */
	public void setRideBasis(int basis);
	/**
	 * The number of Riders which may ride upon this Rideable
	 * @see Rider
	 * @return the maximum riders
	 */
	public int riderCapacity();
	/**
	 * Sets the number of Riders which may ride upon this Rideable
	 * @see Rider
	 * @param newCapacity the maximum riders
	 */
	public void setRiderCapacity(int newCapacity);
	/**
	 * Returns the number of riders currently mounted on this Rideable
	 * @see Rider
	 * @return the number of current Riders
	 */
	public int numRiders();
	/**
	 * Returns an iterator of the riders on this rideable
	 * @see Rider
	 * @return the riders
	 */
	public Iterator<Rider> riders();

	/**
	 * Returns a particular Rider mounted on this Rideable.  May return null
	 * in the case of a race condition
	 * @see Rider
	 * @see Rideable
	 * @see Rideable#numRiders()
	 * @param which which rider to detch
	 * @return the rider riding
	 */
	public Rider fetchRider(int which);
	/**
	 * Adds a new Rider to this Rideable.  Is called by Rider.setRiding(Rideable)
	 * Should also call Rider.setRiding(Rideable) without recursion
	 * @see Rider
	 * @see Rideable
	 * @param mob Rider object, either an item or a mob
	 */
	public void addRider(Rider mob);
	/**
	 * Removes a Rider from this Rideable.  Is called by Rider.setRiding(null)
	 * Should also call Rider.setRiding(null) without recursion
	 * @see Rider
	 * @see Rideable
	 * @param mob Rider object, either an item or a mob
	 */
	public void delRider(Rider mob);
	/**
	 * Returns whether Rider is currently mounted on this Rideable
	 * @see Rider
	 * @see Rideable
	 * @param mob the Rider to check this Rideable for
	 * @return true if the Rider is mounted on this Rideable
	 */
	public boolean amRiding(Rider mob);
	/**
	 * Returns a string grammatically correct for the given rider when
	 * they are mounted on this Rideable
	 * @see Rider
	 * @see Rideable
	 * @param R The rider object to make grammatically correct.
	 * @return a string describing  the riders state of riding this Rideable
	 */
	public String stateString(Rider R);
	/**
	 * Returns a string grammatically correct for the given rider when
	 * they are putting something on this Rideable
	 * @see Rider
	 * @see Rideable
	 * @param R The rider object to make grammatically correct.
	 * @return a string describing  the riders state of putting something on this Rideable
	 */
	public String putString(Rider R);
	/**
	 * Returns a string grammatically correct for this Rideable when
	 * Riders are mounted
	 * @see Rider
	 * @see Rideable
	 * @param R The rider object to make grammatically correct.
	 * @return a string describing the Riderable state of being ridden
	 */
	public String stateStringSubject(Rider R);
	/**
	 * Whether this Rideable moves when the Rider wants to move it.  Largely derived
	 * from rideBasis().
	 * @see Rideable#rideBasis()
	 * @return whether this item moves with the rider
	 */
	public boolean mobileRideBasis();
	/**
	 * Returns a string grammatically correct for the given rider when
	 * they are mounting this Rideable
	 * @see Rider
	 * @see Rideable
	 * @param R The rider object to make grammatically correct.
	 * @return a string describing the riders state of mounting this Rideable
	 */
	public String mountString(int commandType, Rider R);
	/**
	 * Returns a string grammatically correct for the given rider when
	 * they are dismounting this Rideable
	 * @see Rider
	 * @see Rideable
	 * @param R The rider object to make grammatically correct.
	 * @return a string describing the riders state of dismounting this Rideable
	 */
	public String dismountString(Rider R);
	/**
	 * Adds all of the MOB Riders on this Rideable to the given HashSet and returns it
	 * @param list the hashset into which to add all the mob riders
	 * @return the same list passed in, filled
	 */
	public Set<MOB> getRideBuddies(Set<MOB> list);
}
