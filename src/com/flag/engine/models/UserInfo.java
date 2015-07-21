package com.flag.engine.models;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, table = "userinfos")
public class UserInfo extends BaseModel {
	@PrimaryKey
	@Persistent
	private Long userId;

	@Persistent
	private String pushToken;

	@Persistent
	@Index
	private String phone;

	@Persistent
	@Index
	private int sex;

	@Persistent
	@Index
	private long birth;

	@Persistent
	private int job;

	@Persistent
	private String verificationCode;

	@Persistent
	private int os;

	@Persistent
	private long lastTime;

	@Persistent
	private String recoEmail;

	public UserInfo() {
		super();
	}

	public UserInfo(Long userId) {
		this.userId = userId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getPushToken() {
		return pushToken;
	}

	public void setPushToken(String pushToken) {
		this.pushToken = pushToken;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public long getBirth() {
		return birth;
	}

	public void setBirth(long birth) {
		this.birth = birth;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public int getOs() {
		return os;
	}

	public void setOs(int os) {
		this.os = os;
	}

	public boolean isAndroid() {
		return String.valueOf(os).startsWith("1");
	}

	public boolean isIOS() {
		return String.valueOf(os).startsWith("2");
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public String getRecoEmail() {
		return recoEmail;
	}

	public void setRecoEmail(String recoEmail) {
		this.recoEmail = recoEmail;
	}

	public void update(UserInfo userInfo) {
		if (userInfo.getPushToken() != null && !userInfo.getPushToken().isEmpty())
			this.pushToken = userInfo.getPushToken();
		if (userInfo.getPhone() != null && !userInfo.getPhone().isEmpty())
			this.phone = userInfo.getPhone();
		if (userInfo.getSex() != 0)
			this.sex = userInfo.getSex();
		if (userInfo.getBirth() != 0)
			this.birth = userInfo.getBirth();
		if (userInfo.getJob() != 0)
			this.job = userInfo.getJob();
		if (userInfo.getVerificationCode() != null && !userInfo.getVerificationCode().isEmpty())
			this.verificationCode = userInfo.getVerificationCode();
		if (userInfo.getOs() != 0)
			this.os = userInfo.getOs();
		if (userInfo.getLastTime() != 0)
			this.lastTime = userInfo.getLastTime();
	}

	public boolean isEmpty() {
		if (sex == 0 && birth == 0 && job == 0)
			return true;
		else
			return false;
	}

	public boolean verifyCode(String code) {
		if (verificationCode == null || verificationCode.isEmpty())
			return false;

		if (verificationCode.substring(0, verificationCode.indexOf("+")).equals(code))
			return true;

		return false;
	}
}
