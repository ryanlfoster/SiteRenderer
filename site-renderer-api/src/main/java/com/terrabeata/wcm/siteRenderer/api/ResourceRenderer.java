package com.terrabeata.wcm.siteRenderer.api;

import java.io.InputStream;

/**
 * Interface for services which will convert a Sling Resource into an 
 * InputStream that would be the result of an HTTP call to that Resource.
 * 
 * @author deke@terrabeata.com
 *
 */
public interface ResourceRenderer {
	InputStream getInputStream(ResourceConfiguration resource);
}
