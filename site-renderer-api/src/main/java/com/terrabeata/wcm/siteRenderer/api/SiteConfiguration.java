package com.terrabeata.wcm.siteRenderer.api;

import org.apache.sling.api.resource.Resource;

public interface SiteConfiguration {
	String getPublisherName();
	Resource getSiteRoot();
	String getName();
	String getRenderSelector();
	String[] getIngoreNodeNames();
	String[] getIgnoreNodeTypes();
	String getIndexFileName();
	String getDefaultSuffix();
}
