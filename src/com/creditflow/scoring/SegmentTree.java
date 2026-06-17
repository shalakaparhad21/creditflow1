package com.creditflow.scoring;

/**
 * DSA: Segment Tree (array-based, 1-indexed)
 *
 * Stores credit limits of approved customers sorted by score.
 * Supports range-sum and range-max queries in O(log n).
 * Point updates in O(log n).
 * Build time O(n).
 *
 * Banking use: "Total exposure for score band 650-750?" -> rangeSum() O(log n)
 */
public class SegmentTree {

    private final long[] sumTree;
    private final long[] maxTree;
    private final int    n;

    public SegmentTree(long[] values) {
        this.n       = values.length;
        this.sumTree = new long[4 * Math.max(n, 1)];
        this.maxTree = new long[4 * Math.max(n, 1)];
        if (n > 0) build(values, 1, 0, n - 1);
    }

    private void build(long[] values, int node, int l, int r) {
        if (l == r) {
            sumTree[node] = values[l];
            maxTree[node] = values[l];
            return;
        }
        int mid = (l + r) / 2;
        build(values, 2 * node,     l,     mid);
        build(values, 2 * node + 1, mid + 1, r);
        pushUp(node);
    }

    private void pushUp(int node) {
        sumTree[node] = sumTree[2 * node] + sumTree[2 * node + 1];
        maxTree[node] = Math.max(maxTree[2 * node], maxTree[2 * node + 1]);
    }

    public void update(int idx, long newValue) {
        if (n > 0) update(1, 0, n - 1, idx, newValue);
    }

    private void update(int node, int l, int r, int idx, long newValue) {
        if (l == r) { sumTree[node] = newValue; maxTree[node] = newValue; return; }
        int mid = (l + r) / 2;
        if (idx <= mid) update(2 * node,     l,     mid, idx, newValue);
        else            update(2 * node + 1, mid + 1, r, idx, newValue);
        pushUp(node);
    }

    public long rangeSum(int ql, int qr) {
        if (ql > qr || n == 0) return 0;
        return querySum(1, 0, n - 1, Math.max(0, ql), Math.min(n - 1, qr));
    }

    private long querySum(int node, int l, int r, int ql, int qr) {
        if (ql <= l && r <= qr) return sumTree[node];
        if (r < ql  || l > qr) return 0;
        int mid = (l + r) / 2;
        return querySum(2 * node, l, mid, ql, qr)
             + querySum(2 * node + 1, mid + 1, r, ql, qr);
    }

    public long rangeMax(int ql, int qr) {
        if (ql > qr || n == 0) return 0;
        return queryMax(1, 0, n - 1, Math.max(0, ql), Math.min(n - 1, qr));
    }

    private long queryMax(int node, int l, int r, int ql, int qr) {
        if (ql <= l && r <= qr) return maxTree[node];
        if (r < ql  || l > qr) return 0;
        int mid = (l + r) / 2;
        return Math.max(queryMax(2 * node, l, mid, ql, qr),
                        queryMax(2 * node + 1, mid + 1, r, ql, qr));
    }

    public long totalSum() { return n > 0 ? sumTree[1] : 0; }
    public long totalMax() { return n > 0 ? maxTree[1] : 0; }
    public int  size()     { return n; }
}
