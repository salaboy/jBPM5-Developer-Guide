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
public class DetailedTaskSummariesModel extends DefaultTableModel implements TaskSummariesModel{

    private List<TaskSummary> taskSummaries;
    private String[] columnNames = {"id","name", "subject", "description", "status", 
                        "priority", "skipable", "actualOwner", "createdBy", "createdOn", "activationTime",
                        "expirationTime", "processInstanceId", "processId", "processSessionId"};
    

    public DetailedTaskSummariesModel(List<TaskSummary> taskSummaries) {
        this.taskSummaries = taskSummaries;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        if(taskSummaries == null){
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
        switch(col){
            case 0: 
                return task.getId();
            case 1: 
                return task.getName();
            case 2: 
                return task.getSubject();
            case 3:
                return task.getDescription();
            case 4: 
                return task.getStatus();
            case 5:
                return task.getPriority();
            case 6:
                return task.isSkipable();
            case 7:
                return task.getActualOwner();
            case 8:
                return task.getCreatedBy();
            case 9:
                return task.getCreatedOn();
            case 10:
                return task.getActivationTime();
            case 11:
                return task.getExpirationTime();
            case 12:
                return task.getProcessInstanceId();
            case 13:
                return task.getProcessId();
            case 14:
                return task.getProcessSessionId();
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
