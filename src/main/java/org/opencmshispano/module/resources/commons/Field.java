package org.opencmshispano.module.resources.commons;

import java.util.ArrayList;
import java.util.List;

public class Field 
{
	private String path;
	private String name;
	private String type;
	private String OpenCmsType;
	private boolean simple;
	private int maxOccurs;
	private int minOcccurs;
	private List values;
	private int index;


	public Field() 
	{
		this.path = "";
		this.name = "";
		this.type = "";
		this.values = new ArrayList();
		this.simple = true;
		this.maxOccurs = 0;
		this.minOcccurs = 0;
	}
	
	public Field(String path, String name, String type, ArrayList values, boolean simple, int maxOccurs, int minOcccurs) 
	{
		//this.type = parseType(type);
		this.type = type;
		this.path = path;
		this.name = name;
		this.simple = simple;
		this.values = values;
		this.maxOccurs = maxOccurs;
		this.minOcccurs = minOcccurs;
	}
	
	public Field(String path, String name, String type, boolean simple, int maxOccurs, int minOcccurs) 
	{
		//this.type = parseType(type);
		this.type = type;
		this.path = path;
		this.name = name;
		this.simple = simple;
		this.maxOccurs = maxOccurs;
		this.minOcccurs = minOcccurs;
		this.values = new ArrayList();
	}
	
	public String getPath() 
	{
		return path;
	}
	
	public void setPath(String path) 
	{
		this.path = path;
	}
	public String getName() 
	{
		return name;
	}
	
	public void setName(String name) 
	{
		this.name = name;
	}
	
	public int getMaxOccurs() 
	{
		return maxOccurs;
	}
	
	public void setMaxOccurs(int maxOccurs) 
	{
		this.maxOccurs = maxOccurs;
	}
	
	public int getMinOcccurs() 
	{
		return minOcccurs;
	}
	
	public void setMinOcccurs(int minOcccurs) 
	{
		this.minOcccurs = minOcccurs;
	}

	public String getType() 
	{
		return type;
	}

	public void setType(String type) 
	{
		//setOpenCmsType(type);
		//this.type = parseType(type);
		this.type=type;
	}	
	
	public boolean isSimple()
	{
		return simple;
	}

	public void setSimple(boolean simple) 
	{
		this.simple = simple;
	}
	
	public List getValues() 
	{
		return values;
	}

	public void setValue(List values)
	{
		this.values = values;
	}
	
	
	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index) 
	{
		this.index = index;
	}
	

	public String getOpenCmsType() 
	{
		return OpenCmsType;
	}

	public void setOpenCmsType(String openCmsType) 
	{
		OpenCmsType = openCmsType;
	}
	
	public String toString() 
	{
		return "Nombre: " + name + " Ruta: " + path + " valor: " + values + " MaxOccurs: " + maxOccurs + " MinOccurs: " + minOcccurs + " Tipo: " + type + " simple: " + simple;
	}
	
	private String parseType(String type)
	{
		String typeAux="";
		if(type.equals("OpenCmsString"))
			typeAux = "text";
		else if(type.equals("OpenCmsHtml"))
			typeAux = "textarea";
		else if(type.equals("OpenCmsDateTime"))
			typeAux = "date";
		else if(type.equals("OpenCmsVfsFile"))
			typeAux = "file";
		else if(type.equals("OpenCmsBoolean"))
			typeAux = "select";
		else if(type.equals("OpenCmsExternalLink"))
			type="externalLink";
		else if(type.equals("OpenCmsExternalLink"))
			type="externalLink";
		return typeAux;
	}

}
