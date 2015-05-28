package com.github.avdyk.stockupdater;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

  final Path excelFile;
  final String sheetName;
  final String out;
  final String[] in;

  @Autowired
  public ArticleService(@Value("#{confImpl.excelFile}") final Path excelFile,
                        @Value("#{confImpl.sheetName}") final String sheetName,
                        @Value("#{confImpl.out}") final String out,
                        @Value("#{confImpl.in}") final String...in) throws IOException {
    // pre-requis:
    if (StringUtils.isBlank(out)) {
      throw new IllegalArgumentException("Unknown 'out' column to update the stock");
    }
    if (in == null || in.length == 0) {
      throw new IllegalArgumentException("Must have at least one 'in' column to lookup the barcode");
    }
    for (String i : in) {
      if (StringUtils.isBlank(i)) {
        throw new IllegalArgumentException("Illegal value for one of the 'in' column");
      }
    }
    this.excelFile = excelFile;
    this.sheetName = sheetName;
    this.out = out;
    this.in = in;
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
    final int outIndex;
    final List<Integer> intIndexes = new ArrayList<>();
    final Iterator<Row> rowIter = sheet.rowIterator();
    if (rowIter != null && rowIter.hasNext()) {
      final Row header = rowIter.next();
      final Iterator<Cell> cellIter = header.iterator();
      // TODO parcourir les colonnes pour trouver la out et toutes les in
    }
  }

  public void updateStock(final UpdateType updateType,
                          final Map<Long,Long> stock) {
    if (stock == null) {
      throw new NullPointerException("Stock cannot be 'null'");
    }
    if (updateType == null) {
      throw new NullPointerException("Update Type cannot be 'null'");
    }
  }
}
