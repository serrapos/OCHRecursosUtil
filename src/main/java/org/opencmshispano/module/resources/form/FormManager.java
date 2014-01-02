package org.opencmshispano.module.resources.form;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;
import org.opencmshispano.module.resources.commons.Field;

public class FormManager
{
	
	private static final Log LOG = CmsLog.getLog(FormManager.class);
	
	public static String getHtmlSimpleField(Field field, String defaultValue, String maxLength,  boolean multiple, String size, String rows, String cols, String styleClass, HashMap options)
	{
		String type = field.getType();
		String name = field.getName();
		String html2 = "<div id=\"read"+name+"\" style=\"display: none\">";
		String html = "<div class=\""+styleClass+"\" name=\""+name+"\">";
		html += field.getName() + generateHtmlInput(type, field.getPath().replace("/", "_"), defaultValue, maxLength, multiple, size, rows, cols, styleClass, options, field.getMaxOccurs());	
        html2 += field.getName() + generateHtmlInput(type, field.getPath().replace("/", "_"), defaultValue, maxLength, multiple, size, rows, cols, styleClass, options, field.getMaxOccurs());
		int indice = html2.lastIndexOf("<span");
		if(indice!=-1)
			html2 = html2.substring(0, indice);
        html += "</div>";
		html2 += "</div>";
		if(field.getMaxOccurs()>1)
		{
             html= html2 + html + "<input type=\"button\" value=\"añadir campo\" id=\"anadir"+name+"\" name=\"anadir"+name+"\" onclick=\"moreFields('read"+name+"', 'write"+name+"', "+field.getMaxOccurs()+",'count_"+name+"')\" />";
		}
        return html  + "<input type=\"hidden\" name=\"count_"+name+"\" id=\"count_"+name+"\" value=\"1\">";
	}
	
	public static String getHtmlNestedField(HashMap hm, String defaultValue, String maxLength,  boolean multiple, String size, String rows, String cols, String styleClass, HashMap options)
	{
         Iterator itKeys = hm.keySet().iterator();
         String html = "";
         String out = "";
         while(itKeys.hasNext())
         {
              Field field = (Field)itKeys.next();
              String name = field.getName();
              ArrayList list = (ArrayList)hm.get(field);
              Iterator itList = list.iterator();
           	  String html2 = "<div id=\"read"+name+"\" style=\"display: none\">";
              html += name+"</br>";
              html2 += name+"</br>";
              while(itList.hasNext())
              {
                  Field campo = (Field)itList.next();
                  if(campo.isSimple())
                  {
                         String tipo = campo.getType();
                         html2 += campo.getName() + generateHtmlInput(tipo, campo.getPath().replace("/", "_"),  null, null,  false, null, null, null, null, new HashMap(), campo.getMaxOccurs());
                         html += campo.getName() + generateHtmlInput(tipo, campo.getPath().replace("/", "_")+"_1",  null, null,  false, null, null, null, null, new HashMap(), campo.getMaxOccurs());
                  }
              }
              String html3 ="";
              if(field.getMaxOccurs()>1)
              {
            	  html3 += "<span id=\"write"+name+"\"></span>";
            	  html3 += "<input type=\"hidden\" name=\"count_"+name+"\" id=\"count_"+name+"\"  value=\"1\"><input type=\"button\" value=\"añadir campo\" id=\"anadir"+name+"\" name=\"anadir"+name+"\" onclick=\"moreFields('read"+name+"', 'write"+name+"', "+field.getMaxOccurs()+", 'count_"+name+"')\" />";
              }
              out = html2 + "</div>" + html + html3 +"<br>" ;
           }
         return out;
	}
	
	public static String generateHtmlInput(String type, String name,  String defaultValue, String maxLength,  boolean multiple, String size, String rows, String cols, String styleClass, HashMap options, int maxOccurs)
	{
		String html="";
		if(type.equals("text"))
			html=generateText(name,  defaultValue, maxLength, size, styleClass, maxOccurs );
		if(type.equals("date"))
			html=generateDate(name,  defaultValue, maxLength, size, styleClass, maxOccurs );
		else if(type.equals("textarea"))
			html=generateTextArea(name, defaultValue, rows, cols, styleClass, maxOccurs);
		else if(type.equals("select"))
			html=generateSelect(name, size, multiple, styleClass, options, maxOccurs);
		else if(type.equals("file"))
			html=generateFile(name,maxLength, size, styleClass, maxOccurs );
		return html;
	}
	
	public static String generateDate(String name,  String defaultValue, String maxLength, String size, String styleClass, int maxOccurs )
	{
		String nameHtml="";
		String defaultHtml="";
		String maxHtml="";
		String sizeHtml="";
		String iniStyleHtml="";
		String endStyleHtml="";
		String write = "";
		String button="<button type=\"reset\" id=\"f_trigger_"+name+"\">...</button>";
		String js= "<script type=\"text/javascript\">" +
		 "Calendar.setup({"+
		        "inputField     :    \"f_date_"+name+"\", "+    
		        "ifFormat       :    \"%d/%m/%Y %I:%M %p\", "+     
		        "showsTime      :    true,            "+
		        "button         :     \"f_trigger_"+name+"\","+  
		        "singleClick    :    false,           "+
		        "step           :    1                "+
		    "});"+
	    "</script>";
	   
		if(name!=null && !name.equals(""))
			nameHtml=" name=\""+name+"\"" + " id=\"f_date_"+name+"\"";
		if(defaultValue!=null && !defaultHtml.equals(""))
			defaultHtml="value=\""+defaultValue+"\"";
		if(maxLength!=null && !maxLength.equals(""))
			maxHtml=" maxlength=\""+maxLength+"\"";
		if(size!=null && !size.equals(""))
			sizeHtml=" size=\""+size+"\"";
		if(maxOccurs>1)
			write = "<span id=\"write"+name+"\"></span>";
		return iniStyleHtml + "<input type=\"text\" readonly=\"1\""+ nameHtml + defaultHtml + maxHtml + sizeHtml + "/>" + button + endStyleHtml + write + js;
	}
	
	public static String generateText(String name,  String defaultValue, String maxLength, String size, String styleClass, int maxOccurs )
	{
		String nameHtml="";
		String defaultHtml="";
		String maxHtml="";
		String sizeHtml="";
		String iniStyleHtml="";
		String endStyleHtml="";
		String write = "";
		if(name!=null && !name.equals(""))
			nameHtml=" name=\""+name+"\"" + " id=\""+name+"\"";
		if(defaultValue!=null && !defaultHtml.equals(""))
			defaultHtml="value=\""+defaultValue+"\"";
		if(maxLength!=null && !maxLength.equals(""))
			maxHtml=" maxlength=\""+maxLength+"\"";
		if(size!=null && !size.equals(""))
			sizeHtml=" size=\""+size+"\"";
		if(maxOccurs>1)
			write = "<span id=\"write"+name+"\"></span>";
		return iniStyleHtml + "<input type=\"text\""+ nameHtml + defaultHtml + maxHtml + sizeHtml + "/>" + endStyleHtml + write;
	}
	
	public static String generateTextArea(String name, String defaultValue, String rows, String cols, String styleClass, int maxOccurs)
	{
		String nameHtml="";
		String rowsHtml="";
		String defaultHtml="";
		String colsHtml="";;
		String iniStyleHtml="";
		String endStyleHtml="";
		String write = "";
		if(name!=null && !name.equals(""))
			nameHtml=" name=\""+name+"\"" + " id=\""+name+"\"";
		if(defaultValue!=null)
			defaultHtml="";
		if(rows!=null && !rows.equals(""))
			rowsHtml=" rows=\""+rows+"\"";
		if(cols!=null && !cols.equals(""))
			colsHtml=" cols=\""+cols+"\"";
		if(maxOccurs>1)
			write = "<span id=\"write"+name+"\"></span>";
		return iniStyleHtml+"<textarea " + nameHtml + rowsHtml + colsHtml + ">"+defaultHtml+"</textarea>"+endStyleHtml + write;		
	}
	
	public static String generateSelect(String name, String size, boolean multiple, String styleClass, HashMap options, int maxOccurs)
	{
		String nameHtml="";
		String sizeHtml="";
		String multipleHtml="";
		String optionsHtml="";
		String iniStyleHtml="";
		String endStyleHtml="";
		String write = "";
		if(name!=null && !name.equals(""))
			nameHtml=" name=\""+name+"\"" + " id=\""+name+"\"";
		if(size!=null && !size.equals(""))
			sizeHtml=" size=\""+size+"\"";
		if(multiple)
			multipleHtml=" MULTIPLE";
		Iterator itKeys = options.keySet().iterator();	
		while(itKeys.hasNext())
		{
			String key = (String)itKeys.next();
			String value = (String) options.get(key);
			optionsHtml+="<option value=\""+value+"\">"+key+"</option>";
		}
		if(maxOccurs>1)
			write = "<span id=\"write"+name+"\"></span>";
		return iniStyleHtml + "<select " + nameHtml + sizeHtml + multipleHtml +">" + optionsHtml + "</select>" + endStyleHtml + write;		
	}
	
	public static String generateFile(String name, String maxLength, String size, String styleClass, int maxOccurs )
	{
		String nameHtml="";
		String defaultHtml="";
		String maxHtml="";
		String sizeHtml="";
		String iniStyleHtml="";
		String endStyleHtml="";
		String write = "";
		if(name!=null && !name.equals(""))
			nameHtml=" name=\""+name+"\"" + " id=\""+name+"\"";
		if(maxLength!=null && !maxLength.equals(""))
			maxHtml=" maxlength=\""+maxLength+"\"";
		if(size!=null && !size.equals(""))
			sizeHtml=" size=\""+size+"\"";
		if(maxOccurs>1)
			write = "<span id=\"write"+name+"\"></span>";
		return iniStyleHtml + "<input type=\"file\""+ nameHtml + defaultHtml + maxHtml + sizeHtml + "/>" + endStyleHtml + write ;
	}
	
}
