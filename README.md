# Perpustakaan Backend

## User Dashboard Demo

### Compile
```bash
mvn -q -DskipTests compile
```

### Run presentation demo
```bash
mvn -q test-compile exec:java "-Dexec.mainClass=com.mycompany.perpustakaan.test.DashboardPresentationDemo" "-Dexec.classpathScope=test"
```

### Run with custom login
```bash
mvn -q test-compile exec:java "-Dexec.mainClass=com.mycompany.perpustakaan.test.DashboardPresentationDemo" "-Dexec.classpathScope=test" "-Dexec.args=staff01 password"
```

### What it shows
- login success
- profile card
- total books
- latest books
- search books
- logout/session check