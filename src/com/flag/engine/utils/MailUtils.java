package com.flag.engine.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.flag.engine.models.FeedbackMessage;

public class MailUtils {
	private static final Logger log = Logger.getLogger(MailUtils.class.getName());

	public static void sendFeedbackMail(FeedbackMessage feedbackMessage) {
		log.warning("sending feedback mail");

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("team.tankers@gmail.com", "Tankers"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("help.tankers@gmail.com", "Help Tankers"));
			msg.setSubject("Dalshop feedback message");
			msg.setText("from: " + feedbackMessage.getEmail() + "\n\n" + feedbackMessage.getMessage().getValue() + "\n\n" + "createdAt: "
					+ feedbackMessage.getCreatedAtString());
			Transport.send(msg);

			log.warning("feedback mail sent");
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
