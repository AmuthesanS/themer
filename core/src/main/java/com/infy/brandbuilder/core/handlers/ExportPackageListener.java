package com.infy.brandbuilder.core.handlers;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
@Service(value = EventHandler.class)
@Property(name = EventConstants.EVENT_TOPIC, value = "my/theme/exportPackage")
public class ExportPackageListener implements EventHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void handleEvent(final Event event) {
		logger.debug("Resource event: {} at: {}", event.getTopic(),
				event.getProperty(SlingConstants.PROPERTY_PATH));
	}
}
