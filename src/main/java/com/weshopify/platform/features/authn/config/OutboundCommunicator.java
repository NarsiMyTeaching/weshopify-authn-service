package com.weshopify.platform.features.authn.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OutboundCommunicator {

	@Autowired
	private RestTemplate rt;

	@Value("${wso2.iam.client-id}")
	private String clientId;

	@Value("${wso2.iam.client-secret}")
	private String secret;

	@Value("${wso2.iam.grant_type}")
	private String grantType;

	@Value("${wso2.iam.scope}")
	private String scope;

	@Value("${wso2.iam.token.endpoint}")
	private String tokenEndpoint;

	@Value("${wso2.iam.userinfo.endpoint}")
	private String userEndpoint;

	public String authenticateId(String userName, String password) {

		String passPhrase = clientId + ":" + secret;
		String encodedHash = new String(Base64.getEncoder().encode(passPhrase.getBytes()));

		String authnBody = "grant_type=" + grantType + "&username=" + userName + "&password=" + password + "&scope="
				+ scope;

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + encodedHash);
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);

		HttpEntity requestData = new HttpEntity(authnBody, headers);
		disableSslVerification();
		ResponseEntity<String> iamResp = rt.postForEntity(tokenEndpoint, requestData, String.class);
		if (iamResp.getStatusCodeValue() == HttpStatus.OK.value()) {
			System.out.println(iamResp.getBody());
			String respBody = iamResp.getBody();
			return respBody;
		}

		return null;
	}

	public String getUserInfo(String accessToken) {

		/*
		 * String passPhrase = clientId + ":" + secret; String encodedHash = new
		 * String(Base64.getEncoder().encode(passPhrase.getBytes()));
		 * 
		 * String authnBody = "grant_type=" + grantType + "&username=" + userName +
		 * "&password=" + password + "&scope=" + scope;
		 */
		System.out.println("access token going to idp is:\t" + accessToken);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		// headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);

		HttpEntity requestData = new HttpEntity(headers);
		disableSslVerification();
		//ResponseEntity<String> iamResp = rt.getForEntity(userEndpoint, String.class, requestData);
		ResponseEntity<String> iamResp = rt.exchange(userEndpoint, HttpMethod.GET, requestData, String.class);
		if (iamResp.getStatusCodeValue() == HttpStatus.OK.value()) {
			System.out.println(iamResp.getBody());
			String respBody = iamResp.getBody();

			JSONObject jsonRespBody = new JSONObject(respBody);
			// String accessToken = jsonRespBody.getString("acces_token");

			return jsonRespBody.toString();
		}

		return null;
	}

	private void ignoreCertificates() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {

		}
	}
	
	private void disableSslVerification() {
	    try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
	        }
	        };

	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return hostname.equals("13.214.221.98");
	            }
	        };

	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (KeyManagementException e) {
	        e.printStackTrace();
	    }
	}
}
