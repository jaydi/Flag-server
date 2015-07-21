package com.flag.engine.models;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, table = "redemptions")
public class Redemption extends BaseModel {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
	private Long id;

	@Persistent
	private long userId;

	@Persistent
	private String userPhone;

	@Persistent
	private long redeemId;

	@Persistent
	private String redeemName;

	@Persistent
	private int redeemPrice;

	@Persistent
	private long createdAt;

	@Persistent
	private boolean sent;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public long getRedeemId() {
		return redeemId;
	}

	public void setRedeemId(long redeemId) {
		this.redeemId = redeemId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRedeemName() {
		return redeemName;
	}

	public void setRedeemName(String redeemName) {
		this.redeemName = redeemName;
	}

	public int getRedeemPrice() {
		return redeemPrice;
	}

	public void setRedeemPrice(int redeemPrice) {
		this.redeemPrice = redeemPrice;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isSent() {
		return sent;
	}

	public void setSent(boolean sent) {
		this.sent = sent;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(userPhone).append("/").append(redeemName).append("/").append(redeemPrice);
		return sb.toString();
	}

}
