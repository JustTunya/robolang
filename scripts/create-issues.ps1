# Creates the labels and the 14 task issues (T00-T13) in the current GitHub repository.
# Requirements: GitHub CLI installed and authenticated (`gh auth login`).
# Run from the repository root:  powershell -ExecutionPolicy Bypass -File scripts\create-issues.ps1

$ErrorActionPreference = 'Stop'

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    throw "GitHub CLI (gh) not found. Install it from https://cli.github.com and run 'gh auth login'."
}

$labels = @(
    @{ name = 'wave-0';          color = '0e8a16'; desc = 'Foundation' },
    @{ name = 'wave-1';          color = '1d76db'; desc = 'Depends on T00 only' },
    @{ name = 'wave-2';          color = '5319e7'; desc = 'Depends on wave 1' },
    @{ name = 'wave-3';          color = 'b60205'; desc = 'Depends on wave 2' },
    @{ name = 'blocked';         color = 'd93f0b'; desc = 'Worker is waiting for a decision' },
    @{ name = 'contract-change'; color = 'fbca04'; desc = 'Changes a frozen contract' }
)
foreach ($l in $labels) {
    gh label create $l.name --color $l.color --description $l.desc --force | Out-Null
}

$files = Get-ChildItem -Path 'issues' -Filter 'T*.md' | Sort-Object Name
foreach ($f in $files) {
    $lines = Get-Content -Path $f.FullName -Encoding UTF8
    $title = ($lines[0] -replace '^#\s*', '').Trim()
    $waveLine = $lines | Where-Object { $_ -match '^\*\*Wave:\*\*\s*(\d)' } | Select-Object -First 1
    if (-not $waveLine) { throw "No '**Wave:** N' line in $($f.Name)" }
    $wave = [regex]::Match($waveLine, '(\d)').Groups[1].Value
    Write-Host "Creating: $title (wave-$wave)"
    gh issue create --title $title --body-file $f.FullName --label "wave-$wave" | Out-Null
}

Write-Host "Done. Created $($files.Count) issues."
