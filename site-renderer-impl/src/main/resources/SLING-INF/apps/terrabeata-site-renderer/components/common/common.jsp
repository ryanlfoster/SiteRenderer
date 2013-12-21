<%@page import="org.apache.sling.api.resource.ResourceResolver"%>
<%@page import="javax.jcr.Value"%><%
%><%@page import="org.apache.sling.commons.osgi.OsgiUtil"%><%
%><%@page import="javax.jcr.Property"%><%
%><%@page import="javax.jcr.PropertyIterator"%><%
%><%@page import="java.util.Iterator"%><%
%><%@page import="javax.jcr.Node"%><%

%><%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%><%
    
%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0"%><%
%><sling:defineObjects/><%

%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head><title>Sample Content</title></head>
<body>
<h1><%= resource.getPath() %></h1>
<p>This is simple, sample, content..</p>
<p><ul><%
Node myNode = resource.adaptTo(Node.class);
PropertyIterator props = myNode.getProperties();
String propNames = "";
while(props.hasNext()){
	%><li><% 
	Property prop = props.nextProperty();
	
	%><%= prop.getName() %>=<% 
	String val = null;
	if (prop.isMultiple()) {
		Value[] propValue = prop.getValues();
		val = "";
		for (int i = 0; i < propValue.length; i++) {
			Value value = propValue[i];
			if (i != 0) val += ",";
			val += value.getString();
		}
	} else {
		val = prop.getString();
		
	}
	%><%= val %><% 
	%></li><%
}

%>
</ul>
</body>
</html>