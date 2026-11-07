$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    Push-Location frontend
    try {
        pnpm install --frozen-lockfile
        if ($LASTEXITCODE -ne 0) { throw 'Frontend installation failed' }
        node node_modules/vite/bin/vite.js build
        if ($LASTEXITCODE -ne 0) { throw 'Frontend build failed' }
    } finally { Pop-Location }
    Push-Location server
    try {
        mvn --batch-mode --no-transfer-progress clean verify
        if ($LASTEXITCODE -ne 0) { throw 'Backend build or tests failed' }
    } finally { Pop-Location }
} finally { Pop-Location }
