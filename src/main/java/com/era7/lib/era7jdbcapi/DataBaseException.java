
package com.era7.lib.era7jdbcapi;

/**
 * Exceptions thrown interacting with the DB system
 * @author Pablo Pareja Tobes
 *
 */
public class DataBaseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3374261325354521660L;
	public static String SESSION_DRIVEN_CONNECTION_SYSTEM_NEEDED = "The connection retrieving system must be Session driven connections in order to use this method!";
	public static String PROBLEMS_INITIALIZING_DATASOURCE = "There was a problem initializing the DataSource";	
	public static String PROBLEMS_GETTING_CONNECTION_TO_DB = "There was a problem getting the connection to the DataBase";
	
	public static String CLOSE_CONNECTION_ERROR = "There was a problem closing the connection"; 
	
	
	protected String subExceptionMessage = "";

	/**
	 * Constructor
	 * @param message Main message of the exception
	 * @param subMessage Message of the exception which fired the current exception
	 */
	public DataBaseException(String message, String subMessage) {
		super(message);

		subExceptionMessage = subMessage;		
	}


	public DataBaseException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public DataBaseException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
	public String getSubExceptionMessage() {
		return subExceptionMessage;
	}

	public void setSubExceptionMessage(String subExceptionMessage) {
		this.subExceptionMessage = subExceptionMessage;
	}
	
	public String toString(){
		return this.getMessage() + "\n" + this.getSubExceptionMessage();
	}

}
