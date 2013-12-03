package com.terrabeata.wcm.publish.api;

import org.apache.sling.api.resource.Resource;

public interface WebsitePublishingConfiguration {
	String getPublisherName();
	Resource getTopResource();
	String getName();
}
