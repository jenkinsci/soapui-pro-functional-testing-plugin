# Migration Guide: Plugin 1.12 to 1.13

## Breaking Change: authMethod Parameter Format

### Overview

Plugin version 1.13 introduced a **breaking change** in how the `authMethod` parameter is handled. This affects all users upgrading from version 1.12 to 1.13.

### What Changed?

The plugin changed from using `AuthMethod.valueOf()` to `AuthMethod.getValue()`, which means:

- **Plugin 1.12**: Matches against the **enum name** (`API_KEY`)
- **Plugin 1.13**: Matches against the **display name** (`API KEY` with space)

### Impact

If you have Jenkinsfiles using `authMethod: 'API_KEY'` (with underscore), they will **fail silently** after upgrading to plugin 1.13. The `-K` parameter (access key) will not be passed to ReadyAPI, causing license authentication errors:

```
ERROR [SlmSaasCommandLineLicenseFlow] There is no license installed. Please specify access key or log in to your account in ReadyAPI application
DEBUG [LicenseFacade] There is no license found. Please verify the license server address.
```

### Migration Steps

**Before (Plugin 1.12):**
```groovy
SoapUIPro(
  pathToTestrunner:
  pathToProjectFile:
  testSuiteTags:
  environment:
  authMethod: 'API_KEY',  // ← With underscore
  slmLicenceAccessKey:
)
```

**After (Plugin 1.13 - Required):**
```groovy
SoapUIPro(
  pathToTestrunner:
  pathToProjectFile:
  testSuiteTags:
  environment:
  authMethod: 'API KEY',  // ← With space (required for 1.13+)
  slmLicenceAccessKey:
)
```

### How to Find and Update

Search for all occurrences of `authMethod: 'API_KEY'` in your Jenkinsfiles and replace with `authMethod: 'API KEY'`.

**Example using grep:**
```bash
grep -r "authMethod: 'API_KEY'" /path/to/jenkinsfiles/
```

**Example using Git:**
```bash
git grep "authMethod: 'API_KEY'"
```

### Technical Details

**Code Change (Commit d098325):**
```java
// Plugin 1.12:
AuthMethod authMethod = AuthMethod.valueOf(params.getAuthMethod());

// Plugin 1.13:
AuthMethod authMethod = AuthMethod.getValue(params.getAuthMethod());
```

**Enum Definition:**
```java
public enum AuthMethod {
    API_KEY("API KEY"),  // enum name: API_KEY, display name: "API KEY"
    // ...
}
```

- `valueOf("API_KEY")` matches the enum name → Works
- `getValue("API_KEY")` matches the display name → Fails (returns INVALID)
- `getValue("API KEY")` matches the display name → Works

### Related

- Commit: [d098325](https://github.com/jenkinsci/soapui-pro-functional-testing-plugin/commit/d09832525252b1d2a4c3af4ef9120fa6b7723e68) - RIA-26393 AuthMethod fix
- File: [`ProcessRunner.java`](https://github.com/jenkinsci/soapui-pro-functional-testing-plugin/blob/master/src/main/java/com/smartbear/ready/jenkins/ProcessRunner.java#L198)
- Enum: [`AuthMethod.java`](https://github.com/jenkinsci/soapui-pro-functional-testing-plugin/blob/master/src/main/java/com/smartbear/ready/jenkins/AuthMethod.java#L5)

### Request for Future Versions

1. **Backward Compatibility**: Consider supporting both formats (`'API_KEY'` and `'API KEY'`) for smoother migration
2. **Error Messages**: Provide clearer error messages when the parameter format is incorrect
3. **Documentation**: Document breaking changes more prominently in release notes

