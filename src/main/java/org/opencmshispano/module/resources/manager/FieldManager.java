package org.opencmshispano.module.resources.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencmshispano.module.resources.commons.Field;
import org.opencmshispano.module.util.Bundle;


public class FieldManager
{

	/*Class attributes*/
	private CmsJspActionElement cms;
	private CmsObject cmsObject;
	private HttpServletRequest request;

	//HashMap attachmentMap;
	
	private static final Log LOG = CmsLog.getLog(ResourceManager.class);
	private static final Bundle BUNDLE = new Bundle("org.opencmshispano.module.resources.workplace");
	/**
	 *@param CmsJspActionElement
	 *
	 */
	public FieldManager(CmsJspActionElement cms, HttpServletRequest request )
	{
		this.cms = cms;
		this.cmsObject = cms.getCmsObject();
		this.request = request;
	}
	
	public HashMap getFields(List files)
	{
       // List files = CmsRequestUtil.readMultipartFileItems(request);
        Iterator itFiles = files.iterator();
        HashMap hmSimple = new HashMap();
        HashMap hmNested = new HashMap();
        while(itFiles.hasNext())
		{
                FileItem fi = (FileItem)itFiles.next();  
                String fieldType = fi.getFieldName();
                if(!fieldType.startsWith("count") && (fi.getSize()>0 || (fi.getName()!=null && !fi.getName().equals(""))))
                { 
                	if(fieldType.indexOf("_")!=fieldType.lastIndexOf("_") && fieldType.indexOf("_")!=-1)
                	{
                		  int numero;
                          try
                          {
                              numero=new Integer(fieldType.substring(fieldType.lastIndexOf("_")+1, fieldType.length())).intValue();
				          }
                          catch(Exception e)
                          {
                              numero=0;
                          } 
                          String nameAux = "";
                          nameAux = fieldType.substring(0, fieldType.lastIndexOf("_"));
                          String name = nameAux.replace("_", "/");
					      Field field = new Field();
					      field.setName(name.substring(name.lastIndexOf("/")+1));
					      field.setPath(name);
					      field.setSimple(false);
                          String value="";
                          if(fi.isFormField())
                          {  
                              value=fi.getString();
                          }
                          else
                          {
                        	  value=uploadFile(fi, fieldType);
                          }
                          field.getValues().add(value);
                          if(!hmNested.containsKey(name.substring(0,name.indexOf("/"))))
                          {
                        	  ArrayList list = new ArrayList();
                        	  HashMap aux = new HashMap();
                        	  list.add(field);
                              aux.put(new Integer(numero), list);
                              hmNested.put(name.substring(0,name.indexOf("/")), aux);
                          }
                          else 
                          {
    					      HashMap aux = (HashMap)hmNested.get(name.substring(0,name.indexOf("/")));
                              if(aux.containsKey(new Integer(numero)))
                              { 
                                    ArrayList listAux = (ArrayList) aux.get(new Integer(numero));
                                    listAux.add(field);
                              }
                              else
                              {
                                    ArrayList list = new ArrayList();
                                    list.add(field);
                                    aux.put(new Integer(numero),list);
                                    hmNested.put(name.substring(0,name.indexOf("/")), aux);
                              }
					}
				}
				else
				{
                		 String nameAux = fieldType;
						 String name = fieldType;
						 if(nameAux.indexOf("_")!=-1)
						 {
					    	     nameAux = fieldType.substring(0, fieldType.indexOf("_"));
					    	     name = name.replace("_", "");
						 }
					     Field field = new Field();
						 field.setName(name);
						 field.setPath(nameAux);
						 field.setSimple(true);
						 String value="";
						 if(fi.isFormField())
                         {  
                              value=fi.getString();
                         }
                         else
                         {
                        	 uploadFile(fi, fieldType);
                         }
						 field.getValues().add(value);
						 if(!hmSimple.containsKey(nameAux))
						 {
							ArrayList list = new ArrayList();
							list.add(field);
						    hmSimple.put(name, list);
						 }
						 else 
						 {
							ArrayList list = (ArrayList)hmSimple.get(nameAux);
	                        list.add(field);
						 }
				}
            }
		}
        HashMap data = treatSimpleData(hmSimple);
        data.putAll(treatNestedData(hmNested));
//        HashMap data = hmSimple;
//        data.putAll(hmNested);
		return data;
	}
	
	public HashMap treatSimpleData(HashMap hm)
	{
		HashMap data = new HashMap();
		Iterator itHash = hm.keySet().iterator();
		while(itHash.hasNext())
		{
			String key = (String)itHash.next();
			ArrayList list = (ArrayList)hm.get(key);
			if(list.size()>1)
			{
				Iterator itList = list.iterator();
				ArrayList listAux = new ArrayList();
				while(itList.hasNext())
				{
					Field field =(Field)itList.next();
					LOG.error(field);
					if (BUNDLE.getString("tsol.dates").indexOf(field.getName())!=-1)
					{
						String value = (String)field.getValues().get(0);
						value = getMillis(value);
						field.getValues().add(0,value);
                    	listAux.add(field.getValues());
					}                    	
                    else
                    {
                    	listAux.add(field.getValues());
                    }
				}
				data.put(key, list);
			}				
			else
			{
				Field field = (Field)list.get(0);
				LOG.error(field);
                if (BUNDLE.getString("tsol.dates").indexOf(field.getName())!=-1)
                {
					String value = (String)field.getValues().get(0);
					value = getMillis(value);
					data.put(key, value);
                }
                else
                {
                	data.put(field.getName(), field.getValues());
                }
			}
		}
		return data;
	}
	
	public HashMap treatNestedData(HashMap hm)
	{
		HashMap data = new HashMap();
		Iterator itHash = hm.keySet().iterator();
		while(itHash.hasNext())
		{
			Object key = itHash.next();
			LOG.error("Key: " + key);
            HashMap hashAux = (HashMap)hm.get(key);
            Set keySet = hashAux.keySet();
            Iterator itHash2 = keySet.iterator();	
            LOG.error("tamano del keyset" + keySet.size());
			if(keySet.size()>1)
			{
				ArrayList array = new ArrayList();
				HashMap hashAux2 = new HashMap();	
				/**ESTO AQUI ES UNA PRUEBA*/
				while (itHash2.hasNext())
				{
					//LOG.error("key: " + itHash2.next());
					ArrayList fields = (ArrayList) hashAux.get(itHash2.next());
					LOG.error("Fields  " + fields);
	                Iterator itFields = fields.iterator();
	                while(itFields.hasNext())
	                {
	                    Field field = (Field)itFields.next();
	                    LOG.error(field);
	                    if (BUNDLE.getString("tsol.dates").indexOf(field.getName())!=-1)
	                    	hashAux2.put(field.getName(), getMillis((String)field.getValues().get(0)));
	                    else
	                    {
	                    	LOG.error("PASOOOOOO!!!!");
	                    	String aux = field.getPath();
                    		aux=aux.substring(aux.indexOf("/")+1);
                    		if(aux.equals(field.getName()))
                    		{
                    			if(hashAux2.containsKey(field.getName()))
                    			{
                    				LOG.error("HOLAAAA!");
                    				//Field fieldAux = (Field)hashAux2.get(field.getName());
                    				LOG.error("HOLAAAA!11111111111111" + hashAux2);
                    				LOG.error("HOLAAAA!");
                    				ArrayList array2 = (ArrayList)hashAux2.get(aux);
                    				LOG.error("Field values: " + field.getValues());
                    				List array3 = field.getValues();
                    				for(int i=0; i<array3.size();i++) 	                   				
                    					array2.add(array3.get(i));
                    				//fieldAux.getValues().add(field.getValues());
                    			}
                    			else
                    			{
                    				hashAux2.put(field.getName(), field.getValues());
                    				//array.add(hashAux2);
                    			}
                    		}
                    		else
                    		{
                       			if(hashAux2.containsKey(aux))
                       			{
                    				LOG.error("HOLAAAA!11111111111111" + hashAux2);
                    				LOG.error("HOLAAAA!");
                    				ArrayList array2 = (ArrayList)hashAux2.get(aux);
                    				LOG.error("Field values: " + field.getValues());
                    				List array3 = field.getValues();
                    				for(int i=0; i<array3.size();i++) 	                   				
                    					array2.add(array3.get(i));
                       			}
                       			else
                       			{
                       				hashAux2.put(aux, field.getValues());
                       				//array.add(hashAux2);
                       			}
                    		}
	                    }
	                }
	                //array.add(hashAux2);
	            }
				array.add(hashAux2);
				data.put(key, array);
				
			}
			else
			{
				HashMap hashAux2 = new HashMap();	
				while (itHash2.hasNext())
				{                               
					Object obj =itHash2.next();
					LOG.error(obj);
					ArrayList fields = (ArrayList) hashAux.get(obj);
	                Iterator itFields = fields.iterator();
	                while(itFields.hasNext())
	                {
	                    Field field = (Field)itFields.next();
	                    LOG.error(field);
	                    if (BUNDLE.getString("tsol.dates").indexOf(field.getName())!=-1)
	                    	hashAux2.put(field.getName(), getMillis((String)field.getValues().get(0)));
	                    else
	                    {
	                    	String aux = field.getPath();
	                    	aux=aux.substring(aux.indexOf("/")+1);
	                    	if(aux.equals(field.getName()))
	                    		hashAux2.put(field.getName(), field.getValues());
	                    	else
	                    	{
	                    		hashAux2.put(aux, field.getValues());
	                    	}
	                    }
	                }
	            }
				data.put(key, hashAux2);
			}
			
		}
		return data;
	}
	
	public String uploadFile(FileItem fi, String fieldType)
	{
		  String type="";
		  String value="";
          if(fieldType.indexOf("Attachment")!= -1) //If it is an attachment field type 
          {
          	type = "plain"; //Set type
          }
          else //If it is an image field type 
          {
          	type = "image"; //Set type
          }
          int t;

          try 
          {
			  t = OpenCms.getResourceManager().getResourceType(type).getTypeId();
				//Get type id
              String path="/system/modules/org.tsol.module.util/";
              path = path + System.currentTimeMillis() + "_" + fi.getName().replaceAll(" ", "_");
              cmsObject.createResource(path, t, fi.get(), null); //Create the resource
              /*Create the resource and set the content*/
              cmsObject.unlockResource(path);
              /*
               * CmsObject.publishResource it's deprecated for OpenCms 7 used this instead
               * OpenCms.getPublishManager().publishResource(cmsObject, resource);
               */
             // cmsObject.publishResource(path);
              OpenCms.getPublishManager().publishResource(cmsObject, path);
              value = path;
          }
          catch (CmsLoaderException e)
		  {
				LOG.error(e);
				e.printStackTrace();
		  }
          catch (CmsIllegalArgumentException e) 
          {
				LOG.error(e);
				e.printStackTrace();
		  } 
          catch (CmsException e) 
          {
				LOG.error(e);
				e.printStackTrace();
		  } 
          catch (Exception e) 
          {
				LOG.error(e);
				e.printStackTrace();
		  }
          return value;
	}
	
	public String getMillis(String fecha)
	{
		String [] fechaArray =fecha.split(" ");
		String [] diamesano = ((String)fechaArray[0]).split("/");
		String [] hora =  ((String)fechaArray[1]).split(":");
		GregorianCalendar gc = new GregorianCalendar(new Date(System.currentTimeMillis()).getYear()+1900, Integer.parseInt(diamesano[1])-1, Integer.parseInt(diamesano[0]), Integer.parseInt(hora[0]), Integer.parseInt(hora[1]), 0); 
		return gc.getTimeInMillis() +"";
	}
}
