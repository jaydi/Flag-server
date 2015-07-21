package com.flag.engine.utils;

import java.util.List;

public class GCMMessage {
	private List<String> registration_ids;
	private GCMData data;

	public GCMMessage() {
		super();
	}

	public List<String> getRegistration_ids() {
		return registration_ids;
	}

	public void setRegistration_ids(List<String> registration_ids) {
		this.registration_ids = registration_ids;
	}

	public GCMData getData() {
		return data;
	}

	public void setData(GCMData data) {
		this.data = data;
	}

}
