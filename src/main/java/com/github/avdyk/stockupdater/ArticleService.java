package com.github.avdyk.stockupdater;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Services de l'application.
 *
 * @author <a href="mailto:avd@mims.be">Arnaud Vandyck</a>
 * @since 24/08/2015 13:53
 */
public interface ArticleService {

  // TODO new design
  void setWorkbook(final XSSFWorkbook workbook);

  XSSFWorkbook getWorkbook();

  List<String> getSheetsName();

  void setSelectedSheetName(final String sheetName);

  String getSelectedSheetName();

  /**
   * Pas nécessaire?..
   * @return
   * @deprecated nécessaire?
   */
  XSSFSheet getSelectedSheet();

  List<String> getColumnsName();

  // TODO old design
  @Deprecated
  List<String> getInSheetNames();

  @Deprecated
  void setInSelectedSheet(String selectedSheetName);

  @Deprecated
  XSSFSheet getInSelectedSheet();

  @Deprecated
  void setOutSelectedSheet(String selectedSheetName);

  @Deprecated
  XSSFSheet getOutSelectedSheet();

  @Deprecated
  List<String> getInColumnNames();

  @Deprecated
  String getStock();

  @Deprecated
  void setStock(String stock);

  @Deprecated
  String getOut();

  @Deprecated
  void setOut(String out);

  @Deprecated
  List<String> getIn();

  @Deprecated
  void setIn(List<String> in);

  @Deprecated
  void updateStock(UpdateType updateType,
                   Map<Long, Long> stock);

  /**
   * Save the sheet on the stream. The stream will not be closed!
   *
   * @param stream the stream.
   * @throws java.io.IOException if a problem with the stream.
   */
  @Deprecated
  void writeExcelWorkbook(OutputStream stream) throws IOException;
}
