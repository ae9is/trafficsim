/*
  SPDX-License-Identifier: GPL-3.0-only

  Part of the AI for Games library
  Copyright (c) 2011 Peter Lager
  Licensed under the GNU LGPL v2.1

  This copy is relicensed under the GNU GPL v3.0
 */

package pathfinder;

import java.text.MessageFormat;
import java.util.Locale;

public class Message {

	
	public static String build(String pattern, Object ... arguments){
		return (pattern == null) ? "" : new MessageFormat(pattern, Locale.UK).format(arguments);        
	}
	
	public static String println(String pattern, Object ... arguments){
		String s = build(pattern, arguments);
		System.out.println(s);
		return s;
	}
}
