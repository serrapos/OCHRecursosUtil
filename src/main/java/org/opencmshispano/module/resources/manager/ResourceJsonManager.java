package org.opencmshispano.module.resources.manager;

import org.opencmshispano.module.resources.bean.Field;
import org.opencmshispano.module.resources.bean.Resource;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencmshispano.module.resources.manager.ResourceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResourceJsonManager {

    private CmsObject cmsObject;
    private CmsJspActionElement cms;
    private ResourceManager rm;

    public ResourceJsonManager(CmsJspActionElement cms, CmsObject cmsObject) {
        this.cmsObject = cmsObject;
        this.cms = cms;
        rm = new ResourceManager(cms);
    }

    public void updateResources(List<Resource> recursos, boolean publish){

        List<CmsResource> recursosOpenCms = new ArrayList<CmsResource>();
        //Recorremos la lista entera y vamos creando cada uno de los recursos
        for (Resource recurso : recursos) {
            //Actualizamos cada uno de los recursos
            recursosOpenCms.add(updateResource(recurso));
        }
        if(publish)
        	publishListResource(recursosOpenCms);        
    }

    public CmsResource updateResource(Resource resource)  {

        try {
            //Generamos el data con el mapeo de la informacion
            HashMap data = getDataByResource(resource.getFields());
            //Obtenemos el id del recurso
            int resourceTypeId = OpenCms.getResourceManager().getResourceType(resource.getResourceType()).getTypeId();
            //Editamos o creamos el recurso pero no publicamos. El locale lo dejamos a null
            CmsResource cmsResource = rm.saveCmsResource(data, resource.getPath(), resourceTypeId, false, null);
            return cmsResource;
        }catch(CmsException ex){
            //noop
        	System.out.println("Error al crear recurso\n ");
        	ex.printStackTrace();
        }
        return null;
    }

    private HashMap<?,?> getDataByResource(List<Field> fields){
        HashMap<String, Object> data = new HashMap<String, Object>();

        //Recorremos todos los campos añadiendo el campo
        for (Field field : fields) {
            if(Field.FIELD_TYPE_SIMPLE.equals(field.getType())){
            	if(field.getValue()!=null){
            		data.put(field.getName(), field.getValue());
            	}
            }else if(Field.FIELD_TYPE_NESTED.equals(field.getType())){
            	if(field.getFields()!=null){
            		data.put(field.getName(), getDataByResource(field.getFields()));
            	}
            }else if(Field.FIELD_TYPE_MULTIPLE_SIMPLE.equals(field.getType())) {
                List<String> fieldsAux = new ArrayList<String>();
                if(field.getFields()!=null){
	                for (Field f : field.getFields()) {
	                    fieldsAux.add(f.getValue());
	                }
	                data.put(field.getName(), fieldsAux);
                }
            }
            else if(Field.FIELD_TYPE_MULTIPLE_NESTED.equals(field.getType())) {
                List<HashMap> fieldsAux = new ArrayList<HashMap>();
                if(field.getFields()!=null){
	                for (Field f : field.getFields()) {
	                    fieldsAux.add(getDataByResource(f.getFields()));
	                }
	                data.put(field.getName(), fieldsAux);
                }
            }else if(Field.FIELD_TYPE_MULTIPLE_CHOICE.equals(field.getType())) {
                List<HashMap> fieldsAux = new ArrayList<HashMap>();
                if(field.getFields()!=null){
	                for (Field f : field.getFields()) {
	                    fieldsAux.add(getDataByResource(f.getFields()));
	                }
	                data.put(field.getName(), fieldsAux);
                }
            }
        }
        return data;
    }
    
    private void publishListResource(List<CmsResource> resources){
    	
    	try {
			OpenCms.getPublishManager().getPublishList(cmsObject, resources, true);
		} catch (CmsException e) {
			e.printStackTrace();
		}
    }
    
}
