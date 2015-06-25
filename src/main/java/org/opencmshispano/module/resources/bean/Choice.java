package org.opencmshispano.module.resources.bean;

import java.util.List;

public class Choice {
	
	private String fieldName;
	private List subfields;
	
	public Choice(String fieldName, List subfields) {
		super();
		this.fieldName = fieldName;
		this.subfields = subfields;
	}
	
	public Choice(String fieldName) {
		super();
		this.fieldName = fieldName;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public List getSubfields() {
		return subfields;
	}
	public void setSubfields(List subfields) {
		this.subfields = subfields;
	}
	
	

}
