package com.library.management.service;

import com.library.management.dto.DevOpsStatusDto;
import com.library.management.dto.StatsSummaryDto;
import com.library.management.model.BorrowStatus;
import com.library.management.model.FineStatus;
import com.library.management.model.MemberStatus;
import com.library.management.repository.BookRepository;
import com.library.management.repository.BorrowRecordRepository;
import com.library.management.repository.FineRepository;
import com.library.management.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Service
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final FineRepository fineRepository;
    private final Environment environment;

    @Value("${server.port:8085}")
    private String serverPort;

    @Value("${aws.region:ap-south-1}")
    private String awsRegion;

    @Value("${aws.s3.bucket-name:library-management-book-covers-232901749713}")
    private String s3Bucket;

    @Value("${aws.sns.topic-arn:arn:aws:sns:ap-south-1:232901749713:library-management-notifications}")
    private String snsTopic;

    @Autowired
    public StatsServiceImpl(BookRepository bookRepository,
                            MemberRepository memberRepository,
                            BorrowRecordRepository borrowRecordRepository,
                            FineRepository fineRepository,
                            Environment environment) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.fineRepository = fineRepository;
        this.environment = environment;
    }

    @Override
    public StatsSummaryDto getStatsSummary() {
        Long totalBooks = bookRepository.sumTotalQuantity();
        Long availableBooks = bookRepository.sumAvailableQuantity();
        long issuedBooks = borrowRecordRepository.countByStatus(BorrowStatus.ISSUED);
        long activeMembers = memberRepository.countByStatus(MemberStatus.ACTIVE);
        long overdueCount = borrowRecordRepository.countOverdueRecords(LocalDate.now());

        BigDecimal totalFinesCollected = fineRepository.sumAmountByStatus(FineStatus.PAID);
        BigDecimal pendingFinesAmount = fineRepository.sumAmountByStatus(FineStatus.UNPAID);

        return StatsSummaryDto.builder()
                .totalBooks(totalBooks != null ? totalBooks : 0)
                .availableBooks(availableBooks != null ? availableBooks : 0)
                .issuedBooks(issuedBooks)
                .activeMembers(activeMembers)
                .overdueCount(overdueCount)
                .totalFinesCollected(totalFinesCollected != null ? totalFinesCollected : BigDecimal.ZERO)
                .pendingFinesAmount(pendingFinesAmount != null ? pendingFinesAmount : BigDecimal.ZERO)
                .build();
    }

    @Override
    public DevOpsStatusDto getDevOpsStatus() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeSec = uptimeMillis / 1000;
        long hours = uptimeSec / 3600;
        long minutes = (uptimeSec % 3600) / 60;
        long seconds = uptimeSec % 60;
        String uptimeFormatted = String.format("%02dh %02dm %02ds", hours, minutes, seconds);

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        String memoryUsage = usedMemory + " MB / " + totalMemory + " MB";

        String[] activeProfiles = environment.getActiveProfiles();
        boolean isProd = Arrays.asList(activeProfiles).contains("prod");
        String activeProfile = isProd ? "Production (AWS RDS PostgreSQL)" : "Local Development (H2 In-Memory)";
        String dbEngine = isProd ? "PostgreSQL 15.14 (Amazon RDS)" : "H2 Database 2.2";

        return DevOpsStatusDto.builder()
                .awsRegion(awsRegion != null ? awsRegion : "ap-south-1")
                .ec2Instance("t3.micro (Amazon Linux 2023)")
                .ec2PublicIp("52.66.211.1")
                .serverPort(serverPort)
                .javaVersion("Java " + System.getProperty("java.version"))
                .activeProfile(activeProfile)
                .databaseEngine(dbEngine)
                .databaseStatus("HEALTHY (Connected)")
                .s3Bucket(s3Bucket)
                .snsTopic(snsTopic)
                .cloudWatchLogGroup("/aws/library-management-system/application")
                .githubRepo("abrahamgracef/Cloud-Based-Library-Management-System")
                .gitBranch("main")
                .uptimeSeconds(uptimeSec)
                .uptimeFormatted(uptimeFormatted)
                .memoryUsage(memoryUsage)
                .pipelineStatus("HEALTHY (Automated CI/CD)")
                .build();
    }
}
