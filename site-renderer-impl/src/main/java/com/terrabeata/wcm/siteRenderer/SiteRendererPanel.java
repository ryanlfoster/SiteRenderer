package com.terrabeata.wcm.siteRenderer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class SiteRendererPanel extends HttpServlet {
	
	private static final long serialVersionUID = 6417729509320849646L;
	
	private static ServiceRegistration panelRegistration;
	
	public static void registerPanel(BundleContext ctx) {
        if (panelRegistration == null) {
            Dictionary<String, Object> props = new Hashtable<String, Object>();
            props.put("felix.webconsole.label", "siterenderer");
            props.put("felix.webconsole.title", "Site Renderer");

           props.put("sling.core.servletName", "Terra Beata Site Renderer Servlet");

           SiteRendererPanel panel = new SiteRendererPanel();
            panelRegistration = ctx.registerService("javax.servlet.Servlet",
                panel, props);
        }
    }
	
	public static void unregisterPanel() {
        if (panelRegistration != null) {
            panelRegistration.unregister();
            panelRegistration = null;
        }
    }
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
		
		final PrintWriter pw = response.getWriter();
		
		pw.println("<h1>Terra Beata Site Renderer</h1>");

	}

}
