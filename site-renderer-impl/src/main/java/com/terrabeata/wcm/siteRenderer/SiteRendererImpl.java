package com.terrabeata.wcm.siteRenderer;

import java.awt.datatransfer.MimeTypeParseException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.mime.MimeTypeProvider;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.JobUtil;
import org.apache.sling.event.jobs.Queue;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.api.SiteRendererConstants;
import com.terrabeata.wcm.siteRenderer.api.SiteRenderer;
import com.terrabeata.wcm.siteRenderer.api.Publisher;
import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfigurationException;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;

@Component(immediate=true, metatype=true)
@Service(value={SiteRenderer.class,EventHandler.class})
@Property(name = EventConstants.EVENT_TOPIC, 
          value=SiteRendererConstants.PUBLISH_JOB_TOPIC)
public class SiteRendererImpl implements 
								    SiteRenderer, EventHandler {
	
	private static final Logger log = 
			LoggerFactory.getLogger(SiteRendererImpl.class);
	
	@Reference
	private JobManager jobManager;
	
	@Reference
	private EventAdmin eventAdmin;
	
	@Reference
	private ResourceResolverFactory resourceResolverFactory;
	
	@Reference
	private MimeTypeService mimeTypeService;

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
		Dictionary properties = context.getProperties();
		Enumeration keys = properties.keys();
		
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			log.debug("activate:: {} = {}", key, properties.get(key));
		}
		
		String slingHome = context.getBundleContext().getProperty("sling.home");
		
		log.debug("slingHome={}",slingHome);
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
					event.getProperty(SiteRendererConstants.PROPERTY_EVENT_PUBLISHER_NAME), 
				     "default");
		
			if (publishers.containsKey(publisherName))
			{
				log.info("handleEvent: use publisher, {}, to process " +
						  "publish job",publisherName);
				
				Publisher publisher = publishers.get(publisherName);
				JobUtil.processJob(event, publisher);
			} else {
				log.warn("handleEvent:: Not able to find publisher, {}", publisherName);
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
		map.put(SiteRendererConstants.PROPERTY_EVENT_PUBLISHER_NAME, publisherName);
		map.put(SiteRendererConstants.PROPERTY_EVENT_DESTINATION_PATH, 
				                          getDestination(resource.getResource(), 
						                  resource.getWebsiteConfiguration()));
		map.put(SiteRendererConstants.PROPERTY_EVENT_WEBSITE_NAME, websiteName);
		map.put(JobUtil.PROPERTY_JOB_TOPIC, SiteRendererConstants.PUBLISH_JOB_TOPIC);
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
	// Private utility methods
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
			log.debug("getSiteRoot:: currentResource={}",currentResource.getName());
			if (currentResource.isResourceType("terrabeata:Website")) {
				log.debug("getSiteRoot:: found root={}",resource.getName());
				return currentResource;
			}
			currentResource = currentResource.getParent();
			
		}
		
		return null;
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
			if (null != siteRoot && siteRoot.isResourceType("terrabeata:Website")) { 
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
					"root resource: "+siteRoot+". Root must be type terrabeata:Website");
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
