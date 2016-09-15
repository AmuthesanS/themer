package com.infy.brandbuilder.core.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.event.jobs.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Property(name = "service.description", value = { "Page Export Servlet" })
@Component(metatype = false)
@Service
@SlingServlet(methods = { "GET" }, resourceTypes = { "sling/servlet/default" }, selectors = { "theming" }, extensions = { "zip" }, generateComponent = false)
public class PageExportServlet extends SlingSafeMethodsServlet {
	private static final long serialVersionUID = 468624717845030082L;
	private static final Logger log = LoggerFactory
			.getLogger(PageExportServlet.class);
	
	 @Reference
     private JobManager jobManager;
	
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {
		log.info("inside PageExportServlet");
		final Map<String, Object> props = new HashMap<String, Object>();
		String path = request.getPathInfo().replace(".theming.zip", "");
        props.put("pagePath", path);

        jobManager.addJob("my/theme/exportPackage", props);
	}
	

}
