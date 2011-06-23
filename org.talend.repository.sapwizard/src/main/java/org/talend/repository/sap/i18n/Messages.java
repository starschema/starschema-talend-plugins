/** 
 *    Copyright (C) 2011, Starschema Ltd. <info at starschema.net>
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 2 of the License, or
 *    any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package org.talend.repository.sap.i18n;

import java.util.ResourceBundle;

import org.talend.commons.i18n.MessagesCore;

/**
 * @author Ammu
 * 
 */
public class Messages extends MessagesCore {

	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

	private static final String PLUGIN_ID = "org.talend.repository.sap"; //$NON-NLS-1$

	private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Returns the i18n formatted message for <i>key</i> in the class bundle.
	 * 
	 * @param key
	 *            - the key for the desired string
	 * @return the string for the given key in the class resource bundle
	 * @see MessagesCore#getString(String, ResourceBundle)
	 */
	public static String getString(String key) {
		return getString(key, PLUGIN_ID, resourceBundle);
	}

	/**
	 * Returns the i18n formatted message for <i>key</i> and <i>args</i> in the
	 * specified bundle.
	 * 
	 * @param key
	 *            - the key for the desired string
	 * @param args
	 *            - arg to include in the string
	 * @return the string for the given key in the given resource bundle
	 * @see MessagesCore#getString(String, ResourceBundle, Object[])
	 */
	public static String getString(String key, Object... args) {
		return MessagesCore.getString(key, PLUGIN_ID, resourceBundle, args);
	}
}
