package downloadmanager;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableModel;

//the download manager
public class DownloadManager extends JFrame implements Observer {
    
    //add download textfield
    private JTextField addTextField;
    
    //download table's data model
    private DownloadsTableModel tableModel;
    
    //table listing downloads
    private JTable table;
    
    //these are the buttons for managing selected download
    private JButton pauseButton, resumeButton;
    private JButton cancelButton, clearButton;
    
    //currently selected download
    private Download selectedDownload;
    
    //flag fo whether or not table selection is being cleared
    private boolean clearing;
    
    //constructor for donwload manager
    public DownloadManager(){
        //set application title
        setTitle("Download Manager");
        
        //set window size
        setSize(640,480);
        
        //handle windows closing event
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                actionExit();
            }
        });
        
        //setup file menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem fileExitMenuItem = new JMenuItem("EXIT",KeyEvent.VK_X);
        fileExitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                actionExit();
            }
        });
        
        fileMenu.add(fileExitMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        
        //set up add panel
        JPanel addPanel = new JPanel();
        addTextField = new JTextField(30);
        addPanel.add(addTextField);
        JButton addButton = new JButton("Add Download");
        addButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                actionAdd();
            }
        });
        addPanel.add(addButton);
        
        //setup downloads table
        tableModel = new DownloadsTableModel();
        table = new JTable((TableModel) tableModel);
        table.getSelectionModel().addListSelectionListener(new
            ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e){
                    tableSelectionChanged();
                }
            });
        
        //allow only one row at a time to be selected.
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        //setup progress bar as renderer fro progress column.
        ProgressRenderer renderer = new ProgressRenderer(0,100);
        renderer.setStringPainted(true); //show progress text
        table.setDefaultRenderer(JProgressBar.class, renderer);
        
        //set table's row height large enough to fit JProgressBar.
        table.setRowHeight((int)renderer.getPreferredSize().getHeight());
        
        //setup downloads panel
        JPanel downloadsPanel = new JPanel();
        downloadsPanel.setBorder(
        BorderFactory.createTitledBorder("Downloads"));
        downloadsPanel.setLayout(new BorderLayout());
        downloadsPanel.add(new JScrollPane(table),BorderLayout.CENTER);
        
        //setup buttons panel
        JPanel buttonsPanel = new JPanel();
        pauseButton = new JButton("PAUSE");
        pauseButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                actionPause();
            }
        });
        pauseButton.setEnabled(false);
        buttonsPanel.add(pauseButton);
        resumeButton = new JButton("Resume");
        resumeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                actionResume();
            }
        });
        resumeButton.setEnabled(false);
        buttonsPanel.add(resumeButton);
        
        cancelButton = new JButton("cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                actionCancel();
            }
        });
        cancelButton.setEnabled(false);
        buttonsPanel.add(cancelButton);
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                actionClear();
            }
        });
        clearButton.setEnabled(false);
        buttonsPanel.add(clearButton);
        
        //add panels to display
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(addPanel, BorderLayout.NORTH);
        getContentPane().add(downloadsPanel,BorderLayout.CENTER);
        getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
    }
    
    //Exit this program
    private void actionExit(){
        System.exit(0);
    }
    
    //Add a new download
    private void actionAdd(){
        URL verifiedUrl = verifyUrl(addTextField.getText());
        if(verifiedUrl!=null){
            tableModel.addDownload(new Download(verifiedUrl));
            addTextField.setText("");//reset add text field
        } else{
            JOptionPane.showMessageDialog(this,"Invalid Download URL","ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //verify download URL
    private URL verifyUrl(String url){
        //only allow HTTP URL's
        if(!url.toLowerCase().startsWith("http://")){
            return null;
        }
        
        //verify format of url
        URL verifiedUrl = null;
        try{
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            return null;
        }
        
        //make sure url specifies a file
        if(verifiedUrl.getFile().length()<2){
            return null;
        }
        return verifiedUrl;
    }
    
    //called when the table row selection changes
    private void tableSelectionChanged(){
        /*unregister from receiving notifications from the last
        selected download*/
        if(selectedDownload != null){
            selectedDownload.deleteObserver(DownloadManager.this);
        }
        
         /*if not in the middle of clearing a download set the
            selected download and register to receive notificaiton 
            form it*/
        if(!clearing && table.getSelectedRow()>-1){
            selectedDownload = tableModel.getDownload(table.getSelectedRow());
            selectedDownload.addObserver(DownloadManager.this);
            updateButtons();
        }
        
    }
    
    //pause the selected download
    private void actionPause(){
        selectedDownload.pause();
        updateButtons();
    }
    
    //resume the selected download.
    private void actionResume(){
        selectedDownload.resume();
        updateButtons();
    }
    
    //cancel the selected download
    private void actionCancel(){
        selectedDownload.cancel();
        updateButtons();
    }
    
    //clear the selected download.
    private void actionClear(){
        clearing = true;
        tableModel.clearDownload(table.getSelectedRow());
        clearing = false;
        selectedDownload = null;
        updateButtons();
    }
    
    /*update each button's state based off of the currently selected
    download's status*/
    private void updateButtons(){
        if(selectedDownload!=null){
            int status = selectedDownload.getStatus();
            switch(status){
                case Download.DOWNLOADING:
                    pauseButton.setEnabled(true);
                    resumeButton.setEnabled(false);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                case Download.PAUSED:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                case Download.ERROR:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    clearButton.setEnabled(true);
                    break;
                default: //compelete or cancelled
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                    clearButton.setEnabled(true);
                    break;
            }
        } else {
            //no download is selected in table
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(false);
            cancelButton.setEnabled(false);
            clearButton.setEnabled(false);
        }
    }
    
    /*update is called when the download notifies its observer any
    changes*/
    public void update(Observable o, Object arg){
        //update buttons if the selected download has changed
        if(selectedDownload != null && selectedDownload.equals(0))
            updateButtons();
    }
    
    //run the download manager
    public static void main(String[] args) {
       SwingUtilities.invokeLater(new Runnable(){
           public void run() {
               DownloadManager manager = new DownloadManager();
               manager.setVisible(true);
           }
       });
    }
    
}
