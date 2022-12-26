package com.generatera.authorization.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 自定义登录页面
 * @author weir
 *
 */
@Controller
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

}
