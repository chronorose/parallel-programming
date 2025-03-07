package org.nsu.syspro.parprog;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nsu.syspro.parprog.base.DefaultFork;
import org.nsu.syspro.parprog.base.DiningTable;
import org.nsu.syspro.parprog.examples.DefaultPhilosopher;
import org.nsu.syspro.parprog.helpers.TestLevels;
import org.nsu.syspro.parprog.interfaces.Fork;

public class CustomSchedulingTest extends TestLevels {

    static final class CustomizedPhilosopher extends DefaultPhilosopher {
        @Override
        public void onHungry(Fork left, Fork right) {
            sleepMillis(this.id * 20);
            System.out.println(Thread.currentThread() + " " + this + ": onHungry");
            super.onHungry(left, right);
        }
    }

    static final class CustomizedFork extends DefaultFork {
        @Override
        public void acquire() {
            System.out.println(Thread.currentThread() + " trying to acquire " + this);
            super.acquire();
            System.out.println(Thread.currentThread() + " acquired " + this);
            sleepMillis(100);
        }
    }

    static final class VeryCustomizedPhilosopher extends DefaultPhilosopher {
        private boolean visited = false;

        @Override
        public void onHungry(Fork left, Fork right) {
            if (id == 0 && !visited) {
                left.acquire();
                right.acquire();
                sleepSeconds(1);
                left.release();
                right.release();
                visited = true;
            }
            super.onHungry(left, right);
        }
    }

    static final class WeakPhilosopher extends DefaultPhilosopher {

        @Override
        public void onHungry(Fork left, Fork right) {
            if ((id & 1) == 0) {
                sleepMillis(1);
            } else {
                sleepMillis(10);
            }
            super.onHungry(left, right);
        }
    }

    static final class WeakTable extends DiningTable<WeakPhilosopher, DefaultFork> {
        public WeakTable(int N) {
            super(N);
        }

        @Override
        public DefaultFork createFork() {
            return new DefaultFork();
        }

        @Override
        public WeakPhilosopher createPhilosopher() {
            return new WeakPhilosopher();
        }

    }

    static final class CustomizedTable extends DiningTable<CustomizedPhilosopher, CustomizedFork> {
        public CustomizedTable(int N) {
            super(N);
        }

        @Override
        public CustomizedFork createFork() {
            return new CustomizedFork();
        }

        @Override
        public CustomizedPhilosopher createPhilosopher() {
            return new CustomizedPhilosopher();
        }
    }

    static final class VeryCustomizedTable extends DiningTable<VeryCustomizedPhilosopher, DefaultFork> {
        public VeryCustomizedTable(int N) {
            super(N);
        }

        @Override
        public DefaultFork createFork() {
            return new DefaultFork();
        }

        @Override
        public VeryCustomizedPhilosopher createPhilosopher() {
            return new VeryCustomizedPhilosopher();
        }

    }

    @EnabledIf("easyEnabled")
    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 5 })
    @Timeout(2)
    void testDeadlockFreedom(int N) {
        final CustomizedTable table = dine(new CustomizedTable(N), 1);
    }

    @EnabledIf("easyEnabled")
    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 5 })
    @Timeout(3)
    void testSingleSlow(int N) {
        final VeryCustomizedTable table = dine(new VeryCustomizedTable(N), 2);
        assert (table.maxMeals() >= 1000);
    }

    @EnabledIf("mediumEnabled")
    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 5 })
    @Timeout(2)
    void testWeakFairness(int N) {
        final WeakTable table = dine(new WeakTable(N), 1);
        assert (table.minMeals() > 0); // every philosopher eat at least once
    }

}
