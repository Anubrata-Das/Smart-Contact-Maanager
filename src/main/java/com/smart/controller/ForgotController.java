package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	//open email id form handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email, HttpSession session) {
		
		//Generating OTP of 4 digit
		int otp = random.nextInt(999999);
		
		//writ code for send otp to email
		String subject ="OTP from smart contact manager";
		String message = ""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+ "OTP is  "
				+"<b>"+otp
				+ "</b>"
				+ "</h1>"
				+ "</div>";
		String to = email;
		
		boolean flag = this.emailService.sendMail(to,subject,message);
		
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
			
		}
		else {
			session.setAttribute("message", "Check your email id !!");
			return "forgot_email_form";
		}		
		
	}
	//verify OTP
	@PostMapping("verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp,HttpSession session){
		
		int myOtp = (Integer)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		
		if(myOtp == otp) {
			//password change form
			User user = this.userRepository.getUserbyUserName(email);
			
			if(user == null) {
				//send error message
				session.setAttribute("message", "User does not exist with this email");
				return "forgot_email_form";
			}
			else {
				//send change password form
				
				
			}
			
			return "password_change_form";
		}
		else {
			session.setAttribute("message", "Oops !! wrong OTP..Check again");
			return "verify_otp";
		}
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session) {
		
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserbyUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=Password changed successfully..";
	}

}
