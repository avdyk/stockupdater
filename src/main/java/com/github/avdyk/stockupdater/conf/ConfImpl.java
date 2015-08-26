package com.github.avdyk.stockupdater.conf;

import com.github.avdyk.stockupdater.UpdateType;
import com.github.avdyk.stockupdater.ui.javafx.JavaFxControllerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Configuration
@PropertySources({@PropertySource("classpath:stockupdater.properties")})
@ComponentScan(basePackages = {"com.github.avdyk.stockupdater"})
public class ConfImpl {

  private final static Logger LOG = LoggerFactory.getLogger(ConfImpl.class);
  public static final String APPLICATION_NAME = "${application.name}";
  public static final String VERSION = "${application.version}";
  public static final String EXCEL_FILE_IN = "excel.file.in";
  public static final String EXCEL_FILE_OUT = "excel.file.out";
  public static final String EXCEL_SHEETNAME_IN = "excel.sheetname.in";
  public static final String EXCEL_SHEETNAME_OUT = "excel.sheetname.out";
  public static final String EXCEL_STOCK_COLUMN_NAME = "excel.stock";
  public static final String EXCEL_OUT_COLUMN_NAME = "excel.out";
  public static final String EXCEL_IN_COLUMN_NAMES = "excel.in";
  public static final String STOCK_FILE = "stock.file";
  public static final String UPDATE_TYPE = "update.type";

  @Autowired
  private Environment env;
  @Autowired
  private JavaFxControllerFactory javaFxControllerFactory;
  @Value(APPLICATION_NAME)
  private String applicationName;
  @Value(VERSION)
  private String version;
  private Path excelFileIn;
  private Path excelFileOut;
  private Path stockFile;
  private UpdateType updateType;
  private String sheetNameIn;
  private String sheetNameOut;
  private String stockColumn;
  private String out;
  private String[] in;

  @PostConstruct
  void postConstruct() {
    // APPLICATION_NAME
    LOG.debug("Application name: {}", applicationName);
    // VERSION
    LOG.debug("Application version: {}", version);
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
    // EXCEL_SHEETNAME_IN
    this.setSheetNameIn(env.getProperty(EXCEL_SHEETNAME_IN));
    LOG.debug("Excel sheetname in: {}", sheetNameIn);
    // EXCEL_SHEETNAME_OUT
    this.setSheetNameOut(env.getProperty(EXCEL_SHEETNAME_OUT));
    LOG.debug("Excel sheetname out: {}", sheetNameOut);
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
    LOG.debug("Excel in column names: {}", Arrays.toString(in));
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

  public String getApplicationName() {
    return applicationName;
  }

  public String getVersion() {
    return version;
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

  public String getSheetNameIn() {
    return sheetNameIn;
  }

  public void setSheetNameIn(final String sheetName) {
    this.sheetNameIn = sheetName;
  }

  public String getSheetNameOut() {
    return sheetNameOut;
  }

  public void setSheetNameOut(final String sheetName) {
    this.sheetNameOut = sheetName;
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
