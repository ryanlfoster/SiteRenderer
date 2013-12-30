package com.terrabeata.wcm.siteRenderer.adapter;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;
import com.terrabeata.wcm.siteRenderer.api.jobs.SiteRendererJobConstants;

import exception.RenderingException;

@Component(
        label = "Site Renderer Configuration Adapter Factory",
        description = "Provide an adapter factory for converting Resources to Site Renderer configurations",
        metatype = false,
        immediate = false
)
@Properties({
    @Property(
            label = "Vendor",
            name = Constants.SERVICE_VENDOR,
            value = "Terra Beata",
            propertyPrivate = true
    ),
    @Property(
            label = "Ranking",
            name = Constants.SERVICE_RANKING,
            intValue = 0,
            propertyPrivate = true
    ),
    @Property(
            label = "Adaptables",
            name = "adaptables",
            value = {"com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration"},
            propertyPrivate = true
    ),
    @Property(
            label = "Adapters",
            name = "adapters",
            value = {"java.util.Map"},
            propertyPrivate = true
    )
})
@Service
public class ResourceConfigAdapterFactory implements AdapterFactory {

	private static final Logger log = LoggerFactory
            .getLogger(ResourceConfigAdapterFactory.class);
	
	public <AdapterType> AdapterType getAdapter(Object adaptable,
			Class<AdapterType> type) {
		
		if (!(adaptable instanceof ResourceConfiguration)) {
			log.warn("Unable to adapt object. " +
					 "Adaptable object not compatible: {}",
					 adaptable.getClass().getName());
			return null;
		}
		
		ResourceConfiguration config = (ResourceConfiguration) adaptable;

		if (type == Map.class) { // create a publish job event from adapter
			
			String path = config.getResource().getPath();
			String fileName = config.getResource().getName();
			SiteConfiguration siteConfig = config.getWebsiteConfiguration();
			String websiteName = siteConfig.getName();
			String indexName = siteConfig.getIndexFileName();
			String publisherName = config.getWebsiteConfiguration().
			                                                 getPublisherName();
			String[] selectors = config.getSelectors();
			String renderSelector = config.getRenderSelector();
			String suffix = config.getSuffix();
			String destinationPath = null;
			
			if (config.isDirectory()) {
				fileName = indexName;
			}
			if (null != selectors) {
				for (int i = 0; i < selectors.length; i++) {
					fileName += "." + selectors[i];
				}
			}
			if (null != suffix && suffix.length() > 0) {
				fileName += "." + suffix;
			}
			
			try {
				destinationPath = getDestination(path, siteConfig);
			} catch (RenderingException e) {
				log.warn("Unable to adapt item: error while obtaining " +
						"destination for resource {}, {}", 
						path, e.getMessage());
				e.printStackTrace();
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			if (null != suffix)
				map.put(SiteRendererJobConstants.PROPERTY_EVENT_SUFFIX, 
						suffix);
			if (null != path)
				map.put(SiteRendererJobConstants.PROPERTY_EVENT_RESOURCE_PATH, 
						path);
			if (null != publisherName)
				map.put(SiteRendererJobConstants.PROPERTY_EVENT_PUBLISHER_NAME, 
						publisherName);
			if (null != destinationPath)
			   map.put(SiteRendererJobConstants.PROPERTY_EVENT_DESTINATION_PATH, 
						destinationPath);
			if (null != websiteName)	
				map.put(SiteRendererJobConstants.PROPERTY_EVENT_WEBSITE_NAME, 
						websiteName);
			if (null != fileName)	
				map.put(SiteRendererJobConstants.PROPERTY_DESTINATION_FILE_NAME, 
						fileName);
			if (null != selectors)
				map.put(SiteRendererJobConstants.PROPERTY_SELECTORS, selectors);
			if (null != renderSelector)
				map.put(SiteRendererJobConstants.PROPERTY_RENDER_SELECTOR,
						renderSelector);
			
			return (AdapterType)map;
		}
		
		log.warn("Unable to adapt object. Adapter type not found: {}",
				 type.getClass().getName());
		return null;
	}
	
	private String getDestination(String destinationPath, 
            SiteConfiguration website) 
            								throws RenderingException {
		String websiteTop = website.getSiteRoot().getPath();
		if (destinationPath.startsWith(websiteTop)) {
			return destinationPath.substring(websiteTop.length());
		} else {
			String msg = "Website " + websiteTop +
			   " does not contain " + destinationPath;
			throw new RenderingException(msg);
		}
	}


}
