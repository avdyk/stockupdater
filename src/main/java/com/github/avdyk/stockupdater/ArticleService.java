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
    }

    public void updateStock(final UpdateType updateType,
                            final Map<Long, Long> stock) {
        if (stock == null) {
            throw new NullPointerException("Stock cannot be 'null'");
        }
        if (updateType == null) {
            throw new NullPointerException("Update Type cannot be 'null'");
        }
        LOG.info("Updating stock");
    }

}
