package com.terrabeata.wcm.siteRenderer.internal;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.exception.ExceptionStringifier;

public class SiteRendererPersistence {
	
	private static final Logger log = 
			LoggerFactory.getLogger(SiteRendererPersistence.class);
	
	public static final String PERSISTENCE_DIRECTORY = "/var/siteRenderer";
	
	private ResourceResolverFactory resolverFactory; 
	
	public SiteRendererPersistence(ResourceResolverFactory resolverFactory) {
		this.resolverFactory = resolverFactory;
	}
	
	public void initialize() {
		log.info("Initialize Site Render Persistence");
		
		ResourceResolver resourceResolver = null;
		try {
			resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
		} catch (LoginException e1) {
			log.error("initialize:: Unable get resource resolver: {}", ExceptionStringifier.stringify(e1));
		}
		
		Resource dataTop = resourceResolver.getResource("/var");
		try {
			resourceResolver.create(dataTop, "siteRenderer", null);
			resourceResolver.commit();
		} catch (PersistenceException e) {
			log.error("Unable to create persistence directory: {}", ExceptionStringifier.stringify(e));
		}
	}
	
	public ModifiableValueMap getData(String dataPath) {
		
		ResourceResolver resourceResolver = null;
		try {
			resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
		} catch (LoginException e1) {
			log.error("getData:: Unable get resource resolver: {}", ExceptionStringifier.stringify(e1));
		}

		String resPath = dataPath;
		while(resPath.startsWith("/")) {
			resPath = resPath.substring(1);
		}
		dataPath = PERSISTENCE_DIRECTORY + "/" + resPath;
		
		Resource dataRes = resourceResolver.getResource(dataPath);
		
		if (null == dataRes) {
			resourceResolver.close();
			return null;
		}
		
		log.debug("getData:: dataRes is null - {}", (null == dataRes) ? "true" : "false");
		return dataRes.adaptTo(ModifiableValueMap.class);
	}
	

}
