# ============================================
# Función para leer archivos XLSX usando .NET
# ============================================

function Read-XlsxSheet {
    <#
    .SYNOPSIS
    Lee los datos de una hoja específica de un archivo XLSX
    
    .PARAMETER XlsxPath
    Ruta del archivo XLSX
    
    .PARAMETER SheetName
    Nombre de la hoja a leer (default: primer hoja)
    
    .EXAMPLE
    $data = Read-XlsxSheet -XlsxPath "file.xlsx" -SheetName "Todos los Pasos"
    #>
    
    param(
        [Parameter(Mandatory=$true)]
        [string]$XlsxPath,
        
        [string]$SheetName = $null
    )
    
    # Cargar ensamblados necesarios
    [System.Reflection.Assembly]::LoadWithPartialName('System.IO.Compression.FileSystem') | Out-Null
    
    if (-not (Test-Path $XlsxPath)) {
        Write-Error "Archivo XLSX no encontrado: $XlsxPath"
        return @()
    }
    
    try {
        $zip = [System.IO.Compression.ZipFile]::OpenRead($XlsxPath)
        
        # Leer workbook.xml para obtener nombres de hojas
        $workbookEntry = $zip.GetEntry('xl/workbook.xml')
        $workbookStream = $workbookEntry.Open()
        $reader = New-Object System.IO.StreamReader($workbookStream)
        $workbookXml = [xml]$reader.ReadToEnd()
        $reader.Close()
        
        # Determinar qué hoja leer
        if ([string]::IsNullOrWhiteSpace($SheetName)) {
            $SheetName = $workbookXml.workbook.sheets.sheet[0].name
        }
        
        # Encontrar el ID de la hoja
        $sheet = $workbookXml.workbook.sheets.sheet | Where-Object { $_.name -eq $SheetName }
        if (-not $sheet) {
            Write-Error "Hoja no encontrada: $SheetName"
            $zip.Dispose()
            return @()
        }
        
        $sheetId = $sheet.id
        $rId = $sheet.'r:id'
        
        # Leer workbook.xml.rels para obtener la ubicación del archivo de la hoja
        $relsEntry = $zip.GetEntry('xl/_rels/workbook.xml.rels')
        $relsStream = $relsEntry.Open()
        $relsReader = New-Object System.IO.StreamReader($relsStream)
        $relsXml = [xml]$relsReader.ReadToEnd()
        $relsReader.Close()
        
        $rel = $relsXml.Relationships.Relationship | Where-Object { $_.Id -eq $rId }
        $sheetPath = "xl/" + $rel.Target
        
        # Leer sheet XML
        $sheetEntry = $zip.GetEntry($sheetPath)
        $sheetStream = $sheetEntry.Open()
        $sheetReader = New-Object System.IO.StreamReader($sheetStream)
        $sheetXml = [xml]$sheetReader.ReadToEnd()
        $sheetReader.Close()
        
        # Leer shared strings (valores de celdas)
        $stringsEntry = $zip.GetEntry('xl/sharedStrings.xml')
        $stringsList = @()
        if ($stringsEntry) {
            $stringsStream = $stringsEntry.Open()
            $stringsReader = New-Object System.IO.StreamReader($stringsStream)
            $stringsXml = [xml]$stringsReader.ReadToEnd()
            $stringsReader.Close()
            
            $stringsXml.sst.si | ForEach-Object { $stringsList += @($_.t[0]) }
        }
        
        # Leer estilos para detectar números
        $stylesEntry = $zip.GetEntry('xl/styles.xml')
        $cellFormatMap = @{}
        if ($stylesEntry) {
            $stylesStream = $stylesEntry.Open()
            $stylesReader = New-Object System.IO.StreamReader($stylesStream)
            $stylesXml = [xml]$stylesReader.ReadToEnd()
            $stylesReader.Close()
            
            # Mapear formatos de celda
            $stylesXml.styleSheet.cellXfs.xf | ForEach-Object -Begin { $index = 0 } -Process {
                $cellFormatMap[$index] = $_.numFmtId
                $index++
            }
        }
        
        # Procesar datos
        $rows = $sheetXml.worksheet.sheetData.row
        $result = @()
        $headers = @()
        
        foreach ($row in $rows) {
            $rowData = @()
            
            foreach ($cell in $row.c) {
                $cellRef = $cell.r
                $cellValue = $null
                
                if ($cell.v) {
                    # Valor directo
                    $cellValue = $cell.v
                } elseif ($cell.t -eq "s") {
                    # Referencia a sharedStrings
                    $stringIndex = [int]$cell.v
                    $cellValue = $stringsList[$stringIndex]
                } elseif ($cell.t -eq "n") {
                    # Número
                    $cellValue = $cell.v
                }
                
                $rowData += @($cellValue)
            }
            
            # Primera fila = headers
            if ($row.r -eq "1") {
                $headers = $rowData
            } else {
                # Crear objeto PSCustomObject
                $obj = [PSCustomObject]@{}
                for ($i = 0; $i -lt $headers.Count; $i++) {
                    if ($i -lt $rowData.Count) {
                        $obj | Add-Member -NotePropertyName $headers[$i] -NotePropertyValue $rowData[$i]
                    } else {
                        $obj | Add-Member -NotePropertyName $headers[$i] -NotePropertyValue $null
                    }
                }
                $result += @($obj)
            }
        }
        
        $zip.Dispose()
        return $result
        
    } catch {
        Write-Error "Error leyendo XLSX: $_"
        return @()
    }
}

# Ejemplo de uso:
# $data = Read-XlsxSheet -XlsxPath "step_details_20260512_102932.xlsx" -SheetName "Todos los Pasos"
# $data | ForEach-Object { Write-Host "$($_.Test) - $($_.Usuario)" }
