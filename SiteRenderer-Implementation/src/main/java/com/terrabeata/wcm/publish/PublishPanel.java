package com.terrabeata.wcm.publish;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class PublishPanel extends HttpServlet {
	private static final long serialVersionUID = 6417729509320849646L;
	
	private static ServiceRegistration panelRegistration;
	
	public static void registerPanel(BundleContext ctx) {
        if (panelRegistration == null) {
            Dictionary<String, Object> props = new Hashtable<String, Object>();
            props.put("felix.webconsole.label", "websitepublish");
            props.put("felix.webconsole.title", "Website Publishing");

           props.put("sling.core.servletName", "Terra Beata Website Publishing Servlet");

           PublishPanel panel = new PublishPanel();
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
		
		pw.println("<h1>Terra Beata Website Publishing Servlet");

	}

}
