package com.clarionmedia.infinitum.rest;

import java.util.List;

import org.apache.http.NameValuePair;

import com.clarionmedia.infinitum.orm.persistence.TypeAdapter;

/**
 * <p>
 * Facilitates the mapping of Java data types to RESTful web service resource
 * fields.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/21/12
 */
public abstract class RestfulTypeAdapter<T> implements TypeAdapter<T> {

	/**
	 * Maps the given value to the given web service form field.
	 * 
	 * @param value
	 *            the value being mapped
	 * @param field
	 *            the form field being mapped to
	 * @param pairs
	 *            {@code NameValuePairs} containing the data mappings for the
	 *            model
	 */
	public abstract void mapToField(T value, String field, List<NameValuePair> pairs);

	/**
	 * Maps the given {@link Object} value to the given web service form field.
	 * 
	 * @param value
	 *            the value being mapped
	 * @param field
	 *            the form field being mapped to
	 * @param pairs
	 *            {@code NameValuePairs} containing the data mappings for the
	 *            model
	 */
	public abstract void mapObjectToField(Object value, String field, List<NameValuePair> pairs);

}
