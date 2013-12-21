package com.terrabeata.wcm.siteRenderer.api;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.event.jobs.JobProcessor;

public interface Publisher extends JobProcessor, Adaptable{
	
	
	String getName();
	String getHost();
	int getPort();
	int getProtocol();
	String getRootDirectory();
	String getURL();
	String getCredentialsUserName();
	String getCredentialsPassword();
	
}
