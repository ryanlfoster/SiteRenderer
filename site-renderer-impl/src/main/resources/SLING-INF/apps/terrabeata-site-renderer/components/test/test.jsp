<%@page
	import="com.terrabeata.wcm.siteRenderer.api.ResourceConfiguration"%>
<%@page import="org.apache.sling.api.resource.ResourceResolver"%>
<%@page import="com.terrabeata.wcm.siteRenderer.api.SiteConfiguration"%>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@page import="com.terrabeata.wcm.siteRenderer.api.SiteRenderManager"%>
<%@page import="org.apache.sling.api.SlingHttpServletRequest"%>
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
	
%>
<%
	
%><%@ taglib prefix="sling"
	uri="http://sling.apache.org/taglibs/sling/1.0"%>
<%
	
%><sling:defineObjects />
<%
	
%><%@page session="false"%>
<%
	
%><!DOCTYPE html >
<html>
<head>
<title>Publisher Test</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
	<h1>Publisher Test</h1>
	<%
	
%>
	<%
	SiteRenderManager publisherManager = sling.getService(SiteRenderManager.class);

final ResourceResolver rr = resourceResolver;

Resource res = resourceResolver.getResource("/content/terrabeata-site-renderer-sample/sample");

ResourceConfiguration resConfig = res.adaptTo(ResourceConfiguration.class);

if (null == publisherManager) {
%>No publisherManager<% 
	
} else if (null != resConfig)
    publisherManager.publishTree(resConfig);
else {%>
	No Resource!
	<% }
%>

</body>
</html>
