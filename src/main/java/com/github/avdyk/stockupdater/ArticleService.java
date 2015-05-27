package com.github.avdyk.stockupdater;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Update the database with the StockCompute.
 *
 * @author <a href="mailto:avd@mims.be">Arnaud Vandyck</a>
 * @since 27/05/2015 17:18
 */
@Service
@Scope("prototype")
public class ArticleService {

  private static final Logger LOG = LoggerFactory.getLogger(ArticleService.class);

  @Value("#{confImpl.excelFile")
  Path excelFile;

  public void updateStock(final UpdateType updateType,
                          final StockCompute stock,
                          final String sheetName,
                          final String out,
                          final String... in) throws IOException {
      final XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(excelFile));
      if (LOG.isDebugEnabled()) {
        final int sheets = workbook.getNumberOfSheets();
        for (int i = 0; i < sheets; i++) {
          LOG.debug("Sheet {}: {}", i, workbook.getSheetName(i));
        }
      }
    final XSSFSheet sheet;
    if (sheetName != null) {
      sheet = workbook.getSheet(sheetName);
      LOG.debug("Reading sheet {}", sheetName);
    } else {
      sheet = workbook.getSheetAt(0);
      LOG.debug("No sheet name, picking the first one");
    }
    final Iterator<Row> rowIter = sheet.rowIterator();
    if (rowIter != null && rowIter.hasNext()) {
      final Row header = rowIter.next();
      final Iterator<Cell> cellIter = header.iterator();
      // TODO parcourir les colonnes pour trouver la out et toutes les in
    }
  }
}
