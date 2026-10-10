# Termux and GitHub integration

This feature is being introduced on `feature/termux-github-control`; it does not replace the existing DevUtils tools.

## Termux setup
1. Install Termux from a trusted source.
2. In Termux, run the following once to enable the required property:
   ```sh
   mkdir -p ~/.termux
   if grep -q '^allow-external-apps' ~/.termux/termux.properties 2>/dev/null; then
     sed -i 's/^allow-external-apps.*/allow-external-apps = true/' ~/.termux/termux.properties
   else
     printf '\nallow-external-apps = true\n' >> ~/.termux/termux.properties
   fi
   ```
3. Fully close and reopen Termux.
4. In Android Settings → Apps → DevUtils → Permissions, grant **Run commands in Termux environment / RUN_COMMAND** under additional permissions if shown.
5. Open DevUtils → **مركز التطوير** → **تشغيل أمر في Termux**.
6. Review the command and working directory carefully, then confirm. Start with a harmless command such as `pwd`.
7. If a terminal session does not open automatically, allow Termux to display over other apps in Android settings. Commands sent to external shared storage may also require Termux storage access.
8. Command output is currently shown in Termux. DevUtils does not yet stream terminal output back into its own screen.

The bridge uses Termux's `com.termux.RUN_COMMAND` service and requests `com.termux.permission.RUN_COMMAND`. Availability depends on the installed Termux version and its external-command setting.

## GitHub setup
1. Create a fine-grained personal access token in GitHub. Do not paste it into chat, source files, or commit it to a repository.
2. Grant access only to the repository you intend to manage.
3. For listing files and reading/editing text files, grant **Contents: Read and write** as needed.
4. For viewing workflow runs, grant **Actions: Read**. For starting a build, grant **Actions: Read and write**.
5. In DevUtils, open **مركز التطوير** → **إدارة GitHub داخل التطبيق** and enter the token, repository in `owner/repository` form, and branch.

The current implementation does not save the token to preferences or a file; it is kept in memory for the active screen flow. Do not use a broad classic token if a narrowly scoped fine-grained token is available.

## Current in-app GitHub features
- List the repository root.
- View the latest workflow runs for the selected branch.
- Dispatch the `build.yml` workflow for the selected branch.
- Read a text file, edit it, review the change, and confirm a GitHub Contents API commit.

The build workflow supports both push and manual dispatch. GitHub API errors are displayed to the user. Large files and binary files are not intended for the in-app text editor.

## Safety and validation
- Review commands before confirming; shell commands can change or delete files.
- Review file changes before committing them.
- Keep this feature branch separate from `main` until cloud builds and on-device testing pass.
- A successful GitHub Actions build confirms compilation only; Termux command execution and GitHub operations still need testing on the actual Android device.
