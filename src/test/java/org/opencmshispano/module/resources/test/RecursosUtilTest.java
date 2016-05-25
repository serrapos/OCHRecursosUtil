package org.opencmshispano.module.resources.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.opencmshispano.module.resources.bean.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
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


	@Test
	public void testJsonRandomText() throws IOException {

		String url = "http://www.randomtext.me/api/lorem/h1/10-30";
		assertNotNull(getText(url));

	}

	public String getText(String wsUrl){
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
			HashMap jsonData = mapper.readValue(bytes, HashMap.class);

			System.out.println(jsonData);

			return ""+jsonData.get("text_out");

		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
