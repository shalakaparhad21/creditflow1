package com.creditflow.scoring;

import com.creditflow.features.CustomerIndex;
import com.creditflow.features.FeatureExtractor;
import com.creditflow.model.Customer;
import com.creditflow.model.CustomerFeature;
import com.creditflow.model.ScoredCustomer;

import java.util.*;

/**
 * Orchestrates the full scoring pipeline for a batch.
 * Builds SegmentTree and CustomerIndex after scoring all customers.
 * Maintains a min-heap (PriorityQueue) of riskiest approved customers.
 */
public class ScoringPipeline {

    private List<ScoredCustomer>          results;
    private SegmentTree                   segTree;
    private CustomerIndex                 index;
    private PriorityQueue<ScoredCustomer> riskHeap;

    public List<ScoredCustomer> run(List<Customer> customers) {
        results  = new ArrayList<>(customers.size());
        index    = new CustomerIndex();
        riskHeap = new PriorityQueue<>(Comparator.comparingInt(sc -> sc.creditScore));

        for (Customer c : customers) {
            CustomerFeature feature = FeatureExtractor.extract(c);
            int score = CreditScorer.score(feature);
            ScoredCustomer scored = ApprovalEngine.decide(
                new ApprovalEngine.Customer_stub(c.customerId), feature, score, c.name);

            results.add(scored);

            if (scored.approved) {
                index.insert(scored);
                riskHeap.offer(scored);
            }
        }

        buildSegmentTree();
        return Collections.unmodifiableList(results);
    }

    private void buildSegmentTree() {
        List<ScoredCustomer> approved = results.stream()
            .filter(sc -> sc.approved)
            .sorted(Comparator.comparingInt(sc -> sc.creditScore))
            .toList();

        long[] limits = new long[approved.size()];
        for (int i = 0; i < approved.size(); i++) limits[i] = approved.get(i).assignedLimit;
        segTree = new SegmentTree(limits);
    }

    public List<ScoredCustomer> topNRiskiest(int n) {
        PriorityQueue<ScoredCustomer> copy = new PriorityQueue<>(riskHeap);
        List<ScoredCustomer> top = new ArrayList<>();
        while (!copy.isEmpty() && top.size() < n) top.add(copy.poll());
        return top;
    }

    public Map<String, Object> summary() {
        long approved      = results.stream().filter(sc ->  sc.approved).count();
        long rejected      = results.stream().filter(sc -> !sc.approved).count();
        long totalExposure = results.stream().mapToLong(sc -> sc.assignedLimit).sum();
        double totalRev    = results.stream().mapToDouble(sc -> sc.expectedRevenue).sum();
        double avgScore    = results.stream().mapToInt(sc -> sc.creditScore).average().orElse(0);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalProcessed", results.size());
        m.put("approved",       approved);
        m.put("rejected",       rejected);
        m.put("approvalRate",   results.isEmpty() ? 0 : (double) approved / results.size() * 100);
        m.put("totalExposure",  totalExposure);
        m.put("totalRevenue",   totalRev);
        m.put("avgScore",       avgScore);
        return m;
    }

    public List<ScoredCustomer> getResults() { return results; }
    public SegmentTree          getSegTree() { return segTree; }
    public CustomerIndex        getIndex()   { return index;   }
}
