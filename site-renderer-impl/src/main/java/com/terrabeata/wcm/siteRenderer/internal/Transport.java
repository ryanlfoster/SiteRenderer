package com.terrabeata.wcm.siteRenderer.internal;

import java.io.InputStream;

import org.osgi.service.event.Event;

import com.terrabeata.wcm.siteRenderer.api.Publisher;

public interface Transport {
	
	boolean publishFile(InputStream stream, String fileName, String destinationPath, long deleteBefore);
	boolean publishFile(InputStream stream, String fileName, String destinationPath);
	boolean removeFile(String fileName, String destinationPath);
	boolean createDirectory(String directoryName, String destinationPath);
	boolean deleteDirectory(String directoryName, String destinationPath);
	boolean stopPublish();

	String getGUID();
	Publisher getPublisher();
	
	Event getCurrentJob();
}
