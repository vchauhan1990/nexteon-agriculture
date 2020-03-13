package com.nexteon.doit.core.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.nexteon.doit.core.constants.PropertyConstants;
import com.nexteon.doit.core.entity.TableRequest;
import com.nexteon.doit.core.entity.TableResponse;

@Component(service = Servlet.class, property = {
		Constants.SERVICE_DESCRIPTION + "=Servlet to convert a table to a json",
		PropertyConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST,
		PropertyConstants.SLING_SERVLET_PATHS + "=" + PropertyConstants.TABLE_TO_JSON_SERVLET_URL, })


public class TableToJSONServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 3369129372015571977L;
	private static final Logger LOGGER = LoggerFactory.getLogger(TableToJSONServlet.class);
	
	@Reference
	private ResourceResolverFactory resolverFactory;

	private ResourceResolver resolver = null;
	private Session session = null;
	
	
	@Override
	protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
		String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		Gson requestJson = new Gson();
		TableRequest tableRequest = requestJson.fromJson(requestBody, TableRequest.class);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(ResourceResolverFactory.SUBSERVICE, "writeService");
		
		try {
			resolver = resolverFactory.getServiceResourceResolver(param);
			session = resolver.adaptTo(Session.class);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while creating the session: {}",e.getMessage());
		}

		try {
			Node pathNode = session.getNode(tableRequest.getPath());
			Node nodeName= pathNode.getNode(tableRequest.getNodeName());
			String source = nodeName.getProperty(tableRequest.getPropertyName()).toString();
			Document doc = Jsoup.parse(source);
			ArrayList<TableResponse> tableResponse = new ArrayList<>();
			for (Element table : doc.select(PropertyConstants.PROPERTY_TABLE)) {
				for (Element row : table.select(PropertyConstants.PROPERTY_TABLE_ROW)) {
					TableResponse jsonObject = new TableResponse();
					Elements tds = row.select(PropertyConstants.PROPERTY_TABLE_DATA);
					String sno = tds.get(0).text();
					String name = tds.get(1).text();
					String filePath1 = tds.get(2).getElementsByTag(PropertyConstants.PROPERTY_ANCHOR).attr(PropertyConstants.PROPERTY_HREF);
					String imagePath1 = tds.get(2).getElementsByTag(PropertyConstants.PROPERTY_IMAGE).attr(PropertyConstants.PROPERTY_SOURCE);
					String filePath2 = tds.get(3).getElementsByTag(PropertyConstants.PROPERTY_ANCHOR).attr(PropertyConstants.PROPERTY_HREF);
					String imagePath2 = tds.get(3).getElementsByTag(PropertyConstants.PROPERTY_IMAGE).attr(PropertyConstants.PROPERTY_SOURCE);
					
					jsonObject.setSno(sno);
					jsonObject.setName(name);
					jsonObject.setFilePath1(filePath1);				
					jsonObject.setImagePath1(imagePath1);
					jsonObject.setFilePath2(filePath2);				
					jsonObject.setImagePath2(imagePath2);
					tableResponse.add(jsonObject);
				}
			}
			response.setContentType(PropertyConstants.APPLICATION_JSON);
			response.getWriter().write(new Gson().toJson(tableResponse));
		} catch (RepositoryException e) {
			LOGGER.error("Exception occurred while fetching the data: {}",e.getMessage());
		}
		
	}
}
