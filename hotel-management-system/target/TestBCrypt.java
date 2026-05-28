import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$EqKcp1WFKVQIShMPC7B3kuznX9gAZMsVnSNjN0ABNuHVBCpzqABae";
        String password = "admin123";
        System.out.println("Matches: " + encoder.matches(password, hash));
        System.out.println("New hash: " + encoder.encode("admin123"));
    }
}
