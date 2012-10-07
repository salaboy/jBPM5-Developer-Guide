/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.tablemodel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author esteban
 */
public class FormRegistryTableModel extends AbstractStringBasedTableModel {

    public static enum ColumnHeaders {

        TASK_NAME("Task Name"),
        FORM("Form Class");

        private final String text;

        ColumnHeaders(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public FormRegistryTableModel() {
        for (ColumnHeaders columnHeaders : ColumnHeaders.values()) {
            this.columnNames.add(columnHeaders.toString());
        }
    }

    @Override
    public void addRow() {
        this.addRow("","");
    }
    
    public void addRow(String task, String form) {
        List<String> row = new ArrayList<String>();
        
        for (ColumnHeaders columnHeader : ColumnHeaders.values()) {
            switch(columnHeader){
                case FORM:
                    row.add(form);
                    break;
                case TASK_NAME:
                    row.add(task);
                    break;
                default:
                    throw new UnsupportedOperationException(columnHeader + "not supported");
            }
        }

        this.values.add(row);
        
        this.fireTableDataChanged();
    }


}
