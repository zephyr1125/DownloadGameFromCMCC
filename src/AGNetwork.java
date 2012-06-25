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
 * ����ģ��
 * @author Zephyr Zhang
 *
 */
public class AGNetwork{

	/**��ҳ�����Է�������ַ*/
	public static final String WEB_SERVER_URL = "http://download.cmgame.com:8513/entry?C=0300000001&ContentID=653510046773&F=853437_853438&T=1&D=0&Y=2&H=12064000&M=0&P=1&G=0&U=0&E=6118&CFM=1&S=d642db2e96f9574a8c7371daf9a35c02";
	
	private int state;
		private static final int STATE_STANDBY = 0;
		private static final int STATE_INIT = 1;
		private static final int STATE_CONNECTING = 2;
		private static final int STATE_RESPONSING = 3;
		
	/**���յ��������ҵ������������������״̬�룺�ϴ������ڴ����У���һ��������*/
	public static final int OP_CODE_THREAD_BUSY = 0;
	/**���յ��������ҵ������������������״̬�룺�����ѷ������������첽�ʹ�����callBack������*/
	public static final int OP_CODE_REQUEST_SENT = 1;
	
	/**�����õ��������ӣ��ڽ���web����ʱ������ֵ���ǽ�����ڽ���game����ʱ����Ҫ��refresh�Ľ��*/
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
				System.out.println("��ʼ����|"+url);
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
					System.out.println("��Ϸ������|"+size+"|"+s);
				}
				System.out.println("����������|"+size);
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
					System.out.println("����IOException,�����ط�����");
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
			//�ɹ���������
	        (new NetworkThread(WEB_SERVER_URL)).start();
	        return OP_CODE_REQUEST_SENT;
		}else{
			//�ϴ�������δ��ý��
			System.out.println("�ϴ�������δ��ɣ�");
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
