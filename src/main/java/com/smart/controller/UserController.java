package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method to add common data
	@ModelAttribute
	public void addCommondata(Model model, Principal principal) {
		String  username = principal.getName(); 
		System.out.println("Username "+username );
		
		//get the user using username (email)
		User user=userRepository.getUserbyUserName(username);
		model.addAttribute("user",user);
	}
	
	//home dashboard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	//handler for add-contact form
	
	@GetMapping("/add-contact")
	public String addContactForm(Model model) {
		model.addAttribute("title","Add contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileimage") MultipartFile file, 
			Principal principal, HttpSession session) {
		try {
			
			String name = principal.getName();
			User user=this.userRepository.getUserbyUserName(name);
			
			
			
			
			
			//processing and uploading file
			if(file.isEmpty()) {
				//System.out.println("File is empty");
				session.setAttribute("message", new Message("Your contact is added !!", "success"));
				contact.setImage("contact.png");
			}
			else {
				contact.setImage(file.getOriginalFilename());
				File savFile=new ClassPathResource("static/img").getFile();
				
				Path path= Paths.get(savFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is uploaded");
				//message success
				session.setAttribute("message", new Message("Your contact is added !!", "success"));
				
				
			}
			
			user.getContacts().add(contact);
			contact.setUser(user);
			
			this.userRepository.save(user);
			
			System.out.println("data "+contact);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			//message error
			session.setAttribute("message", new Message("Somwthing went wrong !! Try again", "danger"));

		}
		return "normal/add_contact_form";
		
	}
	
	//show contacts handler
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page,Model m, Principal principal) {
		m.addAttribute("title","Show User Contacts");
	
		String username = principal.getName();
		User user = this.userRepository.getUserbyUserName(username);
		
		Pageable pageable = PageRequest.of(page, 4);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentpage",page);
		m.addAttribute("totalpages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	
	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		
		Optional<Contact> cOptional = this.contactRepository.findById(cId);
		Contact contact = cOptional.get();
		
		String username = principal.getName();
		User user = this.userRepository.getUserbyUserName(username);
		
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		return "normal/contact_detail";
	}
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session,Principal principal)
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		User user = this.userRepository.getUserbyUserName(principal.getName());
		
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("Contact deleted successfully", "success"));
		
		
		return "redirect:/user/show-contacts/0";
		
	}
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		
		m.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	//Update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,
			@RequestParam("profileimage") MultipartFile file,Model m,
			HttpSession session,Principal principal) {
		
		try {
			
			Contact oldcontactdetails = this.contactRepository.findById(contact.getcId()).get();
			//image
			if(!file.isEmpty()) {
				//file rewrite
				
				//delete old photo
				
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file2 = new File(deleteFile,oldcontactdetails.getImage());
				file2.delete();
				
				
				
				//update new photo
				File savFile=new ClassPathResource("static/img").getFile();
				
				Path path= Paths.get(savFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
				session.setAttribute("message", new Message("Your contact is updated..", "success"));
				
			}
			else {
				contact.setImage(oldcontactdetails.getImage());
			}
			
			User user = this.userRepository.getUserbyUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	// Your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	//change-password method
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword
			,Principal principal,HttpSession session) {
		
		String username = principal.getName();
		User currentuser  = this.userRepository.getUserbyUserName(username);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentuser.getPassword())) {
			//change password
			currentuser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentuser);
			session.setAttribute("message", new Message("Your password is successfully changed", "success"));

		}
		else {
			//error
			session.setAttribute("message", new Message("Oops..Please enter correct password", "danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
	//creating Order for payment
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createorder(@RequestBody Map<String, Object> data) {
		
		System.out.println(data);
		
		int amt = Integer.parseInt(data.get("amount").toString());
		
		try {
			var client = new RazorpayClient("rzp_test_91XGmdbt1l0HaY", "LWY2c4VMZ3WGGIfqHDiLCUm9");
			
			JSONObject object = new JSONObject();
			object.put("amount", amt*100);
			object.put("currency", "INR");
			object.put("receipt", "txn_235425");
			
			//creating new order
			Order order = client.orders.create(object);
			System.out.println(order);
			
			return order.toString();
			
		} 
		
		
		catch (Exception e) {
			e.getMessage();
		}
		
//		try {
//			  JSONObject orderRequest = new JSONObject();
//			  orderRequest.put("amount", 50000); // amount in the smallest currency unit
//			  orderRequest.put("currency", "INR");
//			  orderRequest.put("receipt", "order_rcptid_11");
//
//			  Order order = RazorpayClient.Orders.create(orderRequest);
//		} catch (RazorpayException e) {
//		  // Handle Exception
//		  System.out.println(e.getMessage());
//		}
		
		return "done";
	}
}
