package servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import domain.Parcel;
import exception.ConfigException;
import manager.ParcelManager;
import manager.SessionManager;
import util.cesar.Debugger;

/**
 * Servlet for creating and sending a message.
 * 
 * @author Cesar Guzman
 */
@WebServlet("/parcel/create")
@MultipartConfig(fileSizeThreshold = 0, maxFileSize = 1024 * 1024 * 25, maxRequestSize = 1024 * 1024 * 25 * 10) //0mb, 25mb, 10x 25mb
public final class CreateParcelServlet extends HttpServlet {
	private static final long serialVersionUID = 4807630350769183535L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CreateParcelServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Debugger.log("CreateParcelServlet.doGet");

		//get session email
		HttpSession session = request.getSession();
		String email = SessionManager.getEmail(session.getId());
		Debugger.log("Email: " + email);

		//get parcel
		Parcel parcel = null;
		try {
			parcel = ParcelManager.get(request.getParameter("parcelID"));
		} catch (ConfigException e) {
			request.setAttribute("errorMessage", "Count not retrieve reply");
			e.printStackTrace();
		}

		//compare parcel to user, to determine who the new receiver should be
		String sendTo = "";
		if (parcel.getSender() != null && !parcel.getSender().equals(email)) {
			sendTo = parcel.getSender();
		} else if (parcel.getReceiver() != null && !parcel.getReceiver().equals(email)) {
			sendTo = parcel.getReceiver();
		}

		//Send parcel and sendTo to page
		request.setAttribute("parcel", parcel);
		request.setAttribute("sendTo", sendTo);

		//Display NewMessage page
		getServletContext().getRequestDispatcher("/WEB-INF/parcel/create.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Debugger.log("CreateParcelServlet.doPost");

		//get session email
		HttpSession session = request.getSession();
		String email = SessionManager.getEmail(session.getId());
		Debugger.log("Email: " + email);

		//get attachments
		Collection<Part> parts = null;
		try {
			parts = request.getParts();
		} catch (IllegalStateException e) {
			request.setAttribute("errorMessage", "Attachments exceed size limit");
			Debugger.log("caught IllegalStateException");
			getServletContext().getRequestDispatcher("/WEB-INF/parcel/create.jsp").forward(request, response);
			//response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "File too large");
			return;
		}

		//grab tax year
		String taxYearString = request.getParameter("taxYear");
		if (taxYearString == null) {
			request.setAttribute("errorMessage", "Couldn't send message");
			Debugger.log("taxYearString == null");
			getServletContext().getRequestDispatcher("/WEB-INF/parcel/create.jsp").forward(request, response);
			return;
		}
		int taxYear = -1;
		try {
			taxYear = Integer.parseInt(taxYearString);
		} catch (NumberFormatException e) {
			request.setAttribute("errorMessage", "Couldn't send message");
			Debugger.log("taxYearString not an integer");
			getServletContext().getRequestDispatcher("/WEB-INF/parcel/create.jsp").forward(request, response);
			return;
		}

		//grab message
		String message = request.getParameter("message");
		if (message == null) {
			request.setAttribute("errorMessage", "Couldn't send message");
			Debugger.log("message == null");
			getServletContext().getRequestDispatcher("/WEB-INF/parcel/create.jsp").forward(request, response);
			return;
		}

		//grab subject
		String subject = request.getParameter("subject");
		if (subject == null) {
			request.setAttribute("errorMessage", "Couldn't send message");
			Debugger.log("subject == null");
			getServletContext().getRequestDispatcher("/WEB-INF/parcel/create.jsp").forward(request, response);
			return;
		}

		//grab sender
		String sender = email;
		//grab receiver
		String receiver = request.getParameter("receiver");
		if (receiver == null) {
			receiver = "";
			Debugger.log("receiver == null");
			getServletContext().getRequestDispatcher("/WEB-INF/parcel/create.jsp").forward(request, response);
			return;
		}
		//grab requiresSignature
		String requiresSignatureString = request.getParameter("reqSig");
		boolean requiresSignature = false;
		if (requiresSignatureString != null) {
			requiresSignature = true;
		}

		Debugger.log("parts: " + "can't check parts here" + "\nSubject: " + subject + "\nMessage: " + message
				+ "\nSender: " + sender + "\nReceiver: " + receiver + "\nTax Year: " + taxYear
				+ "\nRequires Signature: " + requiresSignatureString);

		if (!ParcelManager.createParcel(parts, subject, message, sender, receiver, new Date(), null, taxYear,
				requiresSignature)) {
			request.setAttribute("errorMessage", "Couldn't send message");
			getServletContext().getRequestDispatcher("/WEB-INF/parcel/create.jsp").forward(request, response);
		} else {
			request.setAttribute("successMessage", "Message Sent");
			response.sendRedirect("/inbox?successMessage=Successfully sent message");
		}

	}
}
