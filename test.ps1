$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$taskJava = (Get-ChildItem tools\jre -Directory | Select-Object -First 1).FullName
New-Item -ItemType Directory -Force build\test-classes | Out-Null
& "$taskJava\bin\java.exe" -jar tools\ecj.jar -encoding UTF-8 -source 8 -target 8 -d build\test-classes src\IconPixels.java src\SearchQuery.java tests\IconPixelsTest.java tests\SearchQueryTest.java
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
foreach ($taskTest in @('IconPixelsTest', 'SearchQueryTest')) {
    & "$taskJava\bin\java.exe" -cp build\test-classes "local.tv.lightlauncher.$taskTest"
    if ($LASTEXITCODE -ne 0) { throw "$taskTest failed" }
}
