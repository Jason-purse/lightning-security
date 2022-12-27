package com.generatera.authorization.web;

import com.generatera.authorization.jpa.entity.UserPrincipal;
import com.jianyue.lightning.exception.DefaultApplicationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自定义登录页面
 * @author weir
 *
 */
@RestController
@RequestMapping
public class LoginController {

	// http://127.0.0.1:9000/springauthserver
	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/login-error")
	public String loginError(Model model) {
		model.addAttribute("loginError", true);
		return login();
	}


	@GetMapping("user")
	public UserPrincipal userPrincipal() {
		throw new DefaultApplicationException("123123",null);
	}



}
