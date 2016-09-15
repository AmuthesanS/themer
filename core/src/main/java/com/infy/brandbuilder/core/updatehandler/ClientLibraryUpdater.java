/**
 * 
 */
package com.infy.brandbuilder.core.updatehandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.widget.ClientLibrary;
import com.day.cq.widget.HtmlLibraryManager;
import com.day.cq.widget.LibraryType;

/**
 * @author amuthesan_s
 *
 */

@Component(immediate = true)
@Service(value = ClientLibraryUpdater.class)
public class ClientLibraryUpdater {

	private Logger logger = LoggerFactory.getLogger(ClientLibraryUpdater.class);

	@Reference
	private HtmlLibraryManager htmlLibManager;

	@Reference
	private ResourceResolverFactory resolverFactory;

	public List<String> outputCssLib(String themeName, int pageDepth) {
		String[] themes = { themeName };
		List<String> answer = new ArrayList<String>();
		LibraryType css = LibraryType.CSS;
		Collection<ClientLibrary> libs = htmlLibManager.getThemeLibraries(
				themes, css, themeName, true);
		Iterator<ClientLibrary> cssIter = libs.iterator();
		if (cssIter.hasNext()) {
			ClientLibrary cl = (ClientLibrary) cssIter.next();
			ResourceResolver resolver = null;
			try {
				resolver = resolverFactory
						.getAdministrativeResourceResolver(null);
				Node cssNode = resolver.resolve(cl.getPath() + "/css").adaptTo(
						Node.class);
				String repeated = new String(new char[pageDepth-1]).replace("\0",
						"../");
				NodeIterator iter = cssNode.getNodes();
				while (iter.hasNext()) {
					Node cssFile = (Node) iter.next();
					answer.add(repeated + cssFile.getPath().substring(1));
				}
			} catch (Exception e) {
				logger.error("Exception : {}", e);
			} finally {
				if (resolver != null && resolver.isLive()) {
					resolver.close();
				}

			}
		}
		return answer;
	}

	public List<String> outputJsLib(String themeName, int pageDepth) {
		String[] themes = { themeName };
		List<String> answer = new ArrayList<String>();
		LibraryType css = LibraryType.JS;
		Collection<ClientLibrary> libs = htmlLibManager.getThemeLibraries(
				themes, css, themeName, true);
		Iterator<ClientLibrary> jsIter = libs.iterator();
		if (jsIter.hasNext()) {
			ClientLibrary cl = jsIter.next();
			ResourceResolver resolver = null;
			try {
				resolver = resolverFactory
						.getAdministrativeResourceResolver(null);
				Node cssNode = resolver.resolve(cl.getPath() + "/js").adaptTo(
						Node.class);
				String repeated = new String(new char[pageDepth-1]).replace("\0",
						"../");
				NodeIterator iter = cssNode.getNodes();
				while (iter.hasNext()) {
					Node cssFile = (Node) iter.next();
					answer.add(repeated + cssFile.getPath());
				}
			} catch (Exception e) {
				logger.error("Exception : {}", e);
			} finally {
				if (resolver != null && resolver.isLive()) {
					resolver.close();
				}
			}
		}
		return answer;
	}
}
