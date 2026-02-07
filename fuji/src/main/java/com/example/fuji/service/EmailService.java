package com.example.fuji.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) {
        log.info("📧 Đang gửi OTP đến email: {}", toEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("nguyennhat082004@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("🔐 Mã xác thực OTP - FUJI");
            
            // Dùng plain text thay vì HTML để test
            String plainText = String.format("""
                FUJI - Nền tảng học tiếng Nhật số 1 Việt Nam
                
                Xin chào!
                
                Mã xác thực OTP của bạn là: %s
                
                Mã này sẽ hết hạn sau 5 phút.
                Vui lòng không chia sẻ mã này với bất kỳ ai.
                
                Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.
                
                ---
                © 2026 FUJI. All rights reserved.
                """, otpCode);
            
            helper.setText(plainText, false);
            
            mailSender.send(message);
            log.info("✅ Email OTP đã gửi thành công đến: {} - OTP: {}", toEmail, otpCode);
        } catch (MessagingException e) {
            log.error("❌ Lỗi khi gửi email OTP đến {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email xác thực. Vui lòng thử lại sau.");
        } catch (Exception e) {
            log.error("❌ Lỗi không xác định khi gửi email: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email xác thực. Vui lòng thử lại sau.");
        }
    }
    
    private String buildOtpEmailTemplate(String otpCode) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Mã xác thực OTP</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);">
                <table role="presentation" style="width: 100%%; border-collapse: collapse; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);">
                    <tr>
                        <td align="center" style="padding: 40px 20px;">
                            <!-- Main Card -->
                            <table role="presentation" style="width: 100%%; max-width: 600px; background: white; border-radius: 20px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); overflow: hidden;">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                        <div style="display: inline-flex; align-items: center; justify-content: center; width: 70px; height: 70px; background: rgba(255,255,255,0.2); border-radius: 15px; margin-bottom: 20px; backdrop-filter: blur(10px);">
                                            <span style="font-size: 40px; color: white;">🏔️</span>
                                        </div>
                                        <h1 style="margin: 0; color: white; font-size: 32px; font-weight: 800; letter-spacing: 2px;">FUJI</h1>
                                        <p style="margin: 10px 0 0 0; color: rgba(255,255,255,0.9); font-size: 14px; font-weight: 500;">Nền tảng học tiếng Nhật số 1 Việt Nam</p>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="margin: 0 0 20px 0; color: #1a202c; font-size: 24px; font-weight: 700;">Xác thực tài khoản</h2>
                                        <p style="margin: 0 0 30px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                            Chào bạn! 👋<br><br>
                                            Cảm ơn bạn đã đăng ký tài khoản FUJI. Để hoàn tất đăng ký, vui lòng sử dụng mã OTP bên dưới:
                                        </p>
                                        
                                        <!-- OTP Box -->
                                        <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); border-radius: 15px; padding: 30px; text-align: center; margin: 30px 0;">
                                            <p style="margin: 0 0 10px 0; color: rgba(255,255,255,0.9); font-size: 14px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;">Mã xác thực của bạn</p>
                                            <div style="background: white; border-radius: 10px; padding: 20px; display: inline-block;">
                                                <span style="font-size: 36px; font-weight: 800; color: #667eea; letter-spacing: 8px; font-family: 'Courier New', monospace;">%s</span>
                                            </div>
                                        </div>
                                        
                                        <!-- Warning Box -->
                                        <div style="background: #fff5f5; border-left: 4px solid #fc8181; padding: 15px 20px; border-radius: 8px; margin: 30px 0;">
                                            <p style="margin: 0; color: #c53030; font-size: 14px; line-height: 1.6;">
                                                ⏰ <strong>Lưu ý:</strong> Mã OTP này sẽ hết hạn sau <strong>5 phút</strong>. Vui lòng không chia sẻ mã này với bất kỳ ai.
                                            </p>
                                        </div>
                                        
                                        <p style="margin: 30px 0 0 0; color: #718096; font-size: 14px; line-height: 1.6;">
                                            Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background: #f7fafc; padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                                        <p style="margin: 0 0 10px 0; color: #4a5568; font-size: 14px; font-weight: 600;">
                                            FUJI - Học tiếng Nhật dễ dàng 🇯🇵
                                        </p>
                                        <p style="margin: 0; color: #a0aec0; font-size: 12px;">
                                            © 2026 FUJI. All rights reserved.
                                        </p>
                                        <div style="margin-top: 15px;">
                                            <a href="#" style="color: #667eea; text-decoration: none; margin: 0 10px; font-size: 12px;">Điều khoản</a>
                                            <span style="color: #cbd5e0;">|</span>
                                            <a href="#" style="color: #667eea; text-decoration: none; margin: 0 10px; font-size: 12px;">Bảo mật</a>
                                            <span style="color: #cbd5e0;">|</span>
                                            <a href="#" style="color: #667eea; text-decoration: none; margin: 0 10px; font-size: 12px;">Hỗ trợ</a>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(otpCode);
    }
}