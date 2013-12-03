package com.terrabeata.wcm.publish.api;

import org.apache.sling.api.resource.Resource;

public interface ResourcePublishingConfiguration {
	WebsitePublishingConfiguration getWebsiteConfiguration();
	Resource getResource();
	String getSuffix();
	String[] getSelectors();
}
