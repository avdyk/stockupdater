package com.github.avdyk.stockupdater.conf;

import com.github.avdyk.stockupdater.UpdateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ComponentScan(basePackages = {"com.github.avdyk.stockupdater"})
public class ConfImpl {

    private final static Logger LOG = LoggerFactory.getLogger(ConfImpl.class);
    public static final String EXCEL_FILE_IN = "excel.file.in";
    public static final String EXCEL_FILE_OUT = "excel.file.out";
    public static final String EXCEL_SHEETNAME = "excel.sheetname";
    public static final String EXCEL_STOCK_COLUMN_NAME = "excel.stock";
    public static final String EXCEL_OUT_COLUMN_NAME = "excel.out";
    public static final String EXCEL_IN_COLUMN_NAMES = "excel.in";
    public static final String STOCK_FILE = "stock.file";
    public static final String UPDATE_TYPE = "update.type";

    @Autowired
    private Environment env;
    private Path excelFileIn;
    private Path excelFileOut;
    private Path stockFile;
    private UpdateType updateType;
    private String sheetName;
    private String stockColumn;
    private String out;
    private String[] in;

    @PostConstruct
    void postConstruct() {
        // EXCEL_FILE_IN
        final String exFstr = env.getProperty(EXCEL_FILE_IN);
        if (exFstr != null) {
            this.setExcelFileIn(Paths.get(exFstr));
        }
        LOG.debug("Excel in filename: {}", excelFileIn);
        // EXCEL_FILE_OUT
        final String exFOutstr = env.getProperty(EXCEL_FILE_OUT);
        if (exFOutstr != null) {
            this.setExcelFileOut(Paths.get(exFOutstr));
        }
        LOG.debug("Excel out filename: {}", excelFileOut);
        // EXCEL_SHEETNAME
        this.setSheetName(env.getProperty(EXCEL_SHEETNAME));
        LOG.debug("Excel sheetname: {}", sheetName);
        // EXCEL_STOCK_COLUMN_NAME
        this.setStockColumn(EXCEL_STOCK_COLUMN_NAME);
        LOG.debug("Excel stock column name: {}", EXCEL_STOCK_COLUMN_NAME);
        // EXCEL_OUT_COLUMN_NAME
        this.setOut(env.getProperty(EXCEL_OUT_COLUMN_NAME));
        LOG.debug("Excel out column name: {}", out);
        // EXCEL_IN_COLUMN_NAMES
        final String instr = env.getProperty(EXCEL_IN_COLUMN_NAMES);
        if (instr != null) {
            this.setIn(instr.split(","));
        }
        LOG.debug("Excel in column names: {}", in);
        // STOCK_FILE
        final String stFstr = env.getProperty(STOCK_FILE);
        if (stFstr != null) {
            this.setStockFile(Paths.get(stFstr));
        }
        LOG.debug("Stock filename: {}", stockFile);
        // UPDATE_TYPE
        final String upTstr = env.getProperty(UPDATE_TYPE);
        if (upTstr != null) {
            updateType = UpdateType.valueOf(upTstr);
        }
        LOG.debug("Update type: {}", updateType);
    }

    public Path getExcelFileIn() {
        return excelFileIn;
    }

    public void setExcelFileIn(Path excelFileIn) {
        this.excelFileIn = excelFileIn;
    }

    public Path getExcelFileOut() {
        return excelFileOut;
    }

    public void setExcelFileOut(Path excelFileOut) {
        this.excelFileOut = excelFileOut;
    }

    public Path getStockFile() {
        return stockFile;
    }

    public void setStockFile(final Path stockFile) {
        this.stockFile = stockFile;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public void setUpdateType(final UpdateType updateType) {
        this.updateType = updateType;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(final String sheetName) {
        this.sheetName = sheetName;
    }

    public String getStockColumn() {
        return stockColumn;
    }

    public void setStockColumn(String stockColumn) {
        this.stockColumn = stockColumn;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public String[] getIn() {
        return in;
    }

    public void setIn(String[] in) {
        this.in = in;
    }

    /**
     * To resolve ${} in @Value.
     *
     * @return a property sources place holder configurer for Spring.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
