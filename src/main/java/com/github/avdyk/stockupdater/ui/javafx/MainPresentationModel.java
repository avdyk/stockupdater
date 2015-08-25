package com.github.avdyk.stockupdater.ui.javafx;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Represent the state of the view.
 *
 * Created by arnaud on 19/08/15.
 */
@Component
public class MainPresentationModel {

  private StringProperty excelFileIn = new SimpleStringProperty();
  private StringProperty excelFileOut = new SimpleStringProperty();
  private StringProperty stockFile = new SimpleStringProperty();
  // FIXME BINDINGS private UpdateType updateType;
  private ListProperty<String> excelFileInSheetNames = new SimpleListProperty<>();
  private StringProperty sheetNameIn = new SimpleStringProperty();
  private ListProperty<String> inColumns = new SimpleListProperty<>();
//  private ListProperty<String> in = new SimpleListProperty<>();
  private StringProperty in = new SimpleStringProperty();
  private StringProperty stockColumn = new SimpleStringProperty();
  private ListProperty<String> excelFileOutSheetNames = new SimpleListProperty<>();
  private StringProperty sheetNameOut = new SimpleStringProperty();
  private ListProperty<String> outColumns = new SimpleListProperty<>();
  private StringProperty out = new SimpleStringProperty();
  private BooleanProperty computable = new SimpleBooleanProperty();
  private BooleanProperty saveable = new SimpleBooleanProperty();

  public String getExcelFileIn() {
    return excelFileIn.get();
  }

  public StringProperty excelFileInProperty() {
    return excelFileIn;
  }

  public void setExcelFileIn(String excelFileIn) {
    this.excelFileIn.set(excelFileIn);
  }

  public String getExcelFileOut() {
    return excelFileOut.get();
  }

  public StringProperty excelFileOutProperty() {
    return excelFileOut;
  }

  public void setExcelFileOut(String excelFileOut) {
    this.excelFileOut.set(excelFileOut);
  }

  public String getStockFile() {
    return stockFile.get();
  }

  public StringProperty stockFileProperty() {
    return stockFile;
  }

  public void setStockFile(String stockFile) {
    this.stockFile.set(stockFile);
  }

  public String getSheetNameIn() {
    return sheetNameIn.get();
  }

  public StringProperty sheetNameInProperty() {
    return sheetNameIn;
  }

  public void setSheetNameIn(String sheetNameIn) {
    this.sheetNameIn.set(sheetNameIn);
  }

  public String getSheetNameOut() {
    return sheetNameOut.get();
  }

  public StringProperty sheetNameOutProperty() {
    return sheetNameOut;
  }

  public void setSheetNameOut(String sheetNameOut) {
    this.sheetNameOut.set(sheetNameOut);
  }

  public String getStockColumn() {
    return stockColumn.get();
  }

  public StringProperty stockColumnProperty() {
    return stockColumn;
  }

  public void setStockColumn(String stockColumn) {
    this.stockColumn.set(stockColumn);
  }

  public String getOut() {
    return out.get();
  }

  public StringProperty outProperty() {
    return out;
  }

  public void setOut(String out) {
    this.out.set(out);
  }

/*
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

  public StringProperty inProperty() {
    return in;
  }

  public void setIn(String in) {
    this.in.set(in);
  }

  public ObservableList<String> getInColumns() {
    return inColumns.get();
  }

  public ListProperty<String> inColumnsProperty() {
    return inColumns;
  }

  public void setInColumns(ObservableList<String> inColumns) {
    this.inColumns.set(inColumns);
  }

  public ObservableList<String> getOutColumns() {
    return outColumns.get();
  }

  public ListProperty<String> outColumnsProperty() {
    return outColumns;
  }

  public void setOutColumns(ObservableList<String> outColumns) {
    this.outColumns.set(outColumns);
  }

  public ObservableList<String> getExcelFileInSheetNames() {
    return excelFileInSheetNames.get();
  }

  public ListProperty<String> excelFileInSheetNamesProperty() {
    return excelFileInSheetNames;
  }

  public void setExcelFileInSheetNames(ObservableList<String> excelFileInSheetNames) {
    this.excelFileInSheetNames.set(excelFileInSheetNames);
  }

  public ObservableList<String> getExcelFileOutSheetNames() {
    return excelFileOutSheetNames.get();
  }

  public ListProperty<String> excelFileOutSheetNamesProperty() {
    return excelFileOutSheetNames;
  }

  public void setExcelFileOutSheetNames(ObservableList<String> excelFileOutSheetNames) {
    this.excelFileOutSheetNames.set(excelFileOutSheetNames);
  }

  public boolean getComputable() {
    return computable.get();
  }

  public BooleanProperty computableProperty() {
    return computable;
  }

  public void setComputable(boolean computable) {
    this.computable.set(computable);
  }

  public boolean getSaveable() {
    return saveable.get();
  }

  public BooleanProperty saveableProperty() {
    return saveable;
  }

  public void setSaveable(boolean saveable) {
    this.saveable.set(saveable);
  }
}
