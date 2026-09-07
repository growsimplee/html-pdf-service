/*
 * {{{ header & license
 * Copyright (c) 2016 Farrukh Mirza
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.farrukh.mirza.pdf.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextUserAgent;

/**
 * Flying Saucer's default resource loader (NaiveUserAgent#resolveAndOpenStream) opens a plain
 * URLConnection for every embedded resource (e.g. <img src="...">) with no connect/read timeout.
 * If a referenced host is slow or unreachable, that single image fetch can hang the whole
 * layout()/createPDF() call for as long as the underlying socket takes to give up, dragging the
 * entire HTML-to-PDF conversion request down with it.
 *
 * This override bounds every resource fetch with explicit timeouts and fails soft (returns null,
 * which Flying Saucer treats as "resource unavailable" and skips) instead of hanging or
 * propagating the failure.
 */
public class TimeoutAwareITextUserAgent extends ITextUserAgent {

	private static final Logger logger = LoggerFactory.getLogger(TimeoutAwareITextUserAgent.class);

	private static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
	private static final int DEFAULT_READ_TIMEOUT_MS = 3000;

	private final int connectTimeoutMs;
	private final int readTimeoutMs;

	public TimeoutAwareITextUserAgent(ITextOutputDevice outputDevice) {
		this(outputDevice, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
	}

	public TimeoutAwareITextUserAgent(ITextOutputDevice outputDevice, int connectTimeoutMs, int readTimeoutMs) {
		super(outputDevice);
		this.connectTimeoutMs = connectTimeoutMs;
		this.readTimeoutMs = readTimeoutMs;
	}

	@Override
	protected InputStream resolveAndOpenStream(String uri) {
		String resolvedUri = resolveURI(uri);
		if (resolvedUri == null) {
			return null;
		}
		try {
			URL url = new URL(resolvedUri);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(connectTimeoutMs);
			connection.setReadTimeout(readTimeoutMs);
			return connection.getInputStream();
		} catch (IOException e) {
			// Covers SocketTimeoutException (connect/read timeout), FileNotFoundException,
			// UnknownHostException, etc. A missing/slow/unreachable resource should not fail or
			// hang the whole conversion -- treat it the same way Flying Saucer treats "not found".
			logger.warn("Skipping unreachable/slow resource '{}' after timeout: {}", resolvedUri, e.getMessage());
			return null;
		} catch (Exception e) {
			logger.warn("Skipping resource '{}' due to unexpected error: {}", resolvedUri, e.getMessage());
			return null;
		}
	}
}
