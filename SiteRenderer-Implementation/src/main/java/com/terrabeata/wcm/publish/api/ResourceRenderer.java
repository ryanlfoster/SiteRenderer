package com.terrabeata.wcm.publish.api;

import java.io.InputStream;

import org.apache.sling.api.resource.Resource;

/**
 * Interface for services which will convert a Sling Resource into an 
 * InputStream that would be the result of an HTTP call to that Resource.
 * 
 * @author deke@terrabeata.com
 *
 */
public interface ResourceRenderer {
	
	InputStream getInputStream(Resource resource, String suffix, 
			                   String[] selectors);
	InputStream getInputStream(Resource resource);
}
