package com.iteye.weimingtom.soap;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.iteye.weimingtom.dom4j.Document;
import com.iteye.weimingtom.dom4j.DocumentException;
import com.iteye.weimingtom.dom4j.DocumentHelper;
import com.iteye.weimingtom.dom4j.Element;
import com.iteye.weimingtom.dom4j.io.SAXReader;

public class SoapClient {
	private final static boolean D = false;
	
	public static void connect(String server, 
		String soapAction, 
		Map<String, String> mapParam,
		Map<String, String> mapResult, List<String> listResult, String namespace) throws IOException {
		DataOutputStream out = null;
		InputStream instr = null;
		try {
			URL callURL = new URL(server);
			//URLConnection urlconn = callURL.openConnection();
			HttpURLConnection urlconn = (HttpURLConnection)callURL.openConnection();
			urlconn.setDoInput(true);
			urlconn.setDoOutput(true);
			
			Document requestDoc = DocumentHelper.createDocument();
			Element root = requestDoc.addElement("soapenv:Envelope");
			root.addNamespace("soapenv", "http://www.w3.org/2003/05/soap-envelope");
			root.addAttribute("xmlns:soapenv", "http://www.w3.org/2003/05/soap-envelope");
			Element body = root.addElement("soapenv:Body");
			body.addNamespace("ns1", namespace);
			Element act = body.addElement("ns1:" + soapAction);
			if (mapParam != null && 
				mapParam.size() > 0) {
				for (Map.Entry<String, String> entry : mapParam.entrySet()) {
					Element item = act.addElement("ns1:" + entry.getKey());
					//item.addNamespace("ns1", "http://WebXml.com.cn/");
					item.setText(entry.getValue());
				}					
			}
		
			String requestData = requestDoc.asXML();
			if (D) {
				System.out.println("requestData = " + requestData);
			}
			
			//requestData
			byte[] bytes = requestData.getBytes("utf-8");
			
			urlconn.setUseCaches(false);
			urlconn.setConnectTimeout(6000);
			urlconn.setRequestMethod("POST");
			//urlconn.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
			//urlconn.setRequestProperty("SOAPAction", soapAction);
			urlconn.setRequestProperty("Content-Length", "" + bytes.length);
			
			urlconn.setRequestProperty("Content-Type", "application/soap+xml; charset=UTF-8; action=\"urn:" + soapAction + "\"");
			//urlconn.setRequestProperty("Content-Type", "application/soap+xml; charset=UTF-8; action=\"" + namespace + "getEnCnTwoWayTranslator\"");
			out = new DataOutputStream(urlconn.getOutputStream());		
			//out.writeBytes(strAuthRequest);
			out.write(bytes);
			out.flush();

			StringBuffer reply = new StringBuffer();
			instr = urlconn.getInputStream();
//			BufferedReader in = new BufferedReader(new InputStreamReader(instr));
//			for (int c = in.read(); c != -1; c = in.read()) {
//				reply.append((char) c);
//			}
			reply.append(convertStreamToString(instr, "utf-8"));
			String strReply = reply.toString();
			if (D) {
				System.out.println(strReply);
			}
			
			parse(strReply, mapResult, listResult);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (instr != null) {
				try {
					instr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void parse(String content,
		Map<String, String> mapResult, List<String> listResult) {
		Reader reader = null;
		Document doc = null;
		try {
			reader = new StringReader(content);
	        SAXReader saxReader = new SAXReader();
	        doc = saxReader.read(reader);
		} catch (DocumentException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (doc == null) {
			return;
		}
		Element node = firstChild(doc, "Envelope");
		if (node == null) {
			return;
		}
		if (D) {
			System.out.println("Envelope = " + node);	
		}
		node = firstChild(node, "Body");
		if (node == null) {
			return;
		}
		if (D) {
			System.out.println("Body = " + node);
		}
		node = firstChild(node);
		if (node == null) {
			return;
		}
		if (D) {
			System.out.println("Response = " + node);
		}
		node = firstChild(node, "getEnCnTwoWayTranslatorResult");
		if (node == null) {
			return;
		}
		if (D) {
			System.out.println("getEnCnTwoWayTranslatorResult = " + node);
		}
		for (Element nodeReturn : node.elements()) {
			if (D) {
				System.out.println("nodeReturn = " + nodeReturn);
			}
			if (nodeReturn != null) {
				String key = nodeReturn.getName();
				if (D) {
					System.out.println(">>>>key = " + key);
				}
				String val = nodeReturn.getText();
				if (D) {
					System.out.println(">>>>value = " + val);
				}
				if (mapResult != null &&
					key != null && val != null) {
					mapResult.put(key, val);
					listResult.add(val);
					if (key.contains(":")) {
						String[] strs = key.split(":");
						if (strs != null && strs.length >= 2) {
							mapResult.put(strs[1], val);
						}
					}
				}
			}
		}
	}

	private static Element firstChild(Document root, String name) {
		Element element = root.getRootElement();
		if (element.getName() != null) {
			if (element.getName().equals(name)) {
				return element;
			}
			if (element.getName().contains(":")) {
				String[] strs = element.getName().split(":");
				if (strs != null && strs.length > 1) {
					if (strs[1].equals(name)) {
						return element;
					}
				}
			}
		}
		return null;
	}
	
	private static Element firstChild(Element root, String name) {
		for (Element element : root.elements()) {
			if (element.getName().equals(name)) {
				return element;
			}
			if (element.getName().contains(":")) {
				String[] strs = element.getName().split(":");
				if (strs != null && strs.length > 1) {
					if (strs[1].equals(name)) {
						return element;
					}
				}
			}
		}
		return null;
	}
	
	private static Element firstChild(Element root) {
		for (Element element : root.elements()) {
			return element;
		}
		return null;
	}
	
	public static String convertStreamToString(InputStream is, String charset) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		try {
//			byte[] b = new byte[4096];
//	        for (int n; (n = is.read(b)) != -1;) {
//	            sb.append(new String(b, 0, n, charset));
//	        }
			ByteArrayOutputStream ots = new ByteArrayOutputStream();
			byte[] data = new byte[4096];
			int count = -1;
			while ((count = is.read(data)) != -1) {
				ots.write(data, 0, count);
			}
			byte[] bytes = ots.toByteArray();
			sb.append(new String(bytes, "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}

