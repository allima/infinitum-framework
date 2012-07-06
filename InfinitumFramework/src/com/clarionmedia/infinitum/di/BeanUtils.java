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

package com.clarionmedia.infinitum.di;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.clarionmedia.infinitum.context.BeanFactory;
import com.clarionmedia.infinitum.context.exception.InfinitumConfigurationException;

/**
 * <p>
 * Contains static utility methods for dealing with JavaBeans.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0
 * @since 07/05/12
 */
public class BeanUtils {
	
	public static Object findCandidateBean(BeanFactory beanFactory, Class<?> clazz) {
		// TODO: find candidates who are subclasses of clazz
		String candidate = invert(beanFactory.getBeanDefinitions()).get(clazz);
		return beanFactory.loadBean(candidate);
	}

	private static <V, K> Map<V, K> invert(Map<K, V> map) {
		Map<V, K> inv = new HashMap<V, K>();
		for (Entry<K, V> entry : map.entrySet()) {
			if (inv.containsKey(entry.getValue()))
				throw new InfinitumConfigurationException(
						"More than 1 bean candidate found of type '"
								+ entry.getValue() + "'.");
			inv.put(entry.getValue(), entry.getKey());
		}
		return inv;
	}

}
