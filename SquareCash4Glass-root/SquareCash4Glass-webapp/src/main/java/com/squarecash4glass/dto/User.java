package com.squarecash4glass.dto;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class User {
	@Id Long id;
	String email;
	List<Key<StoredCredential>> credentials = new ArrayList<Key<StoredCredential>>();
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<Key<StoredCredential>> getCredentials() {
		return credentials;
	}
	public void setCredentials(List<Key<StoredCredential>> credentials) {
		this.credentials = credentials;
	}
	
	
}
