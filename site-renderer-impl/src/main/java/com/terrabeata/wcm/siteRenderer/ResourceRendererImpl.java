package com.terrabeata.wcm.siteRenderer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.api.ResourceRenderingHelper;

@Component (
		metatype = true,
		immediate = true,
		label = "Terra Beata Resource Renderer",
		createPid = true
		)
@Service(value={ResourceRenderingHelper.class})
public class ResourceRendererImpl implements ResourceRenderingHelper {
	
	private static final Logger log = 
			LoggerFactory.getLogger(ResourceRendererImpl.class);
	
	//--------------------------------------------------------------------------
	// Configuration properties
	//--------------------------------------------------------------------------

	@Property(description="The host name for the rendering CQ instance", 
			  label="Rendering host", value="localhost")
	static final String HOST = "filerrenderer.host";
	
	@Property(description="The port for the rendering CQ instance", 
			  label="Rendering port", intValue=8080)
	static final String PORT = "filerrenderer.port";
	
	@Property(description="User name to use for rendering", label="User Name")
	static final String USER_NAME = "filerrenderer.username";
	
	@Property(description="User's password to use for rendering", 
			  label="Password")
	static final String PASSWORD = "filerrenderer.password";
	
	@Property(description="Use https for render", label="HTTPS", 
			  boolValue=false)
	static final String HTTPS = "filerrenderer.https";
		
	//--------------------------------------------------------------------------
	// Instance variables
	//--------------------------------------------------------------------------
	
	private String host;
	private int port;
	private String userName;
	private String password;
	private boolean useHTTPS;
	private String pid;
    
    //--------------------------------------------------------------------------
    // OSGi life cycle methods
    //--------------------------------------------------------------------------

	@Activate
	@Modified
	private void activate(ComponentContext context) throws Exception {
		pid = OsgiUtil.toString("service.pid", ""); 
		log.debug("FileRenderer {} activated", pid);
		Dictionary properties = context.getProperties();
		host = OsgiUtil.toString(properties.get(HOST), "localhost");
		port = OsgiUtil.toInteger(properties.get(PORT), 8080);
		userName = OsgiUtil.toString(USER_NAME, "");
		password = OsgiUtil.toString(PASSWORD, "");
		useHTTPS = OsgiUtil.toBoolean(HTTPS, false);
	}
	
	@Deactivate
	private void deactivate() throws Exception {
		log.debug("FileRenderer {} deactivated", pid);
	}
	
	//--------------------------------------------------------------------------
	// FileRenderer implementation
	//--------------------------------------------------------------------------
	
	public InputStream getInputStream(Resource resource, String suffix,
			String[] selectors) {
		log.debug("getInputStream (Resource resource, String suffix, " +
				  "String[] selectors):: resource:{}", resource.getPath());
		if ((suffix == "" || suffix == null) && 
			(selectors == null || selectors.length == 0)) {
			return getInputStream(resource);
		}
		return getHTTPInputStream(resource, suffix, selectors);
	}

	public InputStream getInputStream(Resource resource) {
		log.debug("getInputStream (Resource resource):: resource:{}", 
				  resource.getPath());
		InputStream inputStream = resource.adaptTo(InputStream.class);
		if (inputStream != null) {
			log.debug("getInputStream:: inputStream for {} created from binary",
					  resource.getPath());
			return inputStream;
		}
		inputStream = getHTTPInputStream(resource, null, null);
		if (inputStream == null)
			inputStream = getHTTPInputStream(resource, "html", null);
		return inputStream;
	}
	
	//--------------------------------------------------------------------------
	// HTTP
	//--------------------------------------------------------------------------
	
	private InputStream getHTTPInputStream(Resource resource, String suffix, 
			String[] selectors) {
		log.debug("getHTTPInputStream:: resource: {}, suffix:{}, selectors:{}",
				  new Object[]{resource.getPath(),suffix,selectors});
		
		String uri = (useHTTPS) ? "https://" : "http://";
		uri += host + ":" + Integer.toString(port);
		uri += resource.getPath();
		if (null != selectors) {
			for (int i = 0; i < selectors.length; i++) {
				uri += "." + selectors[i];
			}
		}
		if (null != suffix && suffix != "") uri += "." + suffix;
		
		URL urlObj = null;
		try {
			urlObj = new URL(uri);
			return urlObj.openStream();
		} catch (MalformedURLException e1) {
			log.warn("getHTTPInputStream:: Invalid URL, {}, {}",
					 uri, e1.toString());
			e1.printStackTrace();
		} catch (IOException e) {
			log.warn("getHTTPInputStream:: Error while getting inputStream, " +
					"{}, {}", uri, e.toString());
			e.printStackTrace();
		}
		
		return null;
	}
	

}
