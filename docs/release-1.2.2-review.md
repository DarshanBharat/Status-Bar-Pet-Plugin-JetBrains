# Release review — Status Bar Pet 1.2.2

Date: 24 October 2026. Scope: the JetBrains plugin in this repository.

## Automated results

- **36 regression checks passed, zero failed.** The checks include 1,000 comparisons against the original XP curve, all pet/activity renderers at five display scales, narrow layouts, burst events, preference/save coordination, localized editing keys, and typing during animation freeze.
- **JetBrains Plugin Verifier 1.411: Compatible** with PyCharm 2026.2.3, build `PY-262.10968.92`. The final report contains no deprecated API findings.
- **Plugin structure validation passed.** Standard `check` runs the regression suite successfully.
- Distribution: `build/distributions/pycharm-pet-1.2.2.zip`.
- Plugin ID: `com.github.pet.pycharm-pet`; vendor contact: `contact@darshan-bharat.com`.
- Java classfile version 69 (Java 25), matching the target IDE. Regression runners and application stubs are excluded from the distribution.
- SHA-256: `0d175c9a38a66af79e844c58891c7b323245b198d1d0f9c6fab998e74e1ec722`.

## Findings fixed

| Area | Finding and correction |
| --- | --- |
| Streaks | Idle timer callbacks updated the active date. Only actual coding activity now updates streaks. Invalid and future dates reset daily counters safely. |
| Coding time | Idle gaps discarded partial minutes, flow bonuses survived breaks, and fractional seconds were lost. Partial active time is retained; continuous flow resets independently; timer accounting preserves fractions and avoids double counting between IDE windows. |
| XP and statistics | Large XP additions could wrap negative. Progression uses bounded arithmetic and a binary search instead of a potentially long leveling loop. Counter additions saturate safely. |
| Feeding | A future saved feeding timestamp could block rewards after a clock correction. Backwards-clock recovery is handled. |
| Saved settings | Persistence previously exposed the live mutable state. Save snapshots now have detached maps and sets; event-result updates use synchronized state methods. Menu changes and collection mutators take the same lock as background saving, preventing inconsistent snapshots and concurrent collection modification. |
| Companion layouts | Sequential resize constraints could temporarily overlap awake pets. Awake lanes are now placed together; sleeping anchors remain fixed, with scale limited if needed to preserve spacing. Shared settings refresh other IDE windows. |
| Popup lifecycle | Hidden windows attempted popup creation. Messages now avoid hidden/inactive windows, use one reusable dismissal timer, and stop timers on disposal. Queued resize callbacks ignore disposed widgets. |
| User names | Long names squeezed the XP fraction; names resembling HTML were interpreted as markup. XP keeps its space, and names are plain text. |
| Edit attribution | Keyboard shortcuts launching formatters/refactorings could count tool edits as personal typing. Tool commands are excluded; direct typing, Enter, and Tab remain supported independently of localized command names. Unnamed changes require a direct editing key rather than any mouse/keyboard event to count as personal. AI detection remains best effort. |
| Animation freeze | Editing during a freeze failed to refresh inactivity, allowing the pet to sleep immediately after unfreezing. Activity is now recorded even while animation is paused or an important reaction is being displayed. |
| Event bursts | Typing, deletion, and save signals could enqueue redundant work. Visual signals are coalesced while statistics are still recorded for each edit. |
| Animation work | Reused pet lists, palette colors, and classification tables; replaced full call-stack snapshots with a bounded stack walk; bounded retained particles at 64 and removed copy-on-write particle updates. |
| Build compatibility | Updated the JetBrains Gradle plugin to 2.19.0, matched Java 25, declared the VCS dependency, and replaced a deprecated drawing API. |

## Performance measurements

Three separate JVM runs per version were alternated, using the same installed Java runtime and a fixed 256 MB heap. Each warmed up with 8,000 iterations and then timed 6,000 three-pet idle rendering iterations into an offscreen image.

| Metric | 1.2.1 | 1.2.2 |
| --- | ---: | ---: |
| Median of run medians | 0.161 ms | 0.167 ms |
| Median of run p95 values | 0.363 ms | 0.344 ms |
| Range of run p95 values | 0.351–0.382 ms | 0.342–0.411 ms |

Drawing performance is effectively in the same sub-millisecond range; these differences are too small to claim a material speedup. The regression run performed alongside build activity reached 0.855 ms p95, illustrating scheduling noise. These figures are **offscreen renderer timings, not whole-IDE CPU or GPU measurements**.

The tested resource improvements are more concrete: 10,000 typing signals produce one queued visual callback while retaining all 10,000 edit counts; a burst of 10,000 particle requests retains at most 64 particles; hidden/minimized animation policy stops the animation timer; widget disposal stops every widget timer. The warmed-up lookup benchmark optimized away many allocations in both versions, so no percentage allocation improvement is claimed.

Raw evidence:

- `build/reports/release-before.txt` — original regressions reproduced before fixes.
- `build/reports/release-source-review-before.txt` — three additional regressions reproduced during direct source review.
- `build/reports/release-checks.txt` — final checks and the in-suite benchmark.
- `build/reports/release-benchmark-comparison.txt` — isolated old/new benchmark runs.
- `build/reports/pluginVerifier/PY-262.10968.92/report.html` — compatibility results.

## Live IDE check

PyCharm was launched, but the user interrupted computer control before startup and interactive inspection could be observed. The live IDE smoke check remains incomplete. The later source review and automated checks do not replace it.

## Remaining release checks and limits

The automated suite uses an isolated application stub for widget checks; it does not simulate the entire IDE. Other IDE builds, Linux, macOS, mixed-monitor DPI transitions, native mail-client handoff, and full project interactions have not been verified by this suite.

Before publishing, confirm the menu and companion layout in the real IDE, run one normal task and a real test configuration, make a commit in a disposable project, check personal versus AI edit counts, and try minimize/restore and restart. Reporting should open a draft without sending it or marking it delivered automatically.

Test-run recognition still uses configuration/executor names and process results. Its counters represent successful test **runs**, not individual test cases. Unknown agent edits and unknown command origins remain conservative best-effort classifications; use the explicit AI editing session switch when an integration does not expose its origin.

Reproduce the automated review with Java 25 and the configured local IDE:

```text
gradlew.bat check verifyPluginStructure verifyPlugin
```

Reference: [JetBrains compatibility verification](https://plugins.jetbrains.com/docs/intellij/verifying-plugin-compatibility.html), [Java requirements for 2026.2](https://plugins.jetbrains.com/docs/intellij/api-changes-list-2026.html), [Gradle plugin 2.19.0](https://github.com/JetBrains/intellij-platform-gradle-plugin/releases/tag/2.19.0).
