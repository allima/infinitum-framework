/*
 * Copyright (c) 2012 Tyler Treat
 * 
 * This file is part of Infinitum Framework.
 *
 * Infinitum Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Infinitum Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Infinitum Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.clarionmedia.infinitum.context;


/**
 * <p>
 * Contains constants used in {@link InfinitumContext},
 * {@link ContextFactory}, and other supporting classes.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/12/12
 * 
 */
public class ContextConstants {
	
	// Configuration values
	public static final String MODE_ANNOTATIONS = "annotations";
	public static final String MODE_XML = "xml";
	
	// XML element names
	public static final String CONFIG_ELEMENT = "infinitum-configuration";
	public static final String APPLICATION_ELEMENT = "application";
	public static final String SQLITE_ELEMENT = "sqlite";
	public static final String REST_ELEMENT = "rest";
	public static final String DOMAIN_ELEMENT = "domain";
	public static final String PROPERTY_ELEMENT = "property";
	public static final String AUTHENTICATION_ELEMENT = "authentication";
	public static final String MODEL_ELEMENT = "model";
	public static final String BEANS_ELEMENT = "beans";
	public static final String BEAN_ELEMENT = "bean";
	
	// XML attribute names
	public static final String NAME_ATTRIBUTE = "name";
	public static final String DB_NAME_ATTRIBUTE = "dbname";
	public static final String DB_VERSION_ATTRIBUTE = "dbversion";
	public static final String DB_GENERATE_SCHEMA_ATTRIBUTE = "generateSchema";
	public static final String DB_AUTOCOMMIT_ATTRIBUTE = "autocommit";
	public static final String REST_HOST_ATTRIBUTE = "host";
	public static final String CONNECTION_TIMEOUT_ATTRIBUTE = "connectionTimeout";
	public static final String RESPONSE_TIMEOUT_ATTRIBUTE = "responseTimeout";
	public static final String DEBUG_ATTRIBUTE = "debug";
	public static final String MODE_ATTRIBUTE = "mode";
	public static final String RECYCLE_ATTRIBUTE = "recycleCache";
	public static final String DOMAIN_RESOURCE_ATTRIBUTE = "resource";
	public static final String STRATEGY_ATTRIBUTE = "strategy";
	public static final String ENABLED_ATTRIBUTE = "enabled";
	public static final String TOKEN_NAME_ATTRIBUTE = "tokenName";
	public static final String TOKEN_ATTRIBUTE = "token";
	public static final String REF_ATTRIBUTE = "ref";
	public static final String ID_ATTRIBUTE = "id";
	public static final String CLASS_ATTRIBUTE = "class";
	public static final String GENERATOR_ATTRIBUTE = "generator";

	// Error messages
	public static final String CONFIG_NOT_CALLED = "You must call configure before accessing the InfinitumContext!";
	public static final String CONFIG_PARSE_ERROR = "Could not parse configuration file.";
	public static final String CONFIG_PARSE_ERROR_LINE = "Could not parse configuration file at line %d.";
	public static final String SQLITE_DB_NAME_MISSING = "SQLite database name is missing!";
	public static final String REST_HOST_MISSING = "REST host is missing!";
	public static final String AUTH_TOKEN_NAME_MISSING = "Authentication token name is missing!";
	public static final String AUTH_TOKEN_MISSING = "Authentication token is missing!";
	public static final String AUTH_STRAT_MISSING = "Authentication strategy is missing!";
	public static final String MODEL_RESOURCE_MISSING = "Model resource is missing!";
	public static final String BEAN_ID_MISSING_LINE = "Bean is missing ID at line %d.";
	public static final String BEAN_CLASS_MISSING_LINE = "Bean is missing class at line %d.";
	public static final String REST_CLIENT_BEAN_MISSING = "REST client bean is missing!";
	public static final String REST_CLIENT_BEAN_UNRESOLVED = "REST client bean could not be resolved.";

}
