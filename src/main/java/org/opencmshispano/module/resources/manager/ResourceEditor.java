package org.opencmshispano.module.resources.manager;

/*Java util imports*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/*Apache commons imports*/
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;

/*OpenCms imports*/
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;

import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.main.CmsLog;

import org.opencmshispano.module.util.Schemas;

/**
 *
 * @author Alejandro Alves Calderón
 * @version 1.0
 *
 */

public class ResourceEditor
{

			/*Class attributes*/
			private CmsJspActionElement cms;
			private CmsObject cmsObject;
			//HashMap attachmentMap;

			private static final Log LOG = CmsLog.getLog(ResourceEditor.class);
			/**
			 *@param CmsJspActionElement
			 *
			 */
			public ResourceEditor(CmsJspActionElement cms)
			{
				this.cms = cms;
				this.cmsObject = cms.getCmsObject();
			}
			
			public ResourceEditor(CmsJspActionElement cms, CmsObject cmsObjectAdmin)
			{
				this.cms = cms;
				this.cmsObject = cmsObjectAdmin;
			}

			public  boolean editSingleField(String resource, String path, int index, Locale locale, String value)
		    {
				boolean resultado = true;
				try {
					CmsFile cmsFile = cmsObject.readFile(resource);
					CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);
					I_CmsXmlContentValue contentValue = null;

					if(content.hasValue(path, locale,index))
					{
						contentValue = content.getValue(path, locale, index);
						contentValue.setStringValue(cmsObject, value);
					}
					else
					{
						contentValue = content.addValue(cmsObject, path, locale, index);
						contentValue.setStringValue(cmsObject, value);
					}

				} catch (CmsException e) {
					LOG.error("Error recuperando el campo "+path+" del recurso "+resource+". Mensaje: "+e.getMessage());
					e.printStackTrace();
					resultado=false;
				}
				return resultado;
		    }


			public  boolean addSingleField(String resource, String path, Locale locale, String value)
		    {
				boolean resultado = true;
				try {
					CmsFile cmsFile = cmsObject.readFile(resource);
					CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);
					I_CmsXmlContentValue contentValue = null;
					I_CmsXmlContentValue contentNewValue = null;

					if(content.hasValue(path, locale))
					{
						contentValue = content.getValue(path, locale);
						int numElementos = contentValue.getMaxIndex();
						contentNewValue = content.addValue(cmsObject, path, locale, numElementos);
						contentNewValue.setStringValue(cmsObject, value);
					}
					else
					{
						contentValue = content.addValue(cmsObject, path, locale,0);
						contentValue.setStringValue(cmsObject, value);
					}
					content.getContentDefinition().getTypeName();
					ResourceManager rm = new ResourceManager(cms);
					rm.editResource(resource, content);

				} catch (CmsException e) {
					LOG.error("Error recuperando el campo "+path+" del recurso "+resource+". Mensaje: "+e.getMessage());
					e.printStackTrace();
					resultado=false;
				}
				return resultado;
		    }

			public boolean editMultiField(String resource, String path, Locale locale, List valores)
			{
				boolean resultado = true;
				try {
					CmsFile cmsFile = cmsObject.readFile(resource);
					CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);
					I_CmsXmlContentValue contentValue = null;
					I_CmsXmlContentValue contentNewValue = null;

					if(content.hasValue(path, locale))
					{
						//Si existen elementos los borramos previamente.
						contentValue = content.getValue(path, locale);
						int numElementos = contentValue.getMaxIndex();
						for (int i=numElementos-1;i>0;i--)
						{
							content.removeValue(path, locale, i);
						}
					}

					ResourceManager rm = new ResourceManager(cms);
					rm.manageMultipleContent(valores, path, locale, content);
					rm.editResource(resource, content);


				} catch (CmsException e) {
					LOG.error("Error recuperando el campo "+path+" del recurso "+resource+". Mensaje: "+e.getMessage());
					e.printStackTrace();
					resultado=false;
				}
				return resultado;
			}
}
