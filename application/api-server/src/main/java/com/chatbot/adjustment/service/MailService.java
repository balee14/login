package com.chatbot.adjustment.service;

public interface MailService {

	void sendMessage(String to, String subject, String text);
	boolean sendMessageUsingTemplate(String to, String subject, String text, String searchType);
}
