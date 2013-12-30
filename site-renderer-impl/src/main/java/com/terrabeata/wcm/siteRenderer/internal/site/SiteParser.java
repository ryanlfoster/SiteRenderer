package com.terrabeata.wcm.siteRenderer.internal.site;

import java.util.Arrays;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;

import exception.RenderingException;

public class SiteParser {
	
	private static final Logger log = LoggerFactory.getLogger(SiteParser.class);
	
	private MimeTypeService mimeTypeService;
	
	public SiteParser(MimeTypeService mimeTypeService) {
		this.mimeTypeService = mimeTypeService;
	}

	public SiteConfiguration getSiteConfiguration(Resource member) 
			throws RenderingException {
		log.debug("getSiteConfiguration::");
		if (member == null || member.getPath() == null) {
			throw new IllegalArgumentException(
					"Unable to get configuration. Invalid value for resource");
		}
		
		log.debug("getSiteRoot:: member={}", member.getPath());
		
		Resource currentResource = member;
		
		while (null != currentResource) {
			Node node = currentResource.adaptTo(Node.class);
			try {
				if (node.isNodeType(
						        SiteRendererMixinConstants.SITE_RENDER_SITE_MIXIN)) {
					return new WebsiteConfigImpl(currentResource);
				}
			} catch (Throwable e) {
				log.warn("getSiteConfiguration:: Error from isNodeType: {}", 
						                                        e.getMessage());
			}
			currentResource = currentResource.getParent();
		}
		throw new RenderingException("Unable to find site root for " +
					member.getPath());
	}
	
	public Iterator<ResourceConfiguration> getTreeResources(Resource top, 
			SiteConfiguration site) throws RenderingException {
		Resource[] resources = getRenderableChildren(top, site);
		resources = (Resource[])ArrayUtils.add(resources, 0, top);
		ResourceConfiguration[] configs = 
					new ResourceConfiguration[resources.length];
		for (int i = 0; i < resources.length; i++) {
			log.debug("Add resource to iterator: {}", resources[i].getName());
			ResourceConfiguration config = 
					 getResourceConfiguration(resources[i], site);
			if (null != config) {
				configs[i] = config;
			} else {
				log.warn("Unable to create config for resource: {}", 
						resources[i]);
			}
		}
		return Arrays.asList(configs).iterator();
	}
	
	public Iterator<ResourceConfiguration> getSiteResourceConfigurations(
			                                             SiteConfiguration site) 
			                                 throws RenderingException {
		return getTreeResources(site.getSiteRoot(), site);
	}
	
	public ResourceConfiguration getResourceConfiguration(Resource resource) 
			throws RenderingException {
		String name = resource.getName();
		return getResourceConfiguration(resource, name);	
	}
	
	public ResourceConfiguration getResourceConfiguration(Resource resource, 
														  String name) 
			throws RenderingException {
		SiteConfiguration site = getSiteConfiguration(resource);
		return getResourceConfiguration(resource, name, site);
	}
	
	public ResourceConfiguration getResourceConfiguration(Resource resource, 
			SiteConfiguration site) throws RenderingException {
		return getResourceConfiguration(resource, resource.getName(), site);
	}

	public ResourceConfiguration getResourceConfiguration(Resource resource, 
			String name, SiteConfiguration site) {
		String suffix = null;
		// if not a file or a mimetype, treat as html
		if (resource.isResourceType("nt:file")) {
			suffix = "";
		} else if (resource.isResourceType("mix:mimeType")) {
			Node node = resource.adaptTo(Node.class);
			if (null != node) {
				try {
					String mimetype = 
						OsgiUtil.toString(node.getProperty("jcr:mimeType"),"");
					suffix = mimeTypeService.getExtension(mimetype);
				} catch (Throwable e) {
					log.warn("publishResource:: Unable to get mimetype from " +
							 "mix:mimeType resource: {}", resource.getPath());
					e.printStackTrace();
				}
			}
		}
		if (null == suffix) suffix = site.getDefaultSuffix();
		
		ResourceConfiguration resourceConfig = 
				getResourceConfiguration(resource,
						                         name,
						                         suffix,
						                         null,
						                         site);
		return resourceConfig;
	}
	
	public ResourceConfiguration getResourceConfiguration(Resource item, 
			        String suffix, String[] selectors, SiteConfiguration site) {
		return getResourceConfiguration(item, null, suffix, selectors, site);
	}
	
	public ResourceConfiguration getResourceConfiguration(Resource item, 
			String name, String suffix, String[] selectors, 
			                                           SiteConfiguration site) {
		ResourceRenderConfigImpl config = null;
		try {
			config = new ResourceRenderConfigImpl(item);
		} catch (RenderingException e) {
			log.warn("Unable to get resource configuration for {}, error: {}", 
					 item.getPath(), e.getMessage());
			e.printStackTrace();
		}
		config.setFileName(null);
		config.setSuffix(null);
		config.setSelectors(null);
		if (null != site) config.setWebsiteConfiguration(site);
		if (null != name) config.setFileName(name);
		if (null != suffix) config.setSuffix(suffix);
		if (null != selectors) config.setSelectors(selectors);
		
		return config;
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
				
				resources = (Resource[])ArrayUtils.add(resources, 0, child);

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
				log.warn("Unable to adapt resource to a node: {}",resource.getPath());
				return false;
			}
				try {
					if (node.isNodeType(SiteRendererMixinConstants.SITE_RENDER_RESOURCE_MIXIN)) {
						Property ignoreProp = node.getProperty(SiteRendererMixinConstants.RESOURCE_IGNORE);
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
						log.warn("isRenderable: error while call " +
								  "Node.isNodeType: {}", e.getMessage());
						e.printStackTrace();
						return false;
					}
				}

		}
		
		return true;
	}
}
