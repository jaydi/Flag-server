package com.flag.engine.models;

import javax.jdo.annotations.NotPersistent;

public class BaseModel {
	@NotPersistent
	protected int statusCode;

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}
