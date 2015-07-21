package com.flag.engine.models;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Version extends BaseModel {
	@PrimaryKey
	@Persistent
	private String version;
	
	@Persistent
	private long createdAt;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public static Version getDefault() {
		Version version = new Version();
		version.setVersion("1.0");
		version.setCreatedAt(new Date().getTime());
		return version;
	}
}
