package com.github.avdyk.stockupdater.ui.javafx;

import com.github.avdyk.stockupdater.conf.ConfImpl;
import com.github.avdyk.stockupdater.ui.javafx.controller.MainFrameController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.PrintWriter;
import java.io.StringWriter;

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
    Thread.setDefaultUncaughtExceptionHandler(this::exceptionHandler);
    MainFrameController controller = context.getBean(MainFrameController.class);
    controller.setStage(primaryStage);
    ConfImpl conf = context.getBean(ConfImpl.class);
    Scene scene = new Scene(controller.getView(), 450, 300);
    scene.getStylesheets().add("/css/stockupdater.css");
    primaryStage.setScene(scene);
    primaryStage.setTitle(String.format("%s - %s", conf.getApplicationName(), conf.getVersion()));
    primaryStage.show();
  }

  private void exceptionHandler(final Thread t, final Throwable e) {
    // FIXME update the text in the dialog
    logger.error("Oups... un problème dans le thread {}: {}", t.getName(), e.getMessage(), e);
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Exception Dialog");
    alert.setHeaderText("Look, an Exception Dialog");
    alert.setContentText("Could not find file blabla.txt!");

    // Create expandable Exception.
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    String exceptionText = sw.toString();

    Label label = new Label("The exception stacktrace was:");

    TextArea textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    GridPane expContent = new GridPane();
    expContent.setMaxWidth(Double.MAX_VALUE);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);

    alert.showAndWait();  }

}
