package org.opencmshispano.module.resources.manager;

/*Java util imports*/

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.dom4j.Element;
import org.opencms.file.*;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.*;
import org.opencms.util.CmsRequestUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencmshispano.module.resources.bean.Choice;
import org.opencmshispano.module.util.Schemas;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.*;


/**
 * Revision 1.1: Añadido constructor para poder enviar un CmsObject distinto al del CmsJspActionElement para poder
 * disponer de permisos especiales de administracion.
 * @author OpenCms Hispano : Alejandro Alves Calderon | Sergio Raposo Vargas 
 * @version 1.1
 *
 */

public class ResourceManager
{

			/*Class attributes*/
			private CmsJspActionElement cms;
			private CmsObject cmsObject;
			
			//HashMap attachmentMap;

			private static final Log LOG = CmsLog.getLog(ResourceManager.class);
			/**
			 *@param CmsJspActionElement
			 *
			 */
			public ResourceManager(CmsJspActionElement cms)
			{
				this.cms = cms;
				this.cmsObject = cms.getCmsObject();
			}
			
			public ResourceManager(CmsObject cmsObjectAdmin)
			{
				this.cms = null;
				this.cmsObject = cmsObjectAdmin;
			}
			
			public ResourceManager(CmsJspActionElement cms, CmsObject cmsObjectAdmin)
			{
				this.cms = cms;
				this.cmsObject = cmsObjectAdmin;
			}
			
			/**
			 * Sube una imagen a OpenCms a la carpeta indicada
			 * @param image
			 * @param title
			 * @param vfsPath
			 * @param publish
			 * @return
			 */
			public boolean uploadImage(byte[] image, String title, String vfsPath, boolean publish)
			{
				boolean res = false;
				try{
					//Comprobamos primero si existe ya un fichero igual, si no existe lo creamos y si existe lo sobre escribimos
					if(!cmsObject.existsResource(vfsPath,CmsResourceFilter.ALL))
					{
						//Creamos el recurso
						int typeId = OpenCms.getResourceManager().getResourceType("image").getTypeId();
						List<CmsProperty> propiedades = new ArrayList<CmsProperty>();
						propiedades.add(new CmsProperty("Title",title,title));
						cmsObject.createResource(vfsPath, typeId, image, propiedades);
					}else{
						//Si existe, leemos el recurso y lo modificamos
						CmsFile r = cmsObject.readFile(vfsPath);
						r.setContents(image);
						cmsObject.writeFile(r);
					}
					
					//Si hay que publicar, lo publicamos
					if(publish)
					{
						cmsObject.unlockResource(vfsPath);
						OpenCms.getPublishManager().publishResource(cmsObject, vfsPath);
					}
					res = true;
				}catch(CmsException ex)
				{
					ex.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return res;
			}
			
			/**
			 * Sube un fichero binario a OpenCms a la carpeta indicada
			 * @param image
			 * @param title
			 * @param vfsPath
			 * @param publish
			 * @return
			 */
			public boolean uploadBinary(byte[] image, String title, String vfsPath, boolean publish)
			{
				boolean res = false;
				try{
					//Comprobamos primero si existe ya un fichero igual, si no existe lo creamos y si existe lo sobre escribimos
					if(!cmsObject.existsResource(vfsPath, CmsResourceFilter.ALL))
					{
						//Creamos el recurso
						int typeId = OpenCms.getResourceManager().getResourceType("binary").getTypeId();
						List<CmsProperty> propiedades = new ArrayList<CmsProperty>();
						propiedades.add(new CmsProperty("Title",title,title));
						cmsObject.createResource(vfsPath, typeId, image, propiedades);
					}else{
						//Si existe, leemos el recurso y lo modificamos
						CmsFile r = cmsObject.readFile(vfsPath);
						r.setContents(image);
						cmsObject.writeFile(r);
					}
					
					//Si hay que publicar, lo publicamos
					if(publish)
					{
						cmsObject.unlockResource(vfsPath);
						OpenCms.getPublishManager().publishResource(cmsObject, vfsPath);
					}
					res = true;
				}catch(CmsException ex)
				{
					ex.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return res;
			}

			/**
			 * This method uploads all the files and sets a hashmap to link uploads to the resource
			 * @param path - path to the folder in which the uploads will be placed.
			 *
			 */
			public HashMap uploadFile(String path)
			{

				/*Get request*/
				HttpServletRequest request = cms.getRequest();
				/*Parses a request of the form multipart/form-data.*/
				List files = CmsRequestUtil.readMultipartFileItems(request);
		        HashMap attachmentMap = null;
		        boolean change = false;
		        CmsProject project = cms.getRequestContext().currentProject();
		        try
				{
	                /*If any file was uploaded*/
			        if(files!=null)
					{

			        	 if(project.getName().equals("Online"))
							{
								  cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
								  change = true;
							}
			        	    attachmentMap = new HashMap();
					        Iterator itFi = files.iterator();

					        ArrayList pathsAttachment = new ArrayList();
					        ArrayList pathsImage = new ArrayList();
					        /*Go through the list*/
					        while (itFi.hasNext())
					        {

					             FileItem fi = (FileItem)itFi.next();
					             String fieldType = fi.getFieldName(); //Field type
					             String type="";
					             if(fieldType.indexOf("Attachment")!= -1) //If it is an attachment field type
					             {
					            	 pathsAttachment.add(path + fi.getName().replaceAll(" ", "_")); //Replace spaces for "_"
					            	 type = "plain"; //Set type
					             }
					             else //If it is an image field type
					             {
					            	 pathsImage.add(path + fi.getName().replaceAll(" ", ""));//Replace spaces for "_"
					            	 type = "image"; //Set type
					             }

					            int t;

							    t = OpenCms.getResourceManager().getResourceType(type).getTypeId(); //Get type id
								cmsObject.createResource(path, t, fi.get(), null); //Create the resource
								 /*Create the resourcd and set the content*/
					            cmsObject.unlockResource(path);
						        /*
						         * CmsObject.publishResource it's deprecated for OpenCms 7 used this instead
						         * OpenCms.getPublishManager().publishResource(cmsObject, resource);
						         */
						        //cmsObject.publishResource(path);
					            OpenCms.getPublishManager().publishResource(cmsObject, path);

					        }
				            if(change)
				            {
				           	  cmsObject.getRequestContext().setCurrentProject(project);
				            }
					        if(pathsAttachment.size()>0)
					        	attachmentMap.put("Attachment", pathsAttachment);
					        if(pathsImage.size()>0)
					        	attachmentMap.put("Image", pathsImage);
					}
				}
				catch (CmsLoaderException e)
				{
					LOG.error(e.toString());
					e.printStackTrace();
					attachmentMap=null;
				}
	            catch (CmsIllegalArgumentException e)
	            {
					LOG.error(e.toString());
					e.printStackTrace();
					attachmentMap=null;
				}
	            catch (CmsException e)
	            {
					LOG.error(e.toString());
					e.printStackTrace();
					attachmentMap=null;
				}
	            catch (Exception e)
	            {
					LOG.error(e.toString());
					e.printStackTrace();
				}

	            return attachmentMap;
			}

			/**
			 * Copia un recurso de una ruta a otra
			 * @param fuente
			 * @param destino
			 * @return
			 */
			public boolean copyResource(String fuente, String destino)
			{
				/* Check if the resource exists*/
				  boolean existsDestino = cmsObject.existsResource(destino,CmsResourceFilter.ALL);
				  boolean existsFuente = cmsObject.existsResource(fuente,CmsResourceFilter.ALL);
				  boolean change = false;

				  boolean resultado = false;

				  try
				  {
					  /*
					   * Get the current project and check if the current project is the online project, if it is change to the offline project
					   * since one can only create resources on the offline project.
					   */
					  CmsProject project = cmsObject.getRequestContext().currentProject();
					  if(project.getName().equals("Online"))
					  {
						  cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
						  change = true;
					  }
		              if(existsFuente && !existsDestino) //Si existe el fuente pero no el destino
		              {
		            	cmsObject.lockResource(fuente);
		  				cmsObject.copyResource(fuente, destino);
		  				cmsObject.unlockResource(fuente);
		  				cmsObject.unlockResource(destino);
			            //Publicamos el destino
		                OpenCms.getPublishManager().publishResource(cmsObject, destino);
		                resultado = true;
		              }
		              else{
		            	  LOG.error("Error al copiar un recurso, o no existe la fuente o ya existe el recurso de destino.");
		              }

		              if(change)
		              {
		            	  cmsObject.getRequestContext().setCurrentProject(project);
		              }
				  }
				  catch(Exception e)
				  {
					  e.printStackTrace();
					  LOG.error("Error al copiar un recurso"+e.toString());
				  }

	              return resultado;
			}

			/**
			 * Crea un sibling de un recurso
			 * @param fuente
			 * @param destino
			 * @return
			 */
			public boolean createSibling(String fuente, String destino)
			{
				/* Check if the resource exists*/
				  boolean existsDestino = cmsObject.existsResource(destino,CmsResourceFilter.ALL);
				  boolean existsFuente = cmsObject.existsResource(fuente,CmsResourceFilter.ALL);
				  boolean success = true;
				  boolean change = false;

				  boolean resultado = false;

				  try
				  {
					  /*
					   * Get the current project and check if the current project is the online project, if it is change to the offline project
					   * since one can only create resources on the offline project.
					   */
					  CmsProject project = cmsObject.getRequestContext().currentProject();
					  if(project.getName().equals("Online"))
					  {
						  cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
						  change = true;
					  }
		              if(existsFuente && !existsDestino) //Si existe el fuente pero no el destino
		              {
		            	  	cmsObject.lockResource(fuente);
			            	cmsObject.createSibling(fuente, destino, null);
			  				cmsObject.unlockResource(fuente);
				            //Publicamos el destino
			                OpenCms.getPublishManager().publishResource(cmsObject, destino);
			                resultado = true;
		              }

		              if(change)
		              {
		            	  cmsObject.getRequestContext().setCurrentProject(project);
		              }
				  }
				  catch(Exception e)
				  {
					  e.printStackTrace();
					  LOG.error(e.toString());
				  }

	              return resultado;
			}

			/**
			 * Deprecado en version 1.2. Alternativa: saveCmsResource
			 * This method creates a new resource or edits an existing one, and sets it's content according to the info passed by the HashMap data.
			 * @param data - Data associated to the resource's content
			 * @param resource - Resources path+name
			 * @param type - The resource's type
			 */
			@Deprecated 			
			public boolean saveResource (HashMap data, String resource, int type, boolean publish)
			{
				if(saveCmsResource (data, resource, type, publish, null) == null)
					return false;
				else
					return true;
			}
			
			/**
			 * Deprecado en version 1.2. Alternativa: saveCmsResource
			 * This method creates a new resource or edits an existing one, and sets it's content according to the info passed by the HashMap data.
			 * @param data - Data associated to the resource's content
			 * @param resource - Resources path+name
			 * @param type - The resource's type
			 */
			@Deprecated 
			public boolean saveResource (HashMap data, String resource, int type, boolean publish, String customLocale)
			{
				if(saveCmsResource (data, resource, type, publish, customLocale) == null)
					return false;
				else
					return true;
			}
			
			public CmsResource saveCmsResource (HashMap data, String resource, String resourceTypeName, boolean publish){
				return saveCmsResource (data, resource, resourceTypeName, publish, null);
			}
			
			public CmsResource saveCmsResource (HashMap data, String resource, String resourceTypeName){
				return saveCmsResource (data, resource, resourceTypeName, false, null);
			}
			
			public CmsResource saveCmsResource (HashMap data, String resource, int type, boolean publish, String customLocale){
				try {
					I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(type);
					return saveCmsResource (data, resource, resType.getTypeName(), publish, customLocale);
				} catch (CmsLoaderException e) {
					e.printStackTrace();
				}
				return null;
			}

			/**
			 * This method creates a new resource or edits an existing one, and sets it's content according to the info passed by the HashMap data.
			 * @param data - Data associated to the resource's content
			 * @param resource - Resources path+name
			 * @param type - The resource's type
			 */
			public CmsResource saveCmsResource (HashMap data, String resource, String type, boolean publish, String customLocale)
			{
				boolean success=true;
				CmsResource cmsResource=null;

				try
		        {
					
					 boolean exists = cmsObject.existsResource(resource,CmsResourceFilter.ALL);
					 CmsXmlContent content = null;
					 /*Get the locale*/
					 Locale localizacion = cmsObject.getRequestContext().getLocale();
					 if(customLocale!=null)
						 localizacion = new Locale(customLocale);	
					 
					 //Comprobamos si existe para crear un content nuevo o leer el recurso actual
					 if(exists){
						 /*Create the XmlContent associated to the new resource to access and manage the structured content */
						 CmsFile cmsFile = cmsObject.readFile(resource);
						 cmsResource = (CmsResource)cmsFile;
						 content = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);
						 List<Locale> localesActuales = content.getLocales();
						 if(!localesActuales.contains(localizacion)){
							 //Si no tenemos el locale solicitado creado, copiamos de uno existente
							 content.copyLocale(localesActuales.get(0), localizacion);
						 }
						 
					 }else{
						 /*get the schema*/
						 String schema = Schemas.getSchemaByType(type);					  
						 /*Create the XmlContent associated to the new resource to access and manage the structured content */	
						 CmsXmlContentDefinition def = CmsXmlContentDefinition.unmarshal(schema,new CmsXmlEntityResolver(cmsObject));
						 content = CmsXmlContentFactory.createDocument(cmsObject, localizacion, CmsEncoder.ENCODING_UTF_8,def);
					 }
					 

		              /*Go through the MAP's list with all the data.*/
		              Set keys = data.keySet();
		              Iterator itKeys = keys.iterator();

					  //Creamos una variable para comprobar si realmente se ha editado el recurso o no
					  boolean modified = false;

		              while (itKeys.hasNext())
		              {
		            	  //The key is a field's name
		                  String key = (String)itKeys.next();
		                  //Value is the fild's value.
		                  Object value = data.get(key);

		                  /*
		                   * Depending on the object's type an action is carried out:
		                   * ArrayList = Meaning that there is more than one element of the same field.
		                   * HashMap = Nested content
		                   * String = Simple content
		                   */

			              if (value instanceof ArrayList) {
				              modified = manageMultipleContent((ArrayList)value, key, localizacion,  content) || modified;
		                  } else if (value instanceof HashMap) {
							  modified = manageNestedContent((HashMap) value, key, localizacion, content) || modified;
				          } else if (value instanceof Choice) {
							  modified = manageChoiceContent(((Choice)value).getSubfields(), key, localizacion, content) || modified;
				          } else {
							  modified = manageSimpleContent(key, (String)value, localizacion, content) || modified;
				          }
		              }

		              if(modified) {
						  cmsResource = createOrEditResource(resource, type, content, publish);
						  if (cmsResource == null)
							  success = false;
						  else
							  success = true;
					  }else{
						  success = true;
					  }
		          }
		          catch(Exception exc)
		          {
		              exc.printStackTrace();
		              LOG.error(exc.toString());
		              success=false;
		          }
		          return cmsResource;
			}
			
			/**
			 * This method creates a new resource or edits an existing one, and sets it's content according to the info passed by the HashMap data.
			 * @param data - Data associated to the resource's content
			 * @param resource - Resources path+name
			 * @param type - The resource's type
			 */
			private CmsXmlContent generaXmlContent (HashMap data, CmsXmlContent content, Locale localizacion)
			{
				try{
					Element el = content.getLocaleNode(localizacion);
				}catch(CmsRuntimeException ex)
				{
					try {
						content.addLocale(cmsObject, localizacion);
					} catch (CmsXmlException e) {
						e.printStackTrace();
						return null;
					}
				}
				
				try
		        {
					
		              /*Go through the MAP's list with all the data.*/
		              Set keys = data.keySet();
		              Iterator itKeys = keys.iterator();

		              while (itKeys.hasNext())
		              {
		            	  //The key is a field's name
		                  String key = (String)itKeys.next();
		                  //Value is the fild's value.
		                  Object value = data.get(key);

		                  /*
		                   * Depending on the object's type an action is carried out:
		                   * ArrayList = Meaning that there is more than one element of the same field.
		                   * HashMap = Nested content
		                   * String = Simple content
		                   */

			              if (value instanceof ArrayList)
		                  {
				              manageMultipleContent((ArrayList)value, key, localizacion,  content);
		                  }
		                  else if (value instanceof HashMap)
		                  {
				              manageNestedContent((HashMap) value, key, localizacion, content);
				          }
		                  else
		                  {
				              manageSimpleContent(key, (String)value, localizacion, content);
				          }
		              }

		          }
		          catch(Exception exc)
		          {
		              exc.printStackTrace();
		              LOG.error(exc.toString());
		          }
		          return content;
			}

			/**
			 * Deprecado en version 1.2: usar saveCmsResource
			 * This method edits any fields a resource existing one, and sets it's content according to the info passed by the HashMap data.
			 * @param data - Data associated to the resource's content
			 * @param resource - Resources path+name
			 * @param type - The resource's type
			 */
			@Deprecated
			public boolean editResource (HashMap data, String resource, int type, boolean publish)
			{
				CmsResource cmsResource=null;
				boolean success=true;

				try
		        {
					  /*get the schema*/
					  //String schema = Schemas.getSchemaByType(type);

		              /*Create the XmlContent associated to the new resource to access and manage the structured content */
					  CmsFile cmsFile = cmsObject.readFile(resource);
					  CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);

		              /*Get the locale*/
		              Locale localizacion = cmsObject.getRequestContext().getLocale();

		              /*Go through the MAP's list with all the data.*/
		              Set keys = data.keySet();
		              Iterator itKeys = keys.iterator();

		              while (itKeys.hasNext())
		              {
		            	  //The key is a field's name
		                  String key = (String)itKeys.next();
		                  //Value is the fild's value.
		                  Object value = data.get(key);

		                  /*
		                   * Depending on the object's type an action is carried out:
		                   * ArrayList = Meaning that there is more than one element of the same field.
		                   * HashMap = Nested content
		                   * String = Simple content
		                   */

			              if (value instanceof ArrayList)
		                  {
				              manageMultipleContent((ArrayList)value, key, localizacion,  content);
		                  }
		                  else if (value instanceof HashMap)
		                  {
				              manageNestedContent((HashMap) value, key, localizacion, content);
				          }
		                  else
		                  {
				              manageSimpleContent(key, (String)value, localizacion, content);
				          }
		              }

		              /*If everything went well, the resource will be edited or created*/
		              try {
							I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(type);
							cmsResource = createOrEditResource(resource, resType.getTypeName(), content, publish);
						} catch (CmsLoaderException e) {
							e.printStackTrace();
						}
		              
		              if(cmsResource == null)
		            	  success = false;
		              else
		            	  success = true;

		          }
		          catch(Exception exc)
		          {
		              exc.printStackTrace();
		              LOG.error(exc.toString());
		              success=false;
		          }
		          return success;
			}

			/**
			 * Deprecado en version 1.2: usar saveCmsResource
			 * This method edits any fields a resource existing one, and sets it's content according to the info passed by the HashMap data.
			 * @param data - Data associated to the resource's content
			 * @param resource - Resources path+name
			 * @param type - The resource's type
			 */
			@Deprecated
			public boolean editResource (HashMap data, String resource)
			{
				return editResource(data, resource, true);
			}
			
			/**
			 * Deprecado en version 1.2: usar saveCmsResource
			 * @param data
			 * @param resource
			 * @param publish
			 * @return
			 */
			@Deprecated
			public boolean editResource (HashMap data, String resource, boolean publish)
			{
				CmsResource cmsResource=null;
				boolean success=true;

				try
		        {
					  /*get the schema*/
					  //String schema = Schemas.getSchemaByType(type);

		              /*Create the XmlContent associated to the new resource to access and manage the structured content */
					  CmsFile cmsFile = cmsObject.readFile(resource);
					  CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);

		              /*Get the locale*/
		              Locale localizacion = cmsObject.getRequestContext().getLocale();

		              /*Go through the MAP's list with all the data.*/
		              Set keys = data.keySet();
		              Iterator itKeys = keys.iterator();

		              while (itKeys.hasNext())
		              {
		            	  //The key is a field's name
		                  String key = (String)itKeys.next();
		                  //Value is the fild's value.
		                  Object value = data.get(key);

		                  /*
		                   * Depending on the object's type an action is carried out:
		                   * ArrayList = Meaning that there is more than one element of the same field.
		                   * HashMap = Nested content
		                   * String = Simple content
		                   */

			              if (value instanceof ArrayList)
		                  {
				              manageMultipleContent((ArrayList)value, key, localizacion,  content);
		                  }
		                  else if (value instanceof HashMap)
		                  {
				              manageNestedContent((HashMap) value, key, localizacion, content);
				          }
		                  else
		                  {
				              manageSimpleContent(key, (String)value, localizacion, content);
				          }
		              }

		              /*If everything went well, the resource will be edited or created*/
		              cmsResource = editResource(resource, content, publish);
		              if(cmsResource == null)
		            	  success = false;
		              else
		            	  success = true;

		          }
		          catch(Exception exc)
		          {
		              exc.printStackTrace();
		              LOG.error(exc.toString());
		              success=false;
		          }
		          return success;
			}
			
			/**
			 * Añade una categoria a un recurso
			 * @param resource
			 * @param category
			 * @param fieldCategory
			 * @param localeStr
			 * @param publish
			 * @return
			 * @throws Exception
			 */
			public boolean addCategory(String resource, String category, String fieldCategory, String localeStr, Boolean publish) throws Exception{
				CmsResource cmsResource=null;
				boolean success=true;
				boolean change = false;

				//Chequeamos que los campos son correctos
				if(fieldCategory!=null && category!=null){
					
				  /*
				   * Get the current project and check if the current project is the online project, if it is change to the offline project
				   * since one can only create resources on the offline project.
				   */
				  CmsProject project = cmsObject.getRequestContext().getCurrentProject();
				  if(project.getName().equals("Online"))
				  {
					  cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
					  change = true;
				  }

					
		              /*Create the XmlContent associated to the new resource to access and manage the structured content */
					  CmsFile cmsFile = cmsObject.readFile(resource);
					  CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);
	
		              /*Get the locale*/
					  Locale localizacion = cmsObject.getRequestContext().getLocale();
					  if(localeStr!=null)
						  localizacion = new Locale(localeStr);
	
					  I_CmsXmlContentValue contentValue = null;
	
					  /*Checks if the content at the declared index exists*/
					  if(content.hasValue(fieldCategory, localizacion, 0) && category !=null)
					  {
						/*If the content exists, get the value and set it*/
						contentValue = content.getValue(fieldCategory, localizacion, 0);
						String categoryValue = contentValue.getStringValue(cmsObject);
						if(categoryValue!=null && categoryValue.length()>0)
							categoryValue = categoryValue + ","+category;
						else
							categoryValue = category;
						
	              		contentValue.setStringValue(cmsObject, categoryValue);
					  }					 
	              	  else 
	              	  {
	              		/*If the content does not exist, add it to the xml and set it */
	              	    contentValue = content.addValue(cmsObject, fieldCategory, localizacion, 0);
	              		contentValue.setStringValue(cmsObject, category);
	              	  }
	
					  //Bloqueamos el recurso
		              cmsObject.lockResource(resource);
		            	
		              //Modificamos el contenido
		              byte[] byteContent= content.marshal();
		              cmsFile.setContents(byteContent);
		              cmsObject.writeFile(cmsFile);
		                
		              /*Ejecutamos mappings y otras acciones necesarias despues de crear el recurso, para ello lo volvemos a leer*/
			          cmsFile = cmsObject.readFile(resource);
			          content = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);
		                
		              // Comprueba que el xml este bien formado, escribe los mapeos y asigna las categorías de los campos tipo OpenCmsCategory
		              cmsFile = content.getHandler().prepareForWrite(cmsObject, content, cmsFile);
		                
		              //Desbloqueamos
		              cmsObject.unlockResource(resource);
		              //Publicamos el recurso
		              if(publish)
		                	OpenCms.getPublishManager().publishResource(cmsObject, resource);
	                
		              cmsResource = cmsFile;
		              if(cmsResource == null)
		            	  success = false;
		              else
		            	  success = true;
		              
		              if(change)
		              {
		            	  cmsObject.getRequestContext().setCurrentProject(project);
		              }
				}else{
					return false;
				}

				return success;
			}



			/**
			 * This method creates or edits a CmsResource.
			 * @param resource - resource's path + name
			 * @param type - Resorce type's id
			 * @param content - CmsXmlContent associated to the resource
			 * @return
			 */
			protected CmsResource createOrEditResource(String resource, String typeName, CmsXmlContent content, boolean publish)
			{
				  /* Check if the resource exists*/
				  boolean exists = cmsObject.existsResource(resource,CmsResourceFilter.ALL);
				  boolean change = false;

				  CmsResource cmsResource = null;

				  try
				  {
					  /*
					   * Get the current project and check if the current project is the online project, if it is change to the offline project
					   * since one can only create resources on the offline project.
					   */
					  CmsProject project = cmsObject.getRequestContext().getCurrentProject();
					  if(project.getName().equals("Online"))
					  {
						  cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
						  change = true;
					  }

					  /* Prepare the xml conent */
					  byte[] byteContent= content.marshal();

		              if(exists) //The resource exists
		              {
			            	//Bloqueamos el recurso
			            	cmsObject.lockResource(resource);
			            	
			            	/*Leemos el recurso a modificar*/
			                CmsFile cmsFile = cmsObject.readFile(resource);
	
			                //Modificamos el contenido
			                cmsFile.setContents(byteContent);
			                cmsObject.writeFile(cmsFile);
			                
			                /*Ejecutamos mappings y otras acciones necesarias despues de crear el recurso, para ello lo volvemos a leer*/
				            cmsFile = cmsObject.readFile(resource);
				            content = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);
			                
			                // Comprueba que el xml este bien formado, escribe los mapeos y asigna las categorías de los campos tipo OpenCmsCategory
			                cmsFile = content.getHandler().prepareForWrite(cmsObject, content, cmsFile);
			                
			                //Desbloqueamos
			                cmsObject.unlockResource(resource);
		                    /*
			                 * CmsObject.publishResource it's deprecated for OpenCms 7 use this instead
			                 * OpenCms.getPublishManager().publishResource(cmsObject, resource);
			                 */
			                //cmsObject.publishResource(resource);
			                //Publicamos el recurso
			                if(publish)
			                	OpenCms.getPublishManager().publishResource(cmsObject, resource);
		                
			                cmsResource = cmsFile;
		              }
		              else //The resource does not exist
		              {
			            	 /*Create the resource and set the content*/
		            	  	 cmsResource = cmsObject.createResource(resource, OpenCms.getResourceManager().getResourceType(typeName), byteContent, new ArrayList());
			            	 
		            	  	 //Metodo deprecado
		            	  	 //cmsResource = cmsObject.createResource(resource, typeName, byteContent, new ArrayList());
			            	 
			            	 /*Ejecutamos mappings y otras acciones necesarias despues de crear el recurso, para ello lo volvemos a leer*/
				             CmsFile cmsFile = cmsObject.readFile(resource);
				             content = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);
				             
				             // Comprueba que el xml este bien formado, escribe los mapeos y asigna las categorías de los campos tipo OpenCmsCategory
				             cmsFile = content.getHandler().prepareForWrite(cmsObject, content, cmsFile);
				             
			                 cmsObject.unlockResource(resource);
				              /*
				               * CmsObject.publishResource it's deprecated for OpenCms 7 use this instead
				               * OpenCms.getPublishManager().publishResource(cmsObject, resource);
				               */
				             //cmsObject.publishResource(resource);
			                 if(publish)
			                	 OpenCms.getPublishManager().publishResource(cmsObject, resource);
		              }
		              if(change)
		              {
		            	  cmsObject.getRequestContext().setCurrentProject(project);
		              }
				  }
				  catch(Exception e)
				  {
					  e.printStackTrace();
					  LOG.error(e.toString());
				  }

	              return cmsResource;
			}

			/**
			 * This method creates or edits a CmsResource.
			 * @param resource - resource's path + name
			 * @param type - Resorce type's id
			 * @param content - CmsXmlContent associated to the resource
			 * @return
			 */
			protected CmsResource editResource(String resource, CmsXmlContent content)
			{
				  return editResource(resource, content, true);
			}
			
			protected CmsResource editResource(String resource, CmsXmlContent content, boolean publish)
			{
				  /* Check if the resource exists*/
				  boolean exists = cmsObject.existsResource(resource,CmsResourceFilter.ALL);
				  boolean change = false;

				  CmsResource cmsResource = null;

				  try
				  {
					  /*
					   * Get the current project and check if the current project is the online project, if it is change to the offline project
					   * since one can only create resources on the offline project.
					   */
					  CmsProject project = cmsObject.getRequestContext().currentProject();
					  if(project.getName().equals("Online"))
					  {
						  cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
						  change = true;
					  }

					  /* Prepare the xml conent */
		              byte[] byteContent= content.marshal();

		              if(exists) //The resource exists
		              {
		            	/*Read the resource, and modify it*/
		                CmsFile cmsFile = cmsObject.readFile(resource);
		                cmsObject.lockResource(resource);
		                cmsFile.setContents(byteContent);
		                cmsObject.writeFile(cmsFile);
		                cmsObject.unlockResource(resource);
		                  /*
			               * CmsObject.publishResource it's deprecated for OpenCms 7 use this instead
			               * OpenCms.getPublishManager().publishResource(cmsObject, resource);
			               */
			            //cmsObject.publishResource(resource);
		                if(publish) {
		                	OpenCms.getPublishManager().publishResource(cmsObject, resource);
		                }
		                cmsResource = cmsFile;
		              }

		              if(change)
		              {
		            	  cmsObject.getRequestContext().setCurrentProject(project);
		              }
				  }
				  catch(Exception e)
				  {
					  e.printStackTrace();
					  LOG.error(e.toString());
				  }

	              return cmsResource;
			}

			/**
			 * This auxiliary method manages the content, when there are more than one occurence of the same field
			 *
			 * @param listaValores - List that contains all the values associated to one field
			 * @param key - Field's name
			 * @param localizacion - Locale
			 * @param content - XML content
			 */
			protected boolean manageMultipleContent(List listaValores, String key, Locale localizacion, CmsXmlContent content)
			{
				boolean modified = false;
				if(listaValores!=null && listaValores.size()>0){
	                int i=0;
	                I_CmsXmlContentValue contentValue = null, contentValueInterno = null;

	                //Borramos todos los elementos previamente de la lista multiple.
	                if(content.hasValue(key, localizacion))
					{
						//Si existen elementos los borramos previamente.
						contentValue = content.getValue(key, localizacion);
						int numElementos = contentValue.getMaxIndex();


						//Tenemos 2 posibles casos:
						// 1- Mismo numero de campos, por lo que los recorremos y los editamos.
						// 2- Numero distinto, por lo que borramos y volvemos a editar todo

						if(listaValores.size()==numElementos){
							//actualizamos uno a uno viendo si hay cambios

							//Check if the first element is nested content.
							if(listaValores.get(0) instanceof HashMap)
							{
								//Map iteration
								Iterator itList = listaValores.iterator();
								while (itList.hasNext())
								{
									HashMap map2 = (HashMap)itList.next();
	                    	   		/*manage simple nested content*/
									modified = manageNestedContent (map2, key, localizacion, content, i) || modified;
									i++;
								}
							}
							else //The content is simple
							{
								//Go through the list and insert the values.
								while (i<listaValores.size())
								{
									modified = manageSimpleContent(key,(String)listaValores.get(i), localizacion, content, i) || modified;
									i++;
								}
							}

						}else{
							//Borramos todos para volverlos a crear

							for (int j=numElementos-1;j>0;j--)
							{
								content.removeValue(key, localizacion, j);
							}

							//Check if the first element is nested content.
							if(listaValores.get(0) instanceof HashMap)
							{
								//Map iteration
								Iterator itList = listaValores.iterator();
								while (itList.hasNext())
								{
									HashMap map2 = (HashMap)itList.next();
	                    	   /*manage simple nested content*/
									manageNestedContent (map2, key, localizacion, content, i);
									i++;
								}
							}
							else //The content is simple
							{
								//Go through the list and insert the values.
								while (i<listaValores.size())
								{
									manageSimpleContent(key,(String)listaValores.get(i), localizacion, content, i);
									i++;
								}
							}
							modified = true;
						}
					}else{
						//Check if the first element is nested content.
						if(listaValores.get(0) instanceof HashMap)
						{
							//Map iteration
							Iterator itList = listaValores.iterator();
							while (itList.hasNext())
							{
								HashMap map2 = (HashMap)itList.next();
	                    	   /*manage simple nested content*/
								manageNestedContent (map2, key, localizacion, content, i);
								i++;
							}
						}
						else //The content is simple
						{
							//Go through the list and insert the values.
							while (i<listaValores.size())
							{
								manageSimpleContent(key,(String)listaValores.get(i), localizacion, content, i);
								i++;
							}
						}
						modified = true;
					}
				}
				return modified;
			}
			
			/**
			 * This auxiliary method manages the choice elemnts
			 *
			 * @param listaValores - List that contains all the values associated to one field
			 * @param key - Field's name
			 * @param localizacion - Locale
			 * @param content - XML content
			 */
			protected boolean manageChoiceContent(List<HashMap> listaValores, String key, Locale localizacion, CmsXmlContent content)
			{
				boolean modified = false;
				if(listaValores!=null && listaValores.size()>0)
				{
					//Si se han definido valores nuevos, miramos primero si tiene valores anteriormente y si tiene, si el numero de elementos es el mismo
					//en el caso que no sea el mismo valor, borramos todo y volvemos a crear, si son el mismo numero, editamos los actuales
	                int i=0;
	                I_CmsXmlContentValue contentValue = null, contentValueInterno = null;

					boolean borrarYCrear = false;
	
	                //Miramos si hay valores actualmente
	                if(content.hasValue(key, localizacion))
					{
	                	//Si hay valores, miramos primero si el numero de elementos es igual o no a los valores nuevos
						contentValue = content.getValue(key, localizacion);
						int numElementos = contentValue.getMaxIndex();
						if(numElementos == listaValores.size()){
							//Si el numero de elementos es el mismo, ahora tenemos que ver que sean del mismo tipo todos ellos
							int contFields = 1;
							for(HashMap c: listaValores){
								for(Object fieldName: c.keySet()){
									String fieldNameAux = key+"["+contFields+"]";
									List<I_CmsXmlContentValue> subValues = content.getSubValues(fieldNameAux,localizacion);
									if(subValues.size()>0 && !(""+fieldName).equals(subValues.get(0).getName())){
										//Si alguno de los campos del choice no es del mismo tipo, tenemos que borrar y crear todo de nuevo
										borrarYCrear = true;
									}
								}
								contFields++;
							}
						}else{
							borrarYCrear = true;
						}


						if(borrarYCrear) {
							//Borramos y volvemos a crear
							for (int j = numElementos - 1; j >= 0; j--) {
								content.removeValue(key, localizacion, j);
							}

							//Contador por tipos
							Map<String,Integer> contadorMap = new HashMap<String,Integer>();

							//Añadimos el elemento principal antes de añadir los hijos
							contentValue = content.addValue(cmsObject, key, localizacion, 0);

							/*Path to the node*/
							String xPath = contentValue.getPath() + "/";

							//Map iteration
							for (HashMap c : listaValores) {

								//El hashmap solo tendra un valor, pero aun asi lo recorremos
								Iterator it = c.keySet().iterator();
								while(it.hasNext())
								{
									String key2 = (String)it.next();
									Integer currentCont = 1;

									//Buscamos el siguiente contador
									if(contadorMap.containsKey(key2)){
										//Si existe, le sumamos uno y se lo unimos a la key
										currentCont = contadorMap.get(key2);
										currentCont = currentCont + 1;
										contadorMap.put(key2,currentCont);
									}else{
										//Inicializamos el contador
										contadorMap.put(key2,1);
									}

									//Obtenemos el valor y seguimos el proceso
									Object valor2 = c.get(key2);

									if(valor2 instanceof ArrayList){
										manageMultipleContent((ArrayList) valor2, xPath + key2 + "["+currentCont+"]", localizacion, content);
									}else if (valor2 instanceof HashMap){
										manageNestedContent((HashMap) valor2, xPath + key2, localizacion, content,currentCont-1);
									}else if(valor2 instanceof Choice){
										manageChoiceContent(((Choice)valor2).getSubfields(), xPath + key2 + "["+currentCont+"]", localizacion, content);
									}else if(valor2 instanceof String){
										manageSimpleContent(xPath + key2, (String)valor2, localizacion, content, currentCont-1);
									}else{
										//noop
									}
								}
							}
							//Se han realizado cambios:
							modified = true;
						}else{
							//Editamos los ya existentes

							/*Path to the node*/
							//String xPath = contentValue.getPath() + "/";
							String xPath = key+"[1]" + "/";

							//Contador por tipos
							Map<String,Integer> contadorMap = new HashMap<String,Integer>();

							//Map iteration
							for(HashMap c: listaValores)
							{

								//El hashmap solo tendra un valor, pero aun asi lo recorremos
								Iterator it = c.keySet().iterator();
								while(it.hasNext())
								{
									String key2 = (String)it.next();
									Integer currentCont = 1;

									//Buscamos el siguiente contador
									if(contadorMap.containsKey(key2)){
										//Si existe, le sumamos uno y se lo unimos a la key
										currentCont = contadorMap.get(key2);
										currentCont = currentCont + 1;
										contadorMap.put(key2,currentCont);
									}else{
										//Inicializamos el contador
										contadorMap.put(key2,1);
									}

									//Obtenemos el valor y seguimos el proceso
									Object valor2 = c.get(key2);

									if(valor2 instanceof ArrayList){
										manageMultipleContent((ArrayList) valor2, xPath + key2 + "["+currentCont+"]", localizacion, content);
									}else if (valor2 instanceof HashMap){
										manageNestedContent((HashMap) valor2, xPath + key2, localizacion, content,currentCont-1);
									}else if(valor2 instanceof Choice){
										manageChoiceContent(((Choice)valor2).getSubfields(), xPath + key2 + "["+currentCont+"]", localizacion, content);
									}else if(valor2 instanceof String){
										manageSimpleContent(xPath + key2, (String)valor2, localizacion, content, currentCont-1);
									}else{
										//noop
									}
								}
							}
						}
					}else{
						//No existen valores actualmente, tenemos que crearlos todos

						//Añadimos el elemento principal antes de añadir los hijos
						contentValue = content.addValue(cmsObject, key, localizacion, 0);

						/*Path del campo choice*/
						String xPath = contentValue.getPath() + "/";

						//Contador por tipos
						Map<String,Integer> contadorMap = new HashMap<String,Integer>();

						//Recorremos todos los hijos del choice
						for(HashMap c: listaValores)
						{

							//El hashmap solo tendra un valor, pero aun asi lo recorremos
							Iterator it = c.keySet().iterator();
							while(it.hasNext())
							{
								String key2 = (String)it.next();
								Integer currentCont = 1;

								//Buscamos el siguiente contador
								if(contadorMap.containsKey(key2)){
									//Si existe, le sumamos uno y se lo unimos a la key
									currentCont = contadorMap.get(key2);
									currentCont = currentCont + 1;
									contadorMap.put(key2,currentCont);
								}else{
									//Inicializamos el contador
									contadorMap.put(key2,1);
								}

								//Obtenemos el valor y seguimos el proceso
								Object valor2 = c.get(key2);

								if(valor2 instanceof ArrayList){
									manageMultipleContent((ArrayList) valor2, xPath + key2 + "["+currentCont+"]", localizacion, content);
								}else if (valor2 instanceof HashMap){
									manageNestedContent((HashMap) valor2, xPath + key2, localizacion, content,currentCont-1);
								}else if(valor2 instanceof Choice){
									manageChoiceContent(((Choice)valor2).getSubfields(), xPath + key2 + "["+currentCont+"]", localizacion, content);
								}else if(valor2 instanceof String){
									manageSimpleContent(xPath + key2, (String)valor2, localizacion, content, currentCont-1);
								}else{
									//noop
								}
							}
						}
						//Como se han creado campos nuevos, marcamos que se han producido cambios
						modified = true;
					}
				}else{
					//Tenemos que borrar el valor actual del campo
					if(content.hasValue(key, localizacion, 0))
					{
						//Si el contenido existe y el valor es null
						I_CmsXmlContentValue contentValue = content.getValue(key, localizacion);
						int numElementos = contentValue.getMaxIndex();
						for (int j=numElementos-1;j>=contentValue.getMinOccurs();j--)
						{
							content.removeValue(key, localizacion, j);
						}
					}
					modified = true;
				}
				return modified;
			}


			/**
			 * This method sets nested content's values
			 * @param map2 - Nested content
			 * @param key - Field's name
			 * @param localizacion - locale
			 * @param content - Xml content
			 */
			private boolean manageNestedContent (HashMap map2, String key, Locale localizacion, CmsXmlContent content)
			{
				/* calls the method which manages the set up on a determined index*/
				return manageNestedContent (map2, key, localizacion, content, 0);
			}

			/**
			 * This method sets up the content values on the declarated index
			 * @param map2 - Nested content
			 * @param key - Field's name
			 * @param localizacion - locale
			 * @param content - Xml content
			 * @param i - index
			 */
			private boolean manageNestedContent (HashMap map2, String key, Locale localizacion, CmsXmlContent content, int i)
			{
				 boolean modified = false;

				 I_CmsXmlContentValue contentValueInterno = null, contentValue = null;
				 /*Checks if the nested content has that value*/
	        	 if(!content.hasValue(key, localizacion, i))
	        	 {
	        		 /*If it does not have it, it has to be created*/
                     contentValue = content.addValue(cmsObject, key, localizacion,i);
					 modified = true;
	        	 }
                 else
                 {
                	 /*if it exists, get the value*/
                     contentValue = content.getValue(key, localizacion, i);
                 }
	        	 /*Path to the node*/
                 String xPath = contentValue.getPath() + "/";
                 /*Map iteration*/
                 Set keys2 = map2.keySet();
             	 Iterator itKeys2 = keys2.iterator();
             	 while(itKeys2.hasNext())
             	 {
                     Object key2 = (String)itKeys2.next();
                     Object valor2 = map2.get(key2);

	                  /*
	                   * Depending on the object's type an action is carried out:
	                   * ArrayList = Meaning that there is more than one element of the same field.
	                   * HashMap = Nested content
	                   * String = Simple content
	                   */

                     if(valor2 instanceof ArrayList){
						 modified = manageMultipleContent((ArrayList)valor2, xPath+key2, localizacion, content) || modified;
                     }else if (valor2 instanceof HashMap){
						 modified =  manageNestedContent((HashMap) valor2, xPath+key2, localizacion, content) || modified;
                     }else if(valor2 instanceof Choice)
                     {
						 modified = manageChoiceContent(((Choice)valor2).getSubfields(), xPath+key, localizacion, content) || modified;
                     }else if(valor2 instanceof Long)
					 {
						 modified = manageSimpleContent(xPath + key2, ((Long)valor2).toString(), localizacion, content) || modified;
					 }else if(valor2 instanceof String)
                     {
						 modified = manageSimpleContent(xPath+key2, (String)valor2, localizacion, content) || modified;
                     }
             	}
				return modified;
	        }

			/**
			 * This method sets simple content's values
			 * @param key - field's name
			 * @param valor - field's new value
			 * @param localizacion - locale
			 * @param content - XML content
			 */
			private boolean manageSimpleContent(String key, String valor, Locale localizacion, CmsXmlContent content)
			{
				/* calls the method which manages the set up on a determined index*/
				return manageSimpleContent(key, valor, localizacion, content, 0);
			}


			/**
			 * This method sets the content on the declared index
			 * @param key - field's name
			 * @param valor - field's new value
			 * @param localizacion - locale
			 * @param content - XML content
			 */
			private boolean manageSimpleContent(String key, String valor, Locale localizacion, CmsXmlContent content, int i)
			{
				I_CmsXmlContentValue contentValue = null;
				boolean modified = false;

				 /*Checks if the content at the declared index exists*/
				if(content.hasValue(key, localizacion, i) && valor !=null)
                {
					/*If the content exists, get the value and set it*/
					contentValue = content.getValue(key, localizacion, i);
					if(!valor.equals(contentValue.getStringValue(cmsObject))) { //Si el valor actual es diferente al nuevo, hacemos la modificacion
						contentValue.setStringValue(cmsObject, (String) valor);
						modified = true;
					}
                }
				else if(content.hasValue(key, localizacion, i) && valor ==null)
				{
					//Si el contenido existe y el valor es null
					content.removeValue(key, localizacion, i);
					modified = true;
				}
              	else if(valor !=null)
              	{
              		/*If the content does not exist, add it to the xml and set it */
              	    contentValue = content.addValue(cmsObject, key, localizacion, i);
              		contentValue.setStringValue(cmsObject, (String)valor);
					modified = true;
              	}
				return modified;
	        }

			/**
			 * Metodo para copiar de un locale a otro	
			 */
			
			public boolean copyToLocale(String  resource,Locale fromLocale, Locale toLocales){
				return copyToLocale(resource,fromLocale,toLocales,true);
			}
			
			public boolean copyToLocale(String  resource,Locale fromLocale, Locale toLocales,boolean publicar){
				List<Locale> l= new ArrayList<Locale>();
				l.add(toLocales);
				return copyToLocale(resource,fromLocale,l,publicar);
			}
			
			public boolean copyToLocale(String resource,Locale fromLocale, List<Locale> toLocales){
				return copyToLocale(resource,fromLocale,toLocales,true);
			}
			
			public boolean copyToLocale(String ruta,Locale fromLocale, List<Locale> toLocales,boolean publicar){
				
				boolean b=true;
				
				try{
					
				CmsResource resource = cmsObject.readResource(ruta);	
				
				cmsObject.lockResource(ruta);
					
				CmsFile file = cmsObject.readFile(resource);
				
				CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, file);
				
				for(Locale to:toLocales){
				
		            if (content.hasLocale(to)) {
		                content.removeLocale(to);
		            }
	            	content.copyLocale(fromLocale, to);
				}
				
				// write the temporary file
                String decodedContent = content.toString();
                
                file.setContents(decodedContent.getBytes(content.getEncoding()));
                
                // the file content might have been modified during the write operation    
                cmsObject.writeFile(file);
				
                cmsObject.unlockResource(ruta);

                if(publicar){
                	OpenCms.getPublishManager().publishResource(cmsObject, ruta);
                }
                
				} catch (UnsupportedEncodingException e) {
                    b=false;
                    e.printStackTrace();
					LOG.error("Error copiando de un idioma a otro");
                }catch (CmsException e) {
                	 b=false;
                	 e.printStackTrace();
                	 LOG.error("Error copiando de un idioma a otro");
				} catch (Exception e) {
					 b=false;
					 LOG.error("Error copiando de un idioma a otro");
					 e.printStackTrace();
				}
                
                
                return b;
			}
			
}


