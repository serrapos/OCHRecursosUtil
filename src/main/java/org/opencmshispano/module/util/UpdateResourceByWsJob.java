package org.opencmshispano.module.util;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsStringUtil;
import org.opencmshispano.module.resources.manager.ClientRestManager;

import java.util.List;
import java.util.Map;

public class UpdateResourceByWsJob implements I_CmsScheduledJob{
	
	private static final Log LOG = CmsLog.getLog(UpdateResourceByWsJob.class);
	
	/** Parametros de la tarea. */
    public static final String PARAM_URL_WS = "urlws";
    public static final String PARAM_PUBLISH = "publish";

    public String launch(CmsObject cms, Map parameters) throws Exception {

        String url = (String)parameters.get(PARAM_URL_WS);
        String publishStr = (String)parameters.get(PARAM_PUBLISH);
        
        //Extraemos el valor del parametro publish. Si no se dice nada por defecto es false
        Boolean publish = false;
        if(!CmsStringUtil.isEmptyOrWhitespaceOnly(publishStr)){
        	try{
        		publish = Boolean.valueOf(publishStr);
        	}catch (Exception ex){}//noop
        }

        if(url!=null) {
            //Obtenemos la lista de recursos a actualizar
            ClientRestManager restManager = new ClientRestManager();
            List<CmsResource> resources = restManager.updateByWs(url, cms, publish);
            return "Ejecutada tarea de actualización de contenido desde la url: "+url+". Se han actualizado "+resources.size()+" recursos";
        }else{
            LOG.error("No se ha encontrado el parámetro urlws en la configuración de la tarea.");
        }
        return "La tarea no se ha ejecutado correctamente: "+url;
    }

}
