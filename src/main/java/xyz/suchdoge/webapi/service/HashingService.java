package xyz.suchdoge.webapi.service;

import com.google.common.hash.Hashing;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class HashingService {
    public String hashString(String string) {
        return Hashing.sha256().hashString(string, StandardCharsets.UTF_8).toString();
    }
}
