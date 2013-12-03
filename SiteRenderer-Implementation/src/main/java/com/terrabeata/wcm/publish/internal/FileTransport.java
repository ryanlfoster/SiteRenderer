package com.terrabeata.wcm.publish.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrabeata.wcm.publish.api.PublishConstants;
import com.terrabeata.wcm.publish.api.Publisher;

public class FileTransport extends AbstractTransport {
	
	private static final Logger log = 
			LoggerFactory.getLogger(FileTransport.class);
	
	private static final String DEFAULT_ROOT_PATH = 
			                    PublishConstants.SLING_HOME_TAG + "/websites/" + 
		                        PublishConstants.WEBSITE_NAME_TAG;
	
	public FileTransport (Publisher publisher)
	{
		super(publisher);
	}

	//--------------------------------------------------------------------------
	// Override AbstractTransport
	//--------------------------------------------------------------------------
	
	@Override
    @SuppressWarnings("unchecked")
	public boolean publishFile(InputStream stream, String fileName,
			String destinationPath, long deleteBefore) {
		log.info("publishFile:: fileName: {}, " +
				  "destinationPath: {}, deleteBefore: {}",
				  new String[]{fileName,
				               destinationPath,
				               Long.toString(deleteBefore)});
		String filePath = getRootDirectoryPath();
		
		while (destinationPath.startsWith("/"))
			destinationPath = destinationPath.substring(1);
		filePath += destinationPath;
		
		if (filePath.endsWith("/") != true) {
			filePath += "/";
		}
		filePath += fileName;
		
		File newFile = new File(filePath);
		
		newFile.getParentFile().mkdirs();
		
	    OutputStream output = null;
	    
		try {
			if (newFile.exists())
				newFile.delete();
			byte[] input = IOUtils.toByteArray(stream);

			try {
				output = 
					new BufferedOutputStream(new FileOutputStream(filePath));
				output.write(input);
			}
			finally {
				output.close();
			}
	    }
	    catch(FileNotFoundException ex){
	    	log.warn("publishFile:: Unable to publish file, {}. {}", newFile, 
	    			 ex.toString());
	    	return false;
		} catch (IOException e) {
			log.warn("publishFile:: Unable to publish file, {}. {}", newFile, 
					 e.toString());
			e.printStackTrace();
			return false;
		}
		log.debug("publishFile:: successfully wrote {}", filePath);
		return true;
	}

	@Override
	public boolean publishFile(InputStream stream, String fileName,
			String destinationPath) {
		log.debug("publishFile:: fileName: {}, " +
				  "destinationPath: {}, deleteBefore: {}",
				  new String[]{fileName,
				               destinationPath});
		return publishFile(stream, fileName, destinationPath, 0);
	}

	@Override
	public boolean removeFile(String fileName, String destinationPath) {
		String filePath = getRootDirectoryPath();
		while (destinationPath.startsWith("/"))
			destinationPath = destinationPath.substring(1);
		if (destinationPath.endsWith("/") != true)
			destinationPath += "/";
		filePath += destinationPath + fileName;
		
		File file = new File(filePath);
		
		return file.delete();
	}

	@Override
	public boolean createDirectory(String directoryName, 
			String destinationPath) {
		String filePath = getRootDirectoryPath();
		while (destinationPath.startsWith("/"))
			destinationPath = destinationPath.substring(1);
		if (destinationPath.endsWith("/") != true)
			destinationPath += "/";
		filePath += destinationPath + directoryName;
		
		File file = new File(filePath);
		return file.mkdir();
	}

	@Override
	public boolean deleteDirectory(String directoryName, 
			String destinationPath) {
		return removeFile(directoryName, destinationPath);
	}

	@Override
	public boolean stopPublish() {
		log.warn("stopPublish:: method not implemented");
		return true;
	}
	
	//--------------------------------------------------------------------------
	// Utility methods
	//--------------------------------------------------------------------------
	
    @SuppressWarnings("unchecked")
	private String getRootDirectoryPath() {
		Map<String, Object> map = getPublisher().adaptTo(Map.class);
		String rootPath = 
				OsgiUtil.toString(map.get(PublishConstants.PROPERTY_ROOT_DIRECTORY), "");
		
		BundleContext ctx = getPublisher().adaptTo(BundleContext.class);
		String slingHome = ctx.getProperty("sling.home");

		if (null == rootPath || "" == rootPath) {
			rootPath = DEFAULT_ROOT_PATH;
		} 
		rootPath = rootPath.replace(PublishConstants.SLING_HOME_TAG, slingHome);
		rootPath = rootPath.replace(PublishConstants.PUBLISHER_NAME_TAG,
				 OsgiUtil.toString(map.get(PublishConstants.PROPERTY_NAME),"unknown"));
		rootPath = rootPath.replace(PublishConstants.WEBSITE_NAME_TAG, 
				OsgiUtil.toString(map.get(PublishConstants.PROPERTY_EVENT_WEBSITE_NAME),"unknown"));
		
		if (rootPath.endsWith("/") != true) {
			rootPath += "/";
		}
		
		return rootPath;
	}

	
}
