package com.terrabeata.wcm.siteRenderer.api;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;

public interface ResourceConfiguration extends Adaptable {
	SiteConfiguration getWebsiteConfiguration();
	Resource getResource();
	String getSuffix();
	String[] getSelectors();
	boolean isDirectory();
	boolean isIgnore();
	String getRenderSelector();
}
