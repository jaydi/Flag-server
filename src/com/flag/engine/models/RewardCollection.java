package com.flag.engine.models;

import java.util.List;

public class RewardCollection extends BaseModel {
	private List<Reward> rewards;

	public RewardCollection() {
		super();
	}

	public RewardCollection(List<Reward> rewards) {
		super();
		this.rewards = rewards;
	}

	public List<Reward> getRewards() {
		return rewards;
	}

	public void setRewards(List<Reward> rewards) {
		this.rewards = rewards;
	}
}
