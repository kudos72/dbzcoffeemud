package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.Auctioneer.AuctionData;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
public class CoffeeShops extends StdLibrary implements ShoppingLibrary
{
	@Override public String ID(){return "CoffeeShops";}
	@Override
	public ShopKeeper getShopKeeper(Environmental E)
	{
		if(E==null)
			return null;
		if(E instanceof ShopKeeper)
			return (ShopKeeper)E;
		if(E instanceof Physical)
		{
			final Physical P=(Physical)E;
			for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A instanceof ShopKeeper)
					return (ShopKeeper)A;
			}
		}
		if(E instanceof MOB)
		{
			Item I=null;
			final MOB mob=(MOB)E;
			for(int i=0;i<mob.numItems();i++)
			{
				I=mob.getItem(i);
				if(I instanceof ShopKeeper)
					return (ShopKeeper)I;
				if(I!=null)
					for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if(A instanceof ShopKeeper)
							return (ShopKeeper)A;
					}
			}
		}
		return null;
	}

	@Override
	public List<Environmental> getAllShopkeepers(Room here, MOB notMOB)
	{
		final Vector<Environmental> V=new Vector<Environmental>();
		if(here!=null)
		{
			if(getShopKeeper(here)!=null)
				V.addElement(here);
			final Area A=here.getArea();
			if(getShopKeeper(A)!=null)
				V.addElement(A);
			final List<Area> V2=A.getParentsRecurse();
			for(int v2=0;v2<V2.size();v2++)
				if(getShopKeeper(V2.get(v2))!=null)
					V.addElement(V2.get(v2));

			for(int i=0;i<here.numInhabitants();i++)
			{
				final MOB thisMOB=here.fetchInhabitant(i);
				if((thisMOB!=null)
				&&(thisMOB!=notMOB)
				&&(getShopKeeper(thisMOB)!=null)
				&&((notMOB==null)||(CMLib.flags().canBeSeenBy(thisMOB,notMOB))))
					V.addElement(thisMOB);
			}
			for(int i=0;i<here.numItems();i++)
			{
				final Item thisItem=here.getItem(i);
				if((thisItem!=null)
				&&(thisItem!=notMOB)
				&&(getShopKeeper(thisItem)!=null)
				&&(!CMLib.flags().isGettable(thisItem))
				&&(thisItem.container()==null)
				&&((notMOB==null)||(CMLib.flags().canBeSeenBy(thisItem,notMOB))))
					V.addElement(thisItem);
			}
		}
		return V;
	}

	protected String getSellableElectronicsName(MOB viewerM, Electronics E)
	{
		String baseName=E.name(viewerM);
		final String[] marks=CMProps.getListFileStringList(CMProps.ListFile.TECH_LEVEL_NAMES);
		if(baseName.indexOf(E.getFinalManufacturer().name())<0)
			baseName+= "("+E.getFinalManufacturer().name()+")";
		if(marks.length>0)
			baseName+=" "+marks[E.techLevel()%marks.length];
		return baseName;
	}
	
	@Override
	public String getViewDescription(MOB viewerM, Environmental E)
	{
		final StringBuffer str=new StringBuffer("");
		if(E==null)
			return str.toString();
		str.append(L("Interested in @x1?",E.name()));
		str.append(L(" Here is some information for you:"));
		if(E instanceof Physical)
			str.append("\n\rLevel      : "+((Physical)E).phyStats().level());
		if(E instanceof Item)
		{
			final Item I=(Item)E;
			str.append(L("\n\rMaterial   : @x1",L(CMStrings.capitalizeAndLower(RawMaterial.CODES.NAME(I.material()).toLowerCase()))));
			str.append(L("\n\rWeight     : @x1 pounds",""+I.phyStats().weight()));
			if(I instanceof Electronics)
			{
				str.append(L("\n\rMake       : @x1",""+((Electronics)I).getFinalManufacturer().name()));
				str.append(L("\n\rType       : @x1",""+((Electronics)I).getTechType().getDisplayName()));
			}
			if(I instanceof Technical)
			{
				str.append(L("\n\rModel Num. : @x1",""+((Technical)I).techLevel()));
			}
			if(I instanceof BoardableShip)
			{
				final Area A=((BoardableShip)I).getShipArea();
				if(A!=null)
				{
					str.append(L("\n\rRooms      : @x1",""+A.numberOfProperIDedRooms()));
				}
				final List<String> miscItems=new ArrayList<String>();
				for(final Enumeration<Room> r= A.getProperMap(); r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R==null)
						continue;
					for(final Enumeration<Item> i = R.items();i.hasMoreElements();)
					{
						final Item I2=i.nextElement();
						if(I2.displayText().length()>0)
						{
							if(I2 instanceof ShipComponent)
								str.append(L("\n\r"+CMStrings.padRight(((ShipComponent)I2).getTechType().getShortDisplayName(),11)+": @x1",getSellableElectronicsName(viewerM,(Electronics)I2)));
							else
								miscItems.add(I2.name(viewerM));
						}
					}
				}
				if(miscItems.size()>0)
				{
					str.append(L("\n\rMisc Items : "));
					str.append(CMParms.toStringList(miscItems));
				}
			}
			else
			if(I instanceof Weapon)
			{
				str.append(L("\n\rWeap. Type : @x1",L(CMStrings.capitalizeAndLower(Weapon.TYPE_DESCS[((Weapon)I).weaponType()]))));
				str.append(L("\n\rWeap. Class: @x1",L(CMStrings.capitalizeAndLower(Weapon.CLASS_DESCS[((Weapon)I).weaponClassification()]))));
			}
			else
			if(I instanceof Armor)
			{
				str.append(L("\n\rWear Info  : Worn on "));
				final Wearable.CODES codes = Wearable.CODES.instance();
				final List<String> locs = new ArrayList<String>();
				for(final long wornCode : codes.all())
				{
					if(wornCode != Wearable.IN_INVENTORY)
					{
						if(codes.name(wornCode).length()>0)
						{
							if(((I.rawProperLocationBitmap()&wornCode)==wornCode))
							{
								locs.add(CMStrings.capitalizeAndLower(codes.name(wornCode)));
							}
						}
					}
				}
				str.append(CMParms.combineWith(locs, L(I.rawLogicalAnd() ? " and " : " or ")));
				if(I.phyStats().height()>0)
				{
					Armor.SizeDeviation deviation=((Armor) I).getSizingDeviation(viewerM);
					if(deviation != Armor.SizeDeviation.FITS)
						str.append(L("\n\rSize       : ")+I.phyStats().height()+" ("+L(deviation.toString().toLowerCase().replace('_',' ')+")"));
				}
			}
		}
		str.append(L("\n\rDescription: @x1",E.description()));
		return str.toString();
	}

	protected Ability getTrainableAbility(MOB teacher, Ability A)
	{
		if((teacher==null)||(A==null))
			return A;
		Ability teachableA=teacher.fetchAbility(A.ID());
		if(teachableA==null)
		{
			teachableA=(Ability)A.copyOf();
			teacher.addAbility(teachableA);
		}
		teachableA.setProficiency(100);
		return teachableA;
	}

	protected boolean shownInInventory(MOB seller, MOB buyer, Environmental product, ShopKeeper shopKeeper)
	{
		if(CMSecurity.isAllowed(buyer,buyer.location(),CMSecurity.SecFlag.CMDMOBS))
			return true;
		if(seller == buyer)
			return true;
		if(product instanceof Item)
		{
			if(((Item)product).container()!=null)
				return false;
			if(((Item)product).phyStats().level()>buyer.phyStats().level())
				return false;
			if(!CMLib.flags().canBeSeenBy(product,buyer))
				return false;
		}
		if(product instanceof MOB)
		{
			if(((MOB)product).phyStats().level()>buyer.phyStats().level())
				return false;
		}
		if(product instanceof Ability)
		{
			if(shopKeeper.isSold(ShopKeeper.DEAL_TRAINER))
			{
				if(!CMLib.ableMapper().qualifiesByLevel(buyer, (Ability)product))
					return false;
			}
		}
		return true;
	}

	@Override
	public double rawSpecificGoldPrice(Environmental product,
									   CoffeeShop shop,
									   double numberOfThem)
	{
		double price=0.0;
		if(product instanceof Item)
			price=((Item)product).value()*numberOfThem;
		else
		if(product instanceof Ability)
		{
			if(shop.isSold(ShopKeeper.DEAL_TRAINER))
				price=CMLib.ableMapper().lowestQualifyingLevel(product.ID())*100;
			else
				price=CMLib.ableMapper().lowestQualifyingLevel(product.ID())*75;
		}
		else
		if(product instanceof MOB)
		{
			final MOB M=(MOB)product;
			final Ability A=M.fetchEffect("Prop_Retainable");
			if(A!=null)
			{
				if(A.text().indexOf(';')<0)
				{
					if(CMath.isDouble(A.text()))
						price=CMath.s_double(A.text());
					else
						price=CMath.s_int(A.text());
				}
				else
				{
					final String s2=A.text().substring(0,A.text().indexOf(';'));
					if(CMath.isDouble(s2))
						price=CMath.s_double(s2);
					else
						price=CMath.s_int(s2);
				}
			}
			if(price==0.0)
				price=(25.0+M.phyStats().level())*M.phyStats().level();
		}
		else
			price=CMLib.ableMapper().lowestQualifyingLevel(product.ID())*25;
		return price;
	}

	@Override
	public double prejudiceValueFromPart(MOB customer, boolean pawnTo, String part)
	{
		final int x=part.indexOf('=');
		if(x<0)
			return 0.0;
		final String sellorby=part.substring(0,x);
		part=part.substring(x+1);
		if(pawnTo&&(!sellorby.trim().equalsIgnoreCase("SELL")))
		   return 0.0;
		if((!pawnTo)&&(!sellorby.trim().equalsIgnoreCase("BUY")))
		   return 0.0;
		if(part.trim().indexOf(' ')<0)
			return CMath.s_double(part.trim());
		final Vector<String> V=CMParms.parse(part.trim());
		double d=0.0;
		boolean yes=false;
		final List<String> VF=customer.fetchFactionRanges();
		final String align=CMLib.flags().getAlignmentName(customer);
		final String sex=customer.charStats().genderName();
		for(int v=0;v<V.size();v++)
		{
			final String bit=V.elementAt(v);
			if(CMath.s_double(bit)!=0.0)
				d=CMath.s_double(bit);
			if(bit.equalsIgnoreCase(customer.charStats().getCurrentClass().name() ))
			{ yes=true; break;}
			if(bit.equalsIgnoreCase(customer.charStats().getCurrentClass().name(customer.charStats().getCurrentClassLevel()) ))
			{ yes=true; break;}
			if(bit.equalsIgnoreCase(sex ))
			{ yes=true; break;}
			if(bit.equalsIgnoreCase(customer.charStats().getMyRace().racialCategory()))
			{   yes=true; break;}
			if(bit.equalsIgnoreCase(align))
			{ yes=true; break;}
			for(int vf=0;vf<VF.size();vf++)
				if(bit.equalsIgnoreCase(V.elementAt(v)))
				{ yes=true; break;}
		}
		if(yes)
			return d;
		return 0.0;

	}

	@Override
	public double prejudiceFactor(MOB customer, String factors, boolean pawnTo)
	{
		factors=factors.toUpperCase();
		if(factors.length()==0)
		{
			factors=CMProps.getVar(CMProps.Str.PREJUDICE).trim();
			if(factors.length()==0)
				return 1.0;
		}
		if(factors.indexOf('=')<0)
		{
			if(CMath.s_double(factors)!=0.0)
				return CMath.s_double(factors);
			return 1.0;
		}
		int x=factors.indexOf(';');
		while(x>=0)
		{
			final String part=factors.substring(0,x).trim();
			factors=factors.substring(x+1).trim();
			final double d=prejudiceValueFromPart(customer,pawnTo,part);
			if(d!=0.0)
				return d;
			x=factors.indexOf(';');
		}
		final double d=prejudiceValueFromPart(customer,pawnTo,factors.trim());
		if(d!=0.0)
			return d;
		return 1.0;
	}

	public double itemPriceFactor(Environmental E, Room R, String[] priceFactors, boolean pawnTo)
	{
		if(priceFactors.length==0)
			return 1.0;
		double factor=1.0;
		int x=0;
		String factorMask=null;
		ItemPossessor oldOwner=null;
		if(E instanceof Item)
		{
			oldOwner=((Item)E).owner();
			if(R!=null)
				((Item)E).setOwner(R);
		}
		for (final String priceFactor : priceFactors)
		{
			factorMask=priceFactor.trim();
			x=factorMask.indexOf(' ');
			if(x<0)
				continue;
			if(CMLib.masking().maskCheck(factorMask.substring(x+1).trim(),E,false))
				factor*=CMath.s_double(factorMask.substring(0,x).trim());
		}
		if(E instanceof Item)
			((Item)E).setOwner(oldOwner);
		if(factor!=0.0)
			return factor;
		return 1.0;
	}

	@Override
	public ShopKeeper.ShopPrice sellingPrice(MOB seller,
											 MOB buyer,
											 Environmental product,
											 ShopKeeper shop,
											 boolean includeSalesTax)
	{
		final double number=1.0;
		final ShopKeeper.ShopPrice val=new ShopKeeper.ShopPrice();
		if(product==null)
			return val;
		final int stockPrice=shop.getShop().stockPrice(product);
		if(stockPrice<=-100)
		{
			if(stockPrice<=-1000)
				val.experiencePrice=(stockPrice*-1)-1000;
			else
				val.questPointPrice=(stockPrice*-1)-100;
			return val;
		}
		if(stockPrice>=0)
			val.absoluteGoldPrice=stockPrice;
		else
			val.absoluteGoldPrice=rawSpecificGoldPrice(product,shop.getShop(),number);

		if(buyer==null)
		{
			if(val.absoluteGoldPrice>0.0)
				val.absoluteGoldPrice=CMLib.beanCounter().abbreviatedRePrice(seller,val.absoluteGoldPrice);
			return val;
		}

		double prejudiceFactor=prejudiceFactor(buyer,shop.finalPrejudiceFactors(),false);
		final Room loc=CMLib.map().roomLocation(shop);
		prejudiceFactor*=itemPriceFactor(product,loc,shop.finalItemPricingAdjustments(),false);
		val.absoluteGoldPrice=CMath.mul(prejudiceFactor,val.absoluteGoldPrice);

		// the price is 200% at 0 charisma, and 100% at 35
		if(seller.isMonster() && (!CMLib.flags().isGolem(seller)))
			val.absoluteGoldPrice=val.absoluteGoldPrice
								 +val.absoluteGoldPrice
								 -CMath.mul(val.absoluteGoldPrice,CMath.div(buyer.charStats().getStat(CharStats.STAT_QUICKNESS),35.0));
		if(includeSalesTax)
		{
			final double salesTax=getSalesTax(seller.getStartRoom(),seller);
			val.absoluteGoldPrice+=((salesTax>0.0)?(CMath.mul(val.absoluteGoldPrice,CMath.div(salesTax,100.0))):0.0);
		}
		if(val.absoluteGoldPrice<=0.0)
			val.absoluteGoldPrice=1.0;
		else
			val.absoluteGoldPrice=CMLib.beanCounter().abbreviatedRePrice(seller,val.absoluteGoldPrice);

		// the magical aura discount for miscmagic (potions, anything else.. MUST be basePhyStats tho!
		if((CMath.bset(buyer.basePhyStats().disposition(),PhyStats.IS_BONUS))
		&&(product instanceof MiscMagic)
		&&(val.absoluteGoldPrice>2.0))
			val.absoluteGoldPrice/=2;

		return val;
	}


	@Override
	public double devalue(ShopKeeper shop, Environmental product)
	{
		final int num=shop.getShop().numberInStock(product);
		if(num<=0)
			return 0.0;
		double resourceRate=0.0;
		double itemRate=0.0;
		final String s=shop.finalDevalueRate();
		final Vector<String> V=CMParms.parse(s.trim());
		if(V.size()<=0)
			return 0.0;
		else
		if(V.size()==1)
		{
			resourceRate=CMath.div(CMath.s_double(V.firstElement()),100.0);
			itemRate=resourceRate;
		}
		else
		{
			itemRate=CMath.div(CMath.s_double(V.firstElement()),100.0);
			resourceRate=CMath.div(CMath.s_double(V.lastElement()),100.0);
		}
		double rate=(product instanceof RawMaterial)?resourceRate:itemRate;
		rate=rate*num;
		if(rate>1.0)
			rate=1.0;
		if(rate<0.0)
			rate=0.0;
		return rate;
	}

	@Override
	public ShopKeeper.ShopPrice pawningPrice(MOB seller,
											 MOB buyer,
											 Environmental product,
											 ShopKeeper shop)
	{
		double number=1.0;
		if(product instanceof PackagedItems)
		{
			number=((PackagedItems)product).numberOfItemsInPackage();
			product=((PackagedItems)product).getFirstItem();
		}
		final ShopKeeper.ShopPrice val=new ShopKeeper.ShopPrice();
		if(product==null)
			return val;
		final int stockPrice=shop.getShop().stockPrice(product);
		if(stockPrice<=-100)
			return val;

		if(stockPrice>=0.0)
			val.absoluteGoldPrice=stockPrice;
		else
			val.absoluteGoldPrice=rawSpecificGoldPrice(product,shop.getShop(),number);

		if(buyer==null)
			return val;

		double prejudiceFactor=prejudiceFactor(buyer,shop.finalPrejudiceFactors(),true);
		final Room loc=CMLib.map().roomLocation(shop);
		prejudiceFactor*=itemPriceFactor(product,loc,shop.finalItemPricingAdjustments(),true);
		val.absoluteGoldPrice=CMath.mul(prejudiceFactor,val.absoluteGoldPrice);

		// gets the shopkeeper a deal on junk.  Pays 5% at 3 charisma, and 50% at 35
		double buyPrice=CMath.div(CMath.mul(val.absoluteGoldPrice,buyer.charStats().getStat(CharStats.STAT_QUICKNESS)),70.0);
		if(!(product instanceof Ability))
			buyPrice=CMath.mul(buyPrice,1.0-devalue(shop,product));


		final double sellPrice=sellingPrice(seller,buyer,product,shop,false).absoluteGoldPrice;

		if(buyPrice>sellPrice)
			val.absoluteGoldPrice=sellPrice;
		else
			val.absoluteGoldPrice=buyPrice;

		if(val.absoluteGoldPrice<=0.0)
			val.absoluteGoldPrice=1.0;
		return val;
	}


	@Override
	public double getSalesTax(Room homeRoom, MOB seller)
	{
		if((seller==null)||(homeRoom==null))
			return 0.0;
		final Law theLaw=CMLib.law().getTheLaw(homeRoom,seller);
		if(theLaw!=null)
		{
			final String taxs=(String)theLaw.taxLaws().get("SALESTAX");
			if(taxs!=null)
				return CMath.s_double(taxs);
		}
		return 0.0;

	}

	@Override
	public boolean standardSellEvaluation(MOB seller,
										  MOB buyer,
										  Environmental product,
										  ShopKeeper shop,
										  double maxToPay,
										  double maxEverPaid,
										  boolean sellNotValue)
	{
		if((product!=null)
		&&(shop.doISellThis(product))
		&&(!(product instanceof Coins)))
		{
			final Room sellerR=seller.location();
			if(sellerR!=null)
			{
				final int medianLevel=sellerR.getArea().getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()];
				if(medianLevel>0)
				{
					final String range=CMParms.getParmStr(shop.finalPrejudiceFactors(),"RANGE","0");
					int rangeI=0;
					if((range.endsWith("%"))&&(CMath.isInteger(range.substring(0,range.length()-1))))
					{
						rangeI=CMath.s_int(range.substring(0,range.length()-1));
						rangeI=(int)Math.round(CMath.mul(medianLevel,CMath.div(rangeI,100.0)));
					}
					else
					if(CMath.isInteger(range))
						rangeI=CMath.s_int(range);
					if((rangeI>0)
					&&(product instanceof Physical)
					&&((((Physical)product).phyStats().level()>(medianLevel+rangeI))
						||(((Physical)product).phyStats().level()<(medianLevel-rangeI))))
					{
						CMLib.commands().postSay(seller,buyer,L("I'm sorry, that's out of my level range."),true,false);
						return false;
					}
				}
			}
			final double yourValue=pawningPrice(seller,buyer,product,shop).absoluteGoldPrice;
			if(yourValue<2)
			{
				CMLib.commands().postSay(seller,buyer,L("I'm not interested."),true,false);
				return false;
			}
			if((product instanceof Physical)&&CMLib.flags().isEnspelled((Physical)product) || CMLib.flags().isOnFire((Physical)product))
			{
				CMLib.commands().postSay(seller, buyer, L("I won't buy that in it's present state."), true, false);
				return false;
			}
			if((sellNotValue)&&(yourValue>maxToPay))
			{
				if(yourValue>maxEverPaid)
					CMLib.commands().postSay(seller,buyer,L("That's way out of my price range! Try AUCTIONing it."),true,false);
				else
					CMLib.commands().postSay(seller,buyer,L("Sorry, I can't afford that right now.  Try back later."),true,false);
				return false;
			}
			if(product instanceof Ability)
			{
				CMLib.commands().postSay(seller,buyer,L("I'm not interested."),true,false);
				return false;
			}
			if((product instanceof Container)&&(((Container)product).hasALock()))
			{
				boolean found=false;
				final List<Item> V=((Container)product).getContents();
				for(int i=0;i<V.size();i++)
				{
					final Item I=V.get(i);
					if((I instanceof DoorKey)
					&&(((DoorKey)I).getKey().equals(((Container)product).keyName())))
						found=true;
					else
					if(CMLib.flags().isEnspelled(I) || CMLib.flags().isOnFire(I))
					{
						CMLib.commands().postSay(seller, buyer, L("I won't buy the contents of that in it's present state."), true, false);
						return false;
					}
				}
				if(!found)
				{
					CMLib.commands().postSay(seller,buyer,L("I won't buy that back unless you put the key in it."),true,false);
					return false;
				}
			}
			if((product instanceof Item)&&(buyer.isMine(product)))
			{
				final CMMsg msg2=CMClass.getMsg(buyer,product,CMMsg.MSG_DROP,null);
				if(!buyer.location().okMessage(buyer,msg2))
					return false;
			}
			return true;
		}
		CMLib.commands().postSay(seller,buyer,L("I'm sorry, I'm not buying those."),true,false);
		return false;
	}


	@Override
	public boolean standardBuyEvaluation(MOB seller,
										 MOB buyer,
										 Environmental product,
										 ShopKeeper shop,
										 boolean buyNotView)
	{
		if((product!=null)
		&&(shop.getShop().doIHaveThisInStock("$"+product.Name()+"$",buyer)))
		{
			if(buyNotView)
			{
				final ShopKeeper.ShopPrice price=sellingPrice(seller,buyer,product,shop,true);
				if((price.experiencePrice>0)&&(price.experiencePrice>buyer.getExperience()))
				{
					CMLib.commands().postSay(seller,buyer,L("You aren't experienced enough to buy @x1.",product.name()),false,false);
					return false;
				}
				if((price.questPointPrice>0)&&(price.questPointPrice>buyer.getQuestPoint()))
				{
					CMLib.commands().postSay(seller,buyer,L("You don't have enough quest points to buy @x1.",product.name()),false,false);
					return false;
				}
				if((price.absoluteGoldPrice>0.0)
				&&(price.absoluteGoldPrice>CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(buyer,seller)))
				{
					CMLib.commands().postSay(seller,buyer,L("You can't afford to buy @x1.",product.name()),false,false);
					return false;
				}
			}
			if(product instanceof Item)
			{
				if(((Item)product).phyStats().level()>buyer.phyStats().level())
				{
					CMLib.commands().postSay(seller,buyer,L("That's too advanced for you, I'm afraid."),true,false);
					return false;
				}
			}
			if((product instanceof LandTitle)
			&&((shop.isSold(ShopKeeper.DEAL_CLANDSELLER))||(shop.isSold(ShopKeeper.DEAL_CSHIPSELLER))))
			{
				final Pair<Clan,Integer> clanPair=CMLib.clans().findPrivilegedClan(buyer, Clan.Function.PROPERTY_OWNER);
				if(clanPair==null)
				{
					if(!buyer.clans().iterator().hasNext())
						CMLib.commands().postSay(seller,buyer,L("I only sell to clans."),true,false);
					else
					if(!buyer.isMonster())
						CMLib.commands().postSay(seller,buyer,L("You are not authorized by your clan to handle property."),true,false);
					return false;
				}
			}
			if(product instanceof MOB)
			{
				if(buyer.totalFollowers()>=buyer.maxFollowers())
				{
					CMLib.commands().postSay(seller,buyer,L("You can't accept any more followers."),true,false);
					return false;
				}
				if((CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF)>0)
				&&(!CMSecurity.isAllowed(seller,seller.location(),CMSecurity.SecFlag.ORDER))
				&&(!CMSecurity.isAllowed(buyer,buyer.location(),CMSecurity.SecFlag.ORDER)))
				{
					if(seller.phyStats().level() > (buyer.phyStats().level() + CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF)))
					{
						buyer.tell(L("@x1 is too advanced for you.",product.name()));
						return false;
					}
					if(seller.phyStats().level() < (buyer.phyStats().level() - CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF)))
					{
						buyer.tell(L("@x1 is too inexperienced for you.",product.name()));
						return false;
					}
				}
			}
			if(product instanceof Ability)
			{
				if(shop.isSold(ShopKeeper.DEAL_TRAINER))
				{
					final MOB teacher=CMClass.getMOB("Teacher");
					final Ability teachableA=getTrainableAbility(teacher, (Ability)product);
					if((teachableA==null)||(!teachableA.canBeLearnedBy(teacher,buyer)))
					{
						teacher.destroy();
						return false;
					}
					teacher.destroy();
				}
				else
				if(buyNotView)
				{
					final Ability A=(Ability)product;
					if(A.canTarget(Ability.CAN_MOBS)){}
					else
					if(A.canTarget(Ability.CAN_ITEMS))
					{
						Item I=buyer.fetchWieldedItem();
						if(I==null)
							I=buyer.fetchHeldItem();
						if(I==null)
						{
							CMLib.commands().postSay(seller,buyer,L("You need to be wielding or holding the item you want this cast on."),true,false);
							return false;
						}
					}
					else
					{
						CMLib.commands().postSay(seller,buyer,L("I don't know how to sell that spell."),true,false);
						return false;
					}
				}
			}
			return true;
		}
		CMLib.commands().postSay(seller,buyer,L("I don't have that in stock.  Ask for my LIST."),true,false);
		return false;
	}

	@Override
	public String getListInventory(MOB seller,
								   MOB buyer,
								   List<? extends Environmental> rawInventory,
								   int limit,
								   ShopKeeper shop,
								   String mask)
	{
		final StringBuffer str=new StringBuffer("");
		int csize=0;
		final Vector<Environmental> inventory=new Vector<Environmental>();
		Environmental E=null;
		for(int i=0;i<rawInventory.size();i++)
		{
			E=rawInventory.get(i);
			if(shownInInventory(seller,buyer,E,shop)
			&&((mask==null)||(mask.length()==0)||(CMLib.english().containsString(E.name(),mask))))
				inventory.addElement(E);
		}

		if(inventory.size()>0)
		{
			final int totalCols=((shop.isSold(ShopKeeper.DEAL_LANDSELLER))
						   ||(shop.isSold(ShopKeeper.DEAL_CLANDSELLER))
						   ||(shop.isSold(ShopKeeper.DEAL_SHIPSELLER))
						   ||(shop.isSold(ShopKeeper.DEAL_CSHIPSELLER)))?1:2;
			final int totalWidth=ListingLibrary.ColFixer.fixColWidth(60.0/totalCols,buyer);
			String showPrice=null;
			ShopKeeper.ShopPrice price=null;
			for(int i=0;i<inventory.size();i++)
			{
				E=inventory.elementAt(i);
				price=sellingPrice(seller,buyer,E,shop,true);
				if((price.experiencePrice>0)&&(((""+price.experiencePrice).length()+2)>(4+csize)))
					csize=(""+price.experiencePrice).length()-2;
				else
				if((price.questPointPrice>0)&&(((""+price.questPointPrice).length()+2)>(4+csize)))
					csize=(""+price.questPointPrice).length()-2;
				else
				{
					showPrice=CMLib.beanCounter().abbreviatedPrice(seller,price.absoluteGoldPrice);
					if(showPrice.length()>(4+csize))
						csize=showPrice.length()-4;
				}
			}

			final String c="^x["+CMStrings.padRight(L("Cost"),4+csize)+"] "+CMStrings.padRight(L("Product"),Math.max(totalWidth-csize,5));
			str.append(c+((totalCols>1)?c:"")+"^.^N^<!ENTITY shopkeeper \""+CMStrings.removeColors(seller.name())+"\"^>^.^N\n\r");
			int colNum=0;
			int rowNum=0;
			String col=null;
			for(int i=0;i<inventory.size();i++)
			{
				E=inventory.elementAt(i);
				price=sellingPrice(seller,buyer,E,shop,true);
				col=null;
				if(price.questPointPrice>0)
					col=CMStrings.padRight(L("[@x1qp",""+price.questPointPrice),(5+csize))+"] ^<SHOP^>"+CMStrings.padRight(E.name(),"^</SHOP^>",Math.max(totalWidth-csize,5));
				else
				if(price.experiencePrice>0)
					col=CMStrings.padRight(L("[@x1xp",""+price.experiencePrice),(5+csize))+"] ^<SHOP^>"+CMStrings.padRight(E.name(),"^</SHOP^>",Math.max(totalWidth-csize,5));
				else
					col=CMStrings.padRight("["+CMLib.beanCounter().abbreviatedPrice(seller,price.absoluteGoldPrice),5+csize)+"] ^<SHOP^>"+CMStrings.padRight(E.name(),"^</SHOP^>",Math.max(totalWidth-csize,5));
				if((++colNum)>totalCols)
				{
					str.append("\n\r");
					rowNum++;
					if((limit>0)&&(rowNum>limit))
						break;
					colNum=1;
				}
				str.append(col);
			}
		}
		if(str.length()==0)
		{
			if((!shop.isSold(ShopKeeper.DEAL_BANKER))
			&&(!shop.isSold(ShopKeeper.DEAL_CLANBANKER))
			&&(!shop.isSold(ShopKeeper.DEAL_CLANPOSTMAN))
			&&(!shop.isSold(ShopKeeper.DEAL_AUCTIONEER))
			&&(!shop.isSold(ShopKeeper.DEAL_POSTMAN)))
				return seller.name()+" has nothing for sale.";
			return "";
		}
		final double salesTax=getSalesTax(seller.getStartRoom(),seller);
		return "\n\r"+str
				+((salesTax<=0.0)?"":"\n\r\n\rPrices above include a "+salesTax+"% sales tax.")
				+"^T";
	}

	@Override
	public String findInnRoom(InnKey key, String addThis, Room R)
	{
		if(R==null)
			return null;
		final String keyNum=key.getKey();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if((R.getExitInDir(d)!=null)&&(R.getExitInDir(d).keyName().equals(keyNum)))
			{
				final String dirName=((R instanceof BoardableShip)||(R.getArea() instanceof BoardableShip))?
						Directions.getShipDirectionName(d):Directions.getDirectionName(d);
				if(addThis.length()>0)
					return addThis+" and to the "+dirName.toLowerCase();
				return "to the "+dirName.toLowerCase();
			}
		}
		return null;
	}

	@Override
	public MOB parseBuyingFor(MOB buyer, String message)
	{
		MOB mobFor=buyer;
		if((message!=null)&&(message.length()>0)&&(buyer.location()!=null))
		{
			final Vector<String> V=CMParms.parse(message);
			if(V.elementAt(V.size()-2).equalsIgnoreCase("for"))
			{
				String s=V.lastElement();
				if(s.endsWith("."))
					s=s.substring(0,s.length()-1);
				final MOB M=buyer.location().fetchInhabitant("$"+s+"$");
				if(M!=null)
					mobFor=M;
			}
		}
		return mobFor;
	}

	@Override
	public double transactPawn(MOB shopkeeper,
							   MOB pawner,
							   ShopKeeper shop,
							   Environmental product)
	{
		Environmental coreSoldItem=product;
		final Environmental rawSoldItem=product;
		int number=1;
		if(coreSoldItem instanceof PackagedItems)
		{
			coreSoldItem=((PackagedItems)rawSoldItem).getFirstItem();
			number=((PackagedItems)rawSoldItem).numberOfItemsInPackage();
		}
		if((coreSoldItem!=null)&&(shop.doISellThis(coreSoldItem)))
		{
			final double val=pawningPrice(shopkeeper,pawner,rawSoldItem,shop).absoluteGoldPrice;
			final String currency=CMLib.beanCounter().getCurrency(shopkeeper);
			if(!(shopkeeper instanceof ShopKeeper))
				CMLib.beanCounter().subtractMoney(shopkeeper,currency,val);
			CMLib.beanCounter().giveSomeoneMoney(shopkeeper,pawner,currency,val);
			pawner.recoverPhyStats();
			pawner.tell(L("@x1 pays you @x2 for @x3.",shopkeeper.name(),CMLib.beanCounter().nameCurrencyShort(shopkeeper,val),rawSoldItem.name()));
			if(rawSoldItem instanceof Item)
			{
				List<Item> V=null;
				if(rawSoldItem instanceof Container)
					V=((Container)rawSoldItem).getDeepContents();
				((Item)rawSoldItem).unWear();
				((Item)rawSoldItem).removeFromOwnerContainer();
				if(V!=null)
				for(int v=0;v<V.size();v++)
					V.get(v).removeFromOwnerContainer();
				shop.getShop().addStoreInventory(coreSoldItem,number,-1);
				if(V!=null)
				for(int v=0;v<V.size();v++)
				{
					final Item item2=V.get(v);
					if(!shop.doISellThis(item2)||(item2 instanceof DoorKey))
						item2.destroy();
					else
						shop.getShop().addStoreInventory(item2,1,-1);
				}
			}
			else
			if(product instanceof MOB)
			{
				final MOB newMOB=(MOB)product.copyOf();
				newMOB.setStartRoom(null);
				final Ability A=newMOB.fetchEffect("Skill_Enslave");
				if(A!=null)
					A.setMiscText("");
				newMOB.setLiegeID("");
				newMOB.setClan("", Integer.MIN_VALUE); // delete all sequence
				shop.getShop().addStoreInventory(newMOB);
				((MOB)product).setFollowing(null);
				if((((MOB)product).basePhyStats().rejuv()>0)
				&&(((MOB)product).basePhyStats().rejuv()!=PhyStats.NO_REJUV)
				&&(((MOB)product).getStartRoom()!=null))
					((MOB)product).killMeDead(false);
				else
					((MOB)product).destroy();
			}
			else
			if(product instanceof Ability)
			{

			}
			return val;
		}
		return Double.MIN_VALUE;
	}

	@Override
	public void transactMoneyOnly(MOB seller,
								  MOB buyer,
								  ShopKeeper shop,
								  Environmental product,
								  boolean sellerGetsPaid)
	{
		if((seller==null)||(seller.location()==null)||(buyer==null)||(shop==null)||(product==null))
			return;
		final Room room=seller.location();
		final ShopKeeper.ShopPrice price=sellingPrice(seller,buyer,product,shop,true);
		if(price.absoluteGoldPrice>0.0)
		{
			CMLib.beanCounter().subtractMoney(buyer,CMLib.beanCounter().getCurrency(seller),price.absoluteGoldPrice);
			double totalFunds=price.absoluteGoldPrice;
			if(getSalesTax(seller.getStartRoom(),seller)!=0.0)
			{
				final Law theLaw=CMLib.law().getTheLaw(room,seller);
				final Area A2=CMLib.law().getLegalObject(room);
				if((theLaw!=null)&&(A2!=null))
				{
					final Law.TreasurySet treas=theLaw.getTreasuryNSafe(A2);
					final Room treasuryR=treas.room;
					final Container treasuryContainer=treas.container;
					if(treasuryR!=null)
					{
						final double taxAmount=totalFunds-sellingPrice(seller,buyer,product,shop,false).absoluteGoldPrice;
						totalFunds-=taxAmount;
						final Coins COIN=CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(seller),taxAmount,treasuryR,treasuryContainer);
						if(COIN!=null)
							COIN.putCoinsBack();
					}
				}
			}
			if(seller.isMonster())
			{
				final LandTitle T=CMLib.law().getLandTitle(seller.getStartRoom());
				if((T!=null)&&(T.getOwnerName().length()>0))
				{
					CMLib.beanCounter().modifyLocalBankGold(seller.getStartRoom().getArea(),
													T.getOwnerName(),
													CMLib.utensils().getFormattedDate(buyer)+": Deposit of "+CMLib.beanCounter().nameCurrencyShort(seller,totalFunds)+": Purchase: "+product.Name()+" from "+seller.Name(),
													CMLib.beanCounter().getCurrency(seller),
													totalFunds);
				}
			}
			if(sellerGetsPaid)
				CMLib.beanCounter().giveSomeoneMoney(seller,seller,CMLib.beanCounter().getCurrency(seller),totalFunds);
		}
		if(price.questPointPrice>0)
			buyer.setQuestPoint(buyer.getQuestPoint()-price.questPointPrice);
		if(price.experiencePrice>0)
			CMLib.leveler().postExperience(buyer,null,null,-price.experiencePrice,false);
		buyer.recoverPhyStats();
	}

	@Override
	public boolean purchaseItems(Item baseProduct, List<Environmental> products, MOB seller, MOB mobFor)
	{
		if((seller==null)||(seller.location()==null)||(mobFor==null))
			return false;
		final Room room=seller.location();
		for(int p=0;p<products.size();p++)
			if(products.get(p) instanceof Item)
				room.addItem((Item)products.get(p),ItemPossessor.Expire.Player_Drop);
		final CMMsg msg2=CMClass.getMsg(mobFor,baseProduct,seller,CMMsg.MSG_GET,null);
		if((baseProduct instanceof LandTitle)||(room.okMessage(mobFor,msg2)))
		{
			room.send(mobFor,msg2);
			if(baseProduct instanceof InnKey)
			{
				final InnKey item =(InnKey)baseProduct;
				String buf=findInnRoom(item, "", room);
				if(buf==null)
					buf=findInnRoom(item, "upstairs", room.getRoomInDir(Directions.UP));
				if(buf==null)
					buf=findInnRoom(item, "downstairs", room.getRoomInDir(Directions.DOWN));
				if(buf!=null)
					CMLib.commands().postSay(seller,mobFor,L("Your room is @x1.",buf),true,false);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean purchaseMOB(MOB product, MOB seller, ShopKeeper shop, MOB mobFor)
	{
		if((seller==null)||(seller.location()==null)||(product==null)||(shop==null)||(mobFor==null))
			return false;
		product.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		product.recoverPhyStats();
		product.setMiscText(product.text());
		Ability slaveA=null;
		if(shop.isSold(ShopKeeper.DEAL_SLAVES))
		{
			slaveA=product.fetchEffect("Skill_Enslave");
			if(slaveA!=null)
				slaveA.setMiscText("");
			else
			if(!CMLib.flags().isAnimalIntelligence(product))
			{
				slaveA=CMClass.getAbility("Skill_Enslave");
				if(slaveA!=null)
					product.addNonUninvokableEffect(slaveA);
			}
		}
		product.bringToLife(seller.location(),true);
		if(slaveA!=null)
		{
			product.setLiegeID("");
			product.setClan("", Integer.MIN_VALUE); // delete all sequence
			product.setStartRoom(null);
			slaveA.setMiscText(mobFor.Name());
			product.text();
		}
		CMLib.commands().postFollow(product,mobFor,false);
		if(product.amFollowing()==null)
		{
			mobFor.tell(L("You cannot accept seem to accept this follower!"));
			return false;
		}
		return true;
	}

	@Override
	public void purchaseAbility(Ability A,
								MOB seller,
								ShopKeeper shop,
								MOB mobFor)
	{
		if((seller==null)||(seller.location()==null)||(A==null)||(shop==null)||(mobFor==null))
			return ;
		final Room room=seller.location();
		if(shop.isSold(ShopKeeper.DEAL_TRAINER))
		{
			final MOB teacher=CMClass.getMOB("Teacher");
			teacher.setName(seller.name());
			teacher.setBaseCharStats(seller.baseCharStats());
			teacher.setLocation(room);
			teacher.recoverCharStats();
			final Ability teachableA=getTrainableAbility(teacher,A);
			if(teachableA!=null)
				CMLib.expertises().postTeach(teacher,mobFor,A);
			teacher.destroy();
		}
		else
		{
			if(seller.isMonster())
			{
				seller.curState().setMana(seller.maxState().getMana());
				seller.curState().setMovement(seller.maxState().getMovement());
			}
			final Object[][] victims=new Object[room.numInhabitants()][2];
			for(int x=0;x>victims.length;x++)
			{ // save victim status
				final MOB M=room.fetchInhabitant(x);
				if(M!=null){ victims[x][0]=M;victims[x][1]=M.getVictim();}
			}
			final Vector<String> V=new Vector<String>();
			if(A.canTarget(Ability.CAN_MOBS))
			{
				V.addElement("$"+mobFor.name()+"$");
				A.invoke(seller,V,mobFor,true,0);
			}
			else
			if(A.canTarget(Ability.CAN_ITEMS))
			{
				Item I=mobFor.fetchWieldedItem();
				if(I==null)
					I=mobFor.fetchHeldItem();
				if(I==null)
					I=mobFor.fetchItem(null,Wearable.FILTER_WORNONLY,"all");
				if(I==null)
					I=mobFor.fetchItem(null,Wearable.FILTER_UNWORNONLY,"all");
				if(I!=null)
				{
					V.addElement("$"+I.name()+"$");
					seller.addItem(I);
					A.invoke(seller,V,I,true,0);
					seller.delItem(I);
					if(!mobFor.isMine(I))
						mobFor.addItem(I);
				}
			}
			if(seller.isMonster())
			{
				seller.curState().setMana(seller.maxState().getMana());
				seller.curState().setMovement(seller.maxState().getMovement());
			}
			for(int x=0;x>victims.length;x++)
				((MOB)victims[x][0]).setVictim((MOB)victims[x][1]);
		}
	}

	@Override
	public List<Environmental> addRealEstateTitles(List<Environmental> V, MOB buyer, CoffeeShop shop, Room myRoom)
	{
		if((myRoom==null)||(buyer==null))
			return V;
		final Area myArea=myRoom.getArea();
		String name=buyer.Name();
		Pair<Clan,Integer> buyerClanPair=null;
		if(shop.isSold(ShopKeeper.DEAL_CLANDSELLER)
		&&((buyerClanPair=CMLib.clans().findPrivilegedClan(buyer, Clan.Function.PROPERTY_OWNER))!=null))
			name=buyerClanPair.first.clanID();
		if((shop.isSold(ShopKeeper.DEAL_LANDSELLER)||(buyerClanPair!=null))
		&&(myArea!=null))
		{
			final HashSet<Environmental> roomsHandling=new HashSet<Environmental>();
			final Map<Environmental,LandTitle> titles=new Hashtable<Environmental,LandTitle>();
			for(final Enumeration<Room> r=myArea.getProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				final LandTitle A=CMLib.law().getLandTitle(R);
				if((A!=null)&&(R.roomID().length()>0))
					titles.put(R,A);
			}

			for(final Environmental R : titles.keySet())
			{
				final LandTitle A=titles.get(R);
				if(!roomsHandling.contains(R))
				{
					if(R instanceof Area)
						roomsHandling.add(R);
					else
					{
						final List<Room> V2=A.getAllTitledRooms();
						roomsHandling.addAll(V2);
					}
					if((A.getOwnerName().length()>0)
					&&(!A.getOwnerName().equals(name))
					&&((!A.getOwnerName().equals(buyer.getLiegeID()))||(!buyer.isMarriedToLiege())))
						continue;
					boolean skipThisOne=false;
					if(R instanceof Room)
						for(int d=0;d<4;d++)
						{
							final Room R2=((Room)R).getRoomInDir(d);
							LandTitle L2=null;
							if(R2!=null)
							{
								L2=titles.get(R2);
								if(L2==null)
								{ skipThisOne=false; break;}
							}
							else
								continue;
							if((L2.getOwnerName().equals(name))
							||(L2.getOwnerName().equals(buyer.getLiegeID())&&(buyer.isMarriedToLiege())))
							{ skipThisOne=false; break;}
							if(L2.getOwnerName().length()>0)
								skipThisOne=true;
						}
					if(skipThisOne)
						continue;
					final Item I=CMClass.getItem("GenTitle");
					if(R instanceof Room)
						((LandTitle)I).setLandPropertyID(CMLib.map().getExtendedRoomID((Room)R));
					else
						((LandTitle)I).setLandPropertyID(R.Name());
					if((((LandTitle)I).getOwnerName().length()>0)
					&&(!I.Name().endsWith(" (Copy)")))
						I.setName(L("@x1 (Copy)",I.Name()));
					I.text();
					I.recoverPhyStats();
					if((A.getOwnerName().length()==0)
					&&(I.Name().endsWith(" (Copy)")))
						I.setName(I.Name().substring(0,I.Name().length()-7));
					V.add(I);
				}
			}
		}
		if(V.size()<2)
			return V;
		final Vector<Environmental> V2=new Vector<Environmental>(V.size());
		LandTitle L=null;
		LandTitle L2=null;
		int x=-1;
		int x2=-1;
		while(V.size()>0)
		{
			if(((!(V.get(0) instanceof LandTitle)))
			||((x=(L=(LandTitle)V.get(0)).landPropertyID().lastIndexOf('#'))<0))
			{
				if(V2.size()==0)
					V2.addElement(V.remove(0));
				else
					V2.insertElementAt(V.remove(0),0);
			}
			else
			{

				int lowest=CMath.s_int(L.landPropertyID().substring(x+1).trim());
				for(int v=1;v<V.size();v++)
					if(V.get(v) instanceof LandTitle)
					{
						L2=(LandTitle)V.get(v);
						x2=L2.landPropertyID().lastIndexOf('#');
						if((x2>0)&&(CMath.s_int(L2.landPropertyID().substring(x+1).trim())<lowest))
						{
							lowest=CMath.s_int(L2.landPropertyID().substring(x+1).trim());
							L=L2;
						}
					}
				V.remove(L);
				V2.addElement(L);
			}
		}
		return V2;
	}

	@Override
	public boolean ignoreIfNecessary(MOB mob, String ignoreMask, MOB whoIgnores)
	{
		if((ignoreMask.length()>0)&&(!CMLib.masking().maskCheck(ignoreMask,mob,false)))
		{
			mob.tell(whoIgnores,null,null,L("<S-NAME> appear(s) to be ignoring you."));
			return false;
		}
		return true;
	}


	@Override
	public String storeKeeperString(CoffeeShop shop)
	{
		if(shop==null)
			return "";
		if(shop.isSold(ShopKeeper.DEAL_ANYTHING))
			return "*Anything*";

		final Vector<String> V=new Vector<String>();
		for(int d=1;d<ShopKeeper.DEAL_DESCS.length;d++)
		if(shop.isSold(d))
		switch(d)
		{
		case ShopKeeper.DEAL_GENERAL:
			V.addElement("General items"); break;
		case ShopKeeper.DEAL_ARMOR:
			V.addElement("Armor"); break;
		case ShopKeeper.DEAL_MAGIC:
			V.addElement("Miscellaneous Magic Items"); break;
		case ShopKeeper.DEAL_WEAPONS:
			V.addElement("Weapons"); break;
		case ShopKeeper.DEAL_PETS:
			V.addElement("Pets and Animals"); break;
		case ShopKeeper.DEAL_LEATHER:
			V.addElement("Leather"); break;
		case ShopKeeper.DEAL_INVENTORYONLY:
			V.addElement("Only my Inventory"); break;
		case ShopKeeper.DEAL_TRAINER:
			V.addElement("Training in skills/spells/prayers/songs"); break;
		case ShopKeeper.DEAL_CASTER:
			V.addElement("Caster of spells/prayers"); break;
		case ShopKeeper.DEAL_ALCHEMIST:
			V.addElement("Potions"); break;
		case ShopKeeper.DEAL_INNKEEPER:
			V.addElement("My services as an Inn Keeper"); break;
		case ShopKeeper.DEAL_JEWELLER:
			V.addElement("Precious stones and jewellery"); break;
		case ShopKeeper.DEAL_BANKER:
			V.addElement("My services as a Banker"); break;
		case ShopKeeper.DEAL_CLANBANKER:
			V.addElement("My services as a Banker to Clans"); break;
		case ShopKeeper.DEAL_LANDSELLER:
			V.addElement("Real estate"); break;
		case ShopKeeper.DEAL_CLANDSELLER:
			V.addElement("Clan estates"); break;
		case ShopKeeper.DEAL_ANYTECHNOLOGY:
			V.addElement("Any technology"); break;
		case ShopKeeper.DEAL_BUTCHER:
			V.addElement("Meats"); break;
		case ShopKeeper.DEAL_FOODSELLER:
			V.addElement("Foodstuff"); break;
		case ShopKeeper.DEAL_GROWER:
			V.addElement("Vegetables"); break;
		case ShopKeeper.DEAL_HIDESELLER:
			V.addElement("Hides and Furs"); break;
		case ShopKeeper.DEAL_LUMBERER:
			V.addElement("Lumber"); break;
		case ShopKeeper.DEAL_METALSMITH:
			V.addElement("Metal ores"); break;
		case ShopKeeper.DEAL_STONEYARDER:
			V.addElement("Stone and rock"); break;
		case ShopKeeper.DEAL_SHIPSELLER:
			V.addElement("Ships"); break;
		case ShopKeeper.DEAL_CSHIPSELLER:
			V.addElement("Clan Ships"); break;
		case ShopKeeper.DEAL_SLAVES:
			V.addElement("Slaves"); break;
		case ShopKeeper.DEAL_POSTMAN:
			V.addElement("My services as a Postman"); break;
		case ShopKeeper.DEAL_CLANPOSTMAN:
			V.addElement("My services as a Postman for Clans"); break;
		case ShopKeeper.DEAL_AUCTIONEER:
			V.addElement("My services as an Auctioneer"); break;
		default:
			V.addElement("... I have no idea WHAT I sell"); break;
		}
		return CMParms.toStringList(V);
	}

	protected boolean shopKeeperItemTypeCheck(Environmental E, int dealCode, ShopKeeper shopKeeper)
	{
		switch(dealCode)
		{
		case ShopKeeper.DEAL_ANYTHING:
			return !(E instanceof LandTitle);
		case ShopKeeper.DEAL_ARMOR:
			return (E instanceof Armor);
		case ShopKeeper.DEAL_MAGIC:
			return (E instanceof MiscMagic);
		case ShopKeeper.DEAL_WEAPONS:
			return (E instanceof Weapon)||(E instanceof Ammunition);
		case ShopKeeper.DEAL_GENERAL:
			return ((E instanceof Item)
					&&(!(E instanceof Armor))
					&&(!(E instanceof MiscMagic))
					&&(!(E instanceof ClanItem))
					&&(!(E instanceof Weapon))
					&&(!(E instanceof Ammunition))
					&&(!(E instanceof MOB))
					&&(!(E instanceof LandTitle))
					&&(!(E instanceof BoardableShip))
					&&(!(E instanceof RawMaterial))
					&&(!(E instanceof Ability)));
		case ShopKeeper.DEAL_LEATHER:
			return ((E instanceof Item)
					&&((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LEATHER)
					&&(!(E instanceof RawMaterial)));
		case ShopKeeper.DEAL_PETS:
			return ((E instanceof MOB)&&(CMLib.flags().isAnimalIntelligence((MOB)E)));
		case ShopKeeper.DEAL_SLAVES:
			return ((E instanceof MOB)&&(!CMLib.flags().isAnimalIntelligence((MOB)E)));
		case ShopKeeper.DEAL_INVENTORYONLY:
			return (shopKeeper.getShop().inEnumerableInventory(E));
		case ShopKeeper.DEAL_INNKEEPER:
			return E instanceof InnKey;
		case ShopKeeper.DEAL_JEWELLER:
			return ((E instanceof Item)
					&&(!(E instanceof Weapon))
					&&(!(E instanceof MiscMagic))
					&&(!(E instanceof ClanItem))
					&&(((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_GLASS)
					||((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
					||((Item)E).fitsOn(Wearable.WORN_EARS)
					||((Item)E).fitsOn(Wearable.WORN_NECK)
					||((Item)E).fitsOn(Wearable.WORN_RIGHT_FINGER)
					||((Item)E).fitsOn(Wearable.WORN_LEFT_FINGER)));
		case ShopKeeper.DEAL_ALCHEMIST:
			return (E instanceof Potion);
		case ShopKeeper.DEAL_LANDSELLER:
		case ShopKeeper.DEAL_CLANDSELLER:
			return (E instanceof LandTitle);
		case ShopKeeper.DEAL_SHIPSELLER:
		case ShopKeeper.DEAL_CSHIPSELLER:
			return (E instanceof BoardableShip);
		case ShopKeeper.DEAL_ANYTECHNOLOGY:
			return (E instanceof Electronics);
		case ShopKeeper.DEAL_BUTCHER:
			return ((E instanceof RawMaterial)
				&&(((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH);
		case ShopKeeper.DEAL_FOODSELLER:
			return (((E instanceof Food)||(E instanceof Drink))
					&&(!(E instanceof RawMaterial)));
		case ShopKeeper.DEAL_GROWER:
			return ((E instanceof RawMaterial)
				&&(((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION);
		case ShopKeeper.DEAL_HIDESELLER:
			return ((E instanceof RawMaterial)
				&&((((RawMaterial)E).material()==RawMaterial.RESOURCE_HIDE)
				||(((RawMaterial)E).material()==RawMaterial.RESOURCE_FEATHERS)
				||(((RawMaterial)E).material()==RawMaterial.RESOURCE_LEATHER)
				||(((RawMaterial)E).material()==RawMaterial.RESOURCE_SCALES)
				||(((RawMaterial)E).material()==RawMaterial.RESOURCE_WOOL)
				||(((RawMaterial)E).material()==RawMaterial.RESOURCE_FUR)));
		case ShopKeeper.DEAL_LUMBERER:
			return ((E instanceof RawMaterial)
				&&((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN));
		case ShopKeeper.DEAL_METALSMITH:
			return ((E instanceof RawMaterial)
				&&(((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
				||(((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL));
		case ShopKeeper.DEAL_STONEYARDER:
			return ((E instanceof RawMaterial)
				&&((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_ROCK));
		}
		return false;
	}

	@Override
	public boolean doISellThis(Environmental thisThang, ShopKeeper shop)
	{
		if(thisThang instanceof PackagedItems)
			thisThang=((PackagedItems)thisThang).getFirstItem();
		if(thisThang==null)
			return false;
		if((thisThang instanceof Coins)
		||(thisThang instanceof DeadBody)
		||(CMLib.flags().isChild(thisThang)))
			return false;
		if(shop.isSold(ShopKeeper.DEAL_ANYTHING))
			return !(thisThang instanceof LandTitle);
		else
		for(int d=1;d<ShopKeeper.DEAL_DESCS.length;d++)
			if(shop.isSold(d) && shopKeeperItemTypeCheck(thisThang,d,shop))
				return true;
		return false;
	}

	@Override
	public void returnMoney(MOB to, String currency, double amt)
	{
		if(amt>0)
			CMLib.beanCounter().giveSomeoneMoney(to, currency, amt);
		else
			CMLib.beanCounter().subtractMoney(to, currency,-amt);
		if(amt!=0)
			if(!CMLib.flags().isInTheGame(to,true))
				CMLib.database().DBUpdatePlayerItems(to);
	}

	@Override
	public String[] bid(MOB mob, double bid, String bidCurrency, Auctioneer.AuctionData auctionData, Item I, List<String> auctionAnnounces)
	{
		String bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.currency,auctionData.bid);
		final String currencyName=CMLib.beanCounter().getDenominationName(auctionData.currency);
		if(bid==0.0)
			return new String[]{"Up for auction: "+I.name()+".  The current bid is "+bidWords+".",null};

		if(!bidCurrency.equals(auctionData.currency))
			return new String[]{"This auction is being bid in "+currencyName+" only.",null};

		if(bid>CMLib.beanCounter().getTotalAbsoluteValue(mob,auctionData.currency))
			return new String[]{"You don't have enough "+currencyName+" on hand to cover that bid.",null};

		if((bid<auctionData.bid)||(bid==0))
		{
			final String bwords=CMLib.beanCounter().nameCurrencyShort(bidCurrency, bid);
			return new String[]{"Your bid of "+bwords+" is insufficient."+((auctionData.bid>0)?" The current high bid is "+bidWords+".":""),null};
		}
		else
		if((bid>auctionData.highBid)||((bid>auctionData.bid)&&(auctionData.highBid==0)))
		{
			final MOB oldHighBider=auctionData.highBidderM;
			if(auctionData.highBidderM!=null)
				returnMoney(auctionData.highBidderM,auctionData.currency,auctionData.highBid);
			auctionData.highBidderM=mob;
			if(auctionData.highBid<=0.0)
			{
				if(auctionData.bid>0)
					auctionData.highBid=auctionData.bid;
				else
					auctionData.highBid=0.0;
			}
			auctionData.bid=auctionData.highBid+1.0;
			auctionData.highBid=bid;
			returnMoney(auctionData.highBidderM,auctionData.currency,-bid);
			bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.currency,auctionData.bid);
			final String yourBidWords = CMLib.beanCounter().abbreviatedPrice(currencyName, auctionData.highBid);
			auctionAnnounces.add("A new bid has been entered for "+I.name()+". The current high bid is "+bidWords+".");
			if((oldHighBider!=null)&&(oldHighBider==mob))
				return new String[]{"You have submitted a new high bid of "+yourBidWords+" for "+I.name()+".",null};
			else
			if((oldHighBider!=null)&&(oldHighBider!=mob))
				return new String[]{"You have the new high reserve bid of "+yourBidWords+" for "+I.name()+". The current nominal high bid is "+bidWords+".","You have been outbid for "+I.name()+"."};
			else
				return new String[]{"You have submitted a bid of "+yourBidWords+" for "+I.name()+".",null};
		}
		else
		if((bid==auctionData.bid)&&(auctionData.highBidderM!=null))
		{
			return new String[]{"You must bid higher than "+bidWords+" to have your bid accepted.",null};
		}
		else
		if((bid==auctionData.highBid)&&(auctionData.highBidderM!=null))
		{
			if((auctionData.highBidderM!=null)&&(auctionData.highBidderM!=mob))
			{
				auctionData.bid=bid;
				bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.currency,auctionData.bid);
				auctionAnnounces.add("A new bid has been entered for "+I.name()+". The current bid is "+bidWords+".");
				return new String[]{"You have been outbid by proxy for "+I.name()+".","Your high bid for "+I.name()+" has been reached."};
			}
		}
		else
		{
			auctionData.bid=bid;
			bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.currency,auctionData.bid);
			auctionAnnounces.add("A new bid has been entered for "+I.name()+". The current bid is "+bidWords+".");
			return new String[]{"You have been outbid by proxy for "+I.name()+".",null};
		}
		return null;
	}

	@Override
	public Auctioneer.AuctionData getEnumeratedAuction(String named, String auctionHouse)
	{
		final List<AuctionData> V=getAuctions(null,auctionHouse);
		final Vector<Item> V2=new Vector<Item>();
		for(int v=0;v<V.size();v++)
			V2.addElement(V.get(v).auctioningI);
		Environmental E=CMLib.english().fetchEnvironmental(V2,named,true);
		if(!(E instanceof Item))
			E=CMLib.english().fetchEnvironmental(V2,named,false);
		if(E!=null)
		for(int v=0;v<V.size();v++)
			if(V.get(v).auctioningI==E)
				return V.get(v);
		return null;
	}

	@Override
	public void saveAuction(Auctioneer.AuctionData data, String auctionHouse, boolean updateOnly)
	{
		if(data.auctioningI instanceof Container)
			((Container)data.auctioningI).emptyPlease(false);
		final StringBuffer xml=new StringBuffer("<AUCTION>");
		xml.append("<PRICE>"+data.bid+"</PRICE>");
		xml.append("<BUYOUT>"+data.buyOutPrice+"</BUYOUT>");
		if(data.highBidderM!=null)
			xml.append("<BIDDER>"+data.highBidderM.Name()+"</BIDDER>");
		else
			xml.append("<BIDDER />");
		xml.append("<MAXBID>"+data.highBid+"</MAXBID>");
		xml.append("<AUCTIONITEM>");
		xml.append(CMLib.coffeeMaker().getItemXML(data.auctioningI).toString());
		xml.append("</AUCTIONITEM>");
		xml.append("</AUCTION>");
		if(!updateOnly)
			CMLib.database().DBWriteJournal("SYSTEM_AUCTIONS_"+auctionHouse.toUpperCase().trim(),
											data.auctioningM.Name(),
											""+data.tickDown,
											CMStrings.limit(data.auctioningI.name(),38),
											xml.toString());
		else
			CMLib.database().DBUpdateJournal(data.auctionDBKey, data.auctioningI.Name(),xml.toString(), 0);
	}

	@Override
	public List<AuctionData> getAuctions(Object ofLike, String auctionHouse)
	{
		final Vector<AuctionData> auctions=new Vector<AuctionData>();
		final String house="SYSTEM_AUCTIONS_"+auctionHouse.toUpperCase().trim();
		final List<JournalsLibrary.JournalEntry> otherAuctions=CMLib.database().DBReadJournalMsgs(house);
		for(int o=0;o<otherAuctions.size();o++)
		{
			final JournalsLibrary.JournalEntry auctionData=otherAuctions.get(o);
			final String from=auctionData.from;
			final String to=auctionData.to;
			final String key=auctionData.key;
			if((ofLike instanceof MOB)&&(!((MOB)ofLike).Name().equals(to)))
				continue;
			if((ofLike instanceof String)&&(!((String)ofLike).equals(key)))
				continue;
			final AuctionData data=new AuctionData();
			data.start=auctionData.date;
			data.tickDown=CMath.s_long(to);
			final String xml=auctionData.msg;
			List<XMLLibrary.XMLpiece> xmlV=CMLib.xml().parseAllXML(xml);
			xmlV=CMLib.xml().getContentsFromPieces(xmlV,"AUCTION");
			final String bid=CMLib.xml().getValFromPieces(xmlV,"PRICE");
			final double oldBid=CMath.s_double(bid);
			data.bid=oldBid;
			final String highBidder=CMLib.xml().getValFromPieces(xmlV,"BIDDER");
			if(highBidder.length()>0)
				data.highBidderM=CMLib.players().getLoadPlayer(highBidder);
			final String maxBid=CMLib.xml().getValFromPieces(xmlV,"MAXBID");
			final double oldMaxBid=CMath.s_double(maxBid);
			data.highBid=oldMaxBid;
			data.auctionDBKey=key;
			final String buyOutPrice=CMLib.xml().getValFromPieces(xmlV,"BUYOUT");
			data.buyOutPrice=CMath.s_double(buyOutPrice);
			data.auctioningM=CMLib.players().getLoadPlayer(from);
			data.currency=CMLib.beanCounter().getCurrency(data.auctioningM);
			for(int v=0;v<xmlV.size();v++)
			{
				final XMLLibrary.XMLpiece X=xmlV.get(v);
				if(X.tag.equalsIgnoreCase("AUCTIONITEM"))
				{
					data.auctioningI=CMLib.coffeeMaker().getItemFromXML(X.value);
					break;
				}
			}
			if((ofLike instanceof Item)&&(!((Item)ofLike).sameAs(data.auctioningI)))
				continue;
			auctions.addElement(data);
		}
		return auctions;
	}

	@Override
	public String getListForMask(String targetMessage)
	{
		if(targetMessage==null)
			return null;
		final int x=targetMessage.toUpperCase().lastIndexOf("FOR '");
		if(x>0)
		{
			final int y=targetMessage.lastIndexOf('\'');
			if(y>x)
				return targetMessage.substring(x+5,y);
		}
		return null;
	}

	@Override
	public String getAuctionInventory(MOB seller,
									  MOB buyer,
									  Auctioneer auction,
									  String mask)
	{
		final StringBuffer str=new StringBuffer("");
		str.append("^x"+CMStrings.padRight(L("Lvl"),3)+" "+CMStrings.padRight(L("Item"),50)+" "+CMStrings.padRight(L("Days"),4)+" ["+CMStrings.padRight(L("Bid"),6)+"] Buy^.^N\n\r");
		final List<AuctionData> auctions=getAuctions(null,auction.auctionHouse());
		for(int v=0;v<auctions.size();v++)
		{
			final Auctioneer.AuctionData data=auctions.get(v);
			if(shownInInventory(seller,buyer,data.auctioningI,auction))
			{
				if(((mask==null)||(mask.length()==0)||(CMLib.english().containsString(data.auctioningI.name(),mask)))
				&&((data.tickDown>System.currentTimeMillis())||(data.auctioningM==buyer)||(data.highBidderM==buyer)))
				{
					Area area=CMLib.map().getStartArea(seller);
					if(area==null)
						area=CMLib.map().getStartArea(buyer);
					str.append(CMStrings.padRight(""+data.auctioningI.phyStats().level(),3)+" ");
					str.append(CMStrings.padRight(data.auctioningI.name(),50)+" ");
					if(data.tickDown>System.currentTimeMillis())
					{
						final long days=data.daysRemaining(buyer,seller);
						str.append(CMStrings.padRight(""+days,4)+" ");
					}
					else
					if(data.auctioningM==buyer)
						str.append("DONE ");
					else
						str.append("WON! ");
					str.append("["+CMStrings.padRight(CMLib.beanCounter().abbreviatedPrice(seller,data.bid),6)+"] ");
					if(data.buyOutPrice<=0.0)
						str.append(CMStrings.padRight("-",6));
					else
						str.append(CMStrings.padRight(CMLib.beanCounter().abbreviatedPrice(seller,data.buyOutPrice),6));
					str.append("\n\r");
				}
			}
		}
		return "\n\r"+str.toString();
	}

	@Override
	public void auctionNotify(MOB M, String resp, String regardingItem)
	{
		try
		{
			if(CMLib.flags().isInTheGame(M,true))
				M.tell(resp);
			else
			if(M.playerStats()!=null)
			{
				CMLib.smtp().emailIfPossible(CMProps.getVar(CMProps.Str.SMTPSERVERNAME),
												"auction@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(),
												"noreply@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(),
												M.playerStats().getEmail(),
												"Auction Update for item: "+regardingItem,
												resp);
			}
		}catch(final Exception e){}
	}

	@Override
	public void cancelAuction(String auctionHouse, Auctioneer.AuctionData data)
	{
		if(data.auctioningI!=null)
		{
			if(data.auctioningM!=null)
				data.auctioningM.moveItemTo(data.auctioningI);
			if(data.highBidderM!=null)
			{
				final MOB M=data.highBidderM;
				auctionNotify(M,"The auction for "+data.auctioningI.Name()+" was closed early.  You have been refunded your max bid.",data.auctioningI.Name());
				CMLib.coffeeShops().returnMoney(M,data.currency,data.highBid);
			}
		}
		CMLib.database().DBDeleteJournal(auctionHouse, data.auctionDBKey);
		if(data.auctioningI!=null)
			data.auctioningM.tell(L("Auction ended."));
	}
}
