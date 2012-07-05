/*
 * Copyright (c) 2012 Tyler Treat
 * 
 * This file is part of Infinitum Framework.
 *
 * Infinitum Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Infinitum Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Infinitum Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.clarionmedia.infinitum.internal;

/**
 * Contains static utility methods for dealing with strings.
 * 
 * @author Tyler Treat
 * @version 1.0 03/15/12
 */
public class StringUtil {

	/**
	 * Returns the getter method name for the given field name.
	 * 
	 * @param valName
	 *            the name of the field to get the getter method name for
	 * @return name of getter method
	 */
	public static String getterName(String valName) {
		if (valName == null)
			return null;
		return "get" + capitalizeString(valName);
	}

	/**
	 * Capitalizes the first letter of the given {@link String}.
	 * 
	 * @param string
	 *            the {@code String} to capitalize
	 * @return capitalized {@code String}
	 */
	public static String capitalizeString(String string) {
		if (string == null)
			return string;
		if (string.length() == 0)
			return string;
		if (string.length() == 1)
			return string.toUpperCase();
		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}

	/**
	 * Returns the formatted field name by removing the 'm' prefix found in
	 * Android naming conventions and putting the name to lowercase.
	 * 
	 * @param name
	 *            the field name to format
	 * @return the formatted name
	 */
	public static String formatFieldName(String name) {
		if (!name.startsWith("m") || name.length() <= 1)
			return name.toLowerCase();
		if (Character.isUpperCase(name.charAt(1)))
			return name.substring(1).toLowerCase();
		return name.toLowerCase();
	}

}