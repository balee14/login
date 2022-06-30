package com.chatbot.adjustment.kafka.domain;

import lombok.Data;

@Data
public class CdrUnit {

	private long mall;
	private String user;
	private String session;
	private long date;
}
