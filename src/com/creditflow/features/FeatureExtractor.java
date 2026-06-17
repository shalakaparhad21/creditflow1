package com.creditflow.features;

import com.creditflow.model.Customer;
import com.creditflow.model.CustomerFeature;

/**
 * Extracts behavioral features from a Customer's transaction history.
 * Uses TransactionStream (Deque sliding window) for O(n) processing.
 */
public class FeatureExtractor {

    private static final int WINDOW_DAYS = 90;

    public static CustomerFeature extract(Customer c) {

        TransactionStream stream = new TransactionStream(WINDOW_DAYS);
        stream.loadAll(c.transactions);

        double avgMonthlySpend      = stream.avgMonthlySpend();
        double spendVolatility      = stream.spendVolatility();
        double maxSingleTransaction = stream.maxSingleTransaction();
        double salaryConsistency    = stream.salaryConsistency();

        double paymentHistoryScore = Math.max(0.0, 1.0 - (c.missedPayments * 0.2));

        double utilizationRate = (c.currentLimit > 0)
                ? c.currentBalance / c.currentLimit : 0.0;

        double debtToIncomeRatio = (c.monthlyIncome > 0)
                ? (c.existingEmi + avgMonthlySpend * 0.3) / c.monthlyIncome
                : 1.0;

        return new CustomerFeature(
            c.customerId,
            c.missedPayments,
            paymentHistoryScore,
            utilizationRate,
            debtToIncomeRatio,
            c.monthlyIncome,
            avgMonthlySpend,
            spendVolatility,
            maxSingleTransaction,
            salaryConsistency
        );
    }
}
