package com.demo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VideoUrlParser {
	
	private final String USER_AGENT = "Mozilla/5.0";
	
    /**
     * Converts the url to the downloadable url
     * 
     * @param url
     * @return downloadable url or null
     */
    public String parse(String url) {
    	String youTubeUrl = null;
    	try {    		
    		String videoInfoUrl = executePost(url);
    		String videoInfoContent = executeGet(videoInfoUrl);
    		youTubeUrl = getYouTubeUrl(videoInfoContent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return youTubeUrl;
    }
    
    private String executePost(String youtubeUrl) throws Exception{
    	String url = "http://kej.tw/flvretriever/youtube.php";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
		String urlParameters = "videoUrl=" + URLEncoder.encode(youtubeUrl, "UTF-8");
 
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		//System.out.println(response.toString());
		
		Document doc = Jsoup.parse(response.toString());
		System.out.println(doc.toString());
		Elements elements = doc.select("a");
		String videoInfoUrl = "";
		for(Element element : elements){
			if(element.text().equals("下載此檔案")){
				videoInfoUrl = element.attr("href");
			}
		}
		
		return videoInfoUrl;
    }

    private String executeGet(String videoInfoUrl) throws Exception {
    	 
		//String url = "http://www.google.com/search?q=mkyong";
 
		URL obj = new URL(videoInfoUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + videoInfoUrl);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			if(!inputLine.contains("//") && !inputLine.contains("$") && !inputLine.contains("createElement")){
				response.append(inputLine);
			}
		}
		in.close();
 
		//print result
		//System.out.println(response.toString());
		return response.toString();
	}

    private String getYouTubeUrl(String videoInfoContent){
    	String youTubeUrl = null;
    	
    	// create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        // evaluate JavaScript code from String
        try {
        	//engine.eval(executeGet("http://jqueryjs.googlecode.com/svn/trunk/jquery/build/runtest/env.js"));
        	//engine.eval("window.location = 'http://kej.tw/flvretriever/youtube.php?videoUrl=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3Do9IVocUaOPY'");
        	
        	//engine.eval(executeGet("http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js"));
        	engine.put("youTubeUrl", "");
        	engine.put("rdata", videoInfoContent);
        	System.out.println(videoInfoContent);
//        	String script = "var document = new Object (); " +
//                    "document.writeln = function (s) { println (s); }; " +
//                    "document.write = function (s) { print (s); }";
//        	engine.eval (script);
//        	 script = "var navigator = new Object (); " +
//                    "document.writeln = function (s) { println (s); }; " +
//                    "document.write = function (s) { print (s); }";
//        	engine.eval (script);
        	String jsContent = executeGet("http://kej.tw/flvretriever/script/parse.youtube.fmt_url_map.js?v=20131107");
        	int start = jsContent.indexOf("/*");
        	int end = jsContent.indexOf("*/");
        	String replaceStr = jsContent.substring(start, end+2);
        	jsContent = jsContent.replace(replaceStr, "");
        	//jsContent = jsContent.replace("$('#videoInfo').val()", "'" + videoInfoContent + "'");
        	jsContent = jsContent.replace("parseTitle(rdataArray);", "parseTitle(rdataArray);youTubeUrl = unescape(url_classic[0].fmt_url) + '&signature=' + url_classic[0].fmt_sig + '&title=' + title;");
        	//System.out.println(jsContent.replace("parseTitle(rdataArray);", "parseTitle(rdataArray);youTubeUrl = unescape(url_classic[0].fmt_url) + '&signature=' + url_classic[0].fmt_sig + '&title=' + title;"));
        	System.out.println(jsContent);
        	engine.eval(jsContent);
        	/*
        	engine.eval("var fmt_str = new Array(); fmt_str[0]  = '(FLV, 320 x 240, Mono 22KHz MP3)'; // delete ? fmt_str[5]  = '(FLV, 400 x 240, Mono 44KHz MP3)'; fmt_str[6]  = '(FLV, 480 x 360, Mono 44KHz MP3)'; // delete ? fmt_str[34] = '(FLV, 640 x 360, Stereo 44KHz AAC)'; fmt_str[35] = '(FLV, 854 x 480, Stereo 44KHz AAC)'; fmt_str[13] = '(3GP, 176 x 144, Stereo 8KHz)';    // delete ? fmt_str[17] = '(3GP, 176 x 144, Stereo 44KHz AAC)'; fmt_str[36] = '(3GP, 320 x 240, Stereo 44KHz AAC)'; fmt_str[18] = '(MP4(H.264), 640 x 360, Stereo 44KHz AAC)'; fmt_str[22] = '(MP4(H.264), 1280 x 720, Stereo 44KHz AAC)'; fmt_str[37] = '(MP4(H.264), 1920 x 1080, Stereo 44KHz AAC)'; fmt_str[38] = '(MP4(H.264), 4096 x 3072, Stereo 44KHz AAC)'; fmt_str[83] = '(MP4(H.264), 854 x 240, Stereo 44KHz AAC)'; fmt_str[82] = '(MP4(H.264), 640 x 360, Stereo 44KHz AAC)'; fmt_str[85] = '(MP4(H.264), 1920 x 520, Stereo 44KHz AAC)'; fmt_str[84] = '(MP4(H.264), 1280 x 720, Stereo 44KHz AAC)'; fmt_str[43] = '(WebM(VP8), 640 x 360, Stereo 44KHz Vorbis)'; fmt_str[44] = '(WebM(VP8), 854 x 480, Stereo 44KHz Vorbis)'; fmt_str[45] = '(WebM(VP8), 1280 x 720, Stereo 44KHz Vorbis)'; fmt_str[100] = '(WebM(VP8), 640 x 360, Stereo 44KHz Vorbis)'; fmt_str[101] = '(WebM(VP8), 854 x 480, Stereo 44KHz Vorbis)'; fmt_str[46] = '(WebM(VP8), 1920 x 540, Stereo 44KHz Vorbis)'; fmt_str[102] = '(WebM(VP8), 1280 x 720, Stereo 44KHz Vorbis)'; fmt_str[133] = '(MP4(H.264), 426 x 240, <span style=\"color:#f00;\">no audio</span>)'; fmt_str[134] = '(MP4(H.264), 640 x 360, <span style=\"color:#f00;\">no audio</span>)'; fmt_str[135] = '(MP4(H.264), 854 x 480, <span style=\"color:#f00;\">no audio</span>)'; fmt_str[136] = '(MP4(H.264), 1280 x 720, <span style=\"color:#f00;\">no audio</span>)'; fmt_str[137] = '(MP4(H.264), 1920 x 1080, <span style=\"color:#f00;\">no audio</span>)'; fmt_str[138] = '(MP4(H.264), 4096 x 3072, <span style=\"color:#f00;\">no audio</span>)'; fmt_str[139] = '(M4A, 48 kbit/s <span style=\"color:#f00;\">audio only</span>)'; fmt_str[140] = '(M4A, 128 kbit/s <span style=\"color:#f00;\">audio only</span>)'; fmt_str[141] = '(M4A, 256 kbit/s <span style=\"color:#f00;\">audio only</span>)'; fmt_str[160] = '(MP4(H.264), 256 x 144, <span style=\"color:#f00;\">no audio</span>)'; fmt_str[264] = '(MP4(H.264), 1920 x 1080, <span style=\"color:#f00;\">no audio</span>)'; var fmt_ext = new Array(); fmt_ext[0]  = '.flv'; // delete ? fmt_ext[5]  = '.flv'; fmt_ext[6]  = '.flv'; // delete ? fmt_ext[34] = '.flv'; fmt_ext[35] = '.flv'; fmt_ext[13] = '.3gp';    // delete ? fmt_ext[17] = '.3gp'; fmt_ext[36] = '.3gp'; fmt_ext[18] = '.mp4'; fmt_ext[22] = '.mp4'; fmt_ext[37] = '.mp4'; fmt_ext[38] = '.mp4'; fmt_ext[83] = '.mp4'; fmt_ext[82] = '.mp4'; fmt_ext[85] = '.mp4'; fmt_ext[84] = '.mp4'; fmt_ext[43] = '.webm'; fmt_ext[44] = '.webm'; fmt_ext[45] = '.webm'; fmt_ext[100] = '.webm'; fmt_ext[101] = '.webm'; fmt_ext[46] = '.webm'; fmt_ext[102] = '.webm'; fmt_ext[133] = '.mp4'; fmt_ext[134] = '.mp4'; fmt_ext[135] = '.mp4'; fmt_ext[136] = '.mp4'; fmt_ext[137] = '.mp4'; fmt_ext[137] = '.mp4'; fmt_ext[139] = '.m4a'; fmt_ext[140] = '.m4a'; fmt_ext[141] = '.m4a'; fmt_ext[160] = '.mp4'; fmt_ext[264] = '.mp4';");
        	//engine.eval("function getYouTubeUrl(){  var rdata = $('#videoInfo').val();  var rdataArray = rdata.split('&');  var succ = 0;  var url_classic = parseUrlsClassic(rdataArray);  var url_adaptive = parseUrlsAdaptive(rdataArray);  var url_alter = parseUrlsAlter(rdataArray, url_classic, url_adaptive);  var title = parseTitle(rdataArray);  var dllinks = '';  var webmlinks = '';  var dllinksAdaptive = '';  var dllinksAlter = '';  for(var i in url_classic){  if(url_classic[i].fmt == 43 || url_classic[i].fmt == 44 || url_classic[i].fmt == 45 || url_classic[i].fmt == 46 || url_classic[i].fmt == 100 || url_classic[i].fmt == 101 || url_classic[i].fmt == 102){  if(webmlinks.length > 0){  webmlinks += '<br />';  }  webmlinks += '<a href=\"' + unescape(url_classic[i].fmt_url) + \"&signature=\" + url_classic[i].fmt_sig + \"&title=\" + title + '\" target=\"_blank\"><b>Watch online&nbsp;&nbsp;&nbsp;' + fmt_str[url_classic[i].fmt] + '</b></a>';  }else{  if(dllinks.length > 0){  dllinks += '<br />';  }  dllinks += '<a href=\"' + unescape(url_classic[i].fmt_url) + \"&signature=\" + url_classic[i].fmt_sig + \"&title=\" + title + '\" target=\"_blank\"><b>Download&nbsp;&nbsp;&nbsp;' + fmt_str[url_classic[i].fmt] + '</b></a>';  }  }  if(webmlinks.length > 0){  if(dllinks.length > 0){  dllinks += '<br />';  }  dllinks += webmlinks;  }  if(url_alter.length > 0){  for(var i in url_alter){  if(dllinksAlter.length > 0){  dllinksAlter += '<br />';  }  dllinksAlter += '<a href=\"' + unescape(url_alter[i].fmt_url) + \"&title=\" + escape(title) + '\" target=\"_blank\"><b>Download&nbsp;&nbsp;&nbsp;' + fmt_str[url_alter[i].fmt] + '</b></a>';  }  }  if(dllinksAlter.length > 0){  if(dllinks.length > 0){  dllinks += '<br /><br /><span style=\"color:#f00; font-weight:bold;\">sadly 1080p\'s dead again...</span><br /><del>1080p & some other formats redirect download are back online and <span style=\"color:#f00;font-weight:bold;\">testing</span>:<br />';  }  dllinks += dllinksAlter + '</del>';  }  for(var i in url_adaptive){  if(dllinksAdaptive.length > 0){  dllinksAdaptive += '<br />';  } dllinksAdaptive += '<a href=\"' + unescape(url_adaptive[i].fmt_url) + \"&title=\" + escape(title) + '\" target=\"_blank\"><b>Download&nbsp;&nbsp;&nbsp;' + fmt_str[url_adaptive[i].fmt] + '</b></a>';  }  if(dllinksAdaptive.length > 0){  if(dllinks.length > 0){  dllinks += '<br /><br />special files (separated audio and video):<br />';  }  dllinks += dllinksAdaptive;  }  if(dllinks.length > 0){  $('#result_div').remove();  var div_dl = document.createElement('div');  $(div_dl).html(dllinks).css('padding', '7px 0 0 0');  $(div_dl).attr('id', 'result_div');  $('#videoInfo').after(div_dl);  $('#downloadInfo').css('display', 'block');  succ = 1;  }  if(succ == 0){  var result;  var rdata_status;  var rdata_reason;  var rdata_temp;  for(i = 0; i < rdataArray.length; i++){  rdata_temp = rdataArray[i].split('=');  if(rdata_temp[0] == 'status'){  rdata_status = rdata_temp[1];  }  if(rdata_temp[0] == 'reason'){  rdata_reason = urldecode(rdata_temp[1]);  }  }  result = '<b>&#28961;&#27861;&#21462;&#24471;&#24433;&#29255; URL</b><br />status : <span style=\"color:#f00;\">' + rdata_status + '</span><br />' + 'reason : <span style=\"color:#f00;\">' + rdata_reason + '</span>';  $('#result_div').remove();  var div_dl = document.createElement('div');  $(div_dl).html(result).css('padding', '7 0 7px 0');  $(div_dl).attr('id', 'result_div');  $('#videoInfo').after(div_dl);  } }");
        	engine.eval("function getYouTubeUrl(){  var rdata = '" + videoInfoContent+ "';  var rdataArray = rdata.split('&');  var succ = 0;  var url_classic = parseUrlsClassic(rdataArray);  var url_adaptive = parseUrlsAdaptive(rdataArray);  var url_alter = parseUrlsAlter(rdataArray, url_classic, url_adaptive);  var title = parseTitle(rdataArray); print(unescape(url_classic[0].fmt_url) + '&signature=' + url_classic[0].fmt_sig + '&title=' + title);print(title);}");
        	engine.eval("function parseUrlsClassic(rdataArray){  for(i = 0; i < rdataArray.length; i++){  r0 = rdataArray[i].substr(0, 26);  if(r0 == 'url_encoded_fmt_stream_map'){  r1 = unescape(rdataArray[i].substr(27));  var temp1 = r1.split(',');  var fmt = new Array;  var fmt_url = new Array;  var fmt_sig = new Array;  var items = [];  for(j = 0; j < temp1.length; j++){  var temp2 = temp1[j].split('&');  var item = {};  var temp_itag = -1;  var temp_type = '';  for(jj = 0; jj < temp2.length; jj++){  if(temp2[jj].substr(0, 5) == 'itag='){  temp_itag = parseInt(temp2[jj].substr(5), 10);  item.fmt = temp_itag;  }else if(temp2[jj].substr(0, 4) == 'url='){  item.fmt_url = temp2[jj].substr(4);  }else if(temp2[jj].substr(0, 4) == 'sig='){  item.fmt_sig = temp2[jj].substr(4);  }else if(temp2[jj].substr(0, 2) == 's='){  item.fmt_sig = SigHandlerAlternative(temp2[jj].substr(2));  }else if(temp2[jj].substr(0, 5) == 'type='){  temp_type = '(' + unescape(temp2[jj].substr(5)) + ')';  }  }  if(fmt_str[temp_itag] == 'undefined'){  fmt_str[temp_itag] = temp_type;  }  items.push(item);  }  return items;  }  } }");
        	engine.eval("function parseUrlsAdaptive(rdataArray){  for(i = 0; i < rdataArray.length; i++){  r0 = rdataArray[i].substr(0, 13);  if(r0 == 'adaptive_fmts'){  r1 = unescape(rdataArray[i].substr(14));  var temp1 = r1.split(',');  var fmt = new Array;  var fmt_url = new Array;  var fmt_sig = new Array;  var items = [];  for(j = 0; j < temp1.length; j++){  var temp2 = temp1[j].split('&');  var item = {};  var temp_itag = -1;  var temp_type = '';  for(jj = 0; jj < temp2.length; jj++){  if(temp2[jj].substr(0, 5) == 'itag='){  temp_itag = parseInt(temp2[jj].substr(5), 10);  item.fmt = temp_itag;  }else if(temp2[jj].substr(0, 4) == 'url='){  item.fmt_url = temp2[jj].substr(4);  }else if(temp2[jj].substr(0, 4) == 'sig='){  item.fmt_sig = temp2[jj].substr(4);  }else if(temp2[jj].substr(0, 2) == 's='){  item.fmt_sig = SigHandlerAlternative(temp2[jj].substr(2));  }else if(temp2[jj].substr(0, 5) == 'type='){  temp_type = '(' + unescape(temp2[jj].substr(5)) + ')';  }  }  if(fmt_str[temp_itag] == 'undefined'){  fmt_str[temp_itag] = temp_type;  }  items.push(item);  }  return items;  }  } }");
        	engine.eval("function parseUrlsAlter(rdataArray, url_classic, url_adaptive){  for(i = 0; i < rdataArray.length; i++){  r0 = rdataArray[i].substr(0, 7);  if(r0 == 'dashmpd'){  r1 = unescape(rdataArray[i].substr(8)).replace('http://www.youtube.com/api/manifest/dash/', '');  var temp1 = r1.split('/');  for(var j = 0; j < temp1.length; j ++){  if(temp1[j] == 'sig'){  temp1[j] = 'signature';  }  if(temp1[j] == 's'){  temp1[j] = 'signature';  temp1[j+1] = SigHandlerAlternative(temp1[j+1]);  }  }  var qstemp = [];  for(var j = 0; j < temp1.length; j += 2){  qstemp.push(temp1[j] + '=' + temp1[j+1]);  }  var qs = qstemp.join('&');  if (qs.toLowerCase().indexOf('ratebypass') == -1) {  qs = qs + '&ratebypass=yes';  }  var base_url = '';  for(var j in url_classic){  var tempurl = unescape(url_classic[j].fmt_url).split('?');  if(tempurl[0] !== '' && tempurl[0] !== undefined && tempurl[0].length > 0){  base_url = tempurl[0];  break;  }  }  var fmt_classic = [];  for(var j in url_classic){  fmt_classic[url_classic[j].fmt] = true;  }  var fmt_adaptive = [];  for(var j in url_adaptive){  fmt_adaptive[url_adaptive[j].fmt] = true;  }  var items = [];  var item35 = {};  var item37 = {};  var item38 = {};  if(fmt_adaptive[135] && fmt_classic[35] == undefined){  item35.fmt = 35;  item35.fmt_url = base_url + '?' + qs + '&itag=35';  items.push(item35);  }  if((fmt_adaptive[137] || fmt_adaptive[264]) && fmt_classic[37] == undefined){  item37.fmt = 37;  item37.fmt_url = base_url + '?' + qs + '&itag=37';  items.push(item37);  }  if(fmt_adaptive[138] && fmt_classic[38] == undefined){  item38.fmt = 38;  item38.fmt_url = base_url + '?' + qs + '&itag=38';  items.push(item38);  }  return items;  }  }  return []; }");
        	engine.eval("function parseTitle(rdataArray){  for(i = 0; i < rdataArray.length; i++){  r0 = rdataArray[i].substr(0, 5);  if(r0 == 'title'){  return rdataArray[i].substr(6).replace(/%22/g, '');  }  } }");
        	engine.eval("function urldecode(str){ return decodeURIComponent(str.replace(/\\+/g, '%20')); }");
        	engine.eval("function SigHandlerAlternative(s){  var sArray = s.split(\"\");  var tmpA, tmpB;  tmpA = sArray[0];  tmpB = sArray[52];  sArray[0] = tmpB;  sArray[52] = tmpA;  tmpA = sArray[83];  tmpB = sArray[62];  sArray[83] = tmpB;  sArray[62] = tmpA;  sArray = sArray.slice(3);  sArray = sArray.reverse();  sArray = sArray.slice(3);  return sArray.join(\"\"); }");
			*/
        	
			Invocable inv = (Invocable) engine;

	        // invoke the global function named "hello"
	        inv.invokeFunction("getYouTubeUrl");
	        System.out.println(engine.get("youTubeUrl"));
	        youTubeUrl =  (String)engine.get("youTubeUrl");
	        //"unescape(url_classic[i].fmt_url) + '&signature=' + url_classic[i].fmt_sig + '&title=' + title"
	        
//        	Compilable compilable = (Compilable) engine;
//        	Bindings bindings = engine.createBindings(); //Local级别的Binding
//        	String script = "function add(op1,op2){return op1+op2} add(a, b)"; //定义函数并调用
//        	CompiledScript JSFunction = compilable.compile(executeGet("http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js")); //解析编译脚本函数
//        	bindings.put("a", 1);bindings.put("b", 2); //通过Bindings加入参数
//        	Object result = JSFunction.eval(bindings);
//        	System.out.println(result);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return youTubeUrl;
    }
}
