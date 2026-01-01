package com.uhm.uhmcs.popupwindow;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lxj.xpopup.core.CenterPopupView;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.utils.EcrProtocol;
import com.uhm.uhmcs.utils.EcrSocketManager;
import com.uhm.uhmcs.utils.UserUtils;

import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NETS POS 调试与设置弹窗 - 完整复刻版
 */
public class PosSettingPopupWindow extends CenterPopupView {

    private EditText etIp, etPort, etDataInput;
    private TextView tvLog;
    private OnSettingsChangeListener listener;
    private final ExecutorService commExecutor = Executors.newSingleThreadExecutor();
    private final EcrSocketManager socketManager = new EcrSocketManager();

    public interface OnSettingsChangeListener {
        void onSaveAndCheck();
    }

    public PosSettingPopupWindow(Context context, OnSettingsChangeListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_pos_settings;
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        etIp = findViewById(R.id.et_pos_ip);
        etPort = findViewById(R.id.et_pos_port);
        etDataInput = findViewById(R.id.et_ecr_data_input);
        tvLog = findViewById(R.id.tv_response_log);

        // 1. 初始化回显数据
        etIp.setText(UserUtils.getInstance().getEcrIp());
        etPort.setText(String.valueOf(UserUtils.getInstance().getEcrPort()));

        // 2. 绑定所有快捷按钮逻辑 (包含新增的信用卡功能)
        setupQuickButtons();

        // 3. 发送指令按钮
        findViewById(R.id.btn_send_command).setOnClickListener(v -> sendTestCommand());

        // 4. 完成并保存按钮
        findViewById(R.id.btn_pos_save).setOnClickListener(v -> {
            saveCurrentSettings();
            if (listener != null) listener.onSaveAndCheck();
            dismiss();
        });
    }

    /**
     * 绑定快捷按钮点击事件，点击后自动填充命令关键词
     */
    private void setupQuickButtons() {
        // NETS 系列
        findViewById(R.id.btn_q_status).setOnClickListener(v -> etDataInput.setText("STATUS"));
        findViewById(R.id.btn_q_logon).setOnClickListener(v -> etDataInput.setText("LOGON"));
        findViewById(R.id.btn_q_purchase).setOnClickListener(v -> etDataInput.setText("NETSPURCHASE"));
        findViewById(R.id.btn_q_qr).setOnClickListener(v -> etDataInput.setText("NETSQR"));
        findViewById(R.id.btn_q_void).setOnClickListener(v -> etDataInput.setText("VOID"));
        findViewById(R.id.btn_q_settle).setOnClickListener(v -> etDataInput.setText("SETTLE"));

        // 信用卡系列 (新增)
        findViewById(R.id.btn_q_sale).setOnClickListener(v -> etDataInput.setText("SALE"));
        findViewById(R.id.btn_q_cc_refund).setOnClickListener(v -> etDataInput.setText("CC_REFUND"));
        findViewById(R.id.btn_q_cc_settle).setOnClickListener(v -> etDataInput.setText("CC_SETTLE"));
    }

    /**
     * 执行 Socket 指令发送
     */
    private void sendTestCommand() {
        String ip = etIp.getText().toString().trim();
        String portStr = etPort.getText().toString().trim();
        String input = etDataInput.getText().toString().toUpperCase().trim();

        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(portStr) || TextUtils.isEmpty(input)) {
            Toast.makeText(getContext(), "请输入完整参数", Toast.LENGTH_SHORT).show();
            return;
        }

        tvLog.setText("📡 正在连接 " + ip + ":" + portStr + " 并执行指令: " + input + "...");

        commExecutor.execute(() -> {
            try {
                byte[] commandBytes;
                String testRef = "T" + System.currentTimeMillis() / 1000;

                // 模拟 Kotlin when 逻辑的指令构造分支
                switch (input) {
                    case "STATUS":
                        commandBytes = EcrSocketManager.statusCommand();
                        break;
                    case "LOGON":
                        commandBytes = EcrSocketManager.logonCommand();
                        break;
                    case "NETSPURCHASE":
                        commandBytes = EcrSocketManager.netsPurchaseCommand(10, testRef, false);
                        break;
                    case "NETSQR":
                        commandBytes = EcrSocketManager.netsPurchaseCommand(10, testRef, true);
                        break;
                    case "SALE":
                        commandBytes = EcrSocketManager.creditCardSaleCommand(10, testRef);
                        break;
                    case "VOID":
                        commandBytes = EcrSocketManager.netsVoidCommand("123456", testRef);
                        break;
                    case "SETTLE":
                        // NETS 结算 (81)
                        commandBytes = new EcrProtocol.EcrPacketBuilder("000000000001", "81").build();
                        break;
                    case "CC_REFUND":
                        // 信用卡退款 (I4) - 金额固定测试 0.10
                        commandBytes = new EcrProtocol.EcrPacketBuilder("000000000001", "I4")
                                .addField("40", String.format(Locale.US, "%012d", 10L))
                                .addField("HD", "REF" + (System.currentTimeMillis() / 1000))
                                .build();
                        break;
                    case "CC_SETTLE":
                        // 信用卡结算 (I5)
                        commandBytes = new EcrProtocol.EcrPacketBuilder("000000000001", "I5").build();
                        break;
                    default:
                        // 如果不是预定义的关键词，则尝试将输入作为 Hex 字符串解析
                        commandBytes = EcrProtocol.hexToBinary(input);
                }

                // 执行通信
                Map<String, String> result = socketManager.executeCommand(ip, Integer.parseInt(portStr), commandBytes);

                // 更新 UI 日志
                post(() -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("--- 终端响应详情 ---\n");
                    for (Map.Entry<String, String> entry : result.entrySet()) {
                        sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                    tvLog.setText(sb.toString());
                });

            } catch (Exception e) {
                post(() -> tvLog.setText("❌ 通信失败: " + e.getMessage()));
                Log.e("ECR_DEBUG", "Command error", e);
            }
        });
    }

    /**
     * 保存设置到 UserUtils
     */
    private void saveCurrentSettings() {
        String ip = etIp.getText().toString().trim();
        String portStr = etPort.getText().toString().trim();
        if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(portStr)) {
            try {
                UserUtils.getInstance().setEcrIp(getContext(), ip);
                UserUtils.getInstance().setEcrPort(getContext(), Integer.parseInt(portStr));
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "端口格式不正确", Toast.LENGTH_SHORT).show();
            }
        }
    }
}