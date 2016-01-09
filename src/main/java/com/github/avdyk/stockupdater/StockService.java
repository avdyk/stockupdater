package com.github.avdyk.stockupdater;

import com.github.avdyk.stockupdater.ui.javafx.MainPresentationModel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Service to compute and save the workbook.
 *
 * Created by arnaud on 25/08/15.
 */
public interface StockService {

  ArticleService getInService();

  ArticleService getIn2Service();

  ArticleService getStockService();

  ArticleService getOutService();

  void updateStock(UpdateType updateType, Map<Long, Long> stock, MainPresentationModel presentationModel);

  /**
   * Save the sheet on the stream. The stream will not be closed!
   *
   * @param stream the stream.
   * @throws java.io.IOException if a problem with the stream.
   */
  void writeExcelWorkbook(OutputStream stream) throws IOException;

  /**
   * Save the selected sheet to a CSV file. The stream will not be closed but it'll be fulshed!
   *
   * @param stream the stream.
   * @throws IOException if a problem with the stream.
   */
  void writeCSV(BufferedWriter stream) throws IOException;

  /**
   * Save the modified rows to a CSV file. The stream will not be closed but it'll be fulshed!
   *
   * @param stream the stream.
   * @throws IOException if a problem with the stream.
   */
  void writeModifiedRowsToCSV(BufferedWriter stream) throws IOException;

}
