; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "tvhgooglemapi"
#define MyAppVersion "0.5"
#define MyAppPublisher "TVH Corporate services NV"
#define MyAppURL "http://code.google.com/p/tvhgooglemapi/"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{4E637CBF-B2AD-4CA1-AE0E-9445449380CB}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
; AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={pf}\{#MyAppName}
DefaultGroupName={#MyAppName}
LicenseFile=LICENSE-2.0.txt
OutputDir=installer
OutputBaseFilename=tvhgooglemapisetup-{#MyAppVersion}
Compression=lzma
SolidCompression=yes                          

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "tvhgooglemapi\Release\tvhgooglemapi.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "tvhgooglemapi\Release\tvhgooglemapi.exp"; DestDir: "{app}"; Flags: ignoreversion
Source: "tvhgooglemapi\Release\tvhgooglemapi.lib"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\gmaildrafter.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "LICENSE-2.0.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "readme.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

[Registry]
Root: HKCU; Subkey: "Software\Clients\Mail"; ValueType: string; ValueData: "tvhgooglemapi"; Flags: uninsdeletekeyifempty
Root: HKLM; Subkey: "Software\Clients\Mail"; ValueType: string; ValueData: "tvhgooglemapi"; Flags: uninsdeletekeyifempty
Root: HKLM; Subkey: "Software\Clients\Mail\tvhgooglemapi"; Flags: uninsdeletekey
Root: HKLM; Subkey: "Software\Clients\Mail\tvhgooglemapi"; ValueType: string; ValueData: "tvhgooglemapi"; Flags: uninsdeletekey
Root: HKLM; Subkey: "Software\Clients\Mail\tvhgooglemapi"; ValueName: "DLLPath"; ValueData: "{app}\tvhgooglemapi.dll"; ValueType: string; Flags: uninsdeletekey
Root: HKLM; Subkey: "Software\Microsoft\Windows Messaging Subsystem"; ValueName: "MAPI"; ValueData: "1"; ValueType: string; 
Root: HKLM; Subkey: "Software\Microsoft\Windows Messaging Subsystem"; ValueName: "CMC"; ValueData: "1"; ValueType: string;
Root: HKLM; Subkey: "Software\Microsoft\Windows Messaging Subsystem"; ValueName: "CMCDLLNAME"; ValueData: "Mapi.dll"; ValueType: string;
Root: HKLM; Subkey: "Software\Microsoft\Windows Messaging Subsystem"; ValueName: "CMCDLLNAME32"; ValueData: "Mapi32.dll"; ValueType: string;
Root: HKLM; Subkey: "Software\Microsoft\Windows Messaging Subsystem"; ValueName: "MAPIX"; ValueData: "1"; ValueType: string;
Root: HKLM; Subkey: "Software\Microsoft\Windows Messaging Subsystem"; ValueName: "MAPIXVER"; ValueData: "1"; ValueType: string;
Root: HKLM; Subkey: "Software\Microsoft\Windows Messaging Subsystem"; ValueName: "OLEMessaging"; ValueData: "1"; ValueType: string;
