/*
 * Buzz - a monitoring framework for JBoss
 *
 * Copyright 2012-2014 Anders Welén, anders@welen.net
 * 
 * This file is part of Buzz.
 *
 * Buzz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Buzz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Buzz.  If not, see <http://www.gnu.org/licenses/>. 
 */
package net.welen.buzz.protocols.mail;

import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.protocols.IntervalBased;

/**
 * @author welle
 *
 */
public class Mail extends IntervalBased implements MailMBean {
	
	private static final Logger LOG = Logger.getLogger(Mail.class);
	
	private String jndiName = "java:Mail";
	private String fromAddress = "Buzz mail plugin";
	private String[] toAddresses = {"to@someone.that.bother"};
	private String subject = "Buzz mail plugin %level% from server: %serverId%";
	private String serverId = "localhost";

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.IntervalBasedMBean#performWork()
	 */
	public void performWork() {
		BuzzAnswer input = new BuzzAnswer();
		MailAnswer answer = new MailAnswer();				
		getValues(input);
		
		// Warnings
		filterWarnings(input, answer);
		sendMail(answer, true);		

		// Errors		
		answer = new MailAnswer();
		filterAlarms(input, answer);
		sendMail(answer, false);		
	}
		
	private void sendMail(MailAnswer answer, boolean warning) {
		// Check if there are anything in the list
		if (answer.size() == 0) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Answer.size was 0. No warnings/errors found. Returning!");
			}						
			return;
		}
		
		Session session = null;
		try {
		    session = (Session) new InitialContext().lookup(jndiName);
		} catch (javax.naming.NamingException e) {
		    LOG.error(e.getMessage(), e);
		    return;
		}
	                             
		try {
		    Message m = new MimeMessage(session);
		    m.setFrom(new InternetAddress(fromAddress));
		    
		    Address[] addresses = new InternetAddress[toAddresses.length];
		    for (int i=0; i<toAddresses.length; i++) {
		    	addresses[i] = new InternetAddress(toAddresses[i]);
		    }
		    
		    m.setRecipients(Message.RecipientType.TO, addresses);
		    m.setSubject(subject.replace("%serverId%",serverId));
		    m.setSentDate(new Date());
		    String levelString = "errors";
		    if (warning) {
		    	levelString = "warnings";
		    }
		    m.setContent(answer.toString().replace("%serverId%",serverId)
		    		.replace("%level%", levelString), "text/plain");
		    Transport.send(m);
		    if (LOG.isDebugEnabled()) {
		    	LOG.debug("Email sent:\n" + answer.toString());
		    }
		} catch (MessagingException e) {
			LOG.error(e.getMessage(), e);
		}               
		
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.mail.MailMBean#setJNDIName(java.lang.String)
	 */
	public void setJNDIName(String name) {
		jndiName = name;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.mail.MailMBean#getJNDIName()
	 */
	public String getJNDIName() {
		return jndiName;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.mail.MailMBean#setFromAddress(java.lang.String)
	 */
	public void setFromAddress(String address) {
		fromAddress = address;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.mail.MailMBean#getFromAddress()
	 */
	public String getFromAddress() {
		return fromAddress;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.mail.MailMBean#setSubject(java.lang.String)
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.mail.MailMBean#getSubject()
	 */
	public String getSubject() {
		return subject;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.mail.MailMBean#setToAddresses(java.lang.String[])
	 */
	public void setToAddresses(String[] addresses) {
		// Handle JBoss 7 behavior to send in null during "uninjection"
		if (addresses == null) {			
			toAddresses = new String[]{};
		} else {		
			toAddresses = addresses.clone();
		}
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.mail.MailMBean#getToAddresses()
	 */
	public String[] getToAddresses() {
		return toAddresses.clone();
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.mail.MailMBean#setServerId(java.lang.String)
	 */
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.mail.MailMBean#getServerId()
	 */
	public String getServerId() {
		return serverId;
	}

}
