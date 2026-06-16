# Debug XLSX Reading
$xlsxPath = 'e:\Proyectos\Reto_Siigo\Siigo_Front\Sara3\step_details_20260512_102932.xlsx'
$tempDir = 'C:\Temp\xlsx_debug'
if (Test-Path $tempDir) { Remove-Item $tempDir -Recurse -Force }
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

[System.Reflection.Assembly]::LoadWithPartialName('System.IO.Compression.FileSystem') | Out-Null
[System.IO.Compression.ZipFile]::ExtractToDirectory($xlsxPath, $tempDir)

# Debug
Write-Host 'workbook.xml exists:' (Test-Path "$tempDir\xl\workbook.xml")
Write-Host 'rels exists:' (Test-Path "$tempDir\xl\_rels\workbook.xml.rels")

[xml]$wb = Get-Content "$tempDir\xl\workbook.xml" -Encoding UTF8
Write-Host 'Sheets:'
$wb.workbook.sheets.sheet | ForEach-Object { Write-Host '  -' $_.name 'rId:' $_.'r:id' }

$firstSheet = $wb.workbook.sheets.sheet[0]
Write-Host 'First sheet attributes:'
$firstSheet.Attributes | ForEach-Object { Write-Host "  -" $_.Name "=" $_.Value }

# Intentar acceder al atributo r:id con namespace
$nsManager = New-Object System.Xml.XmlNamespaceManager($wb.NameTable)
$nsManager.AddNamespace('r', 'http://schemas.openxmlformats.org/officeDocument/2006/relationships')

# Alternativamente, try direct attribute access
$rid = $firstSheet.GetAttribute('id', 'http://schemas.openxmlformats.org/officeDocument/2006/relationships')
Write-Host 'First sheet rId:' $rid

if (-not $rid) {
    # Otro intento
    $rid = $firstSheet.Attributes['id', 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'].Value
    Write-Host 'Retry rId:' $rid
}

[xml]$rels = Get-Content "$tempDir\xl\_rels\workbook.xml.rels" -Encoding UTF8
$rel = $rels.Relationships.Relationship | Where-Object { $_.Id -eq $rid }
Write-Host 'Relation found:' ($rel -ne $null)
if ($rel) { Write-Host 'Target:' $rel.Target }

$sheetFile = $rel.Target
$sheetPath = "$tempDir\xl\$sheetFile"
Write-Host 'Sheet path:' $sheetPath
Write-Host 'Sheet exists:' (Test-Path $sheetPath)

if (Test-Path $sheetPath) {
    [xml]$sheetXml = Get-Content $sheetPath -Encoding UTF8
    $rows = $sheetXml.worksheet.sheetData.row
    Write-Host 'Total rows:' $rows.Count
    Write-Host 'First row cells:' $rows[0].c.Count
    
    # Print first row
    Write-Host 'Headers:'
    $rows[0].c | ForEach-Object { Write-Host '  -' $_.v }
}
