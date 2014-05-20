package com.demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Main {
    
    static Log logger = LogFactory.getLog(Main.class);

    public static void main(String[] args) {

        
    	if (args == null || args.length != 2) {
            logger.warn("下載程式需要兩個參數: '檔案名稱、youtube網址!!");
            return;
        }

        String filename = args[0];
        logger.info("檔案名稱 : " + filename);
        String youtubeUrl = args[1];
        logger.info("youtube網址 : " + youtubeUrl);
        VideoUrlParser parser = new VideoUrlParser();
        String url = parser.parse(youtubeUrl);

        if (url != null) {
            VideoDownloader downloader = new VideoDownloader();
            downloader.saveVideo(filename, url);
        }
    }
}
