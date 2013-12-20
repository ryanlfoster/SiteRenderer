package com.terrabeata.wcm.siteRenderer.api;

import org.apache.sling.api.resource.Resource;

public interface ResourceConfiguration {
	SiteConfiguration getWebsiteConfiguration();
	Resource getResource();
	String getSuffix();
	String[] getSelectors();
	boolean isDirectory();
}
