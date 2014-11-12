package com.squarecash4glass.util;

import java.io.Serializable;

public class SquareAuthBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String authUrl;
	String applicationId;
	String redirectURI;
	String role;
	String state;
	
	public SquareAuthBean(){
		
	}
	
	public SquareAuthBean (String sandboxURL,String tokenEndpointURI, String role, String applicationId) {
		this.authUrl=sandboxURL+tokenEndpointURI;
		this.applicationId=applicationId;
		this.redirectURI="/oauth2callbacksquare";
		this.role=role;
		this.state="ciao";
	}
	
	
	public String getAuthUrl() {
		return authUrl;
	}
	public void setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
	}
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	public String getRedirectURI() {
		return redirectURI;
	}
	public void setRedirectURI(String redirectURI) {
		this.redirectURI = redirectURI;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

}