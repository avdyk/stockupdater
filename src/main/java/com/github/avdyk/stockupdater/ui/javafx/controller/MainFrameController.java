package com.github.avdyk.stockupdater.ui.javafx.controller;

import com.github.avdyk.stockupdater.StockCompute;
import com.github.avdyk.stockupdater.conf.ConfImpl;
import com.github.avdyk.stockupdater.ui.javafx.MainPresentationModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
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
  @Autowired
  private MainPresentationModel mainPresentationModel;
  private Stage stage;
  @FXML
  private StackPane root;
  @FXML
  private Button excelFileInButton;
  @FXML
  private TextField excelFileInTextField;
  @FXML
  private ComboBox inSheetComboBox;
  @FXML
  private ChoiceBox inColumnsChoiceBox;
  @FXML
  private ComboBox stockColumnsComboBox;
  @FXML
  private TextField excelFileOutTextField;
  @FXML
  private Button excelFileOutButton;
  @FXML
  private ComboBox outSheetComboBox;
  @FXML
  private ComboBox outColumnsComboBox;
  @FXML
  private ComboBox updateTypeComboBox;
  @FXML
  private TextField stockFileTextField;
  @FXML
  private Button stockFileButton;

  public Parent getView() {
    return root;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // actions
    excelFileInButton.setOnAction(e
        -> mainPresentationModel.setExcelFileIn(getPathFromUser("Open Excel In File")));
    excelFileOutButton.setOnAction(e
        -> mainPresentationModel.setExcelFileOut(getPathFromUser("Open Excel Out File")));
    stockFileButton.setOnAction(this::chooseStockTextFile);
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void postConstruct() {
    // bindings
    // - excel in field
    excelFileInTextField.textProperty().bind(mainPresentationModel.excelFileInProperty());
    // - populate sheetnames of excel in file
    inSheetComboBox.itemsProperty().bind(mainPresentationModel.excelFileInSheetNamesProperty());
//    // - selected sheetname of excel in file
//    mainPresentationModel.sheetNameInProperty().bind(inSheetComboBox.itemsProperty());
//    // - populate column names from in sheet
//    inColumnsChoiceBox.itemsProperty().bind(mainPresentationModel.inColumnsProperty());
//    // - selected in columns of excel in file
//    mainPresentationModel.inProperty().bind(inColumnsChoiceBox.selectionModelProperty());
//    // - populate stock columns from in sheet
//    stockColumnsComboBox.itemsProperty().bind(mainPresentationModel.inColumnsProperty());
//    // - selected stock column from excel in file
//    mainPresentationModel.stockColumnProperty().bind(stockColumnsComboBox.selectionModelProperty());
    // - excel out file
    excelFileOutTextField.textProperty().bind(mainPresentationModel.excelFileOutProperty());
//    // - populate sheetnames of excel out file
//    outSheetComboBox.itemsProperty().bind(mainPresentationModel.excelFileOutSheetNamesProperty());
//    // - selected sheetname of excel out file
//    mainPresentationModel.sheetNameOutProperty().bind(outSheetComboBox.itemsProperty());
//    // - populate column names from out sheet
//    outColumnsComboBox.itemsProperty().bind(mainPresentationModel.outColumnsProperty());
//    // - selected out column of excel out file
//    mainPresentationModel.outProperty().bind(outColumnsComboBox.itemsProperty());
    // TODO updateTypeComboBox bindings

    // - stock file field
    stockFileTextField.textProperty().bind(mainPresentationModel.stockFileProperty());
    // bindings to actions
    // businessController
  }

  private void chooseStockTextFile(final ActionEvent actionEvent) {
    final String path = getPathFromUser("Open Stock Text File");
    mainPresentationModel.setStockFile(path);
    // FIXME le code suivant doit passer dans le business controlleur
    try {
      Map<Long, Long> stock = stockCompute.stockStream(Files.lines(Paths.get(mainPresentationModel.getStockFile())));
      if (LOG.isDebugEnabled()) {
        LOG.debug("Résultat du stock");
        for (final Long barCode : stock.keySet()) {
          LOG.debug("barCode: {}; stock: {}", barCode, stock.get(barCode));
        }
      }
    } catch (IOException e) {
      LOG.warn("Une exception a eu lieu", e);
    }
    // FIXME test to populate the combobox/choicebox
    // mainPresentationModel.setInColumns(FXCollections.observableArrayList("test 1", "blahblah"));
  }

  private String getPathFromUser(final String title) {
    final FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(title);
    final File file = fileChooser.showOpenDialog(stage);
    return file != null ? file.toString() : null;
  }

  public void setStage(final Stage stage) {
    this.stage = stage;
  }

  public Stage getStage() {
    return stage;
  }
}
