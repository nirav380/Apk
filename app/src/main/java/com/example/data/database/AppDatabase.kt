package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MemberEntity::class,
        LoanEntity::class,
        RepaymentEntity::class,
        FollowUpEntity::class,
        PtpEntity::class,
        DemandNoticeEntity::class,
        Section101CaseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun loanDao(): LoanDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun recoveryDao(): RecoveryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coop_society_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val memberDao = db.memberDao()
            val loanDao = db.loanDao()
            val repaymentDao = db.repaymentDao()
            val recoveryDao = db.recoveryDao()

            // 1. Initial Members
            val members = listOf(
                MemberEntity(
                    memberId = 1,
                    memberNo = "SK-0001",
                    fullName = "Ramesh Patel",
                    mobile = "9999900001",
                    email = "ramesh.patel@example.com",
                    address = "Plot 14, Patel Colony, Rajkot, Gujarat",
                    joinDate = "2024-01-15",
                    isActive = true,
                    aadharNo = "4589 1234 5678",
                    panNo = "ABCDE1234F",
                    fatherSpouseName = "Kanjibhai Patel",
                    dob = "1982-05-12",
                    gender = "Male",
                    occupation = "Agriculture & Dairy",
                    bankName = "State Bank of India",
                    accountNumber = "20349811234",
                    ifscCode = "SBIN0001244",
                    bankBranch = "Rajkot Main",
                    approvalStatus = "Approved",
                    approvedAt = "2024-01-16",
                    approvedBy = "Admin",
                    roleId = 1 // Admin
                ),
                MemberEntity(
                    memberId = 2,
                    memberNo = "SK-0002",
                    fullName = "Suresh Shah",
                    mobile = "9825012345",
                    email = "suresh.shah@example.com",
                    address = "B-202, Gokul Heights, Morbi Road, Rajkot",
                    joinDate = "2024-03-10",
                    isActive = true,
                    aadharNo = "7823 4567 8901",
                    panNo = "BQYPS5678G",
                    fatherSpouseName = "Manilal Shah",
                    dob = "1978-11-23",
                    gender = "Male",
                    occupation = "Wholesale Trading",
                    bankName = "Bank of Baroda",
                    accountNumber = "0456100012345",
                    ifscCode = "BARB0RAJKOT",
                    bankBranch = "Dhebar Road",
                    approvalStatus = "Approved",
                    approvedAt = "2024-03-11",
                    approvedBy = "Admin",
                    roleId = 2
                ),
                MemberEntity(
                    memberId = 3,
                    memberNo = "SK-0003",
                    fullName = "Geeta Ben Varma",
                    mobile = "9898011223",
                    email = "geeta.varma@example.com",
                    address = "12, Shanti Nagar, Jetpur, Rajkot",
                    joinDate = "2024-06-20",
                    isActive = true,
                    aadharNo = "9123 6543 2109",
                    panNo = "CKDPV9012K",
                    fatherSpouseName = "Kishorebhai Varma",
                    dob = "1985-08-19",
                    gender = "Female",
                    occupation = "Handicrafts & Tailoring",
                    bankName = "HDFC Bank",
                    accountNumber = "50100456789123",
                    ifscCode = "HDFC0000256",
                    bankBranch = "Jetpur",
                    approvalStatus = "Approved",
                    approvedAt = "2024-06-21",
                    approvedBy = "Admin",
                    roleId = 2
                ),
                MemberEntity(
                    memberId = 4,
                    memberNo = "SK-0004",
                    fullName = "Pravin Bhai Parmar",
                    mobile = "9712345678",
                    email = "pravin.parmar@example.com",
                    address = "Village Anandpar, Dist. Rajkot",
                    joinDate = "2024-08-05",
                    isActive = true,
                    aadharNo = "3456 7890 1234",
                    panNo = "DZXPP4321L",
                    fatherSpouseName = "Gordhanbhai Parmar",
                    dob = "1972-02-14",
                    gender = "Male",
                    occupation = "Farming & Tractors",
                    bankName = "Saurashtra Gramin Bank",
                    accountNumber = "78001234567",
                    ifscCode = "SGBA0000101",
                    bankBranch = "Anandpar",
                    approvalStatus = "Approved",
                    approvedAt = "2024-08-06",
                    approvedBy = "Admin",
                    roleId = 2
                ),
                MemberEntity(
                    memberId = 5,
                    memberNo = "SK-0005",
                    fullName = "Anand Mehta",
                    mobile = "9426098765",
                    email = "anand.mehta@example.com",
                    address = "55, Jagnath Plot, Rajkot",
                    joinDate = "2026-09-28",
                    isActive = true,
                    aadharNo = "6543 2109 8765",
                    panNo = "EHKPM6789N",
                    fatherSpouseName = "Dineshbhai Mehta",
                    dob = "1994-04-30",
                    gender = "Male",
                    occupation = "Software Consultant",
                    bankName = "ICICI Bank",
                    accountNumber = "002401567890",
                    ifscCode = "ICIC0000024",
                    bankBranch = "Yagnik Road",
                    approvalStatus = "Pending",
                    roleId = 2
                )
            )
            memberDao.insertMembers(members)

            // 2. Initial Loans
            val loans = listOf(
                LoanEntity(
                    loanId = 1,
                    memberId = 1,
                    memberNo = "SK-0001",
                    memberName = "Ramesh Patel",
                    mobile = "9999900001",
                    loanNo = "LN-2026-001",
                    loanType = "Agriculture Loan",
                    principalAmount = 150000.0,
                    interestRate = 8.5,
                    tenureMonths = 24,
                    emiAmount = 6820.0,
                    totalAmount = 163680.0,
                    disbursedDate = "2026-01-10",
                    dueDate = "2026-10-10",
                    outstandingBalance = 88660.0,
                    totalPaid = 75020.0,
                    status = "Active",
                    daysOverdue = 0,
                    remarks = "Kharif crop fertilizer and irrigation equipment",
                    guarantor1Name = "Bhaveshbhai Patel",
                    guarantor1Mobile = "9824055667"
                ),
                LoanEntity(
                    loanId = 2,
                    memberId = 2,
                    memberNo = "SK-0002",
                    memberName = "Suresh Shah",
                    mobile = "9825012345",
                    loanNo = "LN-2026-002",
                    loanType = "Business Loan",
                    principalAmount = 300000.0,
                    interestRate = 11.0,
                    tenureMonths = 36,
                    emiAmount = 9822.0,
                    totalAmount = 353592.0,
                    disbursedDate = "2025-11-01",
                    dueDate = "2026-10-01",
                    outstandingBalance = 235728.0,
                    totalPaid = 117864.0,
                    status = "Active",
                    daysOverdue = 0,
                    remarks = "Working capital for textile and grain trade",
                    guarantor1Name = "Kiritbhai Shah",
                    guarantor1Mobile = "9925011224"
                ),
                LoanEntity(
                    loanId = 3,
                    memberId = 3,
                    memberNo = "SK-0003",
                    memberName = "Geeta Ben Varma",
                    mobile = "9898011223",
                    loanNo = "LN-2026-003",
                    loanType = "Personal Loan",
                    principalAmount = 50000.0,
                    interestRate = 12.0,
                    tenureMonths = 12,
                    emiAmount = 4442.0,
                    totalAmount = 53304.0,
                    disbursedDate = "2026-03-15",
                    dueDate = "2026-08-15",
                    outstandingBalance = 26652.0,
                    totalPaid = 26652.0,
                    status = "Overdue",
                    daysOverdue = 49, // SMA-1 (31 to 60 days)
                    remarks = "Sewing machine & studio expansion",
                    guarantor1Name = "Kishorebhai Varma",
                    guarantor1Mobile = "9725066778"
                ),
                LoanEntity(
                    loanId = 4,
                    memberId = 4,
                    memberNo = "SK-0004",
                    memberName = "Pravin Bhai Parmar",
                    mobile = "9712345678",
                    loanNo = "LN-2026-004",
                    loanType = "Emergency Loan",
                    principalAmount = 75000.0,
                    interestRate = 10.0,
                    tenureMonths = 18,
                    emiAmount = 4505.0,
                    totalAmount = 81090.0,
                    disbursedDate = "2025-10-01",
                    dueDate = "2026-06-15",
                    outstandingBalance = 54060.0,
                    totalPaid = 27030.0,
                    status = "Overdue",
                    daysOverdue = 111, // NPA (>90 days)
                    remarks = "Medical emergency and farm pump repair",
                    guarantor1Name = "Manubhai Parmar",
                    guarantor1Mobile = "9426033445",
                    guarantor2Name = "Jagdishbhai Rathod",
                    guarantor2Mobile = "9825599881"
                )
            )
            loanDao.insertLoans(loans)

            // 3. Initial Repayments
            val repayments = listOf(
                RepaymentEntity(
                    repaymentId = 1,
                    loanId = 1,
                    loanNo = "LN-2026-001",
                    memberNo = "SK-0001",
                    memberName = "Ramesh Patel",
                    mobile = "9999900001",
                    receiptNo = "RCPT-2026-0182",
                    paymentDate = "2026-10-02",
                    amountPaid = 6820.0,
                    principalPaid = 5800.0,
                    interestPaid = 1020.0,
                    outstandingBalance = 88660.0,
                    paymentMode = "UPI",
                    collectedBy = "R. K. Dave (Officer)",
                    remarks = "Monthly installment via Google Pay"
                ),
                RepaymentEntity(
                    repaymentId = 2,
                    loanId = 2,
                    loanNo = "LN-2026-002",
                    memberNo = "SK-0002",
                    memberName = "Suresh Shah",
                    mobile = "9825012345",
                    receiptNo = "RCPT-2026-0183",
                    paymentDate = "2026-10-03",
                    amountPaid = 9822.0,
                    principalPaid = 7660.0,
                    interestPaid = 2162.0,
                    outstandingBalance = 235728.0,
                    paymentMode = "Cheque",
                    collectedBy = "P. N. Joshi (Counter)",
                    remarks = "Cheque #409211 cleared"
                )
            )
            repaymentDao.insertRepayments(repayments)

            // 4. Initial Recovery & Follow-Ups
            val followUps = listOf(
                FollowUpEntity(
                    followUpId = 1,
                    loanId = 3,
                    memberId = 3,
                    loanNo = "LN-2026-003",
                    memberName = "Geeta Ben Varma",
                    followUpDate = "2026-09-25",
                    followUpType = "Phone Call",
                    contactPerson = "Borrower (Geeta Ben)",
                    staffOrAgent = "H. M. Jadeja (Recovery Exec)",
                    responseSummary = "Customer assured payment by 10th October after garment festival sale.",
                    remarks = "PTP recorded for Rs 8,884."
                ),
                FollowUpEntity(
                    followUpId = 2,
                    loanId = 4,
                    memberId = 4,
                    loanNo = "LN-2026-004",
                    memberName = "Pravin Bhai Parmar",
                    followUpDate = "2026-09-20",
                    followUpType = "Field Visit",
                    contactPerson = "Guarantor 1 (Manubhai Parmar)",
                    staffOrAgent = "V. G. Vaghela (Inspector)",
                    responseSummary = "Met guarantor at Anandpar village. Borrower harvested groundnut, waiting for market APMC price.",
                    remarks = "Form-2 Final Demand Notice delivered by hand."
                )
            )
            recoveryDao.insertFollowUps(followUps)

            // 5. Initial Promise-To-Pay (PTP)
            val ptps = listOf(
                PtpEntity(
                    ptpId = 1,
                    loanId = 3,
                    memberId = 3,
                    loanNo = "LN-2026-003",
                    memberName = "Geeta Ben Varma",
                    mobile = "9898011223",
                    promisedAmount = 8884.0,
                    promisedDate = "2026-10-10",
                    recordedBy = "H. M. Jadeja",
                    status = "Pending",
                    remarks = "Two overdue EMIs committed"
                )
            )
            recoveryDao.insertPtp(ptps[0])

            // 6. Initial Demand Notice
            val notices = listOf(
                DemandNoticeEntity(
                    noticeId = 1,
                    noticeNo = "DN/2026/042",
                    loanId = 4,
                    loanNo = "LN-2026-004",
                    memberId = 4,
                    memberName = "Pravin Bhai Parmar",
                    mobile = "9712345678",
                    address = "Village Anandpar, Dist. Rajkot",
                    noticeType = "Form-2 Final Notice",
                    noticeDate = "2026-09-18",
                    outstandingPrincipal = 54060.0,
                    overdueInterest = 3250.0,
                    penalInterest = 1081.0,
                    noticeCharges = 150.0,
                    totalDemanded = 58541.0,
                    noticeStatus = "Served",
                    advocateName = "S. M. Trivedi, Advocate",
                    remarks = "Served with 15 days cure period before Section 101 filing."
                )
            )
            recoveryDao.insertDemandNotice(notices[0])

            // 7. Initial Section 101 Cooperative Recovery Case
            val s101Cases = listOf(
                Section101CaseEntity(
                    caseId = 1,
                    loanId = 4,
                    loanNo = "LN-2026-004",
                    memberId = 4,
                    memberName = "Pravin Bhai Parmar",
                    mobile = "9712345678",
                    caseNo = "ARB/101/2026/089",
                    filingDate = "2026-09-30",
                    authorityCourt = "Board of Nominees, Rajkot Division",
                    claimAmount = 58541.0,
                    hearingDate = "2026-10-25",
                    caseStatus = "Notice Issued",
                    remarks = "Recovery Certificate under Gujarat Co-operative Societies Act Section 101."
                )
            )
            recoveryDao.insertSection101Case(s101Cases[0])
        }
    }
}
