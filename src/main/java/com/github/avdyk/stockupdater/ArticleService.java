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

  List<String> getSheetsName(final XSSFWorkbook workbookIn);

  List<String> getInSheetNames();

  void setInSelectedSheet(String selectedSheetName);

  XSSFSheet getInSelectedSheet();

  void setOutSelectedSheet(String selectedSheetName);

  XSSFSheet getOutSelectedSheet();

  List<String> getInColumnNames();

  String getStock();

  void setStock(String stock);

  String getOut();

  void setOut(String out);

  List<String> getIn();

  void setIn(List<String> in);

  void updateStock(UpdateType updateType,
                   Map<Long, Long> stock);

  /**
   * Save the sheet on the stream. The stream will not be closed!
   *
   * @param stream the stream.
   * @throws java.io.IOException if a problem with the stream.
   */
  void writeExcelWorkbook(OutputStream stream) throws IOException;
}
