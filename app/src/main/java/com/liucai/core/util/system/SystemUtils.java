package com.liucai.core.util.system;

import java.io.File;

/**
 * @author liucai
 */
public class SystemUtils {

    private static final String[] KNOWN_FILES = {
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"
    };

    public static Boolean CheckEmulatorBuild() {
        String board = android.os.Build.BOARD;
        String bootloader = android.os.Build.BOOTLOADER;
        String brand = android.os.Build.BRAND;
        String device = android.os.Build.DEVICE;
        String hardware = android.os.Build.HARDWARE;
        String model = android.os.Build.MODEL;
        String product = android.os.Build.PRODUCT;

        return equalsIgnoreCase(board, "unknown")
                || equalsIgnoreCase(bootloader, "unknown")
                || equalsIgnoreCase(brand, "generic")
                || equalsIgnoreCase(device, "generic")
                || equalsIgnoreCase(model, "sdk")
                || equalsIgnoreCase(product, "sdk")
                || equalsIgnoreCase(hardware, "goldfish");
    }

    /**
     * 检测模拟器特有文件。
     */
    public static Boolean CheckEmulatorFiles() {
        for (String fileName : KNOWN_FILES) {
            File qemuFile = new File(fileName);
            if (qemuFile.exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean equalsIgnoreCase(String value, String target) {
        return value != null && value.equalsIgnoreCase(target);
    }
}