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
package org.talend.repository.sapwizard.service;

import java.util.Properties;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

/**
 * Represents the destination to a specific SAP system. The destination is
 * maintained via a property file
 * 
 * @author Ammu
 * 
 */
public class SapCustomDataProvider implements DestinationDataProvider {
	public static String SAP_SERVER = "SAP_SERVER";
	private final Properties ABAP_AS_PROPERTIES;

	public SapCustomDataProvider(String client, String language, String sysNumber, String host, String userName, String password) {
		Properties properties = new Properties();
		properties.setProperty(DestinationDataProvider.JCO_ASHOST, host);
		properties.setProperty(DestinationDataProvider.JCO_SYSNR, sysNumber);
		properties.setProperty(DestinationDataProvider.JCO_CLIENT, client);
		properties.setProperty(DestinationDataProvider.JCO_LANG,   language);
		properties.setProperty(DestinationDataProvider.JCO_USER, userName);
		properties.setProperty(DestinationDataProvider.JCO_PASSWD, password);

		ABAP_AS_PROPERTIES = properties;
	}

	public Properties getDestinationProperties(String system) {
		return ABAP_AS_PROPERTIES;
	}

	public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
	}

	public boolean supportsEvents() {
		return false;
	}

}