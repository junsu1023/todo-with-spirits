# 바탕화면 정령(Unity 오버레이)을 빌드한다.
# 출력: unity/TodoSpiritsWindows/spirit-player/TodoSpirit.exe (git 에 올리지 않음)
# 윈도우 앱은 이 위치의 실행 파일을 찾아 띄운다.
param(
    [string]$EditorPath = 'C:/Program Files/Unity/Hub/Editor/6000.3.27f2/Editor/Unity.exe'
)
$ErrorActionPreference = 'Stop'
$project = Join-Path $PSScriptRoot 'SpiritOverlay'
$logs = Join-Path $PSScriptRoot 'spirit-player-logs'
if (-not (Test-Path -LiteralPath $EditorPath -PathType Leaf)) { throw "Unity 에디터를 찾지 못했어요: $EditorPath (-EditorPath 로 위치를 알려 주세요)" }
New-Item -ItemType Directory -Force -Path $logs | Out-Null
$log = Join-Path $logs 'build.log'
$arguments = @('-batchmode', '-quit', '-projectPath', ('"' + $project + '"'), '-buildTarget', 'Win64',
    '-executeMethod', 'TodoSpirits.SpiritEditor.OverlayBuild.BuildWindows', '-logFile', ('"' + $log + '"'))
$process = Start-Process -FilePath $EditorPath -ArgumentList $arguments -WindowStyle Hidden -PassThru
$process.WaitForExit()
if ($process.ExitCode -ne 0) { throw "Unity 빌드가 실패했어요 (종료 코드 $($process.ExitCode)). 로그: $log" }
Write-Output (Join-Path $PSScriptRoot 'spirit-player/TodoSpirit.exe')
