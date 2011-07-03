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
package org.talend.designer.core.runprocess;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.StringUtils;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.properties.SnippetItem;
import org.talend.core.model.properties.SnippetVariable;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.snippet.VariableItemHelper;
import org.talend.designer.core.DesignerPlugin;

/**
 * DOC bqian class global comment. Detailled comment <br/>
 * 
 */
public class SnippetParser {

    public static String systemEOL = VariableItemHelper.systemEOL;

    /**
     * store the snippet temporarily <br/>
     * 
     */
    class SnippetStore {

        String id = null;

        String name = null;

        Map<String, String> variables = new HashMap<String, String>();
    }

    public List<String> findFirstSnippetId(String allSnippetString) {
        List<String> resultList = new ArrayList<String>();
        String snippetId = null;
        String snippetName = null;
        String vString = null;
        try {
            Pattern regex = Pattern.compile(
                    "(?:/\\*|#)SNIPPET_START:(\\w+)(?:\\r\\n|\\n)((?:#*\\w+=.+(?:\\r\\n|\\n))+)#*\\{ID\\}=(\\w+)", //$NON-NLS-1$
                    Pattern.CANON_EQ);
            Matcher regexMatcher = regex.matcher(allSnippetString);
            regexMatcher.find();
            snippetName = regexMatcher.group(1);
            vString = regexMatcher.group(2);
            snippetId = regexMatcher.group(3);
            resultList.add(snippetId);
            resultList.add(snippetName);
            String[] multiVarible = vString.split(systemEOL);
            for (int i = 0; i < multiVarible.length; i++) {
                resultList.add(StringUtils.replace(multiVarible[i], "#", "")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (RuntimeException ex) {
        }
        return resultList;
    }

    public String replaceFristSnippet(String replaceString, String allSnappetString) {
        StringBuffer resultString = new StringBuffer();
        try {
            Pattern regex = Pattern
                    .compile("(/\\*|#)SNIPPET_START(.*(\\r\\n|\\n))+?(/\\*|#)SNIPPET_END(\\*/)*", Pattern.CANON_EQ); //$NON-NLS-1$
            Matcher regexMatcher = regex.matcher(allSnappetString);
            regexMatcher.find();
            replaceString = StringUtils.replace(replaceString, "$", "\\$"); //$NON-NLS-1$ //$NON-NLS-2$
            try {
                regexMatcher.appendReplacement(resultString, replaceString);
            } catch (IllegalStateException ex) {
                // appendReplacement() called without a prior successful call to find()
            } catch (IllegalArgumentException ex) {
                // ex.printStackTrace();
                ExceptionHandler.process(ex);
            } catch (IndexOutOfBoundsException ex) {
                // Non-existent backreference used the replacement text
            }
            regexMatcher.appendTail(resultString);
        } catch (PatternSyntaxException ex) {
            // Syntax error in the regular expression
        }
        return resultString.toString();
    }

    private SnippetStore convert2SnippetModel(List<String> list) {
        SnippetStore snippet = new SnippetStore();
        snippet.id = list.get(0);
        snippet.name = list.get(1);
        list.remove(0);
        list.remove(0);
        for (String string : list) {
            String[] para = string.split("="); //$NON-NLS-1$
            snippet.variables.put(para[0].trim(), para[1].trim());
        }
        return snippet;
    }

    public String convertSnippet(String processCode) {
        while (true) {
            List<String> resultList = findFirstSnippetId(processCode);

            if (resultList.isEmpty()) {
                break;
            }

            SnippetStore store = convert2SnippetModel(resultList);
            SnippetItem item = findSnippet(store);
            String snippetString = generateSnippetString(item, store);

            if (snippetString != null) {
                processCode = replaceFristSnippet(snippetString, processCode);
            } else {
                processCode = replaceFristSnippet("", processCode); //$NON-NLS-1$
            }
        }
        return processCode;
    }

    /**
     * bqian Comment method "generateSnippetString".
     * 
     * @param item
     * @param store
     * @return
     */
    private String generateSnippetString(SnippetItem item, SnippetStore store) {
        String code = getInsertSnippetCode(item, store);
        StringBuilder sb = new StringBuilder();
        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL)) {
            sb.append("#SNIPPET_GENERATED_START:{0}"); //$NON-NLS-1$
            sb.append(systemEOL);
            sb.append("{1}"); //$NON-NLS-1$
            sb.append(systemEOL);
            sb.append("#SNIPPET_GENERATED_END"); //$NON-NLS-1$
        } else {
            // Java comment format
            sb.append("/*SNIPPET_GENERATED_START:{0}*/"); //$NON-NLS-1$
            sb.append(systemEOL);
            sb.append("{1}"); //$NON-NLS-1$
            sb.append(systemEOL);
            sb.append("/*SNIPPET_GENERATED_END*/"); //$NON-NLS-1$
        }
        // StringBuilder b = new StringBuilder();
        // b.append("(");
        // for (SnippetVariable var : (List<SnippetVariable>) item.getVariables()) {
        // b.append(var.getValue()).append(",");
        // }
        // b.deleteCharAt(b.length() - 1);
        // b.append(")");
        String snippetDefinition = item.getProperty().getLabel() + " ID=" + item.getProperty().getId(); //$NON-NLS-1$

        String msg = sb.toString();
        MessageFormat format = new MessageFormat(msg);
        Object[] args = new Object[] { snippetDefinition, code }; //$NON-NLS-1$
        msg = format.format(args);

        return msg;
    }

    /**
     * Caculate the code for snippet.
     * 
     * @param item
     * @return
     */
    private String getInsertSnippetCode(SnippetItem fItem, SnippetStore store) {
        // this could be horribly inefficient
        String text = fItem.getContent();
        List<SnippetVariable> variables = (List<SnippetVariable>) fItem.getVariables();

        for (int i = 0; i < variables.size(); i++) {
            SnippetVariable var = variables.get(i);
            String varName = var.getName();
            String varValue = null;
            if (store.variables.get(varName) != null) {
                varValue = store.variables.get(varName);
            } else {
                varValue = var.getValue();
            }
            text = StringUtils.replace(text, "${" + var.getName() + "}", varValue); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // remove all cursor markers
        text = StringUtils.replace(text, "${cursor}", ""); //$NON-NLS-1$ //$NON-NLS-2$

        // Update EOLs (bug 80231)
        text = StringUtils.replace(text, "\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        text = StringUtils.replace(text, "\r", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        if (!"\n".equals(systemEOL) && systemEOL != null) { //$NON-NLS-1$
            text = StringUtils.replace(text, "\n", systemEOL); //$NON-NLS-1$
        }
        return text;
    }

    private SnippetItem findSnippet(SnippetStore store) {
        try {
            RootContainer<String, IRepositoryViewObject> snippets = DesignerPlugin.getDefault().getRepositoryService()
                    .getProxyRepositoryFactory().getMetadata(ERepositoryObjectType.SNIPPETS);
            List<IRepositoryViewObject> objects = snippets.getMembers();

            SnippetItem item = null;
            for (IRepositoryViewObject repositoryObject : objects) {
                item = (SnippetItem) repositoryObject.getProperty().getItem();
                if (item.getProperty().getId().equals(store)) {
                    break;
                }
            }
            return item;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

}
