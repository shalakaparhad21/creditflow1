package com.creditflow.features;

import com.creditflow.model.Transaction;
import java.time.LocalDate;
import java.util.*;

/**
 * DSA: ArrayDeque — sliding window over transaction history.
 * New transactions added to back O(1), expired ones removed from front O(1).
 * Total processing: O(n) for entire stream.
 */
public class TransactionStream {

    private final int windowDays;
    private final Deque<Transaction> window;
    private LocalDate windowEnd;

    private double runningDebitSum  = 0;
    private double runningCreditSum = 0;

    public TransactionStream(int windowDays) {
        this.windowDays = windowDays;
        this.window     = new ArrayDeque<>();
    }

    public void add(Transaction t) {
        window.addLast(t);
        if (t.isDebit())  runningDebitSum  += t.amount;
        else              runningCreditSum += t.amount;
        windowEnd = t.date;
        evictExpired();
    }

    public void loadAll(List<Transaction> transactions) {
        transactions.stream()
            .sorted(Comparator.comparing(t -> t.date))
            .forEach(this::add);
    }

    private void evictExpired() {
        if (windowEnd == null) return;
        LocalDate cutoff = windowEnd.minusDays(windowDays);
        while (!window.isEmpty() && !window.peekFirst().date.isAfter(cutoff)) {
            Transaction evicted = window.pollFirst();
            if (evicted.isDebit())  runningDebitSum  -= evicted.amount;
            else                    runningCreditSum -= evicted.amount;
        }
    }

    public double avgMonthlySpend() {
        if (window.isEmpty()) return 0;
        double months = Math.max(windowDays / 30.0, 1.0);
        return runningDebitSum / months;
    }

    public double totalSpend()   { return runningDebitSum;  }
    public double totalInflow()  { return runningCreditSum; }

    public double spendVolatility() {
        if (window.isEmpty()) return 0;
        Map<Integer, Double> weeklySpend = new TreeMap<>();
        for (Transaction t : window) {
            if (!t.isDebit()) continue;
            int weekKey = (int)(t.date.toEpochDay() / 7);
            weeklySpend.merge(weekKey, t.amount, Double::sum);
        }
        if (weeklySpend.size() < 2) return 0;
        double[] vals = weeklySpend.values().stream().mapToDouble(Double::doubleValue).toArray();
        double mean = Arrays.stream(vals).average().orElse(0);
        double variance = Arrays.stream(vals).map(v -> (v - mean) * (v - mean)).average().orElse(0);
        return Math.sqrt(variance);
    }

    public double maxSingleTransaction() {
        return window.stream().filter(Transaction::isDebit)
                     .mapToDouble(t -> t.amount).max().orElse(0);
    }

    public double salaryConsistency() {
        if (window.isEmpty()) return 0;
        Set<String> monthsWithSalary = new HashSet<>();
        Set<String> allMonths        = new HashSet<>();
        for (Transaction t : window) {
            String monthKey = t.date.getYear() + "-" + t.date.getMonthValue();
            allMonths.add(monthKey);
            if (t.isCredit() && "SALARY".equalsIgnoreCase(t.category))
                monthsWithSalary.add(monthKey);
        }
        return allMonths.isEmpty() ? 0 : (double) monthsWithSalary.size() / allMonths.size();
    }

    public boolean isEmpty() { return window.isEmpty(); }
}
