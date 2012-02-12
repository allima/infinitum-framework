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

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.clarionmedia.infinitum.exception.InfinitumConfigurationException;

/**
 * <p>
 * Provides access to an {@link ApplicationContext} singleton. In order for this
 * class to work, an infinitum.cfg.xml file must be created and
 * <code>configure</code> must be called using the location of the XML file
 * before accessing the <code>ApplicationContext</code> or an
 * {@link InfinitumConfigurationException} will be thrown.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 */
public class ApplicationContextFactory {

	private static ApplicationContext sApplicationContext;
	private static boolean sConfigured = false;

	/**
	 * Configures Infinitum with the specified configuration file. Configuration
	 * file must be named infinitum.cfg.xml. This method must be called before
	 * attempting to retrieve an {@link ApplicationContext}.
	 * 
	 * @param configPath
	 *            the path to infinitum.cfg.xml
	 * @throws InfinitumConfigurationException
	 *             thrown if the configuration file could not be found or if the
	 *             file could not be parsed
	 */
	public static void configure(String configPath) throws InfinitumConfigurationException {
		File config = new File(configPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			builder = dbFactory.newDocumentBuilder();
			doc = builder.parse(config);
		} catch (IOException e) {
			throw new InfinitumConfigurationException("The specified configuration file ( " + configPath
					+ ") could not be found.");
		} catch (ParserConfigurationException e) {
			throw new InfinitumConfigurationException("The specified configuration file ( " + configPath
					+ ") could not be parsed.");
		} catch (SAXException e) {
			throw new InfinitumConfigurationException("The specified configuration file ( " + configPath
					+ ") could not be parsed.");
		}
		doc.getDocumentElement().normalize();
		NodeList nl = doc.getChildNodes();
		sConfigured = true;
	}

	/**
	 * Retrieves the {@link ApplicationContext} singleton.
	 * <code>configure</code> must be called before using this method.
	 * Otherwise, an {@link InfinitumConfigurationException} will be thrown.
	 * 
	 * @return the ApplicationContext singleton
	 * @throws InfinitumConfigurationException
	 *             thrown if <code>configure</code> was not called
	 */
	public static ApplicationContext getApplicationContext() throws InfinitumConfigurationException {
		if (!sConfigured || sApplicationContext == null)
			throw new InfinitumConfigurationException(
					"You must call configure before accessing the ApplicationContext!");
		return sApplicationContext;
	}

}
