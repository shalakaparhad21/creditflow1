package com.creditflow.scoring;

import com.creditflow.model.CustomerFeature;

/**
 * Weighted credit scoring formula — mirrors CIBIL/FICO methodology.
 *
 * Weights:
 *   Payment history   35%
 *   Utilization       30%
 *   Debt-to-Income    20%
 *   Spend stability   10%
 *   Salary consist.    5%
 *
 * Output: integer score in range [300, 900]
 * Complexity: O(1) per customer.
 */
public class CreditScorer {

    private static final double W_PAYMENT_HISTORY = 0.35;
    private static final double W_UTILIZATION     = 0.30;
    private static final double W_DEBT_TO_INCOME  = 0.20;
    private static final double W_SPEND_STABILITY = 0.10;
    private static final double W_SALARY_CONSIST  = 0.05;

    public static int score(CustomerFeature f) {
        double paymentScore    = f.paymentHistoryScore;

        double utilizationScore = Math.max(0.0, 1.0 - f.utilizationRate);
        if (f.utilizationRate > 0.75) utilizationScore *= 0.5;

        double dtiScore       = Math.max(0.0, 1.0 - (f.debtToIncomeRatio / 0.5));

        double stabilityScore = Math.max(0.0, 1.0 - (f.spendVolatility / 50_000.0));

        double salaryScore    = f.salaryConsistency;

        double rawScore = (paymentScore    * W_PAYMENT_HISTORY)
                        + (utilizationScore * W_UTILIZATION)
                        + (dtiScore        * W_DEBT_TO_INCOME)
                        + (stabilityScore  * W_SPEND_STABILITY)
                        + (salaryScore     * W_SALARY_CONSIST);

        rawScore = Math.max(0.0, Math.min(1.0, rawScore));
        return (int) Math.round(300 + rawScore * 600);
    }

    /**
     * Returns point contribution of each component.
     * Index: [0]=payment, [1]=utilization, [2]=dti, [3]=stability, [4]=salary
     */
    public static double[] breakdown(CustomerFeature f) {
        double paymentScore    = f.paymentHistoryScore;
        double utilizationScore = Math.max(0.0, 1.0 - f.utilizationRate);
        if (f.utilizationRate > 0.75) utilizationScore *= 0.5;
        double dtiScore        = Math.max(0.0, 1.0 - (f.debtToIncomeRatio / 0.5));
        double stabilityScore  = Math.max(0.0, 1.0 - (f.spendVolatility / 50_000.0));
        double salaryScore     = f.salaryConsistency;

        return new double[]{
            paymentScore    * W_PAYMENT_HISTORY * 600,
            utilizationScore * W_UTILIZATION    * 600,
            dtiScore        * W_DEBT_TO_INCOME  * 600,
            stabilityScore  * W_SPEND_STABILITY * 600,
            salaryScore     * W_SALARY_CONSIST  * 600
        };
    }
}
