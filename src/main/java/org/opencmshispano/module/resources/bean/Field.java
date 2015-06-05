package org.opencmshispano.module.resources.bean;

import java.util.List;

/**
 * Created by sraposo on 07/05/2015.
 * Objeto que almacena la información relevante de un campo de un recurso
 */
public class Field {

    private String name;
    private String value;
    private String type;
    private List<Field> fields;

    public static final String FIELD_TYPE_SIMPLE = "simple";
    public static final String FIELD_TYPE_NESTED = "nested";
    public static final String FIELD_TYPE_MULTIPLE_SIMPLE = "multiplesimple";
    public static final String FIELD_TYPE_MULTIPLE_NESTED = "multiplenested";
    public static final String FIELD_TYPE_MULTIPLE_CHOICE = "choice";

    public Field() {
    }

    public Field(String name) {
        this.name = name;
    }

    public Field(String name, String value, String type, List<Field> fields) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

	@Override
	public String toString() {
		return "Field [name=" + name + ", value=" + value + ", type=" + type
				+ ", fields=" + fields + "]";
	}
    
    
}
