# stockupdater
Update the stock from a flat file with numerics in an Excel sheet

Example to launch the tests:

$ java -Dexcel.file=src/test/resources/articles.xls \
       -Dstock.file=src/test/resources/stock.txt \
       -Dupdate.type=UPDATE \
       -jar stockupdater-xxx.jar
