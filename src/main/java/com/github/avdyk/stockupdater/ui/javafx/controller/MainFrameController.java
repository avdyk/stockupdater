package com.github.avdyk.stockupdater.ui.javafx.controller;

import com.github.avdyk.stockupdater.StockCompute;
import com.github.avdyk.stockupdater.conf.ConfImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the main frame.
 *
 * Created by arnaud on 18/08/15.
 */
public class MainFrameController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(MainFrameController.class);
  @Autowired
  private ConfImpl configuration;
  @Autowired
  private StockCompute stockCompute;
  private Stage stage;
  @FXML
  private StackPane root;
  @FXML
  private Button excelFileInButton;
  @FXML
  private TextField excelFileInTextField;
  @FXML
  private TextField stockFileTextField;
  @FXML
  private Button stockFileButton;

  public ConfImpl getConfiguration() {
    return configuration;
  }

  public Parent getView() {
    return root;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    excelFileInButton.setOnAction(this::chooseExcelFileIn);
    stockFileButton.setOnAction(this::chooseStockTextFile);
  }

  private void chooseExcelFileIn(final ActionEvent actionEvent) {
    final FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Excel In File");
    final File f = fileChooser.showOpenDialog(stage);
    // TODO travailler avec un binding
    if (f == null) {
      excelFileInTextField.setText("");
    } else {
      excelFileInTextField.setText(f.toString());
    }
    // FIXME pour l'instant, article service ne peut être construit qu'avec excel in et out déjà configuré.

  }

  private void chooseStockTextFile(final ActionEvent actionEvent) {
    final FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Stock Text File");
    final File stockFile = fileChooser.showOpenDialog(stage);
    // TODO travailler avec un binding
    if (stockFile == null) {
      stockFileTextField.setText("");
    } else {
      stockFileTextField.setText(stockFile.toString());
    }
    try {
      Map<Long, Long> stock = stockCompute.stockStream(Files.lines(Paths.get(stockFile.toURI())));
      if (LOG.isDebugEnabled()) {
        LOG.debug("Résultat du stock");
        for (final Long barCode : stock.keySet()) {
          LOG.debug("barCode: {}; stock: {}", barCode, stock.get(barCode));
        }
      }
    } catch (IOException e) {
      LOG.warn("Une exception a eu lieu", e);
    }
  }

  public void setStage(final Stage stage) {
    this.stage = stage;
  }

  public Stage getStage() {
    return stage;
  }
}
