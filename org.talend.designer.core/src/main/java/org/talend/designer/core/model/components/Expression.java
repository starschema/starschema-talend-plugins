// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.commons.utils.StringUtils;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;

import com.ibm.icu.util.StringTokenizer;

/**
 * This class will test an expression in the element parameters. <br>
 * The expression can be for example: <br>
 * ((VAR1 == 'value1' and VAR2 == 'value2') or (VAR3 != 'value3')) or (VAR4 == 'value4') <br>
 * With VAR1, VAR2, VAR3 & VAR4 as the name of differents parameters and 'value1'.. the values to test. (values must be
 * between quotes)<br>
 * 
 * $Id: Expression.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public final class Expression {

    private Expression leftExpression;

    private Expression rightExpression;

    private String condition; // and / or

    private String expressionString;

    private boolean valid;

    private static final String AND = "and"; //$NON-NLS-1$

    private static final String OR = "or"; //$NON-NLS-1$

    private static final String EQUALS = "=="; //$NON-NLS-1$

    private static final String NOT_EQUALS = "!="; //$NON-NLS-1$

    // private static ElementParameter currentParam;

    private Expression(String expressionString) {
        this.expressionString = expressionString;
    }

    private String getExpressionString() {
        return this.expressionString;
    }

    private void setExpressionString(String value) {
        this.expressionString = value;
    }

    private String getCondition() {
        return this.condition;
    }

    private void setCondition(String condition) {
        this.condition = condition;
    }

    private Expression getLeftExpression() {
        return this.leftExpression;
    }

    private void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    private Expression getRightExpression() {
        return this.rightExpression;
    }

    private void setRightExpression(Expression rightExpression) {
        this.rightExpression = rightExpression;
    }

    private boolean isValid() {
        return this.valid;
    }

    private void setValid(boolean value) {
        this.valid = value;
    }

    public static boolean evaluate(final String string, List<? extends IElementParameter> listParam) {
        return evaluate(string, listParam, null);
    }

    public static boolean evaluate(final String string, List<? extends IElementParameter> listParam, ElementParameter curParam) {
        // currentParam = curParam;
        if (string.contains("(") //$NON-NLS-1$
                && (isThereCondition(string, AND) || isThereCondition(string, OR))) {
            return evaluateExpression(new Expression(string), listParam, curParam).isValid();
        } else {
            String newValue; // remove brackets
            newValue = string.replace("(", ""); //$NON-NLS-1$ //$NON-NLS-2$
            newValue = newValue.replace(")", ""); //$NON-NLS-1$ //$NON-NLS-2$
            return evaluateSimpleExpression(newValue, listParam, curParam);
        }

    }

    public static boolean isThereCondition(String expression, String condition) {
        // example for the reg exp: (.*)[')][ ]*or[ ]*[\w(](.*)
        String refixReg = "(.*)[') ][ ]*"; //$NON-NLS-1$
        String suffixReg = "[ ]*[ (](.*)"; //$NON-NLS-1$
        if (expression.matches(refixReg + condition + suffixReg)) {
            return true;
        }
        if (expression.matches(refixReg + condition.toUpperCase() + suffixReg)) {
            return true;
        }
        return false;
    }

    private static boolean evaluateSimpleExpression(String simpleExpression, List<? extends IElementParameter> listParam,
            ElementParameter currentParam) {
        boolean showParameter = false;
        String test = null;
        if (simpleExpression.contains(EQUALS)) {
            test = EQUALS;
        } else {
            if (simpleExpression.contains(NOT_EQUALS)) {
                test = NOT_EQUALS;
            }
        }

        String[] strings = null;
        if (test != null) {
            strings = simpleExpression.split(test);
        } else {
            strings = new String[] { simpleExpression };
        }

        String variableName = null, variableValue = null;

        for (int i = 0; i < strings.length; i++) {
            String string = strings[i].trim();
            if (string.contains("'")) { // value //$NON-NLS-1$
                variableValue = string;
                variableValue = variableValue.substring(1, string.lastIndexOf("'")); //$NON-NLS-1$
            } else {
                variableName = string;
            }
        }
        /*
         * this is only for Current OS condition.
         */
        if (variableName != null && EParameterName.CURRENT_OS.getName().equals(variableName)) {
            if (variableValue != null) {
                if (checkCurrentOS(variableValue) && EQUALS.endsWith(test)) {
                    return true;
                } else if (NOT_EQUALS.equals(test)) {
                    return true;
                }
            }
        }
        if (listParam == null) {
            return false;
        }

        // 3 levels of variable name accepted maximum (ex: MY_VAR.TABLE.FIELD == 'test')
        String[] varNames;
        StringTokenizer token = new StringTokenizer(variableName, "."); //$NON-NLS-1$
        varNames = StringUtils.split(variableName, '.');

        // wyang: only for bug:9055, to search the related Node, for example tFTPGet wants to check tFTPConnection info
        // variableName like: #LINK@NODE.CONNECTION.SFTP ----->it is a checkbox in tFTPConnection
        // #LINK@NODE, #PREVIOUS@NODE, #NEXT@NODE ----->implement them later
        if ((variableName != null) && (variableValue != null)) {
            if (varNames[0].equals("#LINK@NODE")) {
                if (currentParam.getElement() instanceof INode) {
                    INode node = (INode) currentParam.getElement();
                    String relatedNodeName = ElementParameterParser.getValue(node, "__" + varNames[1] + "__");
                    List<? extends INode> generatingNodes = node.getProcess().getGeneratingNodes();
                    for (INode aNode : generatingNodes) {
                        if (aNode.getUniqueName().equals(relatedNodeName)) {
                            simpleExpression = simpleExpression.replace(varNames[0] + "." + varNames[1] + ".", "");
                            List<? extends IElementParameter> elementParameters = aNode.getElementParameters();
                            // let's supose the currentParam = null, there won't want deal with the TABLE field, only
                            // deal with LIST/CHECKBOX
                            return evaluate(simpleExpression, elementParameters);

                        }
                    }

                }
            }
        }

        if ((variableName != null) && (variableValue != null)) {
            for (IElementParameter param : listParam) {

                if (param.getName().equals(varNames[0])) {
                    IElementParameter testedParameter = param;
                    Object value = null;
                    boolean found = false;
                    if (param.getFieldType().equals(EParameterFieldType.TABLE)) {
                        List<Map<String, Object>> tableValues = (List<Map<String, Object>>) param.getValue();
                        Map<String, Object> currentRow = tableValues.get(currentParam.getCurrentRow());
                        if (currentRow.containsKey(varNames[1])) {
                            for (Object curObj : param.getListItemsValue()) {
                                if (curObj instanceof IElementParameter) {
                                    IElementParameter testParam = (IElementParameter) curObj;
                                    if (testParam.getName().equals(varNames[1])) {
                                        testedParameter = testParam;
                                        break;
                                    }
                                }
                            }
                            if (varNames.length == 2) { // simple value
                                value = currentRow.get(varNames[1]);
                            } else {
                                if ("TYPE".equals(varNames[2])) { //$NON-NLS-1$
                                    IMetadataTable baseTable = null;
                                    IMetadataColumn baseColumn = null;
                                    INode node;
                                    Object obj = currentRow.get(testedParameter.getName());
                                    String columnName = ""; //$NON-NLS-1$
                                    if (obj instanceof String) {
                                        columnName = (String) obj;
                                    } else if (obj instanceof Integer) {
                                        columnName = testedParameter.getListItemsDisplayName()[(Integer) obj];
                                    }
                                    if (currentParam.getElement() instanceof INode) {
                                        node = (INode) currentParam.getElement();

                                        switch (testedParameter.getFieldType()) {
                                        case COLUMN_LIST:
                                            baseTable = node.getMetadataList().get(0);
                                            break;
                                        case PREV_COLUMN_LIST:
                                            IConnection connection = null;
                                            for (int i = 0; i < node.getIncomingConnections().size(); i++) {
                                                IConnection curConnec = node.getIncomingConnections().get(i);
                                                if (curConnec.getLineStyle() == EConnectionType.FLOW_MAIN) {
                                                    connection = curConnec;
                                                    break;
                                                }
                                            }
                                            if (connection != null) {
                                                baseTable = connection.getMetadataTable();
                                            }
                                            break;
                                        case LOOKUP_COLUMN_LIST:
                                            List<IConnection> refConnections = new ArrayList<IConnection>();
                                            for (int i = 0; i < node.getIncomingConnections().size(); i++) {
                                                IConnection curConnec = node.getIncomingConnections().get(i);
                                                if (curConnec.getLineStyle() == EConnectionType.FLOW_REF) {
                                                    refConnections.add(curConnec);
                                                }
                                            }
                                            for (IConnection curConnec : refConnections) {
                                                IMetadataTable table = curConnec.getMetadataTable();
                                                for (IMetadataColumn column : table.getListColumns()) {
                                                    String name = curConnec.getName() + "." + column.getLabel(); //$NON-NLS-1$
                                                    if (name.equals(columnName)) {
                                                        baseColumn = column;
                                                    }
                                                }
                                            }
                                            break;
                                        default:
                                        }

                                        if (baseTable != null) {
                                            for (IMetadataColumn column : baseTable.getListColumns()) {
                                                if (column.getLabel().equals(columnName)) {
                                                    baseColumn = column;
                                                    break;
                                                }
                                            }
                                        }

                                        if (baseColumn != null) {
                                            switch (LanguageManager.getCurrentLanguage()) {
                                            case JAVA:
                                                value = JavaTypesManager.getTypeToGenerate(baseColumn.getTalendType(), baseColumn
                                                        .isNullable());
                                                break;
                                            default:
                                                value = baseColumn.getTalendType();
                                            }
                                            if (value.equals(variableValue)) {
                                                found = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (param.getFieldType().equals(EParameterFieldType.PROPERTY_TYPE)
                            || param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                            || param.getFieldType().equals(EParameterFieldType.QUERYSTORE_TYPE)
                            || param.getFieldType().equals(EParameterFieldType.ENCODING_TYPE)) {

                        boolean child = false;
                        Map<String, IElementParameter> childParameters = param.getChildParameters();

                        if ("PROPERTY".equals(param.getName())) {
                            if (childParameters != null) {
                                IElementParameter iElementParameter = childParameters.get("PROPERTY_TYPE");
                                if (iElementParameter != null) {
                                    Object value2 = iElementParameter.getValue();
                                    if (variableValue.equals(value2.toString())) {
                                        child = true;
                                        found = true;
                                        value = value2.toString();
                                    }
                                }
                            }
                        }

                        if (varNames.length > 1 && varNames[1] != null) {
                            IElementParameter tempParam = childParameters.get(varNames[1]);
                            if (tempParam != null) {
                                value = tempParam.getValue();
                                if (value.equals(variableValue)) {
                                    found = true;
                                }
                                child = true;
                            }
                        }
                        if (!child) {
                            value = testedParameter.getValue();
                        }
                    } else {
                        value = testedParameter.getValue();
                    }
                    if (value instanceof Integer) {
                        if (((Integer) value) < testedParameter.getListItemsValue().length) {
                            value = testedParameter.getListItemsValue()[(Integer) value];
                        }
                    }
                    if (value instanceof String) {
                        if (testedParameter.getListItemsValue() instanceof Object[]) {
                            Object[] values = (Object[]) testedParameter.getListItemsValue();
                            for (int i = 0; i < values.length && !found; i++) {
                                if (values[i].equals(value)) {
                                    String variableCode = testedParameter.getListItemsDisplayCodeName()[i];
                                    if (variableCode.equals(variableValue)) {
                                        found = true;
                                    }
                                }
                            }
                        }
                    } else if (value instanceof Boolean) {
                        Boolean tmpValue = new Boolean(variableValue);
                        if (tmpValue.equals(value)) {
                            found = true;
                        }
                    }

                    if (found) {
                        if (test.equals(EQUALS)) {
                            showParameter = true;
                        }
                    } else {
                        if (test.equals(NOT_EQUALS)) {
                            showParameter = true;
                        }
                    }

                }
            }
        }
        return showParameter;
    }

    private static Expression evaluateExpression(Expression expression, List<? extends IElementParameter> listParam,
            ElementParameter currentParam) {
        int indexBegining = 0, indexEnd;
        int expressionLevel = 0;
        String string = expression.getExpressionString();
        boolean conditionFound = false;

        // if there's no braket then there should be only simple expression
        // or only one expression.
        for (int i = 0; i < string.length() && !conditionFound; i++) {
            if (string.charAt(i) == '(') {
                if (expressionLevel == 0) {
                    indexBegining = i + 1;
                }
                expressionLevel++;
            } else if (string.charAt(i) == ')') {
                expressionLevel--;
                indexEnd = i;

                if (expressionLevel == 0) {
                    if (isThereCondition(string, AND) || isThereCondition(string, OR)) {
                        String leftString = string.substring(indexBegining, indexEnd).trim();
                        if (isThereCondition(leftString, AND) || isThereCondition(leftString, OR)) {
                            Expression leftExpression = new Expression(leftString);
                            expression.setLeftExpression(leftExpression);
                            evaluateExpression(leftExpression, listParam, currentParam);
                        } else {
                            Expression leftExpression = new Expression(leftString);
                            expression.setLeftExpression(leftExpression);
                            leftExpression.setValid(evaluateSimpleExpression(leftString, listParam, currentParam));
                            // debug: System.out.println(leftString + " => " +
                            // leftExpression.isValid());
                        }
                    } else {
                        String newValue; // remove brackets
                        newValue = string.replace("(", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        newValue = newValue.replace(")", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        expression.setExpressionString(newValue);
                        expression.setValid(evaluateSimpleExpression(newValue, listParam, currentParam));
                    }
                }
            } else if (expressionLevel == 0) {
                if ((string.indexOf(AND, i) == i) || (string.indexOf(AND.toUpperCase(), i) == i)) {
                    String subStr = string.substring(i - 3, i + 5);
                    if (isThereCondition(subStr, AND)) {
                        expression.setCondition(AND);
                        conditionFound = true;
                    }
                } else if ((string.indexOf(OR, i) == i) || (string.indexOf(OR.toUpperCase(), i) == i)) {
                    String subStr = string.substring(i - 3, i + 5);
                    if (isThereCondition(subStr, OR)) {
                        expression.setCondition(OR);
                        conditionFound = true;
                    }
                }
            }
            if (conditionFound) {
                if (expression.getLeftExpression() == null) { // no bracket ==
                    // evaluate
                    // expression
                    String leftString = string.substring(0, i - 1).trim();
                    Expression leftExpression = new Expression(leftString);
                    expression.setLeftExpression(leftExpression);
                    leftExpression.setValid(evaluateSimpleExpression(leftString, listParam, currentParam));
                    // debug: System.out.println(leftString + " => " +
                    // leftExpression.isValid());
                }
                String rightString = string.substring(i + 3, string.length()).trim();
                Expression rightExpression = new Expression(rightString);
                expression.setRightExpression(rightExpression);
                if (rightString.contains("(") //$NON-NLS-1$
                        || isThereCondition(rightString, AND) || isThereCondition(rightString, OR)) {
                    evaluateExpression(rightExpression, listParam, currentParam);
                } else { // no bracket == evaluate expression
                    rightExpression.setValid(evaluateSimpleExpression(rightString, listParam, currentParam));
                    // debug: System.out.println(rightString + " => " +
                    // rightExpression.isValid());
                }
                if (expression.getCondition().equals(AND)) {
                    if (expression.getLeftExpression().isValid() && expression.getRightExpression().isValid()) {
                        expression.setValid(true);
                    } else {
                        expression.setValid(false);
                    }
                } else if (expression.getCondition().equals(OR)) {
                    if (expression.getLeftExpression().isValid() || expression.getRightExpression().isValid()) {
                        expression.setValid(true);
                    } else {
                        expression.setValid(false);
                    }
                }
            }
        }
        // if after an expression between bracket there's no other expression,
        // then the validation of the expression
        // will depends on the "left" expression.
        if ((expression.getRightExpression() == null) && (expression.getLeftExpression() != null)) {
            expression.setValid(expression.getLeftExpression().isValid());
        }
        // debug: System.out.println(expression.getExpressionString() + " => " +
        // expression.isValid());
        return expression;
    }

    /*
     * check this OS
     */
    private static boolean checkCurrentOS(final String osName) {
        if (osName == null) {
            return false;
        }
        final String tmpOSName = osName.toLowerCase();
        // check windows
        if (EnvironmentUtils.isWindowsSystem() && tmpOSName.startsWith("windows")) { //$NON-NLS-1$
            return true;
        }
        // check MacOS
        if (EnvironmentUtils.isMacOsSytem() && tmpOSName.startsWith("mac")) { //$NON-NLS-1$
            return true;
        }
        // check linux and unix
        if (EnvironmentUtils.isLinuxUnixSystem()) {
            if (tmpOSName.startsWith("unix") || tmpOSName.startsWith("linux")) { //$NON-NLS-1$ //$NON-NLS-2$
                return true;
            }
        }
        return false;
    }

}
