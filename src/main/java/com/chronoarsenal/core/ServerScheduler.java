package com.chronoarsenal.core;

import com.chronoarsenal.ChronoArsenalMod;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Tiny "run this later" helper. Everything runs on the server thread. */
public final class ServerScheduler {

    private static final class Task {
        int ticksLeft;
        final Runnable action;

        Task(int ticksLeft, Runnable action) {
            this.ticksLeft = ticksLeft;
            this.action = action;
        }
    }

    private static final List<Task> TASKS = new ArrayList<>();

    private ServerScheduler() {}

    public static void schedule(int delayTicks, Runnable action) {
        TASKS.add(new Task(Math.max(1, delayTicks), action));
    }

    public static void tick() {
        if (TASKS.isEmpty()) {
            return;
        }
        List<Task> due = new ArrayList<>();
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            if (--task.ticksLeft <= 0) {
                due.add(task);
                it.remove();
            }
        }
        for (Task task : due) {
            try {
                task.action.run();
            } catch (Exception e) {
                ChronoArsenalMod.LOGGER.error("Scheduled task failed", e);
            }
        }
    }

    public static void clear() {
        TASKS.clear();
    }
}
