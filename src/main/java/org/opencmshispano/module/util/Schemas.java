package org.opencmshispano.module.util;

import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.OpenCms;
import org.opencmshispano.module.exceptions.UtilException;

/**
 * 
 * @author Alejandro Alves Calderon
 * This class helps gets the resource type's schema 
 */
public class Schemas {
	
	private static final String SCHEMA_PREFIX = "opencms:/";
	
	/** Utility method that returns the schema location url
	 * 	of a resource type identified by its id.
	 *  
	 * @param type Resource type ID
	 * @return Schema location URL
	 * @throws UtilException If something was wrong
	 */
	public static String getSchemaByType(int type) throws UtilException {
		try {
			I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(type);
			String schema = (String) resType.getConfiguration().
				get(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA);
			return SCHEMA_PREFIX + schema;
		} catch (CmsLoaderException e) {
			throw new UtilException("No existe el recurso con id "+type);			
		}
	}

}
