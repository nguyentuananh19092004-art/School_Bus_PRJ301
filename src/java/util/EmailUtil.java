package util;

import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailUtil {

    private static final String FROM_EMAIL = "nguyentuananh19092004@gmail.com";
    private static final String PASSWORD = "hwxvgkcblekrjmaa";

    public static boolean sendOTP(String toEmail, String otp) {
        boolean test = false;

        String host = "smtp.gmail.com";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Mã OTP khôi phục mật khẩu - School Bus System");
            message.setText("Chào bạn,\n\nBạn đã yêu cầu khôi phục mật khẩu. Mã OTP của bạn là: " + otp + "\n\nVui lòng không chia sẻ mã này cho bất kỳ ai.\n\nTrân trọng,\nHệ thống School Bus");

            Transport.send(message);
            test = true;
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
        }

        return test;
    }

    public static boolean isValidDomain(String email, String allowedDomainsStr) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Bỏ qua nếu email trống
        }
        if (allowedDomainsStr == null || allowedDomainsStr.trim().isEmpty()) {
            return true; // Không có cấu hình domain thì cho qua
        }
        String[] domains = allowedDomainsStr.split(",");
        for (String domain : domains) {
            if (email.endsWith(domain.trim())) {
                return true;
            }
        }
        return false;
    }
}
