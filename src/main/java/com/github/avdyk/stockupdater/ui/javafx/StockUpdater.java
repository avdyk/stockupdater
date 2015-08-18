package com.github.avdyk.stockupdater.ui.javafx;

import com.github.avdyk.stockupdater.conf.ConfImpl;
import com.github.avdyk.stockupdater.ui.javafx.controller.MainFrameController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Classe de lancement de l'application JavaFX.
 *
 * @author <a href="mailto:avandyck@gmail.com">Arnaud Vandyck</a>
 * @version 1.0, 18/08/15.
 */
public class StockUpdater extends Application {

  private static final Logger logger = LoggerFactory.getLogger(StockUpdater.class);

  public static void main(String[] args) {
    logger.info("StockUpdater starting...");
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    AnnotationConfigApplicationContext context
        = new AnnotationConfigApplicationContext(ConfImpl.class);
    ConfImpl conf = context.getBean(ConfImpl.class);
    SpringFxmlLoader loader = new SpringFxmlLoader(context);

    Parent root = (Parent) loader.load("/fxviews/MainFrame.fxml", MainFrameController.class);
    Scene scene = new Scene(root, 320, 240);
    scene.getStylesheets().add("/css/stockupdater.css");
    primaryStage.setScene(scene);
    primaryStage.setTitle(String.format("%s - %s", conf.getApplicationName(), conf.getVersion()));
    primaryStage.show();
  }

}
