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

package com.clarionmedia.infinitum.orm;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.clarionmedia.infinitum.context.ApplicationContext;
import com.clarionmedia.infinitum.context.ApplicationContextFactory;
import com.clarionmedia.infinitum.orm.Constants.PersistenceMode;
import com.clarionmedia.infinitum.orm.annotation.Persistence;

/**
 * <p>
 * This class provides runtime resolution for model persistence through Java
 * annotations or XML mappings. The latter has not yet been implemented. Model
 * fields can be marked as transient or persistent using the {@link Persistence}
 * annotation. If no annotation is provided, the field will be marked as
 * persistent by default.
 * </p>
 * 
 * <p>
 * If using annotation configurations, model/domain classes must be located
 * within a single package which is referenced in <code>infinitum.cfg.xml</code>
 * using the <code>domainPackage</code> element. For example,
 * <code>&lt;property name="domainPackage"&gt;com.foo.bar.domain&lt;/property&gt;</code>
 * . It's also important to note that domain classes must extend
 * {@link AbstractModel} in order to work with the Infinitum ORM framework.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/12/12
 */
public class PersistenceResolution {

	public static void annotationTest() {
		try {
			ApplicationContext ctx = ApplicationContextFactory.getApplicationContext();
			Class<?>[] classes = getClasses(ctx.getDomainPackage());
			List<Field> fields = getAllFields(classes[0]);
			for (Field f : fields) {
				Persistence persistence = f.getAnnotation(Persistence.class);
				if (persistence != null) {
					PersistenceMode mode = persistence.mode();
					PersistenceMode m = mode;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<Field> getAllFields(Class<?> c) {
		return getAllFieldsRec(c, new LinkedList<Field>());
	}

	private static List<Field> getAllFieldsRec(Class<?> c, List<Field> fields) {
		Class<?> superClass = c.getSuperclass();
		if (superClass != null)
			getAllFieldsRec(superClass, fields);
		fields.addAll(Arrays.asList(c.getDeclaredFields()));
		return fields;
	}
	
	private static Class<?>[] getClasses(String packageName) {
		// TODO
		return null;
	}

}
