package com.github.avdyk.stockupdater;

import com.github.avdyk.stockupdater.ui.javafx.MainPresentationModel;
import org.apache.commons.lang3.StringUtils;
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

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
        final Set<Integer> rows;
        if (inService.getIdsWithLineNumbersIndexes().isEmpty()) {
          rows = inService.findIdInAllColumnsInTheSheet(id);
        } else {
          rows = inService.getIdsWithLineNumbersIndexes().get(id);
        }
        if (rows != null && !rows.isEmpty()) {
          updateStock(updateType, rows, quantity);
          final String label;
          if (presentationModel.getLabelColumn() != null) {
            final int labelColIndex = inService.getColumnsName().indexOf(this.inService.getSelectedLabelColumn());
            final StringJoiner joiner = new StringJoiner(";", "(", ")");
            for (int r : rows) {
              joiner.add(inService.getSelectedSheet().getRow(r).getCell(labelColIndex).getStringCellValue());
            }
            label = joiner.toString();
          } else {
            label = "NO LABEL SELECTED";
          }
          final String msg = String.format("Article found %s: %d", label, id);
          LOG.info(FOUND, msg);
          // TODO try to use a log appender
          presentationModel.setLogOutput(presentationModel.getLogOutput() + msg + "\n");
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
    final Integer outIndex;
    if (StringUtils.isNotBlank(this.outService.getSelectedColumnName())) {
      outIndex = this.outService.getColumnsName().indexOf(this.outService.getSelectedColumnName());
    } else {
      outIndex = null;
    }
    final Integer stockIndex;
    if (StringUtils.isNotBlank(this.stockService.getSelectedColumnName())) {
      stockIndex = this.stockService.getColumnsName().indexOf(this.stockService.getSelectedColumnName());
    } else {
      stockIndex = null;
    }
    rows.stream().filter(r -> r != null)
        .forEach(row -> {
          final long originalStock;
          if (stockIndex != null) {
            originalStock = excelUtilService.getLongValueFromCell(
                this.inService.getSelectedSheet().getRow(row).getCell(stockIndex));
          } else {
            originalStock = 0;
          }
          final XSSFRow r = this.outService.getSelectedSheet().getRow(row);
          final XSSFCell cell;
          if (outIndex != null) {
            if (r.getCell(outIndex) != null) {
              cell = r.getCell(outIndex);
            } else {
              cell = r.createCell(outIndex);
            }
          } else {
            cell = null;
          }
          final double newValue;
          switch (updateType) {
            case ADD:
              newValue = originalStock + quantity;
              break;
            case SUBSTRACT:
              newValue = originalStock - quantity;
              break;
            case TEST:
            case UPDATE:
            default:
              newValue = quantity;
          }
          if (updateType != UpdateType.TEST && cell != null) {
            cell.setCellValue(newValue);
          }
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
        final String value;
        switch (cell.getCellType()) {
          case Cell.CELL_TYPE_NUMERIC:
            value = String.valueOf(cell.getNumericCellValue());
            break;
          case Cell.CELL_TYPE_STRING:
            value = cell.getStringCellValue();
            break;
          case Cell.CELL_TYPE_FORMULA:
            value = cell.getCellFormula();
            break;
          case Cell.CELL_TYPE_BOOLEAN:
            value = String.valueOf(cell.getBooleanCellValue());
            break;
          case Cell.CELL_TYPE_ERROR:
            value = String.format("Error #%d", cell.getErrorCellValue());
            break;
          case Cell.CELL_TYPE_BLANK:
          default:
            value = "";
            break;
        }
        s.append(value)
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
