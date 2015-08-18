package com.github.avdyk.stockupdater.ui.javafx;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;

public class SpringFxmlLoader {
  private ApplicationContext context;

  public SpringFxmlLoader(ApplicationContext context) {
    this.context = context;
  }

  public Object load(String url, Class<?> controllerClass) throws IOException {
    InputStream fxmlStream = null;
    try {
      fxmlStream = controllerClass.getResourceAsStream(url);
      Object instance = context.getBean(controllerClass);
      FXMLLoader loader = new FXMLLoader();
      loader.getNamespace().put("mainFrameController", instance);
      return loader.load(fxmlStream);
    } finally {
      if (fxmlStream != null) {
        fxmlStream.close();
      }
    }
  }
}