package com.worldpay;

import java.io.IOException;

import org.apache.http.message.BasicHeader;

import android.os.Build;

final class WorldpayHttp {
	private String customUserAgentHeader;

	//singleton instance 
	private static WorldpayHttp instance;

	private WorldpayHttp() {

	}

	protected static WorldpayHttp getInstance() {
		if (instance == null) {
			instance = new WorldpayHttp();
		}
		return instance;
	}

	public HttpServerResponse executeRequest(String data) throws IllegalStateException, IOException {
		//		create http error codes

		BasicHeader[] createHeaders = createHeaders();
		return HttpClientUtility.httpEntityRequest(HttpClientUtility.HTTP_METHOD.POST, Constants.API_URL_TOKEN_REQUEST, data,
				createHeaders);

	}
	
	public HttpServerResponse executeReuseableRequest(String token,String data) throws IllegalStateException, IOException {
		//		create http error codes

		BasicHeader[] createHeaders = createHeaders();
		return HttpClientUtility.httpEntityRequest(HttpClientUtility.HTTP_METHOD.PUT, Constants.API_URL_TOKEN_REQUEST + "/" + token, data,
				createHeaders);

	}

	private String getCustomUserAgent() {
		if (customUserAgentHeader == null) {
			StringBuilder userAgent = new StringBuilder();

			String systemArch = System.getProperty("os.arch");
			if (systemArch == null) {
				systemArch = "Unknown arch";
			}

			userAgent.append("os.name=android;os.version=")
					.append(Build.VERSION.SDK_INT).append(";os.arch=")
					.append(systemArch).append(";lang.version=")
					.append(Build.VERSION.RELEASE).append(";lib.version=")
					.append(Worldpay.VERSION).append(";api.version=")
					.append(Constants.API_VERSION).append(";lang=")
					.append("java").append(";owner=")
					.append("world pay").append(';');


			customUserAgentHeader = userAgent.toString();
		}
		return customUserAgentHeader;
	}

	private BasicHeader[] createHeaders() {
		return new BasicHeader[] {// 
		new BasicHeader("Content-type", "application/json"), new BasicHeader("X-wp-client-user-agent", getCustomUserAgent()), };
	}

}
