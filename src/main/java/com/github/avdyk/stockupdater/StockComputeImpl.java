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

	private static final Logger LOG = LoggerFactory.getLogger(StockComputeImpl.class);

  @Override
  public Map<Long, Long> stockStream(final Stream<String> lines) {
    // liste les lignes de nombres comme clés d'une 'map'; les valeurs sont le nombre d'occurences
    final Map<Long, Long> stockMap = lines.filter(NumberUtils::isNumber)
        .collect(
            Collectors.groupingBy(Long::parseLong, Collectors.counting())
        );
    if (LOG.isDebugEnabled()) {
      stockMap
          .forEach((k, v) -> LOG.debug("Code barre: {}, stock: {}", k, v));
    }
    return stockMap;
  }

}
