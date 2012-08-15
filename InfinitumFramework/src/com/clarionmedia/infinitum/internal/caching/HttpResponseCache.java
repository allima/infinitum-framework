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

package com.clarionmedia.infinitum.internal.caching;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.clarionmedia.infinitum.http.HttpClientResponse;
import com.clarionmedia.infinitum.http.impl.BasicHttpClientResponse;
import com.clarionmedia.infinitum.http.impl.HashableHttpRequest;

/**
 * <p>
 * Concrete implementation of {@link AbstractCache} for caching HTTP responses. 
 * The cache is keyed off of a {@link HashableHttpRequest}, which is a wrapper
 * for {@link HttpUriRequest} that implements {@code hashCode} and {@code equals}
 * methods.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 08/15/12
 * @since 1.0
 */
public class HttpResponseCache extends AbstractCache<HashableHttpRequest, HttpClientResponse> {
	
	private static final String CACHE_NAME = "HttpCache";

	public HttpResponseCache(int initialCapacity, long defaultExpiration) {
		super(CACHE_NAME, initialCapacity, defaultExpiration);
	}

    @Override
    public String getFileNameForKey(HashableHttpRequest request) {
        return getFileNameFromUri(request);
    }

    @Override
    protected HttpClientResponse readValueFromDisk(File file) throws IOException {
        BufferedInputStream istream = new BufferedInputStream(new FileInputStream(file));
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
        	istream.close();
            throw new IOException("Cannot read files larger than " + Integer.MAX_VALUE + " bytes");
        }

        // first byte is the status code
        int statusCode = istream.read();

        // the remainder is the response data
        int responseDataLength = (int) fileSize - 1;

        // TODO cache headers too
        byte[] responseBody = new byte[responseDataLength];
        istream.read(responseBody, 0, responseDataLength);
        istream.close();

        return new BasicHttpClientResponse(statusCode, responseBody);
    }

    @Override
    protected void writeValueToDisk(File file, HttpClientResponse data) throws IOException {
        BufferedOutputStream ostream = new BufferedOutputStream(new FileOutputStream(file));

        ostream.write(data.getStatusCode());
        ostream.write(data.getResponseData());

        ostream.close();
    }
    
    private String getFileNameFromUri(HashableHttpRequest request) {
        // replace all special URI characters with a single + symbol
        return request.getRequestUri().replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
    }

}
