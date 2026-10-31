$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$jdk = (Get-ChildItem tools\jre -Directory | Select-Object -First 1).FullName
$bt = (Get-ChildItem tools\build-tools -Directory | Select-Object -First 1).FullName
$platform = (Get-ChildItem tools\platform -Directory | Select-Object -First 1).FullName
$env:JAVA_HOME = $jdk
$env:PATH = "$jdk\bin;$env:PATH"
function Check { if ($LASTEXITCODE -ne 0) { throw "Build failed: $LASTEXITCODE" } }
New-Item -ItemType Directory -Force build\classes,build\dex | Out-Null
New-Item -ItemType Directory -Force assets\fonts | Out-Null
Get-ChildItem -LiteralPath build\classes -Recurse -File -Filter '*.class' | ForEach-Object { Remove-Item -LiteralPath $_.FullName }
& "$jdk\bin\java.exe" -jar tools\ecj.jar -encoding UTF-8 -source 8 -target 8 -classpath "$platform\android.jar" -d build\classes src\MainActivity.java src\IconPixels.java src\SearchQuery.java
Check
$classes = @(Get-ChildItem build\classes -Recurse -Filter *.class | ForEach-Object FullName)
& "$bt\d8.bat" --lib "$platform\android.jar" --min-api 23 --output build\dex $classes
Check
& "$bt\aapt.exe" package -f -M AndroidManifest.xml -A assets -I "$platform\android.jar" -F build\unsigned.apk
Check
Copy-Item build\dex\classes.dex build\classes.dex -Force
Push-Location build
& "$bt\aapt.exe" add unsigned.apk classes.dex
Check
Pop-Location
& "$bt\zipalign.exe" -f 4 build\unsigned.apk build\aligned.apk
Check
if (-not (Test-Path build\launcher.keystore)) {
    & "$jdk\bin\keytool.exe" -genkeypair -keystore build\launcher.keystore -storepass android -keypass android -alias launcher -dname 'CN=Local TV Launcher' -keyalg RSA -keysize 2048 -validity 10000
    Check
}
& "$bt\apksigner.bat" sign --ks build\launcher.keystore --ks-pass pass:android --out build\our-tv.apk build\aligned.apk
Check
& "$bt\apksigner.bat" verify build\our-tv.apk
Check
