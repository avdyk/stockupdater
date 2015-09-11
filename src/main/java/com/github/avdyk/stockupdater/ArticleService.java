package com.github.avdyk.stockupdater;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Services de l'application.
 *
 * @author <a href="mailto:avd@mims.be">Arnaud Vandyck</a>
 * @since 24/08/2015 13:53
 */
public interface ArticleService {

  void setWorkbook(final XSSFWorkbook workbook);

  XSSFWorkbook getWorkbook();

  List<String> getSheetsName();

  void setSelectedSheetName(final String sheetName);

  String getSelectedSheetName();

  XSSFSheet getSelectedSheet();

  List<String> getColumnsName();

  void setSelectedColumn(final String columnName);

  String getSelectedColumnName();

  Map<Long, Set<Integer>> getIdsWithLineNumbersIndexes();

  Set<Integer> findIdInAllColumnsInTheSheet(final long id);

  void setSelectedLabelColumn(final String newValue);

  String getSelectedLabelColumn();

}
