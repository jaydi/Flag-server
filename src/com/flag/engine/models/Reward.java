package com.flag.engine.models;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, table = "rewards")
public class Reward extends BaseModel {
	public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 3;

	public static final int TYPE_SHOP = 1;
	public static final int TYPE_ITEM = 2;
	public static final int TYPE_INVITATION = 3;
	public static final int TYPE_REDEMPTION = 10;

	public static final int VALUE_INVITATION = 300;

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
	private String targetName;

	@Persistent
	@Index
	private int type;

	@Persistent
	private int reward;

	@Persistent
	@Index
	private long createdAt;

	public Reward() {
		super();
	}

	public Reward(Long userId, Long targetId, String targetName, int type, int reward) {
		super();
		this.userId = userId;
		this.targetId = targetId;
		this.targetName = targetName;
		this.type = type;
		this.reward = reward;
	}

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

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}
}
