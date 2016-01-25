package org.opencmshispano.module.util;


import java.util.Locale;
import java.util.ResourceBundle;
 
public class Bundle
{
	
   private ResourceBundle bundle; 
	
   public Bundle(String resourceName, Locale locale)
   {
	   bundle = ResourceBundle.getBundle(resourceName, locale);
   }
   
   public Bundle(String resourceName)
   {
	   bundle = ResourceBundle.getBundle(resourceName);
   }
   
   public String getString(String key)
   {
	  return bundle.getString(key);  
   }
  
}