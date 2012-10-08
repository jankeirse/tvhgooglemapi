Tvhgooglemapi and gmaildrafter are a pair of tools that can be used to make the
following menu entries work with google mail without having to install a 
complete mailclient like Thunderbird or Outlook.
 File->Send To->Mail recipient (as attachment) (in MS Word)
 File->Send->Document as email (and the related entries in LibreOffice and 
 OpenOffice.org)
 File->Attach to email (in Acrobat Reader)
 
All other windows applications that use Simple MAPI MAPISendMail might work but 
have not been tested.
When installing tvhgooglemapi gmaildrafter is automatically installed. 
tvhgooglemapi is the part that integrates with windows and gmaildrafter actually
uploads the mail as a draft and opens the draft in your default webbrowser. 
In theory gmaildrafter could also be used to implement other 'send mail' 
functions. 
 
In addition to that you also need a Java Runtime (at least version 1.6)
You can get one here: http://www.oracle.com/technetwork/java/index.html  

Imap is used to upload the mail, so you need to enable IMAP in your gmail 
settings and imaps trafic has to be allowed on the network (port 993.)

If you're using multiple accounts with gmail the primary logged in account in 
your browser must be the one you want to use for this application or it won't 
work.

The application can't determine the signature of the user. If you want to 
include a signature in the mails created by this application you can put 
it in the file %USERPROFILE%\.gmaildrafter\signatures\username@domain.com.html

When installing the required registry keys are automatically set. 
In case you ever need to modify them (ie after temporarily changing the default 
mailclient) you need the following settings:

[HKEY_LOCAL_MACHINE\SOFTWARE\Clients\Mail\tvhgooglemapi]
@="tvhgooglemapi"
"DLLPath"="C:\\Program Files (x86)\\tvhgooglemapi\\tvhgooglemapi.dll"

[HKEY_CURRENT_USER\Software\Clients\Mail]
@="tvhgooglemapi"

If a user chooses to store his login credentials these are stored in 
%USERPROFILE%\.gmaildrafter . While the password is hidden an average developer 
will be able to decode it, so keep these files private and do not store the 
password on a computer you don't trust. 

If you want to automatically attach a signature to your email you can put a file 
in %USERPROFILE%\.gmaildrafter\signatures\yourusername@yourdomainname.com.html 
which contains the text.     

If you want to compile the dll yourself you'll need Microsoft Visual C++ 2010 
Express Edition 
(http://www.microsoft.com/visualstudio/en-us/products/2010-editions/visual-cpp-express) 
and boost (http://www.boost.org/).

The Java application uses Apache Commons CLI (http://commons.apache.org/cli/), 
Apache Commons IO (http://commons.apache.org/io/) and Apache Commons Lang
(http://commons.apache.org/lang/) in addition to the java-gmail-imap 
implementation of JavaMail (https://code.google.com/p/java-gmail-imap/). 
The sources include a Netbeans Project, but another ide should do just fine.

The installer is made with innosetup.    
     