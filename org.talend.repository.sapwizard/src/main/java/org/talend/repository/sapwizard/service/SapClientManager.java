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

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.SAPFunctionParameterColumn;
import org.talend.core.model.metadata.builder.connection.SAPTestInputParameterTable;
import org.talend.repository.sap.i18n.Messages;

import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Client;
import com.sap.mw.jco.JCO.Function;
import com.sap.mw.jco.JCO.ParameterList;
import com.sap.mw.jco.JCO.Record;
import com.sap.mw.jco.JCO.Structure;
import com.sap.mw.jco.JCO.Table;

/**
 * @author Ammu
 * 
 */
public class SapClientManager {

	/**
	 * repository
	 */
	private IRepository repository = null;

	/**
	 * @param client
	 * @param host
	 * @param userName
	 * @param password
	 * @param language
	 * @param systemNumber
	 * @throws Throwable
	 */
	public SapClientManager(String client, String host, String userName, String password, String language,
			String systemNumber) throws Throwable {
		init(client, host, userName, password, language, systemNumber);
	}

	/**
	 * @param client
	 * @param host
	 * @param userName
	 * @param password
	 * @param language
	 * @param systemNumber
	 * @throws Throwable
	 */
	private void init(String client, String host, String userName, String password, String language, String systemNumber)
			throws Throwable {
		try {
			if (this.repository == null) {
				this.repository = JCO.createRepository("SAPRepository", "R3");
				JCO.removeClientPool("R3");
				JCO.addClientPool("R3", 10, client, userName, password, language, host, systemNumber);
			}
		} catch (Exception exception) {
			throw exception;
		} catch (Throwable throwable) {
			ExceptionHandler.process(throwable);
			throw throwable;
		}
	}

	/**
	 * @param function
	 * @param sapFunctionParameterColumn
	 * @return
	 */
	public Object parseOutputValue(Function function, SAPFunctionParameterColumn sapFunctionParameterColumn) {
		if ((function == null) || (sapFunctionParameterColumn == null)) {
			return null;
		}
		String paramType = sapFunctionParameterColumn.getParameterType();
		String param = paramType.substring(0, paramType.indexOf('.'));
		ParameterList exportParameterList = function.getExportParameterList();
		ParameterList tableParameterList = function.getTableParameterList();
		String subParam = paramType.substring(paramType.indexOf('.') + 1);
		String strucOrTableName;
		Record record;
		if (param.equals("output")) {
			if (subParam.equals("single")) {
				return getParameterValue(sapFunctionParameterColumn, exportParameterList);
			}
			if (subParam.equals("structure")) {
				strucOrTableName = sapFunctionParameterColumn.getStructureOrTableName();
				if (strucOrTableName != null) {
					record = exportParameterList.getStructure(sapFunctionParameterColumn.getStructureOrTableName());
					if (record != null) {
						return getStructureValue(sapFunctionParameterColumn, (Structure) record);
					}
				}
			} else if (subParam.equals("table")) {
				strucOrTableName = sapFunctionParameterColumn.getStructureOrTableName();
				if (strucOrTableName != null) {
					record = exportParameterList.getTable(sapFunctionParameterColumn.getStructureOrTableName());
					if (record != null) {
						return getTableValue(sapFunctionParameterColumn, (Table) record);
					}
				}
			}
		} else if ((param.equals("table")) && (subParam.equals("output"))) {
			strucOrTableName = sapFunctionParameterColumn.getStructureOrTableName();
			if (strucOrTableName != null) {
				record = tableParameterList.getTable(sapFunctionParameterColumn.getStructureOrTableName());
				if (record != null) {
					return getTableValue(sapFunctionParameterColumn, (Table) record);
				}
			}
		}
		return sapFunctionParameterColumn.getValue();
	}

	/**
	 * @param sapFunctionParameterColumn
	 * @param structure
	 * @return
	 */
	private Object getStructureValue(SAPFunctionParameterColumn sapFunctionParameterColumn, Structure structure) {
		try {
			return structure.getValue(sapFunctionParameterColumn.getName());
		} catch (Exception exception) {
			ExceptionHandler.process(exception);
		}
		return null;
	}

	/**
	 * @param sapFunctionParameterColumn
	 * @param table
	 * @return
	 */
	private Object getTableValue(SAPFunctionParameterColumn sapFunctionParameterColumn, Table table) {
		try {
			return table.getValue(sapFunctionParameterColumn.getName());
		} catch (Exception exception) {
			ExceptionHandler.process(exception);
		}
		return null;
	}

	/**
	 * @param sapFunctionParameterColumn
	 * @param parameterList
	 * @return
	 */
	private Object getParameterValue(SAPFunctionParameterColumn sapFunctionParameterColumn, ParameterList parameterList) {
		try {
			return parameterList.getValue(sapFunctionParameterColumn.getName());
		} catch (Exception exception) {
			ExceptionHandler.process(exception);
		}
		return null;
	}

	/**
	 * @param sapTestInputParameterTable
	 * @param function
	 * @throws Throwable
	 */
	public void executeSAPTable(SAPTestInputParameterTable sapTestInputParameterTable, Function function)
			throws Throwable {
		if ((sapTestInputParameterTable != null) && (sapTestInputParameterTable.getColumns() != null)
				&& (!sapTestInputParameterTable.getColumns().isEmpty())) {
			ParameterList importParameterList = function.getImportParameterList();
			((ParameterList) importParameterList).clear();
			ParameterList tableParameterList = function.getTableParameterList();
			tableParameterList.clear();
			for (int i = 0; i < sapTestInputParameterTable.getColumns().size(); i++) {
				SAPFunctionParameterColumn sapFunctionParameterColumn = (SAPFunctionParameterColumn) sapTestInputParameterTable
						.getColumns().get(i);
				String paramType = sapFunctionParameterColumn.getParameterType();
				if (paramType.startsWith("output")) {
					continue;
				}
				String param1 = paramType.substring(0, paramType.indexOf('.'));
				String param2 = paramType.substring(paramType.indexOf('.') + 1);
				String structOrTableName;
				Object localObject3;
				if (param1.equals("input")) {
					if (param2.equals("single")) {
						((ParameterList) importParameterList).setValue(sapFunctionParameterColumn.getValue(),
								sapFunctionParameterColumn.getName());
					} else if (param2.equals("structure")) {
						structOrTableName = sapFunctionParameterColumn.getStructureOrTableName();
						if (structOrTableName == null) {
							continue;
						}
						localObject3 = ((ParameterList) importParameterList).getStructure(sapFunctionParameterColumn
								.getStructureOrTableName());
						if (localObject3 == null)
							continue;
						((Structure) localObject3).setValue(sapFunctionParameterColumn.getValue(),
								sapFunctionParameterColumn.getName());
					} else {
						if (!param2.equals("table"))
							continue;
						structOrTableName = sapFunctionParameterColumn.getStructureOrTableName();
						if (structOrTableName == null)
							continue;
						localObject3 = ((ParameterList) importParameterList).getTable(sapFunctionParameterColumn
								.getStructureOrTableName());
						if (localObject3 == null)
							continue;
						((Table) localObject3).setValue(sapFunctionParameterColumn.getValue(),
								sapFunctionParameterColumn.getName());
					}
				} else {
					if ((!param1.equals("table")) || (!param2.equals("input")))
						continue;
					structOrTableName = sapFunctionParameterColumn.getStructureOrTableName();
					if (structOrTableName == null)
						continue;
					localObject3 = tableParameterList.getTable(sapFunctionParameterColumn.getStructureOrTableName());
					if (localObject3 == null)
						continue;
					((Table) localObject3).appendRow();
					((Table) localObject3).setValue(sapFunctionParameterColumn.getValue(), sapFunctionParameterColumn
							.getName());
				}
			}
		}
		Object localObject1 = null;
		try {
			localObject1 = JCO.getClient("R3");
			((Client) localObject1).execute(function);
			JCO.releaseClient((Client) localObject1);
		} catch (Throwable localThrowable) {
			if (localObject1 != null)
				JCO.releaseClient((Client) localObject1);
			throw localThrowable;
		} finally {
			if (localObject1 != null)
				JCO.releaseClient((Client) localObject1);
		}
	}

	/**
	 * @param functionName
	 * @return
	 * @throws Exception
	 */
	public Function getTableByName(String functionName) throws Exception {
		if ((functionName == null) || (functionName.equals("")))
			return null;
		try {
			IFunctionTemplate functionTemplate = this.repository.getFunctionTemplate(functionName);
			if (functionTemplate != null)
				return functionTemplate.getFunction();
		} catch (Exception localException) {
			throw localException;
		}
		return null;
	}

	/**
	 * @param functionName
	 * @param goupName
	 * @return
	 * @throws java.lang.Exception
	 */
	public List<SapTableDescription> listTables(String functionName, String goupName) throws java.lang.Exception {
		IFunctionTemplate functionTemplate = this.repository.getFunctionTemplate("RFC_FUNCTION_SEARCH");
		Function function = functionTemplate.getFunction();
		if (function == null) {
			throw new java.lang.Exception(Messages.getString("JCOClientManager.UnsupportFunction"));
		}
		ParameterList importParameterList = function.getImportParameterList();
		if ((functionName != null) && (!functionName.equals(""))) {
			importParameterList.setValue(functionName, "FUNCNAME");
		}
		if ((goupName != null) && (!goupName.equals(""))) {
			importParameterList.setValue(goupName, "GROUPNAME");
		}
		Client client = JCO.getClient("R3");
		client.execute(function);
		Table table = function.getTableParameterList().getField(0).getTable();
		List<SapTableDescription> functionDiscList = new ArrayList<SapTableDescription>();
		do {
			SapTableDescription functionDescription = new SapTableDescription(table.getString(0), table
					.getString(4));
			functionDiscList.add(functionDescription);
		} while (table.nextRow());
		return functionDiscList;
	}
}