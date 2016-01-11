package com.github.avdyk.stockupdater;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Update the database with the StockCompute.
 *
 * @author <a href="mailto:avd@mims.be">Arnaud Vandyck</a>
 * @since 27/05/2015 17:18
 */
@Service
@Scope("prototype")
public class ArticleServiceImpl implements ArticleService {

  private static final Logger LOG = LoggerFactory.getLogger(ArticleServiceImpl.class);

  XSSFWorkbook workbook;
  List<String> sheetNames = Collections.emptyList();
  XSSFSheet selectedSheet;
  String selectedSheetName;
  List<String> columnNames = Collections.emptyList();
  String selectedColumn;
  Map<Long, Set<Integer>> INDEXES = new HashMap<>();
  boolean indexed = false;
  String selectedLabelColumn;

  @Autowired
  private ExcelUtilServiceImpl excelUtilService;

  public ArticleServiceImpl() {
    super();
  }

  @Override
  public XSSFWorkbook getWorkbook() {
    return workbook;
  }

  @Override
  public void setWorkbook(XSSFWorkbook workbook) {
    this.workbook = workbook;
    if (workbook != null) {
      this.sheetNames = getSheetsName(this.workbook);
    } else {
      this.sheetNames = Collections.emptyList();
    }
    this.selectedSheet = null;
    this.selectedSheetName = null;
    this.columnNames = Collections.emptyList();
    this.selectedColumn = null;
  }

  @Override
  public List<String> getSheetsName() {
    return sheetNames;
  }

  List<String> getSheetsName(final XSSFWorkbook wb) {
    final int numberOfInSheets = wb.getNumberOfSheets();
    final List<String> inNames = new ArrayList<>(numberOfInSheets);
    for (int i = 0; i < numberOfInSheets; i++) {
      inNames.add(wb.getSheetName(i));
    }
    return Collections.unmodifiableList(inNames);
  }

  @Override
  public String getSelectedSheetName() {
    return selectedSheetName;
  }

  @Override
  public void setSelectedSheetName(String selectedSheetName) {
    this.selectedSheetName = selectedSheetName;
    if (StringUtils.isNotBlank(selectedSheetName)) {
      this.selectedSheet = this.workbook.getSheet(selectedSheetName);
      if (this.selectedSheet != null) {
        this.columnNames = getColumnsNameInSheet(this.selectedSheet);
      } else {
        throw new IllegalArgumentException(String.format("Sheet '%s' not found", selectedSheetName));
      }
    } else {
      throw new IllegalArgumentException(String.format("Sheet '%s' not found", selectedSheetName));
    }
    this.setSelectedColumn(null);
  }

  @Override
  public XSSFSheet getSelectedSheet() {
    return selectedSheet;
  }

  List<String> getColumnsNameInSheet(final XSSFSheet sheet) {
    final List<String> columnNames = new ArrayList<>();
    final Iterator<Row> rowIter = sheet.rowIterator();
    if (rowIter != null && rowIter.hasNext()) {
      final Row header = rowIter.next();
      for (Cell c : header) {
        columnNames.add(c.getStringCellValue());
      }
    }
    return Collections.unmodifiableList(columnNames);
  }

  @Override
  public List<String> getColumnsName() {
    return columnNames;
  }

  @Override
  public void setSelectedColumn(String columnName) {
    if (!StringUtils.isBlank(columnName) && !this.columnNames.contains(columnName)) {
      throw new IllegalArgumentException(String.format("Column %s not found", columnName));
    }
    this.selectedColumn = columnName;
    indexed = false;
  }

  @Override
  public String getSelectedColumnName() {
    return selectedColumn;
  }

  @Override
  public Map<Long, Set<Integer>> getIdsWithLineNumbersIndexes() {
    if (!indexed) {
      computeIndex();
    }
    return Collections.unmodifiableMap(INDEXES);
  }

  private void computeIndex() {
    // prepare index
    INDEXES.clear();
    final Iterator<Row> rowIterator = this.selectedSheet.rowIterator();
    assert rowIterator.hasNext();
    // header
    rowIterator.next();
    while (rowIterator.hasNext()) {
      final Row row = rowIterator.next();
      if (selectedColumn != null) {
        final Cell cell = row.getCell(columnNames.indexOf(selectedColumn));
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
    indexed = true;
  }

  @Override
  public Set<Integer> findIdInAllColumnsInTheSheet(final long id) {
    final Set<Integer> rows = new HashSet<>();
    final Iterator<Row> rowIterator = this.selectedSheet.rowIterator();
    assert rowIterator.hasNext();
    // header
    rowIterator.next();
    while (rowIterator.hasNext()) {
      final Row row = rowIterator.next();
      boolean found = false;
      final Iterator<Cell> cellIterator = row.cellIterator();
      while (!found && cellIterator.hasNext()) {
        final Cell cell = cellIterator.next();
        final Integer lineNum = cell.getRowIndex();
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC || cell.getCellType() == Cell.CELL_TYPE_STRING) {
          final Long numericCellValue = excelUtilService.getLongValueFromCell(cell);
          if (numericCellValue != null) {
            found = numericCellValue.equals(id);
            if (found) {
              rows.add(lineNum);
            }
          }
        }
      }
    }
    return rows;
  }

  @Override
  public String getSelectedLabelColumn() {
    return this.selectedLabelColumn;
  }

  @Override
  public void setSelectedLabelColumn(final String newValue) {
    this.selectedLabelColumn = newValue;
  }
}
