package com.github.avdyk.stockupdater.ui.javafx;

import com.github.avdyk.stockupdater.UpdateType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;

/**
 * Represent the state of the view.
 *
 * Created by arnaud on 19/08/15.
 */
@Component
public class MainPresentationModel {

  private StringProperty excelFileIn = new SimpleStringProperty();
  private StringProperty stockFile = new SimpleStringProperty();
  private ObjectProperty<UpdateType> updateType = new SimpleObjectProperty<>();
  private ListProperty<String> excelFileInSheetNames = new SimpleListProperty<>();
  private StringProperty sheetNameIn = new SimpleStringProperty();
  private ListProperty<String> inColumns = new SimpleListProperty<>();
  private ListProperty<String> in2Columns = new SimpleListProperty<>();
  //  private ListProperty<String> in = new SimpleListProperty<>();
  private StringProperty labelColumn = new SimpleStringProperty();
  private StringProperty in = new SimpleStringProperty();
  private StringProperty in2 = new SimpleStringProperty();
  private StringProperty stockColumn = new SimpleStringProperty();
  private ListProperty<String> outColumns = new SimpleListProperty<>();
  private StringProperty out = new SimpleStringProperty();
  private BooleanBinding computable = Bindings.and(excelFileIn.isNotEmpty(), sheetNameIn.isNotEmpty())
      .and(Bindings.or(
          Bindings.and(updateType.isEqualTo(UpdateType.UPDATE),
              Bindings.and(out.isNotEmpty(), stockFile.isNotEmpty())),
          Bindings.and(
              Bindings.or(updateType.isEqualTo(UpdateType.ADD), updateType.isEqualTo(UpdateType.SUBSTRACT)),
              Bindings.and(out.isNotEmpty(), stockFile.isNotEmpty()).and(stockColumn.isNotEmpty()))
      ));
  private BooleanProperty inventoryExecuted = new SimpleBooleanProperty();
  private StringProperty logOutput = new SimpleStringProperty();

  public String getExcelFileIn() {
    return excelFileIn.get();
  }

  public void setExcelFileIn(String excelFileIn) {
    this.excelFileIn.set(excelFileIn);
  }

  public StringProperty excelFileInProperty() {
    return excelFileIn;
  }

  public String getStockFile() {
    return stockFile.get();
  }

  public void setStockFile(String stockFile) {
    this.stockFile.set(stockFile);
  }

  public StringProperty stockFileProperty() {
    return stockFile;
  }

  public String getSheetNameIn() {
    return sheetNameIn.get();
  }

  public void setSheetNameIn(String sheetNameIn) {
    this.sheetNameIn.set(sheetNameIn);
  }

  public StringProperty sheetNameInProperty() {
    return sheetNameIn;
  }

  public String getStockColumn() {
    return stockColumn.get();
  }

  public void setStockColumn(String stockColumn) {
    this.stockColumn.set(stockColumn);
  }

  public StringProperty stockColumnProperty() {
    return stockColumn;
  }

  public String getOut() {
    return out.get();
  }

  public void setOut(String out) {
    this.out.set(out);
  }

  public StringProperty outProperty() {
    return out;
  }

/* TODO multi selection list
  public ObservableList<String> getIn() {
    return in.get();
  }

  public ListProperty<String> inProperty() {
    return in;
  }

  public void setIn(ObservableList<String> in) {
    this.in.set(in);
  }
*/

  public String getIn() {
    return in.get();
  }

  public void setIn(String in) {
    this.in.set(in);
  }

  public StringProperty inProperty() {
    return in;
  }

  public String getIn2() {
    return in2.get();
  }

  public void setIn2(String in2) {
    this.in2.set(in2);
  }

  public StringProperty in2Property() {
    return in2;
  }

  public ObservableList<String> getInColumns() {
    return inColumns.get();
  }

  public void setInColumns(ObservableList<String> inColumns) {
    this.inColumns.set(inColumns);
  }

  public ObservableList<String> getIn2Columns() {
    return in2Columns.get();
  }

  public void setIn2Columns(ObservableList<String> inColumns) {
    this.in2Columns.set(inColumns);
  }

  public ListProperty<String> inColumnsProperty() {
    return inColumns;
  }

  public ListProperty<String> in2ColumnsProperty() {
    return in2Columns;
  }

  public String getLabelColumn() {
    return labelColumn.get();
  }

  public void setLabelColumn(String labelColumn) {
    this.labelColumn.set(labelColumn);
  }

  public StringProperty labelColumnProperty() {
    return labelColumn;
  }

  public ObservableList<String> getOutColumns() {
    return outColumns.get();
  }

  public void setOutColumns(ObservableList<String> outColumns) {
    this.outColumns.set(outColumns);
  }

  public ListProperty<String> outColumnsProperty() {
    return outColumns;
  }

  public ObservableList<String> getExcelFileInSheetNames() {
    return excelFileInSheetNames.get();
  }

  public void setExcelFileInSheetNames(ObservableList<String> excelFileInSheetNames) {
    this.excelFileInSheetNames.set(excelFileInSheetNames);
  }

  public ListProperty<String> excelFileInSheetNamesProperty() {
    return excelFileInSheetNames;
  }

  public UpdateType getUpdateType() {
    return updateType.get();
  }

  public void setUpdateType(UpdateType updateType) {
    this.updateType.set(updateType);
  }

  public ObjectProperty<UpdateType> updateTypeProperty() {
    return updateType;
  }

  public String getLogOutput() {
    return logOutput.get();
  }

  public void setLogOutput(String logOutput) {
    this.logOutput.set(logOutput);
  }

  public StringProperty logOutputProperty() {
    return logOutput;
  }

  public boolean isComputable() {
    return computable.get();
  }

  public ObservableBooleanValue computableProperty() {
    return computable;
  }

  public boolean isInventoryExecuted() {
    return inventoryExecuted.get();
  }

  public void setInventoryExecuted(boolean inventoryExecuted) {
    this.inventoryExecuted.set(inventoryExecuted);
  }

  public BooleanProperty inventoryExecutedProperty() {
    return inventoryExecuted;
  }
}
