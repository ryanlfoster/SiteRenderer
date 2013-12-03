package com.terrabeata.wcm.siteRenderer.api;

import java.util.Enumeration;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.event.jobs.Queue;

public interface SiteRenderer {
	
	Enumeration<Publisher> getPublishers();
	void registerPublisher(Publisher publisher);
	void unregisterPublisher(Publisher publisher);
	
	Queue getQueue();
	
	void publishTree(Resource resource, SiteConfiguration website) 
			throws SiteConfigurationException;
	void publishTree(ResourceConfiguration resource) 
			throws SiteConfigurationException;
	void publishTree(Resource resource) 
			throws SiteConfigurationException;

	void publishResource(Resource resource, SiteConfiguration website) 
			throws SiteConfigurationException;
	void publishResource(ResourceConfiguration resource) 
			throws SiteConfigurationException;
	void publishResource(Resource resource) 
			throws SiteConfigurationException;
}
