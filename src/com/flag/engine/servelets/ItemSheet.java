package com.flag.engine.servelets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.flag.engine.exceptions.InvalidSheetFormatException;
import com.flag.engine.exceptions.InvalidShopException;
import com.flag.engine.models.Item;
import com.flag.engine.models.PMF;

public class ItemSheet extends HttpServlet {
	private static final Logger log = Logger.getLogger(ItemSheet.class.getName());
	private static final long serialVersionUID = 1L;
	private ServletFileUpload upload = new ServletFileUpload();

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		log.warning("item sheet upload: start");
		long startTime = new Date().getTime();
		long shopId = 0;
		List<Item> items = new ArrayList<Item>();

		try {
			FileItemIterator iterator = upload.getItemIterator(req);
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				if (item.isFormField() && item.getFieldName().equals("shopId")) {
					String shopIdString = Streams.asString(item.openStream());
					if (shopIdString == null || shopIdString.isEmpty()) {
						res.getWriter().write("shop id is missing");
						return;
					} else {
						try {
							shopId = Long.valueOf(shopIdString);
							log.warning("item sheet upload: shop id is " + shopId);
						} catch (NumberFormatException e) {
							res.getWriter().write("shop id must be a number : " + shopIdString);
							return;
						}
					}
				} else {
					log.warning("item sheet upload: file found");
					Workbook wb;
					if (item.getName().endsWith(".xlsx"))
						wb = new XSSFWorkbook(item.openStream());
					else if (item.getName().endsWith(".xls"))
						wb = new HSSFWorkbook(item.openStream());
					else
						throw new InvalidSheetFormatException();

					log.warning("item sheet upload: parsing sheet file");
					Sheet sheet = wb.getSheetAt(0);
					Row row;
					Cell cell;

					int rowCount;
					rowCount = sheet.getPhysicalNumberOfRows();

					for (int r = 1; r < rowCount; r++) {
						row = sheet.getRow(r);
						if (row != null) {
							String[] dataArray = new String[6];
							for (int c = 0; c < 6; c++) {
								cell = row.getCell(c);
								if (cell != null)
									dataArray[c] = cell.toString();
								else
									dataArray[c] = "";
							}

							boolean isFalseData = false;
							for (int i = 0; i < 4; i++)
								if (dataArray[i].isEmpty())
									isFalseData = true;

							if (!isFalseData)
								items.add(new Item(dataArray));
						}
					}
				}
			}

			if (shopId != 0)
				saveItems(shopId, items);
			else
				throw new InvalidShopException();

			log.warning("process time: " + (new Date().getTime() - startTime) + "ms");
			res.getWriter().write("success");

		} catch (FileUploadException e) {
			e.printStackTrace();
			res.getWriter().write("no file specified");
		} catch (InvalidSheetFormatException e) {
			e.printStackTrace();
			res.getWriter().write("invalid sheet format. must be .xls or .xlsx");
		} catch (InvalidShopException e) {
			e.printStackTrace();
			res.getWriter().write("no such shop");
		}
	}

	@SuppressWarnings("unchecked")
	private void saveItems(long shopId, List<Item> items) {
		log.warning("# of items to be uploaded: " + items.size());
		long startTime = new Date().getTime();
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Item.class);
		query.setFilter("shopId == theShopId");
		query.declareParameters("long theShopId");
		List<Item> shopItems = (List<Item>) pm.newQuery(query).execute(shopId);

		List<Item> deletableItems = new ArrayList<Item>();
		for (Item item : items) {
			item.setShopId(shopId);
			for (Item shopItem : shopItems)
				if (shopItem.equals(item))
					deletableItems.add(shopItem);
		}

		pm.deletePersistentAll(deletableItems);
		pm.makePersistentAll(items);

		pm.close();

		log.warning("data insertion time: " + (new Date().getTime() - startTime) + "ms");
	}

}
