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

import android.database.SQLException;

/**
 * <p>
 * An abstract implementation of {@link SqliteDao}. This class is designed to
 * provide DAO extensibility while providing implementations for core CRUD
 * operations.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 02/11/12
 * 
 */
public abstract class AbstractSqliteDao implements SqliteDao {

	@Override
	public SqliteDao open() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public long save(AbstractModel model) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long update(AbstractModel model) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean delete(AbstractModel model) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long saveOrUpdate(AbstractModel model) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void saveOrUpdateAll(Iterable<? extends AbstractModel> models) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveAll(Iterable<? extends AbstractModel> models) {
		// TODO Auto-generated method stub

	}

	@Override
	public int deleteAll(Iterable<? extends AbstractModel> models) {
		// TODO Auto-generated method stub
		return 0;
	}

}
