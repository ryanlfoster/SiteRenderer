package com.terrabeata.wcm.siteRenderer.internal.site;

import java.util.Arrays;
import java.util.Iterator;

import javax.jcr.Node;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfigurationException;
import com.terrabeata.wcm.siteRenderer.api.SiteRendererConstants;

public class SiteParser {
	
	private static final Logger log = LoggerFactory.getLogger(SiteParser.class);
	
	private MimeTypeService mimeTypeService;
	
	public SiteParser(MimeTypeService mimeTypeService) {
		this.mimeTypeService = mimeTypeService;
	}

	public SiteConfiguration getSiteConfiguration(Resource member) 
			throws SiteConfigurationException {
		if (member == null || member.getPath() == null) {
			throw new SiteConfigurationException(
					"Unable to get configuration. Invalid value for resource");
		}
		
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
						return new WebsiteConfigImpl(currentResource);
					}
						
				}
			}
			currentResource = currentResource.getParent();
		}
		return null;
	}
	
	public Iterator<ResourceConfiguration> getTreeResources(Resource top, SiteConfiguration site) throws SiteConfigurationException {
		Resource[] resources = getRenderableChildren(top);
		resources = (Resource[])ArrayUtils.add(resources, 0, top);
		ResourceConfiguration[] configs = new ResourceConfiguration[resources.length];
		for (int i = 0; i < resources.length; i++) {
			ResourceConfiguration config = getResourceConfiguration(resources[i], site);
			configs[i] = config;
		}
		return Arrays.asList(configs).iterator();
	}
	
	public Iterator<ResourceConfiguration> getSiteResourceConfigurations(SiteConfiguration site) throws SiteConfigurationException {
		return getTreeResources(site.getTopResource(), site);
	}
	
	public ResourceConfiguration getResourceConfiguration(Resource resource) 
			throws SiteConfigurationException {
		return getResourceConfiguration(resource, "");	
	}
	
	public ResourceConfiguration getResourceConfiguration(Resource resource, String name) 
			throws SiteConfigurationException {
		SiteConfiguration site = getSiteConfiguration(resource);
		return getResourceConfiguration(resource, name, site);
	}
	
	public ResourceConfiguration getResourceConfiguration(Resource resource, 
			SiteConfiguration site) throws SiteConfigurationException {
		return getResourceConfiguration(resource, "", site);
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
		if (null == suffix) suffix = "html";
		
		ResourceConfiguration resourceConfig = 
				getResourceConfiguration(resource,
						                         "",
						                         suffix,
						                         null,
						                         site);
		return resourceConfig;
	}
	
	public ResourceConfiguration getResourceConfiguration(Resource item, String suffix, String[] selectors, SiteConfiguration site) {
		return getResourceConfiguration(item, null, suffix, selectors, site);
	}
	
	public ResourceConfiguration getResourceConfiguration(Resource item, 
			String name, String suffix, String[] selectors, SiteConfiguration site) {
		return new ResourceRenderConfigImpl(item, name, suffix, selectors, site);
	}

	private Resource[] getRenderableChildren(Resource parent) {
		log.debug("getRenderableChildren:: parent={}",parent.getPath());
		Resource[] resources = new Resource[0];
		Resource[] temp;
		Resource[] childResources;
		Iterator<Resource> children = parent.listChildren();
		while(children.hasNext()) {
			Resource child = children.next();
			resources = (Resource[])ArrayUtils.add(resources, 0, child);
			log.debug("getRenderableChildren:: child={}",child.getPath());
			if (child.getName().equals("jcr:content")) continue;
			log.debug("getRenderableChildren:: adding child");
			childResources = getRenderableChildren(child);
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
		log.debug("getRenderableChildren:: total resource={}", resources.length);
		return resources;
	}

}
