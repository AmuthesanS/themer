package com.infy.brandbuilder.core.servlets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.contentsync.PageExporter;

@Component
@Service(value = { JobConsumer.class })
@Property(name = JobConsumer.PROPERTY_TOPICS, value = "my/theme/exportPackage")
public class ExportService implements JobConsumer {

	@Reference
	private PageExporter exporter;

	@Reference
	ResourceResolverFactory resourceResolverFactory;

	private static final Logger log = LoggerFactory
			.getLogger(PageExportServlet.class);

	public JobResult process(final Job job) {
		Map<String, Object> serviceParams = new HashMap<String, Object>();
		serviceParams.put(ResourceResolverFactory.SUBSERVICE, "ExportService");
		try {
			ResourceResolver resourceResolver = resourceResolverFactory
					.getAdministrativeResourceResolver(null);
			log.info("Job Result started");
			SimpleDateFormat sdfDate = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH-mm-ss");
			Date now = new Date();
			String strDate = sdfDate.format(now);
			Resource res = resourceResolver.getResource((String) job
					.getProperty("pagePath"));
			log.info((String) job.getProperty("pagePath"));
			Page page = res.adaptTo(Page.class);
			log.info("inside try block of Job Result" + page.getPath());
			this.exporter.export(page, resourceResolver,
					"/etc/exports/" + page.getName() + strDate + ".zip");

		} catch (WCMException e) {
			log.error("Page export failed: ", e);
			return JobResult.CANCEL;
		} catch (Exception e) {
			log.error("Page export failed: ", e);
			return JobResult.CANCEL;
		}
		log.info("job finished");
		return JobResult.OK;
	}

	protected void bindExporter(PageExporter paramPageExporter) {
		this.exporter = paramPageExporter;
	}

	protected void unbindExporter(PageExporter paramPageExporter) {
		if (this.exporter == paramPageExporter) {
			this.exporter = null;
		}
	}
}
