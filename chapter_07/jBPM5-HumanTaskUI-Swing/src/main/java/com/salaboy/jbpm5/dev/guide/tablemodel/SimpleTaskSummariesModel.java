/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.salaboy.jbpm5.dev.guide.tablemodel;

import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.jbpm.task.query.TaskSummary;

/**
 *
 * @author salaboy
 */
public class SimpleTaskSummariesModel extends DefaultTableModel implements TaskSummariesModel{

    private List<TaskSummary> taskSummaries;
    private String[] columnNames = {"name", "description", "status",
        "priority", "actualOwner", "createdBy", "createdOn", 
        "expirationTime"};

    public SimpleTaskSummariesModel(List<TaskSummary> taskSummaries) {
        this.taskSummaries = taskSummaries;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        if (taskSummaries == null) {
            return 0;
        }
        return taskSummaries.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        TaskSummary task = taskSummaries.get(row);
        return getTaskSummaryProperty(task, col);
    }

    @Override
    public Class getColumnClass(int c) {
        return String.class;
    }

    private Object getTaskSummaryProperty(TaskSummary task, int col) {
        switch (col) {

            case 0:
                return task.getName();
            case 1:
                return task.getDescription();
            case 2:
                return task.getStatus();
            case 3:
                return task.getPriority();
            case 4:
                return task.getActualOwner();
            case 5:
                return task.getCreatedBy();
            case 6:
                return task.getCreatedOn();
            case 7:
                return task.getExpirationTime();
            default:
                return task.getName();
        }
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
       //all cells false
       return false;
    }
    
    public long getTaskId(int row){
        return taskSummaries.get(row).getId();
    }
    
    public String getTaskName(int row) {
        return taskSummaries.get(row).getName();
    }
}
