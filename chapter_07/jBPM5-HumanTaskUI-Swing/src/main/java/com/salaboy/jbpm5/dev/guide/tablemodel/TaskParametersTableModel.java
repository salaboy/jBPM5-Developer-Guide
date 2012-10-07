/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.tablemodel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author esteban
 */
public class TaskParametersTableModel extends AbstractStringBasedTableModel {

    public static enum ColumnHeaders {

        PARAMETER("Parameter"),
        VALUE("Value");

        private final String text;

        ColumnHeaders(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static class ColumnModel extends DefaultTableColumnModel {

        @Override
        public void addColumn(TableColumn aColumn) {
            if (ColumnHeaders.PARAMETER.toString().equals(aColumn.getHeaderValue())) {
                aColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
            } else {
                //TODO: make the editor dynamic according to the type of parameter
                aColumn.setCellEditor(new DefaultCellEditor(new JTextField()));
            }
            super.addColumn(aColumn);
        }
    }

    public TaskParametersTableModel() {

        for (ColumnHeaders columnHeaders : ColumnHeaders.values()) {
            this.columnNames.add(columnHeaders.toString());
        }
    }

    @Override
    public void addRow() {
        this.addRow("","");
    }
    
    public void addRow(String parameter, String value) {
        List<String> row = new ArrayList<String>();
        
        for (ColumnHeaders columnHeader : ColumnHeaders.values()) {
            switch(columnHeader){
                case PARAMETER:
                    row.add(parameter);
                    break;
                case VALUE:
                    row.add(value);
                    break;
                default:
                    throw new UnsupportedOperationException(columnHeader + "not supported");
            }
        }

        this.values.add(row);
        
        this.fireTableDataChanged();
    }


}
