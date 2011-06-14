// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.metadata.connection.files.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.Package;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.PackageHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * DOC yexiaowei class global comment. Detailled comment
 */
public class ExcelReader {

    private String excelPath = null;

    private Workbook workbook = null;

    private XSSFWorkbook xwb = null;

    private String[] sheetNamesForXlsx = null;

    private boolean isXlsx = false;

    public ExcelReader() {

    }

    /**
     * Maybe take long time
     * <p>
     * DOC yexiaowei ExcelReader constructor comment.
     * 
     * @param excel
     * @throws BiffException
     * 
     * @throws IOException
     */
    public ExcelReader(String excel) throws BiffException, IOException {
        this.excelPath = excel;
        init();
    }

    private void init() throws BiffException, IOException {
        // hywang modified for excel 2007
        if (excelPath.endsWith(".xls")) { //$NON-NLS-1$
            isXlsx = false;
        } else if (excelPath.endsWith(".xlsx")) { //$NON-NLS-1$
            isXlsx = true;
        }

        if (!isXlsx) {
            WorkbookSettings worksetting = new WorkbookSettings();
            //worksetting.setEncoding("ISO-8859-15"); //$NON-NLS-1$
            worksetting.setCellValidationDisabled(true); //$NON-NLS-1$
            worksetting.setSuppressWarnings(true); //$NON-NLS-1$
            workbook = Workbook.getWorkbook(new File(excelPath), worksetting);
        } else {
            // modify for bug 12174.
            File file = new File(excelPath);
            Package clone = null;
            try {
                FileInputStream in = new FileInputStream(file);
                Package open = Package.open(in);
                clone = PackageHelper.clone(open);
                open.close();

                // Package createPackage = Package.openOrCreate(file);
                // clone = PackageHelper.clone(createPackage);
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            } catch (OpenXML4JException e) {
                e.printStackTrace();
            }
            if (clone != null) {
                xwb = new XSSFWorkbook(clone);
                List<String> sheetlist = new ArrayList<String>();
                for (XSSFSheet sheet : xwb) {
                    sheetlist.add(sheet.getSheetName());
                }
                sheetNamesForXlsx = new String[sheetlist.size()];
                for (int i = 0; i < sheetlist.size(); i++) {
                    sheetNamesForXlsx[i] = sheetlist.get(i);
                }
                sheetlist.clear();
            }
        }

    }

    public String[] getSheetNames() {
        if (!this.isXlsx) {
            return workbook.getSheetNames();
        }
        return sheetNamesForXlsx;
    }

    public List<String[]> readSheet(String sheetName) {
        List<String[]> res = new ArrayList<String[]>();

        // hywang modified for excel 2007
        if (!this.isXlsx) {
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                return null;
            }

            int rows = sheet.getRows();

            for (int i = 0; i < rows; i++) {
                Cell[] cells = sheet.getRow(i);
                String[] contents = new String[cells.length];
                for (int j = 0, k = cells.length; j < k; j++) {
                    contents[j] = cells[j].getContents();
                }
                res.add(contents);
            }
        } else {

            XSSFSheet sheet = xwb.getSheet(sheetName);
            for (int i = sheet.getFirstRowNum(); i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    List<String> contents = new ArrayList<String>();
                    for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                        String cell = null;
                        if (row.getCell(j) != null && !row.getCell(j).equals("")) { //$NON-NLS-1$
                            cell = row.getCell(j).toString();
                        } else {
                            cell = ""; //$NON-NLS-1$
                        }
                        contents.add(cell);
                    }
                    Object[] objs = contents.toArray();
                    String[] rowContents = new String[objs.length];
                    for (int k = 0; k < rowContents.length; k++) {
                        rowContents[k] = objs[k].toString();
                    }
                    res.add(rowContents);
                }
            }
        }

        return res.size() <= 0 ? null : res;

    }

    /**
     * Getter for excelPath.
     * 
     * @return the excelPath
     */
    public String getExcelPath() {
        return this.excelPath;
    }

    public static String[] getColumnsTitle(int rows) {
        String[] x = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$
                "S", "T", "U", "V", "W", "X", "Y", "Z" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        if (rows <= 0) {
            return null;
        } else if (rows <= 26) {
            String[] res = new String[rows];
            System.arraycopy(x, 0, res, 0, rows);
            return res;
        } else if (rows < 26 * 26) {
            String[] res = new String[rows];
            System.arraycopy(x, 0, res, 0, 26);
            int offset = 26;
            FirstLoop: for (String first : x) {
                for (String second : x) {
                    String rowName = first + second;
                    res[offset] = rowName;
                    offset++;
                    if (offset == rows) {
                        break FirstLoop;
                    }
                }
            }
            return res;
        } else {
            return null;// Too much rows
        }
    }

    /**
     * DOC zli Comment method "getColumnsTitle".
     * 
     * @param s
     * @param rows
     * @return
     */
    public static String[] getColumnsTitle(int index, int rows) {
        if (index > 26 * 26 + 26 || index < 1) {
            return null;
        }
        if (index == 1) {
            return getColumnsTitle(rows);
        }
        String[] x = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$
                "S", "T", "U", "V", "W", "X", "Y", "Z" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$

        index = index - 1;
        int newRows = rows + index;

        if (rows <= 0) {
            return null;
        } else if (newRows <= 26) {
            String[] res = new String[rows];
            System.arraycopy(x, index, res, 0, rows);
            return res;

        } else if (newRows < 26 * 26) {
            String[] res = new String[newRows];
            System.arraycopy(x, 0, res, 0, 26);
            int offset = 26;
            FirstLoop: for (String first : x) {
                for (String second : x) {
                    String rowName = first + second;
                    res[offset] = rowName;
                    offset++;
                    if (offset == newRows) {
                        break FirstLoop;
                    }
                }
            }
            String[] res2 = new String[rows];
            System.arraycopy(res, index, res2, 0, rows);
            return res2;
        } else {
            return null;// Too much rows
        }
    }
}
