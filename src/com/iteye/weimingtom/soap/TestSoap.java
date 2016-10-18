package com.iteye.weimingtom.soap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSoap {
	/**
	 * @see https://github.com/ZhongLeiDev/CodeScan.git
	 * @see https://github.com/cjhxhe/tool-ws.git
	 */
	public static void main(String[] args) {
		test1();
	}
	
	public static void test1() {
		String server = "http://www.webxml.com.cn/WebServices/TranslatorWebService.asmx";
		String soapAction1 = "getEnCnTwoWayTranslator";
		String namespace = "http://WebXml.com.cn/";
		System.out.println(">>>" + soapAction1);
		Map<String, String> mapResult1 = new HashMap<String, String>();
		List<String> listResult1 = new ArrayList<String>();
		Map<String, String> mapParam1 = new HashMap<String, String>();
		mapParam1.put("Word", "hello");
		try {
			SoapClient.connect(server, soapAction1, mapParam1, mapResult1, listResult1, namespace);
			for (String result : listResult1) {
				System.out.println("<<< result = " + result);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
