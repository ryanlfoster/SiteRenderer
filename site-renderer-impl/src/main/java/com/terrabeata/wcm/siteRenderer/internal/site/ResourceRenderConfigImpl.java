package com.terrabeata.wcm.siteRenderer.internal.site;

import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;
import com.terrabeata.wcm.siteRenderer.api.jobs.SiteRendererJobConstants;

import exception.RenderingException;

public class ResourceRenderConfigImpl extends SlingAdaptable 
											 implements ResourceConfiguration {
	
	private static final Logger log = 
			LoggerFactory.getLogger(ResourceRenderConfigImpl.class);
	

	private SiteConfiguration websiteConfig;
	private Resource resource;
	private String suffix = null;
	private String[] selectors = null;
	private String name;
	private String renderSelector = null;
	private boolean ignore = false;
	
	public ResourceRenderConfigImpl(Resource resource) 
			                                   throws RenderingException {
		this.resource = resource;
		setWebsiteConfiguration(resource.adaptTo(SiteConfiguration.class));
	}
	
	public ResourceRenderConfigImpl(Resource resource, Event job) 
			                                  throws RenderingException {
		this(resource);
		
		if (null != job.getProperty(SiteRendererJobConstants.PROPERTY_EVENT_SUFFIX)) {
			setSuffix(job.getProperty(SiteRendererJobConstants.PROPERTY_EVENT_SUFFIX).toString());
		}
		if (null != job.getProperty(SiteRendererJobConstants.PROPERTY_SELECTORS)) {
			setSelectors((String[]) job.getProperty(SiteRendererJobConstants.PROPERTY_SELECTORS));
		}
		if (null != job.getProperty(SiteRendererJobConstants.PROPERTY_DESTINATION_FILE_NAME)) {
			setFileName(job.getProperty(SiteRendererJobConstants.PROPERTY_DESTINATION_FILE_NAME).toString());
		}
		if (null != job.getProperty(SiteRendererJobConstants.PROPERTY_RENDER_SELECTOR)) {
			setRenderSelector(job.getProperty(SiteRendererJobConstants.PROPERTY_DESTINATION_FILE_NAME).toString());
		}
	}
	
	//--------------------------------------------------------------------------
	// Getters - implementation of interface with setters
	//--------------------------------------------------------------------------

	public SiteConfiguration getWebsiteConfiguration() {
		return websiteConfig;
	}
	public void setWebsiteConfiguration(SiteConfiguration value) {
		websiteConfig = value;
		useSiteValues(value);
		useNodeValues(resource);
	}
	
	public String getName() {
		if (null == name) {
			setFileName(resource.getName());
		}
		
		String myName = name;

		if (null != selectors) {
			for (int i =0; i < selectors.length; i++) {
				myName += "." + selectors[i];
			}
		}
		if (null != suffix && suffix.length() > 0) {
			myName += "." + suffix;
		}
		return myName;
	}
	public void setFileName(String value) {
		name = value;
		if (null != name) {
			String[] nameParts = name.split("\\.");
			
			if (nameParts.length > 1) {
				name = nameParts[0];
				suffix = nameParts[nameParts.length-1];
			}
			if (nameParts.length > 2) {
				selectors = new String[nameParts.length-2];
				for (int i = 1; i < nameParts.length-1; i++) {
					selectors[i-1] = nameParts[i];
				}
			}
		}
	}

	public Resource getResource() {
		return resource;
	}

	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String value) {
		suffix = value;
	}

	public String[] getSelectors() {
		return selectors;
	}
	public void setSelectors(String[] value) {
		selectors = value;
	}

	public boolean isDirectory() {
		if (resource.isResourceType("sling:Folder") || 
			resource.isResourceType("nt:nt:folder")) return true;
		if (resource.isResourceType("nt:file")) return false;
		Iterator<Resource> children = resource.listChildren();
		while(children.hasNext()) {
			Resource child = children.next();
			if (child.getName().equals("jcr:content")) continue;
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		String val = "[ResourceRenderConfigImpl resource:\""+getResource().getPath()+"\"";
		val += " name:\"" + getName() + "\"";
		val += " suffix:\"" + getSuffix() + "\"";
		val += " selectors:" + ArrayUtils.toString(getSelectors(), "null");
		val += " isDirectory:" + ((isDirectory()) ? "true" : "false");
		val += "]";
		return val;
	}

	public boolean isIgnore() {
		return ignore;
	}
	public void setIgnore(boolean value) {
		ignore = value;
	}

	public String getRenderSelector() {
		return renderSelector;
	}
	public void setRenderSelector(String value) {
		renderSelector = value;
	}
	
	//--------------------------------------------------------------------------
	// Utility methods to get values from nodes
	//--------------------------------------------------------------------------
	
	private void useSiteValues(SiteConfiguration site) {
		if (isDirectory()) setFileName(site.getIndexFileName());
		suffix = site.getDefaultSuffix();
		renderSelector = site.getRenderSelector();
	}
	
	private void useNodeValues(Resource resource) {
		
		if (! isDirectory()) setFileName(resource.getName());
		
		Node node = resource.adaptTo(Node.class);
		try {
			PropertyIterator props = null;
			if (node.isNodeType(SiteRendererMixinConstants.SITE_RENDER_RESOURCE_MIXIN)) {
				props = node.getProperties(SiteRendererMixinConstants.SITE_RENDER_NAMESPACE+":*");
			}
			if (null != props) {
				while(props.hasNext()) {
					Property currentProperty = props.nextProperty();
					String propName = currentProperty.getName();
					if (propName == SiteRendererMixinConstants.RENDER_SELECTOR) {
						renderSelector = currentProperty.getValue().getString();
					} else if (propName == SiteRendererMixinConstants.RESOURCE_IGNORE) {
						ignore = currentProperty.getValue().getBoolean();
					} else if (propName == SiteRendererMixinConstants.RESOURCE_SUFFIX) {
						suffix = currentProperty.getValue().getString();
					}
				}
			}
		} catch (RepositoryException e) {
			log.warn("Unable to evaluate node: {}, error: {}", resource.getPath(), e.getMessage());
		}
	}

}
