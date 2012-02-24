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

package com.clarionmedia.infinitum.orm.sqlite;

import com.clarionmedia.infinitum.orm.ResultSet;

import android.database.Cursor;

/**
 * <p>
 * This implementation represents a {@link ResultSet} from a SQLite database
 * query. It's essentially a wrapper for a {@link Cursor}, which is what is
 * typically used to represent SQLite relations.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/23/12
 */
public class SqliteResult implements ResultSet {

	private Cursor mCursor;

	public SqliteResult(Cursor cursor) {
		mCursor = cursor;
	}

	public Cursor getCursor() {
		return mCursor;
	}

	public void setCursor(Cursor cursor) {
		mCursor = cursor;
	}

	public void close() {
		mCursor.close();
	}

	public int getInt(int columnIndex) {
		return mCursor.getInt(columnIndex);
	}

	public long getLong(int columnIndex) {
		return mCursor.getLong(columnIndex);
	}

	public String getString(int columnIndex) {
		return mCursor.getString(columnIndex);
	}

	public float getFloat(int columnIndex) {
		return mCursor.getFloat(columnIndex);
	}

	public double getDouble(int columnIndex) {
		return mCursor.getDouble(columnIndex);
	}

	public short getShort(int columnIndex) {
		return mCursor.getShort(columnIndex);
	}

	public byte[] getBlob(int columnIndex) {
		return mCursor.getBlob(columnIndex);
	}

	public int getColumnCount(int columnIndex) {
		return mCursor.getColumnCount();
	}

	public int getColumnIndex(String columnName) {
		return mCursor.getColumnIndex(columnName);
	}

}
