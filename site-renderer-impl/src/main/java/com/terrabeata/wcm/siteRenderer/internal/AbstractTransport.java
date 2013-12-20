package com.terrabeata.wcm.siteRenderer.internal;

import java.io.InputStream;
import java.util.UUID;

import org.osgi.service.event.Event;

import com.terrabeata.wcm.siteRenderer.api.Publisher;



public abstract class AbstractTransport implements Transport {
	
	final private Publisher pub;
	final private UUID guid;
	private Event currentJob;
	
	public AbstractTransport(Publisher publisher) {
		pub = publisher;
		guid = UUID.randomUUID();
	}

	abstract public boolean publishFile(InputStream stream, String fileName,
			String destinationPath, long deleteBefore);

	abstract public boolean publishFile(InputStream stream, String fileName,
			String destinationPath);

	abstract public boolean removeFile(String fileName, String destinationPath);

	abstract public boolean createDirectory(String directoryName, String destinationPath);

	abstract public boolean deleteDirectory(String directoryName, String destinationPath);
	
	abstract public boolean stopPublish();

	public String getGUID() {
		return guid.toString();
	}

	public Publisher getPublisher() {
		return pub;
	}
	
	public Event getCurrentJob() {
		return currentJob;
	}
	
	public void clearCurrentJob() {
		currentJob = null;
	}

}
