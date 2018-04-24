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

package linshare.linthumbnail.dropwizard;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.activation.DataHandler;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.junit.ClassRule;
import org.junit.Test;
import org.linagora.linshare.thumbnail.server.ThumbnailApplication;
import org.linagora.linshare.thumbnail.server.ThumbnailConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

public class DropwizardRestTest {

	private Logger logger = LoggerFactory.getLogger(DropwizardRestTest.class);

	@ClassRule
	public static final DropwizardAppRule<ThumbnailConfiguration> RULE = new DropwizardAppRule<ThumbnailConfiguration>(
			ThumbnailApplication.class, ResourceHelpers.resourceFilePath("test_config.yml"));

	@Test
	public void isFileSupportedTest() {
		final Response content = ClientBuilder.newClient()
				.target("http://0.0.0.0:" + RULE.getLocalPort() + "/linthumbnail?mimeType=image/png").request()
				.get(Response.class);
		assertThat(content.getStatusInfo().getStatusCode(), is(204));
	}

	@Test
	public void generateThumbnailTest() throws IOException {
		WebClient client = WebClient.create("http://localhost:8090/linthumbnail");
		client.type(MediaType.MULTIPART_FORM_DATA);
		client.accept("multipart/mixed");
		client.query("mimeType", "application/vnd.oasis.opendocument.text");
		try (InputStream stream = FileUtils.openInputStream(new File("src/test/resources/testingThumbnail.odt"));) {
			ContentDisposition cd = new ContentDisposition("form-data; name=file; filename=testingThumbnail.odt");
			Attachment attFile = new Attachment("file", stream, cd);
			MultipartBody body = new MultipartBody(attFile);
			Response response = client.post(body);
			assertThat(response.getStatusInfo().getStatusCode(), is(200));
			MultipartBody mb = response.readEntity(MultipartBody.class);
			List<Attachment> allAttachments = mb.getAllAttachments();
			for (Attachment attachment : allAttachments) {
				String fileName = attachment.getContentDisposition().getFilename();
				assertThat(fileName, anyOf(equalTo("LARGE.png"), equalTo("MEDIUM.png"), equalTo("SMALL.png"), equalTo("PDF.pdf")));
				logger.info("Attachment : fileName : " + fileName);
				File file = new File("src/test/resources/" + fileName);
				DataHandler dataHandler = attachment.getDataHandler();
				assertThat(dataHandler, notNullValue());
				dataHandler.writeTo(new FileOutputStream(file));
			}
		}
	}
}
