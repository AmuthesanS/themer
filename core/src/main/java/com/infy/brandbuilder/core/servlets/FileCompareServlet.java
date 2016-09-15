package com.infy.brandbuilder.core.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.io.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Property(name = "service.description", value = { "File comparison" })
@Component(metatype = false)
@SlingServlet(methods = { "GET" }, resourceTypes = { "sling/servlet/default" }, selectors = { "file" }, extensions = { "comp" }, generateComponent = false)
public class FileCompareServlet extends SlingSafeMethodsServlet{

	private static final long serialVersionUID = 468624717845030082L;

	private static final Logger log = LoggerFactory
			.getLogger(FileCompareServlet.class);

	@Override
	protected void doGet(final SlingHttpServletRequest request,
			final SlingHttpServletResponse response) throws ServletException, IOException {
		PrintWriter printWriter = response.getWriter();
		JSONWriter out = new JSONWriter(printWriter);

		final String PREFIX1 = "stream1file";
		final String PREFIX2 = "stream2file";
		final String SUFFIX = ".tmp";
		try {
			ResourceResolver res = request.getResourceResolver();
			InputStream node1 = res.resolve(request.getParameter("file1").toString()).adaptTo(InputStream.class);			
			InputStream node2 = res.resolve(request.getParameter("file2").toString()).adaptTo(InputStream.class);		

			final File file1 = File.createTempFile(PREFIX1, SUFFIX);
			final File file2 = File.createTempFile(PREFIX2, SUFFIX);
			FileOutputStream out1 = new FileOutputStream(file1);			
			FileOutputStream out2 = new FileOutputStream(file2);
			IOUtils.copy(node1, out1);
			IOUtils.copy(node2, out2);

			Set<String> lines1 = new HashSet<String>(FileUtils.readLines(file1));
			Set<String> lines2 = new HashSet<String>(FileUtils.readLines(file2));

			Iterator<String> linesItr1 = lines1.iterator();
			Iterator<String> linesItr2 = lines2.iterator();
			int counter = 0;
			//JSONArray jsonArr = new JSONArray();
			
			out.array();
			while (linesItr1.hasNext() && linesItr2.hasNext()) {
				String line1 = new String(linesItr1.next());
				String line2 = new String(linesItr2.next());
				if (!line1.equalsIgnoreCase(line2)) {
					log.info("diff"+counter);
					out.object();
					out.key("linenum");
					out.value(new Integer(counter));
					out.key("leftfile");
					out.value(line1);
					out.key("rightfile");
					out.value(line2);
					out.endObject();
					/*JSONObject jsonObj = new JSONObject();
					jsonObj.put("linenum", new Integer(counter));
					jsonObj.put("leftfile", line1);
					jsonObj.put("rightfile", line2);
					jsonArr.put(jsonObj);*/
				}
				counter++;
			}
			out.endArray();
			
			/*out.array();
			out.object();
			out.key("filediff");
			out.value(jsonArr);
			out.endObject();
			out.endArray();*/
		} catch (Exception e) {
			log.error("error",e);
		} finally{
			printWriter.close();
		}
	}

}
