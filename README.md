# Career Explorer

This is a small desktop app for trying out a career and job-search service. The app is still a beta, so some screens are examples and may not be finished.

## What you need

- A Windows computer
- Java Development Kit (JDK) 17 or newer

You need the **JDK**, not just Java, because the JDK includes the program that builds the app.

## Start the app

1. Open the project folder in File Explorer.
2. Click the folder address bar, type `powershell`, and press **Enter**.
3. Copy and paste these commands into PowerShell, one at a time:

   ```powershell
   New-Item -ItemType Directory -Force build | Out-Null
   & 'C:\Program Files\Microsoft\jdk-17.0.21.102-hotspot\bin\javac.exe' -encoding UTF-8 -d build CareerExplorer.java
   if ($LASTEXITCODE -eq 0) { & 'C:\Program Files\Microsoft\jdk-17.0.21.102-hotspot\bin\java.exe' -cp build CareerExplorer }
   ```

The app window should open. The first command makes a `build` folder. Compiled files go there, so they do not clutter the project folder.

These commands use the JDK path installed on this computer. If your JDK is installed somewhere else, replace that path with your JDK's `bin` folder. If `javac` and `java` are already available in PowerShell, you can instead run:

```powershell
New-Item -ItemType Directory -Force build | Out-Null
javac -encoding UTF-8 -d build CareerExplorer.java
if ($LASTEXITCODE -eq 0) { java -cp build CareerExplorer }
```

## Try it out

- Choose **Бүртгүүлэх** (Register) to create an account.
- The login screen also has example buttons for trying the app as a job seeker or an organization.
- The app interface is mostly in Mongolian.

## Your saved information

The app saves user information in `careerexplorer_users.dat` and photos in the `photos` folder, inside the project folder. Keep these files if you want to keep the saved information.

## If it does not start

- If PowerShell says `javac` or `java` cannot be found, install JDK 17 or newer, or check the JDK path in the commands above.
- If you see a compile error, make sure PowerShell is open in the folder containing `CareerExplorer.java`.
- Close the app by closing its window.
