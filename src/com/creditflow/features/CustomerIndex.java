package com.creditflow.features;

import com.creditflow.model.ScoredCustomer;
import java.util.*;

/**
 * DSA: TreeMap<Integer, List<ScoredCustomer>>
 * Red-Black Tree internally. Keys (credit scores) stay sorted automatically.
 * subMap() gives O(log n + k) range queries.
 */
public class CustomerIndex {

    private final TreeMap<Integer, List<ScoredCustomer>> index = new TreeMap<>();
    private int totalCustomers = 0;

    public void insert(ScoredCustomer sc) {
        index.computeIfAbsent(sc.creditScore, k -> new ArrayList<>()).add(sc);
        totalCustomers++;
    }

    public void insertAll(List<ScoredCustomer> customers) {
        customers.forEach(this::insert);
    }

    public List<ScoredCustomer> queryRange(int minScore, int maxScore) {
        List<ScoredCustomer> result = new ArrayList<>();
        index.subMap(minScore, true, maxScore, true)
             .values().forEach(result::addAll);
        return result;
    }

    public long totalExposureInBand(int minScore, int maxScore) {
        return queryRange(minScore, maxScore).stream()
                .mapToLong(sc -> sc.assignedLimit).sum();
    }

    public int countInBand(int minScore, int maxScore) {
        return queryRange(minScore, maxScore).size();
    }

    public double percentileRank(int score) {
        if (totalCustomers == 0) return 0;
        int below = index.headMap(score, false)
                         .values().stream().mapToInt(List::size).sum();
        return (double) below / totalCustomers * 100.0;
    }

    public List<ScoredCustomer> topNHighestRisk(int n) {
        List<ScoredCustomer> result = new ArrayList<>();
        for (List<ScoredCustomer> bucket : index.values()) {
            for (ScoredCustomer sc : bucket) {
                result.add(sc);
                if (result.size() == n) return result;
            }
        }
        return result;
    }

    public Map<String, Integer> scoreBandDistribution() {
        Map<String, Integer> dist = new LinkedHashMap<>();
        int[] bands = {300,350,400,450,500,550,600,650,700,750,800,850,900};
        for (int i = 0; i < bands.length - 1; i++) {
            String key = bands[i] + "-" + bands[i+1];
            dist.put(key, countInBand(bands[i], bands[i+1] - 1));
        }
        return dist;
    }

    public int totalCustomers() { return totalCustomers; }
}
