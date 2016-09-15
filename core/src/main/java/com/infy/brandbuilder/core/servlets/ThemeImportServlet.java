package com.infy.brandbuilder.core.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.event.jobs.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infy.brandbuilder.core.models.CssFile;
import com.infy.brandbuilder.core.models.FaviconFile;
import com.infy.brandbuilder.core.models.GifFile;
import com.infy.brandbuilder.core.models.ImportFile;
import com.infy.brandbuilder.core.models.ImportResource;
import com.infy.brandbuilder.core.models.JpegFile;
import com.infy.brandbuilder.core.models.JsFile;
import com.infy.brandbuilder.core.models.PngFile;

@Property(name = "service.description", value = { "Page Import Servlet" })
@Component(metatype = false)
@Service
@SlingServlet(methods = { "GET" }, resourceTypes = { "sling/servlet/default" }, selectors = { "import" }, extensions = { "html" }, generateComponent = false)
public class ThemeImportServlet extends SlingSafeMethodsServlet {

	@Reference
	private JobManager jobManager;

	@Reference
	private ResourceResolverFactory resolverFactory;

	private static final long serialVersionUID = 1L;

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {

		final Map<String, Object> props = new HashMap<String, Object>();
		props.put("item1", "/something");
		props.put("count", 5);

		jobManager.addJob("com/infy/import", props);

		ResourceResolver resolver = request.getResource().getResourceResolver();

		Resource resource = resolver.resolve("/etc/imports/home.zip");
		Session session = resolver.adaptTo(Session.class);
		ZipInputStream zip = null;
		FileOutputStream fos = null;
		try {
			zip = new ZipInputStream(resource.adaptTo(InputStream.class));
			byte[] buffer = new byte[2048];
			ZipEntry entry;
			ImportResource res;
			while ((entry = zip.getNextEntry()) != null) {
				File tempFile = new File("temp");
				fos = new FileOutputStream(tempFile);
				boolean flag = false;
				int len = 0;
				while ((len = zip.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				if (entry.getName().endsWith(".png")) {
					res = new PngFile(entry.getName(), tempFile);
					flag = true;
				} else if (entry.getName().endsWith(".jpeg")) {
					res = new JpegFile(entry.getName(), tempFile);
					flag = true;
				} else if (entry.getName().endsWith(".js")) {
					res = new JsFile(entry.getName(), tempFile);
					flag = true;
				} else if (entry.getName().endsWith(".css")) {
					res = new CssFile(entry.getName(), tempFile);
					flag = true;
				} else if (entry.getName().endsWith(".gif")) {
					res = new GifFile(entry.getName(), tempFile);
					flag = true;
				} else if (entry.getName().endsWith(".ico")) {
					res = new FaviconFile(entry.getName(), tempFile);
					flag = true;
				} else {
					res = new ImportFile(entry.getName(), tempFile);
					flag = false;
				}
				LOG.info("Before Split, path::" + flag + res.getPath());
				if (flag && res.getPath().startsWith("etc")) {
					String path = res.getPath();
					String resourcePath = persistFile(path).getPath();
					Node node = resolver.resolve(resourcePath).adaptTo(
							Node.class);
					JcrUtils.putFile(node, res.getName(), res.getMimeType(),
							res.getStream());
					session.save();
				}
			}
		} catch (Exception e) {
			LOG.error("Error in importing", e);
		} finally {
			if (zip != null) {
				zip.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}

	public Resource persistFile(String path) {

		String[] folderStructure = path.split("/");
		String[] excludingFile = Arrays.copyOfRange(folderStructure,0,
				folderStructure.length - 1);
		Resource resource = null;
		StringBuilder builder = new StringBuilder();
		for (String eachLevel : excludingFile) {
			builder.append("/");
			builder.append(eachLevel);
			LOG.info("After Split,path::" + builder.toString());
			resource = createFolderStructure(builder.toString());

		}
		return resource;
	}

	public Resource createFolderStructure(String path) {
		ResourceResolver resolver = null;
		Resource resource = null;
		try {
			resolver = resolverFactory.getAdministrativeResourceResolver(null);
			resource = resolver.getResource(path);
			if (resource == null) {
				Map<String, Object> resourceProperties = new HashMap();
				resourceProperties.put("jcr:primaryType", "nt:folder");
				resource = ResourceUtil.getOrCreateResource(resolver, path,
						resourceProperties, "", true);
				// Session session =
				// resource.getResourceResolver().adaptTo(Session.class);
				// Node node = JcrUtil.createPath(path, "nt:folder", session);
				// session.save();
			}
		} catch (LoginException e) {
			LOG.error("Exception in createFolderStructure", e);
		} catch (PersistenceException e) {
			LOG.error("Persistence Exception", e);
		}

		return resource;
	}

}
