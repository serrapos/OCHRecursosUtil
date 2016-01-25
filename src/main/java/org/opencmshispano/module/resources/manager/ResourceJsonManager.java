package org.opencmshispano.module.resources.manager;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencmshispano.module.resources.bean.Choice;
import org.opencmshispano.module.resources.bean.Field;
import org.opencmshispano.module.resources.bean.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ResourceJsonManager {

    private CmsObject cmsObject;
    private ResourceManager rm;

    public ResourceJsonManager(CmsObject cmsObject) {
        this.cmsObject = cmsObject;
        rm = new ResourceManager(cmsObject);
    }
    
    /**
     * Metodo que recibe un String con el json correctamente formateado (ver ejemplos) y crea los recursos en OpenCms. 
     * Segun el parametro publish se publicar�n o no los recursos creados / editados
     * @param json
     * @param publish
     * @return
     */
    public List<CmsResource> updateResourcesByJson(String json, boolean publish){
    	//Mapeamos el json obtenido a un Map
        ObjectMapper mapper = new ObjectMapper();
        try {
			ArrayList<Resource> jsonData = mapper.readValue(json.getBytes(), new TypeReference<List<Resource>>(){});
			return updateResources(jsonData,publish);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();			
		}
        return null;
    }
    
    /**
     * Metodo que recibe un JsonNode correctamente formateado (ver ejemplos) y crea los recursos en OpenCms. 
     * Segun el parametro publish se publicar�n o no los recursos creados / editados
     * @param json
     * @param publish
     * @return
     */
    public List<CmsResource> updateResources(JsonNode node, boolean publish){

        List<CmsResource> recursosOpenCms = new ArrayList<CmsResource>();
        
        if(node.isArray()){
        	List<Resource> resouces = getListResources(node);
        	updateResources(resouces,publish);
        }
        
        return recursosOpenCms;
    }


    /**
     * Metodo que recibe una lista de Resouce correctamente formateado (ver ejemplos) y crea los recursos en OpenCms. 
     * Segun el parametro publish se publicaran o no los recursos creados / editados
     * @param json
     * @param publish
     * @return
     */
    public List<CmsResource> updateResources(List<Resource> recursos, boolean publish){

        List<CmsResource> recursosOpenCms = new ArrayList<CmsResource>();
        //Recorremos la lista entera y vamos creando cada uno de los recursos
        int i=1;
        for (Resource recurso : recursos) {
        	System.out.println(i+"-> Actualizando recurso: "+recurso);
            //Actualizamos cada uno de los recursos
            recursosOpenCms.add(updateResource(recurso));
            i++;
        }
        if(publish)
        	publishListResource(recursosOpenCms);    
        
        return recursosOpenCms;
    }

    /**
     * Metodo que recibe un resource y lo crea / edita en OpenCms
     * @param resource
     * @return
     */
    public CmsResource updateResource(Resource resource)  {

        try {
            //Generamos el data con el mapeo de la informacion
            HashMap data = getDataByResource(resource.getFields());
            //Obtenemos el id del recurso
            //Editamos o creamos el recurso pero no publicamos. El locale lo dejamos a null
            CmsResource cmsResource;
            if ((resource.getLocale() != null) && (!resource.getLocale().equals(""))) {
            	cmsResource = rm.saveCmsResource(data, resource.getPath(), resource.getResourceType(), false, resource.getLocale());
            } else {
            	cmsResource = rm.saveCmsResource(data, resource.getPath(), resource.getResourceType(), false, null);
            }
            return cmsResource;
        }catch(Exception ex){
            //noop
        	System.out.println("Error al crear recurso\n ");
        	ex.printStackTrace();
        }
        return null;
    }

    private HashMap<?,?> getDataByResource(List<Field> fields){
        HashMap<String, Object> data = new HashMap<String, Object>();

        //Recorremos todos los campos a�adiendo el campo
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
            	Choice dataChoice = new Choice(field.getName());
            	ArrayList fieldsAux = new ArrayList();
                if(field.getFields()!=null && field.getFields().size()>0){
	                for (Field f : field.getFields()) {
	                	//Creamos hashmap para el campo principal del choice
	                	HashMap dataAux = new HashMap();
	                	dataAux.put(f.getName(), getDataByResource(f.getFields()));
	                	fieldsAux.add(dataAux);
	                }
                }
                dataChoice.setSubfields(fieldsAux);
                data.put(field.getName(), dataChoice);
            }
        }
        return data;
    }
    
    /**
     * Publica una lista de recursos de OpenCms
     * @param resources
     */
    private void publishListResource(List<CmsResource> resources){
    	
    	try {
			OpenCms.getPublishManager().getPublishList(cmsObject, resources, true);
		} catch (CmsException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Obtiene a partir de un JsonNode una lista de Resource
     * @param listNodes
     * @return
     */
    private List<Resource> getListResources(JsonNode listNodes){
    	List<Resource> resources = new ArrayList<Resource>();
    	
    	//Recorremos la lista de recursos
    	Iterator it = listNodes.iterator();
    	while(it.hasNext()){
    		JsonNode nodeResource = (JsonNode)it.next();
    		Resource r = new Resource();
    		r.setTitle(nodeResource.get("title").asText());
    		r.setPath(nodeResource.get("path").asText());
    		r.setResourceType(nodeResource.get("resourceType").asText());
    		r.setFields(getListFields(nodeResource.get("fields")));
    		resources.add(r);
    	}
    	
    	return resources;
    }
    
    /**
     * Obtiene a partir de un JsonNode una lista de Field
     * @param listNodes
     * @return
     */
    private List<Field> getListFields(JsonNode listNodes){
    	List<Field> fields = new ArrayList<Field>();
    	
    	//Recorremos la lista de recursos
    	Iterator it = listNodes.iterator();
    	while(it.hasNext()){
    		JsonNode nodeField = (JsonNode)it.next();
    		Field f = new Field();
    		f.setName(nodeField.get("name").asText());
    		f.setType(nodeField.get("type").asText());
    		if(nodeField.hasNonNull("value") && nodeField.get("value").isTextual())
    			f.setValue(nodeField.get("value").asText());
    		if(nodeField.hasNonNull("fields") && nodeField.get("fields").isArray())
    			f.setFields(getListFields(nodeField.get("fields")));    
    		fields.add(f);
    	}
    	
    	return fields;
    }
    
}
