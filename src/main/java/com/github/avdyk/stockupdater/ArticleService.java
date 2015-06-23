package com.github.avdyk.stockupdater;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
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

  XSSFWorkbook excelWorkbook;
  List<String> sheetNames;
  XSSFSheet selectedSheet;
  List<String> columnNames;

  String out;
  List<String> in;
  Map<Long, Set<Integer>> INDEXES = new HashMap<>();

  @Autowired
  public ArticleService(@Value("#{confImpl.excelFile}") final Path excelFile) throws IOException {
    this(new XSSFWorkbook(Files.newInputStream(excelFile)));
  }

  @Autowired
  public ArticleService(final XSSFWorkbook workbook) {
    this.excelWorkbook = workbook;
    final int numberOfSheets = this.excelWorkbook.getNumberOfSheets();
    final List<String> names = new ArrayList<>(numberOfSheets);
    for (int i = 0; i < numberOfSheets; i++) {
      names.add(this.excelWorkbook.getSheetName(i));
    }
    this.sheetNames = Collections.unmodifiableList(names);
  }

  @Autowired
  public ArticleService(@Value("#{confImpl.excelFile}") final Path excelFile,
                        @Value("#{confImpl.sheetName}") final String sheetName,
                        @Value("#{confImpl.out}") final String out,
                        @Value("#{confImpl.in}") final String... in) throws IOException {
    this(excelFile);
    // pre-requis:
    this._setOut(out);
    this._setIn(Arrays.asList(in));
    if (LOG.isDebugEnabled()) {
      final int sheets = excelWorkbook.getNumberOfSheets();
      for (int i = 0; i < sheets; i++) {
        LOG.debug("Sheet {}: {}", i, excelWorkbook.getSheetName(i));
      }
    }
    if (sheetName != null) {
      this.setSelectedSheet(sheetName);
      LOG.debug("Reading sheet {}", sheetName);
    } else {
      this.setSelectedSheet(excelWorkbook.getSheetAt(0).getSheetName());
      LOG.debug("No sheet name, picking the first one");
    }
  }

  public List<String> getSheetNames() {
    return new ArrayList<>(this.sheetNames);
  }

  public void setSelectedSheet(final String selectedSheetName) {
    this.selectedSheet = this.excelWorkbook.getSheet(selectedSheetName);
    if (this.selectedSheet != null) {
      int outIndexTemp = -1;
      final Iterator<Row> rowIter = this.selectedSheet.rowIterator();
      if (rowIter != null && rowIter.hasNext()) {
        final Row header = rowIter.next();
        // parcourir les colonnes pour trouver la out
        final Iterator<Cell> cellIterator = header.iterator();
        final List<String> columnNames = new ArrayList<>();
        while (cellIterator.hasNext()) {
          final Cell c = cellIterator.next();
          columnNames.add(c.getStringCellValue());
        }
        this.columnNames = Collections.unmodifiableList(columnNames);
      }
    } else {
      throw new IllegalArgumentException("Sheet not found");
    }
  }

  public XSSFSheet getSelectedSheet() {
    return this.selectedSheet;
  }

  public List<String> getColumnNames() {
    return new ArrayList<>(this.columnNames);
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
    if (!this.columnNames.contains(out)) {
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
        if (!this.columnNames.contains(i)) {
          throw new IllegalArgumentException(String.format("Column 'in' %s not found", i));
        }
      }
    }
    this.in = in;
    // prepare index
    INDEXES.clear();
    final Iterator<Row> rowIterator = this.selectedSheet.rowIterator();
    assert rowIterator.hasNext();
    // header
    rowIterator.next();
    while (rowIterator.hasNext()) {
      final Row row = rowIterator.next();
      for (final String colIndxName : in) {
        final Cell cell = row.getCell(columnNames.indexOf(colIndxName));
        if (cell != null) {
          final Integer lineNum = cell.getRowIndex();
          final Long id;
          if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            id = (long) cell.getNumericCellValue();
          } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            final String cellValue = cell.getStringCellValue();
            if (StringUtils.isNumeric(cellValue)) {
              id = Long.valueOf(cellValue);
            } else {
              LOG.debug("The cell at row {}, col {} has no numeric value ({})", cell.getRow(),
                  cell.getColumnIndex(), cellValue);
              id = null;
            }
          } else {
            LOG.warn("The cell at row {}, col {} is neither numeric or string", cell.getRow(),
                cell.getColumnIndex());
            id = null;
          }
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
  }

}
