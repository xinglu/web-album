package com.wy.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jspsmart.upload.File;
import com.jspsmart.upload.Files;
import com.wy.dao.OperationData;
import com.wy.form.Photo;
import com.wy.form.UserInfo;

public class PhotoServlet extends HttpServlet {
	private String info = "";

	private OperationData data = null;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		info = request.getParameter("info");
		if (info.equals("userQueryPhoto")) {
			this.user_queryPhoto(request, response);
		}
		if (info.equals("userUploadPhoto")) {
			this.user_uploadPhoto(request, response);
		}
		if (info.equals("queryOnePhoto")) {

			this.queryOnePhoto(request, response);
		}
		if (info.equals("queryPhotoList")) {

			this.user_queryPhotoList(request, response);
		}
		if (info.equals("queryOnePhoto")) {

			this.queryOnePhoto(request, response);
		}
		if (info.equals("queryPhotoSlide")) {

			this.queryPhotoSlide(request, response);
		}
		if (info.equals("userDeletePhoto")) {

			this.user_deletePhoto(request, response);
		}
		if (info.equals("userprintPhoto")) {

			this.user_printPhoto(request, response);
		}
		if (info.equals("forward_index")) {

			this.forward_index(request, response);
		}

	}

	public void forward_index(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		List list = new OperationData().queryPhotoList();
		request.setAttribute("list", list);
		request.getRequestDispatcher("photoIndex.jsp").forward(request,
				response);
	}

	public void user_printPhoto(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		data = new OperationData();
		Integer id = Integer.valueOf(request.getParameter("id"));
		String condition = "id = '" + id + "'";
		List list = data.photo_queryList(condition);
		Photo photo1 = (Photo) list.get(0);
		String filePath = request.getRealPath(photo1.getPhotoAddress()); 
		String print = "savePrint/" + System.currentTimeMillis() + ".JPG";
		String printPath = request.getRealPath(print);
		if (!photo1.getPrintAddress().equals("0")) {
			String path = request.getRealPath(photo1.getPrintAddress());
			java.io.File file1 = new java.io.File(path);
			if (file1.exists()) {
				file1.delete();
			}
		}
		String printInforamtion = com.wy.tools.Encrypt.toChinese(request
				.getParameter("information"));
		String information = "���ˮӡЧ��ʧ�ܣ�";
		if (com.wy.tools.ImageUtils.createMark(filePath, printPath,
				printInforamtion)) {
			Photo photo2 = new Photo();
			photo2.setId(photo1.getId());
			photo2.setPrintAddress(print);
			if (data.updatePhoto(photo2)) {
				information = "���ˮӡЧ��ɹ���";
			}
		}

		request.setAttribute("information", information);
		List list1 = data.photo_queryList(condition);
		Photo photo3 = (Photo) list1.get(0);
		request.setAttribute("photo", photo3);
		request.getRequestDispatcher("photoShow.jsp")
				.forward(request, response);
	}

	public void user_deletePhoto(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=GBK");
		PrintWriter out = response.getWriter();
		data = new OperationData();
		Integer id = Integer.valueOf(request.getParameter("id"));
		String condition = "id=" + id; 
		List list = data.photo_queryList(condition); 
		String address = null; 
		String print = null;
		String type = null;
		if (list.size() == 1) { 
			Photo photo = (Photo) list.get(0);
			address = photo.getPhotoAddress(); 
			print = photo.getPrintAddress();
			type = photo.getPhotoType(); 
		}
		String path = request.getRealPath("/" + address);
		data.photo_delete(id); 
		java.io.File file1 = new java.io.File(path);
		if (file1.exists()) {
			file1.delete();
		}
		String printPath = request.getRealPath("/" + print);
		java.io.File file2 = new java.io.File(printPath);
		if (file2.exists()) {
			file2.delete();
		}
		request.setAttribute("type", type);
		request.getRequestDispatcher("dealwith.jsp").forward(request, response);
	}

	public void user_uploadPhoto(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		data = new OperationData();
		com.jspsmart.upload.SmartUpload su = new com.jspsmart.upload.SmartUpload();
		String information = "photoServlet line 145";
		try {
			su.initialize(this.getServletConfig(), request, response);
			su.setMaxFileSize(20 * 1024 * 1024); 
			su.upload();
			Files files = su.getFiles(); 

			for (int i = 0; i < files.getCount(); i++) {
				File singleFile = files.getFile(i); 

				String fileType = singleFile.getFileExt(); 
				String[] type = { "JPG", "jpg", "gif", "bmp", "BMP" }; 
				int place = java.util.Arrays.binarySearch(type, fileType);

				String code = su.getRequest().getParameter("code"); 
				String codeSession = (String) request.getSession()
						.getAttribute("rand"); 
				if (code.equals(codeSession)) { 

					if (place != -1) { 
						if (!singleFile.isMissing()) { 
							String photoName = su.getRequest().getParameter(
									"photoName")
									+ i; 
							String photoType = su.getRequest().getParameter(
									"photoType"); 
							String photoTime = su.getRequest().getParameter(
									"photoTime"); 
							String username = su.getRequest().getParameter(
									"username"); 
							String photoSize = String.valueOf(singleFile
									.getSize()); 
							String filedir = "savefile/"
									+ System.currentTimeMillis() + "."
									+ singleFile.getFileExt(); 

							String smalldir = "saveSmall/"
									+ System.currentTimeMillis() + "."
									+ singleFile.getFileExt();

							Photo photo = new Photo();
							photo.setPhotoName(photoName);
							photo.setPhotoType(photoType);
							photo.setPhotoSize(photoSize);
							photo.setPhotoTime(photoTime);
							photo.setUsername(username);
							photo.setPhotoAddress(filedir);
							photo.setSmallPhoto(smalldir);
							if (data.photo_save(photo)) { 
								singleFile.saveAs(filedir, File.SAVEAS_VIRTUAL);
								com.wy.tools.ImageUtils.createSmallPhoto(
										request.getRealPath("/" + filedir),
										request.getRealPath("/" + smalldir));
								information = "photoServlet line 198!";

							}

						}

					}

				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		request.setAttribute("information", information);
		request.getRequestDispatcher("user_upLoadPhoto.jsp").forward(request,
				response);

	}

	public void user_queryPhoto(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		data = new OperationData();
		UserInfo userInfo = (UserInfo) request.getSession().getAttribute(
				"userInfo"); 
		String username = userInfo.getUsername(); 
		String[] type = data.queryPhotoType(username); 
		request.setAttribute("type", type); 
		String condition = "username = '" + username + "'";
		List list = data.photo_queryList(condition); 
		request.setAttribute("list", list); 
		request.getRequestDispatcher("user_queryPhoto.jsp").forward(request,
				response);
	}

	public void user_queryPhotoList(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		data = new OperationData();
		String type = com.wy.tools.Encrypt.toChinese(request
				.getParameter("type")); 
		String condition = "photoType = '" + type + "'";
		if (null != request.getSession().getAttribute("userInfo")) {
			UserInfo userInfo = (UserInfo) request.getSession().getAttribute(
					"userInfo"); 
			String username = userInfo.getUsername(); 
			condition = "username ='" + username + "' and photoType = '" + type
					+ "'"; 
		}
		List list = data.photo_queryList(condition);
		if (list.size() == 0) {
			request.setCharacterEncoding("gb2312");
			PrintWriter out = response.getWriter();
			out.print("<script language=javascript>history.go(-1);</script>");
		} else {
			request.setAttribute("list", list); 
			request.setAttribute("type", type); 
			request.getRequestDispatcher("user_queryPhotoList.jsp").forward(
					request, response);
		}
	}

	public void queryOnePhoto(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		data = new OperationData();
		Integer id = Integer.valueOf(request.getParameter("id")); 
		String condition = "id = '" + id + "'";
		List list = data.photo_queryList(condition);
		Photo photo = null;
		if (list.size() == 1) { 
			photo = (Photo) list.get(0);
		}
		request.setAttribute("photo", photo);
		try {
			request.getRequestDispatcher("photoShow.jsp").forward(request,
					response);
			return;
		} catch (Exception e) {

		}

	}

	public void queryPhotoSlide(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		data = new OperationData();
		UserInfo userInfo = (UserInfo) request.getSession().getAttribute(
				"userInfo"); 
		String username = userInfo.getUsername();
		String type = com.wy.tools.Encrypt.toChinese(request
				.getParameter("type"));
		String condition = "username ='" + username + "' and photoType = '"
				+ type + "'"; 
		List list = data.photo_queryList(condition);
		String address[] = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Photo photo = (Photo) list.get(i);
			address[i] = photo.getPhotoAddress(); 
		}
		request.setAttribute("address", address); 
		request.getRequestDispatcher("photoShowSlide.jsp").forward(request,
				response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}

}