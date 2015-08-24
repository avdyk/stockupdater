package com.github.avdyk.stockupdater.ui.javafx.controller;

import com.github.avdyk.stockupdater.ArticleService;
import com.github.avdyk.stockupdater.StockCompute;
import com.github.avdyk.stockupdater.conf.ConfImpl;
import com.github.avdyk.stockupdater.ui.javafx.MainPresentationModel;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
 * <p>
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
  @Autowired
  private ArticleService articleServiceIn;
  @Autowired
  private ArticleService articleServiceOut;
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
    excelFileInButton.setOnAction(this::chooseExcelIn);
    excelFileOutButton.setOnAction(this::chooseExcelOut);
    stockFileButton.setOnAction(this::chooseStockTextFile);
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void postConstruct() {
    // bindings
    // - excel in field
    excelFileInTextField.textProperty().bind(mainPresentationModel.excelFileInProperty());
    // - populate sheetnames of excel in file
//    inSheetComboBox.itemsProperty().bind(mainPresentationModel.excelFileInSheetNamesProperty());
    inSheetComboBox.valueProperty().bindBidirectional(mainPresentationModel.sheetNameInProperty());
    inSheetComboBox.setItems(mainPresentationModel.excelFileInSheetNamesProperty());
    inSheetComboBox.valueProperty().addListener(this::inSheetSelected);
//    // - selected sheetname of excel in file
//    mainPresentationModel.sheetNameInProperty().bind(inSheetComboBox.itemsProperty());
    // - populate column names from in sheet
    inColumnsChoiceBox.valueProperty().bind(mainPresentationModel.inColumnsProperty());
//    // - selected in columns of excel in file
//    mainPresentationModel.inProperty().bind(inColumnsChoiceBox.selectionModelProperty());
    // - populate stock columns from in sheet
    stockColumnsComboBox.valueProperty().bind(mainPresentationModel.inColumnsProperty());
//    // - selected stock column from excel in file
//    mainPresentationModel.stockColumnProperty().bind(stockColumnsComboBox.selectionModelProperty());
    // - excel out file
    excelFileOutTextField.textProperty().bind(mainPresentationModel.excelFileOutProperty());
    // - populate sheetnames of excel out file
    outSheetComboBox.valueProperty().bindBidirectional(mainPresentationModel.sheetNameOutProperty());
    outSheetComboBox.setItems(mainPresentationModel.excelFileOutSheetNamesProperty());
    outSheetComboBox.valueProperty().addListener(this::outSheetSelected);
//    // - selected sheetname of excel out file
//    mainPresentationModel.sheetNameOutProperty().bind(outSheetComboBox.itemsProperty());
    // - populate column names from out sheet
    outColumnsComboBox.valueProperty().bind(mainPresentationModel.outColumnsProperty());
//    // - selected out column of excel out file
//    mainPresentationModel.outProperty().bind(outColumnsComboBox.itemsProperty());
    // TODO updateTypeComboBox bindings

    // - stock file field
    stockFileTextField.textProperty().bind(mainPresentationModel.stockFileProperty());
    // bindings to actions
    // businessController
  }

  void chooseExcelIn(final ActionEvent actionEvent) {
    final String fileName = getPathFromUser("Open Excel In File");
    mainPresentationModel.setExcelFileIn(fileName);
    if (fileName instanceof String && StringUtils.isNotBlank(fileName)) {
      try {
        final XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(Paths.get(fileName)));
        articleServiceIn.setWorkbook(wb);
        mainPresentationModel
            .setExcelFileInSheetNames(FXCollections.observableArrayList(articleServiceIn.getSheetsName()));
      } catch (IOException e) {
        LOG.warn("File not found", e);
      }
    }

  }

  void chooseExcelOut(final ActionEvent actionEvent) {
    final String fileName = getPathFromUser("Open Excel Out File");
    mainPresentationModel.setExcelFileOut(fileName);
    if (fileName instanceof String && StringUtils.isNotBlank(fileName)) {
      try {
        final XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(Paths.get(fileName)));
        articleServiceOut.setWorkbook(wb);
        mainPresentationModel
            .setExcelFileOutSheetNames(FXCollections.observableArrayList(articleServiceOut.getSheetsName()));
      } catch (IOException e) {
        LOG.warn("File not found", e);
      }
    }

  }

  void chooseStockTextFile(final ActionEvent actionEvent) {
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
  }

  String getPathFromUser(final String title) {
    final FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(title);
    final File file = fileChooser.showOpenDialog(stage);
    return file != null ? file.toString() : null;
  }

  void inSheetSelected(ObservableValue observable, Object oldValue, Object newValue) {
    LOG.warn("selected in sheetname: {}", newValue);
    if (newValue instanceof String && StringUtils.isNotBlank((String) newValue)) {
      this.articleServiceIn.setSelectedSheetName((String) newValue);
      this.mainPresentationModel
          .setInColumns(FXCollections.observableArrayList(this.articleServiceIn.getColumnsName()));
    }
  }

  void outSheetSelected(ObservableValue observable, Object oldValue, Object newValue) {
    LOG.warn("selected out sheetname: {}", newValue);
    if (newValue instanceof String && StringUtils.isNotBlank((String) newValue)) {
      this.articleServiceOut.setSelectedSheetName((String) newValue);
      this.mainPresentationModel
          .setOutColumns(FXCollections.observableArrayList(this.articleServiceOut.getColumnsName()));
    }
  }

  public void setStage(final Stage stage) {
    this.stage = stage;
  }

  public Stage getStage() {
    return stage;
  }
}
