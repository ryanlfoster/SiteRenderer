package com.terrabeata.wcm.siteRenderer.api;

import java.util.Enumeration;

import org.apache.sling.event.jobs.Queue;

import exception.RenderingException;

public interface SiteRenderer {
	
	Enumeration<Publisher> getPublishers();
	void registerPublisher(Publisher publisher);
	void unregisterPublisher(Publisher publisher);
	
	Queue getQueue();
	
	void publishTree(ResourceConfiguration resource) 
			throws RenderingException;

	void publishResource(ResourceConfiguration resource) 
			throws RenderingException;
}
