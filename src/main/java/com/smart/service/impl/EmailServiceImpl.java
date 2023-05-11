package com.smart.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.smart.service.EmailService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Value
	("${spring.mail.username}") private String sender;

	@Override
	public boolean sendMail( String to, String subject, String body) {
		
        try {
        	 MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        	 
        	 MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,true);
        	 
        	 mimeMessageHelper.setFrom(sender);
        	 mimeMessageHelper.setTo(to);
        	 mimeMessageHelper.setSubject(subject);
//        	 mimeMessageHelper.setText(body);
        	 mimeMessage.setContent(body, "text/html");
        	 
        	 
        	 javaMailSender.send(mimeMessage);
        	 
        	 return true;
        }
 
        // Catch block to handle the exceptions
        catch (Exception e) {
        	e.getMessage();
            return false;
        }
		
	}

}
