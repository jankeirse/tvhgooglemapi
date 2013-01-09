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

#include "stdafx.h"
#include "tvhgooglemapi.h"
#include <string>
#include <MAPI.h>
#include <stdlib.h>
#include <string>
#include <sstream>
#include <iostream>
#include <fstream>
#include <boost/algorithm/string/replace.hpp>

using namespace std;

ULONG session;
// near the top of your CPP file
EXTERN_C IMAGE_DOS_HEADER __ImageBase;
string dllInstallationPath;


std::wstring s2ws(const std::string& s)
{ 
 int len;
 int slength = (int)s.length() + 1;
 len = MultiByteToWideChar(CP_ACP, 0, s.c_str(), slength, 0, 0); 
 wchar_t* buf = new wchar_t[len];
 MultiByteToWideChar(CP_ACP, 0, s.c_str(), slength, buf, len);
 std::wstring r(buf);
 delete[] buf;
 return r;
}

std::string ws2s(const std::wstring& s)
{
 int len;
 int slength = (int)s.length() + 1;
 len = WideCharToMultiByte(CP_ACP, 0, s.c_str(), slength, 0, 0, 0, 0); 
 char* buf = new char[len];
 WideCharToMultiByte(CP_ACP, 0, s.c_str(), slength, buf, len, 0, 0); 
 std::string r(buf);
 delete[] buf;
 return r;
}

template <class T>
std::string to_string(T t, std::ios_base & (__cdecl *f)(std::ios_base&))
{
  std::ostringstream oss;
  oss << f << t;
  return oss.str();
}


std::string getInstallationPath(){
	LPTSTR dllInstallationPath;
	// and then, anywhere you need it:
	dllInstallationPath = new TCHAR[_MAX_PATH];
	::GetModuleFileName((HINSTANCE)&__ImageBase, dllInstallationPath, _MAX_PATH);
	string dllPath = ws2s(dllInstallationPath);
	dllPath = dllPath.substr(0,dllPath.find_last_of("/\\"));
	return dllPath;
}

#define BUFSIZE 1024

int writeTempFile(string fileContent, string* result){
	
    DWORD dwRetVal = 0;
    UINT uRetVal   = 0;

    TCHAR szTempFileName[MAX_PATH];  
    TCHAR lpTempPathBuffer[MAX_PATH];
    
     //  Gets the temp path env string (no guarantee it's a valid path).
    dwRetVal = GetTempPath(MAX_PATH,          // length of the buffer
                           lpTempPathBuffer); // buffer for path 
    if (dwRetVal > MAX_PATH || (dwRetVal == 0))
    {
        return 3;
    }

    //  Generates a temporary file name. 
    uRetVal = GetTempFileName(lpTempPathBuffer, // directory for tmp files
                              TEXT("tvhgooglemapi"),     // temp file name prefix 
                              0,                // create unique name 
                              szTempFileName);  // buffer for name 
    if (uRetVal == 0)
    {
        return (3);
    }

	ofstream tempfile;

	tempfile.open(szTempFileName);
	tempfile << fileContent ;
	tempfile.close();
	*result = ws2s(szTempFileName);
	return 0;
    

}

BOOL WINAPI DllMain(HINSTANCE aInstance, DWORD aReason, LPVOID aReserved)
{
	if (aReason == DLL_PROCESS_ATTACH) dllInstallationPath = getInstallationPath();		
	return TRUE;
} 

ULONG FAR PASCAL MAPILogon(ULONG aUIParam, 
	LPSTR aProfileName,
	LPSTR aPassword,
	FLAGS aFlags,
	ULONG aReserved,
	LPLHANDLE aSession)
{
	session = 7; // We completely ignore the session stuff. Filling in any number to keep the MAPIClient happy. 
	*aSession = *&session;

	return SUCCESS_SUCCESS;
}

ULONG FAR PASCAL MAPILogoff (LHANDLE aSession, ULONG aUIParam,
                                            FLAGS aFlags, ULONG aReserved)
{
	return SUCCESS_SUCCESS;
}


ULONG FAR PASCAL MAPISendMail (LHANDLE lhSession, ULONG ulUIParam, lpMapiMessage lpMessage,
                FLAGS flFlags, ULONG ulReserved )
{
	ULONG exitCode = MAPI_E_FAILURE ;
   if(lpMessage->nFileCount > 0 ) {
	   string subject;
	   if (lpMessage->lpszSubject != NULL)
		   subject = lpMessage->lpszSubject;
	   else 
		   subject = "";
	   
	   string body;
	   if (lpMessage->lpszNoteText != NULL)
			body = lpMessage->lpszNoteText;
	   else
			body = "";
	   boost::replace_all(subject,"\\","\\\\");
	   boost::replace_all(subject,"\"","\\\"");
	   STARTUPINFOA siStartupInfo;
	   PROCESS_INFORMATION piProcessInfo;
	   memset(&siStartupInfo, 0, sizeof(siStartupInfo));
	   memset(&piProcessInfo, 0, sizeof(piProcessInfo));
	   siStartupInfo.cb = sizeof(siStartupInfo);
	   string parameters = " /C java.exe -Dfile.encoding=UTF8 -jar \"" + dllInstallationPath + "\\gmaildrafter.jar\" -s \"" + subject + "\"";
	   for(unsigned int i = 0; i < lpMessage->nFileCount; i++) {
		   parameters = parameters + " -a \"" +  lpMessage->lpFiles[i].lpszPathName + "\"" ;
		   if (lpMessage->lpFiles[i].lpszFileName != NULL) 
			  parameters = parameters +  " -n \"" + lpMessage->lpFiles[i].lpszFileName + "\""  ;
	   }
	   
	   for (unsigned int i = 0; i < lpMessage->nRecipCount; i++) {
		   if (lpMessage->lpRecips[i].lpszAddress != NULL)
			parameters = parameters + " -t \"" + lpMessage->lpRecips[i].lpszAddress + "\"" ;
	   } 
	   
	   string bodyfile;
	   int writeResult = writeTempFile( body ,&bodyfile);
	   if (writeResult != 0) {
		   MessageBox(NULL,L"Could not send your mail, writing bodyfile failed!",L"MAPI Error",MB_ICONERROR | MB_TOPMOST);
		   return MAPI_E_FAILURE;
	   } else 
		   parameters = parameters + " -b \"" + bodyfile + "\" -d";
		   
	   LPSTR lpparameters = (LPSTR)(parameters.c_str());
	   if( CreateProcessA(  "c:\\windows\\system32\\cmd.exe",	
							lpparameters, 
							0, 
							0, 
							false, 
							CREATE_DEFAULT_ERROR_MODE  | CREATE_NO_WINDOW ,
							0,
							0,
							&siStartupInfo, 
							&piProcessInfo  
							) == FALSE) {
			MessageBox(NULL,L"Could not send your mail, starting gmaildrafter failed!",L"MAPI Error",MB_ICONERROR | MB_TOPMOST);
			return MAPI_E_FAILURE;
		} else {
			WaitForSingleObject(piProcessInfo.hProcess, INFINITE);
			DWORD dwExitCode = -1;
			GetExitCodeProcess( piProcessInfo.hProcess, &dwExitCode);
  
			if (dwExitCode != 0)  {
				if(dwExitCode == 5) 
					exitCode = MAPI_E_LOGIN_FAILURE;
				else if (dwExitCode == 3 ) 
					exitCode = MAPI_E_USER_ABORT;
				else {
					string errormessage = "Could not send your mail, exitcode of gmaildrafter was " + to_string<DWORD>(dwExitCode, std::hex);
					MessageBox(NULL,s2ws(errormessage).c_str(),L"MAPI Error",MB_ICONERROR | MB_TOPMOST);
					exitCode = MAPI_E_FAILURE;   
				}
			} else 
				exitCode = SUCCESS_SUCCESS;
		}
	   
   }
   
	return exitCode ; 
}



LONG FAR PASCAL MAPISendMailHelper (LHANDLE lhSession, ULONG ulUIParam, LHANDLE *lpMessage,
                FLAGS flFlags, ULONG ulReserved )
{

   MessageBox(NULL,L"Your application is using mapisendmailHelper, we don't support that!",L"MAPI tracing",NULL);
	return MAPI_E_NOT_SUPPORTED ; 
}

ULONG FAR PASCAL MAPISendDocuments(ULONG ulUIParam, LPTSTR lpszDelimChar, LPTSTR lpszFilePaths,
                                LPTSTR lpszFileNames, ULONG ulReserved)
{
	MessageBox(NULL,L"Your application is using mapisenddocuments, we don't support that!",L"MAPI tracing",MB_ICONERROR);
    return MAPI_E_NOT_SUPPORTED ; 
}

ULONG FAR PASCAL MAPIFindNext(LHANDLE lhSession, ULONG ulUIParam, LPTSTR lpszMessageType,
                              LPTSTR lpszSeedMessageID, FLAGS flFlags, ULONG ulReserved,
                              unsigned char lpszMessageID[64])
{
	MessageBox(NULL,L"Your application called MAPIFindNext, we don't support that!",L"MAPI tracing",MB_ICONERROR);
  return MAPI_E_NOT_SUPPORTED ; 
}


ULONG FAR PASCAL MAPIReadMail(LHANDLE lhSession, ULONG ulUIParam, LPTSTR lpszMessageID,
                              FLAGS flFlags, ULONG ulReserved, LHANDLE **lppMessage)
{
	MessageBox(NULL,L"Your application called mapireadmail, we don't support that!",L"MAPI tracing",MB_ICONERROR);
 return MAPI_E_NOT_SUPPORTED ; 

}

ULONG FAR PASCAL MAPISaveMail(LHANDLE lhSession, ULONG ulUIParam, LHANDLE lpMessage,
                              FLAGS flFlags, ULONG ulReserved, LPTSTR lpszMessageID)
{
	MessageBox(NULL,L"Your application called mapisavemail, we don't support that!",L"MAPI tracing",MB_ICONERROR);
  return MAPI_E_NOT_SUPPORTED ; 
}

ULONG FAR PASCAL MAPIDeleteMail(LHANDLE lhSession, ULONG ulUIParam, LPTSTR lpszMessageID,
                                FLAGS flFlags, ULONG ulReserved)
{
	MessageBox(NULL,L"Your application called mapideletemail, we haven't implemented that!",L"MAPI tracing",MB_ICONERROR);
  return MAPI_E_NOT_SUPPORTED ; 
}

ULONG FAR PASCAL MAPIAddress(LHANDLE lhSession, ULONG ulUIParam, LPTSTR lpszCaption,
                             ULONG nEditFields, LPTSTR lpszLabels, ULONG nRecips,
                             lpMapiRecipDesc lpRecips, FLAGS flFlags,
                             ULONG ulReserved, LPULONG lpnNewRecips,
                             lpMapiRecipDesc FAR *lppNewRecips)
{
	MessageBox(NULL,L"Your application called mapiaddress, we haven't implemented that!",L"MAPI tracing",MB_ICONERROR);
    return MAPI_E_NOT_SUPPORTED;
}

ULONG FAR PASCAL MAPIDetails(LHANDLE lhSession, ULONG ulUIParam, lpMapiRecipDesc lpRecip,
                             FLAGS flFlags, ULONG ulReserved)
{
	MessageBox(NULL,L"Your application called mapidetails, we haven't implemented that!",L"MAPI tracing",MB_ICONERROR);
    return MAPI_E_NOT_SUPPORTED;
}

ULONG FAR PASCAL MAPIResolveName(LHANDLE lhSession, ULONG ulUIParam, LPTSTR lpszName,
                                 FLAGS flFlags, ULONG ulReserved, lpMapiRecipDesc FAR *lppRecip)
{
  MessageBox(NULL,L"Your application called mapiresolvename, we haven't implemented that!",L"MAPI tracing",MB_ICONERROR);
  return MAPI_E_NOT_SUPPORTED;
}

ULONG FAR PASCAL MAPIFreeBuffer(LPVOID pv)
{
  MessageBox(NULL,L"Your application called MAPIFreeBuffer, we haven't implemented that!",L"MAPI tracing",MB_ICONERROR);
  pv = NULL;
  return SUCCESS_SUCCESS; 
}

