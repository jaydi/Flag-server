package com.flag.engine.models;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, table = "likes")
public class Like extends BaseModel {
	public static final int TYPE_SHOP = 1;
	public static final int TYPE_ITEM = 2;

	@Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
	@PrimaryKey
	private Long id;

	@Persistent
	@Index
	private Long userId;

	@Persistent
	@Index
	private Long targetId;

	@Persistent
	@Index
	private int type;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
