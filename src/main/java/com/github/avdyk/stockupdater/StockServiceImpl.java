package com.github.avdyk.stockupdater;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * Service to compute the sotck with the workbooks.
 *
 * Created by arnaud on 25/08/15.
 */
@Service
public class StockServiceImpl implements StockService {

  private static final Logger LOG = LoggerFactory.getLogger(StockServiceImpl.class);

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
                          final Map<Long, Long> stock) {

    if (stock == null) {
      throw new NullPointerException("Stock cannot be 'null'");
    }
    if (stock.isEmpty()) {
      throw new IllegalArgumentException("Stock cannot be empty");
    }
    if (updateType == null) {
      throw new NullPointerException("Update Type cannot be 'null'");
    }
    LOG.info("Updating stock");
    stock.forEach((id, quantity) -> {
      if (id != null && quantity != null) {
        final Set<Integer> rows = inService.getIdsWithLineNumbersIndexes().get(id);
        if (rows != null && !rows.isEmpty()) {
          updateStock(updateType, rows, quantity);
        } else {
          LOG.warn("Article {} has not been found!", id);
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
