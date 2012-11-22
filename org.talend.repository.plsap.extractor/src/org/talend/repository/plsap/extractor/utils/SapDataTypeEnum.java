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

package org.talend.repository.plsap.extractor.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ammu
 * 
 */
public enum SapDataTypeEnum {
	CLNT, NUMC, INT4, TIMS, QUAN, DEC, CURR, DATS, UNIT, INVALID, CHAR, DATE, BCD, TIME, BYTE, ITAB, NUM, FLOAT, INT, INT2, INT1, STRING, XSTRING, STRUCTURE, TABLE, EXCEPTION, ITEM;

	public static String[] getLabels() {
		List<String> arrayOfString = new ArrayList<String>();
		for (SapDataTypeEnum typeEnum : values()) {
			arrayOfString.add(typeEnum.name());
		}
		return arrayOfString.toArray(new String[0]);
	}
}
