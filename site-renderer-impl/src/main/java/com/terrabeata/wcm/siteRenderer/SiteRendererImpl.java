package com.terrabeata.wcm.siteRenderer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.JobUtil;
import org.apache.sling.event.jobs.Queue;
import org.apache.sling.event.jobs.QueueConfiguration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.api.Publisher;
import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfigurationException;
import com.terrabeata.wcm.siteRenderer.api.SiteRenderer;
import com.terrabeata.wcm.siteRenderer.api.SiteRendererConstants;
import com.terrabeata.wcm.siteRenderer.internal.site.SiteParser;
import com.terrabeata.wcm.siteRenderer.internal.site.WebsiteConfigImpl;

@Component(immediate=true)
@Service(value={SiteRenderer.class,EventHandler.class})
@Property(name = EventConstants.EVENT_TOPIC, 
          value=SiteRendererConstants.PUBLISH_JOB_TOPIC)
public class SiteRendererImpl implements 
								    SiteRenderer, EventHandler {
	
	
	public static final String DEFAULT_PUBLISHER_NAME = 
			"terrabeata-renderer-publisher-default";

	public static final String DEFAULT_QUEUE_NAME = 
			"terrabeata-renderer-job-queue";

	private static final Logger log = 
			LoggerFactory.getLogger(SiteRendererImpl.class);
	
	private static final String configurationFilterFormat = 
			"(&(%s=%s) (service.pid=%s*))";
			
	private static final String queueConfigurationFilter = 
			String.format(configurationFilterFormat,
					      new Object[]{"publisher.name",
		                                DEFAULT_QUEUE_NAME,
					                    QueueConfiguration.class.getName()});
					     
	private static final String publisherConfigurationFilter =
			String.format(configurationFilterFormat,
					  new Object[]{"publisher.name",
					                DEFAULT_PUBLISHER_NAME,
				                    PublisherImpl.class.getName()});
	
	@Reference
	private JobManager jobManager;
	
	@Reference
	private EventAdmin eventAdmin;
	
	@Reference
	private MimeTypeService mimeTypeService;
	
	@Reference
	private ConfigurationAdmin configAdmin;
	
	private ComponentContext context;

	private Hashtable<String, Publisher> publishers;
	
	private SiteParser siteParser;
	
	public SiteRendererImpl() {
		super();
		publishers = new Hashtable<String, Publisher>();
		siteParser = new SiteParser(mimeTypeService);
	}
	
	//--------------------------------------------------------------------------
	// OSGi methods
	//--------------------------------------------------------------------------
	
	@Activate
	private void activate(ComponentContext context) throws Exception {
		this.context = context;
		
		Dictionary properties = context.getProperties();
		Enumeration keys = properties.keys();
		
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			log.debug("activate:: {} = {}", key, properties.get(key));
		}
		
		confirmConfigurations();
	}
	
	@Deactivate
	private void deactivate() {
		
	}
	
	//--------------------------------------------------------------------------
	// EventHandler implementation
	//--------------------------------------------------------------------------

	public void handleEvent(Event event) {
		if (EventUtil.isLocal(event))
		{
			String publisherName = OsgiUtil.toString(
					event.getProperty(
						SiteRendererConstants.PROPERTY_EVENT_PUBLISHER_NAME), 
						DEFAULT_PUBLISHER_NAME);
		
			if (publishers.containsKey(publisherName))
			{
				log.info("handleEvent: use publisher, {}, to process " +
						  "publish job",publisherName);
				
				Publisher publisher = publishers.get(publisherName);
				confirmQueue();
				JobUtil.processJob(event, publisher);
			} else {
				log.warn("handleEvent:: Not able to find publisher, {}", 
						 publisherName);
			}
		}
	}

	//--------------------------------------------------------------------------
	// PublishManager implementation
	//--------------------------------------------------------------------------

	public Enumeration<Publisher> getPublishers() {
		return publishers.elements();
	}

	public void registerPublisher(Publisher publisher) {
		log.debug("registerPublisher:: publisher={}", publisher.getName());
		publishers.put(publisher.getName(), publisher);
	}

	public void unregisterPublisher(Publisher publisher) {
		log.debug("unregisterPublisher:: publisher={}", publisher.getName());
		publishers.remove(publisher.getName());
	}
	
	public Queue getQueue() {
		Iterator<Queue> queues = jobManager.getQueues().iterator();
		while(queues.hasNext()) {
			Queue currentQueue = queues.next();
			String[] topics = currentQueue.getConfiguration().getTopics();
			for (int i = 0; i < topics.length; i++) {
				if (topics[i] == SiteRendererConstants.PUBLISH_JOB_TOPIC)
					return currentQueue;
			}
		}
		return null;
	}
	
	public void publishTree(ResourceConfiguration resource)
			throws SiteConfigurationException {
		Iterator<ResourceConfiguration> configs = 
				siteParser.getTreeResources(resource.getResource(), 
			    resource.getWebsiteConfiguration());
		while(configs.hasNext()) {
			publishResource(configs.next());
		}
	}

	public void publishTree(Resource resource) 
			                throws SiteConfigurationException {
		ResourceConfiguration config = 
				                  resource.adaptTo(ResourceConfiguration.class);
		publishTree(config);
	}
	
	public void publishTree(SiteConfiguration website) 
			throws SiteConfigurationException {
		publishTree(website.getSiteRoot());
	}
	
	public void publishTree(Resource resource, SiteConfiguration site) 
			throws SiteConfigurationException {
		ResourceConfiguration config = 
							siteParser.getResourceConfiguration(resource, site);
		publishTree(config);
	}
	

	public void publishResource(ResourceConfiguration resource)
			throws SiteConfigurationException {
		log.debug("publishResource:: {}", resource.toString());
		String path = resource.getResource().getPath();
		String fileName = resource.getResource().getName();
		SiteConfiguration siteConfig = resource.getWebsiteConfiguration();
		String websiteName = siteConfig.getName();
		String defaultSuffix = "." + siteConfig.getDefaultSuffix();
		String indexName = siteConfig.getIndexFileName();
		String publisherName = resource.getWebsiteConfiguration().
                                                             getPublisherName();

		if (resource.isDirectory()) {
			fileName = indexName+ defaultSuffix;
		} else {
			path = path.substring(0, path.lastIndexOf('/'));
			int lastIndex = fileName.lastIndexOf('.');
			if (lastIndex == -1) {
				fileName += defaultSuffix;
			} else if (lastIndex == (fileName.length()-1)) {
				fileName += defaultSuffix;
			}
		}
		
		log.debug("publishResource:: resource={}",resource.getResource().getPath());
		
		String destinationPath = getDestination(path, siteConfig);
		
		log.debug("publishResource:: {},{},{},{},{}", new String[] {
														path, fileName,websiteName,resource.getResource().getPath(),destinationPath
													}
													);

		if (null == publisherName) publisherName = DEFAULT_PUBLISHER_NAME;
		Publisher publisher = publishers.get(publisherName);
		if (null == publisher) {
			String msg = "Publisher " + publisher + " does not exist.";
			throw new SiteConfigurationException(msg);
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(SiteRendererConstants.PROPERTY_EVENT_RESOURCE_PATH, path);
		map.put(SiteRendererConstants.PROPERTY_EVENT_ACTION, 
				SiteRendererConstants.ACTION_FILE_ADD);

		map.put(SiteRendererConstants.PROPERTY_EVENT_PUBLISHER_NAME, 
				                                                 publisherName);
		map.put(SiteRendererConstants.PROPERTY_EVENT_DESTINATION_PATH, 
				                                               destinationPath);
		map.put(SiteRendererConstants.PROPERTY_EVENT_WEBSITE_NAME, websiteName);
		map.put(SiteRendererConstants.PROPERTY_DESTINATION_FILE_NAME, fileName);
		map.put(JobUtil.PROPERTY_JOB_TOPIC, 
				                       SiteRendererConstants.PUBLISH_JOB_TOPIC);
		map.put(JobUtil.PROPERTY_JOB_NAME, UUID.randomUUID().toString());
		
		Event job = new Event(JobUtil.TOPIC_JOB, map);

		log.debug("publishResource:: post event");
		eventAdmin.postEvent(job); 
	}

	public void publishResource(Resource resource)
			throws SiteConfigurationException {
		SiteConfiguration site = siteParser.getSiteConfiguration(resource);
		log.debug("publishResource[1]:: websiteConfig.getPublisherName(){}", site.getPublisherName());
		publishResource(resource, site);
	}

	public void publishResource(Resource resource, 
			                    SiteConfiguration websiteConfig) 
			throws SiteConfigurationException{
		log.debug("publishResource[2]:: websiteConfig.getPublisherName(){}", websiteConfig.getPublisherName());
		ResourceConfiguration resourceConfig = 
				siteParser.getResourceConfiguration(resource, 
						                            websiteConfig);
		publishResource(resourceConfig);
	}
	
	//--------------------------------------------------------------------------
	// Private discovery methods
	//--------------------------------------------------------------------------
	
	private String getDestination(String destinationPath, 
			                      SiteConfiguration website) 
					throws SiteConfigurationException {
		String websiteTop = website.getSiteRoot().getPath();
		if (destinationPath.startsWith(websiteTop)) {
			return destinationPath.substring(websiteTop.length());
		} else {
			String msg = "Website " + websiteTop +
					     " does not contain " + destinationPath;
			throw new SiteConfigurationException(msg);
		}
	}
	
	private void confirmQueue() {
		if (null != getQueue()) return;

		try {
			
			// get the bundle location for Sling event - will bind to that
			BundleContext bctx = context.getBundleContext();
			Bundle[] bundles = bctx.getBundles();
			String bundleLocation = null;
			
			for (int i = 0; i < bundles.length; i++) {
				Bundle bundle = bundles[i];
				if ("org.apache.sling.event".equals(bundle.getSymbolicName())) {
					bundleLocation = bundle.getLocation();
					break;
				}
			}
			
			Configuration config = 
					configAdmin.createFactoryConfiguration(
							QueueConfiguration.class.getName(), bundleLocation);
			// create default queue
			Dictionary<String, Object> props = new Hashtable<String, Object>();
			props.put("queue.name", DEFAULT_QUEUE_NAME);
			props.put("queue.maxparallel", 15);
			props.put("queue.priority", "NORM");
			props.put("queue.retries", 10);
			props.put("queue.retrydelay", 2000);
			props.put("queue.runlocal", true);
			props.put("queue.topics", 
					   new String[] {SiteRendererConstants.PUBLISH_JOB_TOPIC});
			props.put("queue.type", "ORDERED");
			config.update(props);
			log.debug("confirmQueue:: Queue created");
		} catch (IOException e) {
			log.error("Unable find or create queue to handle publish jobs");
		}
	}
	
	private void confirmConfigurations() {
		log.debug("confirmConfigurations:: ");
		// Making it so this bundle can use queue configuration is 
		// accessible to event bundle AND be detectable by this bundle is 
		// problematic. Look for publish config - if exists, assume queue
		// config does too
		try {
			Configuration[] configurations = 
				   configAdmin.listConfigurations(publisherConfigurationFilter);
			if (null != configurations && configurations.length > 0) {
				log.debug("confirmDefaultPublisher:: " +
						  "default publisher exists.");
				return;
			}
		} catch (Throwable e1) {
			log.warn("confirmDefaultPublisher:: Error while retrieving " +
					 "default publisher: {}\n {}", 
					e1.toString(), getStackTrace(e1));
			e1.printStackTrace();
		}
		
		try {
			addQueueConfig();
		} catch (IOException e) {
			log.error("Error while creating queue configuration: {}", 
					  getStackTrace(e));
			e.printStackTrace();
			// do not add publisher if error
			return;
		}
		
		try {
			addDefaultPublisherConfig();
		} catch (IOException e) {
			log.error("Error while creating default publisher: {}",
					  getStackTrace(e));
			e.printStackTrace();
		}
	}
	
	private void addDefaultPublisherConfig() throws IOException{
		Configuration config = 
				configAdmin.createFactoryConfiguration(
						PublisherImpl.class.getName());
		// create default queue
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put(SiteRendererConstants.PROPERTY_NAME, 
				DEFAULT_PUBLISHER_NAME);
		props.put(SiteRendererConstants.PROPERTY_PROTOCOL, "file");
		props.put(SiteRendererConstants.PROPERTY_EVENT_DESTINATION_PATH,
				SiteRendererConstants.SLING_HOME_TAG + "/" +
		        SiteRendererConstants.PUBLISHER_NAME_TAG + "/" +
			    SiteRendererConstants.WEBSITE_NAME_TAG);
		config.update(props);
	}
	
	private void addQueueConfig() throws IOException {
		// get the bundle location for Sling event - will bind to that
		BundleContext bctx = context.getBundleContext();
		Bundle[] bundles = bctx.getBundles();
		String bundleLocation = null;
		
		for (int i = 0; i < bundles.length; i++) {
			Bundle bundle = bundles[i];
			if ("org.apache.sling.event".equals(bundle.getSymbolicName())) {
				bundleLocation = bundle.getLocation();
				break;
			}
		}
		
		Configuration config = 
				configAdmin.createFactoryConfiguration(
						QueueConfiguration.class.getName(), bundleLocation);
		// create default queue
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("queue.name", DEFAULT_QUEUE_NAME);
		props.put("queue.maxparallel", 15);
		props.put("queue.priority", "NORM");
		props.put("queue.retries", 10);
		props.put("queue.retrydelay", 2000);
		props.put("queue.runlocal", true);
		props.put("queue.topics", 
				   new String[] {SiteRendererConstants.PUBLISH_JOB_TOPIC});
		props.put("queue.type", "ORDERED");
		log.debug("addQueueConfig - config added");
		config.update(props);
	}
	
	
	private String getStackTrace(Throwable aThrowable) {
	    Writer result = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(result);
	    aThrowable.printStackTrace(printWriter);
	    return result.toString();
	  }
	
	

	
}
