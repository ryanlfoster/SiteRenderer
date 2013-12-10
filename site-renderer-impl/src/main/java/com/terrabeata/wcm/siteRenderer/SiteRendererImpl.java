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

import javax.jcr.Node;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
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
import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfigurationException;
import com.terrabeata.wcm.siteRenderer.api.SiteRenderer;
import com.terrabeata.wcm.siteRenderer.api.SiteRendererConstants;

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
	private ResourceResolverFactory resourceResolverFactory;
	
	@Reference
	private MimeTypeService mimeTypeService;
	
	@Reference
	private ConfigurationAdmin configAdmin;
	
	private ComponentContext context;

	private Hashtable<String, Publisher> publishers;
	
	public SiteRendererImpl() {
		super();
		publishers = new Hashtable<String, Publisher>();
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
		
		String slingHome = context.getBundleContext().getProperty("sling.home");

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
		publishers.put(publisher.getName(), publisher);
	}

	public void unregisterPublisher(Publisher publisher) {
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
		// TODO Auto-generated method stub
		log.warn("publishTree:: method not implemented");
		
	}

	public void publishTree(Resource resource)
			throws SiteConfigurationException {
		Resource siteRoot = getSiteRoot(resource);
		SiteConfiguration websiteConfig = new WebsiteConfigImpl(siteRoot);
		publishTree(resource, websiteConfig);
	}
	
	public void publishTree(Resource resource, 
			                SiteConfiguration website) 
			throws SiteConfigurationException {
		log.warn("publishTree:: method not implemented");
	}
	

	public void publishResource(ResourceConfiguration resource)
			throws SiteConfigurationException {
		String path = resource.getResource().getPath();
		String fileName = resource.getResource().getName();
		String[] selectors = resource.getSelectors();
		String suffix = resource.getSuffix();
		String websiteName = resource.getWebsiteConfiguration().getName();
		String publisherName = resource.getWebsiteConfiguration().
                                                             getPublisherName();
		log.info("publishResource:: resource: {}, suffix: {}, " +
				   "selectors: {}, website: {}, publisher: {}", 
				    new Object[] {path,
				                  suffix,
				                  selectors,
				                  websiteName,
				                  publisherName}
		          );

		Publisher publisher = publishers.get(publisherName);
		if (null == publisher) {
			String msg = "Publisher " + publisher + " does not exist.";
			throw new SiteConfigurationException(msg);
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(SiteRendererConstants.PROPERTY_EVENT_RESOURCE_PATH, path);
		map.put(SiteRendererConstants.PROPERTY_EVENT_ACTION, 
				SiteRendererConstants.ACTION_FILE_ADD);

		if (null != selectors) {
			for (int i = 0; i < selectors.length; i++) {
				String selector = selectors[i];
				if (null != selector && "" != selector)
					fileName += "."+selector;
			}
		}

		if (null != suffix && "" != suffix) {
			if (suffix.startsWith("."))
				fileName += suffix;
			else
				fileName += "." + suffix;
		}

		map.put(SiteRendererConstants.PROPERTY_EVENT_FILE_NAME, fileName);
		map.put(SiteRendererConstants.PROPERTY_EVENT_PUBLISHER_NAME, 
				                                                 publisherName);
		map.put(SiteRendererConstants.PROPERTY_EVENT_DESTINATION_PATH, 
				                          getDestination(resource.getResource(), 
						                  resource.getWebsiteConfiguration()));
		map.put(SiteRendererConstants.PROPERTY_EVENT_WEBSITE_NAME, websiteName);
		map.put(JobUtil.PROPERTY_JOB_TOPIC, 
				                       SiteRendererConstants.PUBLISH_JOB_TOPIC);
		map.put(JobUtil.PROPERTY_JOB_NAME, UUID.randomUUID().toString());
		
		 
		Event job = new Event(JobUtil.TOPIC_JOB, map);

		log.debug("publishResource:: post event");
		eventAdmin.postEvent(job); 
		
	}

	public void publishResource(Resource resource)
			throws SiteConfigurationException {
		Resource siteRoot = getSiteRoot(resource);
		SiteConfiguration websiteConfig = 
				new WebsiteConfigImpl(siteRoot);
		publishResource(resource, websiteConfig);
	}

	public void publishResource(Resource resource, 
			                    SiteConfiguration websiteConfig) 
			throws SiteConfigurationException{
		String suffix = null;
		// if not a file or a mimetype, treat as html
		if (resource.isResourceType("nt:file")) {
			suffix = "";
		} else if (resource.isResourceType("mix:mimeType")) {
			Node node = resource.adaptTo(Node.class);
			if (null != node) {
				try {
					String mimetype = 
						OsgiUtil.toString(node.getProperty("jcr:mimeType"),"");
					suffix = mimeTypeService.getExtension(mimetype);
				} catch (Throwable e) {
					log.warn("publishResource:: Unable to get mimetype from " +
							 "mix:mimeType resource: {}", resource.getPath());
					e.printStackTrace();
				}
			}
		}
		if (null == suffix) suffix = "html";
		
		ResourceRenderConfigImpl resourceConfig = 
				new ResourceRenderConfigImpl(resource, 
						                         suffix, 
						                         null, 
						                         websiteConfig);
		
		publishResource(resourceConfig);
	}
	
	
	
	//--------------------------------------------------------------------------
	// Private discovery methods
	//--------------------------------------------------------------------------
	
	private String getDestination(Resource resource, 
			                      SiteConfiguration website) 
					throws SiteConfigurationException {
		String websiteTop = website.getTopResource().getPath();
		String resourcePath = resource.getPath();
		if (resourcePath.startsWith(websiteTop)) {
			return resourcePath.substring(websiteTop.length());
		} else {
			String msg = "Website " + websiteTop +
					     " does not contain resource " + resourcePath;
			throw new SiteConfigurationException(msg);
		}
	}
	
	private Resource getSiteRoot(Resource resource) {
		log.debug("getSiteRoot:: resource={}",resource.getName());
		Resource currentResource = resource;
		while(null != currentResource) {
			log.debug("getSiteRoot:: currentResource={}",
					   currentResource.getName());
			if (currentResource.isResourceType("terrabeata:Website")) {
				log.debug("getSiteRoot:: found root={}",resource.getName());
				return currentResource;
			}
			currentResource = currentResource.getParent();
			
		}
		return null;
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
		config.update(props);
	}
	
	
	private String getStackTrace(Throwable aThrowable) {
	    Writer result = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(result);
	    aThrowable.printStackTrace(printWriter);
	    return result.toString();
	  }
	
	
	
	//--------------------------------------------------------------------------
	// Private WebsiteConfiguration implementation
	//--------------------------------------------------------------------------
	
	private class WebsiteConfigImpl implements SiteConfiguration {
		
		private String publisherName;
		private Resource siteRoot;
		private String name;
		
		public WebsiteConfigImpl (Resource siteRoot) 
				                         throws SiteConfigurationException {
			if (null != siteRoot && 
					siteRoot.isResourceType("terrabeata:Website")) { 
				Node root = siteRoot.adaptTo(Node.class);
				String publisher = "default";
				try {
					publisher = OsgiUtil.toString(
						   root.getProperty("terrabeata:publisher"), "default");
				} catch (Throwable e) {
					log.warn("Error reading publisher value: {}", e.toString());
					e.printStackTrace();
				}
				this.publisherName = publisher;
				this.siteRoot = siteRoot;
				this.name = siteRoot.getName();
			} else {
				throw new SiteConfigurationException("Invalid Website " +
					"root resource: "+siteRoot+
					". Root must be type terrabeata:Website");
			}
		}

		public String getPublisherName() {
			return publisherName;
		}

		public Resource getTopResource() {
			return siteRoot;
		}
		
		public String getName() {
			return name;
		}
		
	}
	
	//--------------------------------------------------------------------------
	// Private ResourcePublishingConfiguration implementation
	//--------------------------------------------------------------------------
	
	private class ResourceRenderConfigImpl 
				implements ResourceConfiguration {
		
		private SiteConfiguration websiteConfig;
		private Resource resource;
		private String suffix;
		private String[] selectors;
		
		public ResourceRenderConfigImpl(Resource resource, String suffix, 
				                 String[] selectors, 
				                 SiteConfiguration websiteConfig) {
			this.resource = resource;
			this.suffix = suffix;
			this.selectors = selectors;
			this.websiteConfig = websiteConfig;
		}

		public SiteConfiguration getWebsiteConfiguration() {
			return websiteConfig;
		}

		public Resource getResource() {
			return resource;
		}

		public String getSuffix() {
			return suffix;
		}

		public String[] getSelectors() {
			return selectors;
		}
		
	}
	


	
}
