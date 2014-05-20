package com.demo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.net.ssl.HttpsURLConnection;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VideoUrlParser {
	
	private final String USER_AGENT = "Mozilla/5.0";
	static Log logger = LogFactory.getLog(VideoDownloader.class);
	
    /**
     * Converts the url to the downloadable url
     * 
     * @param url
     * @return downloadable url or null
     */
    public String parse(String url) {
    	String youTubeUrl = null;
    	try {    		
    		//取得檔案資訊URL
    		String videoInfoUrl = executePost(url);    		
    		//取得檔案資訊內容
    		if(!StringUtils.isBlank(videoInfoUrl)){
	    		String videoInfoContent = executeGet(videoInfoUrl);
	    		if(!StringUtils.isBlank(videoInfoContent)){
	    			//取得youtube網址
	    			youTubeUrl = getYouTubeUrl(videoInfoContent);
	    		}else{
	    			logger.info("youtube網址錯誤!!");
	    		}
    		}else{
    			logger.info("檔案資訊URL錯誤!!");
    		}
		} catch (Exception e) {
			logger.error("程式錯誤!!", e);
		}
        return youTubeUrl;
    }
    
    /**
     * execute http post to get video info url
     * 
     * @param youtubeUrl
     * @return video info url or null
     */
    private String executePost(String youtubeUrl){
    	String videoInfoUrl = null;
    	String urlStr = "http://kej.tw/flvretriever/youtube.php";
		URL url;
		try {
			url = new URL(urlStr);
			//建立http url connection
			HttpURLConnection con = (HttpURLConnection) url.openConnection();	 
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	 
			String urlParameters = "videoUrl=" + URLEncoder.encode(youtubeUrl, "UTF-8");
	 
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
	 
			int responseCode = con.getResponseCode();
			//200才進行處理
			if(responseCode == 200){
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				Document doc = Jsoup.parse(response.toString());
				Elements elements = doc.select("a");

				for(Element element : elements){
					if(element.attr("href").contains("www.youtube.com")){
						videoInfoUrl = element.attr("href");
					}
				}
			}
		} catch (MalformedURLException e) {
			logger.error("URL異常!!", e);
		} catch (IOException e) {
			logger.error("IO異常!!", e);
		}
		return videoInfoUrl;
    }

    /**
     * execute http get to download file content
     * 
     * @param videoInfoUrl
     * @return file content or null
     */
    private String executeGet(String videoInfoUrl) {
    	String fileContent = null;
		URL obj;
		try {
			obj = new URL(videoInfoUrl);
			//建立http url connection
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			 
			con.setRequestMethod("GET");	 
			con.setRequestProperty("User-Agent", USER_AGENT);
	 
			int responseCode = con.getResponseCode();
			//200才進行處理
			if(responseCode == 200){
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					//過濾不需要的字串
					if(!inputLine.contains("//") && !inputLine.contains("$") && !inputLine.contains("createElement")){
						response.append(inputLine);
					}
				}
				
				in.close();
				fileContent = response.toString();
			}
		} catch (MalformedURLException e) {
			logger.error("URL異常!! URL : " + videoInfoUrl, e);
		} catch (IOException e) {
			logger.error("IO異常!!", e);
		}
 
		return fileContent;
	}

    /**
     * use video info content to parse youtube download url
     * 
     * @param videoInfoContent
     * @return youTubeUrl or null
     */
    private String getYouTubeUrl(String videoInfoContent){
    	String youTubeUrl = null;
    	//建立script engine
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        try {
        	//建立全域參數
        	engine.put("youTubeUrl", "");
        	engine.put("rdata", videoInfoContent);
        	//取得js內容
        	String jsContent = executeGet("http://kej.tw/flvretriever/script/parse.youtube.fmt_url_map.js?v=20131107");
        	//過濾不需要的字串
        	int start = jsContent.indexOf("/*");
        	int end = jsContent.indexOf("*/");
        	String replaceStr = jsContent.substring(start, end+2);
        	jsContent = jsContent.replace(replaceStr, "");
        	//jsContent = jsContent.replace("parseTitle(rdataArray);", "parseTitle(rdataArray);youTubeUrl = unescape(url_classic[0].fmt_url) + '&signature=' + url_classic[0].fmt_sig + '&title=' + title;");
        	jsContent = jsContent.replace("if(dllinks.length > 0){", "if(youTubeUrl == ''){youTubeUrl = unescape(url_classic[i].fmt_url) + '&signature=' + url_classic[i].fmt_sig + '&title=' + title;}if(dllinks.length > 0){");
        	//將js載入script engine
        	engine.eval(jsContent);
        	        	
			//呼叫js函式
        	Invocable inv = (Invocable) engine;			
			inv.invokeFunction("getYouTubeUrl");
			
			//取得解析完成的youtube url
	        youTubeUrl =  (String)engine.get("youTubeUrl");
	        
		} catch (ScriptException e) {
			logger.error("java script 解析錯誤!!", e);
		} catch (Exception e) {
			logger.error("不明錯誤!!", e);
		}
        
        return youTubeUrl;
    }
}
