package com.terrabeata.wcm.siteRenderer.api;

import org.apache.sling.api.resource.Resource;

public interface SiteConfiguration {
	String getPublisherName();
	Resource getTopResource();
	String getName();
}
