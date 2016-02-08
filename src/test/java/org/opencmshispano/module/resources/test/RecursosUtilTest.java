package org.opencmshispano.module.resources.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.opencmshispano.module.resources.bean.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RecursosUtilTest {
	
	@Test
	public void testJsonToResource() throws IOException {

		assertNotNull("No existe el fichero test.json",
	               getClass().getResource("/test.json"));
		try (InputStream is = getClass().getResourceAsStream("/test.json")) {

			byte[] bytes = IOUtils.toByteArray(is);
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<Map> jsonData = mapper.readValue(bytes, ArrayList.class);

			//Pasamos de Map a lista de Resource
			List<Resource> resources = new ArrayList<Resource>();
			for(Map data:jsonData){
				resources.add(new Resource(data));
			}

			assertTrue("La lista de recursos es vacia",resources.size()>0);

			//validamos los recursos
			for(Resource r: resources){
				assertTrue(r.getPath()!=null);
				assertTrue(r.getResourceType()!=null);
				assertTrue(r.getTitle()!=null);
				assertTrue(r.getFields()!=null && r.getFields().size()>0);
			}

		}


	}

}
