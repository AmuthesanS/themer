package com.infy.brandbuilder.core.updatehandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.contentsync.config.ConfigEntry;
import com.day.cq.contentsync.handler.AbstractSlingResourceUpdateHandler;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.contentsync.PathRewriterOptions;

@Component(metatype = false, factory = "com.day.cq.contentsync.handler.ContentUpdateHandler/page")
public class PageUpdateHandler extends AbstractSlingResourceUpdateHandler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<String, Object> params = new HashMap<String, Object>();

	private PathRewriterOptions options;

	@Override
	public boolean updateCacheEntry(ConfigEntry configEntry, Long lastUpdated,
			String configCacheRoot, Session admin, Session session) {
		logger.info("Config Entry : {}", configEntry);
		configCacheRoot = getConfigCacheRoot(configEntry, configCacheRoot);
		ResourceResolver resolver = this.resolverFactory
				.getResourceResolver(session);
		Page rootPage = ((PageManager) resolver.adaptTo(PageManager.class))
				.getPage(configEntry.getContentPath());

		try {
			renderResource("/content/infosys/en_gb/home.html",
					configCacheRoot, admin, session);
			session.save();
		} catch (RepositoryException e) {
			logger.error("error : {}", e);
		} catch (ServletException e) {
			logger.error("error : {}", e);
		} catch (IOException e) {
			logger.error("error : {}", e);
		}

		return true;
	}

	protected boolean renderResource(String uri, String configCacheRoot,
			Session admin, Session session) throws RepositoryException,
			ServletException, IOException {
		String cachePath = configCacheRoot + getTargetPath(uri);

		ResourceResolver resolver = null;
		try {
			resolver = createResolver(session.getUserID());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			HttpServletRequest request = createRequest(uri);			

			HttpServletResponse response = this.requestResponseFactory
					.createResponse(out);

			this.slingServlet.processRequest(request, response, resolver);
			response.getWriter().flush();

			String md5 = this.requestResponseFactory.getMD5(response);
			String md5Path = cachePath + "/" + "jcr:content" + "/" + "md5";
			Node cacheContentNode;
			if ((!admin.propertyExists(md5Path))
					|| (!admin.getProperty(md5Path).getString().equals(md5))) {
				logger.info(
						"MD5 hash missing or not equal, updating content sync cache: {}",
						cachePath);

				JcrUtil.createPath(cachePath, "sling:Folder", "nt:file", admin,
						false);

				cacheContentNode = JcrUtil.createPath(cachePath
						+ "/jcr:content", "nt:resource", admin);
				String encoding;
				if (needsUtf8Encoding(response)) {
					encoding = response.getCharacterEncoding();
					cacheContentNode.setProperty(
							"jcr:data",
							admin.getValueFactory().createBinary(
									IOUtils.toInputStream(
											out.toString(encoding), encoding)));
				} else {
					cacheContentNode.setProperty(
							"jcr:data",
							admin.getValueFactory()
									.createBinary(
											new ByteArrayInputStream(out
													.toByteArray())));
				}
				cacheContentNode.setProperty("jcr:lastModified",
						Calendar.getInstance());
				if (response.getContentType() != null) {
					cacheContentNode.setProperty("jcr:mimeType",
							response.getContentType());
				}
				if (response.getCharacterEncoding() != null) {
					cacheContentNode.setProperty("jcr:encoding",
							response.getCharacterEncoding());
				}
				cacheContentNode.addMixin("cq:ContentSyncHash");
				cacheContentNode.setProperty("md5", md5);

				admin.save();

			}
			logger.info("Skipping update of content sync cache: {}", uri);

		} catch (LoginException e) {
			HttpServletRequest request;
			logger.error(
					"Creating resource resolver for resource rendering failed: ",
					e);

		} finally {
			if (resolver != null) {
				resolver.close();
			}
			if (admin.hasPendingChanges()) {
				admin.refresh(false);
			}
		}
		return true;
	}

	private ResourceResolver createResolver(String userId)
			throws RepositoryException, LoginException {
		HashMap<String, Object> authInfo = new HashMap();
		authInfo.put("user.impersonation", userId);

		return this.resourceResolverFactory
				.getAdministrativeResourceResolver(authInfo);
	}

	private boolean needsUtf8Encoding(HttpServletResponse response) {
		String contentType = response.getContentType();

		return contentType != null ? contentType.endsWith("/json") : false;
	}

	protected HttpServletRequest createRequest(String uri) {
		HttpServletRequest request = this.requestResponseFactory.createRequest(
				"GET", uri, this.params);
		request.setAttribute("REWRITE_EXPORT", "REWRITE_EXPORT");
		WCMMode.DISABLED.toRequest(request);
		return request;
	}

	private void createParameterMap(ConfigEntry configEntry,
			ResourceResolver resolver) {
		ValueMap values = ResourceUtil.getValueMap(resolver
				.getResource(configEntry.getPath() + "/parameters"));
		this.params = new HashMap();
		for (String key : values.keySet()) {
			if (!key.startsWith("jcr:")) {
				String value = (String) values.get(key, String.class);
				if (value != null) {
					this.params.put(key, value);
				} else {
					this.params.put(key, values.get(key, String[].class));
				}
			}
		}
	}
}
