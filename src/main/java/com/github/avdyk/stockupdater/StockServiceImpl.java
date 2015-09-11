package com.github.avdyk.stockupdater;

import com.github.avdyk.stockupdater.ui.javafx.MainPresentationModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.time.LocalDateTime.*;

/**
 * Service to compute the sotck with the workbooks.
 * <p>
 * Created by arnaud on 25/08/15.
 */
@Service
public class StockServiceImpl implements StockService {

  private static final Logger LOG = LoggerFactory.getLogger(StockServiceImpl.class);
  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.S");
  private static final Marker FOUND = MarkerFactory.getMarker("FOUND");
  private static final Marker NOT_FOUND = MarkerFactory.getMarker("NOT_FOUND");

  @Autowired
  private ArticleService inService;
  @Autowired
  private ArticleService stockService;
  @Autowired
  private ArticleService outService;
  @Autowired
  private ExcelUtilServiceImpl excelUtilService;

  @Override
  public void updateStock(final UpdateType updateType,
                          final Map<Long, Long> stock,
                          final MainPresentationModel presentationModel) {

    if (stock == null) {
      throw new NullPointerException("Stock cannot be 'null'");
    }
    if (stock.isEmpty()) {
      throw new IllegalArgumentException("Stock cannot be empty");
    }
    if (updateType == null) {
      throw new NullPointerException("Update Type cannot be 'null'");
    }
    final String titleMsg = String.format("Updating stock at %s",
        now().format(dateFormatter));
    LOG.info(titleMsg);
    // TODO try to use a log appender
    presentationModel.setLogOutput(titleMsg + ":\n");
    stock.forEach((id, quantity) -> {
      if (id != null && quantity != null) {
        final Set<Integer> rows = inService.getIdsWithLineNumbersIndexes().get(id);
        if (rows != null && !rows.isEmpty()) {
          updateStock(updateType, rows, quantity);
          final String msg = String.format("Article found: %d", id);
          LOG.info(FOUND, msg);
        } else {
          final String msg = String.format("Article not found: %d", id);
          LOG.warn(NOT_FOUND, msg);
          // TODO try to use a log appender
          presentationModel.setLogOutput(presentationModel.getLogOutput() + msg + "\n");
        }
      }
    });
  }

  void updateStock(final UpdateType updateType, final Set<Integer> rows, final long quantity) {
    assert updateType != null;
    assert rows != null;
    assert !rows.isEmpty();
    final int outIndex = this.outService.getColumnsName().indexOf(this.outService.getSelectedColumnName());
    final int stockIndex = this.stockService.getColumnsName().indexOf(this.stockService.getSelectedColumnName());
    rows.stream().filter(r -> r != null)
        .forEach(row -> {
          final Long originalStock = excelUtilService.getLongValueFromCell(
              this.inService.getSelectedSheet().getRow(row).getCell(stockIndex));
          final XSSFRow r = this.outService.getSelectedSheet().getRow(row);
          XSSFCell cell = r.getCell(outIndex);
          if (cell == null) {
            cell = r.createCell(outIndex);
          }
          final double newValue;
          switch (updateType) {
            case UPDATE:
              newValue = quantity;
              break;
            case ADD:
              newValue = originalStock + quantity;
              break;
            case SUBSTRACT:
              newValue = originalStock - quantity;
              break;
            default:
              newValue = -1;
          }
          cell.setCellValue(newValue);
          LOG.info("Update stock for line {}: from {} to {}", row, originalStock, newValue);
        });
  }

  @Override
  public void writeExcelWorkbook(final OutputStream stream) throws IOException {
    this.outService.getWorkbook().write(stream);
  }

  @Override
  public void writeCSV(final BufferedWriter out) throws IOException {
    final XSSFSheet sheet = this.outService.getWorkbook().getSheet(this.outService.getSelectedSheetName());
    final Iterator<Row> rowIterator = sheet.rowIterator();
    while (rowIterator.hasNext()) {
      final Row row = rowIterator.next();
      final Iterator<Cell> cellIterator = row.cellIterator();
      final StringBuilder s = new StringBuilder();
      while (cellIterator.hasNext()) {
        final Cell cell = cellIterator.next();
        s.append(cell.getStringCellValue())
            .append(';');
      }
      if (s.length() > 1) {
        s.deleteCharAt(s.length() - 1);
      }
      out.write(s.toString());
      out.newLine();
    }
    out.flush();
  }

  @Override
  public ArticleService getInService() {
    return inService;
  }

  @Override
  public ArticleService getStockService() {
    return stockService;
  }

  @Override
  public ArticleService getOutService() {
    return outService;
  }

}
