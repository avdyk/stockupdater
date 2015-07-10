package com.github.avdyk.stockupdater;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

  XSSFWorkbook excelWorkbookIn;
  XSSFWorkbook excelWorkbookOut;
  List<String> inSheetNames;
  XSSFSheet inSelectedSheet;
  List<String> inColumnNames;
  List<String> outSheetNames;
  XSSFSheet outSelectedSheet;
  List<String> outColumnNames;

  String out;
  String stock;
  List<String> in;
  Map<Long, Set<Integer>> INDEXES = new HashMap<>();
  @Autowired
  private ExcelUtilServiceImpl excelUtilService;

  @Autowired
  public ArticleService(@Value("#{confImpl.excelFileIn}") final Path excelFileIn,
                        @Value("#{confImpl.excelFileOut}") final Path excelFileOut) throws IOException {
    this(new XSSFWorkbook(Files.newInputStream(excelFileIn)),
        excelFileOut != null ? new XSSFWorkbook(Files.newInputStream(excelFileOut))
            : new XSSFWorkbook(Files.newInputStream(excelFileIn)));
  }

  @Autowired
  public ArticleService(final XSSFWorkbook workbookIn, final XSSFWorkbook workbookOut) {
    this.excelWorkbookIn = workbookIn;
    final int numberOfInSheets = this.excelWorkbookIn.getNumberOfSheets();
    final List<String> inNames = new ArrayList<>(numberOfInSheets);
    for (int i = 0; i < numberOfInSheets; i++) {
      inNames.add(this.excelWorkbookIn.getSheetName(i));
    }
    this.inSheetNames = Collections.unmodifiableList(inNames);
    if (workbookOut == null) {
      this.excelWorkbookOut = this.excelWorkbookIn;
      this.outSheetNames = inSheetNames;
    } else {
      this.excelWorkbookOut = workbookOut;
      final int numberOfOutSheets = this.excelWorkbookOut.getNumberOfSheets();
      final List<String> outNames = new ArrayList<>(numberOfOutSheets);
      for (int i = 0; i < numberOfOutSheets; i++) {
        outNames.add(this.excelWorkbookOut.getSheetName(i));
      }
      this.outSheetNames = Collections.unmodifiableList(outNames);
    }
  }

  @Autowired
  public ArticleService(@Value("#{confImpl.excelFileIn}") final Path excelFileIn,
                        @Value("#{confImpl.excelFileOut}") final Path excelFileOut,
                        @Value("#{confImpl.sheetNameIn}") final String sheetNameIn,
                        @Value("#{confImpl.sheetNameOut}") final String sheetNameOut,
                        @Value("#{confImpl.excelStockColumn}") final String stock,
                        @Value("#{confImpl.out}") final String out,
                        @Value("#{confImpl.in}") final String... in) throws IOException {
    this(excelFileIn, excelFileOut);
    // pre-requis:
    this._setStock(stock);
    this._setOut(out);
    this._setIn(Arrays.asList(in));
    if (LOG.isDebugEnabled()) {
      final int sheets = excelWorkbookIn.getNumberOfSheets();
      for (int i = 0; i < sheets; i++) {
        LOG.debug("Sheet {}: {}", i, excelWorkbookIn.getSheetName(i));
      }
    }
    if (sheetNameIn != null) {
      this.setInSelectedSheet(sheetNameIn);
      LOG.debug("Reading in sheet {}", sheetNameIn);
    } else {
      this.setInSelectedSheet(excelWorkbookIn.getSheetAt(0).getSheetName());
      LOG.debug("No in sheet name, picking the first one");
    }
    if (sheetNameOut != null) {
      this.setOutSelectedSheet(sheetNameOut);
      LOG.debug("Reading out sheet {}", sheetNameOut);
    } else {
      this.setOutSelectedSheet(excelWorkbookOut.getSheetAt(0).getSheetName());
      LOG.debug("No out sheet name, picking the first one");
    }
  }

  public List<String> getInSheetNames() {
    return new ArrayList<>(this.inSheetNames);
  }

  public void setInSelectedSheet(final String selectedSheetName) {
    this.inSelectedSheet = this.excelWorkbookIn.getSheet(selectedSheetName);
    if (this.inSelectedSheet != null) {
      this.inColumnNames = getColumnNamesFromSheet(this.inSelectedSheet);
    } else {
      throw new IllegalArgumentException(String.format("Sheet '%s' not found", selectedSheetName));
    }
  }

  List<String> getColumnNamesFromSheet(final XSSFSheet sheet) {
    final List<String> columnNames = new ArrayList<>();
    final Iterator<Row> rowIter = sheet.rowIterator();
    if (rowIter != null && rowIter.hasNext()) {
      final Row header = rowIter.next();
      // parcourir les colonnes pour trouver la out
      final Iterator<Cell> cellIterator = header.iterator();
      while (cellIterator.hasNext()) {
        final Cell c = cellIterator.next();
        columnNames.add(c.getStringCellValue());
      }
    }
    return Collections.unmodifiableList(columnNames);
  }

  public XSSFSheet getInSelectedSheet() {
    return this.inSelectedSheet;
  }

  public void setOutSelectedSheet(final String selectedSheetName) {
    this.outSelectedSheet = this.excelWorkbookOut.getSheet(selectedSheetName);
    if (this.outSelectedSheet != null) {
      this.outColumnNames = getColumnNamesFromSheet(this.outSelectedSheet);
    } else {
      throw new IllegalArgumentException(String.format("Sheet '%s' not found", selectedSheetName));
    }
  }

  public XSSFSheet getOutSelectedSheet() {
    return this.outSelectedSheet;
  }

  public List<String> getInColumnNames() {
    return new ArrayList<>(this.inColumnNames);
  }

  public String getStock() {
    return stock;
  }

  public void setStock(final String stock) {
    this._setStock(stock);
  }

  private void _setStock(final String stock) {
    if (StringUtils.isBlank(this.stock)) {
      throw new IllegalArgumentException("Unknown 'stock' column to update the stock in 'in sheet'");
    }
    if (!this.inColumnNames.contains(this.stock)) {
      throw new IllegalArgumentException(String.format("Column 'stock' %s not found in 'in sheet'", out));
    }
    this.stock = stock;
  }

  public String getOut() {
    return out;
  }

  public void setOut(final String out) {
    this._setOut(out);
  }

  private void _setOut(final String out) {
    if (StringUtils.isBlank(out)) {
      throw new IllegalArgumentException("Unknown 'out' column to update the stock");
    }
    if (!this.outColumnNames.contains(out)) {
      throw new IllegalArgumentException(String.format("Column 'out' %s not found", out));
    }
    this.out = out;
  }

  public List<String> getIn() {
    return new ArrayList<>(this.in);
  }

  public void setIn(final List<String> in) {
    this._setIn(in);
  }

  public void _setIn(final List<String> in) {
    if (in == null || in.isEmpty()) {
      throw new IllegalArgumentException("Must have at least one 'in' column to lookup the barcode");
    }
    for (String i : in) {
      if (StringUtils.isBlank(i)) {
        throw new IllegalArgumentException("Illegal value for one of the 'in' column");
      } else {
        if (!this.inColumnNames.contains(i)) {
          throw new IllegalArgumentException(String.format("Column 'in' %s not found", i));
        }
      }
    }
    this.in = in;
    // prepare index
    INDEXES.clear();
    final Iterator<Row> rowIterator = this.inSelectedSheet.rowIterator();
    assert rowIterator.hasNext();
    // header
    rowIterator.next();
    while (rowIterator.hasNext()) {
      final Row row = rowIterator.next();
      for (final String colIndxName : in) {
        final Cell cell = row.getCell(inColumnNames.indexOf(colIndxName));
        if (cell != null) {
          final Integer lineNum = cell.getRowIndex();
          final Long id = excelUtilService.getLongValueFromCell(cell);
          if (id != null) {
            if (INDEXES.containsKey(id)) {
              final Set<Integer> ids = INDEXES.get(id);
              final boolean alreadyExists = ids.add(lineNum);
              if (alreadyExists) {
                LOG.warn("id '{}' exists at lines {}", id, Arrays.toString(ids.toArray()));
              }
            } else {
              final Set<Integer> ids = new HashSet<>();
              ids.add(lineNum);
              INDEXES.put(id, ids);
            }
          }
        }
      }
    }
  }

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
        final Set<Integer> rows = INDEXES.get(id);
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
    final int outIndex = this.outColumnNames.indexOf(out);
    final int stockIndex = this.inColumnNames.indexOf(stock);
    rows.stream().filter(r -> r != null)
        .forEach(row -> {
          final Long originalStock = excelUtilService.getLongValueFromCell(
              inSelectedSheet.getRow(row).getCell(stockIndex));
          final XSSFRow r = outSelectedSheet.getRow(row);
          XSSFCell cell = r.getCell(outIndex);
          if (cell == null) {
            cell = r.createCell(outIndex);
          }
          switch (updateType) {
            case UPDATE:
              cell.setCellValue(quantity);
              break;
            case ADD:
              cell.setCellValue(originalStock + quantity);
              break;
            case SUBSTRACT:
              cell.setCellValue(originalStock - quantity);
              break;
          }
        });
  }

  /**
   * Save the sheet on the stream. The stream will not be closed!
   *
   * @param stream the stream.
   * @throws IOException if a problem with the stream.
   */
  public void writeExcelWorkbook(final OutputStream stream) throws IOException {
    this.excelWorkbookOut.write(stream);
  }

}
