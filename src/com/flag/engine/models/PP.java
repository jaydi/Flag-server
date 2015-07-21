package com.flag.engine.models;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PP extends BaseModel {
	@PrimaryKey
	@Persistent
	private Long id;

	@Persistent
	private Text pp;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Text getPp() {
		return pp;
	}

	public void setPp(Text pp) {
		this.pp = pp;
	}

}
