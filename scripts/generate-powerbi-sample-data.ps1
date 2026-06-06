# Genere les CSV du modele en etoile Crédit & Risque pour Power BI Desktop
# Usage: .\scripts\generate-powerbi-sample-data.ps1

$ErrorActionPreference = "Stop"
$outDir = Join-Path $PSScriptRoot "..\powerbi\data"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$rng = [System.Random]::new(42)

function Write-CsvUtf8 {
    param([string]$Path, [object[]]$Rows)
    $Rows | Export-Csv -Path $Path -NoTypeInformation -Encoding UTF8
}

# --- Dimensions ---
$dimDate = @()
$idDate = 1
foreach ($year in 2023..2025) {
    for ($m = 1; $m -le 12; $m++) {
        $date = Get-Date -Year $year -Month $m -Day 1
        $q = [math]::Ceiling($m / 3)
        $dimDate += [pscustomobject]@{
            ID_DATE   = $idDate
            Date      = $date.ToString("yyyy-MM-dd")
            Annee     = $year
            Trimestre = "T$q $year"
            Mois      = $date.ToString("MMMM yyyy", [cultureinfo]"fr-FR")
        }
        $idDate++
    }
}
Write-CsvUtf8 (Join-Path $outDir "DIM_DATE.csv") $dimDate

$dimDr = @(
    [pscustomobject]@{ ID_DR = 1; "DIRECTION REGIONALE" = "DR Tunis" },
    [pscustomobject]@{ ID_DR = 2; "DIRECTION REGIONALE" = "DR Sfax" },
    [pscustomobject]@{ ID_DR = 3; "DIRECTION REGIONALE" = "DR Sousse" },
    [pscustomobject]@{ ID_DR = 4; "DIRECTION REGIONALE" = "DR Bizerte" },
    [pscustomobject]@{ ID_DR = 5; "DIRECTION REGIONALE" = "DR Gabes" }
)
Write-CsvUtf8 (Join-Path $outDir "Dim_DR.csv") $dimDr

$dimSegment = @(
    [pscustomobject]@{ ID_SEG = 1; SEGMENT = "Particulier" },
    [pscustomobject]@{ ID_SEG = 2; SEGMENT = "Professionnel" },
    [pscustomobject]@{ ID_SEG = 3; SEGMENT = "PME" },
    [pscustomobject]@{ ID_SEG = 4; SEGMENT = "Grande Entreprise" }
)
Write-CsvUtf8 (Join-Path $outDir "Dim_SEGMENT.csv") $dimSegment

$gouvernorats = @("Tunis", "Ariana", "Sfax", "Sousse", "Bizerte", "Gabes", "Nabeul", "Monastir", "Kairouan", "Gafsa")
$dimAgence = @()
$agId = 1
foreach ($dr in $dimDr) {
    foreach ($i in 1..3) {
        $gov = $gouvernorats[$rng.Next(0, $gouvernorats.Length)]
        $dimAgence += [pscustomobject]@{
            ID_AGENCE = $agId
            AGENCE    = "Agence $($dr.'DIRECTION REGIONALE'.Replace('DR ','')) $i"
            LIB_GOUV  = $gov
            ID_DR     = $dr.ID_DR
        }
        $agId++
    }
}
Write-CsvUtf8 (Join-Path $outDir "Dim_Agence.csv") $dimAgence

$dimAct = @(
    [pscustomobject]@{ ID_ACTIVITE = 1; ACTIVITE = "Crédit consommation" },
    [pscustomobject]@{ ID_ACTIVITE = 2; ACTIVITE = "Crédit immobilier" },
    [pscustomobject]@{ ID_ACTIVITE = 3; ACTIVITE = "Leasing" },
    [pscustomobject]@{ ID_ACTIVITE = 4; ACTIVITE = "Crédit équipement" }
)
Write-CsvUtf8 (Join-Path $outDir "Dim_ACT.csv") $dimAct

$classesRisque = @("A - Faible", "B - Modéré", "C - Élevé", "D - Critique")
$nbClients = 400
$dimClt = @()
for ($c = 1; $c -le $nbClients; $c++) {
    foreach ($year in 2023..2025) {
        $dimClt += [pscustomobject]@{
            ID_CLT        = $c
            Key_CLT_Annee = "$c-$year"
            Nom_Client    = "Client $c"
        }
    }
}
Write-CsvUtf8 (Join-Path $outDir "Dim_CLT.csv") $dimClt

# --- Faits (grain client x mois pour 2024-2025) ---
$factClt = @()
$factCredit = @()
$factRisque = @()

foreach ($row in $dimDate | Where-Object { $_.Annee -ge 2024 }) {
    $dateId = $row.ID_DATE
    $year = $row.Annee
    $monthFactor = 1 + ($row.ID_DATE % 12) * 0.02

    for ($c = 1; $c -le $nbClients; $c++) {
        if ($rng.NextDouble() -gt 0.35) { continue }

        $seg = $rng.Next(1, 5)
        $act = $rng.Next(1, 5)
        $ag = $dimAgence[$rng.Next(0, $dimAgence.Count)]
        $dr = $ag.ID_DR
        $key = "$c-$year"

        $encours = [math]::Round(($rng.Next(5000, 500000)) * $monthFactor, 0)
        $revenu = [math]::Round($encours * ($rng.NextDouble() * 0.05 + 0.01), 0)

        $factClt += [pscustomobject]@{
            ID_CLT                  = $c
            ID_DATE                 = $dateId
            Key_CLT_Annee           = $key
            ID_DR                   = $dr
            ID_SEG                  = $seg
            ID_AGENCE               = $ag.ID_AGENCE
            ID_ACTIVITE             = $act
            "ENCOURS / MN"          = $encours
            "REVENU MENSUEL(mD)"    = $revenu
        }

        $engagement = [math]::Round($encours * ($rng.NextDouble() * 0.4 + 0.8), 0)
        $impayeRate = if ($seg -ge 3) { $rng.NextDouble() * 0.12 } else { $rng.NextDouble() * 0.06 }
        $impaye = [math]::Round($engagement * $impayeRate, 0)
        $decouvert = [math]::Round($engagement * ($rng.NextDouble() * 0.05), 0)

        $factCredit += [pscustomobject]@{
            ID_CLT     = $c
            ID_DATE    = $dateId
            engagement = $engagement
            impayee    = $impaye
            decouvert  = $decouvert
        }

        $defaut = if ($impayeRate -gt 0.08) { 1 } elseif ($impayeRate -gt 0.04) { if ($rng.NextDouble() -gt 0.5) { 1 } else { 0 } } else { 0 }
        $nbDefauts = if ($defaut -eq 1) { $rng.Next(1, 4) } else { 0 }
        $tauxDefaut = if ($engagement -gt 0) { [math]::Round($nbDefauts / ($engagement / 100000), 4) } else { 0 }
        $classe = if ($defaut -eq 1) { $classesRisque[$rng.Next(2, 4)] } else { $classesRisque[$rng.Next(0, 2)] }

        $factRisque += [pscustomobject]@{
            ID_CLT        = $c
            ID_DATE       = $dateId
            DEFAUT        = $defaut
            "Nb Defauts"  = $nbDefauts
            Taux_Defaut   = $tauxDefaut
            CLASSE_RISQUE = $classe
        }
    }
}

Write-CsvUtf8 (Join-Path $outDir "FACT_CLT.csv") $factClt
Write-CsvUtf8 (Join-Path $outDir "FACT_credit.csv") $factCredit
Write-CsvUtf8 (Join-Path $outDir "FACT_Risque.csv") $factRisque

Write-Host "Donnees generees dans: $outDir"
Write-Host "  DIM_DATE     : $($dimDate.Count) lignes"
Write-Host "  Dim_CLT      : $($dimClt.Count) lignes"
Write-Host "  FACT_CLT     : $($factClt.Count) lignes"
Write-Host "  FACT_credit  : $($factCredit.Count) lignes"
Write-Host "  FACT_Risque  : $($factRisque.Count) lignes"
