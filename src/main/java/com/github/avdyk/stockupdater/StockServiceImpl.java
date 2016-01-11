package com.github.avdyk.stockupdater;

import com.github.avdyk.stockupdater.ui.javafx.MainPresentationModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.LocalDateTime.now;

/**
 * Service to compute the stock with the workbooks.
 * <p>
 * Created by arnaud on 25/08/15.
 */
@Service
public class StockServiceImpl implements StockService {

  private static final Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);
  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.S");
  private static final Marker FOUND = MarkerFactory.getMarker("FOUND");
  private static final Marker NOT_FOUND = MarkerFactory.getMarker("NOT_FOUND");

  @Autowired
  private ArticleService inService;
  @Autowired
  private ArticleService in2Service;
  @Autowired
  private ArticleService stockService;
  @Autowired
  private ArticleService outService;
  @Autowired
  private ExcelUtilServiceImpl excelUtilService;
  @Autowired
  private MainPresentationModel mainPresentationModel;
  private Map<Long, Long> stock;

  private Set<Integer> modifiedRows = new HashSet<>();

  @Override
  public void setStock(final Map<Long, Long> stock) {
    if (stock == null) {
      throw new NullPointerException("Stock cannot be 'null'");
    }
    if (stock.isEmpty()) {
      throw new IllegalArgumentException("Stock cannot be empty");
    }
    this.stock = stock;
    modifiedRows.clear();
  }

  private void addIfNotEmpty(final Set<Integer> rows, final Set<Integer> tempRows) {
    assert rows != null;
    if (tempRows != null && !tempRows.isEmpty()) {
      rows.addAll(tempRows);
    }
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
    if (StringUtils.isNotBlank(this.stockService.getSelectedColumnName())) {
      final Integer stockIndex = this.stockService.getColumnsName().indexOf(this.stockService.getSelectedColumnName());
      final XSSFSheet sheet = this.outService.getSelectedSheet();
      rows.stream().filter(r -> r != null)
          .forEach(row -> {
            final XSSFRow r = sheet.getRow(row);
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
            final double newValue = computeStock(row, stockIndex, updateType, quantity);
            if (updateType != UpdateType.TEST && cell != null) {
              cell.setCellValue(newValue);
            }
            logger.info("Update stock for line {}: {}", row, newValue);
          });
    }
  }

  private double computeStock(final Integer row, final Integer stockIndex, final UpdateType updateType, final long quantity) {
    final long originalStock = getOriginalStock(row, stockIndex);
    final double newValue;
    switch (updateType) {
      case ADD:
        newValue = originalStock + quantity;
        modifiedRows.add(row);
        break;
      case SUBSTRACT:
        newValue = originalStock - quantity;
        modifiedRows.add(row);
        break;
      case TEST:
      case UPDATE:
      default:
        newValue = quantity;
        modifiedRows.add(row);
    }
    logger.debug("Update value for cel({}, {}), update type: {}: old value: {}; quantity: {}; new value: {}",
        row, stockIndex, updateType, originalStock, quantity, newValue);
    return newValue;
  }

  private long getOriginalStock(final Integer row, final Integer stockIndex) {
    final long originalStock;
    if (row != null && stockIndex != null) {
      originalStock = excelUtilService.getLongValueFromCell(
          this.inService.getSelectedSheet().getRow(row).getCell(stockIndex));
    } else {
      originalStock = 0;
    }
    return originalStock;
  }

  @Override
  public void writeExcelWorkbook(final OutputStream stream) throws IOException {
    this.outService.getWorkbook().write(stream);
  }

  @Override
  public void writeExcelWorkbookModifiedRows(final OutputStream stream) throws IOException {
    final XSSFWorkbook copy = this.outService.getWorkbook();
    final String selectedSheetName = this.outService.getSelectedSheetName();
    if (StringUtils.isNotEmpty(selectedSheetName)) {
      removeUnselectedSheets(copy, selectedSheetName);
      final XSSFSheet sheet = copy.getSheet(selectedSheetName);
      final List<Integer> modifiedRowsCopy = new ArrayList<>(modifiedRows);
      if (sheet != null) {
        logger.debug("rows in sheet: {}", sheet.getPhysicalNumberOfRows());
        final List<Integer> rowsToRemove = getRowsToRemove(modifiedRows, sheet.getFirstRowNum(),
            sheet.getLastRowNum());
        logger.debug("will remove {} rows", rowsToRemove.size());
        rowsToRemove.stream()
            .sorted(Collections.reverseOrder())
            .map(sheet::getRow)
            .forEach(sheet::removeRow);
        logger.debug("rows in sheet: {}", sheet.getPhysicalNumberOfRows());
        copy.write(stream);
      } else {
        logger.warn("Selected sheet is null in the copy workbook");
      }
    } else {
      logger.warn("No sheet selected");
    }
  }

  private List<Integer> getRowsToRemove(final Set<Integer> modifiedRows, final int firstRowNum, final int lastRowNum) {
    final List<Integer> rows = IntStream.range(firstRowNum, lastRowNum)
        .filter(r -> !modifiedRows.contains(r))
        .mapToObj(Integer::valueOf)
        .collect(Collectors.toList());

    return rows;
  }

  private void removeUnselectedSheets(final XSSFWorkbook book, final String selectedSheetName) {
    assert book != null;
    assert selectedSheetName != null;
    final int size = book.getNumberOfSheets();
    for (int i = size - 1; i >= 0; i--) {
      if (!selectedSheetName.equals(book.getSheetName(i))) {
        book.removeSheetAt(i);
      }
    }
  }

  @Override
  public void writeCSV(final BufferedWriter out) throws IOException {
    final XSSFSheet sheet = this.outService.getWorkbook().getSheet(this.outService.getSelectedSheetName());
    final Iterator<Row> rowIterator = sheet.rowIterator();
    while (rowIterator.hasNext()) {
      final Row row = rowIterator.next();
      final String s = rowToString(row);
      out.write(s);
      out.newLine();
    }
    out.flush();
  }

  @Override
  public void writeModifiedRowsToCSV(final BufferedWriter out) throws IOException {
    final XSSFSheet sheet = this.outService.getWorkbook().getSheet(this.outService.getSelectedSheetName());
    for (final Integer r : modifiedRows) {
      final Row row = sheet.getRow(r);
      final String s = rowToString(row);
      out.write(s);
      out.newLine();
    }
    out.flush();
  }

  private String rowToString(final Row row) {
    String value = IntStream.range(0, this.inService.getColumnsName().size())
        .mapToObj(row::getCell)
        .map(this::getCellAsString)
        .collect(Collectors.joining(";"));

    return value;
  }

  private String getCellAsString(final Cell cell) {
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
    return value;
  }

  @Override
  public ArticleService getInService() {
    return inService;
  }

  @Override
  public ArticleService getIn2Service() {
    return in2Service;
  }

  @Override
  public ArticleService getStockService() {
    return stockService;
  }

  @Override
  public ArticleService getOutService() {
    return outService;
  }

  @Override
  public Void call() throws Exception {
    modifiedRows.clear();
    final String titleMsg = String.format("Updating stock at %s",
        now().format(dateFormatter));
    logger.info(titleMsg);
    // TODO try to use a log appender
    Platform.runLater(() -> mainPresentationModel.setLogOutput(titleMsg + ":\n"));
    stock.forEach((id, quantity) -> {
      if (id != null && quantity != null) {
        final Set<Integer> rows = new HashSet<>();
        if (inService.getIdsWithLineNumbersIndexes().isEmpty()) {
          addIfNotEmpty(rows, inService.findIdInAllColumnsInTheSheet(id));
        } else {
          addIfNotEmpty(rows, inService.getIdsWithLineNumbersIndexes().get(id));
          // maybe we'll find some articles in the secondary column
          addIfNotEmpty(rows, in2Service.getIdsWithLineNumbersIndexes().get(id));
        }
        if (!rows.isEmpty()) {
          updateStock(mainPresentationModel.getUpdateType(), rows, quantity);
          final String label;
          if (mainPresentationModel.getLabelColumn() != null) {
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
          logger.info(FOUND, msg);
          // TODO try to use a log appender
          Platform.runLater(() -> mainPresentationModel.setLogOutput(mainPresentationModel.getLogOutput() + msg + "\n"));
        } else {
          final String msg = String.format("Article not found: %d", id);
          logger.warn(NOT_FOUND, msg);
          // TODO try to use a log appender
          Platform.runLater(() -> mainPresentationModel.setLogOutput(mainPresentationModel.getLogOutput() + msg + "\n"));
        }
      }
    });
    return null;
  }
}
