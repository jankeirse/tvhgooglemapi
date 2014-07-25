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
        options.addOption(OptionBuilder.withLongOpt("subjectfile").withArgName("filename").hasArg().withDescription("File containing the subject of the mail (UTF-8)").create());
        options.addOption(OptionBuilder.withLongOpt("attachments").hasArgs().withArgName("filename,filename,...").withValueSeparator(',').withDescription("Attachments").create("a"));

        options.addOption(OptionBuilder.withLongOpt("attachmentnames").hasArgs().withArgName("filename,filename,...").withValueSeparator(',').withDescription("Attachment names").create("n"));

        options.addOption(OptionBuilder.withLongOpt("to").hasArgs().withArgName("foo@bar.com,oof@rab.com,...").withValueSeparator(',').withDescription("destination").create("t"));

        options.addOption(new Option("d", "deletebody", false, "Delete bodyfile after sending."));
        options.addOption(new Option(null, "deletesubjectfile", false, "Delete subjectfile after sending."));
        
        options.addOption(new Option(null,"immediate",false,"Immediately send, don't open draft first."));
        options.addOption(OptionBuilder.withLongOpt("cc").hasArgs().withArgName("foo@bar1.com,foo@rba.com").withValueSeparator(',').withDescription("cc").create("c"));
        options.addOption(OptionBuilder.withLongOpt("bcc").hasArgs().withArgName("foo@bar2.com,ofo@bar.com").withValueSeparator(',').withDescription("bcc").create());

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

            if (cmd.hasOption("subject")) 
                emailSubject = cmd.getOptionValue("subject");
            else if (cmd.hasOption("subjectfile")) {
                String subjectFile = cmd.getOptionValue("subjectfile");
                File subject = new File(subjectFile);
                emailSubject = FileUtils.readFileToString(subject);
                if (cmd.hasOption("deletesubjectfile"))
                    subject.delete();
            }
            
            String username = null;
            if (cmd.hasOption("username")) 
                username = cmd.getOptionValue("username");
            
            String password = null;
            if (cmd.hasOption("password")) 
                password = cmd.getOptionValue("password");
                        
            String[] bcc = cmd.getOptionValues("bcc");
            String[] cc = cmd.getOptionValues("cc");
            
            Boolean sendImmediately = cmd.hasOption("immediate");
            
            String[] attachments = cmd.getOptionValues("attachments");
            String[] attachmentnames = cmd.getOptionValues("attachmentnames");
            String[] destinations = cmd.getOptionValues("to");

            Credentials credentials = Authenticater.getValidCredentials(username, password);

            if (credentials != null) {
                boolean success = false;
                while (!success) {
                    try {
                        composeMail(credentials, 
                                    emailSubject, 
                                    emailBody, 
                                    attachments, 
                                    attachmentnames, 
                                    destinations,
                                    cc,
                                    bcc,
                                    sendImmediately);
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
            System.exit(7);
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
    
    private static InternetAddress[] stringToInternetAddress (String[] addresses) throws com.google.code.javax.mail.internet.AddressException{
        if (addresses != null && addresses.length > 0) {
            InternetAddress[] ia = new InternetAddress[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                ia[i] = new InternetAddress(addresses[i]);
            }
            return ia;

        } else        
            return null;
    }
            
            
    private static void composeMail(Credentials credentials,
            String subjectText, 
            String bodyText,
            String[] attachments, 
            String[] attachmentnames,
            String[] destinations,
            String[] cc,
            String[] bcc,
            Boolean sendImmediately) throws IOException, AuthenticationFailedException {
        if (subjectText == null) {
            subjectText = "";
        }
        if (bodyText == null) {
            bodyText = "";
        }

        try {
            Properties props = null; 
            Session session = null;
            if (!sendImmediately) { 
                props = System.getProperties();
                props.setProperty("mail.store.protocol", "imaps");
                session = Session.getDefaultInstance(props, null);
            }
            else {
                props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
                final String username = credentials.getUsername();
                final String password = credentials.getPassword();
                
                session = Session.getInstance(props, 
                        new  Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(username, password );
                            } 
                        }
                        );
            }
            
            

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
                bodyText = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>" + StringEscapeUtils.escapeHtml(bodyText).replace("\n", "<br />" +  "\n") + "<br>"
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
            
            body.setContent(bodyText, "text/html; charset=utf-8");

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
                        File file = new File(attachments[i]);
                        attachment.setFileName(file.getName());                        
                    }
                    parts.addBodyPart(attachment);
                }
            }
            draftMail.setContent(parts);
            if (destinations != null && destinations.length > 0) 
                draftMail.setRecipients(Message.RecipientType.TO, stringToInternetAddress(destinations));
            if (cc != null && cc.length > 0) 
                draftMail.setRecipients(Message.RecipientType.CC , stringToInternetAddress(cc));
            if (bcc != null && bcc.length > 0) 
                draftMail.setRecipients(Message.RecipientType.BCC , stringToInternetAddress(bcc));
            draftMail.setFlag(Flags.Flag.SEEN, true);


            if (sendImmediately) {
                Transport.send(draftMail);
            } else {
                URLName url = new URLName("imaps://imap.gmail.com");
                IMAPSSLStore store = new IMAPSSLStore(session, url);
                store.connect(credentials.getUsername(), credentials.getPassword());


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
                String drafturl = "https://mail.google.com/mail/#drafts/" + Long.toHexString(threadId);
                
                File chrome = new File("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
                if (!chrome.exists()) 
                    chrome = new File("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
                if (!chrome.exists()) {
                    // Chrome not found, using default browser
                    rt.exec("rundll32 url.dll,FileProtocolHandler " + drafturl);
                } else {
                    String[] commandLine = new String[2];
                    commandLine[0] = chrome.getPath();
                    commandLine[1] = drafturl;
                    rt.exec(commandLine);
                }
                
            } // else branch for sendImmediately

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
