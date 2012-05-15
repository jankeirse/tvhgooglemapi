/**
 * Copyright (C) 2012 TVH Group NV. <kalman.tiboldi@tvh.com>
 *
 * This file is part of the tvhgooglemapi project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tvh.gmaildrafter;

import com.google.code.com.sun.mail.imap.IMAPFolder;
import com.google.code.com.sun.mail.imap.IMAPMessage;
import com.google.code.com.sun.mail.imap.IMAPSSLStore;
import com.google.code.javax.activation.DataHandler;
import com.google.code.javax.activation.DataSource;
import com.google.code.javax.activation.FileDataSource;
import com.google.code.javax.mail.*;
import com.google.code.javax.mail.internet.InternetAddress;
import com.google.code.javax.mail.internet.MimeBodyPart;
import com.google.code.javax.mail.internet.MimeMessage;
import com.google.code.javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class Drafter {

    protected static final Logger logger = Logger.getLogger(Drafter.class.getName());

    private static void printHelpMessage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Drafter", "Drafter is a tool to upload a draft to gmail and open it with the default browser.", options, "This tool is intended to support the 'Send as attachment' feature of windows applications.", true);

    }

    private static Options getCMDLineOptions() {
        /*
         * Create the command line options
         */
        Options options = new Options();
        options.addOption(new Option("h", "help", false, "Show this help message."));

        OptionGroup body = new OptionGroup();
        body.addOption(new Option("i", "stdin", false, "Read body text from stdin."));
        body.addOption(OptionBuilder.withLongOpt("body").withArgName("filename").hasArg().withDescription("File containing the body text").create("b"));
        options.addOptionGroup(body);

        options.addOption(OptionBuilder.withLongOpt("username").hasArg().withArgName("email address").withDescription("Your Google Email adress.").create());
        options.addOption(OptionBuilder.withLongOpt("password").hasArg().withArgName("google password").withDescription("Your Google password (caution: providing this on the commandline can be a security problem).").create());

        options.addOption(OptionBuilder.withLongOpt("subject").withArgName("text").hasArg().withDescription("Subject of the mail").create("s"));
        options.addOption(OptionBuilder.withLongOpt("attachments").hasArgs().withArgName("filename,filename,...").withValueSeparator(',').withDescription("Attachments").create("a"));

        options.addOption(OptionBuilder.withLongOpt("attachmentnames").hasArgs().withArgName("filename,filename,...").withValueSeparator(',').withDescription("Attachment names").create("n"));

        options.addOption(OptionBuilder.withLongOpt("to").hasArgs().withArgName("some@somewhere.com,someoneelse@someotherplace.com,...").withValueSeparator(',').withDescription("destination").create("t"));

        options.addOption(new Option("d", "deletebody", false, "Delete bodyfile after sending."));

        return options;

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Options options = getCMDLineOptions();
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                printHelpMessage(options);
                System.exit(0);
            }

            String emailBody = null;
            String emailSubject = null;

            if (cmd.hasOption("body")) {
                String bodyFile = cmd.getOptionValue("body");
                File body = new File(bodyFile);
                emailBody = FileUtils.readFileToString(body);
                if (cmd.hasOption("deletebody")) {
                    body.delete();
                }
            } else if (cmd.hasOption("stdin")) {
                emailBody = Util.readEntireStdin();
            }

            if (cmd.hasOption("subject")) {
                emailSubject = cmd.getOptionValue("subject");
            }


            String username = null;
            if (cmd.hasOption("username")) {
                username = cmd.getOptionValue("username");
            }

            String password = null;
            if (cmd.hasOption("password")) {
                password = cmd.getOptionValue("password");
            }

            String[] attachments = cmd.getOptionValues("attachments");
            String[] attachmentnames = cmd.getOptionValues("attachmentnames");
            String[] destinations = cmd.getOptionValues("to");

            Credentials credentials = Authenticater.getValidCredentials(username, password);

            if (credentials != null) {
                boolean success = false;
                while (!success) {
                    try {
                        composeMail(credentials, emailSubject, emailBody, attachments, attachmentnames, destinations);
                        success = true;
                    } catch (AuthenticationFailedException e) {
                        JOptionPane.showMessageDialog(null, "Invalid login, please try again!");
                        credentials = Authenticater.getValidCredentials(username, null);
                        success = false;
                    }

                }
            }

        } catch (ParseException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
            printHelpMessage(options);
            System.exit(1);
        } catch (IOException ex) {
            System.out.println("IO Exception " + ex.getLocalizedMessage());
            printHelpMessage(options);
            System.exit(2);
        } catch (LoginException ex) {
            System.out.println(ex.getMessage());
            System.exit(3);
        }

        System.exit(0);

    }

    private static void composeMail(Credentials credentials,
            String subjectText, String bodyText,
            String[] attachments, String[] attachmentnames,
            String[] destinations) throws IOException, AuthenticationFailedException {
        if (subjectText == null) {
            subjectText = "";
        }
        if (bodyText == null) {
            bodyText = "";
        }


        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getDefaultInstance(props, null);
            URLName url = new URLName("imaps://imap.gmail.com");
            IMAPSSLStore store = new IMAPSSLStore(session, url);
            store.connect(credentials.getUsername(), credentials.getPassword());

            String signature = Signature.getSignature(credentials);
            if (signature == null) 
                signature = "";

            // Create the message
            Message draftMail = new MimeMessage(session);
            draftMail.setSubject(subjectText);
            Multipart parts = new MimeMultipart();
            BodyPart body = new MimeBodyPart();

            if (bodyText.toLowerCase().indexOf("<body") < 0) // rough guess to see if the body is html
            {
                bodyText = "<html><body>" + StringEscapeUtils.escapeHtml(bodyText) + "<br>"
                        + "</body></html>";
            }

            if (signature != null && signature != "") {
                StringBuilder b = new StringBuilder(bodyText);
                if (signature.indexOf("</") < 0) // assume it's  html if there's no </, rough guess
                {
                    signature = StringEscapeUtils.escapeHtml(signature);
                }
                b.replace(bodyText.lastIndexOf("</body>"), bodyText.lastIndexOf("</body>") + 7, "<br>" + signature + "</body>");
                bodyText = b.toString();
            }
            body.setContent(bodyText, "text/html");

            body.setDisposition("inline");

            parts.addBodyPart(body);
            if (attachments != null) {
                for (int i = 0; i < attachments.length; i++) {
                    BodyPart attachment = new MimeBodyPart();
                    DataSource source = new FileDataSource(attachments[i]);
                    attachment.setDataHandler(new DataHandler(source));
                    if (attachmentnames != null && attachmentnames.length > i) {
                        attachment.setFileName(attachmentnames[i]);
                    } else {
                        attachment.setFileName(attachments[i]);
                    }
                    parts.addBodyPart(attachment);
                }
            }
            draftMail.setContent(parts);
            if (destinations != null && destinations.length > 0) {
                InternetAddress[] toUsers = new InternetAddress[destinations.length];
                for (int i = 0; i < destinations.length; i++) {
                    toUsers[i] = new InternetAddress(destinations[i]);
                }
                draftMail.setRecipients(Message.RecipientType.TO, toUsers);

            }


            Folder[] f = store.getDefaultFolder().xlist("*");
            long threadId = 0;
            for (Folder fd : f) {
                IMAPFolder folder = (IMAPFolder) fd;
                boolean thisIsDrafts = false;
                String atts[] = folder.getAttributes();
                for (String a : atts) {
                    if (a.equalsIgnoreCase("\\Drafts")) {
                        thisIsDrafts = true;
                        break;
                    }
                }

                if (thisIsDrafts) {
                    
                    folder.open(Folder.READ_WRITE);

                    Message[] messages = new Message[1];
                    messages[0] = draftMail;
                    folder.appendMessages(messages);

                    /*
                     * Determine the Google Message Id, needed to open it in the
                     * browser. Because we just created the message it is
                     * reasonable to assume it is the last message in the draft
                     * folder. If this turns out not to be the case we could
                     * start creating the message with a random unique dummy
                     * subject, find it using that subject and then modify the
                     * subject to what it was supposed to be.
                     */
                    messages[0] = folder.getMessage(folder.getMessageCount());
                    FetchProfile fp = new FetchProfile();
                    fp.add(IMAPFolder.FetchProfileItem.X_GM_THRID);
                    folder.fetch(messages, fp);
                    IMAPMessage googleMessage = (IMAPMessage) messages[0];
                    threadId = googleMessage.getGoogleMessageThreadId();
                    folder.close(false);
                }
            }
            if (threadId == 0) {
                System.exit(6);
            }


            store.close();

            // Open the message in the default browser
            Runtime rt = Runtime.getRuntime();
            rt.exec("rundll32 url.dll,FileProtocolHandler https://mail.google.com/mail/#drafts/" + Long.toHexString(threadId));

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            System.exit(4);
        } catch (AuthenticationFailedException e) {
            throw (e);
        } catch (MessagingException e) {

            e.printStackTrace();
            System.exit(5);
        }

    }
}
