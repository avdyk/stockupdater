package com.github.avdyk.stockupdater;

import java.util.List;

/**
 * Services autour d'une feuille excel.
 *
 * @author <a href="mailto:avd@mims.be">Arnaud Vandyck</a>
 * @since 24/08/2015 13:59
 */
public interface XSSFWorkbookService {

  List<String> getSheetsName();

  List<String> getColumnsNameForSheet(final String sheetName);

}
