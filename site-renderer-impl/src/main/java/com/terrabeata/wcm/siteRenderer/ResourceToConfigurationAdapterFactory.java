package com.terrabeata.wcm.siteRenderer;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;
import com.terrabeata.wcm.siteRenderer.internal.site.SiteParser;

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
            value = {"org.apache.sling.api.resource.Resource"},
            propertyPrivate = true
    ),
    @Property(
            label = "Adapters",
            name = "adapters",
            value = {"com.terrabeata.wcm.siteRenderer.api.SiteConfiguration",
            		 "com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration"},
            propertyPrivate = true
    )
})
@Service
public class ResourceToConfigurationAdapterFactory implements AdapterFactory {
	
	private static final Logger log = LoggerFactory
            .getLogger(ResourceToConfigurationAdapterFactory.class);
	
	@Reference
	private MimeTypeService mimeTypeService;
	
	private SiteParser siteParser = new SiteParser(mimeTypeService);

	@SuppressWarnings("unchecked")
	public <AdapterType> AdapterType getAdapter(Object resource,
			Class<AdapterType> type) {
		log.debug("getAdapter::");
		if (resource == null) {
			log.warn("Unable to adapt null resource.");
			return null;
		} else if (! (resource instanceof Resource)) {
			log.warn("Unable to adapt object. Object is type {}", 
					  resource.getClass().getName());
			return null;
		}

		try {
			if (type == SiteConfiguration.class) {
				log.debug("getAdapter:: type is SiteConfiguration");
				return (AdapterType) 
						    siteParser.getSiteConfiguration((Resource)resource);
			} else if (type == ResourceConfiguration.class) {
				log.debug("getAdapter:: type is ResourceConfiguration");
				return (AdapterType) 
						siteParser.getResourceConfiguration((Resource)resource);
			}
		} catch (Throwable e) {
			log.warn("Unable to adapt resource: {}"+e.getMessage());
			return null;
		}
		log.warn("Unable to adapt resource.");
		return null;
	}
	
	 @Activate
    protected void activate(ComponentContext componentContext) {
		 siteParser = new SiteParser(mimeTypeService);
	 }
		 
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
    }
    

}
