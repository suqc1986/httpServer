package com.light.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Request {

	// �������س�+���У�
	private static final String RN = "\r\n";
	private static final String GET = "get";
	private static final String POST = "post";
	private static final String CHARSET = "GBK";
	// ����ʽ
	private String method = "";
	// ���� url
	private String url = "";
	// �������
	private Map<String, List<String>> parameterMap;

	private InputStream in;

	private String requestInfo = "";
	
	public Request() {
		parameterMap = new HashMap<>();
	}

	public Request(InputStream in) {
		this();
		this.in = in;
		try {
			byte[] data = new byte[10240];
			int len = in.read(data);
			requestInfo = new String(data, 0, len);
		} catch (IOException e) {
			return;
		}
		// ����ͷ��Ϣ
		this.analyzeHeaderInfo();
	}

	/**
	 * ����ͷ��Ϣ
	 */
	private void analyzeHeaderInfo() {
		if (this.requestInfo == null || "".equals(this.requestInfo.trim())) {
			return;
		}
		
		// ��һ���������ݣ� GET /login?username=aaa&password=bbb HTTP/1.1
		
		// 1.��ȡ����ʽ
		String firstLine = this.requestInfo.substring(0, this.requestInfo.indexOf(RN));
		int index = firstLine.indexOf("/");
		this.method = firstLine.substring(0,index).trim();
		
		String urlStr = firstLine.substring(index,firstLine.indexOf("HTTP/1.1")).trim();
		String parameters = "";
		if (GET.equalsIgnoreCase(this.method)) {
			if (urlStr.contains("?")) {
				String[] arr = urlStr.split("\\?");
				this.url = arr[0];
				parameters = arr[1];
			} else {
				this.url = urlStr;
			}
			
		} else if (POST.equalsIgnoreCase(this.method)) {
			this.url = urlStr;
			parameters = this.requestInfo.substring(this.requestInfo.lastIndexOf(RN)).trim();
		}
		
		// 2. ��������װ�� map ��
		if ("".equals(parameters)) {
			return;
		}
		this.parseToMap(parameters);
	}

	/**
	 * ��װ������ Map ��
	 * @param parameters
	 */
	private void parseToMap(String parameters) {
		// ���������ʽ��username=aaa&password=bbb&likes=1&likes=2
		
		StringTokenizer token = new StringTokenizer(parameters, "&");
		while(token.hasMoreTokens()) {
			// keyValue ��ʽ��username=aaa �� username=
			String keyValue = token.nextToken();
			String[] kv = keyValue.split("=");
			if (kv.length == 1) {
				kv = Arrays.copyOf(kv, 2);
				kv[1] = null;
			} 
			
			String key = kv[0].trim();
			String value = kv[1] == null ? null : this.decode(kv[1].trim(), CHARSET);
			
			if (!this.parameterMap.containsKey(key)) {
				this.parameterMap.put(key, new ArrayList<String>());
			}
			
			this.parameterMap.get(key).add(value);
		}
	}
	
	/**
	 * ���ݲ�������ȡ�������ֵ
	 * @param name
	 * @return
	 */
	public String[] getParameterValues(String name) {
		List<String> values = null;
		if ((values = this.parameterMap.get(name)) == null) {
			return null;
		}
		
		return values.toArray(new String[0]);
	}
	
	/**
	 * ���ݲ�������ȡΨһ����ֵ
	 * @param name
	 * @return
	 */
	public String getParameter(String name) {
		String[] values = this.getParameterValues(name);
		if (values == null) {
			return null;
		}
		return values[0];
	}
	
	
	/**
	 * ��������
	 * @param value
	 * @return
	 */
	private String decode(String value, String charset) {
		try {
			return URLDecoder.decode(value, charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getUrl() {
		return url;
	}
	
 }
