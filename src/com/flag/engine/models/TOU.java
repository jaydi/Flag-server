package com.flag.engine.models;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class TOU extends BaseModel {
	@PrimaryKey
	@Persistent
	private Long id;
	
	@Persistent
	private Text tou;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Text getTou() {
		return tou;
	}

	public void setTou(Text tou) {
		this.tou = tou;
	}

}
