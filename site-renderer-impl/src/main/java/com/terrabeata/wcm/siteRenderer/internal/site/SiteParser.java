package com.terrabeata.wcm.siteRenderer.internal.site;

import java.util.Iterator;
import java.util.Map;

import javax.management.Query;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.sling.event.impl.jobs.jcr.JCRHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.corba.se.impl.orbutil.graph.Node;
import com.terrabeata.wcm.siteRenderer.PublisherImpl;
import com.terrabeata.wcm.siteRenderer.api.SiteRendererConstants;

public class SiteParser {
	
	private static final Logger log = LoggerFactory.getLogger(SiteParser.class);
	
	private ResourceResolver resourceResolver;
	
	public SiteParser(ResourceResolver resourceResolver) {
		this.resourceResolver = resourceResolver;
	}

	public Resource getSiteRoot(Resource member) {
		log.debug("getSiteRoot:: member={}", member.getPath());
		
		Resource currentResource = member;
		
		while (null != currentResource) {
			ValueMap properties = currentResource.adaptTo(ValueMap.class);
			if (properties.containsKey("jcr:mixinTypes")) {
				String[] mixins =
						properties.get("jcr:mixinTypes", String[].class);
				for (int i = 0; i < mixins.length; i++) {
					if (SiteRendererConstants.WEBSITE_ROOT_MIXIN.
							                                equals(mixins[i])) {
						log.debug("Found site root: {}", 
								   currentResource.getParent());
						return currentResource;
					}
						
				}
			}
			currentResource = currentResource.getParent();
		}
		
		return null;
	}
	

}
