package com.github.avdyk.stockupdater;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StockComputeImpl implements StockCompute {

  private static final Logger logger = LoggerFactory.getLogger(StockComputeImpl.class);

  @Override
  public Map<Long, Long> stockStream(final Stream<String> lines) {
    logger.info("Computing stock old");
    // liste les lignes de nombres comme clés d'une 'map'; les valeurs sont le nombre d'occurences
    final Map<Long, Long> stockMap = lines.filter(NumberUtils::isNumber)
        .mapToLong(NumberUtils::toLong)
        .filter(l -> l != 0)
        .mapToObj(Long::new)
        .collect(
            Collectors.groupingBy(Long::new, Collectors.counting())
        );
    if (logger.isDebugEnabled()) {
      stockMap
          .forEach((k, v) -> logger.debug("Code barre: {}, stock: {}", k, v));
    }
    return stockMap;
  }

}
