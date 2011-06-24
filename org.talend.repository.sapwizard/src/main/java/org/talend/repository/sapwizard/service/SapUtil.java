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

import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.SAPFunctionParameterColumn;
import org.talend.core.model.metadata.builder.connection.SAPFunctionParameterTable;

import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Client;
import com.sap.mw.jco.JCO.Field;
import com.sap.mw.jco.JCO.Function;
import com.sap.mw.jco.JCO.ParameterList;
import com.sap.mw.jco.JCO.Structure;
import com.sap.mw.jco.JCO.Table;

/**
 * @author Ammu
 * 
 */
public class SapUtil {
	/**
	 * SAP_CLIENT
	 */
	public static final String SAP_CLIENT = "000"; //$NON-NLS-1$
	/**
	 * SAP_LANGUAGE
	 */
	public static final String SAP_LANGUAGE = "EN";//$NON-NLS-2$
	/**
	 * SAP_SYSTEM_NUMBER
	 */
	public static final String SAP_SYSTEM_NUMBER = "00";//$NON-NLS-N$

	/**
	 * @param client
	 * @param language
	 * @param sysNumber
	 * @param host
	 * @param userName
	 * @param password
	 * @return
	 * @throws Throwable
	 */
	public static boolean connectSAPserver(String client, String language, String sysNumber, String host,
			String userName, String password) throws Throwable {
		Client jcoClient = null;
		boolean connected = false;
		try {
			jcoClient = JCO
					.createClient(client != null ? client : SAP_CLIENT, userName, password, language != null ? language
							: SAP_LANGUAGE, host, sysNumber != null ? sysNumber : SAP_SYSTEM_NUMBER);
			jcoClient.connect();
			connected = true;
		} catch (Exception exception) {
			connected = false;
			throw exception;
		} catch (Throwable throwable) {
			connected = false;
			throw throwable;
		} finally {
			if (jcoClient != null) {
				jcoClient.disconnect();
			}
		}
		return connected;
	}

	/**
	 * @param sapFunctionParameterTable
	 * @param function
	 * @param paramType
	 */
	public static void createParamsForFunction(SAPFunctionParameterTable sapFunctionParameterTable, Function function,
			int paramType) {
		int j;
		int n;
		Object object;
		Object localObject2;

		if (function == null) {
			return;
		}
		ParameterList parameterList = null;
		String input = "input";
		switch (paramType) {
		case 0:
			parameterList = function.getImportParameterList();
			break;
		case 1:
			parameterList = function.getExportParameterList();
			input = "output";
			break;
		default:
			return;
		}
		if (parameterList != null) {
			int i = parameterList.getFieldCount();
			for (j = 0; j < i; j++) {
				Field field = parameterList.getField(j);
				if (field == null)
					continue;
				int m;
				Field localField2;
				if (field.isTable()) {
					object = field.getTable();
					m = ((Table) object).getFieldCount();
					for (n = 0; n < m; n++) {
						localField2 = ((Table) object).getField(n);
						if ((localField2 == null) || (localField2.isStructure()) || (localField2.isTable()))
							continue;
						localObject2 = "table";
						createSingleParameterColumn(sapFunctionParameterTable, localField2, input + "."
								+ (String) localObject2, field.getName());
					}
				} else if (field.isStructure()) {
					object = field.getStructure();
					m = ((Structure) object).getFieldCount();
					for (n = 0; n < m; n++) {
						localField2 = ((Structure) object).getField(n);
						if ((localField2 == null) || (localField2.isStructure()) || (localField2.isTable()))
							continue;
						localObject2 = "structure";
						createSingleParameterColumn(sapFunctionParameterTable, localField2, input + "."
								+ (String) localObject2, field.getName());
					}
				} else {
					object = "single";
					createSingleParameterColumn(sapFunctionParameterTable, field, input + "." + (String) object, null);
				}
			}
		}
		ParameterList localParameterList2 = function.getTableParameterList();
		if (localParameterList2 != null) {
			input = "table";
			ParameterList localParameterList3 = localParameterList2;
			j = localParameterList3.getFieldCount();
			for (int k = 0; k < j; k++) {
				object = localParameterList3.getField(k);
				if (object == null)
					continue;
				if (((Field) object).isTable()) {
					Table localTable = ((Field) object).getTable();
					n = localTable.getFieldCount();
					for (int i1 = 0; i1 < n; i1++) {
						localObject2 = localTable.getField(i1);
						if ((localObject2 == null) || (((Field) localObject2).isStructure())
								|| (((Field) localObject2).isTable()))
							continue;
						String str2 = "input";
						if (paramType == 1)
							str2 = "output";
						createSingleParameterColumn(sapFunctionParameterTable, (Field) localObject2,
								input + "." + str2, ((Field) object).getName());
					}
				} else {
					((Field) object).isStructure();
				}
			}
		}
	}

	/**
	 * @param sapFunctionParameterTable
	 * @param field
	 * @param paramTyrpe
	 * @param structOrTableName
	 */
	@SuppressWarnings("deprecation")
	private static void createSingleParameterColumn(SAPFunctionParameterTable sapFunctionParameterTable, Field field,
			String paramTyrpe, String structOrTableName) {
		SAPFunctionParameterColumn sapFunctionParameterColumn = ConnectionFactory.eINSTANCE
				.createSAPFunctionParameterColumn();
		sapFunctionParameterColumn.setName(field.getName());
		sapFunctionParameterColumn.setDataType(field.getTypeAsString());
		sapFunctionParameterColumn.setParameterType(paramTyrpe);
		sapFunctionParameterColumn.setLength(Integer.toString(field.getLength()));
		sapFunctionParameterColumn.setDescription(field.getDescription());
		if (structOrTableName != null)
			sapFunctionParameterColumn.setStructureOrTableName(structOrTableName);
		sapFunctionParameterTable.getColumns().add(sapFunctionParameterColumn);
	}

	/**
	 * @param sapFunctionParameterColumn
	 * @return
	 */
	public static String getParameterType(SAPFunctionParameterColumn sapFunctionParameterColumn) {
		String inputParamType = sapFunctionParameterColumn.getParameterType();
		if ((inputParamType.startsWith("input")) || (inputParamType.startsWith("output")))
			return inputParamType.substring(inputParamType.indexOf(".") + 1);
		if (inputParamType.startsWith("table"))
			return "table";
		return null;
	}
}