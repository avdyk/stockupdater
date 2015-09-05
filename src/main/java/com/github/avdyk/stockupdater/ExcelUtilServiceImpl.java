package com.github.avdyk.stockupdater;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Class to help with excel api.
 *
 * @author Arnaud Vandyck
 * @version 10/07/15.
 */
@Service
public class ExcelUtilServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelUtilServiceImpl.class);

    public Long getLongValueFromCell(final Cell cell) {
        final Long id;
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            id = (long) cell.getNumericCellValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            final String cellValue = cell.getStringCellValue();
            if (StringUtils.isNumeric(cellValue)) {
                id = Long.valueOf(cellValue);
            } else {
                LOG.debug("The cell at row {}, col {} has no numeric value ({})", cell.getRowIndex(),
                        cell.getColumnIndex(), cellValue);
                id = null;
            }
        } else {
            LOG.warn("The cell at row {}, col {} is neither numeric or string", cell.getRowIndex(),
                    cell.getColumnIndex());
            id = null;
        }
        return id;
    }
}
