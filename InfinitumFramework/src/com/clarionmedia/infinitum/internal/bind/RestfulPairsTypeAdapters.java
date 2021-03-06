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

package com.clarionmedia.infinitum.internal.bind;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.clarionmedia.infinitum.http.rest.RestfulPairsTypeAdapter;
import com.clarionmedia.infinitum.internal.DateFormatter;
import com.clarionmedia.infinitum.orm.ResultSet;

/**
 * <p>
 * {@link RestfulPairsTypeAdapter} bindings for basic types.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 03/17/12
 * @since 1.0
 */
public final class RestfulPairsTypeAdapters {
	
	public static final RestfulPairsTypeAdapter<String> STRING = new RestfulPairsTypeAdapter<String>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			field.set(model, result.getString(index));
		}
		@Override
		public void mapToPair(String value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, value));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, (String) value));
		}
	};
	
	public static final RestfulPairsTypeAdapter<Integer> INTEGER = new RestfulPairsTypeAdapter<Integer>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			field.set(model, result.getInt(index));
		}
		@Override
		public void mapToPair(Integer value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, Integer.toString(value)));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			int val = (Integer) value;
			pairs.add(new BasicNameValuePair(field, Integer.toString(val)));
		}
	};
	
	public static final RestfulPairsTypeAdapter<Long> LONG = new RestfulPairsTypeAdapter<Long>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			field.set(model, result.getLong(index));
		}
		@Override
		public void mapToPair(Long value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, Long.toString(value)));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			long val = (Long) value;
			pairs.add(new BasicNameValuePair(field, Long.toString(val)));
		}
	};
	
	public static final RestfulPairsTypeAdapter<Float> FLOAT = new RestfulPairsTypeAdapter<Float>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			field.set(model, result.getFloat(index));
		}
		@Override
		public void mapToPair(Float value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, Float.toString(value)));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			float val = (Float) value;
			pairs.add(new BasicNameValuePair(field, Float.toString(val)));
		}
	};
	
	public static final RestfulPairsTypeAdapter<Double> DOUBLE = new RestfulPairsTypeAdapter<Double>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			field.set(model, result.getDouble(index));
		}
		@Override
		public void mapToPair(Double value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, Double.toString(value)));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			double val = (Double) value;
			pairs.add(new BasicNameValuePair(field, Double.toString(val)));
		}
	};
	
	public static final RestfulPairsTypeAdapter<Short> SHORT = new RestfulPairsTypeAdapter<Short>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			field.set(model, result.getShort(index));
		}
		@Override
		public void mapToPair(Short value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, Short.toString(value)));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			short val = (Short) value;
			pairs.add(new BasicNameValuePair(field, Short.toString(val)));
		}
	};
	
	public static final RestfulPairsTypeAdapter<Boolean> BOOLEAN = new RestfulPairsTypeAdapter<Boolean>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			int val = result.getInt(index);
			field.set(model, val == 1);
		}
		@Override
		public void mapToPair(Boolean value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, Boolean.toString(value)));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			boolean val = (Boolean) value;
			pairs.add(new BasicNameValuePair(field, Boolean.toString(val)));
		}
	};

	public static final RestfulPairsTypeAdapter<Byte> BYTE = new RestfulPairsTypeAdapter<Byte>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			field.set(model, result.getBlob(index)[0]);
		}
		@Override
		public void mapToPair(Byte value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, Byte.toString(value)));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			byte val = (Byte) value;
			pairs.add(new BasicNameValuePair(field, Byte.toString(val)));
		}
	};
	
	public static final RestfulPairsTypeAdapter<byte[]> BYTE_ARRAY = new RestfulPairsTypeAdapter<byte[]>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			field.set(model, result.getBlob(index));
		}
		@Override
		public void mapToPair(byte[] value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, value.toString()));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			byte[] val = (byte[]) value;
			pairs.add(new BasicNameValuePair(field, val.toString()));
		}
	};
	
	public static final RestfulPairsTypeAdapter<Character> CHARACTER = new RestfulPairsTypeAdapter<Character>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			field.set(model, result.getString(index).charAt(0));
		}
		@Override
		public void mapToPair(Character value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, value.toString()));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			char val = (Character) value;
			pairs.add(new BasicNameValuePair(field, Character.toString(val)));
		}
	};
	
	public static final RestfulPairsTypeAdapter<Date> DATE = new RestfulPairsTypeAdapter<Date>() {
		@Override
		public void mapToObject(ResultSet result, int index, Field field, Object model) throws IllegalArgumentException, IllegalAccessException {
			field.setAccessible(true);
			String dateStr = result.getString(index);
			field.set(model, DateFormatter.parseStringAsDate(dateStr));
		}
		@Override
		public void mapToPair(Date value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, DateFormatter.getDateAsISO8601String(value)));
		}
		@Override
		public void mapObjectToPair(Object value, String field, List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair(field, DateFormatter.getDateAsISO8601String((Date) value)));
		}
	};
	
}
