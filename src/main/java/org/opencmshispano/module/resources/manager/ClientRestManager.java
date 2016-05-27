package org.opencmshispano.module.resources.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencmshispano.module.resources.bean.Resource;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientRestManager {
	
	public List<Resource> executeWS(String wsUrl){
        try {
            //Lanzamos petición http al WS con los parametros indicados
            URL url = new URL(wsUrl);
            URLConnection uc = url.openConnection();
            uc.connect();

            //Obtenemos la respuesta y la transformamos a UTF8
            byte[] bytes = IOUtils.toByteArray(uc.getInputStream());
            String respuesta = new String(bytes, "UTF-8");

            //Mapeamos el json obtenido a un Map
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Map> jsonData = mapper.readValue(bytes, ArrayList.class);

            //Pasamos de Map a lista de Resource
            List<Resource> resources = new ArrayList<Resource>();
            for(Map data:jsonData){
            	resources.add(new Resource(data));
            }
            return resources;
        } catch(Exception e) {
            return new ArrayList<Resource>();
        }
    }
	
	public List<CmsResource> updateByWs(String wsUrl, CmsObject cms, boolean publish){
        if(wsUrl!=null) {
            //Obtenemos la lista de recursos a actualizar
            List<Resource> recursos = executeWS(wsUrl);

            //Actualizamos los recurso en OpenCms
            ResourceJsonManager resourceService = new ResourceJsonManager(cms);
            return resourceService.updateResources(recursos,publish);
        }
        return null;
    }
	
	public List<CmsResource> updateByWs(String wsUrl, CmsObject cms){
        return updateByWs(wsUrl,cms,false);
    }

}
