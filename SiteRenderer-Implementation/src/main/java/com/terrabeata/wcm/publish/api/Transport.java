package com.terrabeata.wcm.publish.api;

import java.io.InputStream;

import org.osgi.service.event.Event;

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
