# LibraryApp Backend

## Contract

Backend ini mengekspor service API Java yang bisa dipakai langsung oleh frontend Swing dalam monorepo yang sama.

### Entry points
- `com.mycompany.perpustakaan.api.LibraryApi`
- `com.mycompany.perpustakaan.controller.AuthController`
- `com.mycompany.perpustakaan.controller.DashboardController`

### DTO contract
- `com.mycompany.perpustakaan.api.AuthResponse`
- `com.mycompany.perpustakaan.api.UserSummary`
- `com.mycompany.perpustakaan.api.BookSummary`
- `com.mycompany.perpustakaan.api.DashboardSummary`

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