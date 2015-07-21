package com.flag.engine.servelets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.flag.engine.models.Item;
import com.flag.engine.models.PMF;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

public class UploadImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(UploadImage.class.getName());
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Map<String, List<BlobKey>> blobKeyMap = blobstoreService.getUploads(req);
		Map<String, List<BlobInfo>> blobInfoMap = blobstoreService.getBlobInfos(req);

		List<BlobKey> blobKeys = blobKeyMap.get("image");
		List<BlobInfo> blobInfos = blobInfoMap.get("image");

		if (blobKeys == null || blobKeys.isEmpty()) {
			log.warning("no blob uploaded");
			return;
		}

		res.setContentType("application/json");
		PrintWriter out = res.getWriter();
		out.print("{\"url\": \"https://genuine-evening-455.appspot.com/serve?blob-key=" + blobKeys.get(0).getKeyString() + "\"}");
		out.flush();

		String fileName = blobInfos.get(0).getFilename();
		if (fileName.indexOf('.') > -1)
			fileName = fileName.substring(0, fileName.lastIndexOf('.'));
		
		matchWithItem(fileName, blobKeys.get(0).getKeyString(), req.getParameter("shopId"));
	}

	@SuppressWarnings("unchecked")
	private void matchWithItem(String fileName, String keyString, String shopId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		String url = "https://genuine-evening-455.appspot.com/serve?blob-key=" + keyString;

		Query query = pm.newQuery(Item.class);
		query.setFilter("shopId == theShopId && barcodeId == fileName");
		query.declareParameters("long theShopId, String fileName");
		List<Item> items = (List<Item>) pm.newQuery(query).execute(Long.valueOf(shopId), fileName);

		if (items.size() > 0)
			log.warning("image upload: found matching item");
		
		for (Item item : items)
			item.setThumbnailUrl(url);

		pm.close();
	}
}
