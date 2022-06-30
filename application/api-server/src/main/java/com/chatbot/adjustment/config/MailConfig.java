package com.chatbot.adjustment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

	@Value("${mail.host}")
	private String host;

	@Value("${mail.port}")
	private int port;

	@Value("${mail.username}")
	private String userName;

	@Value("${mail.password}")
	private String password;

	@Value("${mail.default-encoding}")
	private String encoding;

	@Value("${mail.protocol}")
	private String protocol;

	@Value("${mail.debug}")
	private String debug;

	@Value("${mail.smtp.ssl-enable}")
	private String isSSL;


	@Bean
	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(host);
		mailSender.setPort(port);

		mailSender.setUsername(userName);
		mailSender.setPassword(password);
		mailSender.setDefaultEncoding(encoding);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", protocol);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", debug);
		props.setProperty("mail.smtp.ssl.enable", isSSL);
		if("true".equalsIgnoreCase(isSSL)) {
			props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
		}

		return mailSender;
	}
}
