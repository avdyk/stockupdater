package com.github.avdyk.stockupdater.ui.console;

import com.github.avdyk.stockupdater.StockCompute;
import com.github.avdyk.stockupdater.conf.ConfImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;
import java.util.stream.Stream;

public class ConsoleApp {

	private final static Logger LOG = LoggerFactory.getLogger(ConsoleApp.class);

	public static void main(String[] args) {
		LOG.info("Application Stock Updater start");
		final ApplicationContext ctx = new AnnotationConfigApplicationContext(ConfImpl.class);
		final StockCompute stockCompute = ctx.getBean(StockCompute.class);
    final Stream<Map.Entry<Long, Long>> stock = stockCompute.stockStream();
    stock.forEach(e -> LOG.info("code: {}; stock: {}", e.getKey(), e.getValue()));
		// -- la suite
		LOG.info("Application Stock Updater stop");
	}

}
