# Status Bar Pet

A silent companion for JetBrains IDEs. Plugin ID: `com.github.pet.pycharm-pet`.
Vendor: `darshan`. Support: [contact@darshan-bharat.com](mailto:contact@darshan-bharat.com).

## Controls

- **Left or right click:** a compact menu with level and XP beside the pet name and Feed, Cheer, and Focus buttons. Hover over Pets, Accessories, Motion, or Statistics to reveal matching submenus. Arrow keys also open the submenus. Choices apply in place, keeping the menu open; Escape or clicking outside dismisses it.
- **Move the pointer over a pet:** gentle petting with hearts. No hover popup appears.
- **Move the pointer within the pet area:** pets follow it within their available space. Otherwise they walk to the edges and rest there.

## Companions

Choose Luna the cat, Milo the dog, Waddles the duck, or Pinchy the rust crab as the main pet. In **Additional pets**, choose other companions to show up to three pets together at a time. Each gets its own movement space and home: a cat bed, doghouse, duck pond, or rocky cove. Homes stay in place while pets move. Idle pets return home and lie down. Sleeping pets stay in place, with gentle breathing and floating Zzz; without homes they sleep where they are.

Choose **Compact**, **Comfortable**, or **Wide** status bar widths. Two pets share 1.5 times the single-pet width; three share at most twice the width. Pets fill the available height and adapt to narrow layouts. They react to feeding, typing, deletion, saving, running, committing, debugging, and run outcomes. Duck accessories follow the duck's head position. Hats replace other hats instead of piling up.

Under **Pets**, rename the main pet (up to 20 characters). Names are saved individually for each species. Select another main pet to rename it. The menu does not include a color-change option.

## Animation and movement

Pets yawn before settling into sleep, blend into their resting pose, and stretch as they stand up. Their horizontal position stays fixed throughout settling, sleeping, and waking. Idle gestures include grooming, looking around, yawning, and ear or wing twitches.

With multiple pets and companion interactions enabled, neighboring pets meet at the edges of their own movement lanes to greet and play with species-specific interactions: cats bat playfully with paws, dogs drop into a joyful play-bow with wagging tails, ducks flutter their wings, and Pinchy clatters raised pincers in celebration. When homes are off, a pair can nap together after playing.

Between 21:00 and 06:00, homes render a soft glowing crescent moon and twinkling star in an ambient day/night cycle.

In **Motion**, choose wandering, mouse following, companion interactions, and optional **Break & stretch reminders** (hydration and posture nudges after sustained focus). **Freeze animation** pauses the pet entirely.

Run success, failed runs, failed tests, successful tests, commits, debugging, and merge conflict markers (`<<<<<<<`) have distinct visual cues. Merge conflicts trigger an investigative magnifying glass cue, while failed runs prompt warm moral support. Ongoing run and debug reactions follow the process lifetime; stopping a process clears its ongoing reaction. Test profiles are recognized from their profile or executor names.

Animation uses a slower refresh while the IDE is unfocused and stops when its window is minimized, hidden, or the widget is not showing. It resumes automatically when visible again. Focus timing and statistics use a separate clock.

## Progress and statistics

Personal edits earn 1 XP per 5 added characters, including characters in a batch. Each full minute of active editing grants 5 XP, with sustained focus bonuses. Runs, tests, commits, and completed focus sessions earn bonuses. Feeding XP is limited to once per minute. Levels use a rising curve; the name row shows level and current/required XP, and pets briefly glow on a level-up. Accessory choices show their required unlock level. Existing level progress is migrated when loading old data.

Personal typing edits earn 1 XP per 5 added characters, while automated edits (AI assistants, code generation, formatters, and refactorings) are isolated automatically and do not inflate personal typing XP or keystroke stats. The tracker observes loaded project documents, including documents shared by split editors, once per application. Only aggregate counts are stored; the plugin does not move or rewrite project code.

Freeze stops visual animation. Focus timing and statistics continue independently. Message popups have close buttons and dismiss automatically. Plugin errors can be reported from JetBrains’ error dialog using “Report by email…”. Review and copy the full report, then open a draft addressed to contact@darshan-bharat.com. Send it from your email app; the plugin does not send automatically or mark a draft as delivered.

## Build and install

The build uses the local PyCharm installation configured in `build.gradle.kts` and Java 25, targeting JetBrains 2026.2 and newer. Run `gradlew.bat buildPlugin` to create a ZIP in `build/distributions/`, then use **Settings → Plugins → Install Plugin from Disk**.

Before a release, run `gradlew.bat releaseChecks verifyPlugin`. `releaseChecks` runs regression tests and offscreen rendering benchmarks in a separate headless JVM; it does not modify the running IDE. Results are saved under `build/reports/`. The compatibility verifier uses the configured local PyCharm installation. Other IDE versions and operating systems need their own verification and interactive smoke checks.

Version 1.1.0 keeps the official plugin identity `com.github.pet.pycharm-pet`. Existing `statusBarPet.xml` state uses the same storage name.
