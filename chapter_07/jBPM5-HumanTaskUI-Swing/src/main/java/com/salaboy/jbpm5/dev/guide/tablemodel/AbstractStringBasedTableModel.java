/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.tablemodel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author esteban
 */
public abstract class AbstractStringBasedTableModel extends AbstractTableModel {
    List<String> columnNames = new ArrayList<String>();
    List<List<String>> values = new ArrayList<List<String>>();

    public AbstractStringBasedTableModel() {
    }

    public abstract void addRow();
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex);
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        values.get(rowIndex).set(columnIndex, String.valueOf(aValue));
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return values.get(rowIndex).get(columnIndex);
    }
    
    public void deleteRowAt(int rowIndex){
        this.values.remove(rowIndex);
        this.fireTableDataChanged();
    }
    
    public void clear(){
        this.values.clear();
        this.fireTableDataChanged();
    }
}
