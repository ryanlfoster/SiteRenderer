package com.terrabeata.wcm.siteRenderer.internal;

import java.io.InputStream;

import com.terrabeata.wcm.siteRenderer.api.Publisher;

public class FTPTransport extends AbstractTransport {
	
	public FTPTransport (Publisher publisher)
	{
		super(publisher);
	}

	//--------------------------------------------------------------------------
	// Override AbstractTransport
	//--------------------------------------------------------------------------
	
	@Override
	public boolean publishFile(InputStream stream, String fileName,
			String destinationPath, long deleteBefore) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean publishFile(InputStream stream, String fileName,
			String destinationPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeFile(String fileName, String destinationPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createDirectory(String directoryName, String destinationPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteDirectory(String directoryName, String destinationPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stopPublish() {
		// TODO Auto-generated method stub
		return false;
	}

}
