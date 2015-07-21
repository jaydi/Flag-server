package com.flag.engine.models;

public class InvitationForm extends BaseModel {
	private long userId;
	private String recoEmail;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getRecoEmail() {
		return recoEmail;
	}

	public void setRecoEmail(String recoEmail) {
		this.recoEmail = recoEmail;
	}

}
