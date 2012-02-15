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
import com.clarionmedia.infinitum.orm.exception.ModelConfigurationException;

/**
 * <p>
 * This annotation indicates if a <code>Field</code> is a primary key. If the
 * annotation is missing from the class hierarchy, Infinitum will look for a
 * <code>Field</code> called <code>mId</code> or <code>id</code> to use as the
 * primary key. If such a <code>Field</code> is found, autoincrement will be
 * enabled for it by default. If the primary key is assigned to a
 * <code>Field</code> which is not an <code>int</code> or <code>long</code> and
 * <code>autoincrement</code> is enabled, a {@link ModelConfigurationException}
 * will be thrown at runtime. Any <code>Field</code> marked as a primary key
 * will inherently be marked as persistent, regardless of any
 * {@link Persistence} annotation that might be associated with it.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/13/12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {
	boolean autoincrement() default true;
}
