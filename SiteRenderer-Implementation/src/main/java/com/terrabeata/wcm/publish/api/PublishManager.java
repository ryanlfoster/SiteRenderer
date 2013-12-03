package com.terrabeata.wcm.publish.api;

import java.util.Enumeration;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.event.jobs.Queue;

public interface PublishManager {
	
	Enumeration<Publisher> getPublishers();
	void registerPublisher(Publisher publisher);
	void unregisterPublisher(Publisher publisher);
	
	Queue getQueue();
	
	void publishTree(Resource resource, WebsitePublishingConfiguration website) 
			throws WebsiteConfigurationException;
	void publishTree(ResourcePublishingConfiguration resource) 
			throws WebsiteConfigurationException;
	void publishTree(Resource resource) 
			throws WebsiteConfigurationException;

	void publishResource(Resource resource, WebsitePublishingConfiguration website) 
			throws WebsiteConfigurationException;
	void publishResource(ResourcePublishingConfiguration resource) 
			throws WebsiteConfigurationException;
	void publishResource(Resource resource) 
			throws WebsiteConfigurationException;
}
