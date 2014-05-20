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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VideoDownloader {
	static Log logger = LogFactory.getLog(VideoDownloader.class);

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
			long fileSize = getFileSize(url);
			if(file.exists()){				
				range = file.length();			
			
				if(fileSize == range){
					if(file.delete()){
						logger.info("檔案已下載完成，刪除既有檔案，重新下載!!");
						range = 0;
					}
				}else{
					logger.info("檔案未下載完成，進行續傳!!");
				}
			}
	    	HttpURLConnection httpConnection = (HttpURLConnection)fileUrl.openConnection(); 
	    	httpConnection.setRequestProperty("User-Agent","NetFox");
	    	httpConnection.setRequestProperty("RANGE","bytes=" + range + "-"); 
	    	httpConnection.connect();
	    	InputStream input = httpConnection.getInputStream(); 
	    	
	    	
	    	RandomAccessFile savedFile = new RandomAccessFile(filename,"rw"); 
	    	long nPos = range; 
	    	savedFile.seek(nPos); 
	    	byte[] b = new byte[1024]; 
	    	int nRead; 
	    	logger.info("檔案下載中!!");
	    	while((nRead=input.read(b,0,1024)) > 0) 
	    	{ 
	    		savedFile.write(b,0,nRead); 
	    		
	    	}
	    	logger.info("下載完成!!");
		} catch (MalformedURLException e) {
			logger.error("URL異常!!", e);
		} catch (IOException e) {
			logger.error("IO異常!!", e);
		} 
    }
    
    /**
     * get download file size
     * 
     * @param urlStr
     * @return file size
     */
    private long getFileSize(String urlStr){
    	long fileSize = -1; 
    	URL url;
		try {
			url = new URL(urlStr);
			HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection ();
			httpConnection.setRequestProperty("User-Agent","NetFox"); 
			int responseCode=httpConnection.getResponseCode();
			
			if(responseCode == 200){
				String header;
				//尋找檔案長度
				for(int i=1;;i++) 
				{ 
					header=httpConnection.getHeaderFieldKey(i); 
					if(header!=null) 
					{ 
						if(header.equals("Content-Length")) 
						{ 
							fileSize = Long.parseLong(httpConnection.getHeaderField(header));
							break;
						}
					}
				}				
			}
		} catch (MalformedURLException e) {
			logger.error("URL異常!!", e);
		} catch (IOException e) {
			logger.error("IO異常!!", e);
		}
		return fileSize;
    }

}
