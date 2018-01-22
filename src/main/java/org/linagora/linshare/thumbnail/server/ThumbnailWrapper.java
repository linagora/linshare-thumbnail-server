/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2017 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2017. Contribute to
 * Linshare R&D by subscribing to an Enterprise offer!” infobox and in the
 * e-mails sent with the Program, (ii) retain all hypertext links between
 * LinShare and linshare.org, between linagora.com and Linagora, and (iii)
 * refrain from infringing Linagora intellectual property rights over its
 * trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for LinShare along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to LinShare software.
 */

package org.linagora.linshare.thumbnail.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.linagora.LinThumbnail.FileResource;
import org.linagora.LinThumbnail.FileResourceFactory;
import org.linagora.LinThumbnail.ThumbnailService;
import org.linagora.LinThumbnail.impl.ThumbnailServiceImpl;
import org.linagora.LinThumbnail.utils.ThumbnailKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbnailWrapper {

	public Logger logger = LoggerFactory.getLogger(ThumbnailWrapper.class);

	private File resource;

	private FileResourceFactory fileResourceFactory;

	private ThumbnailService thumbnailService = new ThumbnailServiceImpl();

	private FileResource fileResource;

	public ThumbnailWrapper(InputStream resource, String fileName, String mimeType) throws IOException {
		this.resource = getFileResource(fileName, resource);
		this.fileResourceFactory = thumbnailService.getFactory();
		this.fileResource = fileResourceFactory.getFileResource(this.resource, mimeType);
	}

	private File getFileResource(String name, InputStream is) throws IOException {
		File file = null;
		try {
			file = File.createTempFile("file", name);
			try (FileOutputStream out = new FileOutputStream(file)) {
				int read = 0;
				byte bytes[] = new byte[1024];
				while ((read = is.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				out.flush();
			}
		} catch (IOException ex) {
			logger.error("Error to get the file !!", ex);
			if (file != null) {
				file.delete();
			}
			return null;
		}
		return file;
	}

	public Map<ThumbnailKind, File> getThumbnailList() throws IOException {
		Map<ThumbnailKind, File> thmbFiles = fileResource.generateThumbnailMap();
		for (Map.Entry<ThumbnailKind, File> entry : thmbFiles.entrySet()) {
			if (entry.getValue() == null || entry.getKey() == null) {
				return fileResource.cleanThumbnailMap(thmbFiles);
			}
		}
		return thmbFiles;
	}

}
