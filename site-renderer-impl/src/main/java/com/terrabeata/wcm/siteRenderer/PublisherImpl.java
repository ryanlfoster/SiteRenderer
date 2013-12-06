package com.terrabeata.wcm.siteRenderer;

import java.io.InputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.api.SiteRendererConstants;
import com.terrabeata.wcm.siteRenderer.api.SiteRenderer;
import com.terrabeata.wcm.siteRenderer.api.Publisher;
import com.terrabeata.wcm.siteRenderer.api.ResourceRenderer;
import com.terrabeata.wcm.siteRenderer.api.Transport;
import com.terrabeata.wcm.siteRenderer.internal.FTPTransport;
import com.terrabeata.wcm.siteRenderer.internal.FileTransport;
import com.terrabeata.wcm.siteRenderer.internal.WebDAVTransport;

@Component (
		label = "Terra Beata Publisher for Site Rendering",
		name = "com.terrabeata.wcm.siteRenderer.PublisherImpl",
		configurationFactory = true,
		metatype = true,
		immediate = true,
		policy=ConfigurationPolicy.REQUIRE
)
//@Service(value={JobProcessor.class})
//@Property(name=JobUtil.PROPERTY_JOB_TOPIC, value="com/terrabeata/sling/publish/publish")

public class PublisherImpl extends SlingAdaptable implements Publisher {
	
	private static final Logger log = LoggerFactory.getLogger(PublisherImpl.class);

	private ComponentContext context;
	
	private Transport transport;
	
	private Dictionary properties;
	
	
	private Event currentJob;
	
	//--------------------------------------------------------------------------
	// Configuration properties
	//--------------------------------------------------------------------------
	
	@Property(description="%publisher.name.description", label="Name")
	static final String NAME = SiteRendererConstants.PROPERTY_NAME;
	
	@Property(description="%publisher.host.description", label="Remote Host")
	static final String HOST = SiteRendererConstants.PROPERTY_HOST;
	
	@Property(description="%publisher.port.description", label="Port")
	static final String PORT = SiteRendererConstants.PROPERTY_PORT;
	
	@Property(description="%publisher.protocol.description", label="Protocol", 
			  value="file",
			options={ 
				@PropertyOption(name="ftp", value="FTP"),
				@PropertyOption(name="sftp", value="SFTP"),
				@PropertyOption(name="webdav", value="WebDAV"),
				@PropertyOption(name="file", value="Local File Directory")
	})
	static final String PROTOCOL = SiteRendererConstants.PROPERTY_PROTOCOL;
	
	@Property(description="%publisher.root.directory.description", 
			  label="Root Directory", 
			  value="{sling.home}/{publisher.name}/{website.name}")
	static final String ROOT_DIRECTORY = SiteRendererConstants.PROPERTY_ROOT_DIRECTORY;
	
	@Property(description="%publisher.url.description", label="Final URL")
	static final String URL = SiteRendererConstants.PROPERTY_URL;
	
	@Property(description="%publisher.category.description", label="Category")
	static final String CATEGORY = SiteRendererConstants.PROPERTY_CATEGORY;
	
	@Property(description="%publisher.credentials.username.description", label="User name")
	static final String USER_NAME = SiteRendererConstants.PROPERTY_USER_NAME;

	@Property(description="%publisher.credentials.password.description", label="User password")
	static final String PASSWORD = SiteRendererConstants.PROPERTY_PASSWORD;

	//--------------------------------------------------------------------------
	// Injected values
	//--------------------------------------------------------------------------
	
	@Reference
	private SiteRenderer publishManager;
	
	@Reference
	private ResourceRenderer resourceRenderer;
	
	@Reference
	private ResourceResolverFactory resourceResolverFactory;
	
	@Reference
	private ServletContext servletContext;
	
	//--------------------------------------------------------------------------
	// OSGi methods
	//--------------------------------------------------------------------------
	
	@Activate
	private void activate(ComponentContext context) throws Exception {
		
		update(context);
		
		log.info("Publisher {} of type {} created for {}, pid:{}", new String[]{getName(), Integer.toString(getProtocol()), getHost(), getPID()});
		
		
		publishManager.registerPublisher(this);
	}

	@Deactivate
	private void deactivate() {
		publishManager.unregisterPublisher(this);
	}
	
	@Modified
	private void update(ComponentContext context) {
		
		properties = context.getProperties();
		this.context = context;
		
		switch (getProtocol())
		{
		case SiteRendererConstants.PROTOCOL_WEBDAV :
			transport = new WebDAVTransport(this);
			break;
		case SiteRendererConstants.PROTOCOL_FTP :
		case SiteRendererConstants.PROTOCOL_SFTP :
			transport = new FTPTransport(this);
			break;
		case SiteRendererConstants.PROTOCOL_FILE :
		default :
			transport = new FileTransport(this);
			break;
		}

	}

	private String getPID() {
		return OsgiUtil.toString(properties.get("service.pid"), "");
	}
	
	private String getComponentID() {
		return OsgiUtil.toString(properties.get("component.id"), "");
	}
	
	//--------------------------------------------------------------------------
	// JobProcessor implementation
	//--------------------------------------------------------------------------
	
	public boolean process(Event job) {
		log.debug("process:: topic={}", job.getTopic());
		
		currentJob = job;
		
		String action =
				OsgiUtil.toString(job.getProperty(SiteRendererConstants.PROPERTY_EVENT_ACTION), "");
		
		log.debug("process:: action={}", action);
		
		if (action == SiteRendererConstants.ACTION_FILE_ADD) {
			
			String[] selectors = null;
			String suffix = null;
			
			log.debug("[1]");
			String resourcePath =
					OsgiUtil.toString(job.getProperty(SiteRendererConstants.PROPERTY_EVENT_RESOURCE_PATH), "");
			String destinationPath =
					OsgiUtil.toString(job.getProperty(SiteRendererConstants.PROPERTY_EVENT_DESTINATION_PATH), "");
			String fileName =
					OsgiUtil.toString(job.getProperty(SiteRendererConstants.PROPERTY_EVENT_FILE_NAME), "");
			ResourceResolver resourceResolver;
			log.debug("[2]");
			try {
				resourceResolver = 
					resourceResolverFactory.getAdministrativeResourceResolver(null);
			} catch (LoginException e) {
				log.debug("process:: Not able to get resource resolver");
				return false;
			}
			Resource resource = resourceResolver.getResource(resourcePath);
			if (null == resource) {
				log.warn("process:: Unable to resolve resource, {}", resourcePath);
				return false;
			}
			log.debug("process:: resource name:{}, fileName:{}",resource.getName(),fileName);
			if (resource.getName() != fileName) { // take for granted selectors and suffix added to file - pull those out
				log.debug("process:: resource name is not equal to filename");
				String[] nameParts = fileName.split("\\.");
				log.debug("process:: nameParts.length={}",Integer.toString(nameParts.length));
				if (nameParts.length > 1) { // has selectors
					
					for (int i = 1; i < (nameParts.length); i++) {
						log.debug("part {} = {}",i,nameParts[i]);
						if (i == (nameParts.length - 1)) {
							log.debug("i={}, {}",i,nameParts[i]);
							suffix = nameParts[i];
						}
						else {
							if (null == selectors)
								selectors = new String[nameParts.length - 2];
							log.debug("-i={}, {}",i,nameParts[i]);
							int which = i - 1;
							selectors[which] = nameParts[i];
						}
						
					}
				}
				
			}
			log.debug("[4]");
	
			InputStream inputStream = resourceRenderer.getInputStream(resource, suffix, selectors);

			if (inputStream == null)
			{
				log.warn("process:: Unable to render resource: {}", resourcePath);
				return false;
			}
			
			log.debug("[7]");
			
			return transport.publishFile(inputStream, fileName, destinationPath);
			
		}
		log.warn("process:: Action not processed: {}", action);
		return false;
	}

	
	//--------------------------------------------------------------------------
	// Publisher implementation
	//--------------------------------------------------------------------------

	public String getHost() {
		return OsgiUtil.toString(properties.get(HOST), "");
	}

	public int getPort() {
		return OsgiUtil.toInteger(properties.get(PORT), 21);
	}

	public int getProtocol() {
		return OsgiUtil.toInteger(properties.get(PROTOCOL), SiteRendererConstants.PROTOCOL_FILE);
	}

	public String getRootDirectory() {
		return OsgiUtil.toString(properties.get(ROOT_DIRECTORY), "/");
	}

	public String getURL() {
		return OsgiUtil.toString(properties.get(URL), "http://"+getHost());
	}
	
	public String getName() {
		return OsgiUtil.toString(properties.get(NAME), "");
	}
	
	public String getCategory() {
		return OsgiUtil.toString(properties.get(CATEGORY), "");
	}
	
	public String getCredentialsUserName() {
		return OsgiUtil.toString(properties.get(USER_NAME), "");
	}

	public String getCredentialsPassword() {
		return OsgiUtil.toString(properties.get(PASSWORD), "");
	}
	
	//--------------------------------------------------------------------------
	// Implements Adaptable
	//--------------------------------------------------------------------------
	
	@Override
    @SuppressWarnings("unchecked")
	public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
		if (type == BundleContext.class) {
			return (AdapterType)context.getBundleContext();
		} else if (type == Map.class) {
			if (properties == null) {
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>(properties.size());
			Enumeration<String> keys = properties.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				map.put(key, properties.get(key));
			}
			if (null != currentJob) {
				String [] jobProps = currentJob.getPropertyNames();
				for (int i = 0; i < jobProps.length; i++)
					map.put(jobProps[i], currentJob.getProperty(jobProps[i]).toString());
			}
			return (AdapterType) map;
		}
		return super.adaptTo(type);
	}



}

