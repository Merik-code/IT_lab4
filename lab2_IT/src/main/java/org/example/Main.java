package org.example;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

class Computer implements Runnable {
    public final AtomicBoolean stop = new AtomicBoolean(false);
    public final BlockingQueue<Process> queue1;
    public final BlockingQueue<Process> queue2;
    private final Processor processor1;
    private final Processor processor2;

    public Computer(int maxSizeQueue1, int maxSizeQueue2) {
        queue1 = new LinkedBlockingQueue<>();
        queue2 = new LinkedBlockingQueue<>();
        processor1 = new Processor("First processor - ", 1, queue1, queue2, maxSizeQueue1, maxSizeQueue2, stop);
        processor2 = new Processor("Second processor - ", 2, queue1, queue2, maxSizeQueue1, maxSizeQueue2, stop);
    }

    @Override
    public void run() {
        processor1.start();
        processor2.start();
    }

    public void join() throws InterruptedException {
        processor1.join();
        processor2.join();
    }

    public void stopAfter(int milliseconds) {
        var scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> stop.set(true), milliseconds, TimeUnit.MILLISECONDS);
        scheduler.shutdown();
    }

    public synchronized int getQueue1ProcessCount() {
        return processor1.getProcessCount();
    }

    public synchronized int getQueue2ProcessCount() {
        return processor2.getProcessCount();
    }
}

class Processor extends Thread {
    private final String name;
    private final int processNumber;
    private final BlockingQueue<Process> queue1;
    private final BlockingQueue<Process> queue2;
    private final int maxSize1;
    private final int maxSize2;
    private final AtomicBoolean stop;
    private Integer processCount = 0;

    public Processor(String name,
                     int processNumber,
                     BlockingQueue<Process> queue1,
                     BlockingQueue<Process> queue2,
                     int maxSize1,
                     int maxSize2,
                     AtomicBoolean stop) {
        this.name = name;
        this.processNumber = processNumber;
        this.queue1 = queue1;
        this.queue2 = queue2;
        this.maxSize1 = maxSize1;
        this.maxSize2 = maxSize2;
        this.stop = stop;
    }

    @Override
    public void run() {
        while (!stop.get()) {
            Process process = null;

            if (processNumber == 1) {
                if (queue1.size() >= maxSize1 && !queue1.isEmpty()) {
                    process = queue1.poll();
                } else if (queue2.size() >= maxSize2 && !queue2.isEmpty()) {
                    process = queue2.poll();
                } else if (!queue1.isEmpty()) {
                    process = queue1.poll();
                }
            } else {
                if (queue2.size() < maxSize2 && !queue2.isEmpty()) {
                    process = queue2.poll();
                }
            }

            if (process != null) {
                incrementProcessCount();
                process.run(name);
            }
        }
    }

    public synchronized int getProcessCount() {
        return this.processCount;
    }

    private synchronized void incrementProcessCount() {
        this.processCount++;
    }
}

class Process {
    private final String queueName;
    private final int id;

    public Process(String queueName, int id) {
        this.queueName = queueName;
        this.id = id;
    }

    public void run(String processorName) {
        System.out.println(processorName + " " + queueName + " " + id);
        try {
            var sleepTime = ThreadLocalRandom.current().nextInt(10, 200);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class ProcessGenerator extends Thread {
    private final AtomicBoolean stop;
    private final String queueName;
    private final BlockingQueue<Process> queue;
    private final int interval;
    public Integer processCount = 0;

    public ProcessGenerator(AtomicBoolean stop,
                            String queueName,
                            BlockingQueue<Process> queue,
                            int interval) {
        this.stop = stop;
        this.queueName = queueName;
        this.queue = queue;
        this.interval = interval;
    }

    @Override
    public void run() {
        for (int i = 0; !stop.get(); i++) {
            queue.add(new Process(queueName, i));
            processCount += 1;
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        var computer = new Computer(5, 40);

        var generator1 = new ProcessGenerator(computer.stop, "First queue", computer.queue1, 50);
        var generator2 = new ProcessGenerator(computer.stop, "Second queue", computer.queue2, 100);

        computer.run();
        generator1.start();
        generator2.start();

        computer.stopAfter(1000);

        generator1.join();
        generator2.join();
        computer.join();

        var totalCount = generator1.processCount + generator2.processCount;

        System.out.println("Total: " + totalCount);
        System.out.println("1: " + GetPercentage(computer.getQueue1ProcessCount(), totalCount)+ "%");
        System.out.println("2: " + GetPercentage(computer.getQueue2ProcessCount(), totalCount)+ "%");
    }

    public static double GetPercentage(int count, int totalCount) {
        return Math.round((count / (double) totalCount) * 100);
    }
}
