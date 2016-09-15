package com.infy.brandbuilder.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;

@Property(name = "service.description", value = { "Exported Packages Servlet" })
@Component(metatype = false)
@SlingServlet(methods = { "GET" }, resourceTypes = { "sling/servlet/default" }, selectors = { "package" }, extensions = { "list" }, generateComponent = false)
public class ExportedPackagesServlet extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = 468624717845030082L;

	@Override
	protected void doGet(final SlingHttpServletRequest request,
			final SlingHttpServletResponse response) throws ServletException,
			IOException {
		response.setContentType("application/json");
		PrintWriter printWriter = response.getWriter();
		JSONWriter out = new JSONWriter(printWriter);
		String type = request.getParameter("type");
		String path = "/etc/exports";
		if (type != null && type.equals("imports")) {
			path = "/etc/imports";
		}
		try {
			ResourceResolver res = request.getResourceResolver();
			Node node = res.resolve(path).adaptTo(Node.class);
			NodeIterator itr = node.getNodes();
			out.array();
			while (itr.hasNext()) {
				Node childNode = (Node) itr.next();
				out.object();
				out.key("name");
				out.value(childNode.getName());
				out.key("path");
				out.value(childNode.getPath());
				out.endObject();
			}
			out.endArray();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			printWriter.close();
		}
	}
}