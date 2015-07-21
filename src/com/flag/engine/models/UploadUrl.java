package com.flag.engine.models;

public class UploadUrl extends BaseModel {
	private String url;

	public UploadUrl() {
		super();
	}

	public UploadUrl(String url) {
		super();
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
