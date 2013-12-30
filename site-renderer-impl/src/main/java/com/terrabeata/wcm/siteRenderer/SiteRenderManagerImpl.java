package com.terrabeata.wcm.siteRenderer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Dictionary;
import java.util.Enumeration;
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
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
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
import com.terrabeata.wcm.siteRenderer.api.PublisherPropertyConstants;
import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteRenderManager;
import com.terrabeata.wcm.siteRenderer.api.exception.RenderingException;
import com.terrabeata.wcm.siteRenderer.api.job.RenderJobConstants;
import com.terrabeata.wcm.siteRenderer.internal.site.SiteParser;


@Component(immediate=true)
@Service(value={SiteRenderManager.class,EventHandler.class})
@Property(name = EventConstants.EVENT_TOPIC, 
          value=RenderJobConstants.PUBLISH_JOB_TOPIC)
public class SiteRenderManagerImpl implements 
								    SiteRenderManager, EventHandler {
	
	
	public static final String DEFAULT_PUBLISHER_NAME = 
			"terrabeata-renderer-publisher-default";

	public static final String DEFAULT_QUEUE_NAME = 
			"terrabeata-renderer-job-queue";

	private static final Logger log = 
			LoggerFactory.getLogger(SiteRenderManagerImpl.class);
	
	private static final String configurationFilterFormat = 
			"(&(%s=%s) (service.pid=%s*))";
			
//	private static final String queueConfigurationFilter = 
//			String.format(configurationFilterFormat,
//					      new Object[]{"publisher.name",
//		                                DEFAULT_QUEUE_NAME,
//					                    QueueConfiguration.class.getName()});
					     
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
	
	@Reference
	private ResourceResolverFactory resolverFactory;
	
	private ComponentContext context;

	private Hashtable<String, Publisher> publishers;
	
	private SiteParser siteParser;
	
	public SiteRenderManagerImpl() {
		super();
		publishers = new Hashtable<String, Publisher>();
		siteParser = new SiteParser();
	}
	
	//--------------------------------------------------------------------------
	// OSGi methods
	//--------------------------------------------------------------------------
	
	@Activate
	@SuppressWarnings("rawtypes")
	private void activate(ComponentContext context) throws Exception {
		this.context = context;
		
		Dictionary properties = context.getProperties();
		Enumeration keys = properties.keys();
		
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			log.debug("activate:: {} = {}", key, properties.get(key));
		}
		
		ResourceResolver resourceResolver = resolverFactory.getResourceResolver(null);
		
		Resource installData = resourceResolver.getResource("/var/siteRender/i001");
		
		
		
		if (null == installData) {
			addDefaultPublisherConfig();
			addQueueConfig();
			// set i001
		}
	}
	
	@Deactivate
	private void deactivate() {
		
	}
	
	//--------------------------------------------------------------------------
	// EventHandler implementation
	//--------------------------------------------------------------------------

	public void handleEvent(Event event) {
		log.debug("handleEvent::");
		if (EventUtil.isLocal(event))
		{
			String publisherName = OsgiUtil.toString(
					event.getProperty(
						RenderJobConstants.PROPERTY_EVENT_PUBLISHER_NAME), 
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
				if (topics[i] == RenderJobConstants.PUBLISH_JOB_TOPIC)
					return currentQueue;
			}
		}
		log.warn("Unable to find queue for publishing site content.");
		return null;
	}
	
	public void publishTree(ResourceConfiguration resource)
			throws RenderingException {
		if (null == resource)
			throw new IllegalArgumentException(
					"publishTree(ResourceConfiguration): " +
					"resource may not be null");
		Iterator<ResourceConfiguration> configs = 
				siteParser.getTreeResources(resource.getResource(), 
			    resource.getWebsiteConfiguration());
		while(configs.hasNext()) {
			ResourceConfiguration config = configs.next();
			publishResource(config);
		}
	}

	@SuppressWarnings({"rawtypes","unchecked"})
	public void publishResource(ResourceConfiguration resource)
			throws RenderingException {
		if (null == resource)
			throw new IllegalArgumentException(
					"publishResource(ResourceConfiguration):" +
					" argument, resource, may not be null.");
		log.debug("publishResource:: {}", resource.toString());
		
		Map map = resource.adaptTo(Map.class);
		
		map.put(RenderJobConstants.PROPERTY_EVENT_ACTION, 
				RenderJobConstants.ACTION_FILE_ADD);
		map.put(JobUtil.PROPERTY_JOB_TOPIC, 
				RenderJobConstants.PUBLISH_JOB_TOPIC);
		map.put(JobUtil.PROPERTY_JOB_NAME, UUID.randomUUID().toString());

		Event job = new Event(JobUtil.TOPIC_JOB, map);

		
		log.debug("publishResource:: post event");
		eventAdmin.postEvent(job); 
	}

	
	//--------------------------------------------------------------------------
	// Private discovery methods
	//--------------------------------------------------------------------------
	
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
					   new String[] {RenderJobConstants.PUBLISH_JOB_TOPIC});
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
		props.put(PublisherPropertyConstants.PROPERTY_NAME, DEFAULT_PUBLISHER_NAME);
		props.put(PublisherPropertyConstants.PROPERTY_PROTOCOL, "file");
		props.put(PublisherPropertyConstants.PROPERTY_DESTINATION_PATH,
				PublisherPropertyConstants.SLING_HOME_TAG + "/" +
				PublisherPropertyConstants.PUBLISHER_NAME_TAG + "/" +
				PublisherPropertyConstants.WEBSITE_NAME_TAG);
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
				   new String[] {RenderJobConstants.PUBLISH_JOB_TOPIC});
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
