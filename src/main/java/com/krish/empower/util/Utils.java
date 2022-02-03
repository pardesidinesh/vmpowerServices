package com.krish.empower.util;

import com.krish.empower.jdocs.Document;
import com.krish.empower.jdocs.JDocument;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.web.multipart.MultipartFile;

public class Utils {
	private static SimpleDateFormat srcFormat = new SimpleDateFormat("yyyy-MM-DD hh:mm:ss");
	private static SimpleDateFormat targetFormat = new SimpleDateFormat("DD/MM/yyyy hh:mm:ss");
	public static String sanitize(String input) {
		String specialChars = "+<>{}%";
		String charcheck;
		String newData = "";
		if(input!=null) {
			for(int i=0; i<input.length(); i++) {
				charcheck = "" + input.substring(i, i+1);
				if(!(specialChars.indexOf(input.charAt(i))>=0)) {
					newData = newData + charcheck;
				}
			}
			return newData;
		}
		return input;
	}
	
	public static HttpMethod getHttpMethod(String reqType) {
		switch(reqType) {
			case "GET": return HttpMethod.GET;
			case "POST": return HttpMethod.POST;
			case "DELETE": return HttpMethod.DELETE;
			case "PUT": return HttpMethod.PUT;
			case "OPTIONS": return HttpMethod.OPTIONS;
			case "PATCH": return HttpMethod.PATCH;
			case "HEAD": return HttpMethod.HEAD;
			case "TRACE": return HttpMethod.TRACE;
			default: return HttpMethod.GET;
		}
	}
	
	public static long convertMills2Units(long millis) {
		return millis/1000;
	}
	
	public static String formatDateFromTimestamp(Long timestamp) {
		Date srcDate = new Date(timestamp);
		return targetFormat.format(srcDate);
	}
	
	public static Date addFieldValue2Date(Date dtm, int fieldValue, int field) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dtm);
		calendar.add(field, fieldValue);
		return calendar.getTime();
	}
	
	public static String formatDateFromSource(String srcDateStr) {
		try {
			Date dtm = srcFormat.parse(srcDateStr);
			srcDateStr=targetFormat.format(dtm);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return srcDateStr;
	}
	
	public static Map<String, String>  prepareFileUploadRequest(HttpClientBuilder clientBuilder, String uri, String reqType, Map<String, String> headers, 
			Map<String, String> params, MultipartFile[] files)throws Exception{
		
		Map<String, String> respMap = new HashedMap<>();
		
		try {
			if(headers!=null) {
				List<Header> reqHdrs = headers.keySet().stream().map(key->new BasicHeader(key, headers.get(key)))
						.collect(Collectors.toList());
				clientBuilder.setDefaultHeaders(reqHdrs);
			}
			RequestBuilder requestBuilder = (reqType.equalsIgnoreCase("post"))?RequestBuilder.post().setUri(uri):RequestBuilder.put().setUri(uri);
			if(params!=null) {
				NameValuePair[] nvps = params.keySet().stream().map(key->new BasicNameValuePair(key,  params.get(key))).toArray(NameValuePair[]::new);
				requestBuilder.addParameters(nvps);
			}
			
			if(files!=null) {
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				for(MultipartFile file : files) {
					builder.addBinaryBody("file", file.getBytes(), ContentType.DEFAULT_BINARY, file.getOriginalFilename());
				}
				requestBuilder.setEntity(builder.build());
			}
			HttpResponse response = clientBuilder.build().execute(requestBuilder.build());
			respMap.put("status_code", response.getStatusLine().getStatusCode()+"");
			String respStr = (response.getEntity()!=null)?EntityUtils.toString(response.getEntity()):"";
			respMap.put("response", respStr);
			
		}catch (Exception e) {
			// TODO: handle exception
			respMap.put("status_code", "500");
			respMap.put("response", e.getMessage());
			e.printStackTrace();
		}
		return respMap;
	}
	
	public static Map<String, String>  prepareServerRequest(HttpClientBuilder clientBuilder, String uri, String reqType, Map<String, String> headers, 
			Map<String, String> params, String requestBody)throws Exception{
		
		Map<String, String> respMap = new HashedMap<>();
		try {
			if(headers!=null) {
				List<Header> reqHdrs = headers.keySet().stream().map(key->new BasicHeader(key, headers.get(key)))
						.collect(Collectors.toList());
				clientBuilder.setDefaultHeaders(reqHdrs);
			}
			RequestBuilder requestBuilder = null;
			switch(reqType.toLowerCase()) {
				case "post": 
					requestBuilder = RequestBuilder.post().setUri(uri); break;
				case "put":
					requestBuilder = RequestBuilder.put().setUri(uri); break;
				case "delete":
					requestBuilder = RequestBuilder.delete().setUri(uri); break;
				default:
					requestBuilder = RequestBuilder.get().setUri(uri);
			}
			if(params!=null) {
				NameValuePair[] nvps = params.keySet().stream().map(key->new BasicNameValuePair(key,  params.get(key))).toArray(NameValuePair[]::new);
				requestBuilder.addParameters(nvps);
			}
			
			if(StringUtils.isNotBlank(requestBody)) {
				requestBuilder = requestBuilder.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
			}
			HttpResponse response = clientBuilder.build().execute(requestBuilder.build());
			respMap.put("status_code", response.getStatusLine().getStatusCode()+"");
			String respStr = (response.getEntity()!=null)?EntityUtils.toString(response.getEntity()):"";
			respMap.put("response", respStr);
			
		}catch (Exception e) {
			// TODO: handle exception
			respMap.put("status_code", "500");
			respMap.put("response", e.getMessage());
			e.printStackTrace();
		}
		return respMap;
	}
	
	public static HttpClientBuilder getClientBuilder(String proxyHost, int port, String userId, String password) throws GeneralSecurityException{
		HttpClientBuilder clientBuilder = HttpClients.custom();
		if(StringUtils.isNotBlank(proxyHost)) {
			HttpHost proxy = new HttpHost(proxyHost, port);
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
			CredentialsProvider creds = new BasicCredentialsProvider();
			creds.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(userId, password));
			AuthCache authCache =new BasicAuthCache();
			BasicScheme authScheme = new BasicScheme();
			authCache.put(proxy, authScheme);
			HttpClientContext context = HttpClientContext.create();
			context.setCredentialsProvider(creds);
			context.setAuthCache(authCache);
			clientBuilder = clientBuilder.setRoutePlanner(routePlanner).setDefaultCredentialsProvider(creds);
		}
		
		return clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).
				setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
			
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// TODO Auto-generated method stub
				return true;
			}
		}).build());
	}
	
	public static boolean compareWithMany(String first, String... others) {
		if(others == null) {
			return false;
		}
		for(int indx=0; indx< others.length; indx++) {
			if (first.equalsIgnoreCase(others[indx])){
				return true;
			}
		}
		return false;
	}
	
	public static String getResourceAsString(Class clsRef, String filePath) {
		InputStream is = new BufferedInputStream(clsRef.getResourceAsStream(filePath));
		String strContent = "";
		try {
			strContent = IOUtils.toString(is, StandardCharsets.UTF_8.name());
		}catch(IOException ex) {
			ex.printStackTrace();
		}finally {
			try {
				is.close();
			}catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		return strContent;
	}
	public static Document getResourceAsDocument(Class clsRef, String filePath) {
		InputStream is = new BufferedInputStream(clsRef.getResourceAsStream(filePath));
		String strContent = "";
		Document jdoc = null;
		try {
			strContent = IOUtils.toString(is, StandardCharsets.UTF_8.name());
			jdoc = new JDocument(strContent);
		}catch(IOException ex) {
			ex.printStackTrace();
		}finally {
			try {
				is.close();
			}catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		return jdoc;
	}
	
	public Object sampleEval(Map<String, String> values, Object input) {
		return new Long(100);
	}
	public Object sampleReturn(Map<String, String> values, Object input) {
		return "From return method";
	}
}
