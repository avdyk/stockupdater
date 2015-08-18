package com.github.avdyk.stockupdater.ui.javafx.controller;

import com.github.avdyk.stockupdater.conf.ConfImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Controller for the main frame.
 *
 * Created by arnaud on 18/08/15.
 */
@Component()
public class MainFrameController {

  @Autowired
  ConfImpl configuration;

}
