; Script optimizado para automatización con Maven

; 1. Recibimos la versión de Maven (o usamos una por defecto si compilas a mano)
#ifndef MyAppVersion
  #define MyAppVersion "1.2.2"
#endif

#define MyAppName "PriceStocker"
#define MyAppPublisher "vluevano_2003"
#define MyAppExeName "PriceStocker.exe"
#define MyAppAssocName MyAppName + "Installer_v" + MyAppVersion
#define MyAppAssocExt ".myp"
#define MyAppAssocKey StringChange(MyAppAssocName, " ", "") + MyAppAssocExt

[Setup]
AppId={{065EC86A-65F7-4590-9582-4CFDF525CE15}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
UninstallDisplayIcon={app}\{#MyAppExeName}
ChangesAssociations=yes
DisableProgramGroupPage=yes

; 2. Rutas relativas: Guardamos el instalador final en la carpeta target de Maven
OutputDir=target
OutputBaseFilename=PriceStocker-Installer-{#MyAppVersion}

; 3. Ruta relativa para el ícono
SetupIconFile=src\main\resources\images\icon.ico
SolidCompression=yes
WizardStyle=modern dynamic

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; 4. Rutas relativas para los archivos fuente
Source: "target\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion

; 5. ¡ATENCIÓN AL JRE! 
; Coloca tu carpeta "jre" (con Java 21) directamente en la raíz de tu proyecto, junto al pom.xml
; Así Launch4j y el instalador la encontrarán siempre sin importar de quién sea la PC.
Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

[Registry]
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocExt}\OpenWithProgids"; ValueType: string; ValueName: "{#MyAppAssocKey}"; ValueData: ""; Flags: uninsdeletevalue
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}"; ValueType: string; ValueName: ""; ValueData: "{#MyAppAssocName}"; Flags: uninsdeletekey
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\{#MyAppExeName},0"
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#MyAppExeName}"" ""%1"""

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent