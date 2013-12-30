package com.terrabeata.wcm.siteRenderer.internal.site;

public class MixinConstants {
	public final static String SITE_RENDER_NAMESPACE = "siteRender";
	
	public final static String SITE_RENDER_SITE_MIXIN = 
			SITE_RENDER_NAMESPACE+":Website";
	public final static String SITE_NAME = 
			SITE_RENDER_NAMESPACE+":siteName";
	public final static String PUBLISHER_NAME = 
			SITE_RENDER_NAMESPACE+":publisher";
	public final static String IGNORE_NODE_NAMES = 
			SITE_RENDER_NAMESPACE+":ignoreNodeNames";
	public final static String IGNORE_NODE_TYPES = 
			SITE_RENDER_NAMESPACE+":ignoreNodeTypes";
	public final static String INDEX_FILE_NAME = 
			SITE_RENDER_NAMESPACE+":indexFileName";
	public final static String DEFAULT_SUFFIX = 
			SITE_RENDER_NAMESPACE+":defaultSuffix";
	
	public final static String SITE_RENDER_RESOURCE_MIXIN = 
			SITE_RENDER_NAMESPACE+":ResourceConfig";
	public final static String RESOURCE_SUFFIX = 
			SITE_RENDER_NAMESPACE+":suffix";
	public final static String RESOURCE_IGNORE = 
			SITE_RENDER_NAMESPACE+":ignore";
	public final static String FILE_NAME = 
			SITE_RENDER_NAMESPACE+":destinationFileName";
	
	public final static String RENDER_SELECTOR = 
			SITE_RENDER_NAMESPACE+":renderSelector";
	public final static String PROPERTY_SELECTORS = 
			SITE_RENDER_NAMESPACE+":selectors";

}
