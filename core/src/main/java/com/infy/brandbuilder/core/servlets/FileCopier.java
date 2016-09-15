package com.infy.brandbuilder.core.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrUtil;
import com.infy.brandbuilder.core.models.CssFile;
import com.infy.brandbuilder.core.models.ImportFile;
import com.infy.brandbuilder.core.models.ImportResource;
import com.infy.brandbuilder.core.models.JpegFile;
import com.infy.brandbuilder.core.models.JsFile;
import com.infy.brandbuilder.core.models.PngFile;

@Component
@Service(value = { JobConsumer.class })
@Properties({ @Property(name = JobConsumer.PROPERTY_TOPICS, value = "com/infy/import") })
public class FileCopier implements JobConsumer {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Reference
	private ResourceResolverFactory resolverFactory;

	public JobResult process(final Job job) {
		ZipInputStream zip = null;
		FileOutputStream fos = null;
		ResourceResolver resolver = null;
		try {
			resolver = resolverFactory.getAdministrativeResourceResolver(null);
			Resource resource = resolver.resolve("/etc/imports/home.zip");
			Session session = resolver.adaptTo(Session.class);

			zip = new ZipInputStream(resource.adaptTo(InputStream.class));
			byte[] buffer = new byte[2048];
			ZipEntry entry;
			ImportResource res;
			while ((entry = zip.getNextEntry()) != null) {
				/*Node parentNode = resolver.resolve("/etc/dummy").adaptTo(
						Node.class);*/
				File tempFile = new File("temp");
				fos = new FileOutputStream(tempFile);
				int len = 0;
				while ((len = zip.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				if (entry.getName().endsWith(".png")) {
					res = new PngFile(entry.getName(), tempFile);
				}
				if (entry.getName().endsWith(".jpeg")) {
					res = new JpegFile(entry.getName(), tempFile);
				}
				if (entry.getName().endsWith(".js")) {
					res = new JsFile(entry.getName(), tempFile);
				}
				if (entry.getName().endsWith(".css")) {
					res = new CssFile(entry.getName(), tempFile);
				} else {
					res = new ImportFile(entry.getName(), tempFile);
				}	
				
				String path = res.getPath();
				String resourcePath = persistFile(path);
				Node node = resolver.resolve(resourcePath).adaptTo(Node.class);
				JcrUtils.putFile(node, res.getName(), res.getMimeType(),
						res.getStream());
				session.save();
				zip.close();
				fos.close();
				LOG.info("Job consumes!!!!!!!");
			}
		} catch (Exception e) {
			LOG.error("Error in importing", e);
			return JobResult.FAILED;
		} finally {
			if (resolver != null) {
				resolver.close();
			}
		}
		return JobResult.OK;
	}
	
	public String persistFile(String path){
		
		String[] folderStructure = path.split("/");
		String[] excludingFile = Arrays.copyOf(folderStructure,folderStructure.length-1);
		Resource resource = null;
		StringBuilder builder =new StringBuilder(); 
		for(String eachLevel : excludingFile){
			builder.append("/");
			builder.append(eachLevel);
			resource = createFolderStructure(builder.toString());
			LOG.info("path"+builder.toString());
		}
		return builder.toString();
	}
	
	public Resource createFolderStructure(String path){
		ResourceResolver resolver= null;
		Resource resource = null;
		try {
			resolver = resolverFactory.getAdministrativeResourceResolver(null);			
			resource = resolver.getResource(path);
			if(ResourceUtil.isNonExistingResource(resource)){
				Session session = resource.getResourceResolver().adaptTo(Session.class);			
			    Node node = JcrUtil.createPath(path, "nt:folder", session);
				session.save();				
			}
		 } catch (LoginException e) {			
			LOG.error("Exception in createFolderStructure",e);
		}	
		catch (RepositoryException e) {			
			LOG.error("Repository Exception",e);
		}	
		
		return resource;
	}
}
