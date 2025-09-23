package com.example.backend.config;

import com.example.backend.entity.Category;
import com.example.backend.entity.Transaction;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.enums.BudgetPeriod;
import com.example.backend.enums.Currency;
import com.example.backend.enums.TransactionType;
import com.example.backend.enums.UserStatus;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SAMPLE_USER_EMAIL = "ltha221104@gmail.com";
    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail(SAMPLE_USER_EMAIL).isEmpty()) {
            System.out.println("====== Starting Data Seeding ======");

            // 1. Tạo người dùng mẫu
            User sampleUser = createUser();

            // 2. Tạo danh mục với dữ liệu đa dạng
            List<Category> expenseCategories = createDiverseExpenseCategories(sampleUser);
            List<Category> incomeCategories = createIncomeCategories(sampleUser);

            // 3. Tạo ví với tên, số dư và ghi chú thực tế
            List<Wallet> wallets = createRealisticWallets(sampleUser);

            // 4. Tạo 100 giao dịch lịch sử từ tháng 1 đến giờ
            createHistoricalTransactions(sampleUser, wallets, expenseCategories, incomeCategories);

            // 5. Bổ sung giao dịch cho tuần hiện tại (full các ngày)
            createThisWeeksTransactions(sampleUser, wallets, expenseCategories, incomeCategories);

            // 6. Cập nhật lại số dư cuối cùng của các ví vào DB
            walletRepository.saveAll(wallets);

            System.out.println("====== Data Seeding Completed ======");
        } else {
            System.out.println("====== Data already exists. Skipping seeding. ======");
        }
    }

    private User createUser() {
        User user = new User();
        user.setEmail(SAMPLE_USER_EMAIL);
        user.setUsername("nhinhi");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setFirstName("Yến");
        user.setLastName("Nhi");
        user.setPhoneNumber("0123456789");
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private List<Category> createDiverseExpenseCategories(User user) {
        List<String> categoryNames = Arrays.asList(
                "Ăn uống", "Đi lại", "Mua sắm", "Giải trí", "Hóa đơn Điện & Nước",
                "Gia đình", "Sức khỏe & Thuốc", "Giáo dục con cái", "Đầu tư Chứng khoán", "Du lịch & Phượt",
                "Quần áo & Phụ kiện", "Quà tặng & Đám tiệc", "Phát triển bản thân", "Sửa chữa nhà cửa", "Chăm sóc thú cưng",
                "Phí dịch vụ (Internet, Netflix)", "Từ thiện", "Giao dịch linh tinh", "Điện thoại & 4G", "Xăng xe"
        );

        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < categoryNames.size(); i++) {
            Category.CategoryBuilder builder = Category.builder()
                    .name(categoryNames.get(i))
                    .user(user)
                    .color(generateRandomHexColor());

            // Tạo dữ liệu đa dạng theo các mẫu
            switch (i % 5) {
                case 0: // Mẫu 1: Đầy đủ tất cả các trường
                    builder.description("Chi tiêu thiết yếu hàng tháng cho " + categoryNames.get(i).toLowerCase())
                            .budgetAmount(new BigDecimal(random.nextInt(4000000) + 1000000)) // Ngân sách từ 1-5 triệu
                            .budgetPeriod(BudgetPeriod.MONTHLY)
                            .incomeTargetAmount(new BigDecimal(random.nextInt(500000) + 100000)) // Mục tiêu thu nhập từ 100k-600k (ví dụ: bán đồ cũ)
                            .incomeTargetPeriod(BudgetPeriod.MONTHLY);
                    break;
                case 1: // Mẫu 2: Chỉ có Ngân sách chi tiêu
                    builder.budgetAmount(new BigDecimal(random.nextInt(2000000) + 500000)) // Ngân sách từ 500k-2.5 triệu
                            .budgetPeriod(BudgetPeriod.MONTHLY);
                    break;
                case 2: // Mẫu 3: Chỉ có Mục tiêu thu nhập và ghi chú
                    builder.description("Cố gắng kiếm thêm từ " + categoryNames.get(i).toLowerCase())
                            .incomeTargetAmount(new BigDecimal(random.nextInt(1000000) + 200000)) // Mục tiêu từ 200k-1.2 triệu
                            .incomeTargetPeriod(BudgetPeriod.MONTHLY);
                    break;
                case 3: // Mẫu 4: Chỉ có ghi chú
                    builder.description("Các khoản chi cho " + categoryNames.get(i).toLowerCase());
                    break;
                case 4: // Mẫu 5: Rỗng, chỉ có tên (dữ liệu tối thiểu)
                    // Không làm gì thêm
                    break;
            }
            categories.add(builder.build());
        }
        return categoryRepository.saveAll(categories);
    }

    private List<Category> createIncomeCategories(User user) {
        List<Category> categories = new ArrayList<>();
        categories.add(Category.builder().name("Lương").user(user).color("#28a745").description("Lương hàng tháng từ công ty").build());
        categories.add(Category.builder().name("Thưởng & Bonus").user(user).color("#17a2b8").description("Thưởng dự án, thưởng lễ Tết").build());
        categories.add(Category.builder().name("Thu nhập phụ").user(user).color("#ffc107").description("Từ các công việc làm thêm ngoài giờ").build());
        return categoryRepository.saveAll(categories);
    }

    private List<Wallet> createRealisticWallets(User user) {
        List<Wallet> wallets = new ArrayList<>();

        // Dùng danh sách để đảm bảo không bị trùng lặp
        List<String> walletNames = new ArrayList<>(Arrays.asList(
                "Tiền mặt", "Tài khoản VCB lương", "Ví Momo", "Thẻ tín dụng Shinhan", "Tài khoản Techcombank",
                "Ví ZaloPay", "Tài khoản TPBank", "Tài khoản Cake", "Thẻ tín dụng VIB", "Tài khoản chứng khoán VPS",
                "Quỹ tiết kiệm", "Ví ShopeePay", "Tài khoản MB Bank", "Thẻ tín dụng HSBC", "Tài khoản Sacombank",
                "Đầu tư Finhay", "Tiền ngoại tệ (USD)", "Quỹ đen", "Tài khoản con", "Ví Grab Moca"
        ));
        Collections.shuffle(walletNames); // Xáo trộn để mỗi lần chạy có thể khác nhau

        List<String> icons = Arrays.asList("money.json", "credit.json", "wallet.json", "dollar.json", "chart.json");

        for (int i = 0; i < 20; i++) {
            Wallet wallet = new Wallet();
            String name = walletNames.get(i);
            wallet.setName(name);
            wallet.setUser(user);
            wallet.setCurrency(Currency.VND);
            wallet.setIcon(icons.get(i % icons.size()));

            // Gán số dư và ghi chú thực tế hơn
            if (name.contains("Tiền mặt")) {
                wallet.setBalance(new BigDecimal(random.nextInt(3000000) + 500000)); // 500k - 3.5tr
                wallet.setDescription("Tiền mặt dùng cho chi tiêu vặt hàng ngày.");
            } else if (name.contains("tín dụng")) {
                wallet.setBalance(new BigDecimal(random.nextInt(50000000) + 20000000).negate()); // Âm từ 20-70tr
                wallet.setDescription("Hạn mức thẻ " + name);
            } else if (name.contains("lương")) {
                wallet.setBalance(new BigDecimal(random.nextInt(80000000) + 25000000)); // 25tr - 105tr
                wallet.setDescription("Tài khoản nhận lương chính hàng tháng.");
            } else if (name.contains("Ví")) {
                wallet.setBalance(new BigDecimal(random.nextInt(5000000) + 200000)); // 200k - 5.2tr
                wallet.setDescription("Ví điện tử thanh toán online và chuyển tiền.");
            } else if (name.contains("Đầu tư") || name.contains("chứng khoán")) {
                wallet.setBalance(new BigDecimal(random.nextInt(200000000) + 50000000)); // 50tr - 250tr
                wallet.setDescription("Tài sản trong các kênh đầu tư.");
            }
            else {
                wallet.setBalance(new BigDecimal(random.nextInt(40000000) + 10000000)); // 10tr - 50tr
                wallet.setDescription("Tài khoản ngân hàng phụ.");
            }
            wallets.add(wallet);
        }
        return walletRepository.saveAll(wallets);
    }

    private void createHistoricalTransactions(User user, List<Wallet> wallets, List<Category> expenseCategories, List<Category> incomeCategories) {
        long startEpochSecond = LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long endEpochSecond = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        createRandomTransactions(user, wallets, expenseCategories, incomeCategories, 100, startEpochSecond, endEpochSecond);
    }

    private void createThisWeeksTransactions(User user, List<Wallet> wallets, List<Category> expenseCategories, List<Category> incomeCategories) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            // Chỉ tạo giao dịch cho các ngày từ đầu tuần đến hôm nay
            if (currentDay.isAfter(today)) {
                break;
            }

            long startOfDay = currentDay.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            long endOfDay = currentDay.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1;

            // Tạo từ 1 đến 3 giao dịch mỗi ngày
            int numberOfTransactions = random.nextInt(3) + 1;
            createRandomTransactions(user, wallets, expenseCategories, incomeCategories, numberOfTransactions, startOfDay, endOfDay);
        }
    }

    private void createRandomTransactions(User user, List<Wallet> wallets, List<Category> expenseCategories, List<Category> incomeCategories, int count, long startEpochSecond, long endEpochSecond) {
        List<Transaction> transactions = new ArrayList<>();

        if (startEpochSecond >= endEpochSecond) return;

        for (int i = 0; i < count; i++) {
            Wallet randomWallet = wallets.get(random.nextInt(wallets.size()));
            long randomEpochSecond = ThreadLocalRandom.current().nextLong(startEpochSecond, endEpochSecond);
            Instant randomInstant = Instant.ofEpochSecond(randomEpochSecond);

            TransactionType type = (random.nextInt(10) < 8) ? TransactionType.EXPENSE : TransactionType.INCOME;
            BigDecimal amount;
            Category category;
            String description;

            if (type == TransactionType.EXPENSE) {
                amount = new BigDecimal(random.nextInt(1950001) + 50000);
                category = expenseCategories.get(random.nextInt(expenseCategories.size()));
                description = "Thanh toán cho " + category.getName().toLowerCase();
            } else {
                amount = new BigDecimal(random.nextInt(10000001) + 2000000);
                category = incomeCategories.get(random.nextInt(incomeCategories.size()));
                description = "Nhận tiền " + category.getName().toLowerCase();
            }

            BigDecimal currentBalance = randomWallet.getBalance();
            BigDecimal balanceAfter = (type == TransactionType.INCOME) ? currentBalance.add(amount) : currentBalance.subtract(amount);
            randomWallet.setBalance(balanceAfter);

            Transaction transaction = Transaction.builder()
                    .user(user)
                    .wallet(randomWallet)
                    .category(category)
                    .type(type)
                    .amount(amount)
                    .date(randomInstant)
                    .createdAt(Instant.now())
                    .description(description)
                    .balanceAfterTransaction(balanceAfter)
                    .build();
            transactions.add(transaction);
        }
        transactionRepository.saveAll(transactions);
    }

    private String generateRandomHexColor() {
        return String.format("#%06x", random.nextInt(0xffffff + 1));
    }
}