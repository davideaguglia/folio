# Finance Tracker — Personal Android App

A fully offline, single-user personal finance tracker built with **Kotlin + Jetpack Compose + Material Design 3**. All data lives on-device in a local Room (SQLite) database — no accounts, no backend, no internet required.

---

## Features

| Feature | Description |
|---|---|
| **Transactions** | Log income and expenses with category, account, date and notes |
| **Recurring transactions** | Mark any transaction as recurring (daily / weekly / monthly / yearly); the app auto-generates future entries at launch |
| **Accounts** | Manage Checking, Savings, Cash and Credit Card accounts with live balance tracking |
| **Investment portfolio** | Track stocks, ETFs, crypto, real estate and other assets; update current price manually to record a snapshot |
| **Price history chart** | Per-asset line chart showing how the price evolved over time |
| **Budget** | Set monthly spending limits per expense category with colour-coded progress bars |
| **Dashboard** | Monthly balance, income/expense summary chips, donut chart by spending category, and last 5 transactions |
| **Reports** | 12-month income vs expense bar chart, category breakdown donut, net-worth line chart |
| **Net worth snapshots** | Record point-in-time net worth entries (assets − liabilities) for the history chart |
| **CSV export** | Export all transactions to a CSV file saved in the app's external files directory |
| **Dark mode** | Toggle dark/light theme; preference is persisted across sessions via DataStore |
| **Dynamic colour** | Uses Android 12+ Material You dynamic colour scheme when available |

---

## Tech Stack

| Concern | Library / Tool |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture (Repository pattern) |
| Database | Room 2.6 (SQLite) |
| Dependency injection | Hilt 2.51 |
| Async / reactive | Kotlin Coroutines + StateFlow |
| Navigation | Navigation Compose 2.7 |
| Background work | WorkManager 2.9 (recurring transaction worker) |
| Preferences | DataStore Preferences 1.1 |
| Charts | Custom Canvas composables (DonutChart, LineChart, BarChart) |
| Build | Gradle 8.4 with version catalog (`gradle/libs.versions.toml`) |
| Min SDK | 26 (Android 8.0) · Target SDK 34 |

---

## Project Structure

```
Finance_App/
├── gradle/
│   ├── libs.versions.toml          # Centralised version catalog
│   └── wrapper/
│       └── gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── drawable/           # Adaptive launcher icon assets
│       │   ├── mipmap-anydpi-v26/  # Adaptive icon descriptors
│       │   ├── values/
│       │   │   ├── strings.xml
│       │   │   └── themes.xml
│       │   └── xml/                # Backup / data-extraction rules
│       └── java/com/personal/financeapp/
│           ├── FinanceApplication.kt   # @HiltAndroidApp + WorkManager config
│           ├── MainActivity.kt         # Single-activity host
│           │
│           ├── data/
│           │   ├── local/
│           │   │   ├── AppDatabase.kt          # Room database + seed callback
│           │   │   ├── entity/                 # Room entity data classes
│           │   │   │   ├── AccountEntity.kt
│           │   │   │   ├── CategoryEntity.kt
│           │   │   │   ├── TransactionEntity.kt
│           │   │   │   ├── InvestmentEntity.kt
│           │   │   │   ├── InvestmentPriceEntity.kt
│           │   │   │   └── NetWorthSnapshotEntity.kt
│           │   │   └── dao/                    # Room DAOs
│           │   │       ├── AccountDao.kt
│           │   │       ├── CategoryDao.kt
│           │   │       ├── TransactionDao.kt   # includes TransactionWithDetails join
│           │   │       ├── InvestmentDao.kt
│           │   │       ├── InvestmentPriceDao.kt
│           │   │       └── NetWorthDao.kt
│           │   └── repository/                 # Repository layer (injected into VMs)
│           │       ├── AccountRepository.kt
│           │       ├── CategoryRepository.kt
│           │       ├── TransactionRepository.kt
│           │       ├── InvestmentRepository.kt
│           │       └── NetWorthRepository.kt
│           │
│           ├── di/
│           │   └── DatabaseModule.kt       # Hilt module: DB + all DAOs
│           │
│           ├── util/
│           │   ├── CurrencyFormatter.kt    # Formats amounts as "€ 1.234,56"
│           │   └── CsvExporter.kt          # Writes transactions CSV to external files dir
│           │
│           ├── worker/
│           │   └── RecurringTransactionWorker.kt  # HiltWorker that generates overdue recurring tx
│           │
│           └── ui/
│               ├── theme/
│               │   ├── Color.kt            # Brand + chart colour palette
│               │   ├── Type.kt             # Typography scale
│               │   ├── Theme.kt            # FinanceAppTheme (dark/light + dynamic colour)
│               │   └── ThemeViewModel.kt   # Persists dark-mode toggle via DataStore
│               ├── components/
│               │   └── Charts.kt           # DonutChart, LineChart, BarChart (Canvas)
│               ├── navigation/
│               │   └── AppNavigation.kt    # NavHost + bottom navigation bar
│               └── screens/
│                   ├── dashboard/
│                   │   ├── DashboardScreen.kt
│                   │   └── DashboardViewModel.kt
│                   ├── transactions/
│                   │   ├── TransactionListScreen.kt    # Swipe-to-delete, search, filter
│                   │   ├── AddEditTransactionScreen.kt # Full-page form with date picker
│                   │   └── TransactionViewModel.kt
│                   ├── accounts/
│                   │   ├── AccountsScreen.kt           # Cards + add/edit dialog
│                   │   └── AccountsViewModel.kt
│                   ├── investments/
│                   │   ├── InvestmentsScreen.kt        # Portfolio overview + update price
│                   │   ├── InvestmentDetailScreen.kt   # Price history line chart
│                   │   └── InvestmentsViewModel.kt
│                   ├── budget/
│                   │   ├── BudgetScreen.kt             # Progress bars per category
│                   │   └── BudgetViewModel.kt
│                   └── reports/
│                       ├── ReportsScreen.kt            # Bar + donut + net-worth charts
│                       └── ReportsViewModel.kt
├── build.gradle.kts    # Root build file (plugin declarations only)
├── settings.gradle.kts
└── gradle.properties
```

---

## Database Schema

| Table | Key columns |
|---|---|
| `accounts` | id, name, type (CHECKING/SAVINGS/CASH/CREDIT_CARD), initialBalance, color, icon |
| `categories` | id, name, type (INCOME/EXPENSE), color, icon, monthlyBudget |
| `transactions` | id, amount, type, categoryId, accountId, date, description, isRecurring, recurringPeriod, recurringNextDate |
| `investments` | id, name, ticker, type (STOCK/CRYPTO/ETF/REAL_ESTATE/OTHER), quantity, purchasePrice, currentPrice, purchaseDate |
| `investment_prices` | id, investmentId, price, date — one row per manual price update |
| `net_worth_snapshots` | id, totalAssets, totalLiabilities, netWorth, date |

On first launch the database is seeded with **15 default expense/income categories** and a "Cash" account.

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android device or emulator running API 26+

### Build & Run

```bash
# Clone the repo
git clone <repo-url>
cd Finance_App

# Open in Android Studio — Gradle sync runs automatically
# Then press Run (▶) or use the CLI:
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

> **Note:** The `gradle-wrapper.jar` is not tracked in git. Android Studio generates it automatically on first sync. Alternatively run `gradle wrapper --gradle-version 8.4` if you have Gradle installed locally.

---

## Currency

The app uses **EUR (€)** formatting via `java.util.Locale.ITALY`. To change currency, update `CurrencyFormatter.kt`.
