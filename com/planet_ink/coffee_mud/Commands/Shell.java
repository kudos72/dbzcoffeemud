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
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class Shell extends StdCommand
{
	public Shell(){}

	private final String[] access=I(new String[]{"SHELL","CMFS","."});
	@Override public String[] getAccessWords(){return access;}

	protected static DVector pwds=new DVector(2);
	protected String[][] SUB_CMDS={
			{"$","DIRECTORY","LS"},
			{">","COPY","CP"},
			{".","CHANGEDIRECTORY","CD","GO"},
			{"-","DELETE","RM","RD"},
			{"\\","TYPE","CAT","TP"},
			{"+","MAKEDIRECTORY","MKDIR","MD"},
			{"*","FINDFILE","FF"},
			{"&","SEARCHTEXT","GREP","ST"},
			{"/","EDIT"},
			{"~","MOVE","MV"},
			{"?","COMPAREFILES","DIFF","CF"},
	};

	protected final static String[] badTextExtensions={
		".ZIP",".JPE",".JPG",".GIF",".CLASS",".WAV",".BMP",".JPEG",".GZ",".TGZ",".JAR"
	};

	private class cp_options
	{
		boolean recurse=false;
		boolean forceOverwrites=false;
		boolean preservePaths=false;
		public cp_options(Vector cmds)
		{
			for(int c=cmds.size()-1;c>=0;c--)
			{
				final String s=(String)cmds.get(c);
				if(s.startsWith("-"))
				{
					for(int c2=1;c2<s.length();c2++)
					switch(s.charAt(c2))
					{
					case 'r':
					case 'R':
						recurse=true;
						break;
					case 'f':
					case 'F':
						forceOverwrites=true;
						break;
					case 'p':
					case 'P':
						preservePaths=true;
						break;
					}
					cmds.remove(c);
				}
			}
		}
	}

	private java.util.List<CMFile> sortDirsUp(CMFile[] files)
	{
		final Vector<CMFile> dirs=new Vector<CMFile>();
		CMFile CF=null;
		final Vector<CMFile> finalList=new Vector<CMFile>();
		for(int v=files.length-1;v>=0;v--)
		{
			CF=files[v];
			if((CF.isDirectory())&&(CF.exists()))
			{
				int x=0;
				while(x<=dirs.size())
				{
					if(x==dirs.size())
					{
						dirs.add(CF);
						break;
					}
					else
					if(dirs.get(x).getVFSPathAndName().length()<CF.getVFSPathAndName().length())
						x++;
					else
					{
						dirs.insertElementAt(CF,x);
						break;
					}
				}
			}
			else
				finalList.add(CF);

		}
		finalList.addAll(dirs);
		return finalList;
	}

	private java.util.List<CMFile>  sortDirsDown(CMFile[] files)
	{
		final Vector<CMFile> dirs=new Vector<CMFile>();
		final HashSet<CMFile> dirsH=new HashSet<CMFile>();
		CMFile CF=null;
		for(int v=files.length-1;v>=0;v--)
		{
			CF=files[v];
			if((CF.isDirectory())&&(CF.exists()))
			{
				int x=0;
				while(x<=dirs.size())
				{
					if(x==dirs.size())
					{
						dirs.add(CF);
						dirsH.add(CF);
						break;
					}
					else
					if(dirs.get(x).getVFSPathAndName().length()>CF.getVFSPathAndName().length())
						x++;
					else
					{
						dirs.insertElementAt(CF,x);
						dirsH.add(CF);
						break;
					}
				}
			}
		}
		for(final CMFile F : files)
			if(!dirsH.contains(F))
				dirs.add(F);
		return dirs;
	}

	public static final String incorporateBaseDir(String currentPath, String filename)
	{
		String starter="";
		if(filename.startsWith("::")||filename.startsWith("//"))
		{
			starter=filename.substring(0,2);
			filename=filename.substring(2);
		}
		if(!filename.startsWith("/"))
		{
			boolean didSomething=true;
			while(didSomething)
			{
				didSomething=false;
				if(filename.startsWith(".."))
				{
					filename=filename.substring(2);
					final int x=currentPath.lastIndexOf('/');
					if(x>=0)
						currentPath=currentPath.substring(0,x);
					else
						currentPath="";
					didSomething=true;
				}
				if((filename.startsWith("."))&&(!(filename.startsWith(".."))))
				{
					filename=filename.substring(1);
					didSomething=true;
				}
				while(filename.startsWith("/"))
					filename=filename.substring(1);
			}
			if((currentPath.length()>0)&&(filename.length()>0))
				filename=currentPath+"/"+filename;
			else
			if(currentPath.length()>0)
				filename=currentPath;
		}
		return starter+filename;
	}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String pwd=(pwds.contains(mob))?(String)pwds.get(pwds.indexOf(mob),2):"";
		commands.remove(0);
		if(commands.size()==0)
		{
			mob.tell(L("Current directory: /@x1",pwd));
			return false;
		}
		int cmd=-1;
		String first=((String)commands.get(0)).toUpperCase();
		final StringBuffer allcmds=new StringBuffer("");
		for(int i=0;i<SUB_CMDS.length;i++)
		{
			final String shortcut=SUB_CMDS[i][0];
			if(first.startsWith(shortcut))
			{
				first=first.substring(shortcut.length()).trim();
				if(first.length()>0)
				{
					if(commands.size()>1)
						commands.setElementAt(first,1);
					else
						commands.add(first);
				}
				cmd=i;
				break;
			}
			for(int x=1;x<SUB_CMDS[i].length;x++)
			{
				if(SUB_CMDS[i][x].startsWith(first.toUpperCase()))
				{
					cmd=i;
					break;
				}
				if(x==1)
				{
					allcmds.append(SUB_CMDS[i][x]+" (");
					for(int x2=0;x2<SUB_CMDS[i].length;x2++)
						if(x2!=x)
						{
							allcmds.append(SUB_CMDS[i][x2]);
							if(x2<SUB_CMDS[i].length-1)
								allcmds.append("/");
						}
					allcmds.append("), ");
				}
			}
			if(cmd>=0)
				break;
		}
		switch(cmd)
		{
		case 0: // directory
		{
			final cp_options opts=new cp_options(commands);
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob,opts.recurse,true);
			if(dirs==null)
			{
				mob.tell(L("^xError: invalid directory!^N"));
				return false;
			}
			final StringBuffer msg=new StringBuffer("\n\r^y .\n\r^y ..\n\r");
			for (final CMFile dir : dirs)
			{
				final CMFile entry=dir;
				if(entry.isDirectory())
				{
					if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
						msg.append(" ");
					else
					if((entry.isLocalFile()&&(entry.canVFSEquiv()))
					||((entry.isVFSFile())&&(entry.canLocalEquiv())))
						msg.append("^R+");
					else
						msg.append("^r-");
					msg.append("^y"+CMStrings.padRight(entry.getName(),25));
					msg.append("^w"+CMStrings.padRight(CMLib.time().date2String(entry.lastModified()),20));
					msg.append("^w"+CMStrings.padRight(entry.author(),20));
					msg.append("\n\r");
				}
			}
			for (final CMFile dir : dirs)
			{
				final CMFile entry=dir;
				if(!entry.isDirectory())
				{
					if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
						msg.append(" ");
					else
					if((entry.isLocalFile()&&(entry.canVFSEquiv()))
					||((entry.isVFSFile())&&(entry.canLocalEquiv())))
						msg.append("^R+");
					else
						msg.append("^r-");
					msg.append("^w"+CMStrings.padRight(entry.getName(),25));
					msg.append("^w"+CMStrings.padRight(CMLib.time().date2String(entry.lastModified()),20));
					msg.append("^w"+CMStrings.padRight(entry.author(),20));
					msg.append("\n\r");
				}
			}
			if(mob.session()!=null)
				mob.session().colorOnlyPrintln(msg.toString());
			break;
		}
		case 1: // copy
		{
			final cp_options opts=new cp_options(commands);
			if(commands.size()==2)
				commands.add(".");
			if(commands.size()<3)
			{
				mob.tell(L("^xError  : source and destination must be specified!^N"));
				mob.tell(L("^xOptions: -r = recurse into directories.^N"));
				mob.tell(L("^x       : -p = preserve paths.^N"));
				return false;
			}
			final String source=(String)commands.get(1);
			String target=CMParms.combine(commands,2);
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,source),mob,opts.recurse,true);
			if(dirs==null)
			{
				mob.tell(L("^xError: invalid source!^N"));
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no source files matched^N"));
				return false;
			}
			if((dirs.length==1)&&(!target.trim().startsWith("::")&&(!target.trim().startsWith("//"))))
				target=(dirs[0].isLocalFile())?"//"+target.trim():"::"+target.trim();
			final CMFile DD=new CMFile(incorporateBaseDir(pwd,target),mob);
			final java.util.List<CMFile> ddirs=sortDirsUp(dirs);
			for(final CMFile SF: ddirs)
			{
				if((SF==null)||(!SF.exists())){ mob.tell(L("^xError: source @x1 does not exist!^N",desc(SF))); return false;}
				if(!SF.canRead()){mob.tell(L("^xError: access denied to source @x1!^N",desc(SF))); return false;}
				if((SF.isDirectory())&&(!opts.preservePaths))
				{
					if(dirs.length==1)
					{
						mob.tell(L("^xError: source can not be a directory!^N"));
						return false;
					}
					continue;
				}
				CMFile DF=DD;
				target=DD.getVFSPathAndName();
				if(DD.isDirectory())
				{
					String name=SF.getName();
					if((opts.recurse)&&(opts.preservePaths))
					{
						final String srcPath=SF.getVFSPathAndName();
						if(srcPath.startsWith(pwd+"/"))
							name=srcPath.substring(pwd.length()+1);
						else
							name=srcPath;
					}
					if(target.length()>0)
						target=target+"/"+name;
					else
						target=name;
					if(DD.demandedVFS())
						target="::"+target;
					else
					if(DD.demandedLocal())
						target="//"+target;
					else
						target=(SF.isLocalFile()&&DD.canLocalEquiv())?"//"+target:"::"+target;
					DF=new CMFile(target,mob);
				}
				else
				if(dirs.length>1)
				{
					mob.tell(L("^xError: destination must be a directory!^N"));
					return false;
				}
				if(DF.mustOverwrite()){ mob.tell(L("^xError: destination @x1 already exists!^N",desc(DF))); return false;}
				if(!DF.canWrite()){ mob.tell(L("^xError: access denied to destination @x1!^N",desc(DF))); return false;}
				if((SF.isDirectory())&&(opts.recurse))
				{
					if(!DF.mkdir())
						mob.tell(L("^xWarning: failed to mkdir @x1 ^N",desc(DF)));
					else
						mob.tell(L("@x1 copied to @x2",desc(SF),desc(DF)));
				}
				else
				{
					final byte[] O=SF.raw();
					if(O.length==0){ mob.tell(L("^xWarning: @x1 file had no data^N",desc(SF)));}
					if(!DF.saveRaw(O))
						mob.tell(L("^xWarning: write failed to @x1 ^N",desc(DF)));
					else
						mob.tell(L("@x1 copied to @x2",desc(SF),desc(DF)));
				}
			}
			break;
		}
		case 2: // cd
		{
			final CMFile newDir=new CMFile(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob);
			final String changeTo=newDir.getVFSPathAndName();
			if(!newDir.exists())
			{
				mob.tell(L("^xError: Directory '@x1' does not exist.^N",CMParms.combine(commands,1)));
				return false;
			}
			if((!newDir.canRead())||(!newDir.isDirectory()))
			{
				mob.tell(L("^xError: You are not authorized enter that directory.^N"));
				return false;
			}
			pwd=changeTo;
			mob.tell(L("Directory is now: /@x1",pwd));
			pwds.removeElement(mob);
			pwds.add(mob,pwd);
			return true;
		}
		case 3: // delete
		{
			final cp_options opts=new cp_options(commands);
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob,opts.recurse,false);
			if(dirs==null)
			{
				mob.tell(L("^xError: invalid filename!^N"));
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no files matched^N"));
				return false;
			}
			final java.util.List<CMFile> ddirs=sortDirsDown(dirs);
			for(int d=0;d<ddirs.size();d++)
			{
				final CMFile CF=ddirs.get(d);
				if((CF==null)||(!CF.exists()))
				{
					mob.tell(L("^xError: @x1 does not exist!^N",desc(CF)));
					return false;
				}
				if(!CF.canWrite())
				{
					mob.tell(L("^xError: access denied to @x1!^N",desc(CF)));
					return false;
				}
				if((!CF.delete())&&(CF.exists()))
				{
					mob.tell(L("^xError: delete of @x1 failed.^N",desc(CF)));
					return false;
				}
				mob.tell(L("@x1 deleted.",desc(CF)));
			}
			break;
		}
		case 4: // type
		{
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob,false,false);
			if(dirs==null)
			{
				mob.tell(L("^xError: invalid filename!^N"));
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no files matched^N"));
				return false;
			}
			for (final CMFile dir : dirs)
			{
				final CMFile CF=dir;
				if((CF==null)||(!CF.exists()))
				{
					mob.tell(L("^xError: file does not exist!^N"));
					return false;
				}
				if(!CF.canRead())
				{
					mob.tell(L("^xError: access denied!^N"));
					return false;
				}
				if(mob.session()!=null)
				{
					mob.session().colorOnlyPrintln(L("\n\r^xFile /@x1^.^N\n\r",CF.getVFSPathAndName()));
					mob.session().rawPrint(CF.text().toString());
				}
			}
			break;
		}
		case 5: // makedirectory
		{
			final CMFile CF=new CMFile(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob);
			if(CF.exists())
			{
				mob.tell(L("^xError: file already exists!^N"));
				return false;
			}
			if(!CF.canWrite())
			{
				mob.tell(L("^xError: access denied!^N"));
				return false;
			}
			if(!CF.mkdir())
			{
				mob.tell(L("^xError: makedirectory failed.^N"));
				return false;
			}
			mob.tell(L("Directory '/@x1' created.",CF.getAbsolutePath()));
			break;
		}
		case 6: // findfiles
		{
			String substring=CMParms.combine(commands,1).trim();
			if(substring.length()==0)
				substring="*";
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,substring),mob,true,true);
			final StringBuffer msg=new StringBuffer("");
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no files matched^N"));
				return false;
			}
			for (final CMFile dir : dirs)
			{
				final CMFile entry=dir;
				if(!entry.isDirectory())
				{
					if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
						msg.append(" ");
					else
					if((entry.isLocalFile()&&(entry.canVFSEquiv()))
					||((entry.isVFSFile())&&(entry.canLocalEquiv())))
						msg.append("^R+");
					else
						msg.append("^r-");
					msg.append("^w"+entry.getVFSPathAndName());
					msg.append("\n\r");
				}
			}
			if(mob.session()!=null)
				mob.session().colorOnlyPrintln(msg.toString());
			return false;
		}
		case 7: // searchtext
		{
			String substring=CMParms.combine(commands,1).trim();
			if(substring.length()==0)
			{
				mob.tell(L("^xError: you must specify a search string^N"));
				return false;
			}
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,"*"),mob,true,true);
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no files found!^N"));
				return false;
			}
			mob.session().print(L("\n\rSearching..."));
			substring=substring.toUpperCase();
			final Vector dirs2=new Vector();
			for (final CMFile dir : dirs)
			{
				final CMFile entry=dir;
				if(!entry.isDirectory())
				{
					boolean proceed=true;
					for (final String badTextExtension : badTextExtensions)
						if(entry.getName().toUpperCase().endsWith(badTextExtension))
						{ proceed=false; break;}
					if(proceed)
					{
						final StringBuffer text=entry.textUnformatted();
						if(text.toString().toUpperCase().indexOf(substring)>=0)
							dirs2.add(entry);
					}
				}
			}
			if(dirs2.size()==0)
			{
				mob.tell(L("\n\r^xError: no files matched^N"));
				return false;
			}
			final StringBuffer msg=new StringBuffer("\n\r");
			for(int d=0;d<dirs2.size();d++)
			{
				final CMFile entry=(CMFile)dirs2.get(d);
				if(entry.isLocalFile()&&(!entry.canVFSEquiv()))
					msg.append(" ");
				else
				if((entry.isLocalFile()&&(entry.canVFSEquiv()))
				||((entry.isVFSFile())&&(entry.canLocalEquiv())))
					msg.append("^R+");
				else
					msg.append("^r-");
				msg.append("^w"+entry.getVFSPathAndName());
				msg.append("\n\r");
			}
			if(mob.session()!=null)
				mob.session().colorOnlyPrintln(msg.toString());
			return false;
		}
		case 8: // edit
		{
			final CMFile file=new CMFile(incorporateBaseDir(pwd,CMParms.combine(commands,1)),mob);
			if((!file.canWrite())
			||(file.isDirectory()))
			{
				mob.tell(L("^xError: You are not authorized to create/modify that file.^N"));
				return false;
			}
			StringBuffer buf=file.textUnformatted();
			final String CR=Resources.getLineMarker(buf);
			final List<String> vbuf=Resources.getFileLineVector(buf);
			buf=null;
			mob.tell(L("@x1 has been loaded.\n\r\n\r",desc(file)));
			final String messageTitle="File: "+file.getVFSPathAndName();
			final JournalsLibrary.MsgMkrResolution resolution=CMLib.journals().makeMessage(mob, messageTitle, vbuf, false);
			if(resolution==JournalsLibrary.MsgMkrResolution.SAVEFILE)
			{
				final StringBuffer text=new StringBuffer("");
				for(int i=0;i<vbuf.size();i++)
					text.append((vbuf.get(i))+CR);
				if(file.saveText(text))
				{
					for(final Iterator<String> i=Resources.findResourceKeys(file.getName());i.hasNext();)
						Resources.removeResource(i.next());
					mob.tell(L("File saved."));
				}
				else
					mob.tell(L("^XError: could not save the file!^N^."));
				return true;
			}
			if(resolution==JournalsLibrary.MsgMkrResolution.CANCELFILE)
				return true;
			return false;
		}
		case 9: // move
		{
			final cp_options opts=new cp_options(commands);
			if(commands.size()==2)
				commands.add(".");
			if(commands.size()<3)
			{
				mob.tell(L("^xError  : source and destination must be specified!^N"));
				mob.tell(L("^xOptions: -r = recurse into directories.^N"));
				mob.tell(L("^x       : -f = force overwrites.^N"));
				mob.tell(L("^x       : -p = preserve paths.^N"));
				return false;
			}
			final String source=(String)commands.get(1);
			String target=CMParms.combine(commands,2);
			final CMFile[] dirs=CMFile.getFileList(incorporateBaseDir(pwd,source),mob,opts.recurse,true);
			if(dirs==null)
			{
				mob.tell(L("^xError: invalid source!^N"));
				return false;
			}
			if(dirs.length==0)
			{
				mob.tell(L("^xError: no source files matched^N"));
				return false;
			}
			if((dirs.length==1)&&(!target.trim().startsWith("::")&&(!target.trim().startsWith("//"))))
				target=(dirs[0].isLocalFile())?"//"+target.trim():"::"+target.trim();
			final CMFile DD=new CMFile(incorporateBaseDir(pwd,target),mob);
			final java.util.List<CMFile> ddirs=sortDirsUp(dirs);
			java.util.List<CMFile> dirsLater=new Vector<CMFile>();
			for(int d=0;d<ddirs.size();d++)
			{
				final CMFile SF=ddirs.get(d);
				if((SF==null)||(!SF.exists())){ mob.tell(L("^xError: source @x1 does not exist!^N",desc(SF))); return false;}
				if(!SF.canRead()){mob.tell(L("^xError: access denied to source @x1!^N",desc(SF))); return false;}
				if((SF.isDirectory())&&(!opts.preservePaths))
				{
					if(dirs.length==1)
					{
						mob.tell(L("^xError: source can not be a directory!^N"));
						return false;
					}
					continue;
				}
				CMFile DF=DD;
				target=DD.getVFSPathAndName();
				if(DD.isDirectory())
				{
					String name=SF.getName();
					if((opts.recurse)&&(opts.preservePaths))
					{
						final String srcPath=SF.getVFSPathAndName();
						if(srcPath.startsWith(pwd+"/"))
							name=srcPath.substring(pwd.length()+1);
						else
							name=srcPath;
					}
					if(target.length()>0)
						target=target+"/"+name;
					else
						target=name;
					if(DD.demandedVFS())
						target="::"+target;
					else
					if(DD.demandedLocal())
						target="//"+target;
					else
						target=(SF.isLocalFile()&&DD.canLocalEquiv())?"//"+target:"::"+target;
					DF=new CMFile(target,mob);
				}
				else
				if(dirs.length>1)
				{
					mob.tell(L("^xError: destination must be a directory!^N"));
					return false;
				}
				if(DF.mustOverwrite() && (!opts.forceOverwrites)){ mob.tell(L("^xError: destination @x1 already exists!^N",desc(DF))); return false;}
				if(!DF.canWrite()){ mob.tell(L("^xError: access denied to destination @x1!^N",desc(DF))); return false;}
				if((SF.isDirectory())&&(opts.recurse))
				{
					if((!DF.mustOverwrite())&&(!DF.mkdir()))
						mob.tell(L("^xWarning: failed to mkdir @x1 ^N",desc(DF)));
					else
						mob.tell(L("@x1 copied to @x2",desc(SF),desc(DF)));
					dirsLater.add(SF);
				}
				else
				{
					final byte[] O=SF.raw();
					if(O.length==0){ mob.tell(L("^xWarning: @x1 file had no data^N",desc(SF)));}
					if(!DF.saveRaw(O))
						mob.tell(L("^xWarning: write failed to @x1 ^N",desc(DF)));
					else
						mob.tell(L("@x1 moved to @x2",desc(SF),desc(DF)));
					if((!SF.delete())&&(SF.exists()))
					{
						mob.tell(L("^xError: Unable to delete file @x1",desc(SF)));
						break;
					}
				}
			}
			dirsLater=sortDirsDown(dirsLater.toArray(new CMFile[0]));
			for(int d=0;d<dirsLater.size();d++)
			{
				final CMFile CF=dirsLater.get(d);
				if((!CF.delete())&&(CF.exists()))
				{
					mob.tell(L("^xError: Unable to delete dir @x1",desc(CF)));
					break;
				}
			}
			break;
		}
		case 10: // compare files
		{
			if(commands.size()==2)
				commands.add(".");
			if(commands.size()<3)
			{
				mob.tell(L("^xError  : first and second files be specified!^N"));
				return false;
			}
			final String firstFilename=(String)commands.get(1);
			String secondFilename=CMParms.combine(commands,2);
			final CMFile file1=new CMFile(incorporateBaseDir(pwd,firstFilename),mob);
			if((!file1.canRead())
			||(file1.isDirectory()))
			{
				mob.tell(L("^xError: You are not authorized to read the first file.^N"));
				return false;
			}
			String prefix="";
			if(secondFilename.equals("."))
			{
				if(file1.isVFSFile())
				{
					prefix="//";
					secondFilename=CMFile.vfsifyFilename(firstFilename);
				}
				else
				if(file1.isLocalFile())
				{
					prefix="::";
					secondFilename=CMFile.vfsifyFilename(firstFilename);
				}
				else
				{
					mob.tell(L("^xError  : first and second files be specified!^N"));
					return false;
				}
			}
			final CMFile file2=new CMFile(prefix+incorporateBaseDir(pwd,secondFilename),mob);
			if((!file2.canRead())||(file2.isDirectory()))
			{
				mob.tell(L("^xError: You are not authorized to read the second file.^N"));
				return false;
			}
			final StringBuilder text1=new StringBuilder("");
			for(final String s : Resources.getFileLineVector(file1.text()))
				if(s.trim().length()>0)
					text1.append(s.trim()).append("\n\r");
			final StringBuilder text2=new StringBuilder("");
			for(final String s : Resources.getFileLineVector(file2.text()))
				if(s.trim().length()>0)
					text2.append(s.trim()).append("\n\r");
			final LinkedList<CMStrings.Diff> diffs=CMStrings.diff_main(text1.toString(), text2.toString(), false);
			boolean flipFlop=false;
			for(final CMStrings.Diff d : diffs)
			{
				final StringBuilder str=new StringBuilder("\n\r^H"+d.operation.toString()+": ");
				str.append(flipFlop?"^N":"^w");
				flipFlop=!flipFlop;
				str.append(d.text);
				mob.session().colorOnlyPrintln(str.toString());
			}
			mob.tell(L("^HDONE."));
			return false;
		}
		default:
			mob.tell(L("'@x1' is an unknown command.  Valid commands are: @x2and SHELL alone to check your current directory.",first,allcmds.toString()));
			return false;
		}
		return true;
	}

	public String desc(CMFile CF){ return (CF.isLocalFile()?"Local file ":"VFS file ")+"'/"+CF.getVFSPathAndName()+"'";}

	@Override public boolean canBeOrdered(){return false;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.hasAccessibleDir(mob,null);}

}
