package com.planet_ink.coffee_mud.core;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Pattern;

import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;

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
/**
 * This singleton contains methods for parsing user input and arguments to
 * behaviors and properties.  This library also has methods for reconstructing
 * parseable user input from previously parsed input.  It is: the Parameter 
 * Parsing Library for CoffeeMud.
 * 
 * @author Bo Zimmerman
 */
public class CMParms
{
	private CMParms(){super();}
	private static CMParms inst=new CMParms();
	public final static CMParms instance(){return inst;}
	
	private static final DelimiterChecker spaceDelimiter=new DelimiterChecker();
	
	/**
	 * An overrideable class for supplying a delimiter determination tool
	 * @see CMParms#createDelimiter(char[])
	 * @see CMParms#parseEQParms(String, DelimiterChecker)
	 * @author Bo Zimmerman
	 *
	 */
	public static class DelimiterChecker
	{
		public boolean isDelimiter(final char c)
		{
			return Character.isWhitespace(c);
		}
	}
	
	/**
	 * Create a DelimiterChecker from the given set of characters that will
	 * be the delimiters.
	 * @see CMParms.DelimiterChecker
	 * @see CMParms#parseEQParms(String, DelimiterChecker)
	 * @param chars the delimiters as a char array
	 * @return the class you can use to check for them fast
	 */
	public final static DelimiterChecker createDelimiter(final char[] chars)
	{
		final boolean[] delims=new boolean[256];
		for(char c : chars)
			delims[(byte)c]=true;
		return new DelimiterChecker()
		{
			@Override
			public boolean isDelimiter(final char c)
			{
				return delims[(byte)c];
			}
		};
	}
	
	/**
	 * Combine two string arrays into a single one.
	 * @param strs1 the first array
	 * @param strs2 the second array
	 * @return the combined array
	 */
	public final static String[] combine(final String[] strs1, final String[] strs2)
	{
		final int strs1Len = strs1.length;
		final int strs2Len = strs2.length;
		final String[] array= new String[strs1Len+strs2Len];
		System.arraycopy(strs1, 0, array, 0, strs1Len);
		System.arraycopy(strs2, 0, array, strs1Len, strs2Len);
		return array;
	}
	
	/**
	 * Combine object arrays into a single one.
	 * @param objs the arrays
	 * @return the combined array
	 */
	public final static Object[] combine(final Object[]... objs)
	{
		int len=0;
		for(Object[] obj : objs)
			len+=obj.length;
		final Object[] array= new Object[len];
		int x=0;
		for(Object[] obj : objs)
		{
			System.arraycopy(obj, 0, array, x, obj.length);
			x+=obj.length;
		}
		return array;
	}

	/**
	 * Combine two int arrays into a single one.
	 * @param strs1 the first array
	 * @param strs2 the second array
	 * @return the combined array
	 */
	public final static int[] combine(final int[] strs1, final int[] strs2)
	{
		final int strs1Len = strs1.length;
		final int strs2Len = strs2.length;
		final int[] array= new int[strs1Len+strs2Len];
		System.arraycopy(strs1, 0, array, 0, strs1Len);
		System.arraycopy(strs2, 0, array, strs1Len, strs2Len);
		return array;
	}

	/**
	 * Combine two boolean arrays into a single one.
	 * @param strs1 the first array
	 * @param strs2 the second array
	 * @return the combined array
	 */
	public final static boolean[] combine(final boolean[] strs1, final boolean[] strs2)
	{
		final int strs1Len = strs1.length;
		final int strs2Len = strs2.length;
		final boolean[] array= new boolean[strs1Len+strs2Len];
		System.arraycopy(strs1, 0, array, 0, strs1Len);
		System.arraycopy(strs2, 0, array, strs1Len, strs2Len);
		return array;
	}
	
	/**
	 * Returns the given string, unless it contains a space, in which case
	 * it returns the string with double-quotes around it.
	 * @param str the string to check and return
	 * @return the string, quoted it necessary
	 */
	public final static String quoteIfNecessary(final String str)
	{
		if(str==null) 
			return str;
		if(str.indexOf(' ')>=0)
			return "\""+str+"\"";
		return str;
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now space delimited.
	 * @param commands the objects to combine into a single string
	 * @return the single string
	 */
	public final static String combine(final List<?> commands)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		{
			for(final Object o : commands)
				combined.append(o.toString()+" ");
		}
		return combined.toString().trim();
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now space delimited.
	 * @param commands the objects to combine into a single string
	 * @param startAt the index in the list to start at.
	 * @return the single string
	 */
	public final static String combine(final List<?> commands, final int startAt)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		{
			for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
				combined.append(commands.get(commandIndex).toString()+" ");
		}
		return combined.toString().trim();
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now space delimited.
	 * @param commands the objects to combine into a single string
	 * @param startAt the index in the list to start at.
	 * @param endAt the index in the list to end with
	 * @return the single string
	 */
	public final static String combine(final List<?> commands, final int startAt, final int endAt)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		{
			for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
				combined.append(commands.get(commandIndex).toString()+" ");
		}
		return combined.toString().trim();
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now delimited by the character given.
	 * @param commands the objects to combine into a single string
	 * @param withChar the character to use as a delimiter
	 * @param startAt the index in the list to start at.
	 * @param endAt the index in the list to end with
	 * @return the single string
	 */
	public final static String combineWith(final List<?> commands, final char withChar, final int startAt, final int endAt)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		{
			for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
				combined.append(commands.get(commandIndex).toString()+withChar);
		}
		return combined.toString().trim();
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now delimited by the character given.
	 * @param commands the objects to combine into a single string
	 * @param withChar the character to use as a delimiter
	 * @return the single string
	 */
	public final static String combineWith(final List<?> commands, final char withChar)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		{
			for(int commandIndex=0;commandIndex<commands.size();commandIndex++)
				combined.append(commands.get(commandIndex).toString()+withChar);
		}
		return combined.toString().trim();
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now space delimited.  If any toString() calls
	 * result in a string that contains spaces, then the string will
	 * be surrounded by double-quotes (")
	 * @param commands the objects to combine into a single string
	 * @param startAt the index in the list to start at.
	 * @param endAt the index in the list to end with
	 * @return the single string
	 */
	public final static String combineQuoted(final List<?> commands, final int startAt, final int endAt)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		{
			String s;
			for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
			{
				s=commands.get(commandIndex).toString();
				if(s.indexOf(' ')>=0)
					combined.append('\"').append(s).append("\" ");
				else
					combined.append(s).append(" ");
			}
		}
		return combined.toString().trim();
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now delimited by the string given.
	 * @param commands the objects to combine into a single string
	 * @param withSeparator the string to use as a delimiter
	 * @return the single string
	 */
	public final static String combineWith(final List<?> commands, final String withSeparator)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		{
			for(int commandIndex=0;commandIndex<commands.size();commandIndex++)
				combined.append(commands.get(commandIndex).toString()).append(withSeparator);
		}
		return combined.toString().trim();
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now delimited by spaces.  If any of the objects evaluate to strings with
	 * spaces, then they will be surrounded by double-quotes.
	 * @param commands the objects to combine into a single string
	 * @param startAt the first object in the list to use
	 * @return the single string
	 */
	public final static String combineQuoted(final List<?> commands, final int startAt)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		{
			String s;
			for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
			{
				s=commands.get(commandIndex).toString();
				if(s.indexOf(' ')>=0)
					combined.append('\"').append(s).append("\" ");
				else
					combined.append(s).append(" ");
			}
		}
		return combined.toString().trim();
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now delimited by tab characters.
	 * @param commands the objects to combine into a single string
	 * @param startAt the first object in the list to use
	 * @return the single string
	 */
	public final static String combineWithTabs(final List<?> commands, final int startAt)
	{
		return combineWithX(commands,'\t',startAt);
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now delimited by the given X string.
	 * @param commands the objects to combine into a single string
	 * @param delimiter the delimiter to use
	 * @param startAt the first object in the list to use
	 * @return the single string
	 */
	public final static String combineWithX(final List<?> commands, final String delimiter, final int startAt)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		{
			String s;
			for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
			{
				s=commands.get(commandIndex).toString();
				combined.append(s).append(delimiter);
			}
		}
		return combined.toString().trim();
	}


	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now delimited by the given X character.
	 * @param commands the objects to combine into a single string
	 * @param delimiter the delimiter to use
	 * @param startAt the first object in the list to use
	 * @return the single string
	 */
	public final static String combineWithX(final List<?> commands, final char delimiter, final int startAt)
	{
		final StringBuilder combined=new StringBuilder("");
		if(commands!=null)
		{
			String s;
			for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
			{
				s=commands.get(commandIndex).toString();
				combined.append(s).append(delimiter);
			}
		}
		return combined.toString().trim();
	}

	/**
	 * Returns a string containing the given objects, with toString()
	 * called, and now space delimited.
	 * @param flags the objects to combine into a single string
	 * @return the single string
	 */
	public final static String combine(final Set<?> flags)
	{
		final StringBuilder combined=new StringBuilder("");
		if(flags!=null)
		{
			for (final Object name : flags)
				combined.append(name.toString()).append(" ");
		}
		return combined.toString().trim();
	}

	/**
	 * Parses the given string space-delimited, with respect for quoted
	 * strings.  It then returns them as a simple list in the form
	 * PARAM=VALUE, one to each entry in the returned list.  Quotes
	 * will have been removed if present, though the values intact.
	 * @param str the string to parse
	 * @return the list of strings, with PARAM=VALUE unsplit.
	 */
	public final static List<String> cleanParameterList(final String str)
	{
		final List<String> commands=parse(str);
		String s;
		for(int i=0;i<commands.size();i++)
		{
			s=commands.get(i);
			if(s.startsWith("=")&&(s.length()>1)&&(i>0))
			{
				final String prev=commands.get(i-1);
				commands.set(i-1,prev+s);
				commands.remove(i);
				i--;
			}
			else
			if(s.endsWith("=")&&(s.length()>1)&&(i<(commands.size()-1)))
			{
				final String next=commands.get(i+1);
				commands.set(i,s+next);
				commands.remove(i+1);
			}
			else
			if(s.equals("=")&&((i>0)&&(i<(commands.size()-1))))
			{
				final String prev=commands.get(i-1);
				final String next=commands.get(i+1);
				commands.set(i-1,prev+"="+next);
				commands.remove(i);
				commands.remove(i+1);
				i--;
			}
		}
		return commands;
	}

	/**
	 * Parses the given string space-delimited, with respect for quoted
	 * strings.  
	 * @param str the string to parse
	 * @return the list of parsed strings
	 */
	public final static Vector<String> parse(final String str)
	{
		final Vector<String> commands=new Vector<String>();
		if(str==null) 
			return commands;
		final StringBuilder s=new StringBuilder();
		final char[] cs=str.toCharArray();
		int state=0;
		for(final char c : cs)
			switch(state)
			{
			case 0:
			{
				if(c=='\"')
					state=2;
				else
				if(!Character.isWhitespace(c))
				{
					s.append(c);
					state=1;
				}
				break;
			}
			case 1:
			{
				if(Character.isWhitespace(c))
				{
					if(s.length()>0)
						commands.add(s.toString());
					s.setLength(0);
					state=0;
				}
				else
					s.append(c);
				break;
			}
			case 2:
			{
				if(c=='\"')
				{
					commands.add(s.toString());
					s.setLength(0);
					state=0;
				}
				else
					s.append(c);
				break;
			}
			}
		if(s.length()>0)
			commands.add(s.toString());
		return commands;
	}

	/**
	 * Parses the given string comma-delimited.
	 * @param s the string to parse
	 * @param ignoreNulls don't include any of the empty entries (,,)
	 * @return the list of parsed strings
	 */
	public final static List<String> parseCommas(final String s, final boolean ignoreNulls)
	{
		return parseAny(s,',',ignoreNulls);
	}

	/**
	 * Returns a list of those flag strings that appear in the given string.
	 * @param s the string to search
	 * @param flags the set of flags to look for
	 * @return the list of found flag strings
	 */
	public final static List<String> parseCommandFlags(final String s, final String[] flags)
	{
		if((s==null)||(s.isEmpty())) 
			return new Vector<String>(1);
		final List<String> V=parseCommas(s,true);
		final Vector<String> finalV=new Vector<String>(V.size());
		int index;
		for(final String flag : V)
		{
			index=CMParms.indexOfIgnoreCase(flags, flag);
			if(index>=0)
				finalV.addElement(flags[index]);
		}
		return V;
	}

	/**
	 * Parses the given string tab-delimited.
	 * @param s the string to parse
	 * @param ignoreNulls don't include any of the empty entries (-tab-tab)
	 * @return the list of parsed strings
	 */
	public final static List<String> parseTabs(final String s, final boolean ignoreNulls)
	{
		return parseAny(s,'\t',ignoreNulls);
	}

	/**
	 * Parses the given string by the given delimiter.
	 * @param s the string to parse
	 * @param delimeter the delimeter to use
	 * @param ignoreNulls don't include any of the empty entries (-delim-delim)
	 * @return the list of parsed strings
	 */
	public final static List<String> parseAny(final String s, final String delimeter, final boolean ignoreNulls)
	{
		final Vector<String> V=new Vector<String>(1);
		if((s==null)||(s.isEmpty())) 
			return V;
		if((delimeter==null)||(delimeter.isEmpty()))
		{
			V.add(s.trim());
			return V;
		}
		int last=0;
		int next=s.indexOf(delimeter);
		if(next<0)
		{
			V.add(s.trim());
			return V;
		}
		while(next >=0)
		{
			final String sub=s.substring(last,next).trim(); 
			if(!ignoreNulls||(sub.length()>0))
				V.add(sub);
			last=next+delimeter.length();
			next=s.indexOf(delimeter,last);
		}
		final String sub = (last>=s.length())?"":s.substring(last,s.length()).trim();
		if(!ignoreNulls||(sub.length()>0))
			V.add(sub);
		return V;
	}

	/**
	 * Parses the given string by the given delimiter, ignoring the delimeter case.
	 * @param s the string to parse
	 * @param delimeter the delimeter to use
	 * @param ignoreNulls don't include any of the empty entries (-delim-delim)
	 * @return the list of parsed strings
	 */
	public final static List<String> parseAnyIgnoreCase(final String s, final String delimeter, final boolean ignoreNulls)
	{
		final Vector<String> V=new Vector<String>(1);
		if((s==null)||(s.isEmpty())) 
			return V;
		if((delimeter==null)||(delimeter.isEmpty()))
		{
			V.add(s.trim());
			return V;
		}
		int last=0;
		final String upS=s.toUpperCase();
		final String upD=delimeter.toUpperCase();
		int next=upS.indexOf(upD);
		if(next<0)
		{
			V.add(s.trim());
			return V;
		}
		while(next >=0)
		{
			final String sub=s.substring(last,next).trim(); 
			if(!ignoreNulls||(sub.length()>0))
				V.add(sub);
			last=next+delimeter.length();
			next=upS.indexOf(upD,last);
		}
		final String sub = (last>=s.length())?"":s.substring(last,s.length()).trim();
		if(!ignoreNulls||(sub.length()>0))
			V.add(sub);
		return V;
	}


	/**
	 * Parses the given string by the given delimiter.
	 * @param s the string to parse
	 * @param delimiter the delimiter to use
	 * @param ignoreNulls don't include any of the empty entries (-delim-delim)
	 * @return the list of parsed strings
	 */
	public final static List<String> parseAny(final String s, final char delimiter, final boolean ignoreNulls)
	{
		final Vector<String> V=new Vector<String>(1);
		if((s==null)||(s.isEmpty())) 
			return V;
		int last=0;
		String sub;
		for(int i=0;i<s.length();i++)
		{
			if(s.charAt(i)==delimiter)
			{
				sub=s.substring(last,i).trim();
				last=i+1;
				if(!ignoreNulls||(sub.length()>0))
					V.add(sub);
			}
		}
		sub = (last>=s.length())?"":s.substring(last,s.length()).trim();
		if(!ignoreNulls||(sub.length()>0))
			V.add(sub);
		return V;
	}

	/**
	 * Parses the given string squiggle-delimited.
	 * @param s the string to parse
	 * @return the list of parsed strings
	 */
	public final static List<String> parseSquiggles(final String s)
	{
		return parseAny(s,'~',false);
	}

	/**
	 * Parses the given string period-delimited.
	 * @param s the string to parse
	 * @return the list of parsed strings
	 */
	public final static List<String> parseSentences(final String s)
	{
		final Vector<String> V=new Vector<String>(1);
		if((s==null)||(s.isEmpty())) 
			return V;
		int last=0;
		String sub;
		for(int i=0;i<s.length();i++)
			if(s.charAt(i)=='.')
			{
				sub=s.substring(last,i+1).trim();
				last=i+1;
				V.add(sub);
			}
		sub = (last>=s.length())?"":s.substring(last,s.length()).trim();
		if(sub.isEmpty())
			V.add(sub);
		return V;
	}

	/**
	 * Parses the given string squiggle-delimited.
	 * @param s the string to parse
	 * @param ignoreNulls don't include any of the empty entries (~~)
	 * @return the list of parsed strings
	 */
	public final static List<String> parseSquiggleDelimited(final String s, final boolean ignoreNulls)
	{
		return parseAny(s,'~',ignoreNulls);
	}

	/**
	 * Parses the given string semicolon-delimited.
	 * @param s the string to parse
	 * @param ignoreNulls don't include any of the empty entries (;;)
	 * @return the list of parsed strings
	 */
	public final static List<String> parseSemicolons(final String s, final boolean ignoreNulls)
	{
		return parseAny(s,';',ignoreNulls);
	}

	/**
	 * Parses the given string space-delimited.
	 * @param s the string to parse
	 * @param ignoreNulls don't include any of the empty entries (-space-space)
	 * @return the list of parsed strings
	 */
	public final static List<String> parseSpaces(final String s, final boolean ignoreNulls)
	{
		return parseAny(s,' ',ignoreNulls);
	}

	/**
	 * Returns the number of MOBPROG 'Bits' in this string.
	 * Bits are space-delimited strings, which use single quotes for grouping
	 * words with spaces
	 * @param s the string to check
	 * @return the numbre of bits
	 */
	public final static int numBits(final String s)
	{
		return ((Integer)getBitWork(s,Integer.MAX_VALUE,2)).intValue();
	}

	/**
	 * This method removes any surrounding single quotes or spaces, and
	 * replaces any remaining single quotes with input-friendly fake
	 * single quotes (`).
	 * @param s the string to clean
	 * @return the cleaned string
	 */
	public final static String cleanBit(String s)
	{
		if(s.isEmpty())
			return s;
		if((s.charAt(0)==' ')||(s.charAt(s.length()-1)==' '))
			s=s.trim();
		if(s.length()>=2)
		{
			if(s.charAt(0)=='\'')
			{
				if(s.charAt(s.length()-1)=='\'')
					return s.substring(1,s.length()-1).replace('\'','`');
				return s.substring(1).replace('\'','`');
			}
			if(s.charAt(0)=='`')
			{
				if(s.charAt(s.length()-1)=='`')
					return s.substring(1,s.length()-1).replace('\'','`');
				return s.substring(1).replace('\'','`');
			}
		}
		return s.replace('\'','`');
	}

	/**
	 * Returns the string bit at the given 0-based index, cleaned of quotes.
	 * Bits are space-delimited strings, which use single quotes for grouping
	 * words with spaces
	 * @param s the string to parse
	 * @param which the bit index to return
	 * @return the string representing the given bit, or empty
	 */
	public final static String getCleanBit(final String s, final int which)
	{
		return cleanBit(getBit(s,which));
	}

	/**
	 * Returns the remainder of the string after the string bit at the
	 * given 0-based index, cleaned of quotes.
	 * Bits are space-delimited strings, which use single quotes for grouping
	 * words with spaces
	 * @param s the string to parse
	 * @param which the bit index to return
	 * @return the string representing the given bit, or empty
	 */
	public final static String getPastBitClean(final String s, final int which)
	{
		return cleanBit(getPastBit(s,which));
	}

	/**
	 * Returns the remainder of the string after the string bit at the
	 * given 0-based index, which may still have single quotes.
	 * Bits are space-delimited strings, which use single quotes for grouping
	 * words with spaces
	 * @param s the string to parse
	 * @param which the bit index to return
	 * @return the string representing the given bit, or empty
	 */
	public final static String getPastBit(final String s, final int which)
	{
		return (String)getBitWork(s,which,1);
	}

	/**
	 * Returns the string bit at the given 0-based index, which may still have its
	 * single quotes surrounding it.
	 * Bits are space-delimited strings, which use single quotes for grouping
	 * words with spaces
	 * @param s the string to parse
	 * @param which the bit index to return
	 * @return the string representing the given bit, or empty
	 */
	public final static String getBit(final String s, final int which)
	{
		return (String)getBitWork(s,which,0);
	}

	/**
	 * Work method for MOBPROG bits
	 * @param s string to parse
	 * @param which which 0-based bit to return
	 * @param op operation: 0=get it, 1=get passed bit, 2=get num bits
	 * @return Integer or String, depending on the operation
	 */
	private final static Object getBitWork(final String s, final int which, final int op)
	{
		int currOne=0;
		int start=-1;
		char q=' ';
		final char[] cs=s.toCharArray();
		for(int c=0;c<cs.length;c++)
			switch(start)
			{
			case -1:
				switch(cs[c])
				{
				case ' ': case '\t': case '\n': case '\r':
					break;
				case '\'': case '`':
					q=cs[c];
					start=c;
					break;
				default:
					q=' ';
					start=c;
					break;
				}
				break;
			default:
				if((cs[c]==q)||(q==' ' && cs[c]=='\t'))
				{
					if((q!=' ')
					&&(c<cs.length-1)
					&&(!Character.isWhitespace(cs[c+1])))
						break;
					if(which==currOne)
					{
						switch(op)
						{
						case 0:
							if(q==' ')
								return new String(cs,start,c-start);
							return new String(cs,start,c-start+1);
						case 1:
							return new String(cs,c+1,cs.length-c-1).trim();
						}
					}
					currOne++;
					start=-1;
				}
				break;
			}
		switch(op)
		{
		case 0:
			if(start<0)
				return "";
			return new String(cs,start,cs.length-start);
		case 1:
			return "";
		default:
			if(start<0)
				return Integer.valueOf(currOne);
			return Integer.valueOf(currOne+1);
		}
	}

	/**
	 * This method is a sloppy, forgiving method doing KEY=VALUE value searches in a string.
	 * Returns the value of the given key when the parameter is formatted in the given text
	 * in the format [KEY]=[VALUE] or [KEY]="[VALUE]".  If the key is not found, it will
	 * return the given defaultVal.  The key is case insensitive, and start-partial.  For
	 * example, a key of NAME will match NAMEY or NAME12.
	 * No assumptions are made about the given text.  It could have other garbage data of
	 * any format around it.  For example, if BOB is the key, then a text string like:
	 * 'joe larry bibob=hoo moe="uiuiui bob=goo lou", bob="yoo"' will still return "goo".
	 * If the key is found, but followed by a + or -, the default value is always returned.
	 * The value ends when either an end quote is encountered, or a whitespace, semicolon, or
	 * comma.
	 * @param text the string to search
	 * @param key the key to search for, case insensitive
	 * @param defaultVal the value to return if the key is not found
	 * @return the value
	 */
	public final static String getParmStr(String text, final String key, final String defaultVal)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
				{
					if((text.charAt(x)=='+')||(text.charAt(x)=='-'))
						return defaultVal;
					x++;
				}
				if(x<text.length())
				{
					boolean endWithQuote=false;
					while((x<text.length())&&(!Character.isLetterOrDigit(text.charAt(x))))
					{
						if(text.charAt(x)=='\"')
						{
							endWithQuote=true;
							x++;
							break;
						}
						x++;
					}
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())
							&&((!endWithQuote)&&(!Character.isWhitespace(text.charAt(x)))&&(text.charAt(x)!=';')&&(text.charAt(x)!=','))
								||((endWithQuote)&&(text.charAt(x)!='\"')))
									x++;
						return text.substring(0,x).trim();
					}

				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultVal;
	}

	/**
	 * This method is a sloppy, forgiving method doing KEY>=[VALUE] value searches in a string.
	 * Searches and finds the numeric value of the given key when the parameter is formatted in the given text
	 * in the format [KEY]=[VALUE] where = may be ==,=,!=,>,>=,<,or <=.  The key is case insensitive, 
	 * and start-partial.  For example, a key of NAME will match NAMEY or NAME12.
	 * It will then do the given comparison against the value passed in, populate the comparator found array
	 * with the comparator found, and the method returns the result of the compare.
	 * No assumptions are made about the given text.  It could have other garbage data of
	 * any format around it.  For example, if BOB is the key, and the value is 3, then a text string like:
	 * 'joe larry bibob=2 moe="uiuiui bob>7 lou", bob=5' will return a comparator of > and the compare result of false.
	 * @param text the string to search
	 * @param key the key to search for, case insensitive
	 * @param value the value to compare the found value against
	 * @param comparatorFound a one-dimensional array to contain the found comparator
	 * @return the result of comparing the found value with the given value
	 */
	public final static boolean getParmCompare(String text, final String key, final int value, char[] comparatorFound)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())
					&&(text.charAt(x)!='>')
					&&(text.charAt(x)!='<')
					&&(text.charAt(x)!='!')
					&&(text.charAt(x)!='='))
					x++;

				if(x<text.length()-1)
				{
					final char comp=text.charAt(x);
					boolean andEqual=(text.charAt(x)=='=');
					if(text.charAt(x+1)=='=')
					{ 
						x++; 
						andEqual=true;
					}
					if(x<text.length()-1)
					{
						
						while((x<text.length())&&(Character.isWhitespace(text.charAt(x))))
							x++;
						if((x<text.length())&&(Character.isDigit(text.charAt(x))))
						{
							text=text.substring(x);
							x=0;
							while((x<text.length())&&(Character.isDigit(text.charAt(x))))
								x++;
							final int found=CMath.s_int(text.substring(0,x));
							if((comparatorFound!=null)&&(comparatorFound.length>0))
								comparatorFound[1]=comp;
							if(andEqual&&(found==value))
							{
								return comp != '!';
							}
							switch(comp)
							{
								case '>': return value > found;
								case '<': return value < found;
								case '!': return true;
							}
							return false;
						}
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		if((comparatorFound!=null)&&(comparatorFound.length>0))
			comparatorFound[1]='\0';
		return false;
	}

	/**
	 * Parses the given string for [PARAM]=[VALUE] or [PARAM]="[VALUE]" formatted key/pair
	 * values, relying on the given parmList to provide possible [PARAM] values. 
	 * Returns a map of the found parameters and their values.
	 * This method is a sloppy, forgiving method doing KEY=VALUE value searches in a string.
	 * The key is case insensitive, and start-partial.  For example, a key of NAME will match NAMEY or NAME12.
	 * The value ends when the next parameter is encountered.  If it's not on the parmList, it 
	 * is a value.
	 * @param str the unparsed string
	 * @param parmList exhaustive list of all parameters
	 * @return the map of parameters
	 */
	public final static Map<String,String> parseEQParms(final String str, final String[] parmList)
	{
		final Hashtable<String,String> h=new Hashtable<String,String>();
		int lastEQ=-1;
		String lastParm=null;
		char c;
		for(int x=0;x<str.length();x++)
		{
			c=Character.toUpperCase(str.charAt(x));
			if(Character.isLetter(c))
			{
				for (final String element : parmList)
				{
					if((Character.toUpperCase(element.charAt(0)) == c)
					&&((str.length()-x) >= element.length())
					&&(str.substring(x,x+element.length()).equalsIgnoreCase(element)))
					{
						int chkX=x+element.length();
						while((chkX<str.length())&&(Character.isWhitespace(str.charAt(chkX))))
							chkX++;
						if((chkX<str.length())&&(str.charAt(chkX)=='='))
						{
							chkX++;
							if((lastParm!=null)&&(lastEQ>0))
							{
								String val=str.substring(lastEQ,x).trim();
								if(val.startsWith("\"")&&(val.endsWith("\"")))
									val=val.substring(1,val.length()-1).trim();
								h.put(lastParm,val);
							}
							lastParm=element;
							x=chkX;
							lastEQ=chkX;
						}
					}
				}
			}
		}
		if((lastParm!=null)&&(lastEQ>0))
		{
			String val=str.substring(lastEQ).trim();
			if(val.startsWith("\"")&&(val.endsWith("\"")))
				val=val.substring(1,val.length()-1).trim();
			h.put(lastParm,val);
		}
		return h;
	}

	/**
	 * Given a key/value parameter map, returns a string that is reparseable in a given
	 * delimited way as KEY=VLUE, adding double quotes if any value has the delimiter.
	 * @param parms the key/value pairs
	 * @param delimiter the delimieter to use between key/pairs
	 * @return the combined string
	 */
	public final static String combineEQParms(final Map<String,String> parms, final char delimiter)
	{
		final StringBuilder str=new StringBuilder("");
		for(final String key : parms.keySet())
		{
			final String val=parms.get(key);
			if(val.indexOf(delimiter)>0)
				str.append(key).append("=\"").append(parms.get(key)).append('\"').append(delimiter);
			else
				str.append(key).append('=').append(parms.get(key)).append(delimiter);
		}
		if(str.length()==0)
			return "";
		return str.substring(0,str.length()-1);
	}

	/**
	 * Parses the given string for [PARAM]=[VALUE] or [PARAM]="[VALUE]" formatted key/pair
	 * values in space-delimited fashion, respecting quotes value.
	 * Returns a map of the found parameters and their values, with keys normalized to
	 * uppercase.
	 * @param parms the string to parse
	 * @return the map of key/value pairs found.
	 */
	public final static Map<String,String> parseEQParms(final String parms)
	{
		return parseEQParms(parms,spaceDelimiter);
	}
	
	/**
	 * Parses the given string for [PARAM]=[VALUE] or [PARAM]="[VALUE]" formatted key/pair
	 * values in given-delimited fashion, respecting quotes value.
	 * Returns a map of the found parameters and their values, with keys normalized to
	 * uppercase.
	 * @param parms the string to parse
	 * @param delimiterCheck the checker to determine if a character is the given delimiter
	 * @return the map of key/value pairs found.
	 */
	public final static Map<String,String> parseEQParms(final String parms, final DelimiterChecker delimiterCheck)
	{
		final Map<String,String> h=new Hashtable<String,String>();
		int state=0;
		int start=-1;
		String parmName=null;
		int lastPossibleStart=-1;
		boolean lastWasWhitespace=false;
		final StringBuilder str=new StringBuilder(parms);
		for(int x=0;x<=str.length();x++)
		{
			final char c=(x==str.length())?'\n':str.charAt(x);
			switch(state)
			{
			case 0:
				if((c=='_')||(Character.isLetter(c)))
				{
					start=x;
					state=1;
					parmName=null;
				}
				break;
			case 1:
				if(c=='=')
				{
					parmName=str.substring(start,x).toUpperCase().trim();
					state=2;
				}
				else
				if(delimiterCheck.isDelimiter(c))
				{
					if((!Character.isWhitespace(c))&&(x<str.length()))
						str.setCharAt(x,' '); // has to be trimmable
					parmName=str.substring(start,x).toUpperCase().trim();
					start=x;
				}
				break;
			case 2:
				if((c=='\"')||(c=='\n'))
				{
					state=3;
					start=x+1;
					lastPossibleStart=start;
				}
				else
				if(c=='=')
				{ // do nothing, this is a do-over
				}
				else
				if(!delimiterCheck.isDelimiter(c))
				{
					lastWasWhitespace=false;
					state=4;
					start=x;
					lastPossibleStart=start;
				}
				else
				if(!Character.isWhitespace(c))
					str.setCharAt(x,' '); // has to be trimmable
				break;
			case 3:
				if(c=='\\')
					str.deleteCharAt(x);
				else
				if(c=='\"')
				{
					state=0;
					h.put(parmName,str.substring(start,x));
					parmName=null;
				}
				break;
			case 4:
				if(c=='\\')
					str.deleteCharAt(x);
				else
				if(c=='=')
				{
					final String value=str.substring(start,x).trim();
					if(value.isEmpty())
						state=2;
					else
					{
						h.put(parmName,str.substring(start,lastPossibleStart).trim());
						parmName=str.substring(lastPossibleStart,x).toUpperCase().trim();
						state=2;
					}
				}
				else
				if(c=='\n')
				{
					state=0;
					h.put(parmName,str.substring(start,x));
					parmName=null;
				}
				else
				if(delimiterCheck.isDelimiter(c))
				{
					if(!Character.isWhitespace(c))
						str.setCharAt(x,' '); // has to be trimmable
					lastWasWhitespace=true;
				}
				else
				if(lastWasWhitespace)
				{
					lastWasWhitespace=false;
					lastPossibleStart=x;
				}
				break;
			}
		}
		return h;
	}

	/**
	 * Parses the given strings in the given string list for [PARAM]=[VALUE] or 
	 * [PARAM]="[VALUE]" formatted key/pairv values in space-delimited fashion, respecting quotes value.
	 * Returns a map of the found parameters and their values, with keys normalized to
	 * uppercase. The map is a combine of all the strings in the given list
	 * @param parms the string list of parseable strings with key=value pairs
	 * @param start the starting index in the list to use
	 * @param end the last index in the list to use
	 * @return the key/value mapping pairs combines
	 */
	public final static Map<String,String> parseEQParms(final List<String> parms, int start, final int end)
	{
		final Map<String,String> h=new Hashtable<String,String>();
		for(;start<end;start++)
			h.putAll(parseEQParms(parms.get(start)));
		return h;
	}

	/**
	 * Parses the given string into a two-dimensional list, using the two given delimiters
	 * @param text the string to parse
	 * @param delim1 the 'outer' delimiter
	 * @param delim2 the 'inner' delimiter
	 * @return the two-dimensional string list
	 */
	public final static List<List<String>> parseDoubleDelimited(String text, final char delim1, final char delim2)
	{
		final List<String> preparseV=new Vector<String>();
		int y=0;
		while((text!=null)&&(text.length()>0))
		{
			y=text.indexOf(delim1);
			while((y>0)&&(text.charAt(y-1)=='\\'))
				y=text.indexOf(delim1,y+1);
			String script="";
			if(y<0)
			{
				script=text.trim();
				text="";
			}
			else
			{
				script=text.substring(0,y).trim();
				text=text.substring(y+1).trim();
			}
			if(script.length()>0)
				preparseV.add(script);
		}
		final List<List<String>> parsedV=new Vector<List<String>>();
		for(String s : preparseV)
		{
			final List<String> groupV=new Vector<String>();
			while(s.length()>0)
			{
				y=-1;
				int yy=0;
				while(yy<s.length())
					if((s.charAt(yy)==delim2)
					&&((yy<=0)||(s.charAt(yy-1)!='\\'))) 
					{
						y=yy;
						break;
					}
					else
					if((s.charAt(yy)=='\n')||(s.charAt(yy)=='\r'))
					{
						y=yy;
						break;
					}
					else 
						yy++;
				String cmd="";
				if(y<0)
				{
					cmd=s.trim();
					s="";
				}
				else
				{
					cmd=s.substring(0,y).trim();
					s=s.substring(y+1).trim();
				}
				if((cmd.length()>0)&&(!cmd.startsWith("#")))
				{
					cmd=CMStrings.replaceAll(cmd,"\\"+delim1,""+delim1);
					cmd=CMStrings.replaceAll(cmd,"\\=","=");
					groupV.add(CMStrings.replaceAll(cmd,"\\"+delim2,""+delim2));
				}
			}
			if(groupV.size()>0)
				parsedV.add(groupV);
		}
		return parsedV;
	}

	/**
	 * This method is a sloppy, forgiving method doing KEY+[INT] or KEY-[INT] value searches 
	 * in a string.  Returns the value of the given key.  If the key is not found, it will
	 * return 0.  The key is case insensitive, and start-partial.  For
	 * example, a key of NAME will match NAMEY or NAME12.
	 * No assumptions are made about the given text.  It could have other garbage data of
	 * any format around it.  For example, if BOB is the key, then a text string like:
	 * 'joe larry bibob+5 moe="uiuiui bob-2 lou", bob+2' will still return -2.
	 * If the key is found, but followed by a =, 0 is returned.
	 * @param text the string to search
	 * @param key the key to search for, case insensitive
	 * @return the value
	 */
	public final static int getParmPlus(String text, final String key)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
				{
					if(text.charAt(x)=='=')
						return 0;
					x++;
				}
				if(x<text.length())
				{
					final char pm=text.charAt(x);
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						if(pm=='+')
							return CMath.s_int(text.substring(0,x));
						return -CMath.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0;
	}

	/**
	 * This method is a sloppy, forgiving method doing KEY+[DBL] or KEY-[DBL] value searches 
	 * in a string.  Returns the double value of the given key.  If the key is not found, it will
	 * return 0.  The key is case insensitive, and start-partial.  For
	 * example, a key of NAME will match NAMEY or NAME12.
	 * No assumptions are made about the given text.  It could have other garbage data of
	 * any format around it.  For example, if BOB is the key, then a text string like:
	 * 'joe larry bibob+5.2 moe="uiuiui bob-2.1 lou", bob+3.9' will still return -2.1
	 * If the key is found, but followed by a =, 0.0 is returned.
	 * @param text the string to search
	 * @param key the key to search for, case insensitive
	 * @return the value
	 */
	public final static double getParmDoublePlus(String text, final String key)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
				{
					if(text.charAt(x)=='=')
						return 0.0;
					x++;
				}
				if(x<text.length())
				{
					final char pm=text.charAt(x);
					while((x<text.length())
					&&(!Character.isDigit(text.charAt(x)))
					&&(text.charAt(x)!='.'))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())
						&&((Character.isDigit(text.charAt(x)))||(text.charAt(x)=='.')))
							x++;
						if(text.substring(0,x).indexOf('.')<0)
						{
							if(pm=='+')
								return CMath.s_int(text.substring(0,x));
							return (-CMath.s_int(text.substring(0,x)));
						}
						if(pm=='+')
							return CMath.s_double(text.substring(0,x));
						return -CMath.s_double(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0.0;
	}

	/**
	 * This method is a sloppy, forgiving method doing KEY=VALUE value searches in a string.
	 * Returns the value of the given key when the parameter is formatted in the given text
	 * in the format [KEY]=[VALUE].  If the key is not found, it will return the given 
	 * defaultVal.  The key is case insensitive, and start-partial.  For example, a key of 
	 * NAME will match NAMEY or NAME12.
	 * No assumptions are made about the given text.  It could have other garbage data of
	 * any format around it.  For example, if BOB is the key, then a text string like:
	 * 'joe larry bibob=1.5 moe="uiuiui bob=2.1 lou", bob=3.7' will still return 2.1.
	 * If the key is found, but followed by a + or -, the default value is always returned.
	 * The value ends when either an end quote is encountered, or a whitespace, semicolon, or
	 * comma.
	 * @param text the string to search
	 * @param key the key to search for, case insensitive
	 * @param defaultValue the value to return if the key is not found
	 * @return the value
	 */
	public final static double getParmDouble(String text, final String key, final double defaultValue)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
					x++;
				if(x<text.length())
				{
					while((x<text.length())
					&&(!Character.isDigit(text.charAt(x)))
					&&(text.charAt(x)!='.'))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())
						&&((Character.isDigit(text.charAt(x)))||(text.charAt(x)=='.')))
							x++;
						if(text.substring(0,x).indexOf('.')<0)
							return CMath.s_long(text.substring(0,x));
						return CMath.s_double(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}

	/**
	 * This method is a sloppy, forgiving method doing KEY=VALUE value searches in a string.
	 * Returns the value of the given key when the parameter is formatted in the given text
	 * in the format [KEY]=[VALUE].  If the key is not found, it will return the given 
	 * defaultVal.  The key is case insensitive, and start-partial.  For example, a key of 
	 * NAME will match NAMEY or NAME12.
	 * No assumptions are made about the given text.  It could have other garbage data of
	 * any format around it.  For example, if BOB is the key, then a text string like:
	 * 'joe larry bibob=1 moe="uiuiui bob=2 lou", bob=3' will still return 2.
	 * If the key is found, but followed by a + or -, the default value is always returned.
	 * The value ends when either an end quote is encountered, or a whitespace, semicolon, or
	 * comma.
	 * @param text the string to search
	 * @param key the key to search for, case insensitive
	 * @param defaultValue the value to return if the key is not found
	 * @return the value
	 */
	public final static int getParmInt(String text, final String key, final int defaultValue)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='=')&&(!Character.isDigit(text.charAt(x))))
				{
					if((text.charAt(x)=='+')||(text.charAt(x)=='-'))
						return defaultValue;
					x++;
				}
				if((x<text.length())&&(text.charAt(x)=='='))
				{
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						return CMath.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}

	/**
	 * This method is a sloppy, forgiving method doing KEY=VALUE value searches in a string.
	 * Returns the boolean value of the given key when the parameter is formatted in the given text
	 * in the format [KEY]=[VALUE].  If the key is not found, it will return the given 
	 * defaultVal.  The key is case insensitive, and start-partial.  For example, a key of 
	 * NAME will match NAMEY or NAME12.
	 * No assumptions are made about the given text.  It could have other garbage data of
	 * any format around it.  For example, if BOB is the key, then a text string like:
	 * 'joe larry bibob=False moe="uiuiui bob=True lou", bob=False' will still return True.
	 * If the key is found, but followed by a + or -, the default value is always returned.
	 * The value ends when either an end quote is encountered, or a whitespace, semicolon, or
	 * comma.
	 * @param text the string to search
	 * @param key the key to search for, case insensitive
	 * @param defaultValue the value to return if the key is not found
	 * @return the value
	 */
	public final static boolean getParmBool(String text, final String key, final boolean defaultValue)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		String s;
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
					x++;
				if((x<text.length())&&(text.charAt(x)=='='))
				{
					s=text.substring(x+1).trim();
					if(Character.toUpperCase(s.charAt(0))=='T') 
						return true;
					if(Character.toUpperCase(s.charAt(0))=='F') 
						return false;
				}
			}
			x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}

	/**
	 * Converts the given object list to a string array by calling
	 * "toString()" on all the objects
	 * @param V the list to turn into a string array
	 * @return the string array
	 */
	public final static String[] toStringArray(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			final String[] s=new String[0];
			return s;
		}
		final String[] s=new String[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=V.get(v).toString();
		return s;
	}

	/**
	 * Copies the objects in the given list into the given string set
	 * by calling toString() on all the object
	 * @param V the list
	 * @param S the set
	 * @return the set, again
	 */
	public final static Set<String> toStringSet(final List<?> V, final Set<String> S)
	{
		if(S==null)
			return toStringSet(V,new HashSet<String>());
		if((V==null)||(V.size()==0))
		{
			return S;
		}
		for(int v=0;v<V.size();v++)
			S.add(V.get(v).toString());
		return S;
	}

	/**
	 * Converts the given object array to a string array by calling
	 * "toString()" on all the objects
	 * @param O the objects to turn into a string array
	 * @return the string array
	 */
	public final static String[] toStringArray(final Object[] O)
	{
		if(O==null) 
			return new String[0];
		final String[] s=new String[O.length];
		for(int o=0;o<O.length;o++)
			s[o]=(O[o]!=null)?O[o].toString():"";
		return s;
	}

	/**
	 * Converts the given object list to a long array by calling
	 * "toString()" on all the objects and then converting those to longs.
	 * @param V the list to turn into a long array
	 * @return the long array
	 */
	public final static long[] toLongArray(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			final long[] s=new long[0];
			return s;
		}
		final long[] s=new long[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=CMath.s_long(V.get(v).toString());
		return s;
	}

	/**
	 * Converts the given object list to a double array by calling
	 * "toString()" on all the objects and then converting those to doubles.
	 * @param V the list to turn into a double array
	 * @return the double array
	 */
	public final static double[] toDoubleArray(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			final double[] s=new double[0];
			return s;
		}
		final double[] s=new double[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=CMath.s_double(V.get(v).toString());
		return s;
	}

	/**
	 * Converts the given object list to a int array by calling
	 * "toString()" on all the objects and then converting those to ints.
	 * @param V the list to turn into a int array
	 * @return the int array
	 */
	public final static int[] toIntArray(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			final int[] s=new int[0];
			return s;
		}
		final int[] s=new int[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=CMath.s_int(V.get(v).toString());
		return s;
	}

	/**
	 * Converts the given bytes to their numeric string values
	 * and returns them, semicolon delimited.
	 * @param bytes the bytes to return
	 * @return the semicolon delimited list
	 */
	public final static String toSemicolonList(final byte[] bytes)
	{
		if((bytes==null)||(bytes.length==0))
			return "";
		final StringBuilder str=new StringBuilder(Byte.toString(bytes[0]));
		for(int b=1;b<bytes.length;b++)
			str.append(";").append(Byte.toString(bytes[b]));
		return str.toString();
	}

	/**
	 * Returns the given strings combined, semicolon delimited.
	 * @param bytes the strings to return
	 * @return the semicolon delimited list
	 */
	public final static String toSemicolonList(final String[] bytes)
	{
		if((bytes==null)||(bytes.length==0))
			return "";
		final StringBuilder str=new StringBuilder(bytes[0]);
		for(int b=1;b<bytes.length;b++)
			str.append(";").append(bytes[b]);
		return str.toString();
	}

	/**
	 * Converts the given objects to their string values
	 * and returns them, semicolon delimited.
	 * @param bytes the objects as strings to return
	 * @return the semicolon delimited list
	 */
	public final static String toSemicolonList(final Object[] bytes)
	{
		if((bytes==null)||(bytes.length==0))
			return "";
		final StringBuilder str=new StringBuilder(""+bytes[0]);
		for(int b=0;b<bytes.length;b++)
			str.append(";").append(""+bytes[b]);
		return str.toString();
	}

	/**
	 * Converts the given objects to their string values
	 * and returns them, semicolon delimited.
	 * @param bytes the objects as strings to return
	 * @return the semicolon delimited list
	 */
	public final static String toSemicolonList(final Enumeration<?> bytes)
	{
		if((bytes==null)||(!bytes.hasMoreElements()))
			return "";
		final StringBuilder str=new StringBuilder(""+bytes.nextElement());
		for(;bytes.hasMoreElements();)
			str.append(";").append(""+bytes.nextElement());
		return str.toString();
	}

	/**
	 * Converts the given objects to their string values
	 * and returns them, semicolon delimited.
	 * @param bytes the objects as strings to return
	 * @return the semicolon delimited list
	 */
	public final static String toSemicolonList(final List<?> bytes)
	{
		if((bytes==null)||(bytes.size()==0)) 
			return "";
		final StringBuilder str=new StringBuilder(""+bytes.get(0));
		for(int b=1;b<bytes.size();b++)
			str.append(';').append(""+bytes.get(b));
		return str.toString();
	}

	/**
	 * Converts the given objects to their string values
	 * and returns them, semicolon delimited.  If any values contains
	 * a semicolon, the semicolon is escaped.
	 * @param list the objects as strings to return
	 * @return the semicolon delimited list
	 */
	public final static String toSafeSemicolonList(final List<?> list)
	{
		return toSafeSemicolonList(list.toArray());
	}

	/**
	 * Converts the given objects to their string values
	 * and returns them, semicolon delimited.  If any values contains
	 * a semicolon, the semicolon is escaped.
	 * @param list the objects as strings to return
	 * @return the semicolon delimited list
	 */
	public final static String toSafeSemicolonList(final Object[] list)
	{
		final StringBuilder buf1=new StringBuilder("");
		StringBuilder s=null;
		for(int l=0;l<list.length;l++)
		{
			s=new StringBuilder(list[l].toString());
			for(int i=0;i<s.length();i++)
				switch(s.charAt(i))
				{
				case '\\':
				case ';':
					s.insert(i,'\\');
					i++;
					break;
				}
			buf1.append(s.toString());
			if(l<list.length-1)
				buf1.append(';');
		}
		return buf1.toString();
	}

	/**
	 * Parses the given semicolon-delimited list of strings and returns
	 * the list as a list.  If any of the values contained escaped-semicolons,
	 * the values are unesccaped.
	 * @param list semicolon-delimited list
	 * @param ignoreNulls true to not return empty strings, false otherwise
	 * @return the list of unescaped values
	 */
	public final static List<String> parseSafeSemicolonList(final String list, final boolean ignoreNulls)
	{
		if(list==null) 
			return new Vector<String>(0);
		final StringBuilder buf1=new StringBuilder(list);
		int lastDex=0;
		final Vector<String> V=new Vector<String>();
		for(int l=0;l<buf1.length();l++)
			switch(buf1.charAt(l))
			{
			case '\\':
				buf1.delete(l,l+1);
				break;
			case ';':
				if((!ignoreNulls)||(lastDex<l))
					V.addElement(buf1.substring(lastDex,l));
				lastDex=l+1;
				break;
			}
		if((!ignoreNulls)||(lastDex<buf1.length()))
			V.addElement(buf1.substring(lastDex,buf1.length()));
		return V;
	}

	/**
	 * Returns a byte array made from the semicolon-delimited list
	 * of numeric values in the given string.
	 * @param str the semicolon-delimited decimal byte list
	 * @return the byte array.
	 */
	public final static byte[] parseSemicolonByteList(final String str)
	{
		final List<String> V=CMParms.parseSemicolons(str,true);
		if(V.size()>0)
		{
			final byte[] bytes=new byte[V.size()];
			for(int b=0;b<V.size();b++)
				bytes[b]=Byte.parseByte(V.get(b));
			return bytes;
		}
		return new byte[0];
	}

	/**
	 * Converts the objects in the given Set to a String array
	 * by calling toString() on each object.
	 * @param V the set to convert
	 * @return a string array of the set
	 */
	public final static String[] toStringArray(final Set<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			final String[] s=new String[0];
			return s;
		}
		final String[] s=new String[V.size()];
		int v=0;
		for (final Object name : V)
			s[v++]=(name).toString();
		return s;
	}

	/**
	 * Converts the environmental objects in the given Set to a String List
	 * by calling name() on each object.
	 * @param V the set to convert
	 * @return a string list of the set
	 */
	public final static List<String> toNameVector(final Set<? extends Environmental> V)
	{
		final Vector<String> s=new Vector<String>();
		if((V==null)||(V.size()==0))
			return s;
		for (final Environmental environmental : V)
			s.add(environmental.name());
		return s;
	}

	/**
	 * Converts the environmental objects in the given Enumeration to a String List
	 * by calling name() on each object.
	 * @param V the enumeration to convert
	 * @return a string list of the set
	 */
	public final static List<String> toNameVector(final Enumeration<? extends Environmental> V)
	{
		final Vector<String> s=new Vector<String>();
		if(V==null) 
			return s;
		for(;V.hasMoreElements();)
			s.add(V.nextElement().name());
		return s;
	}


	/**
	 * Converts the given object to a string.  If the object is null, returns "null".
	 * If the object is a List, Array, Iterator, or Enumeration, it converts it to a 
	 * comma-delimited list. Returns toString() on the object. 
	 * @param o the object to convert to something stringy
	 * @return the string representation of the object
	 */
	@SuppressWarnings("rawtypes")
	public final static String toString(final Object o)
	{
		if(o==null) 
			return "null";
		if(o instanceof String) 
			return (String)o;
		if(o instanceof List) 
			return toStringList((List)o);
		if(o instanceof String[]) 
			return toStringList((String[])o);
		if(o instanceof Enumeration) 
			return toStringList((Enumeration)o);
		if(o instanceof Iterator) 
			return toStringList((Iterator)o);
		return o.toString();
	}

	/** 
	 * Converts the given Array to a comma-delimited list, with
	 * spaces after each comma.
	 * @param V the array to convert to a string
	 * @return the array as a comma-delimited list
	 */
	public final static String toStringList(final String[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	/** 
	 * Converts the given Array to a comma-delimited list, with
	 * spaces after each comma by calling toString() on each object.
	 * @param V the array to convert to a string
	 * @return the array as a comma-delimited list
	 */
	public final static String toStringList(final Object[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	/** 
	 * Converts the given Iterator objects to a comma-delimited list, with
	 * spaces after each comma by calling toString() on each object.
	 * @param e the Iterator to convert objects in to a string
	 * @return the objects as a comma-delimited list
	 */
	public final static String toStringList(final Iterator<?> e)
	{
		if((e==null)||(!e.hasNext())) 
			return "";
		final Object o=e.next();
		final StringBuilder s=new StringBuilder(""+o);
		for(;e.hasNext();)
			s.append(", "+e.next());
		return s.toString();
	}

	/** 
	 * Converts the given Enumeration objects to a comma-delimited list, with
	 * spaces after each comma by calling toString() on each object.
	 * @param e the Enumeration to convert objects in to a string
	 * @return the objects as a comma-delimited list
	 */
	public final static String toStringList(final Enumeration<?> e)
	{
		if((e==null)||(!e.hasMoreElements())) 
			return "";
		final Object o=e.nextElement();
		final StringBuilder s=new StringBuilder(""+o);
		for(;e.hasMoreElements();)
			s.append(", "+e.nextElement());
		return s.toString();
	}

	/** 
	 * Converts the given Enumeration objects to a comma-delimited list, with
	 * spaces after each comma by calling name() on each object.
	 * @param e the Enumeration to convert environmentals in to a string
	 * @return the environmentals as a comma-delimited list
	 */
	public final static String toEnvironmentalStringList(final Enumeration<? extends Environmental> e)
	{
		if((e==null)||(!e.hasMoreElements())) 
			return "";
		final Environmental o=e.nextElement();
		final StringBuilder s=new StringBuilder(o.name());
		for(;e.hasMoreElements();)
			s.append(", "+e.nextElement().name());
		return s.toString();
	}

	/** 
	 * Converts the given Enumeration CMObject to a comma-delimited list, with
	 * spaces after each comma by calling ID() on each object.
	 * @param e the Enumeration to convert CMObjects in to a string
	 * @return the CMObjects as a comma-delimited list
	 */
	public final static String toCMObjectStringList(final Enumeration<? extends CMObject> e)
	{
		if((e==null)||(!e.hasMoreElements())) 
			return "";
		final CMObject o=e.nextElement();
		final StringBuilder s=new StringBuilder(o.ID());
		for(;e.hasMoreElements();)
			s.append(", "+e.nextElement().ID());
		return s.toString();
	}

	/** 
	 * Converts the given Array of CMObjects to a comma-delimited list, with
	 * spaces after each comma by calling ID() on each object.
	 * @param e the Array to convert CMObjects in to a string
	 * @return the CMObjects as a comma-delimited list
	 */
	public final static String toCMObjectStringList(final CMObject[] e)
	{
		if((e==null)||(e.length==0)) 
			return "";
		final StringBuilder s=new StringBuilder();
		for(CMObject o : e)
			s.append(", "+o.ID());
		return s.substring(2);
	}

	/** 
	 * Converts the given Iterator CMObject to a comma-delimited list, with
	 * spaces after each comma by calling ID() on each object.
	 * @param e the Iterator to convert CMObjects in to a string
	 * @return the CMObjects as a comma-delimited list
	 */
	public final static String toCMObjectStringList(final Iterator<? extends CMObject> e)
	{
		if((e==null)||(!e.hasNext())) 
			return "";
		final CMObject o=e.next();
		final StringBuilder s=new StringBuilder(o.ID());
		for(;e.hasNext();)
			s.append(", "+e.next().ID());
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string, with
	 * spaces after each comma.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toStringList(final long[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string, with
	 * spaces after each comma.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toStringList(final short[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Short.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string, with
	 * spaces after each comma.
	 * @param V the array to convert into a list string
	 * @return the boolean values in a comma-delimited list
	 */
	public final static String toStringList(final boolean[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string, with
	 * spaces after each comma.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toStringList(final byte[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Integer.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(", "+((int)V[v]));
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string, with
	 * spaces after each comma, casting the characters as numbers.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toStringList(final char[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+((long)V[0]));
		for(int v=1;v<V.length;v++)
			s.append(", "+((long)V[v]));
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string, with
	 * spaces after each comma.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toStringList(final int[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Integer.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string, with
	 * spaces after each comma.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toStringList(final double[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(", "+V[v]);
		return s.toString();
	}

	/**
	 * Converts the given List to a comma-delimited list string, with
	 * spaces after each comma, by calling toString() on each object.
	 * @param V the List to convert into a list string
	 * @return the objects in a comma-delimited list
	 */
	public final static String toStringList(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(V.get(0).toString());
		for(int v=1;v<V.size();v++)
			s.append(", "+V.get(v).toString());
		return s.toString();
	}

	/**
	 * Converts the given Set to a comma-delimited list string, with
	 * spaces after each comma, by calling toString() on each object.
	 * @param V the Set to convert into a list string
	 * @return the objects in a comma-delimited list
	 */
	public final static String toStringList(final Set<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final Iterator<?> i=V.iterator();
		final StringBuilder s=new StringBuilder(i.next().toString());
		for(;i.hasNext();)
			s.append(", "+i.next().toString());
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toTightStringList(final long[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toTightStringList(final short[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Short.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string.
	 * @param V the array to convert into a list string
	 * @return the boolean values in a comma-delimited list
	 */
	public final static String toTightStringList(final boolean[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toTightStringList(final byte[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Integer.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string, 
	 * casting the characters as numbers.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toTightStringList(final char[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+((long)V[0]));
		for(int v=1;v<V.length;v++)
			s.append(',').append((long)V[v]);
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toTightStringList(final int[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(Integer.toString(V[0]));
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	/**
	 * Converts the given array to a comma-delimited list string.
	 * @param V the array to convert into a list string
	 * @return the numbers in a comma-delimited list
	 */
	public final static String toTightStringList(final double[] V)
	{
		if((V==null)||(V.length==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(""+V[0]);
		for(int v=1;v<V.length;v++)
			s.append(',').append(V[v]);
		return s.toString();
	}

	/**
	 * Converts the given List to a comma-delimited list string,
	 * by calling toString() on each object.
	 * @param V the List to convert into a list string
	 * @return the objects in a comma-delimited list
	 */
	public final static String toTightStringList(final List<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder(V.get(0).toString());
		for(int v=1;v<V.size();v++)
			s.append(',').append(V.get(v).toString());
		return s.toString();
	}

	/**
	 * Converts the given Set to a comma-delimited list string,
	 * by calling toString() on each object.
	 * @param V the Set to convert into a list string
	 * @return the objects in a comma-delimited list
	 */
	public final static String toTightStringList(final Set<?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final Iterator<?> i=V.iterator();
		final StringBuilder s=new StringBuilder(i.next().toString());
		for(;i.hasNext();)
			s.append(',').append(i.next().toString());
		return s.toString();
	}

	/**
	 * Returns the keys and values in the given may in the form
	 * KEY=VALUE/KEY=VALUE/etc.. by calling toString() on each value.
	 * @param V the map to convert
	 * @return the key=value slash string
	 */
	public final static String toKeyValueSlashList(final Map<String,?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder("");
		for(final String KEY : V.keySet())
			s.append(KEY+"="+(V.get(KEY).toString().replaceAll("/","\\/"))+"/");
		return s.toString();
	}

	/**
	 * Appends the given two arrays together.
	 * @param front the front array
	 * @param back the back array
	 * @return the combined array
	 */
	public final static String[] appendToArray(final String[] front, final String[] back)
	{
		if(back==null) 
			return front;
		if(front==null) 
			return back;
		if(back.length==0) 
			return front;
		final String[] newa = Arrays.copyOf(front, front.length + back.length);
		for(int i=0;i<back.length;i++)
			newa[newa.length-1-i]=back[back.length-1-i];
		return newa;
	}

	
	/**
	 * Parses the given string of the form KEY=VALUE/KEY=VALUE/etc into a new
	 * String key/value map.
	 * @param s the slash-keyvalue pairs
	 * @return the map of the keys and values.
	 */
	public final static Map<String,String> parseKeyValueSlashList(final String s)
	{
		final Hashtable<String,String> h=new Hashtable<String,String>();
		final String[] allWords = s.split("(?<!\\\\)" + Pattern.quote("/"));
		for(final String word : allWords)
		{
			final int x=word.indexOf('=');
			if(x>0)
			{
				final String key=word.substring(0,x).toUpperCase().trim();
				final String value=word.substring(x+1).replaceAll("\\/","/");
				h.put(key, value);
			}
		}
		return h;
	}

	/**
	 * Returns the key/value pairs in the given map as a single string of the form
	 * KEY=VALUE KEY="VALUE" etc... by calling toString() on the objects.
	 * @param V the map of key/value pairs
	 * @return a single string list of all the key=value pairs
	 */
	public final static String toStringEqList(final Map<String,?> V)
	{
		if((V==null)||(V.size()==0))
		{
			return "";
		}
		final StringBuilder s=new StringBuilder("");
		for(final String KEY : V.keySet())
		{
			String val = V.get(KEY).toString();
			if(val.indexOf(' ')>0)
				val="\""+val+"\"";
			s.append(KEY+"="+val+" ");
		}
		return s.toString().trim();
	}


	/**
	 * Flattens the given list by returning a new list will all of the
	 * objects in it.  If the original list contained any lists, then
	 * the objects in those lists would be added, and not the list itself.
	 * @param V the list to flatten
	 * @return a new flattened list
	 */
	public final static List<Object> copyFlattenList(final List<?> V)
	{
		final Vector<Object> V2=new Vector<Object>();
		for(int v=0;v<V.size();v++)
		{
			final Object h=V.get(v);
			if(h instanceof List<?>)
				V2.addElement(copyFlattenList((List<?>)h));
			else
				V2.addElement(h);
		}
		return V2;
	}

	/**
	 * Returns the index of the given string in the given string array.
	 * The search is case sensitive.
	 * @param stringList the string array
	 * @param str the string to search for
	 * @return the index of the string in the list, or -1 if not found
	 */
	public final static int indexOf(final String[] stringList, final String str)
	{
		if(stringList==null) 
			return -1;
		if(str==null) 
			return -1;
		for(int i=0;i<stringList.length;i++)
			if(stringList[i].equals(str))
				return i;
		return -1;
	}

	/**
	 * Returns the index of the given string in the given string array.
	 * The search is case insensitive.
	 * @param stringList the string array
	 * @param str the string to search for
	 * @return the index of the string in the list, or -1 if not found
	 */
	public final static int indexOfIgnoreCase(final String[] stringList, final String str)
	{
		if(stringList==null) 
			return -1;
		if(str==null) 
			return -1;
		for(int i=0;i<stringList.length;i++)
			if(stringList[i].equalsIgnoreCase(str))
				return i;
		return -1;
	}

	/**
	 * Returns the index of the given number in the given array.
	 * @param theList the string array
	 * @param x the number to search for
	 * @return the index of the number in the list, or -1 if not found
	 */
	public final static int indexOf(final int[] theList, final int x)
	{
		if(theList==null) 
			return -1;
		for(int i=0;i<theList.length;i++)
			if(theList[i]==x)
				return i;
		return -1;
	}

	/**
	 * Returns the index of the given number in the given array.
	 * @param theList the string array
	 * @param x the number to search for
	 * @return the index of the number in the list, or -1 if not found
	 */
	public final static int indexOf(final long[] theList, final long x)
	{
		if(theList==null) 
			return -1;
		for(int i=0;i<theList.length;i++)
			if(theList[i]==x)
				return i;
		return -1;
	}

	/**
	 * Iterates through the given enumeration, returning which the number of
	 * times it must be iterated through before the given object is found
	 * @param e the enumeration of objects
	 * @param key the object to look for
	 * @return the index of the object in the enumeration
	 */
	public final static int indexOf(final Enumeration<?> e, final Object key)
	{
		if(e==null) 
			return -1;
		int index = -1;
		for(;e.hasMoreElements();)
		{
			if(e.nextElement().equals(key))
				return index;
			index++;
		}
		return -1;
	}

	/**
	 * Iterates through the given enumeration, returning which the number of
	 * times it must be iterated through before the given string is found by
	 * calling toString() on each object, and comparing their case insensitive
	 * values.
	 * @param e the enumeration of objects
	 * @param str the String to look for
	 * @return the index of the String in the enumeration
	 */
	public final static int indexOfIgnoreCase(final Enumeration<?> e, final String str)
	{
		if(e==null) 
			return -1;
		int index = -1;
		for(;e.hasMoreElements();)
		{
			if(e.nextElement().toString().equalsIgnoreCase(str))
				return index;
			index++;
		}
		return -1;
	}

	/**
	 * Returns the index of the given Object appears in the given list.
	 * @param theList the list
	 * @param obj the Object to search for
	 * @return the index of the object in the list, or -1 if not found
	 */
	public final static int indexOf(final Object[] theList, final Object obj)
	{
		if(theList==null) 
			return -1;
		if(obj==null) 
			return -1;
		for(int i=0;i<theList.length;i++)
			if(theList[i].equals(obj))
				return i;
		return -1;
	}

	/**
	 * Iterates through the given Iterator, returning which the number of
	 * times it must be iterated through before the given object is found
	 * @param i the Iterator of objects
	 * @param key the object to look for
	 * @return the index of the object in the Iterator
	 */
	public final static int indexOf(final Iterator<?> i, final Object key)
	{
		if(i==null) 
			return -1;
		int index = -1;
		for(;i.hasNext();)
		{
			if(i.next().equals(key))
				return index;
			index++;
		}
		return -1;
	}

	/**
	 * Iterates through the given Iterator, returning which the number of
	 * times it must be iterated through before the given string is found by
	 * calling toString() on each object, and comparing their case insensitive
	 * values.
	 * @param i the Iterator of objects
	 * @param str the String to look for
	 * @return the index of the String in the Iterator
	 */
	public final static int indexOfIgnoreCase(final Iterator<?> i, final String str)
	{
		if(i==null) 
			return -1;
		int index = -1;
		for(;i.hasNext();)
		{
			if(i.next().toString().equalsIgnoreCase(str))
				return index;
			index++;
		}
		return -1;
	}

	/**
	 * Returns the index of the given string in the list by calling toString() 
	 * and comparing their case insensitive values.
	 * @param theList the List of objects
	 * @param str the String to look for
	 * @return the index of the String in the List
	 */
	public final static int indexOfIgnoreCase(final List<?> theList, final String str)
	{
		if(theList==null) 
			return -1;
		if(str==null) 
			return -1;
		for(int i=0;i<theList.size();i++)
			if(theList.get(i).toString().equalsIgnoreCase(str))
				return i;
		return -1;
	}

	/**
	 * Returns whether the given string appears in the given list.
	 * It is case sensitive.
	 * @param theList the list
	 * @param str the string to search for
	 * @return true if the string is in the list, false otherwise
	 */
	public final static boolean contains(final String[] theList, final String str)
	{
		return indexOf(theList,str)>=0;
	}

	/**
	 * Returns whether the given string appears in the given enumeration of strings.
	 * It is case sensitive.
	 * @param e the enumeration
	 * @param str the string to search for
	 * @return true if the string is in the list, false otherwise
	 */
	public final static boolean contains(final Enumeration<String> e, final String str)
	{
		for(;e.hasMoreElements();)
			if(e.nextElement().equalsIgnoreCase(str))
				return true;
		return false;
	}

	/**
	 * Returns whether the given char appears in the given list.
	 * @param theList the list
	 * @param c the char to search for
	 * @return true if the char is in the list, false otherwise
	 */
	public final static boolean contains(final char[] theList, final char c)
	{
		for(final char c2 : theList)
			if(c2==c)
				return true;
		return false;
	}

	/**
	 * Returns whether the given byte appears in the given list.
	 * @param theList the list
	 * @param b the byte to search for
	 * @return true if the byte is in the list, false otherwise
	 */
	public final static boolean contains(final byte[] theList, final byte b)
	{
		for(final byte b2 : theList)
			if(b2==b)
				return true;
		return false;
	}

	/**
	 * Returns whether the given string appears in the given list.
	 * It is case insensitive.
	 * @param theList the list
	 * @param str the string to search for
	 * @return true if the string is in the list, false otherwise
	 */
	public final static boolean containsIgnoreCase(final String[] theList, final String str)
	{
		return indexOfIgnoreCase(theList,str)>=0;
	}

	/**
	 * Returns whether the given string appears in the given list.
	 * It is case insensitive.
	 * @param theList the list
	 * @param str the string to search for
	 * @return true if the string is in the list, false otherwise
	 */
	public final static boolean containsIgnoreCase(final List<?> theList, final String str)
	{
		return indexOfIgnoreCase(theList,str)>=0;
	}

	/**
	 * Returns whether the given Object appears in the given list.
	 * @param theList the list
	 * @param obj the Object to search for
	 * @return true if the Object is in the list, false otherwise
	 */
	public final static boolean contains(final Object[] theList, final Object obj)
	{
		return indexOf(theList,obj)>=0;
	}

	/**
	 * Returns whether the given int appears in the given list.
	 * @param theList the list
	 * @param x the int to search for
	 * @return true if the int is in the list, false otherwise
	 */
	public final static boolean contains(final int[] theList, final int x)
	{
		return indexOf(theList,x)>=0;
	}

	/**
	 * Returns true if the given bytebuffer, starting at the given pos, is the
	 * same as the given byte array.
	 * @param buf the bytebuffer to compare
	 * @param bytes the byte array to compare
	 * @param pos the starting position in the bytebuffer
	 * @return true if they match, false otherwise
	 */
	public final static boolean compareRange(final ByteBuffer buf, final byte[] bytes, final int pos)
	{
		for(int i=0;i<bytes.length && (i+pos)<buf.limit();i++)
			if(buf.get(pos+i)!=bytes[i])
				return false;
		return true;
	}

	/**
	 * Compares the given bytebuffer, starting at the given pos, with each byte array
	 * in the given byte array array, and returns the top level index of which byte
	 * array matches the bytebuffer at that position.  What's this good for?
	 * @param buf the bytebuffer
	 * @param bytes the array of byte arrays
	 * @param pos the starting position in the bytebuffer
	 * @return which byte array matches, or -1
	 */
	public final static int containIndex(final ByteBuffer buf, final byte[][] bytes, final int pos)
	{
		for(int x=0;x<bytes.length;x++)
			if(compareRange(buf,bytes[x],pos))
				return x;
		return -1;
	}

	/**
	 * Returns the index of the string in the string array that starts with 
	 * the given string.  The search is case sensitive
	 * @param theList the list of strings
	 * @param str the string to look for
	 * @return index of entry in the list that starts with the string, or -1
	 */
	public final static int startsWith(final String[] theList, final String str)
	{
		if(theList==null) 
			return 0;
		if(str==null) 
			return -1;
		for(int i=0;i<theList.length;i++)
			if(theList[i].startsWith(str))
				return i;
		return -1;
	}

	/**
	 * Returns the index of the string in the string array that starts with 
	 * the given string.  The search is case insensitive
	 * @param theList the list of strings
	 * @param str the string to look for
	 * @return index of entry in the list that starts with the string, or -1
	 */
	public final static int startsWithIgnoreCase(final String[] theList, final String str)
	{
		if(theList==null) 
			return 0;
		if(str==null) 
			return -1;
		for(int i=0;i<theList.length;i++)
			if(theList[i].toUpperCase().startsWith(str.toUpperCase()))
				return i;
		return -1;
	}

	/**
	 * Returns true if any string in string array starts with 
	 * the given string.  The search is case sensitive
	 * @param theList the list of strings
	 * @param str the string to look for
	 * @return true if any string in the list starts with the str, or false
	 */
	public final static boolean startsAnyWith(final String[] theList, final String str)
	{
		return startsWith(theList,str)>=0;
	}

	/**
	 * Returns true if any string in string array starts with 
	 * the given string.  The search is case insensitive
	 * @param theList the list of strings
	 * @param str the string to look for
	 * @return true if any string in the list starts with the str, or false
	 */
	public final static boolean startsAnyWithIgnoreCase(final String[] theList, final String str)
	{
		return startsWithIgnoreCase(theList,str)>=0;
	}

	/**
	 * Returns the index of the string in the string array that ends with 
	 * the given string.  The search is case sensitive
	 * @param theList the list of strings
	 * @param str the string to look for
	 * @return index of entry in the list that ends with the string, or -1
	 */
	public final static int endsWith(final String[] theList, final String str)
	{
		if(theList==null) 
			return 0;
		if(str==null) 
			return -1;
		for(int i=0;i<theList.length;i++)
			if(theList[i].endsWith(str))
				return i;
		return -1;
	}

	/**
	 * Returns the index of the string in the string array that ends with 
	 * the given string.  The search is case insensitive
	 * @param theList the list of strings
	 * @param str the string to look for
	 * @return index of entry in the list that ends with the string, or -1
	 */
	public final static int endsWithIgnoreCase(final String[] theList, final String str)
	{
		if(theList==null) 
			return 0;
		if(str==null) 
			return -1;
		for(int i=0;i<theList.length;i++)
			if(theList[i].toUpperCase().endsWith(str.toUpperCase()))
				return i;
		return -1;
	}

	/**
	 * Returns true if any string in string array ends with 
	 * the given string.  The search is case sensitive
	 * @param theList the list of strings
	 * @param str the string to look for
	 * @return true if any string in the list ends with the str, or false
	 */
	public final static boolean endsAnyWith(final String[] theList, final String str)
	{
		return endsWith(theList,str)>=0;
	}

	/**
	 * Returns true if any string in string array ends with 
	 * the given string.  The search is case insensitive
	 * @param theList the list of strings
	 * @param str the string to look for
	 * @return true if any string in the list ends with the str, or false
	 */
	public final static boolean endsAnyWithIgnoreCase(final String[] theList, final String str)
	{
		return endsWithIgnoreCase(theList,str)>=0;
	}

	/** constant value representing an undefined/unimplemented miscText/parms format.*/
	public static final String FORMAT_UNDEFINED="{UNDEFINED}";
	/** constant value representing an always empty miscText/parms format.*/
	public static final String FORMAT_EMPTY="{EMPTY}";
}
