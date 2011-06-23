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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ammu
 * 
 */
public enum SapParameterTypeEnum {

	INPUT_SINGLE,

	INPUT_STRUCTURE,

	INPUT_TABLE, OUTPUT_SINGLE,

	OUTPUT_STRUCTURE,

	OUTPUT_TABLE,

	TABLE_INPUT,

	TABLE_OUTPUT;

	/**
	 * @return
	 */
	public String getDisplayLabel() {
		String label = name().toLowerCase().replace('_', '.');
		return label;
	}

	/**
	 * @return
	 */
	public static final String[] getLabels() {
		List<String> paramList = new ArrayList<String>();
		for (SapParameterTypeEnum paramType : values()) {
			paramList.add(paramType.getDisplayLabel());
		}
		return paramList.toArray(new String[0]);
	}

	/**
	 * @return
	 */
	public static final String[] getOutputLabels() {
		List<String> paramList = new ArrayList<String>();
		for (SapParameterTypeEnum paramType : values()) {
			if ((!((SapParameterTypeEnum) paramType).getDisplayLabel().toLowerCase().startsWith("output"))
					&& (!((SapParameterTypeEnum) paramType).getDisplayLabel().toLowerCase().endsWith("output"))) {
				continue;
			}
			paramList.add(((SapParameterTypeEnum) paramType).getDisplayLabel());
		}
		return paramList.toArray(new String[0]);
	}
}