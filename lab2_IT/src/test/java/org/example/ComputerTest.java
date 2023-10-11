package org.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class ComputerTest {

    // Цей тест перевіряє, чи комп'ютер обробляє процеси в першій черзі.
    @Test
    public void testQueue1ProcessCount() {
        // Ініціалізуємо комп'ютер з максимальними розмірами черг 5 і 40.
        Computer computer = new Computer(5, 40);

        // Ініціалізуємо два генератори процесів для двох черг.
        ProcessGenerator generator1 = new ProcessGenerator(computer.stop, "First queue", computer.queue1, 50);
        ProcessGenerator generator2 = new ProcessGenerator(computer.stop, "Second queue", computer.queue2, 100);

        // Запускаємо генератори процесів і комп'ютер.
        generator1.start();
        generator2.start();
        computer.run();

        // Очікуємо півсекунди перед отриманням результатів.
        try {
            Thread.sleep(500L);
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }

        // Отримуємо кількість оброблених процесів в першій черзі.
        int result = computer.getQueue1ProcessCount();

        // Перевіряємо, чи оброблена хоча б одна задача.
        assertTrue(result > 0);
    }

    // Цей тест перевіряє, чи комп'ютер обробляє процеси в другій черзі.
    @Test
    public void testQueue2ProcessCount() {
        Computer computer = new Computer(5, 40);
        ProcessGenerator generator1 = new ProcessGenerator(computer.stop, "First queue", computer.queue1, 50);
        ProcessGenerator generator2 = new ProcessGenerator(computer.stop, "Second queue", computer.queue2, 100);

        generator1.start();
        generator2.start();
        computer.run();

        try {
            Thread.sleep(500L);
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }

        int result = computer.getQueue2ProcessCount();

        assertTrue(result > 0);
    }

    // Цей тест перевіряє, чи комп'ютер зупиняється після певного часу.
    @Test
    public void testStopAfter() {
        Computer computer = new Computer(5, 40);
        computer.run();

        // Командуємо комп'ютеру зупинитися після 100 мс.
        computer.stopAfter(100);

        // Очікуємо 150 мс, щоб впевнитися, що комп'ютер має час зупинитися.
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Перевіряємо, чи комп'ютер зупинився.
        assertTrue(computer.stop.get());
    }

    // Цей тест перевіряє правильність обчислення відсотків у головній програмі.
    @Test
    public void testGetPercentage() {
        double result = Main.GetPercentage(25, 100);
        // Перевіряємо, чи обчислення правильні з точністю до двох знаків після коми.
        assertEquals(25.0, result, 0.01);
    }
}
