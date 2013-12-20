package com.terrabeata.wcm.siteRenderer.internal.site;

import javax.jcr.Node;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.PublisherImpl;
import com.terrabeata.wcm.siteRenderer.SiteRendererImpl;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfigurationException;

public class WebsiteConfigImpl implements SiteConfiguration {
	
	private static final Logger log = 
			LoggerFactory.getLogger(WebsiteConfigImpl.class);


	private String publisherName;
	private Resource siteRoot;
	private String name;
	
	public WebsiteConfigImpl (Resource siteRoot) 
			                         throws SiteConfigurationException {
		
		Node root = siteRoot.adaptTo(Node.class);
		String siteName = null;
		try {
			siteName = OsgiUtil.toString(
					   root.getProperty("terrabeata:siteName"), null);
		} catch (Throwable e1) {
			throw new SiteConfigurationException("Invalid Website, unable to get value for terrabeata:siteName. " + e1.getMessage());
		}
		
		if (null != siteName) { 
			
			String publisher = SiteRendererImpl.DEFAULT_PUBLISHER_NAME;
			try {
				publisher = OsgiUtil.toString(
					   root.getProperty("terrabeata:publisher"), publisher);
			} catch (Throwable e) {
				log.warn("Error reading publisher value: {}", e.toString());
				e.printStackTrace();
			}
			log.debug("publisher={}",publisher);
			this.publisherName = publisher;
			this.siteRoot = siteRoot;
			this.name = siteRoot.getName();
		} else {
			String rootName = (null != siteRoot) ? siteRoot.getPath() : "null";
			throw new SiteConfigurationException("Invalid Website " +
				"root resource: "+rootName+
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
