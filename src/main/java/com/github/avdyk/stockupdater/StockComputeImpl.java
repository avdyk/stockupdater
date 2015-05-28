package com.github.avdyk.stockupdater;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Scope("prototype")
public class StockComputeImpl implements StockCompute {

	private static final Logger LOG = LoggerFactory.getLogger(StockComputeImpl.class);
  private Map<Long, Long> stockMap;
  @Autowired
	public StockComputeImpl(@Value("#{confImpl.stockFile}") Path stockFile) throws IOException {
		final Stream<String> lines = Files.lines(stockFile);
    // liste les lignes de nombres comme clés d'une 'map'; les valeurs sont le nombre d'occurences
		stockMap = lines.filter(NumberUtils::isNumber)
        .collect(
            Collectors.groupingBy(Long::parseLong, Collectors.counting())
        );
    if (LOG.isDebugEnabled()) {
      stockMap
          .forEach((k, v) -> LOG.debug("Code barre: {}, stock: {}", k, v));
    }
  }

  @Override
  public Map<Long, Long> stockStream() {
    return new HashMap<>(stockMap);
  }

}
