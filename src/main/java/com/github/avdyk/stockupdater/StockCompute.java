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
   * Retourne un stream des entrées codeBarre et stock.
   *
   * @return un stream des entrées codeBarre et stock.
   */
  Stream<Map.Entry<Long, Long>> stockStream();
}
