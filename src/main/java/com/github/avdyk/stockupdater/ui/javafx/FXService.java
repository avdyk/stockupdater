package com.github.avdyk.stockupdater.ui.javafx;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Created by arnaud on 12/09/15.
 */
public class FXService extends Service {

  Task<?> task;

  public void setTask(Task<?> task) {
    this.task = task;
  }

  @Override
  protected Task createTask() {
    return task;
  }
}
