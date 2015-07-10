# stockupdater
Update the stock from a flat file with numerics in an Excel sheet

## Install

Install Java [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) minimum.

Use [maven](http://maven.apache.org/download.cgi) to compile, package or launch the project.

## Launch

Example to launch the tests:

`$ mvn exec:java -Dstock.file=src/test/resources/stock.txt -Dexcel.file.in=src/test/resources/articles.xls -Dupdate
.type=UPDATE -Dexcel.sheetname="my export articles" -Dexcel.in=s_id -Dexcel.out=new_stock`

## Properties:

* excel.file.in
* excel.file.out
* excel.sheetname
* excel.stock
* excel.out
* excel.in
* stock.file
* update.type

