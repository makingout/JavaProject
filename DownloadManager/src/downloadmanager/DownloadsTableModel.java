package downloadmanager;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

//this class manage the download table's data. 
class DownloadsTableModel extends AbstractTableModel implements Observer {
    
    //these are the names of table column
    private static final String[] columnNames = {"URL","size","progress","status"};
    
    //these are the classes for each column value.
    private static final Class[] columnClasses = {String.class, String.class,
        JProgressBar.class, String.class};
    
    //table's list of download
    private ArrayList<Download> downloadList = new ArrayList<Download>();
    
    //add a new download to the table
    public void addDownload(Download download){
        //register to be notified whenn the download changes
        download.addObserver(this);
        downloadList.add(download);
        
        //fire table row insertion notification to table.
        fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
    }
    
    //get a download for the specified row.
    public Download getDownload(int row){
        return downloadList.get(row);
    }
    
    //remove a download from the list.
    public void clearDownload(int row){
        downloadList.remove(row);
        
        //fire table row deletion notification to table.
        fireTableRowsDeleted(row,row);
    }
    
    //get table column's count
    public int getColumnCount(){
        return columnNames.length;
    }
    
    //get a column's name
    public String getColumnName(int col){
        return columnNames[col];
    }
    
    //get the column's class
    public Class getColumnClass(int col){
        return columnClasses[col];
    }
    
    //get the table's row count
    public int getRowCount(){
        return downloadList.size();
    }

    //get value for a specific row and column combination
    public Object getValueAt(int row,int col){
        Download download = downloadList.get(row);
        switch(col){
            case 0: //URL
                return download.getUrl();
            case 1: //size
                int size = download.getSize();
                return (size==-1) ? "":Integer.toString(size);
            case 2: //progress
                return new Float(download.getProgress());
            case 3: //status
                return Download.STATUSES[download.getStatus()];
        }
        return "";
    }

    /*update is called when a download notifies its observers of any change*/
    public void update(Observable o, Object arg){
        int index = downloadList.indexOf(o);
    }
    
}

