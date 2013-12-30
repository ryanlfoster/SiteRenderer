package com.terrabeata.wcm.siteRenderer.api;

public class PublisherPropertyConstants {
	
	/**
	 * Property to specify to the publisher's unique name. This value is the
	 * publisher's ID.
	 */
	public static final String PROPERTY_NAME = "publisher.name";
	
	/**
	 * The domain name or IP address for the destination server.
	 */
	public static final String PROPERTY_HOST = "publisher.host";
	
	/**
	 * The port for the destination server.
	 */
	public static final String PROPERTY_PORT = "publisher.port";
	
	/**
	 * The protocol to use to place files on the destination.
	 */
	public static final String PROPERTY_PROTOCOL = "publisher.type";
	
	/**
	 * The URL used to preview the site within a Web browser.
	 */
	public static final String PROPERTY_URL = "publisher.url";
	
	/**
	 * The user name to use with the destination server.
	 */
	public static final String PROPERTY_USER_NAME = 
			                                   "publisher.credentials.username";
	
	/**
	 * The password to use with the destination server.
	 */
	public static final String PROPERTY_PASSWORD = 
			                                   "publisher.credentials.password";
	
	/**
	 * The path on the destination server for the root of the site.
	 */
	public static final String PROPERTY_DESTINATION_PATH = 
			                                       "publisher.destination.path";
	
	/**
	 * Path variable to be used with destinations using the FILE protocol. This
	 * variable is replaced by the path to the sling.home directory.
	 */
	final public static String SLING_HOME_TAG = "{sling.home}";
	
	/**
	 * Path variable. Replaced with the name of the Web site.
	 */
	final public static String WEBSITE_NAME_TAG = "{website.name}";
	
	/**
	 * Path variable. Replaced with the publisher name.
	 */
	final public static String PUBLISHER_NAME_TAG = "{publisher.name}";

	/**
	 * Value for Publisher protocol. The rendering destination is not known.
	 */
	public final static int PROTOCOL_UNKNOWN = 0;
	
	/**
	 * Value for Publisher protocol. The publisher renders published content to
	 * the file system.
	 */
	public final static int PROTOCOL_FILE = 10;
	
	/**
	 * Value for Publisher protocol. The publisher renders published content to
	 * a server using the WebDAV protocol.
	 */
	public final static int PROTOCOL_WEBDAV = 20;
	
	/**
	 * Value for Publisher protocol. The publisher renders published content to
	 * a server using the FTP protocol.
	 */
	public final static int PROTOCOL_FTP = 30;
	
	/**
	 * Value for Publisher protocol. The publisher renders published content to
	 * a server using the Secure FTP (FTP) protocol.
	 */
	public final static int PROTOCOL_SFTP = 31;
	

}
