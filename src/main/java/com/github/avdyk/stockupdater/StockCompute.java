package com.github.avdyk.stockupdater;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Contient le stock du fichier des codes barres.
 *
 * @author <a href="mailto:avd@mims.be">Arnaud Vandyck</a>
 * @since 27/05/2015 15:35
 */
public interface StockCompute {

  /**
   * Retourne une map des codes barre et de leur stock.
   *  @param lines les lignes de codes barre.
   *
   */
  Map<Long, Long> stockStream(final Stream<String> lines);
}
