package com.chatbot.adjustment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

@Component
public class MailServiceImpl implements MailService {

	@Value("${mail.from}")
	private String fromEmail;

	@Autowired
	public JavaMailSender mailSender;

	@Autowired
	public MailContentBuilder mailContentBuilder;

	@Override
	public void sendMessage(String recipient, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(recipient);
		message.setSubject(subject);
		message.setText(text);

		try {
			mailSender.send(message);
		} catch (MailException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean sendMessageUsingTemplate(String recipient, String subject, String text, String searchType) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(fromEmail, "i-Bot");
			messageHelper.setTo(recipient);
			messageHelper.setSubject(subject);
			String content = mailContentBuilder.build(text, searchType);
			messageHelper.setText(content, true);
		};

		try {
			mailSender.send(messagePreparator);
		} catch (MailException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
