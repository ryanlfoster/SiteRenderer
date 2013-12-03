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
<p>This is simple, sample, content. Is siteRoot: <%= resource.isResourceType("terrabeata:Website") %>.</p>
<p><%
Node myNode = resource.adaptTo(Node.class);
PropertyIterator props = myNode.getProperties();
String propNames = "|";
while(props.hasNext()){
	Property prop = props.nextProperty();
	
	if ("jcr:mixinTypes".equals(prop.getName()) )
	{
		propNames+="jcr:mixinTypes=";
		Value[] propValue = prop.getValues();
		propNames += "-- has " + propValue.length + " items---";
		for (int i = 0; i < propValue.length; i++) 
			propNames += "*"+propValue[i].getString()+"*";
		propNames += "|";
	} else {
		
		propNames += prop.getName() + "=" + prop.getString() + "|";
	}
}

%>

<%= propNames %>
</body>
</html>