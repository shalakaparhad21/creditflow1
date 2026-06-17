package com.creditflow.scoring;

import com.creditflow.model.CustomerFeature;
import com.creditflow.model.ScoredCustomer;
import com.creditflow.model.ScoredCustomer.Tier;

/**
 * DSA: Decision Tree (nested conditionals)
 *
 * Stage 1 — Hard reject rules (any one fails = immediate reject)
 * Stage 2 — Tier assignment A/B/C + limit calculation
 *
 * Limits mirror Indian banking practice:
 *   Tier A (750-900): up to 3x income, max Rs.10L
 *   Tier B (600-749): up to 1.5x income, max Rs.5L
 *   Tier C (500-599): 0.5x income, max Rs.2L
 *
 * Capital reserve: 8% of limit (Basel III requirement)
 */
public class ApprovalEngine {

    private static final int    MIN_SCORE       = 500;
    private static final int    MAX_MISSED      = 3;
    private static final double MAX_DTI         = 0.55;
    private static final double MAX_UTILIZATION = 0.90;

    private static final int TIER_A_MIN = 750;
    private static final int TIER_B_MIN = 600;

    public static ScoredCustomer decide(Customer_stub c, CustomerFeature f,
                                        int score, String name) {
        // Stage 1: Hard reject rules
        if (score < MIN_SCORE)
            return reject(c.customerId, name, score,
                "Score " + score + " below minimum threshold of 500");
        if (f.missedPayments > MAX_MISSED)
            return reject(c.customerId, name, score,
                "Too many missed payments (" + f.missedPayments + " > " + MAX_MISSED + ")");
        if (f.debtToIncomeRatio > MAX_DTI)
            return reject(c.customerId, name, score,
                String.format("DTI too high (%.2f > %.2f)", f.debtToIncomeRatio, MAX_DTI));
        if (f.utilizationRate > MAX_UTILIZATION)
            return reject(c.customerId, name, score,
                String.format("Utilization too high (%.0f%% > 90%%)", f.utilizationRate * 100));

        // Stage 2: Tier assignment
        Tier   tier;
        double annualRate;
        double limitMultiplier;
        long   maxLimit;

        if (score >= TIER_A_MIN) {
            tier           = Tier.A;
            annualRate     = 0.18;
            limitMultiplier = score >= 800 ? 3.0 : 2.5;
            maxLimit       = 1_000_000L;
        } else if (score >= TIER_B_MIN) {
            tier           = Tier.B;
            annualRate     = 0.24;
            limitMultiplier = score >= 700 ? 1.5 : 1.0;
            maxLimit       = 500_000L;
        } else {
            tier           = Tier.C;
            annualRate     = 0.36;
            limitMultiplier = 0.5;
            maxLimit       = 200_000L;
        }

        if (f.debtToIncomeRatio > 0.40) limitMultiplier *= 0.8;

        long rawLimit      = (long)(f.monthlyIncome * limitMultiplier);
        long assignedLimit = Math.min(rawLimit, maxLimit);
        assignedLimit      = (assignedLimit / 10_000) * 10_000;
        assignedLimit      = Math.max(assignedLimit, 10_000);

        long   requiredCapital = (long)(assignedLimit * 0.08);
        double expectedRevenue = assignedLimit * annualRate * 0.40;

        return new ScoredCustomer(c.customerId, name, score, tier, true,
                                  assignedLimit, requiredCapital, expectedRevenue, null);
    }

    private static ScoredCustomer reject(String id, String name, int score, String reason) {
        return new ScoredCustomer(id, name, score, Tier.REJECTED, false, 0, 0, 0, reason);
    }

    public record Customer_stub(String customerId) {}
}
