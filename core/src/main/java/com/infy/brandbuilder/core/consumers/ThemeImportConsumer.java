package com.infy.brandbuilder.core.consumers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infy.brandbuilder.core.models.CssFile;
import com.infy.brandbuilder.core.models.ImportFile;
import com.infy.brandbuilder.core.models.ImportResource;
import com.infy.brandbuilder.core.models.JpegFile;
import com.infy.brandbuilder.core.models.JsFile;
import com.infy.brandbuilder.core.models.PngFile;

@Component
@Service(value = { JobConsumer.class })
@Properties({ @Property(name = JobConsumer.PROPERTY_TOPICS, value = "com/infy/import") })
public class ThemeImportConsumer implements JobConsumer {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Reference
	private ResourceResolverFactory resolverFactory;

	public JobResult process(final Job job) {
		ZipInputStream zip = null;
		FileOutputStream fos = null;
		ResourceResolver resolver = null;
		try {
			String themePath = job.getProperty("zipPath", String.class);
			resolver = resolverFactory.getAdministrativeResourceResolver(null);
			Resource resource = resolver.resolve(themePath);
			Session session = resolver.adaptTo(Session.class);

			zip = new ZipInputStream(resource.adaptTo(InputStream.class));
			byte[] buffer = new byte[2048];
			ZipEntry entry;
			ImportResource res;
			while ((entry = zip.getNextEntry()) != null) {
				Node parentNode = resolver.resolve("/etc/dummy").adaptTo(
						Node.class);
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
				JcrUtils.putFile(parentNode, res.getName(), res.getMimeType(),
						res.getStream());
				session.save();

			}
		} catch (Exception e) {
			LOG.error("Error in importing", e);
			return JobResult.FAILED;
		} finally {
			if (resolver != null) {
				resolver.close();
			}
			try {
				if (zip != null) {
					zip.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				LOG.error("Error in closing", e);
			}

		}
		return JobResult.OK;
	}
}
