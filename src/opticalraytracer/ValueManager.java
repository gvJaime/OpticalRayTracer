/***************************************************************************
 *   Copyright (C) 2014 by Paul Lutus                                      *
 *   lutusp@arachnoid.com                                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package opticalraytracer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValueManager {

	String lineSep;

	public ValueManager() {
		lineSep = System.getProperty("line.separator");
	}
	
	void p(String s) {
		System.out.println(s);
	}

	protected String getValues() {
		ArrayList<String> list = new ArrayList<>();
		Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields) {
			String tag = f.getName();
			list.add(String.format("  %-25s = %s%s", tag, getOneValue(tag),lineSep));
		}
		Collections.sort(list);
		StringBuilder sb = new StringBuilder();
		for (String s : list) {
			sb.append(s);
		}
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	protected String getOneValue(String tag) {
		String result = "";
		try {
			Field f = getClass().getDeclaredField(tag);
			//result = f.get(this).toString();
			Class cls = f.getType();
			if(cls == double.class) {
				result = LocaleHandler.formatDouble(f.getDouble(this));
			}
			else if(cls == int.class) {
				result = LocaleHandler.formatInt(f.getInt(this));
			}
			else {
				result = f.get(this).toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		//p("valuemanager.getonevalue: tag: " + tag + ", value: " + result);
		return result;
	}

	public String toString() {
		return getValues();
	}

	protected boolean setValues(String s) {
		boolean result = false;
		if (s != null) {
			String key,value;
			String[] recs = s.split("\n");
			for (String rec : recs) {
				rec = rec.trim();
				//p("record: [" + rec + "]");
				Pattern p = Pattern.compile("(.*?)\\s*=\\s*(.*)");
				Matcher m = p.matcher(rec);
				if(m.matches() && m.groupCount() == 2) {
					 key = m.group(1).trim();
					 value = m.group(2).trim();
					setOneValue(key,value);
					result = true;
				}
			}
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	protected void setOneValue(String tag, String value) {
		//p("setOneValue: key: " + tag + ", value: " + value);
		try {
			Field f = getClass().getDeclaredField(tag);
			Class cls = f.getType();
			if (cls == boolean.class) {
				f.setBoolean(this, value.matches("(?i).*true.*"));
			} else if (cls == int.class) {
				f.setInt(this, LocaleHandler.getInt(value));
			} else if (cls == double.class) {
				f.setDouble(this, LocaleHandler.getDouble(value,LocaleHandler.localeDecimalSeparator));
			} else if (cls == String.class) {
				f.set(this, value);
			} else {
				//throw new Exception("Cannot decode field type " + cls);
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
}
