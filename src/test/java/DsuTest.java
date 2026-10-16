
import algorithms.Dsu;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DsuTest {

    @Test
    void findAndComponentsAgreeWithReferenceAfterRandomUnions() {
        Random r = new Random(1);
        for (int round = 0; round < 30; round++) {
            int n = 1 + r.nextInt(200);
            Dsu dsu = new Dsu(n);
            int[] label = new int[n];
            for (int i = 0; i < n; i++) {
                label[i] = i;
            }

            int ops = r.nextInt(300);
            for (int k = 0; k < ops; k++) {
                int a = r.nextInt(n);
                int b = r.nextInt(n);
                boolean alreadySame = label[a] == label[b];
                boolean merged = dsu.union(a, b);
                assertEquals(!alreadySame, merged);
                if (!alreadySame) {
                    int old = label[b];
                    for (int i = 0; i < n; i++) {
                        if (label[i] == old) {
                            label[i] = label[a];
                        }
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    assertEquals(label[i] == label[j], dsu.find(i) == dsu.find(j));
                }
            }
            Set<Integer> distinct = new HashSet<>();
            for (int x : label) {
                distinct.add(x);
            }
            assertEquals(distinct.size(), dsu.components());
        }
    }

    @Test
    void countersCountAllFindsAndOnlySuccessfulUnions() {
        Dsu d = new Dsu(5);
        assertTrue(d.union(0, 1));
        assertTrue(d.union(2, 3));
        assertFalse(d.union(1, 0));
        assertTrue(d.union(1, 2));
        assertFalse(d.union(0, 3));
        assertEquals(3, d.unionCalls);
        assertEquals(0, d.findCalls);

        d.find(0);
        d.find(4);
        assertEquals(2, d.findCalls);
        assertEquals(2, d.components());
    }

    @Test
    void makeSetStartsAgain() {
        Dsu d = new Dsu(3);
        d.union(0, 1);
        d.find(2);
        d.makeSet(10);
        assertEquals(10, d.components());
        assertEquals(0, d.unionCalls);
        assertEquals(0, d.findCalls);
        assertFalse(d.find(0) == d.find(1));
    }

    @Test
    void longChainOfUnionsWorksAndIsOneSet() {
        int n = 100_000;
        Dsu d = new Dsu(n);
        for (int i = 0; i + 1 < n; i++) {
            assertTrue(d.union(i, i + 1));
        }
        assertEquals(1, d.components());
        int root = d.find(0);
        for (int i = 0; i < n; i += 997) {
            assertEquals(root, d.find(i));
        }
    }
}
