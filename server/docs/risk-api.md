# Risk API — SL-004 through SL-007

`POST /api/v1/risk/calculate`, Content-Type: application/json. Public, stateless,
no persistence and no broker connection. All money is INR.

Request:
```json
{"capital":15000,"riskPercent":1,"entry":200,"stopLoss":194,"target":224}
```

Response (200):
```json
{"riskBudget":150,"quantity":25,"positionValue":5000,"plannedLoss":150,"rewardToRiskRatio":4.0000}
```

All five fields are required positive decimals. riskPercent is a percentage
(1 means 1%), at most 100. stopLoss must be less than entry; target must exceed
entry. Inputs support at most 18 significant digits, up to 8 decimal places,
and scale no lower than -8. These explicit limits bound decimal computation.
Capital means available capital for this calculation, not total account equity.

Risk budget = capital * riskPercent / 100.
Quantity = min(floor(riskBudget / (entry - stopLoss)), floor(capital / entry)).
Position value = quantity * entry; planned loss = quantity * (entry - stopLoss).
Reward-to-risk = (target - entry) / (entry - stopLoss), rounded HALF_UP to four
decimal places. All other decimal results remain exact; do not round budgets up.
Insufficient capital or risk returns zero quantity/value/loss with HTTP 200;
the per-share reward-to-risk remains defined. Planned loss excludes fees and slippage.

Invalid input returns HTTP 400:
```json
{"code":"INVALID_INPUT","message":"stopLoss must be below entry"}
```
Malformed, missing or wrongly typed JSON also returns this error shape with a
readable message, without exception details or stack traces.

PowerShell example:
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/risk/calculate -ContentType application/json -Body '{"capital":15000,"riskPercent":1,"entry":200,"stopLoss":194,"target":224}'
```

Potential profit: response field potentialProfit = quantity × (target - entry), calculated exactly before fees and slippage. For the documented sample it is INR 600. Zero quantity returns zero potential profit.
