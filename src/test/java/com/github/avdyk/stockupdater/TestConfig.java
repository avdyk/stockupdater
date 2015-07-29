package com.github.avdyk.stockupdater;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Spring configuration class.
 *
 * Created by arnaud on 10/07/15.
 */
@Configuration
@ComponentScan(basePackages = { "com.github.avdyk.stockupdater" })
public class TestConfig {

  private static ExcelUtilServiceImpl excelUtilService = new ExcelUtilServiceImpl();

//  @Bean
//  @Scope("prototype")
//  public ArticleService getArticleService() throws IOException {
//    final ArticleService service = new ArticleService(Paths.get("target/test-classes/articles.xlsx"), null);
//    return service;
//  }

  @Bean
  @Scope("singleton")
  public ExcelUtilServiceImpl getExcelUtilService() {
    return excelUtilService;
  }

}
