/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.periphery.demo;

import com.codeferm.periphery.Pwm;
import static com.codeferm.periphery.Pwm.PWM_SUCCESS;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Flash LED with PWM.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@Command(name = "ledflash", mixinStandardHelpOptions = true, version = "ledflash 1.0.0",
        description = "Flash LED with PWM.")
public class LedFlash implements Callable<Integer> {

    /**
     * Logger.
     */
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(LedFlash.class);
    /**
     * Chip option.
     */
    @Option(names = {"-d", "--device"}, description = "PWM device defaults to 0")
    private int chip = 0;
    /**
     * Line option.
     */
    @Option(names = {"-c", "--channel"}, description = "PWM pin defaults to 0 DEBUG_RX(UART_RXD0)/GPIOA5/PWM0 NanoPi Duo")
    private int channel = 0;

    /**
     * Main parsing, error handling and handling user requests for usage help or version help are done with one line of code.
     *
     * @param args Argument list.
     */
    public static void main(String... args) {
        var exitCode = new CommandLine(new LedFlash()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Gradually increase and decrease LED brightness.
     * 
     * @param handle Valid pointer to an allocated LED handle structure.
     * @param period Set the period in seconds of the PWM.
     * @param startDc Starting duty cycle in nanoseconds.
     * @param dcInc Duty cycle increment in nanoseconds.
     * @param count Number of times to loop.
     * @param sleepTime Sleep time in microseconds.
     * @throws InterruptedException Possible exception.
     */
    public void changeBrightness(final long handle, final int period, final int startDc, final int dcInc, final int count,
            final int sleepTime) throws InterruptedException {
        Pwm.pwmSetPeriodNs(handle, period);
        var dutyCycle = startDc;
        var i = 0;
        while (i < count) {
            Pwm.pwmSetDutyCycleNs(handle, dutyCycle);
            TimeUnit.MICROSECONDS.sleep(sleepTime);
            dutyCycle += dcInc;
            i += 1;
        }
    }

    /**
     * Flash LED.
     *
     * @return Exit code.
     * @throws InterruptedException Possible exception.
     */
    @Override
    public Integer call() throws InterruptedException {
        var exitCode = 0;
        final var handle = Pwm.pwmNew();
        if (Pwm.pwmOpen(handle, chip, channel) == PWM_SUCCESS) {
            logger.info("Flash LED");
            Pwm.pwmEnable(handle);
            for (var i = 0; i < 10; i++) {
                changeBrightness(handle, 1000, 0, 10, 100, 5000);
                changeBrightness(handle, 1000, 1000, -10, 100, 5000);
            }
            Pwm.pwmSetDutyCycleNs(handle, 0);
            Pwm.pwmSetPeriod(handle, 0);
            Pwm.pwmDisable(handle);
            Pwm.pwmClose(handle);
        } else {
            exitCode = Pwm.pwmErrNo(handle);
            logger.error(Pwm.pwmErrMessage(handle));
        }
        Pwm.pwmFree(handle);
        return exitCode;
    }
}
