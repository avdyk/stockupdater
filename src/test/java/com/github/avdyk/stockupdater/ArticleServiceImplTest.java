package com.github.avdyk.stockupdater;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the article service.
 */
public class ArticleServiceImplTest {

  public static final List<String> ALL_IN_COLUMNS = Collections.unmodifiableList(Arrays.asList
      ("s_modele", "s_cle1", "s_cle2", "s_cle3", "s_id", "s_modelen", "s_id_rayon",
          "s_id_famil", "s_id_ssfam", "s_qdispo", "s_qv_1", "s_qv_2", "s_qv_3",
          "s_qa_1", "s_qa_2", "newstock"));
  public static final List<String> IN_COLUMNS = Collections.unmodifiableList(Arrays.asList
      ("s_cle1", "s_cle2", "s_cle3", "s_id"));
  public static final String MAIN_SHEET = "export articles mercator";
  public static final String NEW_STOCK = "newstock";
  private static ArticleService service;
  private static Map<Long, Long> STOCK_4950344996964 = new HashMap<>();
  private static Map<Long, Long> STOCK_08702 = new HashMap<>();

    @BeforeClass
    public static void beforeClass() throws IOException {
        service = new ArticleService(Paths.get("target/test-classes/articles.xlsx"), null);
        STOCK_4950344996964.put(4950344996964L, 1L);
        STOCK_08702.put(8702L, 5L);
    }

    private void initArticleService() {
        service.setInSelectedSheet(MAIN_SHEET);
        service.setIn(IN_COLUMNS);
        service.setOut(NEW_STOCK);
    }

    @Test
    public void sheetNamesTest() {
        List<String> sheetNames = service.getInSheetNames();
        Assert.assertEquals(1, sheetNames.size());
        Assert.assertEquals(MAIN_SHEET, sheetNames.get(0));
        Assert.assertFalse(sheetNames.contains("toto"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void selectedSheetFailTest() {
        service.setInSelectedSheet("toto");
    }

    @Test
    public void selectedSheetTest() {
        service.setInSelectedSheet(MAIN_SHEET);
        Assert.assertEquals(16, service.getInColumnNames().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void inColumnKO() {
        service.setInSelectedSheet(MAIN_SHEET);
        service.setIn(Arrays.asList("toto", "no more"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void inNullColumnKO() {
        service.setInSelectedSheet(MAIN_SHEET);
        service.setIn(Arrays.asList(new String[] {null}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void inNoColumnsKO() {
        service.setInSelectedSheet(MAIN_SHEET);
        service.setIn(Collections.emptyList());
    }

    @Test
    public void inOneColumnOK() {
        service.setInSelectedSheet(MAIN_SHEET);
        final List<String> oneArg = new ArrayList<>();
        oneArg.add("s_id");
        service.setIn(oneArg);
        Assert.assertTrue(service.getIn().contains("s_id"));
        Assert.assertEquals(1, service.getIn().size());
    }

    @Test
    public void inAllGoodColumns() {
        service.setInSelectedSheet(MAIN_SHEET);
        service.setIn(ALL_IN_COLUMNS);
        Assert.assertEquals(Arrays.asList("s_modele", "s_cle1", "s_cle2", "s_cle3", "s_id", "s_modelen",
                "s_id_rayon", "s_id_famil", "s_id_ssfam", "s_qdispo", "s_qv_1", "s_qv_2", "s_qv_3",
                "s_qa_1", "s_qa_2", "newstock"), service.getIn());
        Assert.assertEquals(16, service.getIn().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void inMultipleColumnsWithBadOneKO() {
        service.setInSelectedSheet(MAIN_SHEET);
        service.setIn(Arrays.asList("s_modele", "s_cle1", "s_cle2", "toto", "s_id", "newstock"));
        Assert.assertEquals(16, service.getIn().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void outNotFound() {
        service.setInSelectedSheet(MAIN_SHEET);
        service.setOut("toto");
    }

    @Test
    public void outOk() {
        service.setInSelectedSheet(MAIN_SHEET);
        service.setOut("newstock");
        Assert.assertEquals("newstock", service.getOut());
    }

  @Test(expected = NullPointerException.class)
  public void updateStockNullStockType() {
    initArticleService();
    service.updateStock(null, STOCK_08702);
  }

  @Test(expected = NullPointerException.class)
  public void updateStockNullStock() {
    initArticleService();
    service.updateStock(UpdateType.UPDATE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateStockEmptyStock() {
    initArticleService();
    service.updateStock(UpdateType.UPDATE, Collections.emptyMap());
  }

  @Test
  public void stockUpdateArticleNotFound() {
    initArticleService();
    service.updateStock(UpdateType.UPDATE, STOCK_4950344996964);
    Assert.fail("NOT IMPLEMENTED YET");
  }

  @Test
  public void stockUpdateArticle() throws IOException {
    initArticleService();
    service.updateStock(UpdateType.UPDATE, STOCK_08702);
    final XSSFRow r60 = service.getSelectedSheet().getRow(59);
    final XSSFRow r71 = service.getSelectedSheet().getRow(70);
    final XSSFCell cell60 = r60.getCell(15);
    final XSSFCell cell71 = r71.getCell(15);
    Assert.assertNotNull(cell60);
    Assert.assertNotNull(cell71);
    Assert.assertEquals(5, (int) cell60.getNumericCellValue());
    Assert.assertEquals(5, (int) cell71.getNumericCellValue());
  }
}
