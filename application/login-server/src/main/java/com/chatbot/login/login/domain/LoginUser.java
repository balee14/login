package com.chatbot.login.login.domain;

import com.chatbot.login.homepage.domain.TbUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class LoginUser implements UserDetails {

	private long user_id;
	private String username;
	private String password;
	private boolean enabled;
	private String user_type;
	private long mall_id;
	private List<String> roles;

	private TbUser tbUser;

	public LoginUser() {
	}

	public LoginUser(User user) {
		this.user_id = user.getUser_id();
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.enabled = user.isEnabled();
		this.user_type = user.getUser_type();
		this.mall_id = user.getMall_id();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorityList = new ArrayList<>();
		for(String role : roles) {
			authorityList.add(new SimpleGrantedAuthority(role));
		}

		return authorityList;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
}
