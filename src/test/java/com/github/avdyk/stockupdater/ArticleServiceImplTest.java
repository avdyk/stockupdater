package com.github.avdyk.stockupdater;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Tests the article service.
 */
public class ArticleServiceImplTest {

    static ArticleService service;

    @BeforeClass
    public static void beforeClass() throws IOException {
        service = new ArticleService(Paths.get("target/test-classes/articles.xlsx"));
    }

    @Test
    public void sheetNamesTest() {
        List<String> sheetNames = service.getSheetNames();
        Assert.assertEquals(1, sheetNames.size());
        Assert.assertEquals("export articles mercator", sheetNames.get(0));
        Assert.assertFalse(sheetNames.contains("toto"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void selectedSheetFailTest() {
        service.setSelectedSheet("toto");
    }

    @Test
    public void selectedSheetTest() {
        service.setSelectedSheet("export articles mercator");
        Assert.assertEquals(16, service.getColumnNames().size());
    }

    // FIXME test "in's" (columns that does not exists)

    // FIXME test "in's" (no column)

    // FIXME test "in's" (good column name)

    // FIXME test "in's" (multiple good columns)

    // FIXME test "in's" (multiple columns with a bad one)

    // FIXME test out (column not found)

    // FIXME test out (good column)

    // FIXME update stock...

}
