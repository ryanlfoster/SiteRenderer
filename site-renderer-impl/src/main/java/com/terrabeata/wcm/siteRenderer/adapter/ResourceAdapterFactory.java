package com.terrabeata.wcm.siteRenderer.adapter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.jcr.Node;

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

import com.terrabeata.exception.ExceptionStringifier;
import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.SiteConfiguration;
import com.terrabeata.wcm.siteRenderer.api.exception.RenderingException;
import com.terrabeata.wcm.siteRenderer.internal.site.MixinConstants;
import com.terrabeata.wcm.siteRenderer.internal.site.ResourceConfigurationImpl;
import com.terrabeata.wcm.siteRenderer.internal.site.SiteConfigurationImpl;
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
public class ResourceAdapterFactory implements AdapterFactory {
	
	private static final Logger log = LoggerFactory
            .getLogger(ResourceAdapterFactory.class);
	
	@Reference
	private MimeTypeService mimeTypeService;

	@SuppressWarnings("unchecked")
	public <AdapterType> AdapterType getAdapter(Object adaptable,
			Class<AdapterType> type) {
		log.debug("getAdapter::");
		if (adaptable == null) {
			log.warn("Unable to adapt null resource.");
			return null;
		} else if (! (adaptable instanceof Resource)) {
			log.warn("Unable to adapt object. Object is type {}", 
					adaptable.getClass().getName());
			return null;
		}
		
		Resource resource = (Resource)adaptable;

		try {
			if (type == SiteConfiguration.class) {
				log.debug("getAdapter:: type is SiteConfiguration");
				return (AdapterType) getSiteConfiguration(resource);
			} else if (type == ResourceConfiguration.class) {
				log.debug("getAdapter:: type is ResourceConfiguration");
				return (AdapterType) new ResourceConfigurationImpl(resource);
			}
		} catch (Throwable e) {
			log.warn("Unable to adapt resource: {}, error: {}, {}, \n{}",
					 new String[]{resource.getPath(),e.getClass().getName(),e.getMessage(),getStackTrace(e)});
			return null;
		}
		log.warn("Unable to adapt resource.");
		return null;
	}
	
	 @Activate
    protected void activate(ComponentContext componentContext) {
		 
	 }
		 
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
    }
    
	private String getStackTrace(Throwable aThrowable) {
	    Writer result = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(result);
	    aThrowable.printStackTrace(printWriter);
	    return result.toString();
	  }
	
	public SiteConfiguration getSiteConfiguration(Resource member) 
			throws RenderingException {
		if (member == null || member.getPath() == null) {
			throw new IllegalArgumentException(
					"Unable to get configuration. Invalid value for resource");
		}
		
		log.debug("getSiteConfiguration:: member={}", member.getPath());
		
		Resource currentResource = member;
		
		while (null != currentResource) {
			Node node = currentResource.adaptTo(Node.class);
			try {
				if (node.isNodeType(
						        MixinConstants.SITE_RENDER_SITE_MIXIN)) {
					return new SiteConfigurationImpl(currentResource);
				}
			} catch (Throwable e) {
				log.warn("Unable to get site configuration for {}. \n{}", 
						 member.getPath(),
						 ExceptionStringifier.stringify(e));
			}
			currentResource = currentResource.getParent();
		}
		throw new RenderingException("Unable to find site root for " +
					member.getPath());
	}


    

}
