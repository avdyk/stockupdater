package com.github.avdyk.stockupdater.ui;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.github.avdyk.stockupdater.ui.javafx.MainPresentationModel;

/**
 * Appender permettant de logger
 *
 * @author <a href="mailto:avd@mims.be">Arnaud Vandyck</a>
 * @since 26/08/2015 13:29
 */
public class StringAppender extends AppenderBase<ILoggingEvent> {

  private MainPresentationModel presentationModel;

  @Override
  protected void append(ILoggingEvent eventObject) {
    if (presentationModel != null) {
      StringBuilder sb = new StringBuilder(presentationModel.getLogOutput());
      sb.append(eventObject.getLevel().levelStr).append(" -- ").append(eventObject.getMessage()).append('\n');
      presentationModel.setLogOutput(sb.toString());
    } else {
      System.out.println(eventObject.getClass().getName());
    }
  }

  public void setPresentationModel(final MainPresentationModel presentationModel) {
    this.presentationModel = presentationModel;
  }
}
