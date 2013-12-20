package com.terrabeata.wcm.siteRenderer.internal.site;

import java.util.Iterator;

import org.apache.sling.api.resource.Resource;

import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;

public class ResourceRenderConfigImpl implements ResourceConfiguration {

	private SiteConfiguration websiteConfig;
	private Resource resource;
	private String suffix;
	private String[] selectors;
	private String name;
	
	public ResourceRenderConfigImpl(Resource resource, String name, 
							 String suffix, String[] selectors, 
			                 SiteConfiguration websiteConfig) {
		this.resource = resource;
		this.suffix = suffix;
		this.selectors = selectors;
		this.websiteConfig = websiteConfig;
		this.name = name;
	}

	public SiteConfiguration getWebsiteConfiguration() {
		return websiteConfig;
	}
	
	public String getName() {
		String suffixValue = (null == suffix || "".equals(suffix)) ? "" : "." + suffix;
		String selectorsValue = "";
		String nameValue = name;
		if (null != selectors) {
			for (int i = 0; i < selectors.length; i++) {
				selectorsValue += "." + selectors[i];
			}
		}
		if (null == name || "".equals(name)) {
			nameValue = resource.getName();
		}
		if (suffixValue.length() == 0 && nameValue.contains(".")) {
			int suffixStart = nameValue.lastIndexOf('.');
			nameValue = nameValue.substring(0, suffixStart-1) + selectorsValue + nameValue.substring(suffixStart);
		} else {
			nameValue += selectorsValue + suffixValue;
		}
		
		if (! nameValue.contains(".")) {
			nameValue += ".html";
		}
		return nameValue;
	}

	public Resource getResource() {
		return resource;
	}

	public String getSuffix() {
		return suffix;
	}

	public String[] getSelectors() {
		return selectors;
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

}
