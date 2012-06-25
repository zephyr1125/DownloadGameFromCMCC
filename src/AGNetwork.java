import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 联网模块
 * @author Zephyr Zhang
 *
 */
public class AGNetwork{

	/**网页功能性服务器地址*/
	public static final String WEB_SERVER_URL = "http://download.cmgame.com:8513/entry?C=0300000001&ContentID=653510046773&F=853437_853438&T=1&D=0&Y=2&H=12064000&M=0&P=1&G=0&U=0&E=6118&CFM=1&S=d642db2e96f9574a8c7371daf9a35c02";
	
	private int state;
		private static final int STATE_STANDBY = 0;
		private static final int STATE_INIT = 1;
		private static final int STATE_CONNECTING = 2;
		private static final int STATE_RESPONSING = 3;
		
	/**当收到别的类对我的联网请求后，所反馈的状态码：上次请求还在处理中，等一会再请求*/
	public static final int OP_CODE_THREAD_BUSY = 0;
	/**当收到别的类对我的联网请求后，所反馈的状态码：请求已发出，反馈将异步送达代理的callBack方法中*/
	public static final int OP_CODE_REQUEST_SENT = 1;
	
	/**请求用的网络连接，在进行web请求时，返回值就是结果。在进行game请求时，需要等refresh的结果*/
	private static AGNetwork instance;
	
	private class NetworkThread implements Runnable{
		
		private boolean isReceivedData;
		
		private String serverURL;
		
		public NetworkThread(String serverUrl){
			this.serverURL = serverUrl;
		}
		
		public void start(){
			isReceivedData = false;
			(new Thread(this)).start();
		}
		
		private String getRequest() {
			HttpURLConnection conn = null;
			URL url = null;
//			String result = "";
			try {
				url = new java.net.URL(serverURL);
				System.out.println("开始连接|"+url);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(6000);
				conn.connect();

				InputStream urlStream = conn.getInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(urlStream));
				String s = "";
				int size = 0;
				while ((s = reader.readLine()) != null
						&& size<102400
						) {
//					result += s;
					size+=s.length();
					System.out.println("游戏下载中|"+size+"|"+s);
				}
				System.out.println("服务器反馈|"+size);
				reader.close();
				urlStream.close();
				conn.disconnect();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "OK";
		}
		
		@Override
		public void run() {
			while(!isReceivedData){
				state=STATE_CONNECTING;
				String strReturn = getRequest();
				if(strReturn==null || strReturn=="" || strReturn.equals("error")){
					isReceivedData = false;
					System.out.println("遇到IOException,正在重发请求！");
				}else{
					isReceivedData = true;
				}
		        
		        if(isReceivedData){
			        state = STATE_RESPONSING;
			        strReturn = UnicodeToString(strReturn);
			        state = STATE_STANDBY;
		        }
			}
		}
	}
	
	public AGNetwork(){
		init(null);
	}
	
	public static AGNetwork instance() {
		if(instance == null)instance = new AGNetwork();
		instance.init(null);
		return instance;
	}
	
	public void clear() {
		// TODO Auto-generated method stub

	}

	public void init(Map<Object, Object> arg0) {
		state=STATE_STANDBY;
	}
	
	public int downloadGame(){
		if(networkPrepare()){
			//成功发送请求
	        (new NetworkThread(WEB_SERVER_URL)).start();
	        return OP_CODE_REQUEST_SENT;
		}else{
			//上次请求尚未获得结果
			System.out.println("上次请求尚未完成！");
			return OP_CODE_THREAD_BUSY;
		}
	}
	
	private boolean networkPrepare(){
		if(state == STATE_STANDBY){
			return true;
		}else{
			return false;
		}
	}
	
	private String UnicodeToString(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");    
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");    
        }
        return str;
    }
	
}
