package com.jesperapps.email.api.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import org.springframework.stereotype.Service;


@Service
public class OtpSmsServiceImpl implements OtpSmsService {

	
	private static final Integer EXPIRE_MINS = 7;
	
	@Override
	public int generateOTP(String phoneNumber) {
		System.out.println("ph "+ phoneNumber);
		try {
			
			Random random = new Random();
			int otp = 100000 + random.nextInt(900000);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String otpGenerationTime = dtf.format(now);
			
			
			return otp;
		} catch (Exception e) {
			System.out.println("Exc" +e.getLocalizedMessage());
			return 0;
		}

	}
	
	
	@Override
	public void sendSms(String string, String phone) {
		try {

			String apiKey = "elNIWdPL4TVuhKAGt7BnjMoEw9ZFyYU6cXx5kg2J8zHaiOs01Dn50wUgxpFkDubhRT9Ba87Ny6vlMtWr";
			String sendId = "FSTSMS";
			// important step...
			string = URLEncoder.encode(string, "UTF-8");
			String language = "english";

			String route = "t";

			String myUrl = "https://www.fast2sms.com/dev/bulk?authorization=" + apiKey + "&sender_id=" + sendId
					+ "&message=" + string + "&language=" + language + "&route=" + route + "&numbers=" + phone;

			// sending get request using java..

			URL url = new URL(myUrl);

			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			con.setRequestMethod("GET");

			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("cache-control", "no-cache");
			System.out.println("Wait..............");

			int code = con.getResponseCode();

			System.out.println("Response code : " + code);

			StringBuffer response = new StringBuffer();

			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				response.append(line);
			}

			System.out.println(response);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}

}
