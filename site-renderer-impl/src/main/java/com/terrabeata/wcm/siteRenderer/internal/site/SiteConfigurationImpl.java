package com.terrabeata.wcm.siteRenderer.internal.site;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.SiteRenderManagerImpl;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;
import com.terrabeata.wcm.siteRenderer.api.exception.RenderingException;


public class SiteConfigurationImpl implements SiteConfiguration {
	
	private static final Logger log = 
			LoggerFactory.getLogger(SiteConfigurationImpl.class);


	private String publisherName;
	private Resource siteRoot;
	private String name;
	private String renderSelector;
	private String[] ignoreNodeNames;
	private String[] ignoreNodeTypes;
	private String indexFileName;
	private String defaultSuffix;
	
	public SiteConfigurationImpl (Resource siteRoot) 
			                         throws RenderingException {
		
		Node root = siteRoot.adaptTo(Node.class);
		name = getPropertyAsString(root, MixinConstants.SITE_NAME, null);
		
		log.debug("constructor:: name={}", name);
		
		if (null != name) { 
			publisherName = getPropertyAsString(root, 
					              MixinConstants.PUBLISHER_NAME, 
					              SiteRenderManagerImpl.DEFAULT_PUBLISHER_NAME);
			renderSelector = getPropertyAsString(root, 
					MixinConstants.RENDER_SELECTOR, "");
			ignoreNodeNames = getPropertyAsStringArray(root, 
					                          MixinConstants.IGNORE_NODE_NAMES);
			ignoreNodeTypes = getPropertyAsStringArray(root, 
					                          MixinConstants.IGNORE_NODE_TYPES);
			indexFileName = getPropertyAsString(root, 
					                   MixinConstants.INDEX_FILE_NAME, "index");
			defaultSuffix = getPropertyAsString(root, 
					                     MixinConstants.DEFAULT_SUFFIX, "html");
			this.siteRoot = siteRoot;
		} else {
			String rootName = (null != siteRoot) ? siteRoot.getPath() : "null";
			throw new RenderingException("Invalid Website " +
				"root resource: "+rootName+
				". Root must be type terrabeata:Website");
		}
	}

	public String getPublisherName() {
		return publisherName;
	}

	public Resource getSiteRoot() {
		return siteRoot;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRenderSelector() {
		return renderSelector;
	}
	
	public String[] getIngoreNodeNames() {
		return ignoreNodeNames;
	}

	public String[] getIgnoreNodeTypes() {
		return ignoreNodeTypes;
	}

	public String getIndexFileName() {
		return indexFileName;
	}

	public String getDefaultSuffix() {
		return defaultSuffix;
	}

	
	private String getPropertyAsString(Node node, String propName, 
									   String defaultValue){
		try {
			Property prop = node.getProperty(propName);
			return prop.getString();
		} catch (Throwable e) {
			return defaultValue;
		}
	}
	
	private String[] getPropertyAsStringArray(Node node, String propName){
		try {
			Property prop = node.getProperty(propName);
			Value[] values = prop.getValues();
			String[] result = new String[values.length];
			
			for (int i = 0; i < values.length; i++) {
				result[i] = values[i].getString();
			}
			return result;
		} catch (Throwable e) {
			return null;
		}
	}
}
