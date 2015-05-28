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
@ComponentScan(basePackages = { "com.github.avdyk.stockupdater" })
public class ConfImpl {

	private final static Logger LOG = LoggerFactory.getLogger(ConfImpl.class);
  public static final String EXCEL_FILE = "excel.file";
  public static final String EXCEL_SHEETNAME = "excel.sheetname";
  public static final String EXCEL_OUT_COLUMN_NAME = "excel.out";
  public static final String EXCEL_IN_COLUMN_NAMES = "excel.in";
  public static final String STOCK_FILE = "stock.file";
  public static final String UPDATE_TYPE = "update.type";

  @Autowired
	private Environment env;
  private Path excelFile;
  private Path stockFile;
  private UpdateType updateType;
  private String sheetName;
  private String out;
  private String[] in;

  @PostConstruct
  void postConstruct() {
    final String exFstr = env.getProperty(EXCEL_FILE);
    excelFile = Paths.get(exFstr);
    LOG.debug("Excel filename: {}", excelFile);
    sheetName = env.getProperty(EXCEL_SHEETNAME);
    LOG.debug("Excel sheetname: {}", sheetName);
    out = env.getProperty(EXCEL_OUT_COLUMN_NAME);
    LOG.debug("Excel out column name: {}", out);
    final String instr = env.getProperty(EXCEL_IN_COLUMN_NAMES);
    in = instr.split(",");
    LOG.debug("Excel in column names: {}", in);
    final String stFstr = env.getProperty(STOCK_FILE);
    stockFile = Paths.get(stFstr);
    LOG.debug("Stock filename: {}", stockFile);
    final String upTstr = env.getProperty(UPDATE_TYPE);
    updateType = UpdateType.valueOf(upTstr);
    LOG.debug("Update type: {}", updateType);
  }

	public Path getExcelFile() {
		return excelFile;
	}

	public Path getStockFile() {
		return stockFile;
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

  public String getSheetName() {
    return sheetName;
  }

  public String getOut() {
    return out;
  }

  public String[] getIn() {
    return in;
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
