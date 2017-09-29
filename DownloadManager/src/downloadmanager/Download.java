//this class downloads a file from the given URL.
package downloadmanager;

import java.io.*;
import java.net.*;
import java.util.*;

public class Download extends Observable implements Runnable {
      // maximum size of download buffer.
    private static final int MAX_BUFFER_SIZE = 1024;
    // these are status names
    public static String STATUSES[] = {"Downloading","Paused","Complete","Error","Cancelled"};
    //these are the status code
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    
    private URL url; //download url
    private int size;  //size of download in bytes
    private int downloaded; // number of bytes downloaded
    private int status; // current status of download
    
    // constructor for download
    public Download(URL url){
        this.url = url;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;
        //begin the download
        download();
    }
    
    //get this download url
    public String getUrl(){
        return url.toString();
    }
    
    //get the download size
    public int getSize(){
        return size;
    }
    
    //get the download prgress
    public float getProgress(){
        return ((float)downloaded/size)*100;
    }
    
    //get the download's status
    public int getStatus(){
        return status;
    }
    
    //pause the download
    public void pause(){
        status = PAUSED;
        stateChanged();
    }
    
    //resume this download
    public void resume(){
        status = DOWNLOADING;
        stateChanged();
        download();
    }
    
    //cancel this download
    public void cancel(){
        status = CANCELLED;
        stateChanged();
    }
    
    //mark this download having an error
    private void error(){
        status = ERROR;
        stateChanged();
    }
    
    //start or resume downloading
    private void download(){
        Thread thread = new Thread(this);
        thread.start();
    }
    
    //get file name portion of URL
    private String getFileName(URL url){
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/')+1);
    }
    
    //download file
    public void run(){
    RandomAccessFile file = null;
    InputStream stream = null;
    
    try{
        //open connection to url
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        //specify what portion of file to download
        connection.setRequestProperty("Range","bytes="+ downloaded + "-");
        
        //connect to server
        connection.connect();
        
        //make sure response code is in 200 range.
        if(connection.getResponseCode()/100!=2){
            error();
        }
        
        //check for valid connection length
        int contentLength = connection.getContentLength();
        if(contentLength<1){
            error();
        }
        
        //Set the size for this download if it hasen't been already set
        if(size==-1){
            size = contentLength;
            stateChanged();
        }
        
        //open file and seek to the end of it
        file = new RandomAccessFile(getFileName(url),"rw");
        file.seek(downloaded);
        
        stream = connection.getInputStream();
        while(status==DOWNLOADING){
            //sixe buffer according to how much of the file is downloaded
            byte buffer[];
            if(size-downloaded > MAX_BUFFER_SIZE){
                buffer = new byte[MAX_BUFFER_SIZE];
            } else {
                buffer = new byte[size-downloaded];
            }
            
            //read from server into buffer.
            int read = stream.read(buffer);
            if(read==-1){
                break;
            }
            
            //write buffer to file
            file.write(buffer,0,read);
            downloaded+=read;
            stateChanged();
            }
        /*change status to complete if this point was reached bcoz downloading
        has finished*/
        if(status==DOWNLOADING){
            status = COMPLETE;
            stateChanged();
        }
        } catch(Exception e) {
            error();
        } finally{
        //close file
        if(file!=null){
            try{
                file.close();
            } catch (Exception e){}
        }
        
        // close connection to server
        if(stream!=null){
            try{
                stream.close();
            } catch (Exception e) {}
        }
            
        }
    }
    //notify observes that this download's has changed.
    private void stateChanged(){
        setChanged();
        notifyObservers();
    }
    }
    
    


