package com.smart.controller;

import java.util.Random;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;


@Controller
public class HomeController {
	Random random = new Random(1000);
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailService emailService;
	
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home smart - contact");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About smart - contact");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register smart - contact");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	
	//handler for register
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,@RequestParam(value = "agreement",defaultValue= "false") boolean agreement, Model model,
			HttpSession session) 
	{
		try {
			if(result1.hasErrors()) {
				System.out.println("Error "+result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			if(!agreement) {
				System.out.println("Accept terms");
				throw new Exception("You did not Accept terms & conditions");
			}
			
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("deafult.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Aggrement " + agreement);
			System.out.println("user: " + user);
			
			User result=this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			return "signup";
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
			
			return "signup";
		}
		
	}
	
	
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title","Login page");
		return "login";
	}
}
