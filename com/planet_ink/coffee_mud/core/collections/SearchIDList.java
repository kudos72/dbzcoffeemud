package com.planet_ink.coffee_mud.core.collections;

import java.util.List;

/*
Copyright 2012-2014 Bo Zimmerman

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
 * A searchable list, usually sorted or otherwise made worth searching.
 * @author Bo Zimmerman
 *
 * @param <T>
 */
public interface SearchIDList<T> extends List<T>
{
	/**
	 * Searches the sorted list of objects for one with the
	 * given ID;
	 * @param arg0 the ID of the Object to look for
	 * @return the object or null if not found
	 */
	public T find(String arg0);

	/**
	 * Searches the sorted list of objects for one with the
	 * same ID as the object given.
	 * @param arg0 the Object like the one to look for
	 * @return the object or null if not found
	 */
	public T find(T arg0);
}
