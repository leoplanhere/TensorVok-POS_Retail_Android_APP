package com.uhm.uhmcs.utils;

import android.text.TextUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 全球化货币工具类 - 2026 优化版
 * 逻辑：所有货币配置集中在 getCurrencyList()，后续新增币种只需在该方法加一行。
 */
public class CurrencyUtils {

    /**
     * 货币实体类，包含了UI显示、逻辑代码和打印缩写
     */
    public static class CurrencyBean {
        public String name;    // 弹窗显示的名称，如 "泰铢 (THB)"
        public String code;    // 业务代码，存储在系统中的 ID，如 "THB"
        public String symbol;  // 手机 App 界面显示的精美符号，如 "฿"
        public String print;   // 打印机专用缩写，防止乱码，如 "THB"

        public CurrencyBean(String name, String code, String symbol, String print) {
            this.name = name;
            this.code = code;
            this.symbol = symbol;
            this.print = print;
        }
    }

    /**
     * 【配置中心】获取系统支持的所有货币列表
     * 💡 后续需要增加货币（如日元、越南盾），只需在这里 add 一行即可！
     */
    public static List<CurrencyBean> getCurrencyList() {
        List<CurrencyBean> list = new ArrayList<>();
        //                   显示名称          代码    UI符号   打印缩写
        list.add(new CurrencyBean("美元 (USD)",     "USD",  "$",    "$"));
        list.add(new CurrencyBean("迪拉姆 (AED)",   "AED",  "AED",  "AED"));
        list.add(new CurrencyBean("泰铢 (THB)",     "THB",  "฿",    "THB"));
        list.add(new CurrencyBean("卢布 (RUB)",     "RUB",  "₽",    "RUB"));
        list.add(new CurrencyBean("新加坡元 (SGD)", "SGD",  "S$",   "S$"));
        list.add(new CurrencyBean("马币 (MYR)",     "MYR",  "RM",   "MYR"));
        list.add(new CurrencyBean("欧元 (EUR)",     "EUR",  "€",    "EUR"));
        list.add(new CurrencyBean("英镑 (GBP)",     "GBP",  "£",    "GBP"));
        list.add(new CurrencyBean("人民币 (CNY)",   "CNY",  "￥",   "￥"));


        // 示例：未来新增日元
        // list.add(new CurrencyBean("日元 (JPY)", "JPY", "¥", "JPY"));

        return list;
    }

    /**
     * 获取手机界面显示的符号 (用于 UI)
     */
    public static String getSymbol() {
        String type = UserUtils.getInstance().getCurrencyType().toUpperCase();
        if (TextUtils.isEmpty(type)) return "￥";

        for (CurrencyBean bean : getCurrencyList()) {
            if (bean.code.equals(type)) {
                return bean.symbol;
            }
        }
        return "￥"; // 找不到匹配项时的默认值
    }

    /**
     * 获取打印机专用的缩写 (用于小票打印，解决乱码)
     */
    public static String getPrinterSymbol() {
        String type = UserUtils.getInstance().getCurrencyType().toUpperCase();
        if (TextUtils.isEmpty(type)) return "RMB";

        for (CurrencyBean bean : getCurrencyList()) {
            if (bean.code.equals(type)) {
                return bean.print;
            }
        }
        return type; // 找不到匹配项时返回原始代码（如 USD）
    }

    /**
     * 格式化金额显示：符号 + 数值
     */
    public static String format(Object amount) {
        String symbol = getSymbol();
        if (amount == null || TextUtils.isEmpty(String.valueOf(amount))) {
            return symbol + "0.00";
        }
        try {
            BigDecimal value = new BigDecimal(String.valueOf(amount));
            // 统一保留两位小数，向下取整
            String formattedValue = value.setScale(2, RoundingMode.DOWN).toString();
            return symbol + formattedValue;
        } catch (Exception e) {
            return symbol + "0.00";
        }
    }
}