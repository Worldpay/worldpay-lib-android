package com.worldpay;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

final  class HttpClientUtility {

	/**
	 * Available methods
	 */
	public enum HTTP_METHOD {
		POST, PUT
	}

	/**
	 * Default timeout time for the request
	 */
	public static final int HTTP_TIMEOUT = 30 * 1000;

	private synchronized static HttpClient initializeHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);

		SchemeRegistry registry = new SchemeRegistry();

		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);

		return new DefaultHttpClient(manager, params);
	}

	public static HttpServerResponse httpEntityRequest(HTTP_METHOD method, String url, String data, BasicHeader[] headers)
			throws IllegalStateException, IOException {

		HttpClient client = initializeHttpClient();
		HttpEntityEnclosingRequestBase request = null;

		if (method == HTTP_METHOD.POST) {
			request = new HttpPost(url);
		} else {
			//http put in our case 
			request = new HttpPut(url);
		}

		StringEntity stringEntity = new StringEntity(data);

		if (headers != null) {
			request.setHeaders(headers);
		}

		request.setEntity(stringEntity);

		HttpResponse response = client.execute(request);
		return new HttpServerResponse(response);
	}

}
