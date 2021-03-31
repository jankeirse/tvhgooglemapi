### Deprecation notice
This is no longer expected to work. It should be rewritten using the GMAIL API to continue to work. Since it is no longer in use by the author or the company he works for and nobody has volunteered to do the rewrite, it's not expected to happen. 

### TVHGoogleMapi

When you move to gmail or google apps you can no longer use the build in 'send to mail recipient as attachment' menu option of MS Office and other windows applications without installing a local mail client. The Right click menu of files in explorer Send To -> Mail recipient doesn't work either. Tvhgooglemapi is a simple tool that pretends to be a real mail client to windows but really only uploads the mail to the drafts folder of gmail and then opens the draft in the default webbrowser. For the user this is almost exactly the same as having the gmail web interface as the default mailclient for some windows applications (the only difference being that he has to login twice if he is not already logged in to gmail and doesn't let the tool remember the password.)

### Also usefull
Users of this tool will typically want (but do not have) to complement it with google chrome (which can handle mailto links in windows) or something like [http://dl.google.com/tag/s/ap=google.com/googlewebapps/en/googleappsstandalonesetuptagged.exe](http://dl.google.com/tag/s/ap=google.com/googlewebapps/en/googleappsstandalonesetuptagged.exe)

By default tvhgooglemapi will open the draft in a new browser tab/window. If you are using Google Chrome there is an extension that makes sure that only one tab for gmail is open (and also only one for google calendar,...) Using this extension will cause the draft to be opened in the tab where you had gmail open, some people may find that desireable. You can find it here: [https://chrome.google.com/webstore/detail/tab-fixer-for-google-apps/cplbkecindmpapmnffepmnbiogpllcni/details](https://chrome.google.com/webstore/detail/tab-fixer-for-google-apps/cplbkecindmpapmnffepmnbiogpllcni/details) It's support page is here: [https://code.google.com/p/google-apps-tab-fixer/wiki/FAQ](https://code.google.com/p/google-apps-tab-fixer/wiki/FAQ
)

### Readme, please read it
There are some details you might want to know in the readme: [https://raw.githubusercontent.com/jankeirse/tvhgooglemapi/master/readme.txt](https://raw.githubusercontent.com/jankeirse/tvhgooglemapi/master/readme.txt)

### Download
The application is no longer available. 

### Support and feedback
There is no support available.

### License
The code is licensed under the Apache 2.0 License. [http://www.apache.org/licenses/LICENSE-2.0.html](http://www.apache.org/licenses/LICENSE-2.0.html)

### Request for help
I'm looking for a Windows C/C++ developer who can get the DLL to handle international encodings (unicode) properly. This is probably very very easy for someone who's used to working in C/C++ with Visual Studio but I don't have time to look into this myself. Please get in touch or respond to [https://github.com/jankeirse/tvhgooglemapi/issues/2](https://github.com/jankeirse/tvhgooglemapi/issues/2) if you can help.

### Release Notes
## 0.8.2 2014-08-05

Make login window always on top to fix [https://github.com/jankeirse/tvhgooglemapi/issues/7](https://github.com/jankeirse/tvhgooglemapi/issues/7)
Make login window complain if no email address or password is provided.

## 0.8.1 2014-07-25

Give attachment only filename instead of full path name if mapi client does not provide filename. Fix for [https://github.com/jankeirse/tvhgooglemapi/issues/10](https://github.com/jankeirse/tvhgooglemapi/issues/10)

## 0.8.0 2014-01-24

Bug Fixes

## 0.7.0 2014-01-10

Support some features of excel. Fix [https://github.com/jankeirse/tvhgooglemapi/issues/4](https://github.com/jankeirse/tvhgooglemapi/issues/4)
Prepare java code for UTF-8 input (C code still needs changes.)
Set draft flag read.
