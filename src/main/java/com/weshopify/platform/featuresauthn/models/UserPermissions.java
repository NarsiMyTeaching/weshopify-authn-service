package com.weshopify.platform.featuresauthn.models;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

@Entity
@Data
public class UserPermissions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6076958089667595763L;

	@Id
	//@GeneratedValue(strategy = GenerationType.AUTO)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String action;
	
}
