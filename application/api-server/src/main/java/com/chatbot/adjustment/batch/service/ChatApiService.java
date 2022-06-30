package com.chatbot.adjustment.batch.service;

import com.chatbot.adjustment.kafka.domain.CdrUnit;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class ChatApiService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${chat.server.host:http://www.shobot.co.kr:80}")
	private String chattingServerHost;

	/**
	 * 지정 기간의 CDR 개수 조회
	 * @param from
	 * @param to
	 * @return
	 */
	public List<Map<Long, Integer>> getCdrCount(String from, String to) {

		List<Map<Long, Integer>> list = new ArrayList<>();

		try {
			final Map<String, String> params = new HashMap<String, String>();
			params.put("from", from); //201803010000
			params.put("to", to); //201803020000

			final Response response = RestAssured.given()
					.with()
					.params(params)
					.when()
					.get(chattingServerHost + "/billing/cdrs/count");


			if(response.statusCode() == HttpStatus.SC_OK) {
				list = response.jsonPath().getList("details");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		return list;
	}

	/**
	 * 지정 기간의 CDR 목록 요청
	 * @param from
	 * @param to
	 * @return
	 */
	public List<CdrUnit> getList(String from, String to) {

		final Map<String, String> params = new HashMap<String, String>();
		params.put("from", from);
		params.put("to", to);

		List<CdrUnit> cdrs = new ArrayList<>();

		try {
			final Response response = RestAssured.given()
					.with()
					.params(params)
					.when()
					.get(chattingServerHost + "/billing/cdrs");

			ObjectMapper mapper = new ObjectMapper();

			if (response.statusCode() == HttpStatus.SC_OK) {
				InputStream is = response.body().asInputStream();

				try {
					BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

					String str;
					while ((str = streamReader.readLine()) != null) {
						logger.debug("read : " + str);

						JSONObject jsonObject = new JSONObject(str);
						CdrUnit cdrUnit = mapper.readValue(str, CdrUnit.class);

						cdrs.add(cdrUnit);
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cdrs;
	}

	/**
	 * 쇼핑몰별 잔고 상태 변경
	 * 정산 잔고가 바닥나거나, 정상화됐을 때, 이 API 통해 챗팅서버쪽으로 상태 동기화
	 * @param mallID
	 * @param isActivce
	 */
	public void changeBalanceStatus(long mallID, boolean isActivce) {

		final Map<String, String> params = new HashMap<String, String>();
		params.put("balanceState", isActivce == true ? "true" : "false");

		try {
			final Response response = RestAssured.given()
					.with()
					.params(params)
					.when()
					.put(chattingServerHost + "/billing/malls/" + mallID);

			boolean adminState = false;
			boolean balanceState = false;

			if(response.statusCode() == HttpStatus.SC_OK) {
				adminState = response.jsonPath().getBoolean("adminState");
				balanceState = response.jsonPath().getBoolean("balanceState");

				logger.debug("admin state : " + adminState + ", balancestate : " + balanceState);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 쇼핑몰 서비스 상태 조회
	 * serviceState : 서비스 가능 여부(adminState && balanceState)
	 * adminState : 관리자가 설정한 서비스 On/Off
	 * balanceState : 전액 존재 여부(무료건수 포함)
	 * @param mallID
	 */
	public boolean getMallStatus(long mallID) {

		boolean adminState = false;
		boolean balanceState = false;

		try {
			final Response response = RestAssured.given()
					.with()
					.when()
					.get(chattingServerHost + "/billing/malls/" + mallID);

			if(response.statusCode() == HttpStatus.SC_OK) {
				adminState = response.jsonPath().getBoolean("adminState");
				balanceState = response.jsonPath().getBoolean("balanceState");

				logger.debug("admin state : " + adminState + ", balancestate : " + balanceState);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return adminState && balanceState;
	}
}
