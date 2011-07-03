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
package org.talend.core.model.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.INode;

/**
 * DOC YeXiaowei class global comment. Detailled comment <br/>
 * 
 */
public final class PerlVarParserUtils {

    public static final String PATTERN = "\\$row\\[\\w+\\]"; //$NON-NLS-1$

    private PerlVarParserUtils() {

    }

    /**
     * Rule see: feature 3725
     * <p>
     * DOC YeXiaowei Comment method "replaceSingleKeyString".
     * 
     * @param origin
     * @return
     */
    private static String replaceSingleKeyString(final String origin, final INode node) {

        String key = origin.substring("$row[".length(), origin.length() - 1); //$NON-NLS-1$
        StringBuilder builder = new StringBuilder("$"); // Perl //$NON-NLS-1$
        builder.append(node.getUniqueName());
        builder.append("->["); //$NON-NLS-1$

        int index = -1;

        if (node.getMetadataList() != null && !node.getMetadataList().isEmpty()) {
            IMetadataTable metaTable = node.getMetadataList().get(0);
            List<IMetadataColumn> columns = metaTable.getListColumns();
            if (columns != null && !columns.isEmpty()) {
                for (int i = 0; i < columns.size(); i++) {
                    IMetadataColumn column = columns.get(i);
                    if (column.getLabel().equals(key)) {
                        index = i;
                        break;
                    }
                }
            }
        }

        if (index == -1) { // Can't find appropriate match, return origin string
            return origin;
        }

        builder.append(index);
        builder.append("]"); //$NON-NLS-1$

        return builder.toString();
    }

    /**
     * Rule see: feature 3725
     * <p>
     * DOC YeXiaowei Comment method "findAndReplacesAll".
     * 
     * @param origin
     * @return
     */
    public static String findAndReplacesAll(final String origin, final INode node) {

        if (origin == null || origin.equals("")) { //$NON-NLS-1$
            return origin;
        }

        // find
        PatternCompiler compiler = new Perl5Compiler();

        try {
            Pattern pattern = compiler.compile(PATTERN, Perl5Compiler.CASE_INSENSITIVE_MASK);
            PatternMatcherInput input = new PatternMatcherInput(origin);
            PatternMatcher matcher = new Perl5Matcher();

            int lastMatch = 0;
            List<int[]> offsets = new ArrayList<int[]>();
            while (matcher.contains(input, pattern)) {
                MatchResult result = matcher.getMatch();
                int[] temp = { result.beginOffset(0), result.endOffset(0) };
                lastMatch = temp[1];
                offsets.add(temp);
            }

            List<KeyString> sepStrings = null;

            if (!offsets.isEmpty()) {
                sepStrings = new ArrayList<KeyString>();
                int previewEnd = 0;
                for (int[] x : offsets) {
                    int begin = x[0];
                    if (previewEnd < begin) {
                        KeyString nows = new KeyString(origin.substring(previewEnd, begin), false);
                        sepStrings.add(nows);
                    }
                    int end = x[1];
                    KeyString sep = new KeyString(origin.substring(begin, end), true);
                    sepStrings.add(sep);
                    previewEnd = end;
                }
            }

            // Last part string to replace
            if (sepStrings != null && origin.length() > lastMatch) {
                sepStrings.add(new KeyString(origin.substring(lastMatch), false));
            }

            // replace
            if (sepStrings != null && !sepStrings.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (KeyString keyString : sepStrings) {
                    if (keyString.isKey()) {
                        builder.append(replaceSingleKeyString(keyString.getString(), node));
                    } else {
                        builder.append(keyString.getString());
                    }
                }

                return builder.toString();
            }

        } catch (MalformedPatternException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

        return origin;
    }

    /**
     * 
     * DOC YeXiaowei KeyString class global comment. Detailled comment <br/>
     * 
     */
    private static final class KeyString {

        private String string = null;

        private boolean key = false;

        public KeyString(final String string, final boolean key) {
            this.string = string;
            this.key = key;
        }

        public boolean isKey() {
            return this.key;
        }

        public String getString() {
            return this.string;
        }
    }
}
