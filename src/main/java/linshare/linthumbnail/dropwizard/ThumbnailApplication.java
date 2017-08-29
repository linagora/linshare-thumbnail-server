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

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.linagora.LinThumbnail.ServiceOfficeManager;
import org.linagora.LinThumbnail.ThumbnailService;
import org.linagora.LinThumbnail.impl.ThumbnailServiceImpl;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class ThumbnailApplication extends Application<ThumbnailConfiguration> {

	public static void main(final String[] args) throws Exception {
		new ThumbnailApplication().run(args);
	}

	@Override
	public String getName() {
		return "LinThumbnail-WebService";
	}

	@Override
	public void initialize(final Bootstrap<ThumbnailConfiguration> bootstrap) {
	}

	@Override
	public void run(final ThumbnailConfiguration configuration, final Environment environment) {
		environment.jersey().register(MultiPartFeature.class);
		final ThumbnailResource tr = new ThumbnailResource();
		environment.jersey().register(tr);
		final TypeMimeHealthCheck mimeTypeHealthCheck = new TypeMimeHealthCheck("image/png");
		environment.healthChecks().register("MimeType", mimeTypeHealthCheck);
		ServiceOfficeManager som = ServiceOfficeManager.getInstance();
		final ServiceOfficeManagerHealthCheck officeManagerHealthCheck = new ServiceOfficeManagerHealthCheck(som);
		environment.healthChecks().register("Service Office Manager", officeManagerHealthCheck);
		ThumbnailService thumbnailService = new ThumbnailServiceImpl();
		ServiceManager managed = new ServiceManager(thumbnailService);
		environment.lifecycle().manage(managed);
	}
}