package com.terrabeata.wcm.siteRenderer.internal.site;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

public class DefaultConfigurations {
	private static final String PERSISTENT_DATA_PATH = "/var/siteRender/intallDate";
	
	public void createDefaultConfigurations(ResourceResolver resourceResolver) {
		Resource installData = resourceResolver.getResource("/var/siteRender/i001");
		if (installData != null) {
			
		}
	}
	
	public void createDefaultQueue(ResourceResolver resourceResolver) {
		
	}
}
