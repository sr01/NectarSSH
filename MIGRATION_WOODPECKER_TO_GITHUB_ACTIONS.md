# Migration Plan: Woodpecker CI to GitHub Actions

## Current Woodpecker Pipeline (`.woodpecker.yml`)

| Step | Purpose | Trigger |
|------|---------|---------|
| restore-cache | Restore Gradle/SDK cache from filesystem | Always |
| build | `assembleDebug` with debug keystore from secret | Always |
| test | Unit tests (currently commented out) | — |
| rebuild-cache | Save Gradle/SDK cache | Always |
| prepare-release | Copy APK to `artifacts/` | `branch: main` |
| publish-github-release | Create GitHub Release with APK | `event: tag` |
| notify_email | Email on success/failure | Always |

## Secrets to Migrate

| Woodpecker Secret | GitHub Actions Secret | Notes |
|---|---|---|
| `debug_keystore_base64` | `DEBUG_KEYSTORE_BASE64` | Base64-encoded debug keystore |
| `github_token` | `GITHUB_TOKEN` (built-in) | No migration needed |
| `mail_dsn` | `MAIL_DSN` | SMTP connection string |
| `mail_from_address` | `MAIL_FROM_ADDRESS` | Sender email |
| `mail_recipients` | `MAIL_RECIPIENTS` | Notification recipients |

## Target GitHub Actions Structure

```
.github/
└── workflows/
    └── ci.yml       # Single workflow covering build + release
```

## Workflow Design

### Triggers

```yaml
on:
  push:
    branches: [main]
    tags: ['v*']
  pull_request:
    branches: [main]
```

### Jobs

#### 1. `build`
- **Runs on**: `ubuntu-latest`
- **Steps**:
  1. Checkout code
  2. Set up JDK 11
  3. Set up Android SDK (use `android-actions/setup-android`)
  4. Cache Gradle (`actions/cache` with `~/.gradle/caches` and `~/.gradle/wrapper`)
  5. Decode debug keystore from `DEBUG_KEYSTORE_BASE64` secret
  6. Run `./gradlew assembleDebug`
  7. Upload APK as workflow artifact (`actions/upload-artifact`)

#### 2. `release` (runs only on tag push)
- **Needs**: `build`
- **Condition**: `if: startsWith(github.ref, 'refs/tags/v')`
- **Steps**:
  1. Download APK artifact from `build` job
  2. Create GitHub Release with APK attached (use `softprops/action-gh-release`)

#### 3. `notify` (runs on success or failure)
- **Needs**: `build`
- **Condition**: `if: always()`
- **Steps**:
  1. Send email via `dawidd6/action-send-mail` with SMTP credentials from secrets

## Key Differences from Woodpecker

| Concern | Woodpecker | GitHub Actions |
|---------|-----------|----------------|
| Cache | Manual `meltwater/drone-cache` to filesystem volume | Built-in `actions/cache` (hash-based, auto-restore) |
| Android SDK | Baked into `cimg/android` image | `android-actions/setup-android` or pre-installed on `ubuntu-latest` |
| Keystore | Decoded inline in build commands | Decoded in a dedicated step, referenced via env var |
| Release | `woodpeckerci/plugin-release` | `softprops/action-gh-release` |
| Email | `deblan/woodpecker-email` | `dawidd6/action-send-mail` |
| Parallelism | Sequential steps | Separate jobs with `needs` dependency |

## Implementation Steps

1. **Create `.github/workflows/ci.yml`** with the workflow described above
2. **Add secrets** in GitHub repo settings:
   - `DEBUG_KEYSTORE_BASE64`
   - `MAIL_DSN` / `MAIL_FROM_ADDRESS` / `MAIL_RECIPIENTS` (if keeping email notifications)
3. **Test on a feature branch** — push and verify the build job passes
4. **Test release flow** — push a tag like `v1.2.0-rc1` and verify the release is created
5. **Remove `.woodpecker.yml`** once GitHub Actions is confirmed working
6. **Optional**: Uncomment and add the test job (`./gradlew test`) if tests are ready

## Optional Enhancements

- **Lint step**: Add `./gradlew lint` as a separate job or step
- **Release build**: Add `assembleRelease` with a release keystore for production APKs
- **Version bump automation**: Auto-tag on merge to main based on `versionName` in `build.gradle.kts`
- **Dependabot**: Enable for Gradle dependencies
