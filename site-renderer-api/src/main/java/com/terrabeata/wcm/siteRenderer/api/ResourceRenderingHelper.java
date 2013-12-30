package com.terrabeata.wcm.siteRenderer.api;

import java.io.InputStream;

import org.apache.sling.api.resource.Resource;

/**
 * Interface for services which will convert a Sling Resource into an 
 * InputStream that would be the result of an HTTP call to that Resource.
 * 
 * @author deke@terrabeata.com
 *
 */
public interface ResourceRenderingHelper {
	InputStream getInputStream(ResourceConfiguration resource);
}
