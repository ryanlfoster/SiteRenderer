package com.terrabeata.wcm.siteRenderer.internal.site;

import org.apache.sling.api.resource.Resource;

import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;

public class ResourceRenderConfigImpl implements ResourceConfiguration {

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
