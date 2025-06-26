package mx.bank.wkr.currency_exchange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send-test-email")
    public ResponseEntity<String> sendTestEmail() {
        Map<String, Object> model = new HashMap<>();
        model.put("name", "Jorge");
        model.put("description", "Thanks for signing up!");

        emailService.sendEmailWithTemplate(
                "e_jlarriaga@bancoppel.com",
                "Ia Agent!",
                model
        );
        return ResponseEntity.ok("Email sent");
    }
}