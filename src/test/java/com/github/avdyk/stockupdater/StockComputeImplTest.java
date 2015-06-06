package com.github.avdyk.stockupdater;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Tests the stock service.
 */
public class StockComputeImplTest {

    private static Map<Long, Long> STOCK;

    @BeforeClass
    public static void beforeClass() throws IOException {
        final StockComputeImpl stockCompute = new StockComputeImpl();
        STOCK = stockCompute.stockStream(Files.lines(Paths.get("target/test-classes/stock.txt")));
    }

    @Test
    public void computeStock() throws IOException {
        Assert.assertEquals(11, STOCK.size());
    }

    @Test
    public void _08118_stock_1() {
        Assert.assertEquals(new Long(1), STOCK.get(8118L));
    }

    @Test
    public void _1111_stock_null() {
        Assert.assertNull(STOCK.get(1111L));
    }

    @Test
    public void _4950344996964_stock_1() {
        Assert.assertEquals(new Long(1), STOCK.get(4950344996964L));
    }

    @Test
    public void _08702_stock_5() {
        Assert.assertEquals(new Long(5), STOCK.get(8702L));
    }

}
