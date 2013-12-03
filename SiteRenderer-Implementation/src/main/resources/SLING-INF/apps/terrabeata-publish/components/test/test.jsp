<%@page import="com.terrabeata.wcm.publish.api.WebsitePublishingConfiguration"%>
<%@page import="com.terrabeata.wcm.publish.api.WebsiteConfigurationException"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="com.terrabeata.wcm.publish.api.PublishManager"%>
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

PublishManager publisherManager = sling.getService(PublishManager.class);

final Resource top = resourceResolver.getResource("/content/terrabeata-publish");
final String publisherName = "default";

WebsitePublishingConfiguration website = new WebsitePublishingConfiguration() {
	
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

<%

/* requestName
responseName
resourceName
nodeName
logName
resourceResolverName
slingName
 */
%>
