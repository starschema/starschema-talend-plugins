// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.skife.csv.CSVReader;
import org.skife.csv.SimpleReader;

/**
 * Detailled comment <br/>
 * 
 * @author ftang, 07-25-2007
 * 
 */
public class CsvArray {

    private static final String ENCODING = "ISO-8859-15"; //$NON-NLS-1$

    private List<String[]> rows;

    public CsvArray createFrom(File is) throws IOException {
        return createFrom(is, ENCODING);
    }

    /**
     * 
     * DOC YeXiaowei Comment method "createFrom".
     * 
     * @param is
     * @return
     * @throws IOException
     */
    public CsvArray createFrom(File is, final String encoding) throws IOException {
        CsvArray array = new CsvArray();

        String[] row = null;

        // CSVReader csvReader = new CsvReader(new BufferedReader(new InputStreamReader(new java.io.FileInputStream(is),
        // encoding == null ? ENCODING : encoding)), ';');
        // csvReader.setRecordDelimiter('\n');
        // csvReader.setSkipEmptyRecords(true);
        // csvReader.setTextQualifier('"');
        // csvReader.setEscapeMode(com.csvreader.CsvReader.ESCAPE_MODE_DOUBLED);
        // while (csvReader.readRecord()) {
        // array.add(csvReader.getValues());
        // }
        CSVReader csvReader = new SimpleReader();// (new FileReader(is), '\n');
        csvReader.setSeperator(';');
        List items = csvReader.parse(is);
        for (Object item : items) {
            array.add((String[]) item);
        }

        this.add(row);
        return array;
    }

    public void add(String[] row) {
        rows.add(row);
    }

    public List<String[]> getRows() {
        return rows;
    }

    /**
     * Constructs a new XmlArray.
     */
    public CsvArray() {
        super();
        rows = new ArrayList<String[]>();
    }

    // public static void main(String[] args) throws IOException {
    // CsvArray c = new CsvArray();
    // c = c.createFrom(new File("e:/testOraout.csv"));
    // System.out.println(c.getRows().size());
    // for (String[] string : c.getRows()) {
    // for (String string2 : string) {
    // System.out.print(string2 + " ");
    // }
    // System.out.println();
    //
    // }
    //
    // }
}
