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

	@Autowired
	private Environment env;
  private Path excelFile;
  private Path stockFile;
  private UpdateType updateType;

  @PostConstruct
  void postConstruct() {
    final String exFstr = env.getProperty("excel.file");
    excelFile = Paths.get(exFstr);
    LOG.debug("Fichier Excel: {}", excelFile);
    final String stFstr = env.getProperty("stock.file");
    stockFile = Paths.get(stFstr);
    LOG.debug("Fichier stock: {}", stockFile);
    final String upTstr = env.getProperty("update.type");
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
