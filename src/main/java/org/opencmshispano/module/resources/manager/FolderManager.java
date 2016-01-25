package org.opencmshispano.module.resources.manager;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
//import org.tsol.module.util.Bundle;

public class FolderManager
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
	public FolderManager(CmsJspActionElement cms)
	{
		this.cms=cms;
		this.cmsObject=cms.getCmsObject();
		
	}

	public void createSimpleFolder(String path, String name, String title)
	{
		try 
		{
			LOG.error("1");
			CmsProperty property = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null, false);
			int tipofolder = OpenCms.getResourceManager().getResourceType("folder").getTypeId();
			int typeIndex = OpenCms.getResourceManager().getResourceType("xmlpage").getTypeId();
			LOG.error("2");
			CmsResource cmsResource = cmsObject.createResource(path  + name, tipofolder);

			LOG.error("ASDA");
			
			LOG.error("4");			
			cmsObject.writePropertyObject(path  + name, property);
			LOG.error("5");
			//cmsObject.writeProperty(cmsResource.getRootPath(), "Title", title);
            cmsObject.unlockResource(path  + name);
			LOG.error("6");
            //cmsObject.publishResource(path  + name);
            while(OpenCms.getPublishManager().publishResource(cmsObject, path + name)==null)
            LOG.error("5");
            //cmsObject.unlockResource(path  + name);
            //CmsResource indexhtml = cmsObject.createResource(path +name +  "/index.html", typeIndex);
			//CmsProperty property = new CmsProperty("templa­te-elements", templateIndex, null, true);
		    //CmsProperty property2 = new CmsProperty("templa­te", templateIndex, null, true);
			//cmsObject.writePropertyObject(indexhtml.getRootPath(), property);
			//cmsObject.writePropertyObject(indexhtml.getRootPath(), property2);	
			//cmsObject.writeProperty(indexhtml.getRootPath(), "template-elements", templateIndex, false);
			//cmsObject.writeProperty(indexhtml.getRootPath(), "template", templateIndex, false);
           // cmsObject.unlockResource(path  + name);
           // cmsObject.publishResource(indexhtml.getRootPath());
		} 
		catch (CmsLoaderException e) 
		{
			LOG.error(e.toString());
			e.printStackTrace();
		} 
		catch (CmsIllegalArgumentException e)
		{
			LOG.error(e.toString());
			e.printStackTrace();
		} 
		catch (CmsException e) 
		{			
			LOG.error(e.toString());
			e.printStackTrace();
		} 
		catch (Exception e)
		{
			try 
			{
				OpenCms.getPublishManager().publishResource(cmsObject, path);
				OpenCms.getPublishManager().publishResource(cmsObject, path + name);
			} catch (Exception e1) {
				LOG.error(e.toString());
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			LOG.error(e.toString());
			e.printStackTrace();
		}
	}
		
	public void createProjectFolder(String path, String country, String name)
	{
		String countryAux=country;
		String nameAux = name;
		if(country.indexOf("(")!=-1)
		{
			country=country.replace("(", "");
		}
		if(country.indexOf(")")!=-1)
		{
			country=country.replace(")", "");
		}
		if(country.indexOf("'")!=-1)
		{
			country=country.replace("'", "");
		}
		if(country.indexOf(",")!=-1)
		{
			country=country.replace(",", "");
		}
		if(country.indexOf(" ")!=-1)
			country = country.replace(" ", "");
		
		if(name.indexOf("(")!=-1)
		{
			name=name.replace("(", "");
		}
		if(name.indexOf(")")!=-1)
		{
			name=name.replace(")", "");
		}
		if(name.indexOf("'")!=-1)
		{
			name=name.replace("'", "");
		}
		if(name.indexOf(",")!=-1)
		{
			name=name.replace(",", "");
		}
		if(name.indexOf(" ")!=-1)
			name = name.replace(" ", "");
		
		if(!cmsObject.existsResource(path + country))
			createSimpleFolder(path, country, countryAux);
		createSimpleFolder(path + country + "/", name, nameAux);
		path= path + country + "/" + name;
		try
		{
			int typeContract = OpenCms.getResourceManager().getResourceType("contract").getTypeId();
			String pathContract = path + "/contract.html";
			int typeProject = OpenCms.getResourceManager().getResourceType("project").getTypeId();
			String pathProject = path + "/project.html";
			int typeText = OpenCms.getResourceManager().getResourceType("simpletext").getTypeId();
			String pathSimple= path + "/simple_0001.html";
			int typeJob = OpenCms.getResourceManager().getResourceType("jobs").getTypeId();
			String pathJob = path + "/job_0001.html";
			cmsObject.createResource(pathJob, typeJob);
            cmsObject.unlockResource(pathJob);
			cmsObject.createResource(pathContract, typeContract);
            cmsObject.unlockResource(pathContract);
			cmsObject.createResource(pathProject, typeProject);
            cmsObject.unlockResource(pathProject);
			cmsObject.createResource(pathSimple, typeText);
			cmsObject.unlockResource(pathSimple);
            LOG.error("3");
            OpenCms.getPublishManager().publishResource(cmsObject, pathSimple);
            OpenCms.getPublishManager().publishResource(cmsObject, pathProject);
			OpenCms.getPublishManager().publishResource(cmsObject, pathJob);
			OpenCms.getPublishManager().publishResource(cmsObject, pathContract);		
			LOG.error("5");
		} 
		catch (CmsLoaderException e) 
		{
			LOG.error(e.toString());
			e.printStackTrace();
		} 
		catch (CmsIllegalArgumentException e) 
		{
			LOG.error(e.toString());
			e.printStackTrace();
		} 
		catch (CmsException e)
		{
			LOG.error(e.toString());
			e.printStackTrace();
		} catch (Exception e) 
		{
			try
			{
				String path2 = path + country + name;
				String pathContract = path2 + "/contract.html";
				String pathProject = path2 + "/project.html";
				String pathSimple= path2 + "/simple_0001.html";
				String pathJob = path2 + "/job_0001.html";
				OpenCms.getPublishManager().publishResource(cmsObject,path + country );
				OpenCms.getPublishManager().publishResource(cmsObject,path2 );
	            OpenCms.getPublishManager().publishResource(cmsObject, pathSimple);
	            OpenCms.getPublishManager().publishResource(cmsObject, pathSimple);
	            OpenCms.getPublishManager().publishResource(cmsObject, pathProject);
				OpenCms.getPublishManager().publishResource(cmsObject, pathJob);
				OpenCms.getPublishManager().publishResource(cmsObject, pathContract);
			}
			catch (Exception e1)
			{
				LOG.error(e1.toString());
				e.printStackTrace();			
			}
			
		}		
	}
	
}
