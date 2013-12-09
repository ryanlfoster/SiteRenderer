<%@page import="org.apache.sling.api.resource.ResourceResolver"%>
<%@page import="com.terrabeata.wcm.siteRenderer.api.SiteConfiguration"%>
<%@page import="com.terrabeata.wcm.siteRenderer.api.SiteConfigurationException"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="com.terrabeata.wcm.siteRenderer.api.SiteRenderer"%>
<%@page import="org.apache.sling.api.SlingHttpServletRequest"%>
<%@page language="java" contentType="text/html; charset=utf-8" 
    pageEncoding="utf-8" %><%
	
%><%
	
%><%@ taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %><%
	
%><sling:defineObjects /><%
	
%><%@page session="false"%><%
	
%><!DOCTYPE html >
<html>
<head>
	<title>Publisher Test</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<h1>Publisher Test</h1><%
	
%><%
SiteRenderer publisherManager = sling.getService(SiteRenderer.class);
final ResourceResolver rr = resourceResolver;

final Resource top = rr.getResource("/content/terrabeata-publish");
final String publisherName = "default";

SiteConfiguration website = new SiteConfiguration() {
	
	public Resource getTopResource() {
		return top;
	}
	
	public String getPublisherName() {
		return publisherName;
	}
	
	public String getName() {
		return "testSite";
	}
};


Resource res = resourceResolver.getResource("/content/terrabeata-publish/sample");

publisherManager.publishResource(res, website);
%>

</body>
</html>
