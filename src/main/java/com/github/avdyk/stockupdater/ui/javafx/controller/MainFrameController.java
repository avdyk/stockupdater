package com.github.avdyk.stockupdater.ui.javafx.controller;

import com.github.avdyk.stockupdater.StockCompute;
import com.github.avdyk.stockupdater.StockService;
import com.github.avdyk.stockupdater.UpdateType;
import com.github.avdyk.stockupdater.conf.ConfImpl;
import com.github.avdyk.stockupdater.ui.javafx.FXService;
import com.github.avdyk.stockupdater.ui.javafx.JavaFxControllerFactory;
import com.github.avdyk.stockupdater.ui.javafx.MainPresentationModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

  private FXService fxService = new FXService();

  private Stage stage;
  @FXML
  private StackPane root;
  @FXML
  private TextField excelFileInTextField;
  @FXML
  private ComboBox<String> inSheetComboBox;
  @FXML
  private ComboBox<String> inColumnsComboBox;
  @FXML
  private ComboBox<String> in2ColumnsComboBox;
  @FXML
  private ComboBox<String> labelColumnComboBox;
  @FXML
  private ComboBox<String> stockColumnsComboBox;
  @FXML
  private ComboBox<String> outColumnsComboBox;
  @FXML
  private ComboBox<UpdateType> updateTypeComboBox;
  @FXML
  private TextField stockFileTextField;
  @FXML
  private TextField scanTextField;
  @FXML
  private TextArea logOutput;
  @FXML
  private MenuItem executeInventoryMenuItem;
  @FXML
  private MenuItem saveButton;
  @FXML
  private MenuItem saveModifiedRowsButton;
  @FXML
  private MenuItem saveCSVButton;
  @FXML
  private MenuItem saveModifiedRowsToCSVButton;
  @FXML
  private Label serviceLogLabel;
  @FXML
  private ProgressBar serviceProgress;

  private Map<Long, Long> stockComputed;
  private boolean cursorBinded;

  public Parent getView() {
    return root;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // nothing to do at the moment
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
    inColumnsComboBox.setItems(mainPresentationModel.inColumnsProperty());
    inColumnsComboBox.valueProperty().addListener(this::inColumnSelected);
    // - selected in columns of excel in file
    inColumnsComboBox.valueProperty().bindBidirectional(mainPresentationModel.inProperty());
    // - populate column names from in sheet
    in2ColumnsComboBox.setItems(mainPresentationModel.inColumnsProperty());
    in2ColumnsComboBox.valueProperty().addListener(this::in2ColumnSelected);
    // - selected in columns of excel in file
    in2ColumnsComboBox.valueProperty().bindBidirectional(mainPresentationModel.in2Property());
    // - populate column names from in sheet
    labelColumnComboBox.setItems(mainPresentationModel.inColumnsProperty());
    labelColumnComboBox.valueProperty().addListener(this::labelColumnSelected);
    // - selected label column of excel in file
    labelColumnComboBox.valueProperty().bindBidirectional(mainPresentationModel.labelColumnProperty());
    // - populate stock columns from in sheet
    stockColumnsComboBox.setItems(mainPresentationModel.inColumnsProperty());
    stockColumnsComboBox.valueProperty().addListener(this::stockColumnSelected);
    // - selected stock column from excel in file
    stockColumnsComboBox.valueProperty().bindBidirectional(mainPresentationModel.stockColumnProperty());
    // - populate column names from out sheet
    outColumnsComboBox.setItems(mainPresentationModel.inColumnsProperty());
    outColumnsComboBox.valueProperty().addListener(this::outColumnSelected);
    // - selected out column of excel out file
    outColumnsComboBox.valueProperty().bindBidirectional(mainPresentationModel.outProperty());
    // - updateTypeComboBox bindings
    updateTypeComboBox.setItems(FXCollections.observableArrayList(UpdateType.values()));
    updateTypeComboBox.valueProperty().bindBidirectional(mainPresentationModel.updateTypeProperty());
    mainPresentationModel.setUpdateType(UpdateType.TEST);
    // - stock file field
    stockFileTextField.textProperty().bind(mainPresentationModel.stockFileProperty());
    scanTextField.textProperty().addListener(
        (observable, oldValue, newValue) -> {
          if (!newValue.matches("\\d*")) {
            ((StringProperty) observable).setValue(oldValue);
          }
        }
    );
    logOutput.textProperty().bind(mainPresentationModel.logOutputProperty());
    // registrer the appender

    // bindings to button executeInventory enable
    executeInventoryMenuItem.disableProperty().bind(Bindings.not(mainPresentationModel.computableProperty()));
    // bindings to button save
    saveButton.disableProperty().bind(Bindings.not(mainPresentationModel.inventoryExecutedProperty()));
    saveModifiedRowsButton.disableProperty().bind(Bindings.not(mainPresentationModel.inventoryExecutedProperty()));
    saveCSVButton.disableProperty().bind(Bindings.not(mainPresentationModel.inventoryExecutedProperty()));
    saveModifiedRowsToCSVButton.disableProperty().bind(Bindings.not(mainPresentationModel.inventoryExecutedProperty()));

    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
    // threaded service bindings
    serviceLogLabel.textProperty().bind(fxService.stateProperty().asString());
    serviceProgress.visibleProperty().bind(fxService.runningProperty());
  }

  @FXML
  @SuppressWarnings("unused")
  void chooseExcelIn(final ActionEvent actionEvent) {
    final String fileName = getPathFromUser(JavaFxControllerFactory
        .MAIN_FRAME_RESOURCE_BUNDLE.getString("ui.in.excel.file.dialog.title"));
    mainPresentationModel.setExcelFileIn(fileName);
    if (fileName != null && StringUtils.isNotBlank(fileName)) {
      prepareCursorBindings();
      fxService.setTask(new Task<Void>() {
        @Override
        protected Void call() throws Exception {
          try (InputStream instream = Files.newInputStream(Paths.get(fileName))) {
            final XSSFWorkbook wb = new XSSFWorkbook(instream);
            stockService.getInService().setWorkbook(wb);
            stockService.getIn2Service().setWorkbook(wb);
            stockService.getStockService().setWorkbook(wb);
            stockService.getOutService().setWorkbook(wb);
            mainPresentationModel
                .setExcelFileInSheetNames(
                    FXCollections.observableArrayList(stockService.getInService().getSheetsName()));
            mainPresentationModel.setInventoryExecuted(false);
          } catch (IOException e) {
            LOG.warn("File not found", e);
          }
          return null;
        }
      });
      fxService.restart();
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  @FXML
  @SuppressWarnings("unused")
  void chooseStockTextFile(final ActionEvent actionEvent) {
    final String path = getPathFromUser(JavaFxControllerFactory
        .MAIN_FRAME_RESOURCE_BUNDLE.getString("ui.stock.file.dialog.title"));
    prepareCursorBindings();
    fxService.setTask(new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        try {
          updateMessage("Loading Stock file");
          stockComputed =
              stockCompute.stockStream(Files.lines(Paths.get(path)));
          mainPresentationModel.setStockFile(path);
          mainPresentationModel.setInventoryExecuted(false);
          if (LOG.isDebugEnabled()) {
            LOG.debug("Résultat du stock");
            for (final Long barCode : stockComputed.keySet()) {
              LOG.debug("barCode: {}; stock: {}", barCode, stockComputed.get(barCode));
            }
          }
        } catch (IOException e) {
          LOG.warn("Une exception a eu lieu", e);
        }
        return null;
      }
    });
    fxService.restart();
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
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
      this.stockService.getIn2Service().setSelectedSheetName(sheetName);
      this.stockService.getStockService().setSelectedSheetName(sheetName);
      this.stockService.getOutService().setSelectedSheetName(sheetName);
      List<String> cols = this.stockService.getInService().getColumnsName();
      if (LOG.isTraceEnabled()) {
        StringJoiner sj = new StringJoiner(", ", "Columns name: ", ".");
        cols.forEach(sj::add);
        LOG.trace(sj.toString());
      }
      ObservableList<String> obs = FXCollections.observableArrayList(cols);
      this.mainPresentationModel.setInColumns(obs);
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  void inColumnSelected(final ObservableValue observableValue, final Object oldValue, final Object newValue) {
    LOG.info("selected in column names: {}; in service: {}", newValue,
        this.stockService.getInService().getSelectedColumnName());
    if (newValue instanceof String) {
      this.stockService.getInService().setSelectedColumn((String) newValue);
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  void in2ColumnSelected(final ObservableValue observableValue, final Object oldValue, final Object newValue) {
    LOG.info("selected in2 column names: {}; in service: {}", newValue,
        this.stockService.getIn2Service().getSelectedColumnName());
    if (newValue instanceof String) {
      this.stockService.getIn2Service().setSelectedColumn((String) newValue);
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  void labelColumnSelected(final ObservableValue observableValue, final Object oldValue, final Object newValue) {
    LOG.info("selected label column name: {}", newValue);
    if (newValue instanceof String) {
      this.stockService.getInService().setSelectedLabelColumn((String) newValue);
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  void outColumnSelected(final ObservableValue observableValue, final Object oldValue, final Object newValue) {
    LOG.info("selected out column name: {}", newValue);
    if (newValue instanceof String) {
      this.stockService.getOutService().setSelectedColumn((String) newValue);
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  void stockColumnSelected(final ObservableValue observableValue, final Object oldValue, final Object newValue) {
    LOG.info("selected stock column name: {}", newValue);
    if (newValue instanceof String) {
      this.stockService.getStockService().setSelectedColumn((String) newValue);
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  @FXML
  @SuppressWarnings("unused") // called by fxml
  void executeInventory(final ActionEvent actionEvent) {
    LOG.info("executeInventory");
    stockService.setStock(stockComputed);
    prepareCursorBindings();
    fxService.setTask(new Task<Void>() {

      @Override
      protected Void call() throws Exception {
        try {
          stockService.call();
          mainPresentationModel.setInventoryExecuted(true);
        } catch (Exception e) {
          LOG.error("Problem updating stock!", e);
        }
        return null;
      }
    });
    fxService.restart();
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  @FXML
  @SuppressWarnings("unused") // called by fxml
  void save(final ActionEvent actionEvent) {
    final String outputFilename = getOutputFilename(".xlsx");
    LOG.info("save in file: {}", outputFilename);
    try (OutputStream outStream = Files.newOutputStream(
        Paths.get(outputFilename), StandardOpenOption.WRITE)) {
      stockService.writeExcelWorkbook(outStream);
      outStream.flush();
      LOG.info("File {} has been saved", outputFilename);
    } catch (IOException e) {
      LOG.error("Problem writing file {}", outputFilename, e);
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  @FXML
  @SuppressWarnings("unused")
    // called by fxml
  void saveModifiedRows(final ActionEvent actionEvent) {
    final String outputFilename = getOutputFilename("-new-stock.xlsx");
    LOG.info("save in file: {}", outputFilename);
    try (OutputStream outStream = Files.newOutputStream(
        Paths.get(outputFilename), StandardOpenOption.WRITE)) {
      stockService.writeExcelWorkbookModifiedRows(outStream);
      outStream.flush();
      LOG.info("File {} has been saved", outputFilename);
    } catch (IOException e) {
      LOG.error("Problem writing file {}", outputFilename, e);
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  @FXML
  @SuppressWarnings("unused") // called by fxml
  void saveCSV(final ActionEvent actionEvent) {
    final String outputFilename = getOutputFilename(".csv");
    LOG.info("save in CSV file {}", outputFilename);
    try (BufferedWriter out = new BufferedWriter(Files.newBufferedWriter(
        Paths.get(outputFilename), StandardOpenOption.WRITE))) {
      stockService.writeCSV(out);
      out.flush();
    } catch (IOException e) {
      LOG.error("Problem writing file {}", outputFilename, e);
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  @FXML
  @SuppressWarnings("unused") // called by fxml
  void saveModifiedRowsToCSV(final ActionEvent actionEvent) {
    final String outputFilename = getOutputFilename("-modified.csv");
    LOG.info("save in modified rows to CSV file {}", outputFilename);
    try (BufferedWriter out = new BufferedWriter(Files.newBufferedWriter(
        Paths.get(outputFilename), StandardOpenOption.WRITE))) {
      stockService.writeModifiedRowsToCSV(out);
      out.flush();
    } catch (IOException e) {
      LOG.error("Problem writing file {}", outputFilename, e);
    }
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }

  String getOutputFilename(final String suffixe) {
    final String filename = this.mainPresentationModel.getExcelFileIn();
    final String filenameWithoutTheDot = filename.substring(0, filename.lastIndexOf('.'));
    final String bySeconds = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
    final String outputFilename = String.format("%s-%s%s", filenameWithoutTheDot, bySeconds, suffixe);
    final Path outPath = Paths.get(outputFilename);
    if (!Files.exists(outPath)) {
      try {
        Files.createFile(outPath);
      } catch (IOException e) {
        LOG.error("Problem creating the path: {}", outputFilename);
      }
    }
    return outputFilename;
  }

  @FXML
  @SuppressWarnings("unused") // called by fxml
  void clear(final ActionEvent actionEvent) {
    LOG.info("clear");
    mainPresentationModel.setLogOutput("");
    // request focus on the scan textfield
    Platform.runLater(scanTextField::requestFocus);
  }


  @FXML
  @SuppressWarnings("unused") // called by fxml
  public void scanAction(final ActionEvent actionEvent) {
    final String scanText = this.scanTextField.getText();
    if (StringUtils.isNotBlank(scanText)) {
      LOG.info("Direct Scan: {}", scanText);
      final Map<Long, Long> lightStock = new HashMap<>();
      final Long id = Long.parseLong(scanText);
      lightStock.put(id, 1L);
      stockService.setStock(lightStock);
      this.scanTextField.clear();
      // request focus on the scan textfield
      Platform.runLater(scanTextField::requestFocus);
    }
  }

  public void setStage(final Stage stage) {
    this.stage = stage;
  }

  private void prepareCursorBindings() {
    if (!cursorBinded
        && stage != null
        && stage.getScene() != null
        && stage.getScene().getRoot() != null
        && stage.getScene().getRoot().cursorProperty() != null) {
      stage.getScene().getRoot().cursorProperty()
          .bind(Bindings.when(fxService.runningProperty())
              .then(javafx.scene.Cursor.WAIT)
              .otherwise(javafx.scene.Cursor.DEFAULT));
      cursorBinded = true;
    }
  }

  @FXML
  public void quit() {
    LOG.info("Quit");
    stage.close();
  }

}
