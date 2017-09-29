package downloadmanager;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;


//this class renders a Jprogressbar in a table cell
public class ProgressRenderer extends JProgressBar implements TableCellRenderer {
    //constructor for progressrender
    public ProgressRenderer(int min, int max){
        super(min,max);
    }
    
    /*returns this Jprogressbar as the renderer for the given table cell*/
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column){
        //set Jprogresbar to complete value
        setValue ((int)((Float)value).floatValue());
        return this;
    }
}
