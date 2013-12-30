package com.terrabeata.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionStringifier {

	public static String stringify(Throwable e) {
		String result = ExceptionUtils.getMessage(e) + "\n" +
				        ExceptionUtils.getStackTrace(e);
		Throwable rootCause = ExceptionUtils.getRootCause(e);
		if (null != rootCause) {
			result += ExceptionUtils.getMessage(rootCause) + "\n" +
		              ExceptionUtils.getStackTrace(rootCause);
		}
		return result;
	}
}
