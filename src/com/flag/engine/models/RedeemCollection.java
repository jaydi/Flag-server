package com.flag.engine.models;

import java.util.List;

public class RedeemCollection extends BaseModel {
	private List<Redeem> redeems;

	public RedeemCollection() {
		super();
	}

	public RedeemCollection(List<Redeem> redeems) {
		super();
		this.redeems = redeems;
	}

	public List<Redeem> getRedeems() {
		return redeems;
	}

	public void setRedeems(List<Redeem> redeems) {
		this.redeems = redeems;
	}

}
