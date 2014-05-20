package com.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.RandomAccess;

public class VideoDownloader {

    /**
     * Saves the content of the {@code url} to a file with the name
     * {@code filename}
     * 
     * @param filename
     * @param url
     */
    public void saveVideo(String filename, String url) {
    	URL fileUrl;
		try {
			fileUrl = new URL(url);
	    	
			long range = 0;
			File file = new File(filename);
			
			if(file.exists()){
				range = file.length();
			}
			
			System.out.println("range : " + range);
			//System.out.println("getFileSize : " + getFileSize(url));
	    	HttpURLConnection httpConnection = (HttpURLConnection)fileUrl.openConnection(); 
	    	httpConnection.setRequestProperty("User-Agent","NetFox");
	    	httpConnection.setRequestProperty("RANGE","bytes=" + range + "-"); 
	    	httpConnection.connect();
	    	InputStream input = httpConnection.getInputStream(); 
	    	
	    	
	    	RandomAccessFile oSavedFile = new RandomAccessFile(filename,"rw"); 
	    	long nPos = range; 
	    	oSavedFile.seek(nPos); 
	    	byte[] b = new byte[1024]; 
	    	int nRead; 
	    	while((nRead=input.read(b,0,1024)) > 0) 
	    	{ 
	    		oSavedFile.write(b,0,nRead); 
	    	}
	    	System.out.println("下載完成!!");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    private long getFileSize(String urlStr){
    	long fileLength = -1; 
    	 URL url;
		try {
			url = new URL(urlStr);
			HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection ();
			httpConnection.setRequestProperty("User-Agent","NetFox"); 
			int responseCode=httpConnection.getResponseCode();
			
			if(responseCode == 200){
				String header;
				for(int i=1;;i++) 
				{ 
				 //DataInputStream in = new DataInputStream(httpConnection.getInputStream ()); 
				 //Utility.log(in.readLine()); 
				 header=httpConnection.getHeaderFieldKey(i); 
					 if(header!=null) 
					 { 
						 if(header.equals("Content-Length")) 
						 { 
							 fileLength = Long.parseLong(httpConnection.getHeaderField(header));
						 }
					 }
				}				
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	 return fileLength;
    }

}
