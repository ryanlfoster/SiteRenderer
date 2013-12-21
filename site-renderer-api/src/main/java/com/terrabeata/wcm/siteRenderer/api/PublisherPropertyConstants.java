package com.terrabeata.wcm.siteRenderer.api;

public class PublisherPropertyConstants {
	public static final String PROPERTY_NAME = "publisher.name";
	public static final String PROPERTY_HOST = "publisher.host";
	public static final String PROPERTY_PORT = "publisher.port";
	public static final String PROPERTY_PROTOCOL = "publisher.type";
	public static final String PROPERTY_ROOT_DIRECTORY = "publisher.root.directory";
	public static final String PROPERTY_URL = "publisher.url";
	public static final String PROPERTY_CATEGORY = "publisher.category";
	public static final String PROPERTY_USER_NAME = "publisher.credentials.username";
	public static final String PROPERTY_PASSWORD = "publisher.credentials.password";
	public static final String PROPERTY_DESTINATION_PATH = "publisher.destination.path";
	
	final public static String SLING_HOME_TAG = "{sling.home}";
	final public static String WEBSITE_NAME_TAG = "{website.name}";
	final public static String PUBLISHER_NAME_TAG = "{publisher.name}";

	/**
	 * Value for Publisher protocol. The value is not known.
	 */
	public final static int PROTOCOL_UNKNOWN = 0;
	/**
	 * Value for Publisher protocol. The publisher renders published content to
	 * the file system.
	 */
	public final static int PROTOCOL_FILE = 1;
	/**
	 * Value for Publisher protocol. The publisher renders published content to
	 * a server using the WebDAV protocol.
	 */
	public final static int PROTOCOL_WEBDAV = 100;
	/**
	 * Value for Publisher protocol. The publisher renders published content to
	 * a server using the FTP protocol.
	 */
	public final static int PROTOCOL_FTP = 200;
	/**
	 * Value for Publisher protocol. The publisher renders published content to
	 * a server using the Secure FTP (FTP) protocol.
	 */
	public final static int PROTOCOL_SFTP = 201;
	

}
