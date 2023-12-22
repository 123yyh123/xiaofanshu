package com.yyh.xfs.common.utils;

import java.time.LocalDate;
import java.time.Period;

/**
 * @author yyh
 * @date 2023-12-22
 */
public class TimeUtil {
    public static int calculateAge(LocalDate birthDate) {
        LocalDate currentDate = LocalDate.now();
        if (birthDate != null) {
            return Period.between(birthDate, currentDate).getYears();
        } else {
            return 0;
        }
    }
}
