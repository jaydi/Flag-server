package com.flag.engine.models;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class User extends BaseModel {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	@Index
	private String email;

	@Persistent
	private String password;

	@Persistent
	private int reward;

	public User() {
		super();
	}

	public User(UserForm userForm) {
		this.email = userForm.getEmail();
		this.password = userForm.getPassword();
	}

	public User(User user) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.reward = user.getReward();
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

	public void rewarded(int reward) {
		this.reward += reward;
	}

	public void redeemed(int redeem) {
		reward -= redeem;
	}
}
