package com.github.avdyk.stockupdater.ui.javafx.controller;

import com.github.avdyk.stockupdater.StockCompute;
import com.github.avdyk.stockupdater.StockService;
import com.github.avdyk.stockupdater.UpdateType;
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
import org.springframework.context.MessageSource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

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
  private StockService stockService;
/*
  @Autowired
  private MessageSource messageSource;
*/

  private Stage stage;
  @FXML
  private StackPane root;
  @FXML
  private Button excelFileInButton;
  @FXML
  private TextField excelFileInTextField;
  @FXML
  private ComboBox<String> inSheetComboBox;
  @FXML
  private ChoiceBox<String> inColumnsChoiceBox;
  @FXML
  private ComboBox<String> stockColumnsComboBox;
  @FXML
  private TextField excelFileOutTextField;
  @FXML
  private Button excelFileOutButton;
  @FXML
  private ComboBox<String> outSheetComboBox;
  @FXML
  private ComboBox<String> outColumnsComboBox;
  @FXML
  private ComboBox<UpdateType> updateTypeComboBox;
  @FXML
  private TextField stockFileTextField;
  @FXML
  private Button stockFileButton;
  @FXML
  private Button computeButton;
  @FXML
  private Button saveButton;
  @FXML
  private TextField outputLog;
  @FXML
  private Button clearButton;

  private Map<Long, Long> stockComputed;

  public Parent getView() {
    return root;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // actions
    // TODO put onAction="#" in fxml
/*
    LOG.info(messageSource.getMessage("ui.in.excel.file.label", null, Locale.getDefault()));
*/
    excelFileInButton.setOnAction(this::chooseExcelIn);
    excelFileOutButton.setOnAction(this::chooseExcelOut);
    stockFileButton.setOnAction(this::chooseStockTextFile);
    computeButton.setOnAction(this::compute);
    saveButton.setOnAction(this::save);
    clearButton.setOnAction(this::clear);
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void postConstruct() {
    // bindings
    // - excel in field
    excelFileInTextField.textProperty().bind(mainPresentationModel.excelFileInProperty());
    // - populate sheetnames of excel in file
    inSheetComboBox.setItems(mainPresentationModel.excelFileInSheetNamesProperty());
    inSheetComboBox.valueProperty().addListener(this::inSheetSelected);
    // - selected sheetname of excel in file
    inSheetComboBox.valueProperty().bindBidirectional(mainPresentationModel.sheetNameInProperty());
    // - populate column names from in sheet
    inColumnsChoiceBox.setItems(mainPresentationModel.inColumnsProperty());
    inColumnsChoiceBox.valueProperty().addListener(this::inColumnSelected);
    // - selected in columns of excel in file
    inColumnsChoiceBox.valueProperty().bindBidirectional(mainPresentationModel.inProperty());
    // - populate stock columns from in sheet
    stockColumnsComboBox.setItems(mainPresentationModel.inColumnsProperty());
    stockColumnsComboBox.valueProperty().addListener(this::stockColumnSelected);
    // - selected stock column from excel in file
    stockColumnsComboBox.valueProperty().bindBidirectional(mainPresentationModel.stockColumnProperty());
    // - excel out file
    excelFileOutTextField.textProperty().bind(mainPresentationModel.excelFileOutProperty());
    // - populate sheetnames of excel out file
    outSheetComboBox.setItems(mainPresentationModel.excelFileOutSheetNamesProperty());
    outSheetComboBox.valueProperty().addListener(this::outSheetSelected);
    // - selected sheetname of excel out file
    outSheetComboBox.valueProperty().bindBidirectional(mainPresentationModel.sheetNameOutProperty());
    // - populate column names from out sheet
    outColumnsComboBox.setItems(mainPresentationModel.outColumnsProperty());
    outColumnsComboBox.valueProperty().addListener(this::outColumnSelected);
    // - selected out column of excel out file
    outColumnsComboBox.valueProperty().bindBidirectional(mainPresentationModel.outProperty());
    // - updateTypeComboBox bindings
    updateTypeComboBox.setItems(FXCollections.observableArrayList(UpdateType.values()));
    updateTypeComboBox.valueProperty().bindBidirectional(mainPresentationModel.updateTypeProperty());
    // - stock file field
    stockFileTextField.textProperty().bind(mainPresentationModel.stockFileProperty());
    // TODO bindings to button save and button compute enable
  }

  void chooseExcelIn(final ActionEvent actionEvent) {
    final String fileName = getPathFromUser("Open Excel In File");
    mainPresentationModel.setExcelFileIn(fileName);
    if (fileName != null && StringUtils.isNotBlank(fileName)) {
      try {
        final XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(Paths.get(fileName)));
        this.stockService.getInService().setWorkbook(wb);
        this.stockService.getStockService().setWorkbook(wb);
        mainPresentationModel
            .setExcelFileInSheetNames(
                FXCollections.observableArrayList(this.stockService.getInService().getSheetsName()));
      } catch (IOException e) {
        LOG.warn("File not found", e);
      }
    }

  }

  void chooseExcelOut(final ActionEvent actionEvent) {
    final String fileName = getPathFromUser("Open Excel Out File");
    mainPresentationModel.setExcelFileOut(fileName);
    if (fileName != null && StringUtils.isNotBlank(fileName)) {
      try {
        final XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(Paths.get(fileName)));
        this.stockService.getOutService().setWorkbook(wb);
        mainPresentationModel
            .setExcelFileOutSheetNames(
                FXCollections.observableArrayList(this.stockService.getOutService().getSheetsName()));
      } catch (IOException e) {
        LOG.warn("File not found", e);
      }
    }

  }

  void chooseStockTextFile(final ActionEvent actionEvent) {
    final String path = getPathFromUser("Open Stock Text File");
    mainPresentationModel.setStockFile(path);
    try {
      stockComputed =
          stockCompute.stockStream(Files.lines(Paths.get(mainPresentationModel.getStockFile())));
      if (LOG.isDebugEnabled()) {
        LOG.debug("Résultat du stock");
        for (final Long barCode : stockComputed.keySet()) {
          LOG.debug("barCode: {}; stock: {}", barCode, stockComputed.get(barCode));
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
    LOG.debug("selected in sheetname: {}", newValue);
    if (newValue instanceof String && StringUtils.isNotBlank((String) newValue)) {
      final String sheetName = (String) newValue;
      this.stockService.getInService().setSelectedSheetName(sheetName);
      this.stockService.getStockService().setSelectedSheetName(sheetName);
      List<String> cols = this.stockService.getInService().getColumnsName();
      if (LOG.isTraceEnabled()) {
        StringJoiner sj = new StringJoiner(", ", "Columns name: ", ".");
        for (String c : cols) {
          sj.add(c);
        }
        LOG.trace(sj.toString());
      }
      ObservableList<String> obs = FXCollections.observableArrayList(cols);
      this.mainPresentationModel.setInColumns(obs);
    }
  }

  void outSheetSelected(ObservableValue observable, Object oldValue, Object newValue) {
    LOG.debug("selected out sheetname: {}", newValue);
    if (newValue instanceof String && StringUtils.isNotBlank((String) newValue)) {
      this.stockService.getOutService().setSelectedSheetName((String) newValue);
      this.mainPresentationModel
          .setOutColumns(FXCollections.observableArrayList(this.stockService.getOutService().getColumnsName()));
    }
  }

  void inColumnSelected(final ObservableValue observableValue, final Object oldValue, final Object newValue) {
    LOG.debug("selected in column names: {}; in service: {}", newValue,
        this.stockService.getInService().getSelectedColumnName());
  }

  void outColumnSelected(final ObservableValue observableValue, final Object oldValue, final Object newValue) {
    LOG.debug("selected out column name: {}", newValue);
  }

  void stockColumnSelected(final ObservableValue observableValue, final Object oldValue, final Object newValue) {
    LOG.debug("selected stock column name: {}", newValue);
  }

  void compute(final ActionEvent actionEvent) {
    LOG.debug("compute");
    stockService.updateStock(mainPresentationModel.getUpdateType(), stockComputed);
    // TODO mettre un feed back dans l'interface pour afficher les id's du stock qui n'ont pas été trouvés

  }

  void save(final ActionEvent actionEvent) {
    LOG.debug("save");
    final String filename = this.mainPresentationModel.getExcelFileOut();
    try {
      OutputStream outStream = Files.newOutputStream(
          Paths.get(filename), StandardOpenOption.WRITE);
    stockService.writeExcelWorkbook(outStream);
      outStream.flush();
      outStream.close();
    } catch (IOException e) {
      LOG.error(String.format("Problem writing file %s", filename));
    }
  }

  void clear(final ActionEvent actionEvent) {
    LOG.debug("clear");
    outputLog.setText("");
  }

  public void setStage(final Stage stage) {
    this.stage = stage;
  }

  public Stage getStage() {
    return stage;
  }
}
