# Shri Kamdar Credit Co-Operative Society - Android App

A native Android application built with **Kotlin**, **Jetpack Compose**, and **Room Database** for Credit Co-Operative Societies (સહકારી મંડળી).

Rewritten from the original legacy web system into a modern, offline-first Android application designed for credit society managers, field recovery agents, and members.

## Core Features

- **Executive Dashboard**: Society portfolio summary (total disbursed, outstanding balance, recovery efficiency, today's collection, monthly collection, NPA & delinquency metrics).
- **Member Registry & KYC**: Member onboarding with complete KYC details (Aadhar, PAN, Father/Spouse, Gender, Occupation) and Bank Account details. Includes Maker-Checker approval workflow (Pending, Approved, Rejected).
- **Loan Portfolio & Disbursement**: Multi-scheme loans (Agriculture, Personal, Business, Gold, Vehicle, Emergency) with automated reducing-balance EMI calculator, guarantor tracking, and repayment schedules.
- **Daily Collection Counter**: Fast counter and field collection module. Automatically splits interest and principal, generates digital receipts with unique receipt numbers, and updates loan balances instantly.
- **NPA & Recovery Management (RBI / Co-Op Standards)**:
  - Overdue classification: SMA-0 (1–30 days), SMA-1 (31–60 days), SMA-2 (61–90 days), NPA (> 90 days).
  - Defaulter account ledger with automated 2% penal interest calculation.
  - Recovery Follow-Up register (Phone calls, Field visits, Notices, Office meetings).
  - Promise To Pay (PTP) commitment tracking with Kept/Broken status.
  - Statutory Legal Demand Notices (Form-1, Form-2, Advocate Legal Notice).
  - Gujarat Co-operative Societies Act **Section 101** court case tracking and recovery certificate records.
- **Financial & Regulatory Reports**: Key regulatory indicators (Gross NPA ratio, Recovery efficiency %, Scheme-wise breakdown, Collection register).
- **Multi-Role Switching**: Toggle between Admin/Manager, Recovery Officer, and Member view directly from the app bar.

## Tech Stack

- **UI**: 100% Jetpack Compose with Material Design 3
- **Language**: Kotlin 2.2.10
- **Build Tool**: Gradle 9.3.1 (Kotlin DSL), Android Gradle Plugin 9.1.1
- **Architecture**: MVVM with Repository Pattern and Reactive Kotlin Coroutines / Flow
- **Local Persistence**: Room 2.7.0 (with KSP)
- **Target SDK**: Android 36 (minSdk 24)
