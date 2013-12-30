package com.terrabeata.wcm.siteRenderer.api.job;

/**
 * Constants for the Terra Beata Publishing Suite.
 * 
 * 
 * @author deke@terrabeata.com
 *
 */
public class RenderJobConstants {
	
	/**
	 * The event topic for publish jobs
	 */
	public final static String PUBLISH_JOB_TOPIC = 
			"com/terrabeata/wcm/publish/job";
	
	/**
	 * Publish job property. The PublishManager will
	 * use this value to lookup the Publisher to process the job. This is 
	 * a mandatory value.
	 */
	public final static String PROPERTY_EVENT_PUBLISHER_NAME = 
			"event.publisher.name";
	
	/**
	 * Publish job property. The path to the resource to be published. This is 
	 * a mandatory value for add file action jobs only. Publishers ignore this 
	 * value for all other actions.
	 */
	public final static String PROPERTY_EVENT_RESOURCE_PATH = 
			"event.publisher.resource.path";
	
	/**
	 * Publish job property. The path, relative to the Publisher root path, 
	 * that the item being acted on is/will be. This is a mandatory value.
	 */
	public final static String PROPERTY_EVENT_DESTINATION_PATH = 
			"event.publisher.destination.path";
	
	/**
	 * Publish job property. If true, the Publisher will overwrite the value
	 * at the publish destination even if the file at that location has a 
	 * modification date equal to or more recent than the resource. This is an
	 * optional value for Add action jobs. This value is ignored for all other
	 * actions.
	 */
	public final static String PROPERTY_EVENT_FORCE_OVERWRITE = 
			"event.publisher.force-overwrite";
	
	/**
	 * Publish job property. The action the Publisher should perform for the
	 * job. The is a mandatory property.
	 */
	public final static String PROPERTY_EVENT_ACTION = 
			"event.publisher.action";

	/**
	 * Publish job property. The name of the file when created on the 
	 * destination.
	 */
	public final static String PROPERTY_DESTINATION_FILE_NAME = 
			"event.publisher.destination.filename";
	
	/**
	 * Publish job property. The modification time stamp of the item to be acted
	 * on. If the value, PROPERTY_FORCE_OVERWRITE, is false, then any item at 
	 * publish destination is overwritten if it is older than this date. This
	 * value should be an instance of Long. This value is optional for add file
	 * actions and is ignored for all others.
	 */
	public final static String PROPERTY_EVENT_PAYLOAD_TIMESTAMP = 
			"event.publisher.payload.timestamp";
	
	public final static String PROPERTY_SELECTORS = 
			"event.publisher.payload.selectors";
	
	public final static String PROPERTY_RENDER_SELECTOR = 
			"event.publisher.payload.render.selector";
	
	/**
	 * The Web site name property.
	 */
	public final static String PROPERTY_EVENT_WEBSITE_NAME =
			"event.publisher.website.name";
	
	public final static String PROPERTY_EVENT_SUFFIX = 
			"event.publisher.payload.suffix";
	
	/**
	 * Value for the publish job PROPERTY_ACTION property. Publisher should 
	 * delete the file specified by PROPERTY_FILE_NAME at the destination path.
	 */
	public final static String ACTION_FILE_DELETE = 
			"action.publisher.file.delete";
	
	/**
	 * Value for the publish job PROPERTY_ACTION property. Publisher should 
	 * add the file specified by PROPERTY_FILE_NAME at the destination path.
	 */
	public final static String ACTION_FILE_ADD = 
			"action.publisher.file.add";
	
	/**
	 * Value for the publish job PROPERTY_ACTION property. Publisher should 
	 * delete the directory specified by PROPERTY_FILE_NAME at the destination 
	 * path and all content within it.
	 */
	public final static String ACTION_DIRECTORY_DELETE = 
			"action.publisher.directory.delete";
	
	/**
	 * Value for the publish job PROPERTY_ACTION property. Publisher should 
	 * create the directory specified by PROPERTY_FILE_NAME at the destination 
	 * path if it does not exist.
	 */
	public final static String ACTION_DIRECTORY_CREATE = 
			"action.publisher.directory.add";
	
}
