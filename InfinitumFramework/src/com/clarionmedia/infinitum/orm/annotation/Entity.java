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

package com.clarionmedia.infinitum.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clarionmedia.infinitum.orm.OrmConstants.PersistenceMode;

/**
 * <p>
 * This annotation is used to indicate the persistence state of a model. A model
 * can be marked as either persistent or transient using the
 * {@code PersistenceMode} enumeration. Persistent models must include an empty
 * constructor in order for the Infinitum ORM to work. For example:
 * </p>
 * 
 * <pre>
 * public class Foobar {
 *     // ...
 *     public Foobar() {}
 *     // ...
 * }
 * </pre>
 * 
 * @author Tyler Treat
 * @version 1.0 02/12/12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
	
	PersistenceMode mode() default PersistenceMode.Persistent;
	
	boolean cascade() default true;
	
}
