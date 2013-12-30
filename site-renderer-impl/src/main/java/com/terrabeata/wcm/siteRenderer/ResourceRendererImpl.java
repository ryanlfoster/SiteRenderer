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
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration;
import com.terrabeata.wcm.siteRenderer.api.ResourceRenderer;

@Component (
		metatype = true,
		immediate = true,
		label = "Terra Beata Resource Renderer",
		createPid = true
		)
@Service(value={ResourceRenderer.class})
public class ResourceRendererImpl extends SlingAdaptable 
                                                   implements ResourceRenderer {
	
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
	@SuppressWarnings("rawtypes")
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
	
	public InputStream getInputStream(ResourceConfiguration config) {
		Resource resource = config.getResource();
		if (resource.getName().equals(config.getFileName())) {
			InputStream result = resource.adaptTo(InputStream.class);
			if (null != result) return result;
		}
		return getHTTPInputStream(config);
	}
	
	//--------------------------------------------------------------------------
	// HTTP rendering
	//--------------------------------------------------------------------------
	
	private InputStream getHTTPInputStream(ResourceConfiguration config) {
		
		Resource resource = config.getResource();
		String suffix = config.getSuffix();
		String[] selectors = config.getSelectors();
		
		log.debug("getHTTPInputStream:: resource: {}, suffix:{}, selectors:{}",
				  new Object[]{resource.getPath(),suffix,selectors});
		
		String path = resource.getPath();
		int nameIx = path.lastIndexOf(resource.getName());
		path = path.substring(0, nameIx);
		String name = resource.getName().split("\\.")[0];
		
		String uri = (useHTTPS) ? "https://" : "http://";
		uri += host + ":" + Integer.toString(port);
		uri += path + name;
		
		// TODO - clean up selectors and make tolerant of file resources with suffixes
		if (null != selectors) {
			for (int i = 0; i < selectors.length; i++) {
				if (null != selectors[i])
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
