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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.linagora.LinThumbnail.utils.ThumbnailKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/linthumbnail")
public class ThumbnailResource {

	public Logger logger = LoggerFactory.getLogger(ThumbnailResource.class);

	protected String delay;

	public ThumbnailResource(String delay) {
		super();
		this.delay = delay;
	}

	@POST
	@Consumes("multipart/form-data")
	@Produces("multipart/mixed")
	public Response generateThumbnail(
			@FormDataParam("file") InputStream is,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@QueryParam("mimeType")String mimeType)
			throws IOException {
		try {
			Map<ThumbnailKind, File> thumbnailMap = new HashMap<ThumbnailKind, File>();
			ThumbnailWrapper tw = new ThumbnailWrapper(is, fileDetail.getFileName(), mimeType, delay);
			thumbnailMap = tw.getThumbnailList();
			MultiPart multiPart = new MultiPart();
			thumbnailMap.forEach((key, value)->{
				multiPart.bodyPart(getBodyPart(value, key));
			});
			tw.cleanFiles(thumbnailMap);
			return Response.ok(multiPart, "multipart/mixed").build();
		} catch (IOException io) {
			logger.debug("Error to generate the thumbnail !!", io);
			return Response.status(500).build();
		}
	}

	private BodyPart getBodyPart(File image, ThumbnailKind kind) {
		BodyPart bodyPart = new BodyPart();
		ContentDisposition contentDisposition = ContentDisposition.type("attachement")
				.fileName(kind + ThumbnailKind.getFileType(kind)).build();
		bodyPart.setContentDisposition(contentDisposition);
		bodyPart.setEntity(image);
		if (image != null) {
			image.deleteOnExit();
		}
		return bodyPart;
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response isSupported(@QueryParam("mimeType") String mimeType) {
		if (mimeType == null) {
			return Response.accepted().build();
		}
		if (SupportedMimeType.isSupported(mimeType)) {
			return Response.noContent().build();
		} else {
			return Response.status(405).build(); // Not Allowed
		}
	}
}