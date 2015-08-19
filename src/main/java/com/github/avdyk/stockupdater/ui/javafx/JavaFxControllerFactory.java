package com.github.avdyk.stockupdater.ui.javafx;

import com.github.avdyk.stockupdater.ui.javafx.controller.MainFrameController;
import javafx.fxml.FXMLLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * JavaFx controller factory.
 *
 * @author <a href="mailto:avd@mims.be">Arnaud Vandyck</a>
 * @since 19/08/2015 09:12
 */
@Service
public class JavaFxControllerFactory {

  public static final String MAIN_FRAME = "/fxviews/MainFrame.fxml";

  @Bean
  public MainFrameController mainFrameController() throws IOException {
    return loadController(MAIN_FRAME);
  }

  /**
   * Chargement d'un controller FXMLLoader en fonction du fichier FXML passé en paramètre.
   * @param url l'url du fichier FXML.
   * @return le FXML construit avec le fichier FXML passé en paramètre
   * @throws IOException si le fichier FXML n'est pas trouvé ou illisible.
   * @see javafx.fxml.FXMLLoader
   */
  private <E> E loadController(String url) throws IOException {
    InputStream fxmlStream = null;
    FXMLLoader loader = null;
    try {
      fxmlStream = getClass().getResourceAsStream(url);
      loader = new FXMLLoader();
      loader.load(fxmlStream);
    } finally {
      if (fxmlStream != null) {
        fxmlStream.close();
      }
    }
    return loader.getController();
  }

}
