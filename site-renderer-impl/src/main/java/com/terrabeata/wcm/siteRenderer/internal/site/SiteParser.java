package com.terrabeata.wcm.siteRenderer.internal.site;

import java.util.Arrays;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.exception.ExceptionStringifier;
import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;
import com.terrabeata.wcm.siteRenderer.api.exception.RenderingException;


public class SiteParser {
	
	private static final Logger log = LoggerFactory.getLogger(SiteParser.class);

	
	public Iterator<ResourceConfiguration> getTreeResources(Resource top, 
			                 SiteConfiguration site) throws RenderingException {
		Resource[] resources = getRenderableChildren(top, site);
		resources = ArrayUtils.add(resources, 0, top);
		ResourceConfiguration[] configs = 
					new ResourceConfiguration[resources.length];
		for (int i = 0; i < resources.length; i++) {
			log.debug("Add resource to tree resources: {}", 
					  resources[i].getName());
			ResourceConfigurationImpl config = 
					                new ResourceConfigurationImpl(resources[i]);
			config.setWebsiteConfiguration(site);
			configs[i] = config;
		}
		return Arrays.asList(configs).iterator();
	}
	
	public Iterator<ResourceConfiguration> getTreeResources(
			                                             SiteConfiguration site) 
			                                 throws RenderingException {
		return getTreeResources(site.getSiteRoot(), site);
	}
	

	private Resource[] getRenderableChildren(Resource parent, 
			                                 SiteConfiguration site) {
		
		log.debug("getRenderableChildren:: parent={}",parent.getPath());
		
		Resource[] resources = new Resource[0];
		Resource[] temp;
		Resource[] childResources;
		Iterator<Resource> children = parent.listChildren();
		
		while(children.hasNext()) {
			Resource child = children.next();
			
			log.debug("getRenderableChildren:: child={}",child.getPath());
			
			if (isRenderable(child, site)) {
				
				resources = ArrayUtils.add(resources, 0, child);

				childResources = getRenderableChildren(child, site);
				temp = new Resource[resources.length+childResources.length];
				int i = 0;
				for (i = 0; i < resources.length; i++) {
					temp[i] = resources[i];
				}
				for (int j = 0; j < childResources.length; j++) {
					temp[i+j] = childResources[j];
				}
				resources = temp;
			}
		}
		return resources;
	}
	
	private boolean isRenderable(Resource resource, SiteConfiguration site) {
		String[] ignoredNames = site.getIngoreNodeNames();
		
		if (null == resource) return false;
		
		Node node = resource.adaptTo(Node.class);
		if (null != ignoredNames){
			for (int i = 0; i < ignoredNames.length; i++) {
				if (ignoredNames[i].equals(resource.getName())) {
					log.debug("Do not render resource. Ignored name: {}, " +
						"resource: {}", ignoredNames[i], resource.getPath());
					return false;
				}
			}
		}
		
		String[] ignoredTypes = site.getIgnoreNodeTypes();
		if (null != ignoredTypes) {
			if (null == node) {
				log.warn("Unable to adapt resource to a node: {}",
						  resource.getPath());
				return false;
			}
				try {
					if (node.isNodeType(MixinConstants.SITE_RENDER_RESOURCE_MIXIN)) {
						Property ignoreProp = node.getProperty(MixinConstants.RESOURCE_IGNORE);
						if (ignoreProp.getBoolean()) return false;
					}
				} catch (PathNotFoundException e1) {
					// do nothing
				} catch (RepositoryException e1) {
					// do nothing
				}
				for (int i = 0; i < ignoredTypes.length; i++) {
					try {
						if (node.isNodeType(ignoredTypes[i])){
							log.debug("Do not render resource. Ignored type: {}, " +
									  "resource: {}", ignoredTypes[i], 
									  resource.getPath());
							return false;
						}
					} catch (RepositoryException e) {
						log.warn("Cannot resolve renderable of node. " +
								 "Got error while detecting node type. {}",
								 ExceptionStringifier.stringify(e));
						e.printStackTrace();
						return false;
					}
				}

		}
		
		return true;
	}
}
