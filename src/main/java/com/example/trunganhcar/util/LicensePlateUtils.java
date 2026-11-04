package com.example.trunganhcar.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiện ích chuẩn hóa và định dạng Biển kiểm soát (BKS) xe ô tô / xe cơ giới
 * Định dạng chuẩn: Ví dụ "35H - 182.89", "30K - 868.86"
 */
public class LicensePlateUtils {

    // Pattern biển 5 số: 2 số tỉnh + 1-2 chữ cái sê-ri + 5 chữ số (VD: 35H18289, 30K86886, 29LD12345)
    private static final Pattern PLATE_5_DIGITS = Pattern.compile("^([0-9]{2}[A-Z]{1,2})([0-9]{5})$");

    // Pattern biển 4 số cũ: 2 số tỉnh + 1-2 chữ cái sê-ri + 4 chữ số (VD: 29A1234, 30L9999)
    private static final Pattern PLATE_4_DIGITS = Pattern.compile("^([0-9]{2}[A-Z]{1,2})([0-9]{4})$");

    // Pattern tổng quát cho biển có chữ số phụ (VD: 30F1 - 182.89)
    private static final Pattern PLATE_GENERAL_5 = Pattern.compile("^([0-9]{2}[A-Z0-9]{1,3})([0-9]{5})$");
    private static final Pattern PLATE_GENERAL_4 = Pattern.compile("^([0-9]{2}[A-Z0-9]{1,3})([0-9]{4})$");

    /**
     * Tự động format BKS thành định dạng chuẩn như: "35H - 182.89", "30K - 868.86"
     */
    public static String formatLicensePlate(String rawPlate) {
        if (rawPlate == null || rawPlate.isBlank()) {
            return rawPlate;
        }

        // Loại bỏ mọi ký tự thừa: khoảng trắng, dấu gạch ngang, dấu chấm và chuyển hoa
        String clean = rawPlate.trim().toUpperCase().replaceAll("[\\s\\-\\.]", "");

        // 1. Khớp biển 5 số chuẩn ô tô (VD: 35H18289 -> 35H - 182.89, 29LD12345 -> 29LD - 123.45)
        Matcher matcher5 = PLATE_5_DIGITS.matcher(clean);
        if (matcher5.matches()) {
            String prefix = matcher5.group(1);
            String digits = matcher5.group(2);
            return prefix + " - " + digits.substring(0, 3) + "." + digits.substring(3);
        }

        // 2. Khớp biển 4 số cũ (VD: 29A1234 -> 29A - 1234)
        Matcher matcher4 = PLATE_4_DIGITS.matcher(clean);
        if (matcher4.matches()) {
            String prefix = matcher4.group(1);
            String digits = matcher4.group(2);
            return prefix + " - " + digits;
        }

        // 3. Khớp biển mở rộng 5 số
        Matcher general5 = PLATE_GENERAL_5.matcher(clean);
        if (general5.matches()) {
            String prefix = general5.group(1);
            String digits = general5.group(2);
            return prefix + " - " + digits.substring(0, 3) + "." + digits.substring(3);
        }

        // 4. Khớp biển mở rộng 4 số
        Matcher general4 = PLATE_GENERAL_4.matcher(clean);
        if (general4.matches()) {
            String prefix = general4.group(1);
            String digits = general4.group(2);
            return prefix + " - " + digits;
        }

        // Nếu không khớp pattern nào, trả về chuỗi đã uppercase và trim
        return rawPlate.trim().toUpperCase();
    }
}

