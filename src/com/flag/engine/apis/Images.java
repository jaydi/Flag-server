package com.flag.engine.apis;

import com.flag.engine.constants.Constants;
import com.flag.engine.models.UploadUrl;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class Images {
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	
	@ApiMethod(name = "images.uploadUrl.get", path = "upload_url", httpMethod = "get")
	public UploadUrl uploadUrl() {
		return new UploadUrl(blobstoreService.createUploadUrl("/upload"));
	}
}
