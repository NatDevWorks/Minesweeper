# Minesweeper

A Java Swing Minesweeper game with difficulty selection, a timer, flag counter, pause/resume support, first-click safety, win/loss dialogs, colored cells, and best-time tracking.

## Run From Source

From the parent folder:

```powershell
cd "J:\Coding Projects"
javac minesweeper\*.java
java minesweeper.Graphical
```

## Run Unit Tests

```powershell
cd "J:\Coding Projects"
javac minesweeper\*.java
java minesweeper.Cell
java minesweeper.Field
```

## Build The Windows App

```powershell
cd "J:\Coding Projects"
New-Item -ItemType Directory -Force -Path minesweeper\build\classes,minesweeper\build\jar,minesweeper\dist
javac -d minesweeper\build\classes minesweeper\*.java
jar --create --file minesweeper\build\jar\minesweeper.jar --main-class minesweeper.Graphical -C minesweeper\build\classes .
jpackage --type app-image --name Minesweeper --input minesweeper\build\jar --main-jar minesweeper.jar --main-class minesweeper.Graphical --dest minesweeper\dist --app-version 1.0.0
```

The executable will be created at:

```text
J:\Coding Projects\minesweeper\dist\Minesweeper\Minesweeper.exe
```

## Documentation

JavaDoc is generated in:

```text
J:\Coding Projects\minesweeper\docs\index.html
```
